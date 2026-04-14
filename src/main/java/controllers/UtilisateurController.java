package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import models.Utilisateur;
import services.UtilisateurService;

public class UtilisateurController {

    @FXML
    private TextField tfNom;

    @FXML
    private TextField tfEmail;

    @FXML
    private TextField tfMotDePasse;

    @FXML
    private TextField tfRole;

    @FXML
    private TableView<Utilisateur> tableUtilisateur;

    private final UtilisateurService service = new UtilisateurService();

    private Utilisateur selectedUtilisateur;

    @FXML
    public void initialize() {
        loadUtilisateurs();
    }

    @FXML
    public void ajouterUtilisateur() {
        try {
            Utilisateur u;
            if (selectedUtilisateur != null) {
                u = selectedUtilisateur;
                u.setNom(tfNom.getText());
                u.setEmail(tfEmail.getText());
                u.setMotDePasse(tfMotDePasse.getText());
                u.setRole(tfRole.getText());

                service.update(u);
                System.out.println("✏️ Utilisateur modifié");
            } else {
                u = new Utilisateur();

                u.setNom(tfNom.getText());
                u.setEmail(tfEmail.getText());
                u.setMotDePasse(tfMotDePasse.getText());
                u.setRole(tfRole.getText());

                service.add(u);
                System.out.println("✅ Utilisateur ajouté");
            }

            loadUtilisateurs();
            clearFields();
            selectedUtilisateur = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadUtilisateurs() {
        try {
            ObservableList<Utilisateur> data =
                    FXCollections.observableArrayList(service.select());

            tableUtilisateur.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        tfNom.clear();
        tfEmail.clear();
        tfMotDePasse.clear();
        tfRole.clear();
    }

    @FXML
    public void modifierUtilisateur() {
        selectedUtilisateur = tableUtilisateur.getSelectionModel().getSelectedItem();
        if (selectedUtilisateur != null) {
            tfNom.setText(selectedUtilisateur.getNom());
            tfEmail.setText(selectedUtilisateur.getEmail());
            tfMotDePasse.setText(selectedUtilisateur.getMotDePasse());
            tfRole.setText(selectedUtilisateur.getRole());
        }
    }

    @FXML
    public void supprimerUtilisateur() {
        selectedUtilisateur = tableUtilisateur.getSelectionModel().getSelectedItem();
        if (selectedUtilisateur != null) {
            try {
                service.delete(selectedUtilisateur.getId());
                loadUtilisateurs();
                clearFields();
                selectedUtilisateur = null;
                System.out.println("🗑️ Utilisateur supprimé");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
