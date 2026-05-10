package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import models.Utilisateur;
import services.UtilisateurService;

import java.sql.SQLException;

public class UtilisateursController {
    @FXML private TextField txtSearch;
    @FXML private TableView<Utilisateur> tableUsers;
    @FXML private TableColumn<Utilisateur, Integer> colId, colCin, colTel;
    @FXML private TableColumn<Utilisateur, String> colNom, colPrenom, colMail, colRole, colPhoto;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer;

    private final UtilisateurService service = new UtilisateurService();
    private final ObservableList<Utilisateur> master = FXCollections.observableArrayList();
    private FilteredList<Utilisateur> filtered;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colCin.setCellValueFactory(new PropertyValueFactory<>("cin"));
        colMail.setCellValueFactory(new PropertyValueFactory<>("mail"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("tel"));
        colPhoto.setCellValueFactory(new PropertyValueFactory<>("photoProfil"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        filtered = new FilteredList<>(master, u -> true);
        tableUsers.setItems(filtered);
        txtSearch.textProperty().addListener((obs, old, value) -> filter(value));
        btnAjouter.setOnAction(e -> showDialog(null));
        btnModifier.setOnAction(e -> {
            Utilisateur selected = tableUsers.getSelectionModel().getSelectedItem();
            if (selected != null) showDialog(selected);
        });
        btnSupprimer.setOnAction(e -> deleteSelected());
        refresh();
    }

    private void refresh() {
        try {
            master.setAll(service.recuperer());
        } catch (SQLException e) {
            alert("Utilisateurs", e.getMessage());
        }
    }

    private void filter(String query) {
        String q = query == null ? "" : query.toLowerCase().trim();
        filtered.setPredicate(u -> q.isEmpty()
                || safe(u.getNom()).contains(q)
                || safe(u.getPrenom()).contains(q)
                || safe(u.getMail()).contains(q)
                || safe(u.getRole()).contains(q)
                || String.valueOf(u.getCin()).contains(q));
    }

    private void showDialog(Utilisateur editing) {
        Dialog<Utilisateur> dialog = new Dialog<>();
        dialog.setTitle(editing == null ? "Nouvel utilisateur" : "Modifier utilisateur");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        TextField nom = new TextField(editing == null ? "" : editing.getNom());
        TextField prenom = new TextField(editing == null ? "" : editing.getPrenom());
        TextField cin = new TextField(editing == null ? "0" : String.valueOf(editing.getCin()));
        TextField mail = new TextField(editing == null ? "" : editing.getMail());
        TextField tel = new TextField(editing == null ? "0" : String.valueOf(editing.getTel()));
        TextField photo = new TextField(editing == null ? "0" : editing.getPhotoProfil());
        ComboBox<String> role = new ComboBox<>(FXCollections.observableArrayList("ADMIN", "EMPLOYE", "CANDIDAT"));
        role.setValue(editing == null ? "EMPLOYE" : editing.getRole());
        PasswordField password = new PasswordField();
        password.setText(editing == null ? "1234" : editing.getPassword());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.getStyleClass().add("form-grid");
        grid.addRow(0, new Label("Nom"), nom);
        grid.addRow(1, new Label("Prénom"), prenom);
        grid.addRow(2, new Label("CIN"), cin);
        grid.addRow(3, new Label("Email"), mail);
        grid.addRow(4, new Label("Téléphone"), tel);
        grid.addRow(5, new Label("Photo"), photo);
        grid.addRow(6, new Label("Rôle"), role);
        grid.addRow(7, new Label("Mot de passe"), password);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) return null;
            Utilisateur u = editing == null ? new Utilisateur() : editing;
            u.setNom(nom.getText().trim());
            u.setPrenom(prenom.getText().trim());
            u.setCin(parseInt(cin.getText()));
            u.setMail(mail.getText().trim());
            u.setTel(parseInt(tel.getText()));
            u.setPhotoProfil(photo.getText().trim());
            u.setRole(role.getValue());
            u.setPassword(password.getText());
            return u;
        });

        dialog.showAndWait().ifPresent(u -> {
            try {
                if (u.getId() == 0) service.ajouter(u); else service.modifier(u);
                refresh();
            } catch (SQLException e) {
                alert("Enregistrement", e.getMessage());
            }
        });
    }

    private void deleteSelected() {
        Utilisateur selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cet utilisateur ?", ButtonType.CANCEL, ButtonType.OK);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            service.supprimer(selected.getId());
            refresh();
        } catch (SQLException e) {
            alert("Suppression", e.getMessage());
        }
    }

    private int parseInt(String value) {
        return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private void alert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
