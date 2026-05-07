package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
    private final String URl = "jdbc:mysql://localhost:3306/pharmaxjava";
    private final String USERNAME = "root";
    private final String PASSWORD = "";
    
    private Connection connection;
    private static MyDatabase instance;

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) { 
                reconnect();
            }
        } catch (SQLException e) {
            reconnect();
        }
        return connection;
    }

    private void reconnect() {
        try {
            connection = DriverManager.getConnection(URl, USERNAME, PASSWORD);
            System.out.println("Database connection re-established.");
        } catch (SQLException e) {
            System.err.println("Failed to reconnect to database: " + e.getMessage());
        }
    }

    private MyDatabase() {
        reconnect();
    }
}
