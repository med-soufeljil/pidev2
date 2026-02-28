package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Offre;
import services.OffreService;
import utils.AuthContext;
import utils.NavigationState;

import java.sql.SQLException;

public class OffreController {

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboTri;
    @FXML private ComboBox<String> comboOrdre;
    @FXML private TableView<Offre> tableOffre;
    @FXML private TableColumn<Offre, String> colNom, colType, colCompetences;
    @FXML private TableColumn<Offre, Integer> colSalaire;
    @FXML private TableColumn<Offre, Void> colAction;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnFiltre, btnResetFiltre;
    @FXML private Label lblCount;
    @FXML private Pagination pagination;

    private final OffreService service = new OffreService();
    private final ObservableList<Offre> masterList = FXCollections.observableArrayList();
    private FilteredList<Offre> filteredList;
    private static final int ROWS_PER_PAGE = 8;

    @FXML
    public void initialize() {
        comboTri.getItems().addAll("Nom", "Type", "Compétences", "Salaire");
        comboOrdre.getItems().addAll("Croissant", "Décroissant");
        comboOrdre.setValue("Croissant");

        colNom.setCellValueFactory(new PropertyValueFactory<>("nomOffre"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colCompetences.setCellValueFactory(new PropertyValueFactory<>("competences"));
        colSalaire.setCellValueFactory(new PropertyValueFactory<>("salaire"));

        loadTable();
        filteredList = new FilteredList<>(masterList, b -> true);

        txtRecherche.textProperty().addListener((obs, o, q) -> {
            filteredList.setPredicate(offre -> {
                if (q == null || q.isBlank()) return true;
                String lower = q.toLowerCase();
                return offre.getNomOffre().toLowerCase().contains(lower)
                        || offre.getType().name().toLowerCase().contains(lower)
                        || offre.getCompetences().toLowerCase().contains(lower)
                        || String.valueOf(offre.getSalaire()).contains(lower);
            });
            updatePagination();
        });

        comboTri.setOnAction(e -> appliquerTri());
        comboOrdre.setOnAction(e -> appliquerTri());

        btnAjouter.setOnAction(e -> {
            NavigationState.clearAll();
            MainController.navigate("OffreForm.fxml");
        });
        btnModifier.setOnAction(e -> {
            Offre selected = tableOffre.getSelectionModel().getSelectedItem();
            if (selected == null || !AuthContext.isAdmin()) return;
            NavigationState.clearAll();
            NavigationState.selectedOffre = selected;
            MainController.navigate("OffreForm.fxml");
        });
        btnSupprimer.setOnAction(e -> supprimerOffre());
        btnFiltre.setOnAction(e -> applySalaryFilterFromSearch());
        btnResetFiltre.setOnAction(e -> {filteredList.setPredicate(o -> true); updatePagination();});

        configureActionColumn();
        applyPermissions();
        updatePagination();
    }

    private void applyPermissions() {
        boolean admin = AuthContext.isAdmin();
        btnAjouter.setDisable(!admin);
        btnModifier.setDisable(!admin);
        btnSupprimer.setDisable(!admin);
        btnFiltre.setDisable(!admin);
        btnResetFiltre.setDisable(!admin);
    }

    private void configureActionColumn() {
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Postuler");
            {
                btn.getStyleClass().add("apply-btn");
                btn.setOnAction(e -> {
                    Offre offre = getTableView().getItems().get(getIndex());
                    NavigationState.clearAll();
                    NavigationState.selectedOffre = offre;
                    MainController.navigate("ApplicationForm.fxml");
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void supprimerOffre() {
        Offre selected = tableOffre.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            service.supprimer(selected.getIdOffre());
            loadTable();
            updatePagination();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void applySalaryFilterFromSearch() {
        try {
            int min = Integer.parseInt(txtRecherche.getText().trim());
            filteredList.setPredicate(o -> o.getSalaire() >= min);
            updatePagination();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.INFORMATION, "Filtre salaire", "Saisissez un montant dans la recherche puis cliquez Filtre avancé.");
        }
    }

    private void appliquerTri() {
        String champ = comboTri.getValue();
        String ordre = comboOrdre.getValue();
        if (champ == null) return;
        SortedList<Offre> sorted = new SortedList<>(filteredList);
        switch (champ) {
            case "Nom" -> sorted.setComparator((o1, o2) -> o1.getNomOffre().compareToIgnoreCase(o2.getNomOffre()));
            case "Type" -> sorted.setComparator((o1, o2) -> o1.getType().name().compareToIgnoreCase(o2.getType().name()));
            case "Compétences" -> sorted.setComparator((o1, o2) -> o1.getCompetences().compareToIgnoreCase(o2.getCompetences()));
            case "Salaire" -> sorted.setComparator((o1, o2) -> Integer.compare(o1.getSalaire(), o2.getSalaire()));
        }
        if ("Décroissant".equals(ordre) && sorted.getComparator() != null) sorted.setComparator(sorted.getComparator().reversed());
        tableOffre.setItems(sorted);
    }

    private void loadTable() {
        try {
            masterList.setAll(service.recuperer());
            lblCount.setText(String.valueOf(masterList.size()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void updatePagination() {
        int total = filteredList.size();
        int pageCount = (int) Math.ceil((double) total / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
        pagination.setPageFactory(this::createPage);
    }

    private TableView<Offre> createPage(int pageIndex) {
        int from = pageIndex * ROWS_PER_PAGE;
        int to = Math.min(from + ROWS_PER_PAGE, filteredList.size());
        tableOffre.setItems(FXCollections.observableArrayList(filteredList.subList(from, to)));
        return tableOffre;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
