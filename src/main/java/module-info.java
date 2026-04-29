module com.pharmax {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.desktop;
    requires java.logging;
    requires java.sql;
    requires org.jsoup;
    requires okhttp3;
    requires com.google.gson;
    requires itextpdf;

    opens com.pharmax to javafx.controls, javafx.fxml;
    opens com.pharmax.ui to javafx.fxml;
    opens com.pharmax.controller to javafx.fxml;
    opens com.pharmax.model to javafx.base, com.google.gson;
    opens com.pharmax.service to javafx.fxml;

    exports com.pharmax;
    exports com.pharmax.ui;
    exports com.pharmax.controller;
    exports com.pharmax.model;
    exports com.pharmax.service;
}
