package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    private final   String url="jdbc:mysql://localhost:3306/pharm";
    private   final   String user ="root";
    private   final String pws ="";

    private Connection connection;
    private static MyConnection instance;
    private MyConnection(){
        try {
            connection= DriverManager.getConnection(url,user,pws);
            System.out.println("connecter a la base de données");
        } catch (SQLException e) {
            System.err.println(e.getMessage());    }
    }
    public static MyConnection getInstance(){
        if (instance==null){
            instance= new MyConnection();

        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}