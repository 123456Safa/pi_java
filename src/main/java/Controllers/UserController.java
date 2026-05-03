package Controllers; // Defines the package for business logic controllers

import Models.User; // Imports the User model for data manipulation
import Services.GoogleAuthService; // Imports Google OAuth service logic
import Services.ServiceUser; // Imports database CRUD service for users
import Services.EmailService; // Imports service for sending emails
import Services.TwoFactorAuthService; // Imports TOTP/2FA logic
import Services.AdminLogService; // Imports audit logging service
import utils.SessionManager; // Imports session utility for current user tracking

import java.io.IOException; // Standard exception for IO operations
import java.nio.charset.StandardCharsets; // For UTF-8 character encoding
import java.security.GeneralSecurityException; // Base class for security-related exceptions
import java.security.MessageDigest; // For hashing passwords (SHA-256)
import java.security.NoSuchAlgorithmException; // Thrown if SHA-256 is not available
import java.sql.SQLDataException; // Exception for SQL data-related errors
import java.util.HashMap; // Map implementation for storing reset codes
import java.util.List; // For handling lists of users
import java.util.Map; // Interface for key-value stores
import java.util.Random; // For generating random reset codes
import java.util.UUID; // For generating unique identifiers
import java.util.regex.Pattern; // For regex-based validation (email, names)

/**
 * UserController orchestrates the interaction between the UI and Services.
 * It handles authentication, registration, security (2FA, lockout), and admin actions.
 */
public class UserController { // Main controller class for user-related workflows

    public static final String ROLE_ADMIN = "Admin"; // Human-readable admin role constant
    public static final String ROLE_NORMAL_USER = "Normal User"; // Human-readable user role constant

    private static final int MAX_FAILED_ATTEMPTS = 5; // Maximum failed logins before lockout
    private static final int LOCKOUT_DURATION_MINUTES = 15; // Duration of account lockout in minutes

    private final ServiceUser serviceUser; // Instance of the user database service
    private final TwoFactorAuthService twoFactorAuthService; // Instance of the 2FA service
    private final AdminLogService adminLogService; // Instance of the audit logging service

    private final Map<String, ResetCodeData> resetCodes = new HashMap<>(); // Temporary store for password reset codes

    /**
     * Inner class to represent reset code data with an expiry timestamp.
     */
    private static class ResetCodeData { // Helper class for memory-stored reset codes
        String code; // The 6-digit reset code
        long expiryTimeMillis; // The system time when the code expires

        ResetCodeData(String code, long expiryTimeMillis) { // Constructor for reset data
            this.code = code; // Sets code
            this.expiryTimeMillis = expiryTimeMillis; // Sets expiry
        } // End of inner constructor
    } // End of ResetCodeData inner class

    /**
     * Constructor initializes required services.
     */
    public UserController() { // Constructor
        this.serviceUser = new ServiceUser(); // Initializes database service
        this.twoFactorAuthService = new TwoFactorAuthService(); // Initializes 2FA service
        this.adminLogService = new AdminLogService(); // Initializes logging service
    } // End of constructor

