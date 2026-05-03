package Services; // Defines the package for AI-related services

import Models.ChatMessage; // Imports the ChatMessage model for conversation tracking
import com.google.gson.Gson; // Imports Google's GSON library for JSON serialization/deserialization
import com.google.gson.JsonArray; // Handles JSON arrays in API requests/responses
import com.google.gson.JsonElement; // Base class for all JSON elements
import com.google.gson.JsonObject; // Handles JSON objects (key-value pairs)
import com.google.gson.JsonParser; // Parses raw JSON strings into JsonObjects

import java.io.IOException; // Standard exception for input/output errors
import java.io.InputStream; // For reading resources like credentials.json
import java.io.InputStreamReader; // Converts input stream to reader
import java.net.URI; // Represents the API endpoint URL
import java.net.http.HttpClient; // Modern Java HTTP client for network requests
import java.net.http.HttpRequest; // Represents an outgoing HTTP request
import java.net.http.HttpResponse; // Represents an incoming HTTP response
import java.nio.charset.StandardCharsets; // Ensures UTF-8 encoding for text
import java.time.Duration; // Used for setting request timeouts
import java.util.List; // For managing lists of chat messages
import java.util.logging.Logger; // Standard logging utility

/**
 * Handles all communication with the OpenRouter API (OpenAI-compatible), including:
 * - Building requests with conversation history and tool declarations
 * - Parsing text responses vs. tool call responses
 * - Executing the tool-call loop
 */
public class OpenRouterChatService { // Main class for AI chat logic

    private static final Logger LOG = Logger.getLogger(OpenRouterChatService.class.getName()); // Logger instance for debugging
    private static final String OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions"; // API endpoint
    
    // Using Nemotron 3 Nano Omni (Free) as requested
    private static final String MODEL_NAME = "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free"; // Selected AI model

    private static final String SYSTEM_INSTRUCTION = // Defines the AI's persona and constraints
            "You are PharmaX Assistant, an AI helper embedded in the PharmaX User Management application.\n\n" +
            "You can:\n" +
            "1. Answer general questions as a helpful, friendly assistant.\n" +
            "3. Manage users by calling the provided functions when the user asks you to create, delete, update, search, or list users.\n" +
            "4. **Learning & Memory:** You have access to the user's previous chat history and admin logs. Use them to remember their preferences, previous questions, or to explain administrative actions taken recently.\n\n" +
            "CRITICAL BEHAVIORAL RULES:\n" +
            "- **Finding Users for Update/Delete:** If the user asks to update or delete a user but only gives a name, last name, or partial info, DO NOT call update_user or delete_user immediately. FIRST, call `search_users` to find their exact email. \n" +
            "- **Typo Correction:** If `search_users` returns nothing, dynamically deduce if there was a typo, fix it, and call `search_users` again.\n" +
            "- **Single Confirmation:** Once you find the correct user via search, ask the user for confirmation exactly ONCE (e.g. \"I found Amal (amal@email.com). Are you sure you want to update her?\"). Do not double-confirm (never say \"Is that correct?\" followed by \"Are you sure?\").\n" +
            "- **Missing Info (Normal):** If the user is missing required fields (e.g. password, email), ask them for the missing information ONE BY ONE, not all at once.\n" +
            "- **Missing Info (Trolling/Avoiding):** If the user is trolling, dodging your questions, or purposefully avoiding providing the missing info after you asked, DO NOT keep asking. Instead, generate the closest reasonable dummy data to fill in the blanks, execute the action, and then give them an unexpected, funny, and ironic message acting as if you got tired of their games (e.g., \"Alright fine, I made up a password for you since you want to play games. Enjoy!\").\n" +
            "- **Validation:** Roles must be \"Admin\" or \"Normal User\". Passwords >= 6 chars. Emails must be valid format.\n\n" +
            "When greeting the user for the first time, introduce yourself briefly.";

    private final String apiKey; // Secret key for API authentication
    private final HttpClient httpClient; // Reusable network client
    private final Gson gson; // Reusable JSON converter
    private final FunctionCallDispatcher dispatcher; // Links AI calls to local Java methods

    public OpenRouterChatService(FunctionCallDispatcher dispatcher) { // Constructor injection
        this.apiKey = loadApiKey(); // Loads API key from resource file
        this.httpClient = HttpClient.newBuilder() // Builds the HTTP client
                .connectTimeout(Duration.ofSeconds(15)) // Sets 15s connection timeout
                .build(); // Finalizes build
        this.gson = new Gson(); // Initializes GSON
        this.dispatcher = dispatcher; // Sets the tool dispatcher
    } // End of constructor

