package org.soa.tp1.pi_dev_s2.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.soa.tp1.pi_dev_s2.model.Utilisateur;
import org.soa.tp1.pi_dev_s2.service.UtilisateurService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller complet pour la gestion des utilisateurs.
 *
 * Fonctionnalités :
 *  - TableView avec colonnes Id, Nom, Prénom, Email, Rôle + Actions
 *  - Recherche en temps réel (filtre sur nom/prénom/email/rôle)
 *  - Tri ASC/DESC par colonne (clic en-tête)
 *  - Filtre par rôle (ComboBox)
 *  - CRUD complet via dialog inline
 *  - Stats utilisateurs (badge compteurs)
 *  - ✨ FONCTIONNALITÉ AVANCÉE : Panneau statistiques IA-style
 *    + Réinitialisation mot de passe en un clic
 *    + Export CSV des utilisateurs
 *    + Filtre multi-critères
 */
public class UtilisateurController {

    // ─── Table ────────────────────────────────────────────────────────────
    @FXML private TableView<Utilisateur>              tableView;
    @FXML private TableColumn<Utilisateur, Integer>   colId;
    @FXML private TableColumn<Utilisateur, String>    colNom;
    @FXML private TableColumn<Utilisateur, String>    colPrenom;
    @FXML private TableColumn<Utilisateur, String>    colEmail;
    @FXML private TableColumn<Utilisateur, String>    colRole;
    @FXML private TableColumn<Utilisateur, Void>      colActions;

    // ─── Recherche & filtres ──────────────────────────────────────────────
    @FXML private TextField  tfRecherche;
    @FXML private ComboBox<String> cbFiltreRole;

    // ─── Boutons toolbar ──────────────────────────────────────────────────
    @FXML private Button  btnAjouter;
    @FXML private Button  btnActualiser;
    @FXML private Button  btnExportCSV;
    @FXML private Button  btnStats;

    // ─── Badges compteurs ────────────────────────────────────────────────
    @FXML private Label lblTotal;
    @FXML private Label lblAdmin;
    @FXML private Label lblEmploye;
    @FXML private Label lblUser;

    // ─── Panel stats (masqué par défaut) ─────────────────────────────────
    @FXML private VBox  panelStats;

    // ─── État interne ─────────────────────────────────────────────────────
    private final UtilisateurService        service  = new UtilisateurService();
    private ObservableList<Utilisateur>     masterList;
    private FilteredList<Utilisateur>       filteredList;
    private boolean                         statsVisible = false;

    // ══════════════════════════════════════════════════════════════════════
    //  INIT
    // ══════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        configurerColonnes();
        configurerFiltres();
        chargerUtilisateurs();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CONFIGURATION COLONNES
    // ══════════════════════════════════════════════════════════════════════