    /**
     * Handles traditional email/password login logic with lockout protection.
     */
    public User login(String email, String password) throws SQLDataException { // Main login method
        if (!isValidEmail(email)) { // Validates email format
            throw new SQLDataException("Provide a valid email address."); // Throws on invalid format
        } // End of email check

        if (password == null || password.isEmpty()) { // Validates password presence
            throw new SQLDataException("Password is required."); // Throws on missing password
        } // End of password check

        User user = serviceUser.findByEmail(email.trim()); // Attempts to find user in DB
        if (user == null) { // Checks if user exists
            throw new SQLDataException("No account found with this email."); // Throws on unknown user
        } // End of user check

        // Check if account is locked
        if (user.getLockoutTime() != null) { // Checks if a lockout timestamp exists
            long lockoutMillis = user.getLockoutTime().getTime(); // Gets lockout start time
            long currentMillis = System.currentTimeMillis(); // Gets current system time
            long diffMinutes = (currentMillis - lockoutMillis) / (60 * 1000); // Calculates elapsed minutes

            if (diffMinutes < LOCKOUT_DURATION_MINUTES) { // Checks if lockout is still active
                long remainingMinutes = LOCKOUT_DURATION_MINUTES - diffMinutes; // Calculates remaining time
                throw new SQLDataException("Account is locked due to too many failed attempts. Try again in " + remainingMinutes + " minutes."); // Throws lockout error
            } else { // Lockout period has expired
                user.setFailedAttempts(0); // Resets failed count
                user.setLockoutTime(null); // Clears lockout timestamp
                serviceUser.modifier(user); // Updates DB
            } // End of time check
        } // End of lockout check

        if ("BLOCKED".equalsIgnoreCase(user.getStatus())) { // Checks for administrative block
            throw new SQLDataException("Your account has been blocked. Please contact an administrator."); // Throws on blocked state
        } // End of block check

        if (!passwordMatches(password, user.getPassword())) { // Validates password hash
            int attempts = user.getFailedAttempts() + 1; // Increments failed count
            user.setFailedAttempts(attempts); // Updates object

            if (attempts >= MAX_FAILED_ATTEMPTS) { // Checks if max attempts reached
                user.setLockoutTime(new java.sql.Timestamp(System.currentTimeMillis())); // Sets lockout timestamp
                serviceUser.modifier(user); // Updates DB
                throw new SQLDataException("Account locked for " + LOCKOUT_DURATION_MINUTES + " minutes due to 5 failed attempts."); // Throws lockout error
            } else { // Attempts remain
                serviceUser.modifier(user); // Updates DB count
                int remaining = MAX_FAILED_ATTEMPTS - attempts; // Calculates remaining attempts
                throw new SQLDataException("Invalid email or password. " + remaining + " attempts remaining."); // Throws error with hint
            } // End of attempt limit check
        } // End of password match check

        // Successful login - reset failed attempts
        if (user.getFailedAttempts() > 0 || user.getLockoutTime() != null) { // Checks if user was previously penalized
            user.setFailedAttempts(0); // Resets count
            user.setLockoutTime(null); // Clears lockout
            serviceUser.modifier(user); // Updates DB
        } // End of reset check

        return user; // Returns the authenticated user
    } // End of login method

    /**
     * Initiates a password reset by sending a 6-digit code via email.
     */
    public void requestPasswordReset(String email) throws Exception { // Password reset request
        if (!isValidEmail(email)) { // Validates email format
            throw new SQLDataException("Provide a valid email address."); // Throws on error
        } // End of email check
        User user = serviceUser.findByEmail(email.trim()); // Checks if user exists
        if (user == null) { // User check
            throw new SQLDataException("No account found with this email."); // Throws on error
        } // End of check

        // Generate 6-digit code
        String code = String.format("%06d", new Random().nextInt(999999)); // Generates numeric string
        // Valid for 10 minutes
        long expiry = System.currentTimeMillis() + (10 * 60 * 1000); // Sets expiry time
        
        resetCodes.put(email.trim(), new ResetCodeData(code, expiry)); // Stores code in memory
        
        EmailService.sendResetCode(email.trim(), code); // Dispatches email
    } // End of requestPasswordReset

    /**
     * Verifies that the provided code matches the one sent to the email and isn't expired.
     */
    public void verifyResetCode(String email, String code) throws Exception { // Code verification logic
        if (email == null || code == null) { // Validation
            throw new Exception("Email and code are required."); // Throws on missing data
        } // End validation
        ResetCodeData data = resetCodes.get(email.trim()); // Retrieves code from memory
        if (data == null) { // Missing check
            throw new Exception("No reset request found for this email."); // Throws on error
        } // End check
        if (System.currentTimeMillis() > data.expiryTimeMillis) { // Expiry check
            resetCodes.remove(email.trim()); // Cleans up expired data
            throw new Exception("Reset code has expired. Please request a new one."); // Throws on expiry
        } // End expiry check
        if (!data.code.equals(code.trim())) { // Comparison
            throw new Exception("Invalid verification code."); // Throws on mismatch
        } // End mismatch check
        // Code is valid if we reach here
    } // End of verifyResetCode