    public String sendMessage(List<ChatMessage> conversationHistory) throws IOException, InterruptedException { // Main entry point
        JsonObject requestBody = buildRequest(conversationHistory); // Creates the JSON payload
        JsonObject response = callApi(requestBody); // Sends request to OpenRouter
        return processResponse(response, conversationHistory); // Parses result and handles tools
    } // End of sendMessage

    private JsonObject buildRequest(List<ChatMessage> conversationHistory) { // Constructs API payload
        JsonObject request = new JsonObject(); // Root object
        request.addProperty("model", MODEL_NAME); // Specifies the AI model

        JsonArray messages = new JsonArray(); // Array for chat history

        // System Instruction
        JsonObject systemMsg = new JsonObject(); // Instruction object
        systemMsg.addProperty("role", "system"); // System role
        systemMsg.addProperty("content", SYSTEM_INSTRUCTION); // The prompt content
        messages.add(systemMsg); // Adds to history

        // History
        for (ChatMessage msg : conversationHistory) { // Iterates through previous messages
            JsonObject m = new JsonObject(); // Individual message object
            String role = msg.getRole(); // Gets role (user/model)
            if (role.equals("model")) role = "assistant"; // Normalizes model -> assistant for API compatibility
            
            m.addProperty("role", role); // Sets message role
            
            if (msg.getToolCalls() != null) { // Checks if message was a tool call
                m.add("tool_calls", msg.getToolCalls()); // Includes tool call data
                // OpenAI requires content to be null or empty string if tool_calls is present
                m.addProperty("content", ""); // Placeholder content
            } else if (role.equals("tool")) { // Checks if message is a tool result
                m.addProperty("tool_call_id", msg.getToolCallId()); // Links result to call ID
                m.addProperty("content", msg.getText()); // Result data
            } else { // Normal text message
                m.addProperty("content", msg.getText()); // Text content
            } // End of message type check
            messages.add(m); // Adds to message array
        } // End of history loop

        request.add("messages", messages); // Attaches messages to request
        request.add("tools", buildToolDeclarations()); // Attaches available functions
        return request; // Returns full request object
    } // End of buildRequest

    private String processResponse(JsonObject response, List<ChatMessage> conversationHistory) throws IOException, InterruptedException { // Handles AI output
        if (!response.has("choices")) { // Validation check
            return "Error: Unexpected response from OpenRouter."; // Handles API failures
        } // End of validation

        JsonObject choice = response.getAsJsonArray("choices").get(0).getAsJsonObject(); // Gets first completion choice
        JsonObject message = choice.getAsJsonObject("message"); // Gets message object
        
        // Handle tool calls
        if (choice.has("message") && choice.getAsJsonObject("message").has("tool_calls")) { // Detects function calls
            JsonArray toolCalls = choice.getAsJsonObject("message").getAsJsonArray("tool_calls"); // Extracts calls
            JsonObject toolCall = toolCalls.get(0).getAsJsonObject(); // Takes first call
            String callId = toolCall.get("id").getAsString(); // Unique call ID
            JsonObject function = toolCall.getAsJsonObject("function"); // Function data
            String functionName = function.get("name").getAsString(); // Method name
            String argumentsStr = function.get("arguments").getAsString(); // JSON arguments string

            System.out.println("AI calling tool: " + functionName + " with " + argumentsStr); // Console log for debugging

            JsonObject functionArgs = gson.fromJson(argumentsStr, JsonObject.class); // Parses args
            JsonObject result = dispatcher.dispatch(functionName, functionArgs); // Executes Java method

            // Add the assistant's tool call to history
            ChatMessage toolCallMsg = new ChatMessage("model", ""); // History entry for the call
            toolCallMsg.setToolCalls(toolCalls); // Stores call metadata
            conversationHistory.add(toolCallMsg); // Updates history

            // Add the tool's response to history
            ChatMessage toolResultMsg = new ChatMessage("tool", gson.toJson(result)); // History entry for result
            toolResultMsg.setToolCallId(callId); // Links to call
            conversationHistory.add(toolResultMsg); // Updates history

            // Now call the API again with the full history including the tool result
            JsonObject followUpRequest = buildRequest(conversationHistory); // Recursive call preparation
            JsonObject finalResponse = callApi(followUpRequest); // Resubmits to AI
            return processResponse(finalResponse, conversationHistory); // Processes final text
        } // End of tool call check

        return message.get("content").getAsString(); // Returns simple text response
    } // End of processResponse

