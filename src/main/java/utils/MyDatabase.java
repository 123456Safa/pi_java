package utils; // Defines the package for utility classes

import java.sql.Connection; // Imports the SQL Connection interface
import java.sql.DriverManager; // Imports the Driver Manager for establishing connections
import java.sql.SQLException; // Imports the SQL Exception class for error handling

/**
 * MyDatabase class implements the Singleton pattern to manage a single
 * database connection throughout the application lifecycle.
 */
public class MyDatabase { // Main class for database management
    // Database connection parameters
    private final String URl = "jdbc:mysql://localhost:3306/pharmax"; // JDBC URL for MySQL database 'pharmax'
    private final String USERNAME = "root"; // Default MySQL username
    private final String PASSWORD = ""; // Default empty password for local development
    
    private Connection connection; // Holds the active SQL connection object
    private static MyDatabase instance; // Static instance for the Singleton pattern

    /**
     * Provides access to the single instance of MyDatabase.
     * @return the unique MyDatabase instance.
     */
    public static MyDatabase getInstance() { // Static accessor method
        if (instance == null) { // Checks if instance has been created yet
            instance = new MyDatabase(); // Creates the instance if it doesn't exist
        } // End of null check
        return instance; // Returns the singleton instance
    } // End of getInstance method

    /**
     * Retrieves the active database connection, ensuring it is open and valid.
     * @return the SQL Connection object.
     */
    public Connection getConnection() { // Connection getter
        try { // Error handling block
            // Checks if connection is null, closed, or unresponsive (2-second timeout)
            if (connection == null || connection.isClosed() || !connection.isValid(2)) { 
                reconnect(); // Attempts to re-establish the connection
            } // End of validation check
        } catch (SQLException e) { // Catches SQL-related errors
            reconnect(); // Retries reconnection on exception
        } // End of try-catch
        return connection; // Returns the verified connection
    } // End of getConnection method

    /**
     * Internal method to establish or refresh the connection to MySQL.
     */
    private void reconnect() { // Private reconnection logic
        try { // Error handling block
            // Uses DriverManager to create a new connection using credentials
            connection = DriverManager.getConnection(URl, USERNAME, PASSWORD); 
            System.out.println("Database connection re-established."); // Console log for success
        } catch (SQLException e) { // Catches connection failures
            System.err.println("Failed to reconnect to database: " + e.getMessage()); // Logs error to stderr
        } // End of try-catch
    } // End of reconnect method

    /**
     * Private constructor to prevent direct instantiation.
     */
    private MyDatabase() { // Private constructor
        reconnect(); // Establishes initial connection during first instantiation
    } // End of constructor
} // End of MyDatabase class