    /**
     * Resets the user's password in the database.
     */
    public void resetPassword(String email, String newPassword) throws Exception { // Final reset logic
        if (!isStrongPassword(newPassword)) { // Validates password strength
            throw new SQLDataException("Password must contain at least 6 characters."); // Throws on weak pass
        } // End validation
        User user = serviceUser.findByEmail(email.trim()); // Fetches user
        if (user == null) { // Existence check
            throw new SQLDataException("User not found."); // Throws error
        } // End check
        user.setPassword(hashPassword(newPassword)); // Hashes and sets new password
        serviceUser.modifier(user); // Updates DB
        
        // Remove code after successful reset
        resetCodes.remove(email.trim()); // Clean up memory
    } // End of resetPassword

    /**
     * Authenticates a user via Google OAuth 2.0.
     * Opens the system browser for Google login, then:
     *   - If the Google email matches an existing account, logs that user in.
     *   - If no account exists, auto-registers a new user with Google profile data.
     */
    public User loginWithGoogle() throws Exception { // Google OAuth workflow
        // 1. Run the Google OAuth flow (opens browser, waits for callback)
        GoogleAuthService.GoogleUserInfo googleUser = GoogleAuthService.authenticate(); // Executes OAuth handshake

        String email = googleUser.getEmail(); // Extracts email from Google token
        String firstName = googleUser.getFirstName(); // Extracts first name
        String lastName = googleUser.getLastName(); // Extracts last name

        // 2. Check if this Google email already exists in our database
        User existingUser = serviceUser.findByEmail(email); // Database lookup
        if (existingUser != null) { // User already has an account
            if ("BLOCKED".equalsIgnoreCase(existingUser.getStatus())) { // Block check
                throw new SQLDataException("Your account has been blocked. Please contact an administrator."); // Throws on block
            } // End block check
            // Update avatar if it's missing or changed
            if (googleUser.getPicture() != null && !googleUser.getPicture().isEmpty()) { // Avatar check
                existingUser.setAvatar(googleUser.getPicture()); // Updates avatar URL
                serviceUser.modifier(existingUser); // Saves to DB
            } // End avatar update
            // User already registered (via Google or normal signup) — log them in
            SessionManager.getInstance().setCurrentUser(existingUser); // Sets global session
            return existingUser; // Returns user
        } // End existing user logic

        // 3. New Google user — auto-register them
        // Generate a random dummy password (they authenticate via Google, not local password)
        String dummyPassword = hashPassword(UUID.randomUUID().toString()); // Secure random hash

        // Use "N/A" if Google didn't return a first name
        if (firstName == null || firstName.trim().isEmpty()) { // Name fallback
            firstName = "Google User"; // Sets default name
        } // End fallback

        User newUser = new User( // Constructs new user object
                email, // Google email
                toDatabaseRole(ROLE_NORMAL_USER), // Default role
                dummyPassword, // Random password
                firstName.trim(), // Name
                lastName != null && !lastName.trim().isEmpty() ? lastName.trim() : null // Optional last name
        ); // End constructor

        newUser.setAvatar(googleUser.getPicture()); // Sets Google profile picture
        serviceUser.ajouter(newUser); // Saves to DB

        // Return the freshly-created user (with DB-assigned id)
        User result = serviceUser.findByEmail(email); // Refetches for assigned ID
        SessionManager.getInstance().setCurrentUser(result); // Sets session
        return result; // Returns new user
    } // End of loginWithGoogle

    /**
     * Handles manual registration of a new user.
     */
    public User register(String email, String password, String firstName, String lastName) throws SQLDataException { // Registration method
        if (!isValidEmail(email)) { // Validation
            throw new SQLDataException("Provide a valid email address."); // Error
        } // End email check

        if (!isStrongPassword(password)) { // Strength check
            throw new SQLDataException("Password must contain at least 6 characters."); // Error
        } // End pass check

        if (!isValidName(firstName)) { // Name format check
            throw new SQLDataException("First name is required and must contain letters."); // Error
        } // End name check

        if (lastName != null && !lastName.trim().isEmpty() && !isValidName(lastName)) { // Optional name check
            throw new SQLDataException("Last name must contain letters only."); // Error
        } // End last name check

        if (serviceUser.emailExists(email.trim())) { // Uniqueness check
            throw new SQLDataException("An account with this email already exists."); // Error on duplicate
        } // End uniqueness check

        User user = new User( // Constructs user
                email.trim(), // Email
                toDatabaseRole(ROLE_NORMAL_USER), // Default role
                hashPassword(password), // Hashed pass
                firstName.trim(), // First name
                normalizeNullable(lastName) // Nullable last name
        ); // End constructor

        serviceUser.ajouter(user); // Saves to DB
        return login(email, password); // Auto-logs in
    } // End of register method

