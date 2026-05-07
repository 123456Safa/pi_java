package utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckDB {
    public static void main(String[] args) {
        try {
            Connection conn = MyConnection.getInstance().getConnection();
            Statement st = conn.createStatement();
            
            System.out.println("Searching for table name...");
            ResultSet rs = st.executeQuery("SELECT table_name FROM information_schema.columns WHERE column_name = 'last_name' AND table_schema = 'pharm'");
            while(rs.next()) {
                System.out.println("Table found: " + rs.getString("table_name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
