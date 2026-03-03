package com.esprit.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Evenement {
    private int idEvenement;
    private String titre;
    private int idCategorie; // foreign key vers CategorieEvenement
    private LocalDate dateEvenement;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String lieu;
    private int nombrePlacesMax;
    private int nombreInscrits;
    private String statut;
    private String description;

    public Evenement() {}

    public Evenement(int idEvenement, String titre, int idCategorie, LocalDate dateEvenement,
                     LocalTime heureDebut, LocalTime heureFin, String lieu,
                     int nombrePlacesMax, int nombreInscrits, String statut, String description) {
        this.idEvenement = idEvenement;
        this.titre = titre;
        this.idCategorie = idCategorie;
        this.dateEvenement = dateEvenement;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.lieu = lieu;
        this.nombrePlacesMax = nombrePlacesMax;
        this.nombreInscrits = nombreInscrits;
        this.statut = statut;
        this.description = description;
    }

    public Evenement(String titre, int idCategorie, LocalDate dateEvenement,
                     LocalTime heureDebut, LocalTime heureFin, String lieu,
                     int nombrePlacesMax, int nombreInscrits, String statut, String description) {
        this.titre = titre;
        this.idCategorie = idCategorie;
        this.dateEvenement = dateEvenement;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.lieu = lieu;
        this.nombrePlacesMax = nombrePlacesMax;
        this.nombreInscrits = nombreInscrits;
        this.statut = statut;
        this.description = description;
    }

    // Getters & Setters
    public int getIdEvenement() { return idEvenement; }
    public void setIdEvenement(int idEvenement) { this.idEvenement = idEvenement; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public int getidCategorie() { return idCategorie; }
    public void setidCategorie(int idCategorie) { this.idCategorie = idCategorie; }

    public LocalDate getDateEvenement() { return dateEvenement; }
    public void setDateEvenement(LocalDate dateEvenement) { this.dateEvenement = dateEvenement; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public int getNombrePlacesMax() { return nombrePlacesMax; }
    public void setNombrePlacesMax(int nombrePlacesMax) { this.nombrePlacesMax = nombrePlacesMax; }

    public int getNombreInscrits() { return nombreInscrits; }
    public void setNombreInscrits(int nombreInscrits) { this.nombreInscrits = nombreInscrits; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }


    @Override
    public String toString() {
        return "Evenement{" +
                "idEvenement=" + idEvenement +
                ", titre='" + titre + '\'' +
                ", idCategorie=" + idCategorie +
                ", dateEvenement=" + dateEvenement +
                ", heureDebut=" + heureDebut +
                ", heureFin=" + heureFin +
                ", lieu='" + lieu + '\'' +
                ", nombrePlacesMax=" + nombrePlacesMax +
                ", nombreInscrits=" + nombreInscrits +
                ", statut='" + statut + '\'' +
                ", description='" + description + '\'' +
                '}';
    }


    public int getIdCategorie() {
        return idCategorie;
    }

    public void setIdCategorie(int idCategorie) {
        this.idCategorie = idCategorie;
    }
}