    private JsonArray buildToolDeclarations() { // Registers all functions AI can use
        JsonArray tools = new JsonArray(); // Tools container
        tools.add(wrapTool(buildCreateUserTool())); // Registration for create
        tools.add(wrapTool(buildDeleteUserTool())); // Registration for delete
        tools.add(wrapTool(buildUpdateUserTool())); // Registration for update
        tools.add(wrapTool(buildListAllUsersTool())); // Registration for list
        tools.add(wrapTool(buildSearchUsersTool())); // Registration for search
        tools.add(wrapTool(buildGetStatisticsTool())); // Registration for stats
        tools.add(wrapTool(buildUpdateUserStatusTool())); // Registration for status
        tools.add(wrapTool(buildExportUsersTool())); // Registration for export
        tools.add(wrapTool(buildTriggerUiActionTool())); // Registration for UI
        tools.add(wrapTool(buildGetDatabaseSchemaTool())); // Registration for schema
        tools.add(wrapTool(buildGetUserHistoryTool())); // Registration for memory
        tools.add(wrapTool(buildGetAdminLogsTool())); // Registration for audit
        return tools; // Returns all tool definitions
    } // End of buildToolDeclarations

    private JsonObject wrapTool(JsonObject function) { // Helper to wrap function in tool schema
        JsonObject tool = new JsonObject(); // Tool wrapper
        tool.addProperty("type", "function"); // Required field
        tool.add("function", function); // Attaches function schema
        return tool; // Returns wrapped object
    } // End of wrapTool

    private JsonObject buildCreateUserTool() { // Schema for user creation
        JsonObject func = new JsonObject(); // Function object
        func.addProperty("name", "create_user"); // Internal name
        func.addProperty("description", "Creates a new user."); // Human description
        
        JsonObject params = new JsonObject(); // Parameters object
        params.addProperty("type", "object"); // Input is an object
        JsonObject props = new JsonObject(); // Properties list
        
        props.add("email", createProp("string", "User's email")); // email prop
        props.add("password", createProp("string", "User's password (min 6 chars)")); // password prop
        props.add("firstName", createProp("string", "First name")); // name prop
        props.add("lastName", createProp("string", "Last name (optional)")); // last name prop
        
        JsonObject role = createProp("string", "Role: 'Admin' or 'Normal User'"); // role prop
        JsonArray enums = new JsonArray(); // Enum values
        enums.add("Admin"); // Option 1
        enums.add("Normal User"); // Option 2
        role.add("enum", enums); // Attaches enums
        props.add("role", role); // Attaches role prop
        
        params.add("properties", props); // Attaches all properties
        JsonArray req = new JsonArray(); // Required fields array
        req.add("email"); req.add("password"); req.add("firstName"); req.add("role"); // List of required
        params.add("required", req); // Attaches required list
        
        func.add("parameters", params); // Attaches params to function
        return func; // Returns schema
    } // End of buildCreateUserTool

    private JsonObject buildDeleteUserTool() { // Schema for deletion
        JsonObject func = new JsonObject(); // Function object
        func.addProperty("name", "delete_user"); // Internal name
        func.addProperty("description", "Deletes a user by email, ID, or name."); // Description
        
        JsonObject params = new JsonObject(); // Parameters object
        params.addProperty("type", "object"); // Type
        JsonObject props = new JsonObject(); // Props
        props.add("searchAttribute", createProp("string", "Email, ID, or Name to identify the user")); // Input prop
        params.add("properties", props); // Attaches props
        JsonArray req = new JsonArray(); req.add("searchAttribute"); // Marks as required
        params.add("required", req); // Attaches
        
        func.add("parameters", params); // Attaches params
        return func; // Returns schema
    } // End of buildDeleteUserTool

