package services; // Defines the package for logic dispatching

import controllers.UserController; // Imports the controller for user operations
import models.User; // Imports the user data model
import com.google.gson.JsonArray; // Handles JSON arrays for AI responses
import com.google.gson.JsonObject; // Handles JSON objects for AI requests/responses

import java.io.File; // For file operations (exports)
import java.sql.SQLDataException; // Exception for database errors
import java.time.LocalDateTime; // For generating timestamps
import java.time.format.DateTimeFormatter; // For formatting file names
import java.util.List; // For handling lists of users
import java.util.function.Consumer; // Functional interface for UI callbacks

/**
 * Bridges Openrouter's function call requests to the actual UserController methods.
 * Each supported function name is dispatched to the corresponding CRUD operation.
 */
public class FunctionCallDispatcher { // Class that connects AI tools to Java logic

    private final UserController userController; // Controller instance for core logic
    private final ServiceUser serviceUser; // Direct service access for some queries
    private final ChatHistoryService historyService; // Service for chat history retrieval
    private final AdminLogService adminLogService; // Service for audit log retrieval

    /** Callback to notify the UI that user data has changed (so tables refresh). */
    private Runnable onDataChanged; // Event hook for data updates
    /** Callback to notify the UI to perform an action (open profile, etc.) */
    private Consumer<String> uiActionCallback; // Event hook for navigation

    public FunctionCallDispatcher() { // Constructor
        this.userController = new UserController(); // Initializes user controller
        this.serviceUser = new ServiceUser(); // Initializes user service
        this.historyService = new ChatHistoryService(); // Initializes history service
        this.adminLogService = new AdminLogService(); // Initializes logging service
    } // End of constructor

    public void setOnDataChanged(Runnable onDataChanged) { // Setter for data refresh callback
        this.onDataChanged = onDataChanged; // Assigns the callback
    } // End of setOnDataChanged

    public void setUiActionCallback(Consumer<String> callback) { // Setter for UI navigation callback
        this.uiActionCallback = callback; // Assigns the callback
    } // End of setUiActionCallback

    /**
     * Dispatches a function call from Openrouter to the appropriate UserController method.
     *
     * @param functionName The name of the function Openrouter wants to call
     * @param args         The arguments Openrouter extracted from the user's message
     * @return A JsonObject with {status, message} describing the result
     */
    public JsonObject dispatch(String functionName, JsonObject args) { // Main routing method
        try { // Error handling block
            switch (functionName) { // Branches based on function name
                case "create_user": // Handle user creation
                    return handleCreateUser(args); // Calls creation handler
                case "delete_user": // Handle user deletion
                    return handleDeleteUser(args); // Calls deletion handler
                case "update_user": // Handle user updates
                    return handleUpdateUser(args); // Calls update handler
                case "list_all_users": // Handle full listing
                    return handleListAllUsers(); // Calls listing handler
                case "search_users": // Handle filtered search
                    return handleSearchUsers(args); // Calls search handler
                case "get_user_statistics": // Handle stats request
                    return handleGetStatistics(); // Calls stats handler
                case "update_user_status": // Handle block/unblock
                    return handleUpdateUserStatus(args); // Calls status handler
                case "export_users": // Handle file exports
                    return handleExportUsers(args); // Calls export handler
                case "trigger_ui_action": // Handle UI navigation
                    return handleUiAction(args); // Calls UI handler
                case "get_database_schema": // Handle technical info request
                    return handleGetDatabaseSchema(); // Calls schema handler
                case "get_user_history": // Handle memory request
                    return handleGetUserHistory(); // Calls history handler
                case "get_admin_logs": // Handle audit request
                    return handleGetAdminLogs(); // Calls log handler
                default: // Fallback for unknown functions
                    return errorResult("Unknown function: " + functionName); // Returns error
            } // End of switch
        } catch (Exception e) { // Catches any execution errors
            e.printStackTrace(); // Logs error to console
            return errorResult("Execution error: " + e.getMessage()); // Returns formatted error to AI
        } // End of try-catch
    } // End of dispatch

