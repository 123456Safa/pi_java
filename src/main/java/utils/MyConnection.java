package utils;

import java.sql.Connection;
import java.sql.DriverManager;

public class MyConnection {

    private static Connection cnx;

    public static Connection getInstance() {

        if (cnx == null) {
            try {
                cnx = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/pharmax",
                        "root",
                        ""
                );
                System.out.println("DB Connected ✅");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return cnx;
    }
}