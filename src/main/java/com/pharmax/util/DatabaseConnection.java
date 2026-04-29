package com.pharmax.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConnection — Singleton utility for MySQL/MariaDB connectivity.
 * Based on MyDatabase singleton pattern.
 *
 * IMPORTANT: The singleton Connection must NOT be closed by service code.
 * Services should call {@code DatabaseConnection.getInstance()} and use
 * the returned Connection without wrapping it in try-with-resources.
 * Only PreparedStatement / ResultSet should be auto-closed.
 *
 * Usage:
 *   Connection cnx = DatabaseConnection.getInstance();
 *   try (PreparedStatement ps = cnx.prepareStatement(sql)) { ... }
 */
public class DatabaseConnection {

    private final String URL;
    private final String USERNAME;
    private final String PASSWORD;
    
    private Connection connection;
    private static DatabaseConnection instance;
    
    private DatabaseConnection() {
        // Load properties from db.properties file
        Properties props = loadProperties();
        this.URL = props.getProperty("db.url", "jdbc:mysql://localhost:3306/pharmax?connectTimeout=3000&socketTimeout=5000&autoReconnect=true");
        this.USERNAME = props.getProperty("db.user", "root");
        this.PASSWORD = props.getProperty("db.password", "");
        initConnection();
    }
    
    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                System.out.println("⚠ db.properties not found, using defaults");
            }
        } catch (IOException e) {
            System.err.println("⚠ Could not load db.properties: " + e.getMessage());
        }
        return props;
    }
    
    private void initConnection() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✓ Connected to database successfully");
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            throw new RuntimeException(
                "Could not connect to MySQL at " + URL + 
                ". Ensure MySQL is running and database 'pharmax' exists.", e);
        }
    }

    public static DatabaseConnection getInstanceObj() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Returns the singleton Connection instance.
     * Automatically reconnects if the previous connection was closed or lost.
     *
     * WARNING: Do NOT use the returned Connection in a try-with-resources block.
     * Closing the singleton connection forces expensive reconnections.
     */
    public static Connection getInstance() {
        try {
            if (instance == null || instance.connection == null || instance.connection.isClosed()) {
                instance = new DatabaseConnection();
            }
            // Also validate the connection is still usable
            if (!instance.connection.isValid(2)) {
                System.err.println("⚠ Connection invalid, reconnecting...");
                instance = new DatabaseConnection();
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification connexion: " + e.getMessage());
            instance = new DatabaseConnection();
        }
        return instance.connection;
    }

    public Connection getConnection() {
        return connection;
    }



    /**
     * Close the database connection explicitly.
     */
    public static void close() {
        if (instance != null && instance.connection != null) {
            try {
                instance.connection.close();
                System.out.println("🔒 Connexion à la base de données fermée.");
            } catch (SQLException e) {
                System.err.println("⚠️  Erreur lors de la fermeture: " + e.getMessage());
            }
        }
    }
}