    private JsonObject handleCreateUser(JsonObject args) throws SQLDataException { // Creation handler logic
        String email = getStringOrNull(args, "email"); // Extracts email
        String password = getStringOrNull(args, "password"); // Extracts password
        String firstName = getStringOrNull(args, "firstName"); // Extracts first name
        String lastName = getStringOrNull(args, "lastName"); // Extracts last name (optional)
        String role = getStringOrNull(args, "role"); // Extracts role

        if (email == null || password == null || firstName == null || role == null) { // Validation
            return errorResult("Missing required fields. Need: email, password, firstName, role."); // Error response
        } // End of validation

        // Check if email already exists
        if (serviceUser.emailExists(email)) { // Uniqueness check
            return errorResult("A user with email '" + email + "' already exists."); // Error if duplicate
        } // End of check

        User user = new User(email, "", password, firstName, lastName); // Creates user object
        userController.createUser(user, role); // Saves user via controller
        notifyDataChanged(); // Triggers UI refresh

        JsonObject result = new JsonObject(); // Success response container
        result.addProperty("status", "success"); // Sets status
        result.addProperty("message", "User '" + firstName +
                (lastName != null ? " " + lastName : "") +
                "' created successfully with email " + email + " and role " + role + "."); // Sets success message
        return result; // Returns result
    } // End of handleCreateUser

    private JsonObject handleDeleteUser(JsonObject args) throws SQLDataException { // Deletion handler logic
        String searchAttribute = getStringOrNull(args, "searchAttribute"); // Extracts search key

        if (searchAttribute == null) { // Validation
            return errorResult("Missing required field: searchAttribute (provide an email, name, or ID)."); // Error if missing
        } // End of validation

        List<User> matchingUsers = userController.searchAndSortUsers(searchAttribute, "id", true); // Finds the user
        if (matchingUsers.isEmpty()) { // Check if found
            return errorResult("No user found matching '" + searchAttribute + "'."); // Error if none
        } // End of check
        if (matchingUsers.size() > 1) { // Check for ambiguity
            return errorResult("Multiple users found matching '" + searchAttribute + "'. Please ask the user to provide a more specific identifier like an exact email or ID."); // Error if ambiguous
        } // End of check

        User user = matchingUsers.get(0); // Takes the single matching user
        userController.deleteUserById(user.getId()); // Executes deletion
        notifyDataChanged(); // Triggers UI refresh

        JsonObject result = new JsonObject(); // Success response container
        result.addProperty("status", "success"); // Sets status
        result.addProperty("message", "User '" + user.getFirstName() +
                (user.getLastName() != null ? " " + user.getLastName() : "") +
                "' (Email: " + user.getEmail() + ", ID: " + user.getId() + ") has been deleted."); // Success message
        return result; // Returns result
    } // End of handleDeleteUser

    private JsonObject handleUpdateUser(JsonObject args) throws SQLDataException { // Update handler logic
        String searchAttribute = getStringOrNull(args, "searchAttribute"); // Extracts target key

        if (searchAttribute == null) { // Validation
            return errorResult("Missing required field: searchAttribute (to identify the user to update)."); // Error if missing
        } // End of validation

        List<User> matchingUsers = userController.searchAndSortUsers(searchAttribute, "id", true); // Locates user
        if (matchingUsers.isEmpty()) { // Check existence
            return errorResult("No user found matching '" + searchAttribute + "'."); // Error if missing
        } // End of check
        if (matchingUsers.size() > 1) { // Check ambiguity
            return errorResult("Multiple users found matching '" + searchAttribute + "'. Please ask the user to provide a more specific identifier like an exact email or ID."); // Error if ambiguous
        } // End of check

        User existingUser = matchingUsers.get(0); // Fetches current data

        // Merge: use new values if provided, otherwise keep existing
        String newEmail = getStringOrNull(args, "newEmail"); // Optional new email
        String newPassword = getStringOrNull(args, "newPassword"); // Optional new password
        String newFirstName = getStringOrNull(args, "newFirstName"); // Optional new first name
        String newLastName = getStringOrNull(args, "newLastName"); // Optional new last name
        String newRole = getStringOrNull(args, "newRole"); // Optional new role

        User updatedUser = new User( // Constructs updated user object
                existingUser.getId(), // Keeps ID
                newEmail != null ? newEmail : existingUser.getEmail(), // Updates email if provided
                existingUser.getRoles(), // Keeps roles (processed below)
                newPassword != null ? newPassword : existingUser.getPassword(), // Updates password if provided
                newFirstName != null ? newFirstName : existingUser.getFirstName(), // Updates first name if provided
                newLastName != null ? newLastName : existingUser.getLastName() // Updates last name if provided
        ); // End of constructor

        String roleLabel = newRole != null ? newRole : userController.toRoleLabel(existingUser.getRoles()); // Normalizes role
        userController.updateUser(updatedUser, roleLabel); // Saves changes
        notifyDataChanged(); // Triggers UI refresh

        JsonObject result = new JsonObject(); // Success container
        result.addProperty("status", "success"); // Sets status
        result.addProperty("message", "User with ID " + existingUser.getId() + " (" + existingUser.getEmail() + ") has been updated successfully."); // Success message
        return result; // Returns result
    } // End of handleUpdateUser