    /**
     * Admin action to create a user with specific roles.
     */
    public void createUser(User user, String roleLabel) throws SQLDataException { // Admin creation logic
        validateUser(user, false); // Runs full validation
        user.setRoles(toDatabaseRole(roleLabel)); // Converts UI role to DB string
        user.setPassword(normalizePasswordForStorage(user.getPassword())); // Hashes password
        user.setLastName(normalizeNullable(user.getLastName())); // Normalizes last name
        serviceUser.ajouter(user); // Saves to DB

        // Log the action
        User admin = SessionManager.getInstance().getCurrentUser(); // Gets current admin
        if (admin != null) { // Audit log check
            User created = serviceUser.findByEmail(user.getEmail()); // Finds created user ID
            adminLogService.logAction(admin.getId(), "CREATE_USER", created != null ? created.getId() : null, "Created user: " + user.getEmail()); // Logs action
        } // End audit log
    } // End of createUser

    /**
     * Admin action to update user details.
     */
    public void updateUser(User user, String roleLabel) throws SQLDataException { // Admin update logic
        if (user == null || user.getId() <= 0) { // Payload check
            throw new SQLDataException("A valid selected user is required for update."); // Error
        } // End check
        validateUser(user, true); // Validates data
        user.setRoles(toDatabaseRole(roleLabel)); // Converts role
        user.setPassword(normalizePasswordForStorage(user.getPassword())); // Processes password
        user.setLastName(normalizeNullable(user.getLastName())); // Normalizes name
        serviceUser.modifier(user); // Saves to DB

        // Log the action
        User admin = SessionManager.getInstance().getCurrentUser(); // Gets admin
        if (admin != null) { // Audit log
            adminLogService.logAction(admin.getId(), "UPDATE_USER", user.getId(), "Updated user: " + user.getEmail()); // Logs
        } // End audit
    } // End of updateUser

    /**
     * Admin action to update status (Block/Unblock).
     */
    public void updateStatus(int userId, String newStatus) throws SQLDataException { // Status change logic
        serviceUser.modifierStatus(userId, newStatus); // Direct service call for status
        
        // Log the action
        User admin = SessionManager.getInstance().getCurrentUser(); // Gets admin
        if (admin != null) { // Audit log
            adminLogService.logAction(admin.getId(), "UPDATE_STATUS", userId, "Changed status to: " + newStatus); // Logs
        } // End audit
    } // End of updateStatus

    /**
     * Admin action to delete a user by ID.
     */
    public void deleteUserById(int id) throws SQLDataException { // Deletion logic
        if (id <= 0) { // ID validation
            throw new SQLDataException("A valid selected user is required for delete."); // Error
        } // End check

        User user = new User(); // Dummy user object for service requirement
        user.setId(id); // Sets target ID
        serviceUser.supprimer(user); // Executes deletion

        // Log the action
        User admin = SessionManager.getInstance().getCurrentUser(); // Gets admin
        if (admin != null) { // Audit log
            adminLogService.logAction(admin.getId(), "DELETE_USER", id, "Deleted user with ID: " + id); // Logs
        } // End audit
    } // End of deleteUserById

    /**
     * Advanced search and sort bridge.
     */
    public List<User> searchAndSortUsers(String searchText, String sortBy, boolean ascending) throws SQLDataException { // Search bridge
        return serviceUser.rechercherEtTrier(searchText, sortBy, ascending); // Calls service logic
    } // End of searchAndSortUsers

    /**
     * Statistics retrieval bridge.
     */
    public int[] getStatistics() throws SQLDataException { // Stats bridge
        return serviceUser.recupererStatistiques(); // Calls service logic
    } // End of getStatistics

