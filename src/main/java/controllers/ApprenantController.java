package controllers;
// package : indique que cette classe appartient au dossier logique "controllers"
// BUT : organiser le projet (MVC : Controller)

import entities.Apprenant;
// import : permet d'utiliser la classe Apprenant définie dans le package entities
// BUT : manipuler des objets Apprenant (id, nom, prénom, formation)

import entities.Formation;
// import : permet d'utiliser la classe Formation
// BUT : représenter une formation (id, titre…)

import services.ApprenantService;
// import : service qui contient les méthodes CRUD pour Apprenant
// BUT : communiquer avec la base de données pour les apprenants

import services.FormationService;
// import : service pour gérer les formations dans la base

import javafx.fxml.FXML;
// import FXML : permet d’associer les éléments graphiques du FXML au code Java
// BUT : faire le lien entre interface graphique et contrôleur

import javafx.fxml.FXMLLoader;
// FXMLLoader : sert à charger un fichier FXML
// BUT : changer de page (scène)

import javafx.fxml.Initializable;
// Initializable : interface JavaFX
// BUT : forcer l’existence de la méthode initialize()

import javafx.collections.FXCollections;
// FXCollections : outils pour créer des listes observables
// BUT : permettre la mise à jour automatique du TableView

import javafx.collections.transformation.FilteredList;
// FilteredList : liste filtrable
// BUT : permettre la recherche dynamique

import javafx.scene.control.*;
// import des composants graphiques (TextField, TableView, ComboBox, etc.)

import javafx.scene.input.KeyEvent;
// KeyEvent : événement clavier
// BUT : détecter la saisie dans le champ de recherche

import javafx.scene.Parent;
// Parent : noeud racine d’une scène JavaFX

import javafx.scene.Scene;
// Scene : représente le contenu d’une fenêtre

import javafx.stage.Stage;
// Stage : représente la fenêtre principale

import java.net.URL;
// URL : utilisé par initialize()

import java.sql.SQLException;
// SQLException : erreur liée à la base de données

import java.util.List;
// List : collection d’objets

import java.util.ResourceBundle;
// ResourceBundle : ressources (langue, config)

public class ApprenantController implements Initializable {
// public : accessible partout
// class : déclaration de classe
// implements Initializable : oblige à définir initialize()
// BUT : cette classe est le contrôleur de l’interface des apprenants

    @FXML private TextField tfNom, tfPrenom, tfRecherche;
    // @FXML : lié aux champs du fichier FXML
    // TextField : champ de saisie
    // BUT : récupérer le nom, prénom et texte de recherche

    @FXML private ComboBox<Formation> cbFormation;
    // ComboBox : liste déroulante
    // BUT : choisir une formation pour l’apprenant

    @FXML private TableView<Apprenant> tableApprenant;
    // TableView : tableau graphique
    // BUT : afficher la liste des apprenants

    @FXML private TableColumn<Apprenant, Integer> colId;
    // TableColumn : colonne du tableau
    // BUT : afficher l’id de l’apprenant

    @FXML private TableColumn<Apprenant, String> colNom;
    // BUT : afficher le nom

    @FXML private TableColumn<Apprenant, String> colPrenom;
    // BUT : afficher le prénom

    @FXML private TableColumn<Apprenant, String> colFormation;
    // BUT : afficher le titre de la formation

    private ApprenantService service = new ApprenantService();
    // new : création d’un objet
    // BUT : accéder aux méthodes ajouter, modifier, supprimer, récupérer

    private FormationService formationService = new FormationService();
    // BUT : accéder aux formations depuis la base

    private FilteredList<Apprenant> filteredList;
    // FilteredList : liste filtrable
    // BUT : permettre la recherche

    private List<Formation> formations;
    // List : liste normale
    // BUT : stocker les formations récupérées

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // initialize : exécutée automatiquement au chargement du FXML
        // BUT : initialiser la ComboBox, la TableView et les données

        try {
            formations = formationService.recuperer();
            // BUT : récupérer les formations depuis la base

            cbFormation.setItems(FXCollections.observableArrayList(formations));
            // BUT : afficher les formations dans la ComboBox

            cbFormation.setConverter(new javafx.util.StringConverter<Formation>() {
                // BUT : afficher uniquement le titre de la formation dans la ComboBox

                @Override
                public String toString(Formation f) {
                    // BUT : transformer un objet Formation en texte
                    return f != null ? f.getTitre() : "";
                }

                @Override
                public Formation fromString(String string) {
                    // BUT : retrouver l’objet Formation correspondant au texte affiché
                    return cbFormation.getItems().stream()
                            .filter(f -> f.getTitre().equals(string))
                            .findFirst()
                            .orElse(null);
                }
            });

            colId.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleIntegerProperty(
                            data.getValue().getIdApprenant()
                    ).asObject());
            // BUT : afficher l’id dans la colonne

