package com.esprit.models;


public class CategorieEvenement {
    private int idCategorie;
    private String nomCategorie;
    private String description;

    public CategorieEvenement() {}

    public CategorieEvenement(int idCategorie, String nomCategorie, String description) {
        this.idCategorie = idCategorie;
        this.nomCategorie = nomCategorie;
        this.description = description;
    }

    public CategorieEvenement(String nomCategorie, String description) {
        this.nomCategorie = nomCategorie;
        this.description = description;
    }

    // Getters & Setters
    public int getIdCategorie() { return idCategorie; }
    public void setIdCategorie(int idCategorie) { this.idCategorie = idCategorie; }

    public String getNomCategorie() { return nomCategorie; }
    public void setNomCategorie(String nomCategorie) { this.nomCategorie = nomCategorie; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "CategorieEvenement{" +
                "idCategorie=" + idCategorie +
                ", nomCategorie='" + nomCategorie + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}