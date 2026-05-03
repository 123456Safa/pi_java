package Models;

import java.sql.Timestamp;

public class AdminLog {
    private int id;
    private int adminId;
    private String actionType;
    private Integer targetId; // Can be null
    private String details;
    private Timestamp createdAt;
    private String adminName;
    private String adminEmail;
    private String targetEmail;

    public AdminLog() {}

    public AdminLog(int adminId, String actionType, Integer targetId, String details) {
        this.adminId = adminId;
        this.actionType = actionType;
        this.targetId = targetId;
        this.details = details;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public Integer getTargetId() { return targetId; }
    public void setTargetId(Integer targetId) { this.targetId = targetId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }

    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public String getTargetEmail() { return targetEmail; }
    public void setTargetEmail(String targetEmail) { this.targetEmail = targetEmail; }
}
