module pidevjava {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.java;

    opens controllers to javafx.fxml;
    exports controllers.frontoffice to javafx.fxml;
    opens controllers.frontoffice to javafx.fxml;
    opens models to javafx.fxml;
    opens org.example to javafx.graphics;
}
