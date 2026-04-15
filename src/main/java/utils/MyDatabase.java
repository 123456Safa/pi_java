package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
  private   final String URl = "jdbc:mysql://localhost:3306/pharmax";
    private final String USERNAME = "root";
    private final String PASSWORD = "";
private Connection connection;
private  static MyDatabase instance ;

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private MyDatabase() {

        try {
            connection = DriverManager.getConnection(URl,USERNAME,PASSWORD);
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
