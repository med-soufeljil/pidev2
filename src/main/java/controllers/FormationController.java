package controllers;
// package : indique que cette classe appartient au package "controllers"
// BUT : organiser le projet selon l’architecture MVC (Controller)

import entities.Categorie;
// import : permet d’utiliser la classe Categorie (enum ou entité)

import entities.Formation;
// import : permet d’utiliser la classe Formation (objet métier)

import entities.Niveau;
// import : permet d’utiliser la classe Niveau (enum)

import javafx.beans.property.SimpleBooleanProperty;
// SimpleBooleanProperty : propriété observable pour les booléens (TableView)

import javafx.beans.property.SimpleIntegerProperty;
// SimpleIntegerProperty : propriété observable pour les entiers

import javafx.beans.property.SimpleObjectProperty;
// SimpleObjectProperty : propriété observable pour les objets (Niveau, Categorie)

import javafx.beans.property.SimpleStringProperty;
// SimpleStringProperty : propriété observable pour les chaînes de caractères

import javafx.collections.FXCollections;
// FXCollections : utilitaires pour créer des listes observables JavaFX

import javafx.collections.transformation.FilteredList;
// FilteredList : liste qui permet le filtrage (recherche)

import javafx.fxml.FXML;
// @FXML : relie les composants du fichier FXML au contrôleur Java

import javafx.fxml.FXMLLoader;
// FXMLLoader : sert à charger un fichier FXML (changer d’écran)

import javafx.fxml.Initializable;
// Initializable : interface qui impose la méthode initialize()

import javafx.scene.Parent;
// Parent : nœud racine d’une scène JavaFX

import javafx.scene.Scene;
// Scene : représente le contenu d’une fenêtre

import javafx.scene.control.*;
// TextField, ComboBox, TableView, TableColumn, CheckBox, Alert…

import javafx.stage.Stage;
// Stage : représente une fenêtre

import services.FormationService;
// FormationService : service qui gère la base de données pour Formation

import java.net.URL;
// URL : utilisé par initialize()

import java.sql.SQLException;
// SQLException : exception liée à la base de données

import java.util.ResourceBundle;
// ResourceBundle : ressources (langue, configuration)

public class FormationController implements Initializable {
// public : accessible depuis tout le projet
// class : déclaration de la classe
// implements Initializable : oblige à définir initialize()
// BUT : cette classe est le contrôleur de l’interface Formation

    @FXML private TextField tfRecherche;
    // Champ de recherche

    @FXML private TextField tfTitre, tfDescription, tfDuree;
    // Champs pour saisir titre, description et durée

    @FXML private ComboBox<Niveau> cbNiveau;
    // ComboBox pour choisir le niveau

    @FXML private ComboBox<Categorie> cbCategorie;
    // ComboBox pour choisir la catégorie

    @FXML private CheckBox cbCertification;
    // CheckBox pour indiquer si la formation est certifiante

    @FXML private TableView<Formation> tableFormation;
    // TableView qui affiche les formations

    @FXML private TableColumn<Formation, Integer> colId;
    // Colonne pour l’id

    @FXML private TableColumn<Formation, String> colTitre;
    // Colonne pour le titre

    @FXML private TableColumn<Formation, Integer> colDuree;
    // Colonne pour la durée

    @FXML private TableColumn<Formation, Niveau> colNiveau;
    // Colonne pour le niveau

    @FXML private TableColumn<Formation, Categorie> colCategorie;
    // Colonne pour la catégorie

    @FXML private TableColumn<Formation, Boolean> colCertif;
    // Colonne pour la certification (true/false)

    private FormationService service = new FormationService();
    // new : création d’un objet
    // BUT : accéder aux méthodes ajouter, modifier, supprimer, récupérer

    private FilteredList<Formation> filteredData;
    // Liste filtrable
    // BUT : permettre la recherche dynamique

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // initialize : exécutée automatiquement au chargement du FXML
        // BUT : initialiser ComboBox, TableView et données

        cbNiveau.setItems(FXCollections.observableArrayList(Niveau.values()));
        // BUT : remplir la ComboBox Niveau avec les valeurs de l’enum Niveau

        cbCategorie.setItems(FXCollections.observableArrayList(Categorie.values()));
        // BUT : remplir la ComboBox Catégorie avec les valeurs de l’enum Categorie