    private JsonObject handleListAllUsers() throws SQLDataException { // Listing logic
        List<User> users = serviceUser.recuperer(); // Fetches all from DB
        JsonArray array = new JsonArray(); // JSON array for AI
        for (User u : users) { // Iterates and maps
            JsonObject obj = new JsonObject(); // User JSON object
            obj.addProperty("id", u.getId()); // ID
            obj.addProperty("email", u.getEmail()); // Email
            obj.addProperty("firstName", u.getFirstName()); // First name
            obj.addProperty("lastName", u.getLastName()); // Last name
            obj.addProperty("role", userController.toRoleLabel(u.getRoles())); // Human role
            obj.addProperty("status", u.getStatus() != null ? u.getStatus() : "UNBLOCKED"); // Status
            obj.addProperty("is2faEnabled", u.getGoogleAuthenticatorSecret() != null); // Security status
            array.add(obj); // Adds to array
        } // End of loop

        JsonObject result = new JsonObject(); // Root container
        result.addProperty("status", "success"); // Status
        result.add("users", array); // Attaches list
        return result; // Returns result
    } // End of handleListAllUsers

    private JsonObject handleSearchUsers(JsonObject args) throws SQLDataException { // Search logic
        String searchText = getStringOrNull(args, "searchText"); // Extracts query
        if (searchText == null) return errorResult("Missing searchText."); // Validation

        List<User> users = userController.searchAndSortUsers(searchText, "id", true); // Executes search
        JsonArray array = new JsonArray(); // Results container
        for (User u : users) { // Maps results
            JsonObject obj = new JsonObject(); // User object
            obj.addProperty("id", u.getId()); // ID
            obj.addProperty("email", u.getEmail()); // Email
            obj.addProperty("firstName", u.getFirstName()); // First
            obj.addProperty("lastName", u.getLastName()); // Last
            obj.addProperty("role", userController.toRoleLabel(u.getRoles())); // Role
            obj.addProperty("status", u.getStatus() != null ? u.getStatus() : "UNBLOCKED"); // Status
            array.add(obj); // Adds to array
        } // End loop

        JsonObject result = new JsonObject(); // Root
        result.addProperty("status", "success"); // Status
        result.add("users", array); // List
        return result; // Returns result
    } // End of handleSearchUsers

    private JsonObject handleGetStatistics() throws SQLDataException { // Statistics logic
        int[] stats = userController.getStatistics(); // Fetches stats array
        JsonObject result = new JsonObject(); // JSON container
        result.addProperty("status", "success"); // Status
        result.addProperty("totalUsers", stats[0]); // Total count
        result.addProperty("adminUsers", stats[1]); // Admin count
        result.addProperty("normalUsers", stats[2]); // User count
        return result; // Returns result
    } // End of handleGetStatistics

