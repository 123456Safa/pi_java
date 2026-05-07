package com.pharmax.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection — Unified singleton for the blog module.
 * Points to the same 'pharm' database as the rest of the application.
 *
 * Usage:
 *   Connection cnx = DatabaseConnection.getInstance();
 *   try (PreparedStatement ps = cnx.prepareStatement(sql)) { ... }
 *
 * WARNING: Do NOT close the returned connection — it is a shared singleton.
 */
public class DatabaseConnection {

    // ── Unified database URL — same DB as MyConnection / MyDataBase ──
    private static final String URL      = "jdbc:mysql://localhost:3306/pharm?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&connectTimeout=3000&socketTimeout=5000&autoReconnect=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private Connection connection;
    private static DatabaseConnection instance;

    private DatabaseConnection() {
        initConnection();
    }

    private void initConnection() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✓ [Blog] Connected to database 'pharm'");
        } catch (SQLException e) {
            System.err.println("❌ [Blog] Database connection failed: " + e.getMessage());
            throw new RuntimeException(
                "Could not connect to MySQL 'pharm' database. Ensure XAMPP MySQL is running.", e);
        }
    }

    public static DatabaseConnection getInstanceObj() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Returns a valid singleton Connection, reconnecting if needed.
     */
    public static Connection getInstance() {
        try {
            if (instance == null || instance.connection == null || instance.connection.isClosed()) {
                instance = new DatabaseConnection();
            }
            if (!instance.connection.isValid(2)) {
                System.err.println("⚠ [Blog] Connection invalid, reconnecting...");
                instance = new DatabaseConnection();
            }
        } catch (SQLException e) {
            System.err.println("❌ [Blog] Connection check error: " + e.getMessage());
            instance = new DatabaseConnection();
        }
        return instance.connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public static void close() {
        if (instance != null && instance.connection != null) {
            try {
                instance.connection.close();
                System.out.println("🔒 [Blog] Database connection closed.");
            } catch (SQLException e) {
                System.err.println("⚠ [Blog] Error closing connection: " + e.getMessage());
            }
        }
    }
}
