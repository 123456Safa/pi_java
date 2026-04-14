package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.MyConnection;
import utils.DataInitializer;

import java.sql.Connection;

public class Main extends Application {

    public static void main(String[] args) {
        // Test DB connection
        Connection connection = MyConnection.getInstance().getConnection();
        Connection connection1 = MyConnection.getInstance().getConnection();

        System.out.println("✅ Connecté à la base de données");
        System.out.println(connection);
        System.out.println(connection1);

        // Initialize sample data if needed
        DataInitializer.initializeSampleData();

        // Launch JavaFX
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/main.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 700);
        primaryStage.setTitle("PHARMAX - E-Pharmacy");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
