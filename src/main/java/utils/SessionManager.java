package utils; // Defines the package for utility classes

import Models.User; // Imports the User model for session storage

/**
 * SessionManager implements the Singleton pattern to track the currently logged-in user.
 * It provides a global point of access to session data across the application.
 */
public class SessionManager { // Main class for session state management
    private static SessionManager instance; // Static instance for the Singleton pattern
    private User currentUser; // Holds the User object for the currently authenticated session

    /**
     * Private constructor prevents external instantiation.
     */
    private SessionManager() {} // Private constructor

    /**
     * Retrieves the unique instance of the SessionManager.
     * @return the Singleton instance.
     */
    public static SessionManager getInstance() { // Static accessor method
        if (instance == null) { // Checks if instance has been created
            instance = new SessionManager(); // Initializes instance if missing
        } // End of null check
        return instance; // Returns the global instance
    } // End of getInstance method

    /**
     * Gets the currently logged-in user.
     * @return the User object, or null if no one is logged in.
     */
    public User getCurrentUser() { // Getter for session user
        return currentUser; // Returns the user object
    } // End of getCurrentUser method

    /**
     * Sets the user for the current session after a successful login.
     * @param user The User object to be stored in session.
     */
    public void setCurrentUser(User user) { // Setter for session user
        this.currentUser = user; // Updates the session state
    } // End of setCurrentUser method

    /**
     * Clears the current session, effectively logging the user out.
     */
    public void logout() { // Logout logic
        this.currentUser = null; // Wipes the session data
    } // End of logout method
} // End of SessionManager class
