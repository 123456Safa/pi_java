package controllers;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class Home extends Application {

    @Override
    public void start(Stage stage) {

        System.setProperty("prism.order", "sw");

        WebView web = new WebView();
        web.getEngine().load("package org.example;\n" +
                "\n" +
                "import javafx.application.Application;\n" +
                "import javafx.scene.Scene;\n" +
                "import javafx.scene.web.WebView;\n" +
                "import javafx.stage.Stage;\n" +
                "\n" +
                "public class Main extends Application {\n" +
                "\n" +
                "    @Override\n" +
                "    public void start(Stage stage) {\n" +
                "        System.setProperty(\"prism.order\", \"sw\");\n" +
                "        WebView web = new WebView();\n" +
                "\n" +
                "        web.getEngine().getLoadWorker().stateProperty().addListener((obs, old, newState) -> {\n" +
                "            System.out.println(\"STATE: \" + newState);\n" +
                "        });\n" +
                "\n" +
                "        web.getEngine().load(\"https://demos.themeselection.com/sneat-bootstrap-html-admin-template-free/html/tables-basic.html\");\n" +
                "\n" +
                "        Scene scene = new Scene(web, 1000, 700);\n" +
                "        stage.setScene(scene);\n" +
                "        stage.show();\n" +
                "    } // \uD83D\uDD25 هذا ناقص عندك\n" +
                "\n" +
                "    public static void main(String[] args) {\n" +
                "        launch();\n" +
                "    }\n" +
                "}");

        Scene scene = new Scene(web, 1000, 700);

        stage.setTitle("Test WebView");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}