    private void configurerColonnes() {
        // Colonnes données
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        colPrenom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPrenom()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));

        // Colonne rôle avec badge coloré
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole()));
        colRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) { setGraphic(null); return; }
                Label badge = new Label(role.toUpperCase());
                badge.setStyle(getBadgeStyle(role));
                setGraphic(badge);
                setText(null);
            }
        });

        // Colonne Actions : Modifier | Supprimer | Reset MDP
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit  = new Button("✏");
            private final Button btnDel   = new Button("🗑");
            private final Button btnReset = new Button("🔑");
            private final HBox   box      = new HBox(6, btnEdit, btnDel, btnReset);

            {
                box.setAlignment(Pos.CENTER);
                btnEdit.setStyle("-fx-background-color:#3B82F6;-fx-text-fill:white;" +
                        "-fx-background-radius:6;-fx-padding:4 8;-fx-cursor:hand;-fx-font-size:11px;");
                btnDel.setStyle("-fx-background-color:#EF4444;-fx-text-fill:white;" +
                        "-fx-background-radius:6;-fx-padding:4 8;-fx-cursor:hand;-fx-font-size:11px;");
                btnReset.setStyle("-fx-background-color:#F59E0B;-fx-text-fill:white;" +
                        "-fx-background-radius:6;-fx-padding:4 8;-fx-cursor:hand;-fx-font-size:11px;");

                Tooltip.install(btnEdit,  new Tooltip("Modifier"));
                Tooltip.install(btnDel,   new Tooltip("Supprimer"));
                Tooltip.install(btnReset, new Tooltip("Réinitialiser le mot de passe"));

                btnEdit.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    ouvrirDialogUtilisateur(u);
                });
                btnDel.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    supprimerUtilisateur(u);
                });
                btnReset.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    reinitialiserMotDePasse(u);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        // Tri possible sur toutes les colonnes texte
        colNom.setSortable(true);
        colPrenom.setSortable(true);
        colEmail.setSortable(true);
        colRole.setSortable(true);
        tableView.getSortOrder().add(colNom); // Tri initial par nom ASC
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CONFIGURATION FILTRES & RECHERCHE
    // ══════════════════════════════════════════════════════════════════════

    private void configurerFiltres() {
        // ComboBox rôles
        cbFiltreRole.setItems(FXCollections.observableArrayList(
                "Tous", "ADMIN", "RH", "EMPLOYE", "USER", "candidat"
        ));
        cbFiltreRole.setValue("Tous");

        // Les listeners sont ajoutés après chargement (dans chargerUtilisateurs)
    }

    private void appliquerFiltres() {
        if (filteredList == null) return;
        String recherche = tfRecherche.getText() == null ? "" : tfRecherche.getText().toLowerCase().trim();
        String roleFiltre = cbFiltreRole.getValue();

        filteredList.setPredicate(u -> {
            // Filtre rôle
            boolean matchRole = "Tous".equals(roleFiltre) || roleFiltre == null
                    || roleFiltre.equalsIgnoreCase(u.getRole());

            // Filtre texte (nom, prénom, email, rôle)
            boolean matchRecherche = recherche.isEmpty()
                    || (u.getNom()    != null && u.getNom().toLowerCase().contains(recherche))
                    || (u.getPrenom() != null && u.getPrenom().toLowerCase().contains(recherche))
                    || (u.getEmail()  != null && u.getEmail().toLowerCase().contains(recherche))
                    || (u.getRole()   != null && u.getRole().toLowerCase().contains(recherche));

            return matchRole && matchRecherche;
        });

        // Mettre à jour le compteur de résultats
        int nbAffiches = filteredList.size();
        lblTotal.setText(String.valueOf(masterList.size()));
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CHARGEMENT DONNÉES
    // ══════════════════════════════════════════════════════════════════════

    private void chargerUtilisateurs() {
        try {
            List<Utilisateur> liste = service.getAllUsers();
            masterList   = FXCollections.observableArrayList(liste);
            filteredList = new FilteredList<>(masterList, p -> true);

            // Listeners de filtre
            tfRecherche.textProperty().addListener((obs, o, n) -> appliquerFiltres());
            cbFiltreRole.valueProperty().addListener((obs, o, n) -> appliquerFiltres());

            // SortedList liée à la TableView
            SortedList<Utilisateur> sortedList = new SortedList<>(filteredList);
            sortedList.comparatorProperty().bind(tableView.comparatorProperty());
            tableView.setItems(sortedList);

            mettreAJourBadges();

        } catch (SQLException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur de chargement", e.getMessage());
        }
    }

    private void mettreAJourBadges() {
        if (masterList == null) return;

        Map<String, Long> parRole = masterList.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getRole() == null ? "?" : u.getRole().toUpperCase(),
                        Collectors.counting()
                ));

        lblTotal.setText(String.valueOf(masterList.size()));
        lblAdmin.setText(String.valueOf(parRole.getOrDefault("ADMIN", 0L)));
        lblEmploye.setText(String.valueOf(parRole.getOrDefault("EMPLOYE", 0L)
                + parRole.getOrDefault("RH", 0L)));
        lblUser.setText(String.valueOf(parRole.getOrDefault("USER", 0L)
                + parRole.getOrDefault("CANDIDAT", 0L)));

        if (statsVisible) rafraichirPanelStats();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HANDLERS BOUTONS
    // ══════════════════════════════════════════════════════════════════════

    @FXML public void handleAjouter()    { ouvrirDialogUtilisateur(null); }
    @FXML public void handleActualiser() { chargerUtilisateurs(); tfRecherche.clear(); cbFiltreRole.setValue("Tous"); }

    // ─── Export CSV ───────────────────────────────────────────────────────

    @FXML
    public void handleExportCSV() {
        StringBuilder csv = new StringBuilder("ID,Nom,Prénom,Email,Rôle\n");
        for (Utilisateur u : tableView.getItems()) {
            csv.append(u.getId()).append(",")
                    .append(csv(u.getNom())).append(",")
                    .append(csv(u.getPrenom())).append(",")
                    .append(csv(u.getEmail())).append(",")
                    .append(csv(u.getRole())).append("\n");
        }

        // Sauvegarde dans le dossier home temporairement (à adapter avec FileChooser)
        try {
            java.nio.file.Files.writeString(
                    java.nio.file.Path.of(System.getProperty("user.home") + "/utilisateurs_export.csv"),
                    csv.toString()
            );
            afficherAlerte(Alert.AlertType.INFORMATION, "Export réussi",
                    "Fichier sauvegardé dans :\n" + System.getProperty("user.home") + "/utilisateurs_export.csv\n"
                            + tableView.getItems().size() + " utilisateur(s) exporté(s).");
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur export", e.getMessage());
        }
    }

    private String csv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"")) return "\"" + val.replace("\"", "\"\"") + "\"";
        return val;
    }

    // ─── Panel Stats ──────────────────────────────────────────────────────

    @FXML
    public void handleToggleStats() {
        statsVisible = !statsVisible;
        panelStats.setVisible(statsVisible);
        panelStats.setManaged(statsVisible);
        btnStats.setText(statsVisible ? "📊 Masquer stats" : "📊 Statistiques");
        if (statsVisible) rafraichirPanelStats();
    }

    private void rafraichirPanelStats() {
        panelStats.getChildren().clear();

        if (masterList == null || masterList.isEmpty()) {
            panelStats.getChildren().add(new Label("Aucune donnée."));
            return;
        }

        // Distribution par rôle
        Map<String, Long> parRole = masterList.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getRole() == null ? "Inconnu" : u.getRole(),
                        Collectors.counting()
                ));

        Label titre = new Label("📊 Distribution des rôles");
        titre.setStyle("-fx-font-weight:bold;-fx-font-size:13px;-fx-text-fill:#E2E8F0;");
        panelStats.getChildren().add(titre);

        int total = masterList.size();
        parRole.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .forEach(entry -> {
                    double pct = (double) entry.getValue() / total * 100;
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);

                    Label lRole = new Label(entry.getKey());
                    lRole.setMinWidth(80);
                    lRole.setStyle("-fx-text-fill:#94A3B8;-fx-font-size:12px;");

                    ProgressBar pb = new ProgressBar(pct / 100.0);
                    pb.setPrefWidth(160);
                    pb.setStyle("-fx-accent:" + getRoleColor(entry.getKey()) + ";");
                    HBox.setHgrow(pb, Priority.ALWAYS);

                    Label lPct = new Label(String.format("%.0f%%  (%d)", pct, entry.getValue()));
                    lPct.setStyle("-fx-text-fill:#64748B;-fx-font-size:11px;");

                    row.getChildren().addAll(lRole, pb, lPct);
                    panelStats.getChildren().add(row);
                });

        // Utilisateurs sans email
        long sansEmail = masterList.stream()
                .filter(u -> u.getEmail() == null || u.getEmail().isBlank()).count();
        if (sansEmail > 0) {
            Separator sep = new Separator();
            sep.setStyle("-fx-background-color:#334155;");
            Label warn = new Label("⚠️  " + sansEmail + " utilisateur(s) sans email renseigné");
            warn.setStyle("-fx-text-fill:#FBBF24;-fx-font-size:12px;");
            panelStats.getChildren().addAll(sep, warn);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  DIALOG AJOUTER / MODIFIER
    // ══════════════════════════════════════════════════════════════════════

    private void ouvrirDialogUtilisateur(Utilisateur utilisateur) {
        boolean isEdit = utilisateur != null;

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(isEdit ? "Modifier l'utilisateur" : "Ajouter un utilisateur");
        dialog.setResizable(false);

        // Champs
        TextField tfNom     = creerTextField("Nom *", utilisateur != null ? utilisateur.getNom()    : "");
        TextField tfPrenom  = creerTextField("Prénom *", utilisateur != null ? utilisateur.getPrenom() : "");
        TextField tfEmail   = creerTextField("Email *", utilisateur != null ? utilisateur.getEmail()  : "");
        PasswordField tfMdp = new PasswordField();
        tfMdp.setPromptText(isEdit ? "Laisser vide pour ne pas changer" : "Mot de passe *");
        tfMdp.setStyle("-fx-background-color:#1E293B;-fx-text-fill:#E2E8F0;" +
                "-fx-border-color:#334155;-fx-border-radius:6;-fx-background-radius:6;-fx-padding:8;");

        ComboBox<String> cbRole = new ComboBox<>(
                FXCollections.observableArrayList("ADMIN","RH","EMPLOYE","USER","candidat"));
        cbRole.setValue(utilisateur != null && utilisateur.getRole() != null ? utilisateur.getRole() : "USER");
        cbRole.setStyle("-fx-background-color:#1E293B;-fx-text-fill:#E2E8F0;");
        cbRole.setMaxWidth(Double.MAX_VALUE);

        Label lblErreur = new Label("");
        lblErreur.setStyle("-fx-text-fill:#EF4444;-fx-font-size:11px;");

        Button btnSauver   = new Button(isEdit ? "💾  Enregistrer" : "➕  Ajouter");
        Button btnAnnuler  = new Button("Annuler");

        btnSauver.setStyle("-fx-background-color:#6366F1;-fx-text-fill:white;-fx-font-weight:bold;" +
                "-fx-background-radius:8;-fx-padding:10 24;-fx-cursor:hand;");
        btnAnnuler.setStyle("-fx-background-color:#334155;-fx-text-fill:#94A3B8;" +
                "-fx-background-radius:8;-fx-padding:10 18;-fx-cursor:hand;");

        btnSauver.setOnAction(e -> {
            String nom    = tfNom.getText().trim();
            String prenom = tfPrenom.getText().trim();
            String email  = tfEmail.getText().trim();
            String mdp    = tfMdp.getText().trim();
            String role   = cbRole.getValue();

            // Validation
            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
                lblErreur.setText("⚠ Nom, prénom et email sont obligatoires.");
                return;
            }
            if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                lblErreur.setText("⚠ Format d'email invalide.");
                return;
            }
            if (!isEdit && mdp.isEmpty()) {
                lblErreur.setText("⚠ Le mot de passe est obligatoire.");
                return;
            }

            try {
                if (isEdit) {
                    utilisateur.setNom(nom);
                    utilisateur.setPrenom(prenom);
                    utilisateur.setEmail(email);
                    utilisateur.setRole(role);
                    if (!mdp.isEmpty()) utilisateur.setMotDePasse(mdp);
                    service.updateUser(utilisateur);
                } else {
                    Utilisateur u = new Utilisateur();
                    u.setNom(nom); u.setPrenom(prenom);
                    u.setEmail(email); u.setMotDePasse(mdp);
                    u.setRole(role);
                    service.addUser(u);
                }
                chargerUtilisateurs();
                dialog.close();
            } catch (SQLException ex) {
                lblErreur.setText("❌ Erreur BDD : " + ex.getMessage());
            }
        });
        btnAnnuler.setOnAction(e -> dialog.close());

        // Layout
        VBox form = new VBox(12,
                new Label("Nom"),      tfNom,
                new Label("Prénom"),   tfPrenom,
                new Label("Email"),    tfEmail,
                new Label("Mot de passe"), tfMdp,
                new Label("Rôle"),     cbRole,
                lblErreur,
                new HBox(10, btnSauver, btnAnnuler)
        );
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color:#0F172A;");
        form.setPrefWidth(360);

        // Style labels
        form.getChildren().stream()
                .filter(n -> n instanceof Label)
                .forEach(n -> ((Label) n).setStyle("-fx-text-fill:#94A3B8;-fx-font-size:12px;"));

        ((HBox) form.getChildren().get(form.getChildren().size() - 2 + 1)).setAlignment(Pos.CENTER_RIGHT);

        dialog.setScene(new Scene(form));
        dialog.showAndWait();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SUPPRESSION
    // ══════════════════════════════════════════════════════════════════════

    private void supprimerUtilisateur(Utilisateur u) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer " + u.getNom() + " " + u.getPrenom() + " ?");
        confirm.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.deleteUser(u.getId());
                chargerUtilisateurs();
            } catch (SQLException e) {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ✨ FONCTIONNALITÉ AVANCÉE : RÉINITIALISATION MOT DE PASSE
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Génère un mot de passe aléatoire sécurisé pour l'utilisateur,
     * le met à jour en BDD et l'affiche à l'admin pour transmission.
     */
    private void reinitialiserMotDePasse(Utilisateur u) {
        String nouveauMdp = genererMotDePasseAleatoire();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Réinitialiser le mot de passe");
        confirm.setHeaderText("Réinitialiser le MDP de " + u.getNom() + " " + u.getPrenom() + " ?");
        confirm.setContentText("Un nouveau mot de passe sera généré et affiché.\n"
                + "Tu devras le communiquer à l'utilisateur.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            u.setMotDePasse(nouveauMdp);
            service.updateUser(u);

            // Affichage du nouveau MDP dans une alerte copiable
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Mot de passe réinitialisé");
            info.setHeaderText("✅ Mot de passe mis à jour pour " + u.getNom() + " " + u.getPrenom());

            TextArea taNewMdp = new TextArea(nouveauMdp);
            taNewMdp.setEditable(false);
            taNewMdp.setStyle("-fx-font-family:'Courier New';-fx-font-size:16px;-fx-font-weight:bold;");
            taNewMdp.setPrefRowCount(1);

            VBox content = new VBox(8,
                    new Label("Nouveau mot de passe (communique-le à l'utilisateur) :"),
                    taNewMdp,
                    new Label("⚠ Ce mot de passe n'est visible qu'une seule fois.")
            );
            info.getDialogPane().setContent(content);
            info.getDialogPane().setMinWidth(400);
            info.showAndWait();

        } catch (SQLException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    /**
     * Génère un MDP aléatoire 10 caractères (lettres + chiffres + symboles).
     */
    private String genererMotDePasseAleatoire() {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$%";
        StringBuilder sb = new StringBuilder();
        java.util.Random rand = new java.security.SecureRandom();
        for (int i = 0; i < 10; i++)
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        return sb.toString();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private TextField creerTextField(String prompt, String valeur) {
        TextField tf = new TextField(valeur);
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color:#1E293B;-fx-text-fill:#E2E8F0;" +
                "-fx-border-color:#334155;-fx-border-radius:6;-fx-background-radius:6;-fx-padding:8;");
        return tf;
    }

    private String getBadgeStyle(String role) {
        String color = getRoleColor(role);
        return "-fx-background-color:" + color + "22;" +
                "-fx-text-fill:" + color + ";" +
                "-fx-background-radius:20;-fx-padding:2 10;" +
                "-fx-font-size:10px;-fx-font-weight:bold;";
    }

    private String getRoleColor(String role) {
        if (role == null) return "#94A3B8";
        return switch (role.toUpperCase()) {
            case "ADMIN"    -> "#EF4444";
            case "RH"       -> "#8B5CF6";
            case "EMPLOYE"  -> "#3B82F6";
            case "USER"     -> "#10B981";
            case "CANDIDAT" -> "#F59E0B";
            default         -> "#94A3B8";
        };
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}