    /**
     * Maps UI role labels to database-compatible JSON role strings.
     */
    public String toDatabaseRole(String roleLabel) throws SQLDataException { // Role mapper
        if (roleLabel == null) { // Validation
            throw new SQLDataException("Role is required."); // Error
        } // End check

        if (ROLE_ADMIN.equalsIgnoreCase(roleLabel)) { // Admin case
            return "[\"ROLE_ADMIN\"]"; // Symfony-style JSON role
        } // End admin check

        return "[\"ROLE_USER\"]"; // Default user role
    } // End of toDatabaseRole

    /**
     * Maps database JSON role strings to UI labels.
     */
    public String toRoleLabel(String dbRole) { // Reverse role mapper
        if (dbRole == null) { // Null check
            return ROLE_NORMAL_USER; // Default
        } // End null check

        String normalized = dbRole.toLowerCase(); // Normalizes for search
        if (normalized.contains("role_admin")) { // Admin check
            return ROLE_ADMIN; // Returns label
        } // End check

        return ROLE_NORMAL_USER; // Returns label
    } // End of toRoleLabel

    /**
     * Regex validation for email addresses.
     */
    public boolean isValidEmail(String email) { // Email validator
        if (email == null) { // Null check
            return false; // Invalid
        } // End null check

        String value = email.trim(); // Trims whitespace
        if (value.isEmpty()) { // Empty check
            return false; // Invalid
        } // End empty check

        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"; // Standard email regex
        return Pattern.compile(regex).matcher(value).matches(); // Matches against regex
    } // End of isValidEmail

    /**
     * Minimum complexity check for passwords.
     */
    public boolean isStrongPassword(String password) { // Password validator
        return password != null && password.length() >= 6; // Simple length check
    } // End of isStrongPassword

    /**
     * Regex validation for names (letters and spaces only).
     */
    public boolean isValidName(String name) { // Name validator
        if (name == null) { // Null check
            return false; // Invalid
        } // End null check

        String value = name.trim(); // Trim
        if (value.isEmpty()) { // Empty check
            return false; // Invalid
        } // End check

        return Pattern.compile("^[\\p{L} '-]{1,50}$").matcher(value).matches(); // International letter support
    } // End of isValidName

    /**
     * Comprehensive validation for User objects before DB submission.
     */
    private void validateUser(User user, boolean update) throws SQLDataException { // Internal validator
        if (user == null) { // Null check
            throw new SQLDataException("User payload is required."); // Error
        } // End check

        if (!isValidEmail(user.getEmail())) { // Email check
            throw new SQLDataException("Provide a valid email address."); // Error
        } // End email check

        if (!isStrongPassword(user.getPassword())) { // Password check
            throw new SQLDataException("Password must contain at least 6 characters."); // Error
        } // End pass check

        if (!isValidName(user.getFirstName())) { // First name check
            throw new SQLDataException("First name is required and must contain letters."); // Error
        } // End name check

        if (user.getLastName() != null && !user.getLastName().trim().isEmpty() && !isValidName(user.getLastName())) { // Last name check
            throw new SQLDataException("Last name must contain letters only."); // Error
        } // End last name check

        if (update && user.getId() <= 0) { // Update ID check
            throw new SQLDataException("Selected user id is required for update."); // Error
        } // End ID check
    } // End of validateUser

    /**
     * Checks if a raw password matches a stored hash.
     */
    private boolean passwordMatches(String rawPassword, String storedPassword) { // Password comparator
        if (rawPassword == null || storedPassword == null) { // Null check
            return false; // Mismatch
        } // End null check

        if (storedPassword.equals(rawPassword)) { // Plain text fallback (not recommended for production)
            return true; // Match
        } // End plain text check

        return storedPassword.equals(hashPassword(rawPassword)); // Compares raw hash to stored hash
    } // End of passwordMatches

    /**
     * Ensures password is consistently hashed before storage.
     */
    private String normalizePasswordForStorage(String password) { // Hash normalizer
        if (password == null) { // Null check
            return null; // Return null
        } // End null check

        String trimmed = password.trim(); // Trim
        if (trimmed.matches("^[a-fA-F0-9]{64}$")) { // Checks if already hashed (SHA-256 is 64 hex chars)
            return trimmed.toLowerCase(); // Returns as is
        } // End hex check

        return hashPassword(password); // Hashes if plain text
    } // End of normalizePasswordForStorage

