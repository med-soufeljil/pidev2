package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import services.OfferResponseHttpServer;
import utils.ApiRuntime;
import utils.AuthContext;
import utils.SessionContext;


public class MainController {

    private static MainController instance;

    @FXML
    private Button btnDashboard, btnCandidat, btnOffre, btnReunion, btnRecrutement;
    @FXML
    private Button btnRecruitmentSpace, btnTrainingSpace, btnLeaveSpace, btnUsers, btnFormation, btnApprenant;
    @FXML
    private Button btnFormationDashboard, btnCongesDashboard, btnConges, btnTeletravail;
    @FXML
    private ToggleButton toggleDarkMode;
    @FXML
    private AnchorPane contentArea;
    @FXML
    private VBox welcomePane, recruitmentMenu, trainingMenu, leaveMenu;
    @FXML
    private BorderPane rootPane;
    @FXML
    private Label lblRole, lblModuleSubtitle, lblTopbarTitle, lblTopbarSubtitle, lblSystemStatus;
    @FXML
    private Label lblWelcomeTitle, lblWelcomeSubtitle;
    @FXML
    private Label lblKpi1Label, lblKpi1Value, lblKpi2Label, lblKpi2Value, lblKpi3Label, lblKpi3Value;
    @FXML
    private Label lblWorkflowTitle, lblWorkflow1, lblWorkflow2, lblWorkflow3;

    private String activeSpace = "recruitment";

    @FXML
    public void initialize() {
        instance = this;
        rootPane.getStyleClass().add("light-mode");

        syncFormationRole();
        OfferResponseHttpServer.ensureStarted();
        ApiRuntime.ensureStarted();

        toggleDarkMode.selectedProperty().addListener((obs, oldVal, isDarkMode) -> {
            rootPane.getStyleClass().removeAll("light-mode", "dark-mode");
            rootPane.getStyleClass().add(isDarkMode ? "dark-mode" : "light-mode");
        });

        btnRecruitmentSpace.setOnAction(e -> showRecruitmentSpace());
        btnTrainingSpace.setOnAction(e -> showTrainingSpace());
        btnLeaveSpace.setOnAction(e -> showLeaveSpace());
        btnUsers.setOnAction(e -> loadUI("Utilisateurs.fxml"));

        btnDashboard.setOnAction(e -> loadUI("Dashboard.fxml"));
        btnCandidat.setOnAction(e -> loadUI("Candidat.fxml"));
        btnOffre.setOnAction(e -> loadUI("Offre.fxml"));
        btnRecrutement.setOnAction(e -> loadUI("Recrutement.fxml"));
        btnReunion.setOnAction(e -> loadUI("Reunion.fxml"));

        btnFormation.setOnAction(e -> loadUI("FormationView.fxml"));
        btnApprenant.setOnAction(e -> loadUI("ApprenantView.fxml"));
        btnFormationDashboard.setOnAction(e -> loadUI("dashboardformation.fxml"));

        btnCongesDashboard.setOnAction(e -> loadUI("CongesTeletravail.fxml"));
        btnConges.setOnAction(e -> loadUI("Conges.fxml"));
        btnTeletravail.setOnAction(e -> loadUI("Teletravail.fxml"));

        applyPermissions();
        showRecruitmentSpace();
    }

    public static boolean isActive() {
        return instance != null;
    }

    public static void navigate(String fxml) {
        if (instance != null) {
            instance.loadUI(fxml);
        }
    }

    public static void showHome() {
        if (instance != null) {
            instance.showWelcome();
        }
    }

    public static void showTrainingHome() {
        if (instance != null) {
            instance.showTrainingSpace();
        }
    }

    private void syncFormationRole() {
        if (SessionContext.getCurrentRole() == null) {
            SessionContext.setCurrentRole(AuthContext.isAdmin() ? SessionContext.Role.ADMIN : SessionContext.Role.USER);
        }
    }

    private void applyPermissions() {
        boolean isAdmin = AuthContext.isAdmin();
        lblRole.setText("Role: " + (AuthContext.getCurrentUser() == null ? AuthContext.getRole() : AuthContext.getCurrentUser().getRole()));

        btnUsers.setVisible(isAdmin);
        btnUsers.setManaged(isAdmin);

        btnDashboard.setVisible(isAdmin);
        btnDashboard.setManaged(isAdmin);
        btnCandidat.setDisable(!isAdmin);
        btnRecrutement.setDisable(!isAdmin);
        btnReunion.setDisable(!isAdmin);

        btnApprenant.setVisible(isAdmin);
        btnApprenant.setManaged(isAdmin);
        btnFormationDashboard.setVisible(isAdmin);
        btnFormationDashboard.setManaged(isAdmin);
    }

    private void showRecruitmentSpace() {
        activeSpace = "recruitment";
        recruitmentMenu.setVisible(true);
        recruitmentMenu.setManaged(true);
        trainingMenu.setVisible(false);
        trainingMenu.setManaged(false);
        leaveMenu.setVisible(false);
        leaveMenu.setManaged(false);
        lblModuleSubtitle.setText("Recruitment Management Suite");
        lblTopbarTitle.setText("Recruitment Operations Dashboard");
        lblTopbarSubtitle.setText("Track candidates, offers, hires and interviews in one place");
        lblSystemStatus.setText("● Connected to recruitment database");
        showWelcome();
    }

