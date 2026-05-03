package Models;

public class User {

    private int id;
    private String email;
    private String roles;
    private String password;
    private String firstName;
    private String lastName;
    private String avatar;
    private String status = "UNBLOCKED";
    
    // 2FA Fields
    private String googleAuthenticatorSecret;
    private String googleAuthenticatorSecretPending;
    private boolean is2faSetupInProgress;
    private String faceEncoding;
    private boolean faceAuthEnabled = false;
    private int failedAttempts = 0;
    private java.sql.Timestamp lockoutTime;
    
    // Schema Sync Fields
    private String googleId;
    private String phoneNumber;
    private String dataFaceApi;
    private java.sql.Timestamp createdAt;
    private java.sql.Timestamp updatedAt;

    public User() {
    }

    public User(int id, String email, String roles, String password, String firstName, String lastName) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDataFaceApi() {
        return dataFaceApi;
    }

    public void setDataFaceApi(String dataFaceApi) {
        this.dataFaceApi = dataFaceApi;
    }

    public java.sql.Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.sql.Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public java.sql.Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.sql.Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFaceEncoding() {
        return faceEncoding;
    }

    public void setFaceEncoding(String faceEncoding) {
        this.faceEncoding = faceEncoding;
    }

    public boolean isFaceAuthEnabled() {
        return faceAuthEnabled;
    }

    public void setFaceAuthEnabled(boolean faceAuthEnabled) {
        this.faceAuthEnabled = faceAuthEnabled;
    }

    public User(String email, String roles, String password, String firstName, String lastName) {
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGoogleAuthenticatorSecret() {
        return googleAuthenticatorSecret;
    }

    public void setGoogleAuthenticatorSecret(String googleAuthenticatorSecret) {
        this.googleAuthenticatorSecret = googleAuthenticatorSecret;
    }

    public String getGoogleAuthenticatorSecretPending() {
        return googleAuthenticatorSecretPending;
    }

    public void setGoogleAuthenticatorSecretPending(String googleAuthenticatorSecretPending) {
        this.googleAuthenticatorSecretPending = googleAuthenticatorSecretPending;
    }

    public boolean isIs2faSetupInProgress() {
        return is2faSetupInProgress;
    }

    public void setIs2faSetupInProgress(boolean is2faSetupInProgress) {
        this.is2faSetupInProgress = is2faSetupInProgress;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public boolean isAdmin() {
        return roles != null && roles.toUpperCase().contains("ROLE_ADMIN");
    }

    public java.sql.Timestamp getLockoutTime() {
        return lockoutTime;
    }

    public void setLockoutTime(java.sql.Timestamp lockoutTime) {
        this.lockoutTime = lockoutTime;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", roles='" + roles + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", avatar='" + avatar + '\'' +
                ", status='" + status + '\'' +
                ", googleAuthenticatorSecret='" + googleAuthenticatorSecret + '\'' +
                ", googleAuthenticatorSecretPending='" + googleAuthenticatorSecretPending + '\'' +
                ", is2faSetupInProgress=" + is2faSetupInProgress +
                ", failedAttempts=" + failedAttempts +
                ", lockoutTime=" + lockoutTime +
                '}';
    }
}