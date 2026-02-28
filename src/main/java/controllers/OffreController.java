package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Candidat;
import models.Offre;
import models.TypeOffre;
import services.CandidatService;
import services.OffreService;
import services.RecruitmentWorkflowService;
import utils.AuthContext;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
    private final CandidatService candidatService = new CandidatService();
    private final ObservableList<Offre> masterList = FXCollections.observableArrayList();
    private final RecruitmentWorkflowService workflowService = new RecruitmentWorkflowService();
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

        btnAjouter.setOnAction(e -> ouvrirFormulaireOffre(null));
        btnModifier.setOnAction(e -> ouvrirFormulaireOffre(tableOffre.getSelectionModel().getSelectedItem()));
        btnSupprimer.setOnAction(e -> supprimerOffre());
        btnFiltre.setOnAction(e -> ouvrirFiltreSalaire());
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
                    ouvrirFormulairePostuler(offre);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void ouvrirFormulairePostuler(Offre offre) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Postuler à: " + offre.getNomOffre());
        ButtonType applyType = new ButtonType("Envoyer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyType, ButtonType.CANCEL);

        TextField nom = new TextField();
        TextField prenom = new TextField();
        TextField cin = new TextField();
        TextField tel = new TextField();
        TextField adresse = new TextField();
        TextField email = new TextField();
        TextField cv = new TextField();

        GridPane g = new GridPane();
        g.getStyleClass().add("form-grid");
        g.setHgap(8); g.setVgap(8);
        g.addRow(0, new Label("Nom"), nom);
        g.addRow(1, new Label("Prénom"), prenom);
        g.addRow(2, new Label("CIN"), cin);
        g.addRow(3, new Label("Téléphone"), tel);
        g.addRow(4, new Label("Adresse"), adresse);
        g.addRow(5, new Label("Email"), email);
        g.addRow(6, new Label("CV"), cv);

        dialog.getDialogPane().setContent(g);
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == applyType) {
            try {
                Candidat c = new Candidat(nom.getText(), prenom.getText(), Integer.parseInt(cin.getText()), Integer.parseInt(tel.getText()), adresse.getText(), email.getText(), cv.getText());
                candidatService.ajouter(c);
                if (c.getIdCandidat() > 0) {
                    workflowService.updateCandidatePhase(c.getIdCandidat(), RecruitmentWorkflowService.STATUS_NOUVEAU);
                }
                showAlert(Alert.AlertType.INFORMATION, "Postulation", "Votre candidature a été envoyée pour l'offre: " + offre.getNomOffre());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
            }
        }
    }

    private void ouvrirFormulaireOffre(Offre selected) {
        if (selected == null && !AuthContext.isAdmin()) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(selected == null ? "Ajouter Offre" : "Modifier Offre");
        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField nom = new TextField();
        ComboBox<TypeOffre> type = new ComboBox<>(FXCollections.observableArrayList(TypeOffre.values()));
        TextField comp = new TextField();
        TextField salaire = new TextField();

        if (selected != null) {
            nom.setText(selected.getNomOffre());
            type.setValue(selected.getType());
            comp.setText(selected.getCompetences());
            salaire.setText(String.valueOf(selected.getSalaire()));
        }

        GridPane g = new GridPane();
        g.getStyleClass().add("form-grid");
        g.setHgap(8); g.setVgap(8);
        g.addRow(0, new Label("Nom"), nom);
        g.addRow(1, new Label("Type"), type);
        g.addRow(2, new Label("Compétences"), comp);
        g.addRow(3, new Label("Salaire"), salaire);
        dialog.getDialogPane().setContent(g);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveType) {
            try {
                if (selected == null) selected = new Offre();
                selected.setNomOffre(nom.getText());
                selected.setType(type.getValue());
                selected.setCompetences(comp.getText());
                selected.setSalaire(Integer.parseInt(salaire.getText()));

                if (selected.getIdOffre() == 0) service.ajouter(selected); else service.modifier(selected);
                loadTable();
                updatePagination();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
            }
        }
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

    private void ouvrirFiltreSalaire() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Filtre salaire min");
        d.setHeaderText(null);
        d.setContentText("Salaire minimum:");
        d.showAndWait().ifPresent(v -> {
            try {
                int min = Integer.parseInt(v);
                filteredList.setPredicate(o -> o.getSalaire() >= min);
                updatePagination();
            } catch (NumberFormatException ignored) {}
        });
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
        if ("Décroissant".equals(ordre)) sorted.setComparator(sorted.getComparator().reversed());
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
