package org.soa.tp1.pi_dev_s2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.soa.tp1.pi_dev_s2.com.esprit.services.StatutScheduler;
import org.soa.tp1.pi_dev_s2.model.Utilisateur;
import org.soa.tp1.pi_dev_s2.mouhamd.utils.ApiRuntime;
import org.soa.tp1.pi_dev_s2.service.UtilisateurService;

import java.sql.SQLException;
import java.util.List;

public class DashboardController {

    @FXML private VBox      sidebar;
    @FXML private Label     lblPageTitle;
    @FXML private Label     lblUserName;
    @FXML private Label     lblUserRole;
    @FXML private StackPane contentArea;

    private Utilisateur          currentUser;
    private final UtilisateurService service = new UtilisateurService();
    private StatutScheduler      statutScheduler;

    // ─────────────────────────────────────────
    public void setUser(Utilisateur user) {
        this.currentUser = user;
        lblUserName.setText(user.getNom() + " " + user.getPrenom());
        lblUserRole.setText("🏷 " + user.getRole().toUpperCase());
        buildSidebar();
        showWelcome();
    }

    // ─────────────────────────────────────────
    //  SIDEBAR DYNAMIQUE selon le rôle
    // ─────────────────────────────────────────
    private void buildSidebar() {
        sidebar.getChildren().clear();

        // Header sidebar
        VBox header = new VBox(4);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20, 10, 20, 10));
        header.setStyle("-fx-background-color: #0D1B6E;");
        Label appName = new Label("⚙ PI_DEV");
        appName.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:white;");
        Label subTitle = new Label("Système de gestion");
        subTitle.setStyle("-fx-font-size:10px;-fx-text-fill:#9FA8DA;");
        header.getChildren().addAll(appName, subTitle);
        sidebar.getChildren().add(header);

        sidebar.getChildren().add(makeSep("Mon espace"));
        sidebar.getChildren().add(makeNavBtn("🏠  Accueil", this::showWelcome));

        String role = currentUser.getRole().toLowerCase();

        // ── ADMIN ──────────────────────────────
        if (role.equals("admin")) {
            sidebar.getChildren().add(makeSep("Administration"));
            sidebar.getChildren().add(makeNavBtn("👥  Gérer utilisateurs", this::showGererUtilisateurs));
            sidebar.getChildren().add(makeNavBtn("🛡  Attribuer rôles",    this::showAttribuerRoles));
            sidebar.getChildren().add(makeNavBtn("📚  Formations",         this::showGererFormations));
            sidebar.getChildren().add(makeNavBtn("🎉  Gérer événements",   this::showGererEvenements));
            sidebar.getChildren().add(makeNavBtn("💼  Recrutement", this::showGererRecrutement));
            sidebar.getChildren().add(makeNavBtn("👤  Mon Profil",         this::showMonProfil));
            sidebar.getChildren().add(makeNavBtn("📊  Statistiques",       this::showStats));
        }

        // ── RH ─────────────────────────────────
        if (role.equals("rh")) {
            sidebar.getChildren().add(makeSep("Ressources Humaines"));
            sidebar.getChildren().add(makeNavBtn("📋  Gérer candidats",        this::showGererCandidats));
            sidebar.getChildren().add(makeNavBtn("💼  Gérer offres d'emploi",  this::showGererOffres));
            sidebar.getChildren().add(makeNavBtn("👔  Gérer employés",         this::showGererEmployes));
            sidebar.getChildren().add(makeNavBtn("📅  Gérer réunions",         this::showGererReunions));
            sidebar.getChildren().add(makeNavBtn("🔍  Suivre service demandé", this::showSuivreService));
            sidebar.getChildren().add(makeNavBtn("✅  Valider/Refuser congé",  this::showValiderConge));
            sidebar.getChildren().add(makeNavBtn("👤  Mon Profil",             this::showMonProfil));
        }

        // ── EMPLOYÉ ────────────────────────────
        if (role.equals("employe")) {
            sidebar.getChildren().add(makeSep("Mon espace employé"));
            sidebar.getChildren().add(makeNavBtn("💬  Accéder au chat",      this::showChat));
            sidebar.getChildren().add(makeNavBtn("🏖  Demander congé",       this::showDemanderConge));
            sidebar.getChildren().add(makeNavBtn("📋  Consulter état congé", this::showEtatConge));
            sidebar.getChildren().add(makeNavBtn("🎉  Choisir événement",    this::showChoisirEvenement));
            sidebar.getChildren().add(makeNavBtn("👤  Mon Profil",           this::showMonProfil));
        }

        // ── CANDIDAT ───────────────────────────
        if (role.equals("candidat")) {
            sidebar.getChildren().add(makeSep("Mon espace candidat"));
            sidebar.getChildren().add(makeNavBtn("📝  Créer profil candidat", this::showCreerProfil));
            sidebar.getChildren().add(makeNavBtn("🔍  Postuler à une offre",  this::showPostuler));
            sidebar.getChildren().add(makeNavBtn("📊  Suivre candidature",    this::showSuivreCandidature));
            sidebar.getChildren().add(makeNavBtn("👤  Mon Profil",            this::showMonProfil));
        }

        // ── CLIENT ─────────────────────────────
        if (role.equals("client")) {
            sidebar.getChildren().add(makeSep("Services"));
            sidebar.getChildren().add(makeNavBtn("📨  Demander service RH",  this::showDemanderServiceRH));
            sidebar.getChildren().add(makeNavBtn("✅  Valider service rendu", this::showValiderService));
            sidebar.getChildren().add(makeNavBtn("👤  Mon Profil",           this::showMonProfil));
        }

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // Déconnexion
        sidebar.getChildren().add(makeSep(""));
        Button btnLogout = new Button("🚪  Déconnexion");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setStyle("-fx-background-color:#B71C1C;-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-alignment:CENTER-LEFT;"
                + "-fx-background-radius:8;-fx-padding:11 16;-fx-cursor:hand;");
        btnLogout.setOnAction(e -> handleLogout());
        sidebar.getChildren().add(btnLogout);
        VBox.setMargin(btnLogout, new Insets(0, 12, 12, 12));
    }

    // ─────────────────────────────────────────
    //  VUES
    // ─────────────────────────────────────────

    private void showWelcome() {
        lblPageTitle.setText("Tableau de bord");
        String role = currentUser.getRole().toLowerCase();

        VBox box = new VBox(20);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(30));

        // Carte de bienvenue
        VBox welcome = new VBox(10);
        welcome.setAlignment(Pos.CENTER);
        welcome.setPadding(new Insets(30));
        welcome.setStyle("-fx-background-color:white;-fx-background-radius:16;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),12,0,0,3);");
        Label icon = new Label(getRoleIcon(role));
        icon.setStyle("-fx-font-size:48px;");
        Label hello = new Label("Bonjour, " + currentUser.getNom() + " !");
        hello.setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:#1A237E;");
        Label roleLabel = new Label("Connecté en tant que : " + currentUser.getRole().toUpperCase());
        roleLabel.setStyle("-fx-font-size:14px;-fx-text-fill:#666;");
        welcome.getChildren().addAll(icon, hello, roleLabel);
        box.getChildren().add(welcome);

        Label quickTitle = new Label("Accès rapide");
        quickTitle.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#1A237E;");
        box.getChildren().add(quickTitle);

        FlowPane cards = new FlowPane(16, 16);
        cards.setAlignment(Pos.CENTER);

        // ✅ CORRIGÉ : plus de sidebar.getChildren().add() dans les cards
        switch (role) {
            case "admin" -> cards.getChildren().addAll(
                    makeQuickCard("👥", "Utilisateurs", "#3949AB", this::showGererUtilisateurs),
                    makeQuickCard("🛡", "Rôles",        "#6A1B9A", this::showAttribuerRoles),
                    makeQuickCard("👤", "Mon Profil",   "#E65100", this::showMonProfil),
                    makeQuickCard("📊", "Statistiques", "#1565C0", this::showStats)
            );
            case "rh" -> cards.getChildren().addAll(
                    makeQuickCard("📋", "Candidats",  "#1A237E", this::showGererCandidats),
                    makeQuickCard("💼", "Offres",     "#2E7D32", this::showGererOffres),
                    makeQuickCard("👔", "Employés",   "#4A148C", this::showGererEmployes),
                    makeQuickCard("📅", "Réunions",   "#BF360C", this::showGererReunions),
                    makeQuickCard("✅", "Congés",     "#00695C", this::showValiderConge)
            );
            case "employe" -> cards.getChildren().addAll(
                    makeQuickCard("💬", "Chat",       "#1565C0", this::showChat),
                    makeQuickCard("🏖", "Congé",      "#2E7D32", this::showDemanderConge),
                    makeQuickCard("📋", "État congé", "#E65100", this::showEtatConge),
                    makeQuickCard("🎉", "Événements", "#6A1B9A", this::showChoisirEvenement)
            );
            case "candidat" -> cards.getChildren().addAll(
                    makeQuickCard("📝", "Mon profil",  "#1A237E", this::showCreerProfil),
                    makeQuickCard("🔍", "Postuler",    "#2E7D32", this::showPostuler),
                    makeQuickCard("📊", "Candidature", "#E65100", this::showSuivreCandidature)
            );
        }

        box.getChildren().add(cards);
        setContent(box);
    }

    // ── ADMIN VIEWS ────────────────────────────
    private void showGererUtilisateurs() {
        lblPageTitle.setText("Gestion des Utilisateurs");
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/GestionUtilisateurs.fxml"));
            setContent(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAttribuerRoles() {
        lblPageTitle.setText("Attribuer les rôles");
        setContent(makeRoleManager());
    }

    private void showMonProfil() {
        lblPageTitle.setText("Mon Profil");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile.fxml"));
            Parent root = loader.load();
            ProfileController ctrl = loader.getController();
            ctrl.setUtilisateur(currentUser);
            setContent(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showStats() {
        lblPageTitle.setText("Statistiques");
        setContent(makeStatsView());
    }

    private void showGererEvenements() {
        lblPageTitle.setText("Gérer les événements");
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/MainView.fxml"));
            setContent(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
    private void showGererRecrutement() {
        lblPageTitle.setText("Gestion du Recrutement");
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/RoleSelectionView.fxml"));
            setContent(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
    private void showGererFormations() {
        lblPageTitle.setText("Formations");
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FormationView.fxml"));
            setContent(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── RH VIEWS ───────────────────────────────
    private void showGererCandidats() {
        lblPageTitle.setText("Gérer les candidats");
        setContent(makePlaceholder("📋", "Gestion candidats", "Consulter, valider et gérer les candidatures reçues"));
    }

    private void showGererOffres() {
        lblPageTitle.setText("Gérer les offres d'emploi");
        setContent(makePlaceholder("💼", "Offres d'emploi", "Créer et publier des offres d'emploi"));
    }

    private void showGererEmployes() {
        lblPageTitle.setText("Gérer les employés");
        setContent(makePlaceholder("👔", "Gestion employés", "Consulter et gérer les fiches des employés"));
    }

    private void showGererReunions() {
        lblPageTitle.setText("Gérer les réunions");
        setContent(makePlaceholder("📅", "Réunions & entretiens", "Planifier les réunions et entretiens RH"));
    }

    private void showSuivreService() {
        lblPageTitle.setText("Suivre service demandé");
        setContent(makePlaceholder("🔍", "Suivi services", "Suivre l'état des services demandés"));
    }

    private void showValiderConge() {
        lblPageTitle.setText("Valider / Refuser congé");
        setContent(makePlaceholder("✅", "Gestion congés", "Valider ou refuser les demandes de congé"));
    }

    // ── EMPLOYÉ VIEWS ──────────────────────────
    private void showChat() {
        lblPageTitle.setText("Chat");
        setContent(makePlaceholder("💬", "Chat interne", "Communiquer avec vos collègues"));
    }

    private void showDemanderConge() {
        lblPageTitle.setText("Demander un congé");
        setContent(makeCongeForm());
    }

    private void showEtatConge() {
        lblPageTitle.setText("État de mes congés");
        setContent(makePlaceholder("📋", "Mes congés", "Consulter l'état de vos demandes de congé"));
    }

    private void showChoisirEvenement() {
        lblPageTitle.setText("Choisir un événement");
        setContent(makePlaceholder("🎉", "Événements", "Consulter et s'inscrire aux événements"));
    }

    // ── CANDIDAT VIEWS ─────────────────────────
    private void showCreerProfil() {
        lblPageTitle.setText("Mon profil candidat");
        setContent(makeProfilCandidatForm());
    }

    private void showPostuler() {
        lblPageTitle.setText("Postuler à une offre");
        setContent(makePlaceholder("🔍", "Offres disponibles", "Consulter et postuler aux offres d'emploi"));
    }

    private void showSuivreCandidature() {
        lblPageTitle.setText("Suivi de candidature");
        setContent(makePlaceholder("📊", "Ma candidature", "Suivre l'état de votre candidature en temps réel"));
    }

    // ── CLIENT VIEWS ───────────────────────────
    private void showDemanderServiceRH() {
        lblPageTitle.setText("Demander un service RH");
        setContent(makePlaceholder("📨", "Service RH", "Soumettre une demande de service aux RH"));
    }

    private void showValiderService() {
        lblPageTitle.setText("Valider service rendu");
        setContent(makePlaceholder("✅", "Validation service", "Confirmer la réception du service rendu"));
    }

    // ─────────────────────────────────────────
    //  GESTIONNAIRE DE RÔLES (Admin)
    // ─────────────────────────────────────────
    private VBox makeRoleManager() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));
        Label title = new Label("Attribution des rôles utilisateurs");
        title.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#1A237E;");
        try {
            List<Utilisateur> users = service.getAllUsers();
            ScrollPane scroll = new ScrollPane();
            VBox list = new VBox(8);
            list.setPadding(new Insets(10));
            for (Utilisateur u : users) {
                HBox row = new HBox(16);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(12, 16, 12, 16));
                row.setStyle("-fx-background-color:white;-fx-background-radius:10;"
                        + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);");
                Label name = new Label(u.getNom() + " " + u.getPrenom());
                name.setStyle("-fx-font-weight:bold;-fx-font-size:13px;");
                name.setMinWidth(180);
                Label mail = new Label(u.getEmail());
                mail.setStyle("-fx-text-fill:#666;-fx-font-size:12px;");
                mail.setMinWidth(220);
                ComboBox<String> roleBox = new ComboBox<>();
                roleBox.getItems().addAll("admin", "rh", "employe", "candidat", "client");
                roleBox.setValue(u.getRole());
                roleBox.setStyle("-fx-background-radius:6;-fx-pref-width:120;");
                Button save = new Button("💾 Sauver");
                save.setStyle("-fx-background-color:#1A237E;-fx-text-fill:white;"
                        + "-fx-background-radius:6;-fx-cursor:hand;-fx-padding:6 12;");
                save.setOnAction(e -> {
                    u.setRole(roleBox.getValue());
                    try { service.updateUser(u); save.setText("✅ Sauvé"); }
                    catch (Exception ex) { ex.printStackTrace(); }
                });
                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);
                row.getChildren().addAll(name, mail, sp, roleBox, save);
                list.getChildren().add(row);
            }
            scroll.setContent(list);
            scroll.setFitToWidth(true);
            scroll.setPrefHeight(500);
            scroll.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");
            box.getChildren().addAll(title, scroll);
        } catch (SQLException e) {
            e.printStackTrace();
            box.getChildren().add(new Label("Erreur chargement utilisateurs."));
        }
        return box;
    }

    // ─────────────────────────────────────────
    //  STATISTIQUES
    // ─────────────────────────────────────────
    private VBox makeStatsView() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(20));
        Label title = new Label("Statistiques globales");
        title.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#1A237E;");
        box.getChildren().add(title);
        try {
            List<Utilisateur> users = service.getAllUsers();
            long admins    = users.stream().filter(u -> u.getRole().equals("admin")).count();
            long rhs       = users.stream().filter(u -> u.getRole().equals("rh")).count();
            long employes  = users.stream().filter(u -> u.getRole().equals("employe")).count();
            long candidats = users.stream().filter(u -> u.getRole().equals("candidat")).count();
            FlowPane cards = new FlowPane(16, 16);
            cards.getChildren().addAll(
                    makeStatCard("👥", "Total",     String.valueOf(users.size()), "#1A237E"),
                    makeStatCard("🛡", "Admins",    String.valueOf(admins),       "#6A1B9A"),
                    makeStatCard("🧑‍💼", "RH",       String.valueOf(rhs),          "#00695C"),
                    makeStatCard("👔", "Employés",  String.valueOf(employes),     "#E65100"),
                    makeStatCard("📋", "Candidats", String.valueOf(candidats),    "#1565C0")
            );
            box.getChildren().add(cards);
            Label distTitle = new Label("Répartition par rôle");
            distTitle.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#333;");
            box.getChildren().add(distTitle);
            int total = users.isEmpty() ? 1 : users.size();
            box.getChildren().add(makeProgressBar("Admins",    admins,    total, "#6A1B9A"));
            box.getChildren().add(makeProgressBar("RH",        rhs,       total, "#00695C"));
            box.getChildren().add(makeProgressBar("Employés",  employes,  total, "#E65100"));
            box.getChildren().add(makeProgressBar("Candidats", candidats, total, "#1565C0"));
        } catch (SQLException e) { e.printStackTrace(); }
        return box;
    }

    private HBox makeProgressBar(String label, long count, int total, String color) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 16, 6, 16));
        row.setStyle("-fx-background-color:white;-fx-background-radius:8;");
        Label lbl = new Label(label);
        lbl.setMinWidth(90);
        lbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;");
        ProgressBar bar = new ProgressBar((double) count / total);
        bar.setPrefWidth(300);
        bar.setStyle("-fx-accent:" + color + ";");
        Label countLbl = new Label(count + " utilisateurs");
        countLbl.setStyle("-fx-text-fill:#666;-fx-font-size:12px;");
        row.getChildren().addAll(lbl, bar, countLbl);
        return row;
    }

    // ─────────────────────────────────────────
    //  FORMULAIRE CONGÉ
    // ─────────────────────────────────────────
    private VBox makeCongeForm() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(30));
        box.setMaxWidth(500);
        box.setStyle("-fx-background-color:white;-fx-background-radius:16;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),12,0,0,3);");
        Label title = new Label("📝 Nouvelle demande de congé");
        title.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#1A237E;");
        Label lDate = new Label("Type de congé");
        lDate.setStyle("-fx-font-size:12px;-fx-text-fill:#666;-fx-font-weight:bold;");
        ComboBox<String> typeConge = new ComboBox<>();
        typeConge.getItems().addAll("Congé annuel", "Congé maladie", "Congé maternité", "Congé sans solde");
        typeConge.setValue("Congé annuel");
        typeConge.setMaxWidth(Double.MAX_VALUE);
        Label lFrom = new Label("Date de début");
        lFrom.setStyle("-fx-font-size:12px;-fx-text-fill:#666;-fx-font-weight:bold;");
        DatePicker dateDebut = new DatePicker();
        dateDebut.setMaxWidth(Double.MAX_VALUE);
        Label lTo = new Label("Date de fin");
        lTo.setStyle("-fx-font-size:12px;-fx-text-fill:#666;-fx-font-weight:bold;");
        DatePicker dateFin = new DatePicker();
        dateFin.setMaxWidth(Double.MAX_VALUE);
        Label lReason = new Label("Motif");
        lReason.setStyle("-fx-font-size:12px;-fx-text-fill:#666;-fx-font-weight:bold;");
        TextArea motif = new TextArea();
        motif.setPromptText("Décrivez le motif de votre demande...");
        motif.setPrefRowCount(3);
        Label msg = new Label("");
        Button btn = new Button("📨 Envoyer la demande");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color:#1A237E;-fx-text-fill:white;"
                + "-fx-font-weight:bold;-fx-font-size:14px;"
                + "-fx-background-radius:8;-fx-padding:12;-fx-cursor:hand;");
        btn.setOnAction(e -> {
            if (dateDebut.getValue() == null || dateFin.getValue() == null) {
                msg.setText("Veuillez sélectionner les dates !");
                msg.setStyle("-fx-text-fill:#C62828;");
            } else {
                msg.setText("✅ Demande envoyée avec succès !");
                msg.setStyle("-fx-text-fill:#2E7D32;");
            }
        });
        box.getChildren().addAll(title, lDate, typeConge, lFrom, dateDebut, lTo, dateFin, lReason, motif, btn, msg);
        VBox wrapper = new VBox(box);
        wrapper.setPadding(new Insets(30));
        wrapper.setAlignment(Pos.TOP_CENTER);
        return wrapper;
    }

    // ─────────────────────────────────────────
    //  PROFIL CANDIDAT
    // ─────────────────────────────────────────
    private VBox makeProfilCandidatForm() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(30));
        box.setMaxWidth(520);
        box.setStyle("-fx-background-color:white;-fx-background-radius:16;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),12,0,0,3);");
        Label title = new Label("📝 Mon profil candidat");
        title.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#1A237E;");
        TextField tfNom    = makeField("Nom",     currentUser.getNom());
        TextField tfPrenom = makeField("Prénom",  currentUser.getPrenom());
        TextField tfEmail  = makeField("Email",   currentUser.getEmail());
        TextField tfTel    = makeField("Téléphone", "");
        TextField tfPoste  = makeField("Poste souhaité", "");
        Label lCv = new Label("CV (lien ou chemin)");
        lCv.setStyle("-fx-font-size:12px;-fx-text-fill:#666;-fx-font-weight:bold;");
        TextField tfCv = new TextField();
        tfCv.setPromptText("URL ou chemin vers votre CV");
        tfCv.setStyle("-fx-background-radius:8;-fx-border-radius:8;"
                + "-fx-border-color:#E0E0E0;-fx-border-width:1.5;-fx-padding:10;");
        Label lBio = new Label("Présentation");
        lBio.setStyle("-fx-font-size:12px;-fx-text-fill:#666;-fx-font-weight:bold;");
        TextArea bio = new TextArea();
        bio.setPromptText("Parlez-nous de vous...");
        bio.setPrefRowCount(3);
        Label msg = new Label("");
        Button save = new Button("💾 Sauvegarder le profil");
        save.setMaxWidth(Double.MAX_VALUE);
        save.setStyle("-fx-background-color:#2E7D32;-fx-text-fill:white;"
                + "-fx-font-weight:bold;-fx-font-size:14px;"
                + "-fx-background-radius:8;-fx-padding:12;-fx-cursor:hand;");
        save.setOnAction(e -> {
            msg.setText("✅ Profil sauvegardé !");
            msg.setStyle("-fx-text-fill:#2E7D32;");
        });
        box.getChildren().addAll(title, tfNom, tfPrenom, tfEmail, tfTel, tfPoste, lCv, tfCv, lBio, bio, save, msg);
        VBox wrapper = new VBox(box);
        wrapper.setPadding(new Insets(30));
        wrapper.setAlignment(Pos.TOP_CENTER);
        return wrapper;
    }

    // ─────────────────────────────────────────
    //  HELPERS UI
    // ─────────────────────────────────────────
    private VBox makePlaceholder(String icon, String title, String desc) {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60));
        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size:64px;");
        Label ttl = new Label(title);
        ttl.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#1A237E;");
        Label dsc = new Label(desc);
        dsc.setStyle("-fx-font-size:14px;-fx-text-fill:#888;");
        Label badge = new Label("🔧 Module en développement");
        badge.setStyle("-fx-background-color:#E8EAF6;-fx-text-fill:#3949AB;"
                + "-fx-padding:6 16;-fx-background-radius:20;-fx-font-size:12px;");
        box.getChildren().addAll(ico, ttl, dsc, badge);
        return box;
    }

    private VBox makeStatCard(String icon, String label, String value, String color) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(160);
        card.setPrefHeight(110);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,3);");
        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size:26px;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size:30px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:11px;-fx-text-fill:#9E9E9E;");
        card.getChildren().addAll(ico, val, lbl);
        return card;
    }

    private VBox makeQuickCard(String icon, String label, String color, Runnable action) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(140);
        card.setPrefHeight(120);
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,3);");
        card.setOnMouseClicked(e -> action.run());
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color:" + color + "22;"
                + "-fx-background-radius:14;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),14,0,0,4);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color:white;-fx-background-radius:14;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,3);"));
        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size:32px;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        card.getChildren().addAll(ico, lbl);
        return card;
    }

    private Button makeNavBtn(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color:transparent;-fx-text-fill:#C5CAE9;"
                + "-fx-font-size:13px;-fx-alignment:CENTER-LEFT;"
                + "-fx-background-radius:8;-fx-padding:11 16;-fx-cursor:hand;");
        btn.setOnAction(e -> {
            resetNavButtons();
            btn.setStyle("-fx-background-color:#3949AB;-fx-text-fill:white;"
                    + "-fx-font-size:13px;-fx-alignment:CENTER-LEFT;"
                    + "-fx-background-radius:8;-fx-padding:11 16;-fx-cursor:hand;");
            action.run();
        });
        btn.setOnMouseEntered(e -> {
            if (!btn.getStyle().contains("#3949AB"))
                btn.setStyle(btn.getStyle().replace("transparent", "rgba(255,255,255,0.08)"));
        });
        btn.setOnMouseExited(e -> {
            if (!btn.getStyle().contains("#3949AB"))
                btn.setStyle(btn.getStyle().replace("rgba(255,255,255,0.08)", "transparent"));
        });
        VBox.setMargin(btn, new Insets(0, 12, 0, 12));
        return btn;
    }

    private void resetNavButtons() {
        for (javafx.scene.Node n : sidebar.getChildren()) {
            if (n instanceof Button b && !b.getStyle().contains("#B71C1C")) {
                b.setStyle("-fx-background-color:transparent;-fx-text-fill:#C5CAE9;"
                        + "-fx-font-size:13px;-fx-alignment:CENTER-LEFT;"
                        + "-fx-background-radius:8;-fx-padding:11 16;-fx-cursor:hand;");
            }
        }
    }

    private Label makeSep(String text) {
        Label sep = new Label(text.isEmpty() ? "" : "  " + text.toUpperCase());
        sep.setStyle("-fx-font-size:10px;-fx-text-fill:#5C6BC0;-fx-font-weight:bold;"
                + "-fx-padding:16 16 4 16;");
        return sep;
    }

    private TextField makeField(String label, String value) {
        TextField tf = new TextField(value);
        tf.setStyle("-fx-background-radius:8;-fx-border-radius:8;"
                + "-fx-border-color:#E0E0E0;-fx-border-width:1.5;-fx-padding:10;");
        return tf;
    }

    private void setContent(javafx.scene.Node node) {
        contentArea.getChildren().setAll(node);
    }

    private String getRoleIcon(String role) {
        return switch (role) {
            case "admin"    -> "🛡";
            case "rh"       -> "🧑‍💼";
            case "employe"  -> "👔";
            case "candidat" -> "📋";
            case "client"   -> "👤";
            default         -> "👤";
        };
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) lblPageTitle.getScene().getWindow();
            stage.setScene(new Scene(root, 440, 520));
            stage.setTitle("Connexion");
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}