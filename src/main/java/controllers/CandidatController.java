package controllers;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import models.Candidat;
import services.CandidatService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CandidatController {

    @FXML private TextField txtNom, txtPrenom, txtCIN, txtTel, txtAdresse, txtEmail, txtCv, txtRecherche;
    @FXML private ComboBox<String> comboTriChamp;
    @FXML private ComboBox<String> comboTriOrdre;

    @FXML private TableView<Candidat> tableCandidat;
    @FXML private TableColumn<Candidat, String> colNom, colPrenom, colAdresse, colEmail, colCv;
    @FXML private TableColumn<Candidat, Integer> colCIN, colTel;

    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnFiltre, btnResetFiltre;
    @FXML private Label lblCount;
    @FXML private Pagination pagination;

    private final CandidatService service = new CandidatService();
    private final ObservableList<Candidat> masterList = FXCollections.observableArrayList();
    private FilteredList<Candidat> filteredList;

    private static final int ROWS_PER_PAGE = 8;

    @FXML
    public void initialize() {

        // Options tri
        comboTriChamp.getItems().addAll("Nom", "Prénom", "CIN", "Téléphone", "Adresse", "Email", "CV");
        comboTriOrdre.getItems().addAll("Croissant", "Décroissant");
        comboTriOrdre.setValue("Croissant");

        // Configuration colonnes
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colCIN.setCellValueFactory(new PropertyValueFactory<>("CIN"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("Tel"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCv.setCellValueFactory(new PropertyValueFactory<>("cv"));

        loadTable();

        filteredList = new FilteredList<>(masterList, b -> true);

        // RECHERCHE
        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(c -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return c.getNom().toLowerCase().contains(lower)
                        || c.getPrenom().toLowerCase().contains(lower)
                        || String.valueOf(c.getCIN()).contains(lower)
                        || String.valueOf(c.getTel()).contains(lower)
                        || c.getAdresse().toLowerCase().contains(lower)
                        || c.getEmail().toLowerCase().contains(lower)
                        || c.getCv().toLowerCase().contains(lower);
            });
            updatePagination();
        });

        filteredList.addListener((javafx.collections.ListChangeListener<Candidat>) c -> {
            lblCount.setText(String.valueOf(filteredList.size()));
            updatePagination();
        });

        // TRI
        comboTriChamp.setOnAction(e -> appliquerTri());
        comboTriOrdre.setOnAction(e -> appliquerTri());

        // Remplissage auto
        tableCandidat.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                txtNom.setText(selected.getNom());
                txtPrenom.setText(selected.getPrenom());
                txtCIN.setText(String.valueOf(selected.getCIN()));
                txtTel.setText(String.valueOf(selected.getTel()));
                txtAdresse.setText(selected.getAdresse());
                txtEmail.setText(selected.getEmail());
                txtCv.setText(selected.getCv());
            }
        });

        // Buttons
        btnAjouter.setOnAction(e -> ajouterCandidat());
        btnModifier.setOnAction(e -> modifierCandidat());
        btnSupprimer.setOnAction(e -> supprimerCandidat());
        btnFiltre.setOnAction(e -> ouvrirFiltreAvance());
        btnResetFiltre.setOnAction(e -> {
            filteredList.setPredicate(b -> true);
            updatePagination();
        });

        updatePagination();
    }

    private void appliquerTri() {
        String champ = comboTriChamp.getValue();
        String ordre = comboTriOrdre.getValue();
        if (champ == null) return;

        SortedList<Candidat> sorted = new SortedList<>(filteredList);

        switch (champ) {
            case "Nom" -> sorted.setComparator((c1, c2) -> c1.getNom().compareToIgnoreCase(c2.getNom()));
            case "Prénom" -> sorted.setComparator((c1, c2) -> c1.getPrenom().compareToIgnoreCase(c2.getPrenom()));
            case "CIN" -> sorted.setComparator((c1, c2) -> Integer.compare(c1.getCIN(), c2.getCIN()));
            case "Téléphone" -> sorted.setComparator((c1, c2) -> Integer.compare(c1.getTel(), c2.getTel()));
            case "Adresse" -> sorted.setComparator((c1, c2) -> c1.getAdresse().compareToIgnoreCase(c2.getAdresse()));
            case "Email" -> sorted.setComparator((c1, c2) -> c1.getEmail().compareToIgnoreCase(c2.getEmail()));
            case "CV" -> sorted.setComparator((c1, c2) -> c1.getCv().compareToIgnoreCase(c2.getCv()));
        }

        if ("Décroissant".equals(ordre)) sorted.setComparator(sorted.getComparator().reversed());

        tableCandidat.setItems(sorted);
    }

    private void loadTable() {
        try {
            masterList.clear();
            List<Candidat> candidats = service.recuperer();
            masterList.addAll(candidats);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    /* ================= PAGINATION ================= */
    private void updatePagination() {
        if (filteredList == null) return;

        int total = filteredList.size();
        int pageCount = (int) Math.ceil((double) total / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
        pagination.setPageFactory(this::createPage);
    }

    private TableView<Candidat> createPage(int pageIndex) {
        int from = pageIndex * ROWS_PER_PAGE;
        int to = Math.min(from + ROWS_PER_PAGE, filteredList.size());

        SortedList<Candidat> sorted = new SortedList<>(
                FXCollections.observableArrayList(filteredList.subList(from, to))
        );
        sorted.comparatorProperty().bind(tableCandidat.comparatorProperty());
        tableCandidat.setItems(sorted);
        return tableCandidat;
    }

    /* ================= CRUD ================= */
    private void ajouterCandidat() {
        if (!validerChamps()) return;
        try {
            Candidat c = new Candidat(
                    txtNom.getText(),
                    txtPrenom.getText(),
                    Integer.parseInt(txtCIN.getText()),
                    Integer.parseInt(txtTel.getText()),
                    txtAdresse.getText(),
                    txtEmail.getText(),
                    txtCv.getText()
            );
            service.ajouter(c);
            loadTable();
            resetForm();
            updatePagination();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "CIN et Téléphone doivent être des nombres !");
        }
    }

    private void modifierCandidat() {
        Candidat c = tableCandidat.getSelectionModel().getSelectedItem();
        if (c == null) return;
        try {
            c.setNom(txtNom.getText());
            c.setPrenom(txtPrenom.getText());
            c.setCIN(Integer.parseInt(txtCIN.getText()));
            c.setTel(Integer.parseInt(txtTel.getText()));
            c.setAdresse(txtAdresse.getText());
            c.setEmail(txtEmail.getText());
            c.setCv(txtCv.getText());

            service.modifier(c);
            loadTable();
            resetForm();
            updatePagination();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "CIN et Téléphone doivent être des nombres !");
        }
    }

    private void supprimerCandidat() {
        Candidat c = tableCandidat.getSelectionModel().getSelectedItem();
        if (c == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer ce candidat ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(c.getIdCandidat());
                loadTable();
                resetForm();
                updatePagination();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }


    private boolean validerChamps() {
        return !(txtNom.getText().isEmpty()
                || txtPrenom.getText().isEmpty()
                || txtCIN.getText().isEmpty()
                || txtTel.getText().isEmpty()
                || txtAdresse.getText().isEmpty()
                || txtEmail.getText().isEmpty()
                || txtCv.getText().isEmpty());
    }

    private void resetForm() {
        txtNom.clear();
        txtPrenom.clear();
        txtCIN.clear();
        txtTel.clear();
        txtAdresse.clear();
        txtEmail.clear();
        txtCv.clear();
        tableCandidat.getSelectionModel().clearSelection();
    }

    /* ================= FILTRE AVANCÉ ================= */
    private void ouvrirFiltreAvance() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Filtre Avancé");

        ButtonType appliquer = new ButtonType("Appliquer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(appliquer, ButtonType.CANCEL);

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");
        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Prénom:"), 0, 1);
        grid.add(prenomField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().setOpacity(0);
        dialog.setOnShown(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(300), dialog.getDialogPane());
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        });

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == appliquer) {
            filteredList.setPredicate(c -> {
                if (!nomField.getText().isEmpty() &&
                        !c.getNom().toLowerCase().contains(nomField.getText().toLowerCase()))
                    return false;
                if (!prenomField.getText().isEmpty() &&
                        !c.getPrenom().toLowerCase().contains(prenomField.getText().toLowerCase()))
                    return false;
                return true;
            });
            updatePagination();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