    private JsonObject handleUpdateUserStatus(JsonObject args) throws SQLDataException { // Status update logic
        String searchAttribute = getStringOrNull(args, "searchAttribute"); // Target key
        String status = getStringOrNull(args, "status"); // New status

        if (searchAttribute == null || status == null) { // Validation
            return errorResult("Both searchAttribute and status are required."); // Error
        } // End validation

        List<User> matchingUsers = userController.searchAndSortUsers(searchAttribute, "id", true); // Finds user
        if (matchingUsers.isEmpty()) return errorResult("No user found matching '" + searchAttribute + "'."); // Check
        if (matchingUsers.size() > 1) return errorResult("Multiple users found. Be more specific."); // Ambiguity check

        User user = matchingUsers.get(0); // Takes user
        userController.updateStatus(user.getId(), status); // Updates DB
        notifyDataChanged(); // Refreshes UI

        JsonObject result = new JsonObject(); // Container
        result.addProperty("status", "success"); // Status
        result.addProperty("message", "User " + user.getEmail() + " status updated to " + status); // Success message
        return result; // Returns result
    } // End of handleUpdateUserStatus

    private JsonObject handleExportUsers(JsonObject args) throws Exception { // Export logic
        String format = getStringOrNull(args, "format"); // Extracts target format
        if (format == null) return errorResult("Format is required."); // Validation

        List<User> users = serviceUser.recuperer(); // Fetches all data
        File exportDir = new File("exports"); // Exports folder
        if (!exportDir.exists()) exportDir.mkdirs(); // Creates folder if missing

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")); // File timestamp
        String extension = format.equalsIgnoreCase("excel") ? "xlsx" : format.toLowerCase(); // File extension
        String fileName = "users_export_" + timestamp + "." + extension; // Final filename
        File file = new File(exportDir, fileName); // File handle

        switch (format.toLowerCase()) { // Routes based on format
            case "pdf": // PDF case
                ExportService.exportToPdf(users, file); // Generates PDF
                break; // Break
            case "excel": // Excel case
                ExportService.exportToExcel(users, file); // Generates XLSX
                break; // Break
            case "json": // JSON case
                ExportService.exportToJson(users, file); // Generates JSON file
                break; // Break
            default: // Unsupported
                return errorResult("Unsupported format: " + format); // Error
        } // End switch

        JsonObject result = new JsonObject(); // Result container
        result.addProperty("status", "success"); // Status
        result.addProperty("message", "User list exported successfully as " + format.toUpperCase() + "."); // Message
        result.addProperty("filePath", file.getAbsolutePath()); // Technical path
        result.addProperty("fileName", fileName); // Friendly filename
        return result; // Returns result
    } // End of handleExportUsers

    private JsonObject handleUiAction(JsonObject args) { // UI navigation logic
        String action = getStringOrNull(args, "action"); // Extracts command
        if (action == null) return errorResult("Action is required."); // Validation

        if (uiActionCallback != null) { // Checks if UI is listening
            uiActionCallback.accept(action); // Triggers callback in UI controller
            JsonObject result = new JsonObject(); // Container
            result.addProperty("status", "success"); // Status
            result.addProperty("message", "UI Action '" + action + "' triggered."); // Success message
            return result; // Returns result
        } // End callback check
        return errorResult("UI Action callback not configured."); // Error if UI is disconnected
    } // End of handleUiAction

    private JsonObject handleGetUserHistory() { // History retrieval logic
        User currentUser = utils.SessionManager.getInstance().getCurrentUser(); // Gets logged-in admin
        if (currentUser == null) return errorResult("No user logged in."); // Session check

        List<models.ChatMessage> history = historyService.loadHistory(currentUser.getId()); // Fetches from DB
        JsonArray array = new JsonArray(); // Result array
        for (models.ChatMessage msg : history) { // Maps history
            JsonObject obj = new JsonObject(); // Msg object
            obj.addProperty("role", msg.getRole()); // Role
            obj.addProperty("content", msg.getText()); // Text
            obj.addProperty("timestamp", msg.getTimestamp().toString()); // Time
            array.add(obj); // Adds to list
        } // End loop

        JsonObject result = new JsonObject(); // Root
        result.addProperty("status", "success"); // Status
        result.add("history", array); // History list
        return result; // Returns result
    } // End of handleGetUserHistory