    private JsonObject buildUpdateUserTool() { // Schema for updates
        JsonObject func = new JsonObject(); // Function object
        func.addProperty("name", "update_user"); // Name
        func.addProperty("description", "Updates a user. identify by searchAttribute."); // Description
        
        JsonObject params = new JsonObject(); // Params
        params.addProperty("type", "object"); // Type
        JsonObject props = new JsonObject(); // Props
        props.add("searchAttribute", createProp("string", "ID, Email, or Name to find the user")); // Search key
        props.add("newEmail", createProp("string", "New email (if changing)")); // Optional new email
        props.add("newPassword", createProp("string", "New password (min 6 chars)")); // Optional new pass
        props.add("newFirstName", createProp("string", "New first name")); // Optional new first
        props.add("newLastName", createProp("string", "New last name")); // Optional new last
        props.add("newRole", createProp("string", "New role: 'Admin' or 'Normal User'")); // Optional new role
        
        params.add("properties", props); // Attaches props
        JsonArray req = new JsonArray(); req.add("searchAttribute"); // ID is required
        params.add("required", req); // Attaches
        
        func.add("parameters", params); // Attaches params
        return func; // Returns schema
    } // End of buildUpdateUserTool

    private JsonObject buildListAllUsersTool() { // Schema for listing
        JsonObject func = new JsonObject(); // Function object
        func.addProperty("name", "list_all_users"); // Name
        func.addProperty("description", "Lists all users with their full details (Email, Role, Status, 2FA status, etc.)."); // Description
        func.add("parameters", new JsonObject()); // No parameters needed
        return func; // Returns schema
    } // End of buildListAllUsersTool

    private JsonObject buildSearchUsersTool() { // Schema for text search
        JsonObject func = new JsonObject(); // Function object
        func.addProperty("name", "search_users"); // Name
        func.addProperty("description", "Searches users by text."); // Description
        
        JsonObject params = new JsonObject(); // Params
        params.addProperty("type", "object"); // Type
        JsonObject props = new JsonObject(); // Props
        props.add("searchText", createProp("string", "Text to search in name, email, etc.")); // Search input
        params.add("properties", props); // Attaches props
        JsonArray req = new JsonArray(); req.add("searchText"); // Marks as required
        params.add("required", req); // Attaches
        
        func.add("parameters", params); // Attaches params
        return func; // Returns schema
    } // End of buildSearchUsersTool

    private JsonObject buildGetStatisticsTool() { // Schema for stats
        JsonObject func = new JsonObject(); // Function object
        func.addProperty("name", "get_user_statistics"); // Name
        func.addProperty("description", "Gets user stats."); // Description
        func.add("parameters", new JsonObject()); // No params
        return func; // Returns schema
    } // End of buildGetStatisticsTool

    private JsonObject buildUpdateUserStatusTool() { // Schema for blocking/unblocking
        JsonObject func = new JsonObject(); // Function object
        func.addProperty("name", "update_user_status"); // Name
        func.addProperty("description", "Blocks or unblocks a user."); // Description
        
        JsonObject params = new JsonObject(); // Params
        params.addProperty("type", "object"); // Type
        JsonObject props = new JsonObject(); // Props
        props.add("searchAttribute", createProp("string", "ID, Email, or Name to identify the user")); // Key
        
        JsonObject status = createProp("string", "New status: 'BLOCKED' or 'UNBLOCKED'"); // Status prop
        JsonArray enums = new JsonArray(); // Options
        enums.add("BLOCKED"); // Option 1
        enums.add("UNBLOCKED"); // Option 2
        status.add("enum", enums); // Attaches enum
        props.add("status", status); // Attaches status prop
        
        params.add("properties", props); // Attaches props
        JsonArray req = new JsonArray(); req.add("searchAttribute"); req.add("status"); // Marks both as required
        params.add("required", req); // Attaches
        
        func.add("parameters", params); // Attaches params
        return func; // Returns schema
    } // End of buildUpdateUserStatusTool

    private JsonObject buildExportUsersTool() { // Schema for file export
        JsonObject func = new JsonObject(); // Function object
        func.addProperty("name", "export_users"); // Name
        func.addProperty("description", "Exports the user list to a file (PDF, Excel, or JSON)."); // Description
        
        JsonObject params = new JsonObject(); // Params
        params.addProperty("type", "object"); // Type
        JsonObject props = new JsonObject(); // Props
        
        JsonObject format = createProp("string", "Export format: 'pdf', 'excel', or 'json'"); // Format prop
        JsonArray enums = new JsonArray(); // Options
        enums.add("pdf"); // PDF
        enums.add("excel"); // Excel
        enums.add("json"); // JSON
        format.add("enum", enums); // Attaches enums
        props.add("format", format); // Attaches format prop
        
        params.add("properties", props); // Attaches props
        JsonArray req = new JsonArray(); req.add("format"); // Marks required
        params.add("required", req); // Attaches
        
        func.add("parameters", params); // Attaches params
        return func; // Returns schema
    } // End of buildExportUsersTool

