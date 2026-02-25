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
import models.Offre;
import models.TypeOffre;
import services.OffreService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class OffreController {

    @FXML private TextField txtNomOffre, txtCompetences, txtSalaire, txtRecherche;
    @FXML private ComboBox<TypeOffre> comboType;
    @FXML private ComboBox<String> comboTri; // ✅ AJOUT TRI
    @FXML private ComboBox<String> comboOrdre;

    @FXML private TableView<Offre> tableOffre;
    @FXML private TableColumn<Offre, String> colNom, colType, colCompetences;
    @FXML private TableColumn<Offre, Integer> colSalaire;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnReset;
    @FXML private Button btnFiltre, btnResetFiltre;
    @FXML private Label lblCount;
    @FXML private Pagination pagination;

    private final OffreService service = new OffreService();
    private final ObservableList<Offre> masterList = FXCollections.observableArrayList();
    private FilteredList<Offre> filteredList;

    private static final int ROWS_PER_PAGE = 8;

    @FXML
    public void initialize() {

        comboType.getItems().setAll(TypeOffre.values());

        // ✅ TRI OPTIONS
        comboTri.getItems().addAll("Nom", "Type", "Compétences", "Salaire");
        comboOrdre.getItems().addAll("Croissant", "Décroissant");
        comboOrdre.setValue("Croissant"); // valeur par défaut


        colNom.setCellValueFactory(new PropertyValueFactory<>("nomOffre"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colCompetences.setCellValueFactory(new PropertyValueFactory<>("competences"));
        colSalaire.setCellValueFactory(new PropertyValueFactory<>("salaire"));

        loadTable();

        filteredList = new FilteredList<>(masterList, b -> true);

        // ================= RECHERCHE =================
        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(offre -> {
                if (newVal == null || newVal.isEmpty()) return true;

                String lower = newVal.toLowerCase();

                return offre.getNomOffre().toLowerCase().contains(lower)
                        || offre.getType().name().toLowerCase().contains(lower)
                        || offre.getCompetences().toLowerCase().contains(lower)
                        || String.valueOf(offre.getSalaire()).contains(lower);
            });
            updatePagination();
        });

        filteredList.addListener((javafx.collections.ListChangeListener<Offre>) c -> {
            lblCount.setText(String.valueOf(filteredList.size()));
            updatePagination();
        });

        // ================= TRI PAR COMBO =================
        comboTri.setOnAction(e -> appliquerTri());
        comboOrdre.setOnAction(e -> appliquerTri());


        // ================= REMPLISSAGE AUTO =================
        tableOffre.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                txtNomOffre.setText(selected.getNomOffre());
                comboType.setValue(selected.getType());
                txtCompetences.setText(selected.getCompetences());
                txtSalaire.setText(String.valueOf(selected.getSalaire()));
            }
        });

        btnAjouter.setOnAction(e -> ajouterOffre());
        btnModifier.setOnAction(e -> modifierOffre());
        btnSupprimer.setOnAction(e -> supprimerOffre());
        btnReset.setOnAction(e -> resetForm());
        btnFiltre.setOnAction(e -> ouvrirFiltreAvance());
        btnResetFiltre.setOnAction(e -> {
            filteredList.setPredicate(b -> true);
            updatePagination();
        });

        updatePagination();
    }

    private void appliquerTri() {

        String champ = comboTri.getValue();
        String ordre = comboOrdre.getValue();

        if (champ == null) return;

        SortedList<Offre> sorted = new SortedList<>(filteredList);

        switch (champ) {

            case "Nom":
                sorted.setComparator((o1, o2) ->
                        o1.getNomOffre().compareToIgnoreCase(o2.getNomOffre()));
                break;

            case "Type":
                sorted.setComparator((o1, o2) ->
                        o1.getType().name().compareToIgnoreCase(o2.getType().name()));
                break;

            case "Compétences":
                sorted.setComparator((o1, o2) ->
                        o1.getCompetences().compareToIgnoreCase(o2.getCompetences()));
                break;

            case "Salaire":
                sorted.setComparator((o1, o2) ->
                        Integer.compare(o1.getSalaire(), o2.getSalaire()));
                break;
        }

        // 🔥 Gestion Croissant / Décroissant
        if ("Décroissant".equals(ordre)) {
            sorted.setComparator(sorted.getComparator().reversed());
        }

        tableOffre.setItems(sorted);
    }


    private void loadTable() {
        try {
            masterList.clear();
            List<Offre> offres = service.recuperer();
            masterList.addAll(offres);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    /* ================= PAGINATION ================= */

    private void updatePagination() {

        int total = filteredList.size();
        int pageCount = (int) Math.ceil((double) total / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);

        pagination.setPageFactory(this::createPage);
    }

    private TableView<Offre> createPage(int pageIndex) {

        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredList.size());

        SortedList<Offre> sorted = new SortedList<>(
                FXCollections.observableArrayList(
                        filteredList.subList(fromIndex, toIndex)
                )
        );

        sorted.comparatorProperty().bind(tableOffre.comparatorProperty());
        tableOffre.setItems(sorted);

        return tableOffre;
    }

    /* ================= CRUD ================= */

    private void ajouterOffre() {
        if (!validerChamps()) return;

        try {
            Offre o = new Offre(
                    txtNomOffre.getText(),
                    comboType.getValue(),
                    txtCompetences.getText(),
                    Integer.parseInt(txtSalaire.getText())
            );

            service.ajouter(o);
            loadTable();
            resetForm();
            updatePagination();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void modifierOffre() {
        Offre o = tableOffre.getSelectionModel().getSelectedItem();
        if (o == null) return;

        try {
            o.setNomOffre(txtNomOffre.getText());
            o.setType(comboType.getValue());
            o.setCompetences(txtCompetences.getText());
            o.setSalaire(Integer.parseInt(txtSalaire.getText()));

            service.modifier(o);
            loadTable();
            resetForm();
            updatePagination();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void supprimerOffre() {
        Offre o = tableOffre.getSelectionModel().getSelectedItem();
        if (o == null) return;

        try {
            service.supprimer(o.getIdOffre());
            loadTable();
            resetForm();
            updatePagination();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private boolean validerChamps() {
        return !(txtNomOffre.getText().isEmpty()
                || comboType.getValue() == null
                || txtCompetences.getText().isEmpty()
                || txtSalaire.getText().isEmpty());
    }

    private void resetForm() {
        txtNomOffre.clear();
        comboType.getSelectionModel().clearSelection();
        txtCompetences.clear();
        txtSalaire.clear();
        tableOffre.getSelectionModel().clearSelection();
    }

    /* ================= FILTRE AVANCÉ ================= */

    private void ouvrirFiltreAvance() {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Filtre Avancé");

        ButtonType appliquer = new ButtonType("Appliquer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(appliquer, ButtonType.CANCEL);

        ListView<TypeOffre> typeList = new ListView<>();
        typeList.getItems().addAll(TypeOffre.values());
        typeList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        typeList.setPrefHeight(80);

        CheckBox cbType = new CheckBox("Filtrer par Type");
        CheckBox cbSalaire = new CheckBox("Filtrer par Salaire");
        CheckBox cbCompetence = new CheckBox("Filtrer par Compétence");

        TextField salaireMin = new TextField();
        salaireMin.setPromptText("Salaire Min");

        TextField salaireMax = new TextField();
        salaireMax.setPromptText("Salaire Max");

        TextField competenceField = new TextField();
        competenceField.setPromptText("Compétence");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(cbType, 0, 0);
        grid.add(typeList, 1, 0);
        grid.add(cbSalaire, 0, 1);
        grid.add(salaireMin, 1, 1);
        grid.add(salaireMax, 2, 1);
        grid.add(cbCompetence, 0, 2);
        grid.add(competenceField, 1, 2);

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

            filteredList.setPredicate(offre -> {

                if (cbType.isSelected()) {
                    if (!typeList.getSelectionModel().getSelectedItems().contains(offre.getType()))
                        return false;
                }

                if (cbSalaire.isSelected()) {
                    if (!salaireMin.getText().isEmpty()) {
                        if (offre.getSalaire() < Integer.parseInt(salaireMin.getText()))
                            return false;
                    }
                    if (!salaireMax.getText().isEmpty()) {
                        if (offre.getSalaire() > Integer.parseInt(salaireMax.getText()))
                            return false;
                    }
                }

                if (cbCompetence.isSelected()) {
                    if (!offre.getCompetences().toLowerCase()
                            .contains(competenceField.getText().toLowerCase()))
                        return false;
                }

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
