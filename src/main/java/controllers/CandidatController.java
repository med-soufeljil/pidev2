package controllers;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import models.Candidat;
import services.CandidatService;
import services.RecruitmentWorkflowService;
import utils.AuthContext;

import java.sql.SQLException;
import java.util.Optional;

public class CandidatController {

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboTriChamp;
    @FXML private ComboBox<String> comboTriOrdre;
    @FXML private TableView<Candidat> tableCandidat;
    @FXML private TableColumn<Candidat, String> colNom, colPrenom, colAdresse, colEmail, colCv;
    @FXML private TableColumn<Candidat, Integer> colCIN, colTel;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnFiltre, btnResetFiltre;
    @FXML private Label lblCount;
    @FXML private Pagination pagination;

    private final CandidatService service = new CandidatService();
    private final RecruitmentWorkflowService workflowService = new RecruitmentWorkflowService();
    private final javafx.collections.ObservableList<Candidat> master = FXCollections.observableArrayList();
    private FilteredList<Candidat> filtered;
    private static final int ROWS = 8;
    private boolean openingProfile;

    @FXML
    public void initialize() {
        comboTriChamp.getItems().addAll("Nom", "Prénom", "CIN", "Téléphone", "Adresse", "Email", "CV");
        comboTriOrdre.getItems().addAll("Croissant", "Décroissant");
        comboTriOrdre.setValue("Croissant");

        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colCIN.setCellValueFactory(new PropertyValueFactory<>("CIN"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("Tel"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCv.setCellValueFactory(new PropertyValueFactory<>("cv"));

        loadTable();
        filtered = new FilteredList<>(master, x -> true);

        txtRecherche.textProperty().addListener((a,b,q)->{applySearch(q); updatePagination();});
        comboTriChamp.setOnAction(e->applySort());
        comboTriOrdre.setOnAction(e->applySort());

        btnAjouter.setOnAction(e->openForm(null));
        btnModifier.setOnAction(e->openForm(tableCandidat.getSelectionModel().getSelectedItem()));
        btnSupprimer.setOnAction(e->deleteSelected());
        btnFiltre.setOnAction(e->openFilterDialog());
        btnResetFiltre.setOnAction(e->{filtered.setPredicate(x->true); updatePagination();});

        tableCandidat.setRowFactory(tv -> {
            TableRow<Candidat> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (!row.isEmpty() && e.getClickCount() == 1 && AuthContext.isAdmin() && !openingProfile) {
                    openProfileDialog(row.getItem());
                }
            });
            return row;
        });

        boolean admin = AuthContext.isAdmin();
        btnAjouter.setDisable(!admin);
        btnModifier.setDisable(!admin);
        btnSupprimer.setDisable(!admin);

        updatePagination();
    }

    private void openForm(Candidat c) {
        if (!AuthContext.isAdmin()) return;
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle(c == null ? "Ajouter candidat" : "Modifier candidat");
        ButtonType save = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        TextField nom = new TextField();
        TextField prenom = new TextField();
        TextField cin = new TextField();
        TextField tel = new TextField();
        TextField adr = new TextField();
        TextField email = new TextField();
        TextField cv = new TextField();

        if (c != null) {
            nom.setText(c.getNom()); prenom.setText(c.getPrenom()); cin.setText(String.valueOf(c.getCIN()));
            tel.setText(String.valueOf(c.getTel())); adr.setText(c.getAdresse()); email.setText(c.getEmail()); cv.setText(c.getCv());
        }

        GridPane g = new GridPane();
        g.getStyleClass().add("form-grid");
        g.setHgap(8); g.setVgap(8);
        g.addRow(0, new Label("Nom"), nom);
        g.addRow(1, new Label("Prénom"), prenom);
        g.addRow(2, new Label("CIN"), cin);
        g.addRow(3, new Label("Téléphone"), tel);
        g.addRow(4, new Label("Adresse"), adr);
        g.addRow(5, new Label("Email"), email);
        g.addRow(6, new Label("CV"), cv);
        d.getDialogPane().setContent(g);

        Optional<ButtonType> r = d.showAndWait();
        if (r.isPresent() && r.get() == save) {
            try {
                if (c == null) c = new Candidat();
                c.setNom(nom.getText()); c.setPrenom(prenom.getText()); c.setCIN(Integer.parseInt(cin.getText()));
                c.setTel(Integer.parseInt(tel.getText())); c.setAdresse(adr.getText()); c.setEmail(email.getText()); c.setCv(cv.getText());
                if (c.getIdCandidat() == 0) service.ajouter(c); else service.modifier(c);
                loadTable(); updatePagination();
            } catch (Exception ex) { showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage()); }
        }
    }

