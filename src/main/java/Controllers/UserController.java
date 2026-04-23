package Controllers;

import Models.User;
import Services.ServiceUser;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLDataException;
import java.util.List;
import java.util.regex.Pattern;

public class UserController {

    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_NORMAL_USER = "Normal User";

    private final ServiceUser serviceUser;

    public UserController() {
        this.serviceUser = new ServiceUser();
    }

    public User login(String email, String password) throws SQLDataException {
        if (!isValidEmail(email)) {
            throw new SQLDataException("Provide a valid email address.");
        }

        if (password == null || password.isEmpty()) {
            throw new SQLDataException("Password is required.");
        }

        User user = serviceUser.findByEmail(email.trim());
        if (user == null) {
            throw new SQLDataException("No account found with this email.");
        }

        if (!passwordMatches(password, user.getPassword())) {
            throw new SQLDataException("Invalid email or password.");
        }

        return user;
    }

    public User register(String email, String password, String firstName, String lastName) throws SQLDataException {
        if (!isValidEmail(email)) {
            throw new SQLDataException("Provide a valid email address.");
        }

        if (!isStrongPassword(password)) {
            throw new SQLDataException("Password must contain at least 6 characters.");
        }

        if (!isValidName(firstName)) {
            throw new SQLDataException("First name is required and must contain letters.");
        }

        if (lastName != null && !lastName.trim().isEmpty() && !isValidName(lastName)) {
            throw new SQLDataException("Last name must contain letters only.");
        }

        if (serviceUser.emailExists(email.trim())) {
            throw new SQLDataException("An account with this email already exists.");
        }

        User user = new User(
                email.trim(),
                toDatabaseRole(ROLE_NORMAL_USER),
                hashPassword(password),
                firstName.trim(),
                normalizeNullable(lastName)
        );

        serviceUser.ajouter(user);
        return login(email, password);
    }

    public void createUser(User user, String roleLabel) throws SQLDataException {
        validateUser(user, false);
        user.setRoles(toDatabaseRole(roleLabel));
        user.setPassword(normalizePasswordForStorage(user.getPassword()));
        user.setLastName(normalizeNullable(user.getLastName()));
        serviceUser.ajouter(user);
    }

    public void updateUser(User user, String roleLabel) throws SQLDataException {
        if (user == null || user.getId() <= 0) {
            throw new SQLDataException("A valid selected user is required for update.");
        }
        validateUser(user, true);
        user.setRoles(toDatabaseRole(roleLabel));
        user.setPassword(normalizePasswordForStorage(user.getPassword()));
        user.setLastName(normalizeNullable(user.getLastName()));
        serviceUser.modifier(user);
    }

    public void deleteUserById(int id) throws SQLDataException {
        if (id <= 0) {
            throw new SQLDataException("A valid selected user is required for delete.");
        }

        User user = new User();
        user.setId(id);
        serviceUser.supprimer(user);
    }

    public List<User> searchAndSortUsers(String searchText, String sortBy, boolean ascending) throws SQLDataException {
        return serviceUser.rechercherEtTrier(searchText, sortBy, ascending);
    }

    public int[] getStatistics() throws SQLDataException {
        return serviceUser.recupererStatistiques();
    }

    public String toDatabaseRole(String roleLabel) throws SQLDataException {
        if (roleLabel == null) {
            throw new SQLDataException("Role is required.");
        }

        if (ROLE_ADMIN.equalsIgnoreCase(roleLabel)) {
            return "[\"ROLE_ADMIN\"]";
        }

        if (ROLE_NORMAL_USER.equalsIgnoreCase(roleLabel)) {
            return "[\"ROLE_USER\"]";
        }

        throw new SQLDataException("Role must be Admin or Normal User.");
    }

    public String toRoleLabel(String dbRole) {
        if (dbRole == null) {
            return ROLE_NORMAL_USER;
        }

        String normalized = dbRole.toLowerCase();
        if (normalized.contains("role_admin")) {
            return ROLE_ADMIN;
        }

        return ROLE_NORMAL_USER;
    }

    public boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }

        String value = email.trim();
        if (value.isEmpty()) {
            return false;
        }

        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.compile(regex).matcher(value).matches();
    }

    public boolean isStrongPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public boolean isValidName(String name) {
        if (name == null) {
            return false;
        }

        String value = name.trim();
        if (value.isEmpty()) {
            return false;
        }

        return Pattern.compile("^[\\p{L} '-]{1,50}$").matcher(value).matches();
    }

    private void validateUser(User user, boolean update) throws SQLDataException {
        if (user == null) {
            throw new SQLDataException("User payload is required.");
        }

        if (!isValidEmail(user.getEmail())) {
            throw new SQLDataException("Provide a valid email address.");
        }

        if (!isStrongPassword(user.getPassword())) {
            throw new SQLDataException("Password must contain at least 6 characters.");
        }

        if (!isValidName(user.getFirstName())) {
            throw new SQLDataException("First name is required and must contain letters.");
        }

        if (user.getLastName() != null && !user.getLastName().trim().isEmpty() && !isValidName(user.getLastName())) {
            throw new SQLDataException("Last name must contain letters only.");
        }

        if (update && user.getId() <= 0) {
            throw new SQLDataException("Selected user id is required for update.");
        }
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }

        if (storedPassword.equals(rawPassword)) {
            return true;
        }

        return storedPassword.equals(hashPassword(rawPassword));
    }

    private String normalizePasswordForStorage(String password) {
        if (password == null) {
            return null;
        }

        String trimmed = password.trim();
        if (trimmed.matches("^[a-fA-F0-9]{64}$")) {
            return trimmed.toLowerCase();
        }

        return hashPassword(password);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hashed) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to hash password.", e);
        }
    }

    private String normalizeNullable(String text) {
        if (text == null) {
            return null;
        }

        String value = text.trim();
        return value.isEmpty() ? null : value;
    }
}