            colNom.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().getNom()
                    ));
            // BUT : afficher le nom

            colPrenom.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().getPrenom()
                    ));
            // BUT : afficher le prénom

            colFormation.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            getTitreFormation(data.getValue().getId_formation())
                    )
            );
            // BUT : afficher le nom de la formation au lieu de son id

            afficher();
            // BUT : charger les apprenants dans la table

            tableApprenant.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldSel, newSel) -> {
                        // BUT : détecter la sélection d’une ligne

                        if (newSel != null) {
                            tfNom.setText(newSel.getNom());
                            // BUT : remplir le champ nom

                            tfPrenom.setText(newSel.getPrenom());
                            // BUT : remplir le champ prénom

                            cbFormation.getItems().stream()
                                    .filter(f -> f.getId_formation() == newSel.getId_formation())
                                    .findFirst()
                                    .ifPresent(cbFormation::setValue);
                            // BUT : sélectionner la formation correspondante
                        }
                    });

        } catch (SQLException e) {
            e.printStackTrace();
            // BUT : afficher l’erreur si problème DB
        }
    }

    private String getTitreFormation(int idFormation) {
        // BUT : convertir un id de formation en titre

        return formations.stream()
                .filter(f -> f.getId_formation() == idFormation)
                .map(Formation::getTitre)
                .findFirst()
                .orElse("N/A");
    }

    @FXML
    public void ajouter() {
        // BUT : ajouter un apprenant dans la base

        try {
            Formation f = cbFormation.getValue();
            // BUT : récupérer la formation choisie

            if (f == null) return;
            // BUT : empêcher l’ajout sans formation

            Apprenant a = new Apprenant();
            // BUT : créer un nouvel objet Apprenant

            a.setNom(tfNom.getText());
            // BUT : affecter le nom

            a.setPrenom(tfPrenom.getText());
            // BUT : affecter le prénom

            a.setId_formation(f.getId_formation());
            // BUT : affecter la formation

            service.ajouter(a);
            // BUT : enregistrer dans la base

            afficher();
            // BUT : rafraîchir le tableau

            clearFields();
            // BUT : vider les champs

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void modifier() {
        // BUT : modifier l’apprenant sélectionné

        try {
            Apprenant a = tableApprenant.getSelectionModel().getSelectedItem();
            Formation f = cbFormation.getValue();

            if (a != null && f != null) {
                a.setNom(tfNom.getText());
                a.setPrenom(tfPrenom.getText());
                a.setId_formation(f.getId_formation());

                service.modifier(a);
                // BUT : mettre à jour dans la base

                afficher();
                // BUT : rafraîchir le tableau
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void supprimer() {
        // BUT : supprimer l’apprenant sélectionné

        try {
            Apprenant a = tableApprenant.getSelectionModel().getSelectedItem();

            if (a != null) {
                service.supprimer(a.getIdApprenant());
                // BUT : supprimer de la base

                afficher();
                // BUT : rafraîchir le tableau
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void filtrer(KeyEvent event) {
        // BUT : filtrer les apprenants selon la recherche

        if (filteredList != null) {
            String text = tfRecherche.getText().toLowerCase();

            filteredList.setPredicate(a ->
                    a.getNom().toLowerCase().contains(text) ||
                            a.getPrenom().toLowerCase().contains(text) ||
                            getTitreFormation(a.getId_formation()).toLowerCase().contains(text)
            );
            // BUT : afficher seulement les apprenants correspondants
        }
    }

    @FXML
    public void retourMain() {
        // BUT : revenir à la fenêtre principale

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
            Stage stage = (Stage) tfNom.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void afficher() throws SQLException {
        // BUT : charger les apprenants depuis la base et les afficher

        List<Apprenant> apprenants = service.recuperer();

        filteredList = new FilteredList<>(
                FXCollections.observableArrayList(apprenants),
                p -> true
        );

        tableApprenant.setItems(filteredList);
    }

    private void clearFields() {
        // BUT : vider les champs de saisie

        tfNom.clear();
        tfPrenom.clear();
        cbFormation.setValue(null);
    }
}