package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class FormController {

    @FXML
    private TextField titre;

    @FXML
    private TextField description;

    @FXML
    public void initialize() {
        System.out.println("FXML Loaded ✅");
    }
}