    private JsonObject handleGetAdminLogs() { // Audit retrieval logic
        List<models.AdminLog> logs = adminLogService.getAllLogs(); // Fetches logs from DB
        JsonArray array = new JsonArray(); // Results container
        for (models.AdminLog log : logs) { // Maps logs
            JsonObject obj = new JsonObject(); // Log entry
            obj.addProperty("adminId", log.getAdminId()); // Who did it
            obj.addProperty("action", log.getActionType()); // What was done
            obj.addProperty("targetId", log.getTargetId()); // To whom
            obj.addProperty("details", log.getDetails()); // Technical details
            obj.addProperty("timestamp", log.getCreatedAt().toString()); // When
            array.add(obj); // Adds to array
        } // End loop

        JsonObject result = new JsonObject(); // Root
        result.addProperty("status", "success"); // Status
        result.add("logs", array); // Log list
        return result; // Returns result
    } // End of handleGetAdminLogs

    private JsonObject handleGetDatabaseSchema() { // Technical info logic
        JsonObject schema = new JsonObject(); // Schema container
        schema.addProperty("databaseName", "pharmax"); // DB Name
        
        JsonArray tables = new JsonArray(); // Tables list
        JsonObject userTable = new JsonObject(); // Table metadata
        userTable.addProperty("tableName", "user"); // Name
        
        JsonArray columns = new JsonArray(); // Columns metadata
        columns.add(createColumn("id", "INT", "Primary Key, Auto-increment")); // ID
        columns.add(createColumn("email", "VARCHAR(255)", "Unique, User's email")); // Email
        columns.add(createColumn("roles", "LONGTEXT (JSON)", "User roles (e.g., [\"ROLE_USER\"])")); // Roles
        columns.add(createColumn("password", "VARCHAR(255)", "SHA-256 hashed password")); // Pass
        columns.add(createColumn("first_name", "VARCHAR(255)", "User's first name")); // First
        columns.add(createColumn("last_name", "VARCHAR(255)", "User's last name (nullable)")); // Last
        columns.add(createColumn("avatar", "VARCHAR(255)", "Path to avatar image file")); // Avatar
        columns.add(createColumn("status", "VARCHAR(50)", "Account status: 'UNBLOCKED' or 'BLOCKED'")); // Status
        columns.add(createColumn("google_authenticator_secret", "VARCHAR(255)", "Secret key for 2FA")); // 2FA
        
        userTable.add("columns", columns); // Attaches columns
        tables.add(userTable); // Adds table
        schema.add("tables", tables); // Attaches tables

        JsonObject result = new JsonObject(); // Final result
        result.addProperty("status", "success"); // Status
        result.add("schema", schema); // Data
        return result; // Returns result
    } // End of handleGetDatabaseSchema

    private JsonObject createColumn(String name, String type, String desc) { // Helper for schema building
        JsonObject obj = new JsonObject(); // Column object
        obj.addProperty("name", name); // Col name
        obj.addProperty("type", type); // SQL type
        obj.addProperty("description", desc); // Usage
        return obj; // Returns object
    } // End of createColumn

    private String getStringOrNull(JsonObject obj, String key) { // Helper for safe JSON parsing
        if (obj.has(key) && !obj.get(key).isJsonNull()) { // Checks existence and nullity
            String value = obj.get(key).getAsString().trim(); // Extracts and trims
            return value.isEmpty() ? null : value; // Returns null if empty string
        } // End checks
        return null; // Returns null if missing
    } // End of getStringOrNull

    private JsonObject errorResult(String message) { // Helper for error responses
        JsonObject result = new JsonObject(); // Result container
        result.addProperty("status", "error"); // Marks as error
        result.addProperty("message", message); // Technical message
        return result; // Returns result
    } // End of errorResult

    private void notifyDataChanged() { // Internal event notifier
        if (onDataChanged != null) { // Checks for listeners
            onDataChanged.run(); // Calls UI refresh logic
        } // End check
    } // End of notifyDataChanged
} // End of FunctionCallDispatcher class
