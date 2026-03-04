package org.soa.tp1.pi_dev_s2.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.soa.tp1.pi_dev_s2.model.Utilisateur;
import org.soa.tp1.pi_dev_s2.service.UtilisateurService;

import java.sql.SQLException;
import java.util.List;

public class AdminController {

    @FXML private TableView<Utilisateur>            tableUsers;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String>  colNom;
    @FXML private TableColumn<Utilisateur, String>  colPrenom;
    @FXML private TableColumn<Utilisateur, String>  colEmail;
    @FXML private TableColumn<Utilisateur, String>  colRole;
    @FXML private TextField   txtSearch;
    @FXML private Pagination  pagination;
    @FXML private Label       lblCount;

    private static final int PAGE_SIZE = 10;
    private ObservableList<Utilisateur> allUsers    = FXCollections.observableArrayList();
    private ObservableList<Utilisateur> filtered    = FXCollections.observableArrayList();
    private final UtilisateurService    service     = new UtilisateurService();
    private Utilisateur currentUser;

    public void setCurrentUser(Utilisateur u) { this.currentUser = u; }

    @FXML
    public void initialize() {
        setupColumns();
        loadUsers();

        // Recherche en temps réel
        txtSearch.textProperty().addListener((obs, old, val) -> handleSearch());
    }

    private void setupColumns() {
        colId.setCellValueFactory(d ->
                new javafx.beans.property.SimpleIntegerProperty(d.getValue().getId()).asObject());
        colNom.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getNom()));
        colPrenom.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getPrenom()));
        colEmail.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getEmail()));
        colRole.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getRole()));

        // Couleur par rôle
        tableUsers.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Utilisateur u, boolean empty) {
                super.updateItem(u, empty);
                if (u == null || empty) { setStyle(""); return; }
                switch (u.getRole().toLowerCase()) {
                    case "admin"    -> setStyle("-fx-background-color:#EDE7F6;");
                    case "rh"       -> setStyle("-fx-background-color:#E0F7FA;");
                    case "employe"  -> setStyle("-fx-background-color:#E8F5E9;");
                    case "candidat" -> setStyle("-fx-background-color:#FFF3E0;");
                    default         -> setStyle("");
                }
            }
        });
    }

    private void loadUsers() {
        try {
            List<Utilisateur> list = service.getAllUsers();
            allUsers  = FXCollections.observableArrayList(list);
            filtered  = FXCollections.observableArrayList(list);
            setupPagination(filtered);
            lblCount.setText("Total : " + list.size() + " utilisateurs");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── PAGINATION ────────────────────────────
    private void setupPagination(ObservableList<Utilisateur> data) {
        int pageCount = (int) Math.ceil((double) data.size() / PAGE_SIZE);
        pagination.setPageCount(Math.max(1, pageCount));
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(idx -> {
            updatePage(data, idx);
            return new javafx.scene.layout.Region();
        });
        updatePage(data, 0);
    }

    private void updatePage(ObservableList<Utilisateur> data, int idx) {
        int from = idx * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, data.size());
        if (from >= data.size()) from = 0;
        tableUsers.setItems(FXCollections.observableArrayList(
                data.subList(Math.min(from, data.size()), to)));
    }

    @FXML
    private void handleSearch() {
        String kw = txtSearch.getText().toLowerCase().trim();
        filtered = kw.isEmpty() ? allUsers : allUsers.filtered(u ->
                u.getNom().toLowerCase().contains(kw)    ||
                        u.getPrenom().toLowerCase().contains(kw) ||
                        u.getEmail().toLowerCase().contains(kw)  ||
                        u.getRole().toLowerCase().contains(kw));
        lblCount.setText("Résultats : " + filtered.size() + " utilisateurs");
        setupPagination(filtered);
    }

    @FXML private void handleAdd()     { openEditDialog(null); }

    @FXML
    private void handleEdit() {
        Utilisateur sel = tableUsers.getSelectionModel().getSelectedItem();
        if (sel != null) openEditDialog(sel);
        else alert("Sélectionnez un utilisateur à modifier.");
    }

    @FXML
    private void handleDelete() {
        Utilisateur sel = tableUsers.getSelectionModel().getSelectedItem();
        if (sel == null) { alert("Aucun utilisateur sélectionné."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer " + sel.getNom() + " " + sel.getPrenom() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                try { service.deleteUser(sel.getId()); loadUsers(); }
                catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    @FXML private void handleRefresh() { txtSearch.clear(); loadUsers(); }

    @FXML
    private void handleExportCSV() {
        StringBuilder csv = new StringBuilder("ID,Nom,Prénom,Email,Rôle\n");
        for (Utilisateur u : allUsers) {
            csv.append(u.getId()).append(",")
                    .append(u.getNom()).append(",")
                    .append(u.getPrenom()).append(",")
                    .append(u.getEmail()).append(",")
                    .append(u.getRole()).append("\n");
        }
        // Afficher dans une alerte (en vrai tu sauvegarderais dans un fichier)
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Export CSV");
        a.setHeaderText("Données exportées (" + allUsers.size() + " utilisateurs)");
        TextArea ta = new TextArea(csv.toString());
        ta.setEditable(false);
        ta.setPrefRowCount(15);
        a.getDialogPane().setExpandableContent(ta);
        a.showAndWait();
    }

    private void openEditDialog(Utilisateur user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/editUser.fxml"));
            Parent root = loader.load();
            EditUserController ctrl = loader.getController();
            ctrl.setUtilisateur(user);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(user == null ? "Ajouter utilisateur" : "Modifier utilisateur");
            stage.setOnHiding(e -> loadUsers());
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}