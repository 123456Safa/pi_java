package Services; // Defines the package where this service belongs

import Models.User; // Imports the User model class for data handling
import utils.MyDatabase; // Imports the database utility for connection management

import java.sql.Connection; // SQL connection interface for database interaction
import java.sql.PreparedStatement; // Interface for precompiled SQL statements to prevent injection
import java.sql.ResultSet; // Interface for handling SQL query results
import java.sql.SQLDataException; // Exception thrown for data-related SQL errors
import java.sql.SQLException; // Base exception for SQL-related issues
import java.sql.Types; // Defines SQL types for null handling in prepared statements
import java.util.ArrayList; // List implementation for storing retrieved users
import java.util.List; // Interface for list collections

/**
 * ServiceUser class implements Iservice interface for User model.
 * It manages all CRUD (Create, Read, Update, Delete) operations for users in the database.
 */
public class ServiceUser implements Iservice<User> { // Class declaration implementing Iservice for User

    private final Connection connection; // Permanent database connection instance

    /**
     * Constructor initializes the connection using the Singleton MyDatabase instance.
     */
    public ServiceUser() { // Constructor method
        connection = MyDatabase.getInstance().getConnection(); // Fetches connection from MyDatabase singleton
    } // End of constructor

    /**
     * Adds a new user to the database.
     * @param user The User object containing all necessary details.
     * @throws SQLDataException if the insertion fails.
     */
    @Override // Indicates this method overrides Iservice method
    public void ajouter(User user) throws SQLDataException { // Method to add a user
        // SQL query string for insertion with placeholders (?) for security
        String sql = "INSERT INTO `user` (email, roles, password, first_name, last_name, avatar, status, google_authenticator_secret, google_authenticator_secret_pending, is_2fa_setup_in_progress, face_encoding, face_auth_enabled, google_id, phone_number, data_face_api, failed_attempts, lockout_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) { // Try-with-resources to ensure statement is closed
            preparedStatement.setString(1, user.getEmail()); // Sets the user's email in the first placeholder
            preparedStatement.setString(2, user.getRoles()); // Sets the user's roles in the second placeholder
            preparedStatement.setString(3, user.getPassword()); // Sets the user's password in the third placeholder
            preparedStatement.setString(4, user.getFirstName()); // Sets the user's first name in the fourth placeholder
            setNullableLastName(preparedStatement, 5, user.getLastName()); // Handles optional last name with a helper
            setNullableString(preparedStatement, 6, user.getAvatar()); // Handles optional avatar path
            preparedStatement.setString(7, user.getStatus()); // Sets the user's status (UNBLOCKED/BLOCKED)
            setNullableString(preparedStatement, 8, user.getGoogleAuthenticatorSecret()); // Sets 2FA secret if exists
            setNullableString(preparedStatement, 9, user.getGoogleAuthenticatorSecretPending()); // Sets pending 2FA secret
            preparedStatement.setBoolean(10, user.isIs2faSetupInProgress()); // Sets boolean for 2FA setup state
            setNullableString(preparedStatement, 11, user.getFaceEncoding()); // Sets face ID embedding if exists
            preparedStatement.setBoolean(12, user.isFaceAuthEnabled()); // Sets boolean for Face ID activation
            setNullableString(preparedStatement, 13, user.getGoogleId()); // Sets Google OAuth ID if exists
            setNullableString(preparedStatement, 14, user.getPhoneNumber()); // Sets phone number if exists
            setNullableString(preparedStatement, 15, user.getDataFaceApi()); // Sets meta-data for face API
            preparedStatement.setInt(16, user.getFailedAttempts()); // Sets initial failed login attempts
            preparedStatement.setTimestamp(17, user.getLockoutTime()); // Sets lockout timestamp if applicable
            preparedStatement.executeUpdate(); // Executes the insertion in the database
        } catch (SQLException e) { // Catches any database errors during insertion
            throw buildDataException("insert", e); // Wraps SQLException into SQLDataException via helper
        } // End of try-catch
    } // End of ajouter method

    /**
     * Deletes a user from the database by their ID.
     * @param user The User object (ID is the primary requirement).
     * @throws SQLDataException if deletion fails or ID is invalid.
     */
    @Override // Overrides Iservice method
    public void supprimer(User user) throws SQLDataException { // Method to delete a user
        if (user == null || user.getId() <= 0) { // Validates that the user object and ID are valid
            throw new SQLDataException("A valid user id is required for delete."); // Throws error if validation fails
        } // End of validation

        String sql = "DELETE FROM `user` WHERE id = ?"; // SQL query string for deletion by primary key
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) { // Prepares the statement
            preparedStatement.setInt(1, user.getId()); // Binds the user ID to the placeholder
            int rows = preparedStatement.executeUpdate(); // Executes deletion and returns number of rows affected
            if (rows == 0) { // Checks if any user was actually deleted
                throw new SQLDataException("No user found with id " + user.getId()); // Error if ID didn't exist
            } // End of row check
        } catch (SQLException e) { // Catches database errors
            throw buildDataException("delete", e); // Wraps and rethrows exception
        } // End of try-catch
    } // End of supprimer method

    /**
     * Updates an existing user's information.
     * @param user The User object with updated fields.
     * @throws SQLDataException if the update fails.
     */
    @Override // Overrides Iservice method
    public void modifier(User user) throws SQLDataException { // Method to update user data
        if (user == null || user.getId() <= 0) { // Validates user existence and ID
            throw new SQLDataException("A valid user id is required for update."); // Throws error if ID is missing
        } // End of validation

        // SQL query for updating all user fields based on ID
        String sql = "UPDATE `user` SET email = ?, roles = ?, password = ?, first_name = ?, last_name = ?, avatar = ?, status = ?, google_authenticator_secret = ?, google_authenticator_secret_pending = ?, is_2fa_setup_in_progress = ?, face_encoding = ?, face_auth_enabled = ?, google_id = ?, phone_number = ?, data_face_api = ?, failed_attempts = ?, lockout_time = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) { // Prepares the SQL statement
            preparedStatement.setString(1, user.getEmail()); // Sets email
            preparedStatement.setString(2, user.getRoles()); // Sets roles
            preparedStatement.setString(3, user.getPassword()); // Sets hashed password
            preparedStatement.setString(4, user.getFirstName()); // Sets first name
            setNullableLastName(preparedStatement, 5, user.getLastName()); // Sets last name or NULL
            setNullableString(preparedStatement, 6, user.getAvatar()); // Sets avatar path or NULL
            preparedStatement.setString(7, user.getStatus()); // Sets status (UNBLOCKED/BLOCKED)
            setNullableString(preparedStatement, 8, user.getGoogleAuthenticatorSecret()); // Sets 2FA secret
            setNullableString(preparedStatement, 9, user.getGoogleAuthenticatorSecretPending()); // Sets pending 2FA secret
            preparedStatement.setBoolean(10, user.isIs2faSetupInProgress()); // Sets 2FA progress flag
            setNullableString(preparedStatement, 11, user.getFaceEncoding()); // Sets Face ID embedding
            preparedStatement.setBoolean(12, user.isFaceAuthEnabled()); // Sets Face ID enabled flag
            setNullableString(preparedStatement, 13, user.getGoogleId()); // Sets Google ID
            setNullableString(preparedStatement, 14, user.getPhoneNumber()); // Sets phone number
            setNullableString(preparedStatement, 15, user.getDataFaceApi()); // Sets face API data
            preparedStatement.setInt(16, user.getFailedAttempts()); // Sets failed attempts count
            preparedStatement.setTimestamp(17, user.getLockoutTime()); // Sets lockout timestamp
            preparedStatement.setInt(18, user.getId()); // Sets ID for WHERE clause
            int rows = preparedStatement.executeUpdate(); // Executes update in database
            if (rows == 0) { // Checks if user was found
                throw new SQLDataException("No user found with id " + user.getId()); // Error if ID doesn't exist
            } // End of row check
        } catch (SQLException e) { // Catches database errors
            throw buildDataException("update", e); // Wraps and rethrows exception
        } // End of try-catch
    } // End of modifier method

    /**
     * Updates only the status of a user (BLOCKED/UNBLOCKED).
     * @param userId The ID of the user.
     * @param newStatus The new status string.
     * @throws SQLDataException if the update fails.
     */
    public void modifierStatus(int userId, String newStatus) throws SQLDataException { // Method for quick status update
        if (userId <= 0 || newStatus == null || newStatus.trim().isEmpty()) { // Validates parameters
            throw new SQLDataException("Valid user id and status are required."); // Throws error on invalid input
        } // End of validation

        String sql = "UPDATE `user` SET status = ? WHERE id = ?"; // Minimal query for status update
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) { // Prepares the query
            preparedStatement.setString(1, newStatus.trim().toUpperCase()); // Normalizes status to UPPERCASE
            preparedStatement.setInt(2, userId); // Binds the user ID
            int rows = preparedStatement.executeUpdate(); // Executes the update
            if (rows == 0) { // Checks if user was found
                throw new SQLDataException("No user found with id " + userId); // Error if ID didn't exist
            } // End of row check
        } catch (SQLException e) { // Catches database errors
            throw buildDataException("update status", e); // Wraps and rethrows exception
        } // End of try-catch
    } // End of modifierStatus method

    /**
     * Retrieves all users from the database.
     * @return A list of User objects.
     * @throws SQLDataException if the retrieval fails.
     */
    @Override // Overrides Iservice method
    public List<User> recuperer() throws SQLDataException { // Method to fetch all users
        // Selects all relevant columns from the user table
        String sql = "SELECT id, email, roles, password, first_name, last_name, avatar, status, google_authenticator_secret, google_authenticator_secret_pending, is_2fa_setup_in_progress, face_encoding, face_auth_enabled, google_id, phone_number, data_face_api, failed_attempts, lockout_time, created_at, updated_at FROM `user` ORDER BY id";
        return runUserQuery(sql, null); // Uses helper method to execute query and map results
    } // End of recuperer method

    /**
     * Searches for users based on a string and sorts the result.
     * @param searchText Text to look for in email, names, roles, or ID.
     * @param sortBy Column name to sort by.
     * @param ascending Sort direction (true for ASC, false for DESC).
     * @return List of matching users.
     * @throws SQLDataException if the search fails.
     */
    public List<User> rechercherEtTrier(String searchText, String sortBy, boolean ascending) throws SQLDataException { // Advanced search/sort
        String orderByColumn = resolveSortableColumn(sortBy); // Resolves and validates sort column to prevent SQL injection
        String direction = ascending ? "ASC" : "DESC"; // Determines sorting direction

        // Dynamic SQL query with multiple LIKE conditions for flexible search
        String sql = "SELECT id, email, roles, password, first_name, last_name, avatar, status, google_authenticator_secret, google_authenticator_secret_pending, is_2fa_setup_in_progress, face_encoding, face_auth_enabled, google_id, phone_number, data_face_api, failed_attempts, lockout_time, created_at, updated_at FROM `user` " +
                "WHERE LOWER(email) LIKE ? OR LOWER(first_name) LIKE ? OR LOWER(COALESCE(last_name, '')) LIKE ? OR LOWER(roles) LIKE ? OR CAST(id AS CHAR) LIKE ? " +
                "ORDER BY " + orderByColumn + " " + direction;

        String normalized = searchText == null ? "" : searchText.trim().toLowerCase(); // Normalizes search text
        String token = "%" + normalized + "%"; // Wraps search text in wildcards for SQL LIKE

        return runUserQuery(sql, preparedStatement -> { // Executes query with parameter binding
            preparedStatement.setString(1, token); // Binds token to email search
            preparedStatement.setString(2, token); // Binds token to first name search
            preparedStatement.setString(3, token); // Binds token to last name search
            preparedStatement.setString(4, token); // Binds token to roles search
            preparedStatement.setString(5, token); // Binds token to ID search
        }); // End of runUserQuery call
    } // End of rechercherEtTrier method

    /**
     * Calculates statistics about user counts and roles.
     * @return An array [Total, Admins, Users].
     * @throws SQLDataException if query fails.
     */
    public int[] recupererStatistiques() throws SQLDataException { // Statistical analysis method
        // Complex query to count users by role in a single pass
        String sql = "SELECT " +
                "COUNT(*) AS total_users, " +
                "SUM(CASE WHEN LOWER(roles) LIKE '%role_admin%' THEN 1 ELSE 0 END) AS admin_users, " +
                "SUM(CASE WHEN LOWER(roles) LIKE '%role_user%' THEN 1 ELSE 0 END) AS normal_users " +
                "FROM `user`";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql); // Prepares the statement
             ResultSet resultSet = preparedStatement.executeQuery()) { // Executes and gets result set

            if (resultSet.next()) { // Moves to the first (and only) result row
                return new int[]{ // Returns mapped results as an array
                        resultSet.getInt("total_users"), // Total count
                        resultSet.getInt("admin_users"), // Admin count
                        resultSet.getInt("normal_users") // Normal user count
                }; // End of array return
            } // End of if resultSet.next()

            return new int[]{0, 0, 0}; // Default return if no data found
        } catch (SQLException e) { // Catches database errors
            throw buildDataException("read statistics", e); // Wraps and rethrows exception
        } // End of try-catch
    } // End of recupererStatistiques method

    /**
     * Finds a single user by their email address.
     * @param email The email to search for.
     * @return The User object or null if not found.
     * @throws SQLDataException if query fails.
     */
    public User findByEmail(String email) throws SQLDataException { // Lookup by email
        if (email == null || email.trim().isEmpty()) { // Validates email input
            return null; // Returns null immediately if email is empty
        } // End of validation

        // SQL to find user by exact email match
        String sql = "SELECT id, email, roles, password, first_name, last_name, avatar, status, google_authenticator_secret, google_authenticator_secret_pending, is_2fa_setup_in_progress, face_encoding, face_auth_enabled, google_id, phone_number, data_face_api, failed_attempts, lockout_time, created_at, updated_at FROM `user` WHERE email = ? LIMIT 1";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) { // Prepares the statement
            preparedStatement.setString(1, email.trim()); // Binds the email parameter
            try (ResultSet resultSet = preparedStatement.executeQuery()) { // Executes query
                if (resultSet.next()) { // Checks if a user was found
                    User user = new User(); // Creates new User object
                    user.setId(resultSet.getInt("id")); // Maps ID from DB to object
                    user.setEmail(resultSet.getString("email")); // Maps email
                    user.setRoles(resultSet.getString("roles")); // Maps roles
                    user.setPassword(resultSet.getString("password")); // Maps hashed password
                    user.setFirstName(resultSet.getString("first_name")); // Maps first name
                    user.setLastName(resultSet.getString("last_name")); // Maps last name
                    user.setAvatar(resultSet.getString("avatar")); // Maps avatar path
                    
                    String status = resultSet.getString("status"); // Retrieves status string
                    if (status != null) user.setStatus(status); // Sets status if not null
                    
                    user.setGoogleAuthenticatorSecret(resultSet.getString("google_authenticator_secret")); // Maps 2FA secret
                    user.setGoogleAuthenticatorSecretPending(resultSet.getString("google_authenticator_secret_pending")); // Maps pending secret
                    user.setIs2faSetupInProgress(resultSet.getBoolean("is_2fa_setup_in_progress")); // Maps 2FA progress flag
                    user.setFaceEncoding(resultSet.getString("face_encoding")); // Maps face embedding
                    user.setFaceAuthEnabled(resultSet.getBoolean("face_auth_enabled")); // Maps face auth enabled flag
                    user.setFailedAttempts(resultSet.getInt("failed_attempts")); // Maps failed attempts count
                    user.setLockoutTime(resultSet.getTimestamp("lockout_time")); // Maps lockout timestamp
                    user.setCreatedAt(resultSet.getTimestamp("created_at")); // Maps creation date
                    user.setUpdatedAt(resultSet.getTimestamp("updated_at")); // Maps last update date
                    
                    return user; // Returns the fully populated user object
                } // End of resultSet check
            } // End of try ResultSet
        } catch (SQLException e) { // Catches database errors
            throw buildDataException("read by email", e); // Wraps and rethrows exception
        } // End of try-catch

        return null; // Returns null if no user matched the email
    } // End of findByEmail method

    /**
     * Checks if an email is already registered in the system.
     * @param email The email to check.
     * @return true if exists, false otherwise.
     * @throws SQLDataException if query fails.
     */
    public boolean emailExists(String email) throws SQLDataException { // Utility method for uniqueness check
        return findByEmail(email) != null; // Simply reuses findByEmail and checks for null
    } // End of emailExists method

    /**
     * Internal helper to run a SELECT query and map all rows to a list of Users.
     * @param sql The SQL query string.
     * @param binder Functional interface to bind parameters to the statement.
     * @return List of Users.
     * @throws SQLDataException if execution fails.
     */
    private List<User> runUserQuery(String sql, StatementBinder binder) throws SQLDataException { // Centralized query runner
        List<User> users = new ArrayList<>(); // Initializes empty results list

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) { // Prepares the query
            if (binder != null) { // Checks if any parameters need binding
                binder.bind(preparedStatement); // Executes the binding callback
            } // End of binder check

            try (ResultSet resultSet = preparedStatement.executeQuery()) { // Executes and gets results
                while (resultSet.next()) { // Iterates through all rows in result set
                    User user = new User(); // Creates a new User for each row
                    user.setId(resultSet.getInt("id")); // Maps ID
                    user.setEmail(resultSet.getString("email")); // Maps email
                    user.setRoles(resultSet.getString("roles")); // Maps roles
                    user.setPassword(resultSet.getString("password")); // Maps password
                    user.setFirstName(resultSet.getString("first_name")); // Maps first name
                    user.setLastName(resultSet.getString("last_name")); // Maps last name
                    user.setAvatar(resultSet.getString("avatar")); // Maps avatar path
                    
                    String status = resultSet.getString("status"); // Retrieves status
                    if (status != null) user.setStatus(status); // Sets status
                    
                    user.setGoogleAuthenticatorSecret(resultSet.getString("google_authenticator_secret")); // Maps 2FA secret
                    user.setGoogleAuthenticatorSecretPending(resultSet.getString("google_authenticator_secret_pending")); // Maps pending 2FA secret
                    user.setIs2faSetupInProgress(resultSet.getBoolean("is_2fa_setup_in_progress")); // Maps 2FA progress flag
                    user.setFaceEncoding(resultSet.getString("face_encoding")); // Maps face embedding
                    user.setFaceAuthEnabled(resultSet.getBoolean("face_auth_enabled")); // Maps face auth flag
                    user.setFailedAttempts(resultSet.getInt("failed_attempts")); // Maps failed attempts
                    user.setLockoutTime(resultSet.getTimestamp("lockout_time")); // Maps lockout time
                    user.setCreatedAt(resultSet.getTimestamp("created_at")); // Maps creation time
                    user.setUpdatedAt(resultSet.getTimestamp("updated_at")); // Maps update time
                    
                    users.add(user); // Adds the user to the list
                } // End of row iteration
            } // End of try ResultSet
        } catch (SQLException e) { // Catches database errors
            throw buildDataException("read", e); // Wraps and rethrows exception
        } // End of try-catch

        return users; // Returns the final list of users
    } // End of runUserQuery helper

    /**
     * Safely resolves a sort key into a valid database column name.
     * Prevents SQL injection by only allowing predefined keys.
     */
    private String resolveSortableColumn(String sortBy) { // Sort column resolver
        if (sortBy == null) { // Checks for null input
            return "id"; // Defaults to sorting by ID
        } // End of null check

        switch (sortBy.trim().toLowerCase()) { // Normalizes and switches on sort key
            case "id": // Case for ID
                return "id"; // Returns column 'id'
            case "email": // Case for email
                return "email"; // Returns column 'email'
            case "first name": // Case for first name variations
            case "first_name": // Case for first_name
                return "first_name"; // Returns column 'first_name'
            case "last name": // Case for last name variations
            case "last_name": // Case for last_name
                return "last_name"; // Returns column 'last_name'
            case "role": // Case for role variations
            case "roles": // Case for roles
                return "roles"; // Returns column 'roles'
            default: // Default case for unknown keys
                return "id"; // Falls back to ID
        } // End of switch
    } // End of resolveSortableColumn helper

    /**
     * Helper to set a last name or NULL in a prepared statement.
     */
    private void setNullableLastName(PreparedStatement preparedStatement, int index, String lastName) throws SQLException { // Nullable last name handler
        if (lastName == null || lastName.trim().isEmpty()) { // Checks if name is null or empty
            preparedStatement.setNull(index, Types.VARCHAR); // Sets SQL NULL for the column
            return; // Exits helper
        } // End of null check
        preparedStatement.setString(index, lastName.trim()); // Sets the trimmed last name string
    } // End of setNullableLastName helper

    /**
     * General helper to wrap SQLException into a more user-friendly SQLDataException.
     */
    private SQLDataException buildDataException(String action, SQLException exception) { // Exception wrapper
        SQLDataException sqlDataException = new SQLDataException("Unable to " + action + " user: " + exception.getMessage()); // Creates new exception with context
        sqlDataException.initCause(exception); // Sets original exception as cause for debugging
        return sqlDataException; // Returns the wrapped exception
    } // End of buildDataException helper

    /**
     * Helper to set a generic string or NULL in a prepared statement.
     */
    private void setNullableString(PreparedStatement preparedStatement, int index, String value) throws SQLException { // Nullable string handler
        if (value == null || value.trim().isEmpty()) { // Checks if string is null or empty
            preparedStatement.setNull(index, Types.VARCHAR); // Sets SQL NULL
            return; // Exits helper
        } // End of null check
        preparedStatement.setString(index, value.trim()); // Sets the trimmed string
    } // End of setNullableString helper

    /**
     * Functional interface used by runUserQuery for parameter binding callbacks.
     */
    @FunctionalInterface // Indicates this interface only has one abstract method
    private interface StatementBinder { // Local functional interface
        void bind(PreparedStatement statement) throws SQLException; // Method to bind parameters
    } // End of StatementBinder interface
} // End of ServiceUser class