    private void openProfileDialog(Candidat c) {
        openingProfile = true;
        try {
            Dialog<ButtonType> d = new Dialog<>();
            d.setTitle("Profil candidat");
            ButtonType generate = new ButtonType("Générer offre & Envoyer mail", ButtonBar.ButtonData.OK_DONE);
            d.getDialogPane().getButtonTypes().addAll(generate, ButtonType.CLOSE);

            String phaseText;
            try {
                phaseText = workflowService.getCandidatePhase(c.getIdCandidat());
            } catch (SQLException e) {
                phaseText = "UNKNOWN";
            }

            Label info = new Label("Nom: " + c.getNom() + " " + c.getPrenom() + "\nEmail: " + c.getEmail() + "\nPhase: " + phaseText);
            info.getStyleClass().add("profile-info");

            TextField salaireField = new TextField();
            salaireField.setPromptText("Salaire proposé");
            salaireField.textProperty().addListener((obs, oldV, newV) -> {
                if (!newV.matches("\\d*")) salaireField.setText(newV.replaceAll("[^\\d]", ""));
            });

            VBox box = new VBox(12, info, new Label("Salaire de l'offre"), salaireField);
            box.getStyleClass().add("profile-card");
            d.getDialogPane().setContent(box);

            Optional<ButtonType> res = d.showAndWait();
            if (res.isPresent() && res.get() == generate) {
                if (salaireField.getText().isBlank()) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez saisir un salaire.");
                    return;
                }
                int salary = Integer.parseInt(salaireField.getText());
                workflowService.generateSalaryOfferAndSend(c, salary);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Offre envoyée par API mail avec boutons Accepter/Rejeter.");
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        } finally {
            openingProfile = false;
        }
    }

    private void deleteSelected() {
        if (!AuthContext.isAdmin()) return;
        Candidat c = tableCandidat.getSelectionModel().getSelectedItem();
        if (c == null) return;
        try { service.supprimer(c.getIdCandidat()); loadTable(); updatePagination(); }
        catch (SQLException e){showAlert(Alert.AlertType.ERROR,"Erreur",e.getMessage());}
    }

    private void openFilterDialog() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Filtre Nom"); d.setHeaderText(null); d.setContentText("Contient:");
        d.showAndWait().ifPresent(v->{ filtered.setPredicate(c->c.getNom().toLowerCase().contains(v.toLowerCase())); updatePagination();});
    }

    private void applySearch(String q) {
        filtered.setPredicate(c -> q == null || q.isBlank() ||
                c.getNom().toLowerCase().contains(q.toLowerCase()) ||
                c.getPrenom().toLowerCase().contains(q.toLowerCase()) ||
                c.getEmail().toLowerCase().contains(q.toLowerCase()));
    }

    private void applySort() {
        String champ = comboTriChamp.getValue();
        if (champ == null) return;
        SortedList<Candidat> s = new SortedList<>(filtered);
        switch (champ) {
            case "Nom" -> s.setComparator((a,b)->a.getNom().compareToIgnoreCase(b.getNom()));
            case "Prénom" -> s.setComparator((a,b)->a.getPrenom().compareToIgnoreCase(b.getPrenom()));
            case "CIN" -> s.setComparator((a,b)->Integer.compare(a.getCIN(),b.getCIN()));
            case "Téléphone" -> s.setComparator((a,b)->Integer.compare(a.getTel(),b.getTel()));
            case "Adresse" -> s.setComparator((a,b)->a.getAdresse().compareToIgnoreCase(b.getAdresse()));
            case "Email" -> s.setComparator((a,b)->a.getEmail().compareToIgnoreCase(b.getEmail()));
            case "CV" -> s.setComparator((a,b)->a.getCv().compareToIgnoreCase(b.getCv()));
        }
        if ("Décroissant".equals(comboTriOrdre.getValue())) s.setComparator(s.getComparator().reversed());
        tableCandidat.setItems(s);
    }

    private void loadTable() {
        try { master.setAll(service.recuperer()); lblCount.setText(String.valueOf(master.size())); }
        catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage()); }
    }

    private void updatePagination() {
        int total = filtered.size();
        int pages = (int)Math.ceil((double)total/ROWS);
        pagination.setPageCount(pages==0?1:pages);
        pagination.setPageFactory(this::createPage);
    }

    private TableView<Candidat> createPage(int idx){
        int from = idx*ROWS, to = Math.min(from+ROWS, filtered.size());
        tableCandidat.setItems(FXCollections.observableArrayList(filtered.subList(from,to)));
        return tableCandidat;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}