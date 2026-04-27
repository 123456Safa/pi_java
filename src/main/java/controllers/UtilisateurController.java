package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
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

    @FXML
    private TableColumn<Utilisateur, Integer> colId;
    @FXML
    private TableColumn<Utilisateur, String> colNom;
    @FXML
    private TableColumn<Utilisateur, String> colEmail;
    @FXML
    private TableColumn<Utilisateur, String> colRole;

    @FXML
    private Pagination pagination;

    private static final int ITEMS_PER_PAGE = 8;
    private ObservableList<Utilisateur> allUtilisateurs = FXCollections.observableArrayList();

    private final UtilisateurService service = new UtilisateurService();

    private Utilisateur selectedUtilisateur;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        pagination.currentPageIndexProperty().addListener((obs, oldIdx, newIdx) -> updateTablePage(newIdx.intValue()));
        
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
            allUtilisateurs = FXCollections.observableArrayList(service.select());
            updatePagination();
        } catch (Exception e) {
            allUtilisateurs = FXCollections.observableArrayList();
            updatePagination();
            e.printStackTrace();
        }
    }

    private void updatePagination() {
        int totalItems = allUtilisateurs.size();
        int pageCount = (totalItems + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        pagination.setPageCount(Math.max(1, pageCount));
        pagination.setCurrentPageIndex(0);
        updateTablePage(0);
    }

    private void updateTablePage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allUtilisateurs.size());
        
        if (fromIndex >= allUtilisateurs.size()) {
            tableUtilisateur.setItems(FXCollections.observableArrayList());
            return;
        }
        
        tableUtilisateur.setItems(FXCollections.observableArrayList(allUtilisateurs.subList(fromIndex, toIndex)));
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