    /**
     * SHA-256 hashing implementation.
     */
    private String hashPassword(String password) { // Hashing logic
        try { // Error handling
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256"); // Gets instance
            byte[] hashed = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8)); // Calculates hash
            StringBuilder builder = new StringBuilder(); // Container
            for (byte b : hashed) { // Iterates through bytes
                builder.append(String.format("%02x", b)); // Converts byte to hex string
            } // End byte loop
            return builder.toString(); // Returns final hash
        } catch (NoSuchAlgorithmException e) { // Algorithm check
            throw new IllegalStateException("Unable to hash password.", e); // Fatal error
        } // End try-catch
    } // End of hashPassword

    /**
     * Converts empty strings to null for clean database storage.
     */
    private String normalizeNullable(String text) { // String cleaner
        if (text == null) { // Null check
            return null; // Return null
        } // End null check

        String value = text.trim(); // Trim
        return value.isEmpty() ? null : value; // Return null if empty, else trimmed value
    } // End of normalizeNullable

    /**
     * Initiates 2FA setup by generating a secret and QR code image.
     */
    public javafx.scene.image.Image begin2FASetup(User user) throws Exception { // 2FA startup
        String secret = twoFactorAuthService.generateSecretKey(); // Generates TOTP secret
        user.setGoogleAuthenticatorSecretPending(secret); // Stores in pending field
        user.setIs2faSetupInProgress(true); // Flags setup state
        serviceUser.modifier(user); // Saves state to DB
        
        String uri = twoFactorAuthService.generateGoogleAuthenticatorURI(user.getEmail(), secret); // Generates URI for QR
        return twoFactorAuthService.generateQRCodeImage(uri, 200, 200); // Generates JavaFX image
    } // End of begin2FASetup

    /**
     * Finalizes 2FA setup by verifying the first code from the user.
     */
    public void confirm2FASetup(User user, String codeStr) throws Exception { // 2FA confirmation
        int code; // Code storage
        try { // Parsing block
            code = Integer.parseInt(codeStr.trim()); // Converts string to int
        } catch (NumberFormatException e) { // Format check
            throw new Exception("Code must be a 6-digit number."); // Error
        } // End try-catch
        
        if (!twoFactorAuthService.verifyCode(user.getGoogleAuthenticatorSecretPending(), code)) { // Verification check
            throw new Exception("Invalid Authenticator code. Please try again."); // Error on mismatch
        } // End verification check
        
        user.setGoogleAuthenticatorSecret(user.getGoogleAuthenticatorSecretPending()); // Promotes pending to active
        user.setGoogleAuthenticatorSecretPending(null); // Clears pending
        user.setIs2faSetupInProgress(false); // Resets flag
        serviceUser.modifier(user); // Saves to DB
    } // End of confirm2FASetup

    /**
     * Disables 2FA and wipes secrets from the database.
     */
    public void disable2FA(User user) throws Exception { // 2FA deactivation
        user.setGoogleAuthenticatorSecret(null); // Clears secret
        user.setGoogleAuthenticatorSecretPending(null); // Clears pending
        user.setIs2faSetupInProgress(false); // Resets flag
        serviceUser.modifier(user); // Saves to DB
    } // End of disable2FA

    /**
     * Verifies a 2FA code during the login flow.
     */
    public boolean verify2FALogin(User user, String codeStr) throws Exception { // 2FA login verify
        int code; // Code storage
        try { // Parsing block
            code = Integer.parseInt(codeStr.trim()); // Converts
        } catch (NumberFormatException e) { // Format check
            throw new Exception("Code must be a 6-digit number."); // Error
        } // End try-catch
        
        if (!twoFactorAuthService.verifyCode(user.getGoogleAuthenticatorSecret(), code)) { // Verification check
            throw new Exception("Invalid Authenticator code."); // Error on mismatch
        } // End check
        return true; // Match successful
    } // End of verify2FALogin
} // End of UserController class