    private void showTrainingSpace() {
        activeSpace = "training";
        recruitmentMenu.setVisible(false);
        recruitmentMenu.setManaged(false);
        trainingMenu.setVisible(true);
        trainingMenu.setManaged(true);
        leaveMenu.setVisible(false);
        leaveMenu.setManaged(false);
        lblModuleSubtitle.setText("Training Management Suite");
        lblTopbarTitle.setText("Training Operations Dashboard");
        lblTopbarSubtitle.setText("Track formations, learners, feedbacks and reports with the same UI");
        lblSystemStatus.setText("● Connected to training database");
        showWelcome();
    }

    private void showLeaveSpace() {
        activeSpace = "leave";
        recruitmentMenu.setVisible(false);
        recruitmentMenu.setManaged(false);
        trainingMenu.setVisible(false);
        trainingMenu.setManaged(false);
        leaveMenu.setVisible(true);
        leaveMenu.setManaged(true);
        lblModuleSubtitle.setText("Leave & Remote Work Suite");
        lblTopbarTitle.setText("Congés et Télétravail");
        lblTopbarSubtitle.setText("Pilotez les demandes congés, TT, décisions RH et indicateurs");
        lblSystemStatus.setText("● Connected to leave and remote-work database");
        showWelcome();
    }


    private void showWelcome() {
        contentArea.getChildren().setAll(welcomePane);
        AnchorPane.setTopAnchor(welcomePane, 20.0);
        AnchorPane.setBottomAnchor(welcomePane, 20.0);
        AnchorPane.setLeftAnchor(welcomePane, 20.0);
        AnchorPane.setRightAnchor(welcomePane, 20.0);
        welcomePane.setVisible(true);
        welcomePane.setManaged(true);
        if ("training".equals(activeSpace)) {
            lblWelcomeTitle.setText("Bienvenue dans la gestion des formations 🎓");
            lblWelcomeSubtitle.setText("Utilisez le même sidebar pour gérer formations, apprenants et dashboard.");
            setWelcomeCards(
                    "Catalogue", "Formations, niveaux et certificats",
                    "Apprenants", "Suivi des inscriptions",
                    "Analytics", "Dashboard et exports",
                    "Workflow formation",
                    "1. Créer ou mettre à jour les formations.",
                    "2. Suivre les apprenants et feedbacks.",
                    "3. Piloter les statistiques et exports.");
        } else if ("leave".equals(activeSpace)) {
            lblWelcomeTitle.setText("Bienvenue dans la gestion Congés & TT 🌴");
            lblWelcomeSubtitle.setText("Accédez au dashboard puis gérez les demandes congés et télétravail avec CRUD complet.");
            setWelcomeCards(
                    "Dashboard RH", "Congés, TT et décisions",
                    "Demandes congés", "CRUD et validation",
                    "Demandes TT", "Suivi mensuel télétravail",
                    "Workflow congés & TT",
                    "1. Consulter les indicateurs du dashboard.",
                    "2. Ajouter, modifier ou supprimer les demandes.",
                    "3. Approuver ou refuser avec commentaire.");
        } else {
            lblWelcomeTitle.setText("Welcome back, HR Team 👋");
            lblWelcomeSubtitle.setText("Choose a module from the left to start managing the full recruitment lifecycle.");
            setWelcomeCards(
                    "Candidate Pipeline", "Organize profiles, contacts and CVs",
                    "Open Positions", "Centralize offers and required skills",
                    "Hiring Decisions", "Convert top candidates faster",
                    "Recommended workflow",
                    "1. Create or review open offers.",
                    "2. Receive applications from job offers.",
                    "3. Update statuses, generate interviews and send offers.");
        }
    }

    private void setWelcomeCards(String kpi1Label, String kpi1Value, String kpi2Label, String kpi2Value, String kpi3Label, String kpi3Value,
                                 String workflowTitle, String workflow1, String workflow2, String workflow3) {
        lblKpi1Label.setText(kpi1Label);
        lblKpi1Value.setText(kpi1Value);
        lblKpi2Label.setText(kpi2Label);
        lblKpi2Value.setText(kpi2Value);
        lblKpi3Label.setText(kpi3Label);
        lblKpi3Value.setText(kpi3Value);
        lblWorkflowTitle.setText(workflowTitle);
        lblWorkflow1.setText(workflow1);
        lblWorkflow2.setText(workflow2);
        lblWorkflow3.setText(workflow3);
    }

    private void loadUI(String fxml) {
        try {
            Parent pane = FXMLLoader.load(getClass().getResource("/" + fxml));
            if (pane != null) {
                contentArea.getChildren().setAll(pane);
                AnchorPane.setTopAnchor(pane, 0.0);
                AnchorPane.setBottomAnchor(pane, 0.0);
                AnchorPane.setLeftAnchor(pane, 0.0);
                AnchorPane.setRightAnchor(pane, 0.0);
                welcomePane.setVisible(false);
                welcomePane.setManaged(false);
            }
        } catch (Exception ex) {
            System.err.println("Erreur lors du chargement du FXML: " + fxml);
            ex.printStackTrace();
        }
    }
}
