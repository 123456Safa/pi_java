package com.pharmax.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection — Singleton utility for MySQL/MariaDB connectivity.
 * Based on MyDatabase singleton pattern.
 *
 * Usage:
 *   Connection cnx = DatabaseConnection.getInstance();
 */
public class DatabaseConnection {

    private final String URl = "jdbc:mysql://localhost:3306/pharmax";
    private final String USERNAME = "root";
    private final String PASSWORD = "";

    private Connection connection;
    private static DatabaseConnection instance;

    public static DatabaseConnection getInstanceObj() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Returns the singleton Connection instance.
     * Automatically reconnects if the previous connection was closed.
     */
    public static Connection getInstance() {
        try {
            if (instance == null || instance.connection == null || instance.connection.isClosed()) {
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

    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(URl, USERNAME, PASSWORD);
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            System.err.println("❌ Impossible de se connecter à la base de données: " + e.getMessage());
            throw new RuntimeException(
                "Impossible de se connecter à MySQL sur " + URl +
                ". Vérifiez que le serveur MySQL est démarré et que la base 'pharmax' existe.", e);
        }
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