    private JsonObject buildTriggerUiActionTool() { // Schema for navigation
        JsonObject func = new JsonObject(); // Function object
        func.addProperty("name", "trigger_ui_action"); // Name
        func.addProperty("description", "Triggers a specific UI action or navigates to a setting page."); // Description
        
        JsonObject params = new JsonObject(); // Params
        params.addProperty("type", "object"); // Type
        JsonObject props = new JsonObject(); // Props
        
        JsonObject action = createProp("string", "The action to perform"); // Action key
        JsonArray enums = new JsonArray(); // Options
        enums.add("open_profile"); // Nav 1
        enums.add("change_photo"); // Nav 2
        enums.add("enable_2fa"); // Nav 3
        enums.add("disable_2fa"); // Nav 4
        action.add("enum", enums); // Attaches options
        props.add("action", action); // Attaches prop
        
        params.add("properties", props); // Attaches props
        JsonArray req = new JsonArray(); req.add("action"); // Marks required
        params.add("required", req); // Attaches
        
        func.add("parameters", params); // Attaches params
        return func; // Returns schema
    } // End of buildTriggerUiActionTool

    private JsonObject buildGetDatabaseSchemaTool() { // Schema for developer assistance
        JsonObject func = new JsonObject(); // Function object
        func.addProperty("name", "get_database_schema"); // Name
        func.addProperty("description", "Returns the database schema (tables and columns) and technical structure."); // Description
        func.add("parameters", new JsonObject()); // No params
        return func; // Returns schema
    } // End of buildGetDatabaseSchemaTool

    private JsonObject buildGetUserHistoryTool() { // Schema for memory access
        JsonObject func = new JsonObject(); // Function object
        func.addProperty("name", "get_user_history"); // Name
        func.addProperty("description", "Retrieves the user's previous chat history from the database to remember context or styles."); // Description
        func.add("parameters", new JsonObject()); // No params
        return func; // Returns schema
    } // End of buildGetUserHistoryTool

    private JsonObject buildGetAdminLogsTool() { // Schema for auditing
        JsonObject func = new JsonObject(); // Function object
        func.addProperty("name", "get_admin_logs"); // Name
        func.addProperty("description", "Retrieves recent administrative action logs (who created/deleted whom)."); // Description
        func.add("parameters", new JsonObject()); // No params
        return func; // Returns schema
    } // End of buildGetAdminLogsTool

    private JsonObject createProp(String type, String desc) { // Helper to build property schema
        JsonObject prop = new JsonObject(); // Prop object
        prop.addProperty("type", type); // Sets type (string, int, etc)
        prop.addProperty("description", desc); // Sets description
        return prop; // Returns prop
    } // End of createProp

    private JsonObject callApi(JsonObject requestBody) throws IOException, InterruptedException { // Low-level API caller
        String json = gson.toJson(requestBody); // Serializes request

        HttpRequest request = HttpRequest.newBuilder() // Builds HTTP request
                .uri(URI.create(OPENROUTER_API_URL)) // Sets destination
                .header("Content-Type", "application/json") // Content type
                .header("Authorization", "Bearer " + apiKey) // API token
                .header("HTTP-Referer", "http://localhost") // Required by OpenRouter for ranking
                .header("X-Title", "PharmaX") // App identifier
                .timeout(Duration.ofSeconds(30)) // 30s timeout
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8)) // POST with body
                .build(); // Finalizes

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString()); // Sends sync request

        if (response.statusCode() != 200) { // Error handling
            throw new IOException("OpenRouter API error (HTTP " + response.statusCode() + "): " + response.body()); // Throws on error
        } // End of status check

        return JsonParser.parseString(response.body()).getAsJsonObject(); // Parses and returns JSON result
    } // End of callApi

    public boolean isConfigured() { // Configuration check
        return apiKey != null && !apiKey.isEmpty(); // Validates key presence
    } // End of isConfigured

    private String loadApiKey() { // Internal key loader
        try (InputStream inputStream = getClass().getResourceAsStream("/credentials.json")) { // Opens credentials file
            if (inputStream == null) return null; // File missing check
            JsonObject root = JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject(); // Parses file
            return root.has("openrouter_api_key") ? root.get("openrouter_api_key").getAsString() : null; // Extracts key
        } catch (IOException e) { // Catches file errors
            return null; // Fails gracefully
        } // End of try-catch
    } // End of loadApiKey
} // End of OpenRouterChatService class