        colId.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getId_formation()).asObject());
        // BUT : afficher l’id dans la colonne

        colTitre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTitre()));
        // BUT : afficher le titre

        colDuree.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getDuree()).asObject());
        // BUT : afficher la durée

        colNiveau.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getNiveau()));
        // BUT : afficher le niveau

        colCategorie.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getCategorie()));
        // BUT : afficher la catégorie

        colCertif.setCellValueFactory(data ->
                new SimpleBooleanProperty(data.getValue().isCertification()));
        // BUT : afficher true/false pour certification

        tableFormation.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            // BUT : détecter la sélection d’une ligne du tableau

            if (newSel != null) remplirChamps(newSel);
            // BUT : remplir les champs du formulaire avec les données sélectionnées
        });

        afficher();
        // BUT : charger les formations dans le tableau

        tfRecherche.textProperty().addListener((obs, oldVal, newVal) -> filtrerTable(newVal));
        // BUT : filtrer la table quand on tape dans la recherche
    }

    // ---------------- CRUD ----------------

    @FXML
    void ajouter() {
        // BUT : ajouter une formation dans la base

        try {
            String titre = tfTitre.getText();

            if (!titreValide(titre)) {
                alert("Titre invalide", "Le titre ne doit contenir que des lettres.");
                return;
            }

            if (!valideFormulaire()) return;
            // BUT : vérifier que tous les champs sont remplis

            Formation f = new Formation();
            // BUT : créer un objet Formation

            f.setTitre(titre);
            f.setDescription(tfDescription.getText());
            f.setDuree(Integer.parseInt(tfDuree.getText()));
            f.setNiveau(cbNiveau.getValue());
            f.setCategorie(cbCategorie.getValue());
            f.setCertification(cbCertification.isSelected());

            service.ajouter(f);
            // BUT : enregistrer dans la base la nouvelle ligne

            afficher();
            // BUT : rafraîchir la table c ad ajouter la nouvelle ligne au tableau

            clearForm();
            // BUT : vider les champs apres l ajout

        } catch (NumberFormatException e) {
            alert("Erreur de saisie", "La durée doit être un nombre entier.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void modifier() {
        // BUT : modifier la formation sélectionnée

        Formation f = tableFormation.getSelectionModel().getSelectedItem();
        if (f == null) return;

        try {
            String titre = tfTitre.getText();

            if (!titreValide(titre)) {
                alert("Titre invalide", "Le titre ne doit contenir que des lettres.");
                return;
            }

            if (!valideFormulaire()) return;

            f.setTitre(titre);
            f.setDescription(tfDescription.getText());
            f.setDuree(Integer.parseInt(tfDuree.getText()));
            f.setNiveau(cbNiveau.getValue());
            f.setCategorie(cbCategorie.getValue());
            f.setCertification(cbCertification.isSelected());

            service.modifier(f);
            // BUT : mettre à jour dans la base

            afficher();
            clearForm();

        } catch (NumberFormatException e) {
            alert("Erreur de saisie", "La durée doit être un nombre entier.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void supprimer() {
        // BUT : supprimer la formation sélectionnée

        Formation f = tableFormation.getSelectionModel().getSelectedItem();
        if (f == null) return;

        try {
            service.supprimer(f.getId_formation());
            // BUT : supprimer dans la base

            afficher();
            clearForm();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void afficher() {
        // BUT : afficher toutes les formations dans la table

        try {
            filteredData = new FilteredList<>(
                    FXCollections.observableArrayList(service.recuperer()), p -> true
            );
            tableFormation.setItems(filteredData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- Retour Main ----------------

    @FXML
    void retourMain() {
        // BUT : revenir à la fenêtre principale

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
            Stage stage = (Stage) tfTitre.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- Fonctions auxiliaires ----------------

    private void remplirChamps(Formation f) {
        // BUT : remplir le formulaire avec une formation sélectionnée

        tfTitre.setText(f.getTitre());
        tfDescription.setText(f.getDescription());
        tfDuree.setText(String.valueOf(f.getDuree()));
        cbNiveau.setValue(f.getNiveau());
        cbCategorie.setValue(f.getCategorie());
        cbCertification.setSelected(f.isCertification());
    }

    private void clearForm() {
        // BUT : vider le formulaire

        tfTitre.clear();
        tfDescription.clear();
        tfDuree.clear();
        cbNiveau.setValue(null);
        cbCategorie.setValue(null);
        cbCertification.setSelected(false);
    }

    private boolean titreValide(String titre) {
        // BUT : vérifier que le titre contient seulement des lettres

        return titre != null && titre.matches("[a-zA-ZÀ-ÿ\\s]+");
    }

    private void alert(String header, String content) {
        // BUT : afficher une boîte d’erreur

        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur de saisie");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private boolean valideFormulaire() {
        // BUT : vérifier que tous les champs sont remplis

        if (tfTitre.getText().isEmpty() || tfDescription.getText().isEmpty() || tfDuree.getText().isEmpty()) {
            alert("Champs manquants", "Veuillez remplir tous les champs.");
            return false;
        }
        if (cbNiveau.getValue() == null) {
            alert("Niveau manquant", "Veuillez sélectionner un niveau.");
            return false;
        }
        if (cbCategorie.getValue() == null) {
            alert("Catégorie manquante", "Veuillez sélectionner une catégorie.");
            return false;
        }
        return true;
    }

    private void filtrerTable(String texte) {
        // BUT : filtrer les formations selon le texte recherché

        if (filteredData == null) return;

        filteredData.setPredicate(f -> {
            if (texte == null || texte.isEmpty()) return true;
            return f.getTitre().toLowerCase().contains(texte.toLowerCase());
        });
    }
}