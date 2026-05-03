package Services;

import Models.AdminLog;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminLogService {

    private final Connection connection;

    public AdminLogService() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    public void logAction(int adminId, String actionType, Integer targetId, String details) {
        String sql = "INSERT INTO admin_log (admin_id, action_type, target_id, details) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setString(2, actionType);
            if (targetId != null) {
                ps.setInt(3, targetId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save admin log: " + e.getMessage());
        }
    }

    public List<AdminLog> getAllLogs() {
        List<AdminLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, " +
                     "u_admin.first_name as admin_fname, u_admin.last_name as admin_lname, u_admin.email as admin_email, " +
                     "u_target.email as target_email " +
                     "FROM admin_log al " +
                     "LEFT JOIN user u_admin ON al.admin_id = u_admin.id " +
                     "LEFT JOIN user u_target ON al.target_id = u_target.id " +
                     "ORDER BY al.created_at DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                AdminLog log = new AdminLog();
                log.setId(rs.getInt("id"));
                log.setAdminId(rs.getInt("admin_id"));
                log.setActionType(rs.getString("action_type"));
                int tId = rs.getInt("target_id");
                log.setTargetId(rs.wasNull() ? null : tId);
                log.setDetails(rs.getString("details"));
                log.setCreatedAt(rs.getTimestamp("created_at"));
                
                String firstName = rs.getString("admin_fname");
                String lastName = rs.getString("admin_lname");
                log.setAdminName(((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim());
                log.setAdminEmail(rs.getString("admin_email"));
                log.setTargetEmail(rs.getString("target_email"));
                
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch admin logs: " + e.getMessage());
        }
        return logs;
    }
}
