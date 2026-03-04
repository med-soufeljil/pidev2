package org.soa.tp1.pi_dev_s2.model;

public class Utilisateur {

    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String role;

    // ✅ Constructeur complet avec id
    public Utilisateur(int id, String nom, String prenom, String email, String motDePasse, String role) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    // ✅ Constructeur sans id (pour INSERT)
    public Utilisateur(String nom, String prenom, String email, String motDePasse, String role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    // Constructeur vide
    public Utilisateur() {}

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }
    public String getNom()                  { return nom; }
    public void setNom(String nom)          { this.nom = nom; }
    public String getPrenom()               { return prenom; }
    public void setPrenom(String prenom)    { this.prenom = prenom; }
    public String getEmail()                { return email; }
    public void setEmail(String email)      { this.email = email; }
    public String getMotDePasse()           { return motDePasse; }
    public void setMotDePasse(String mdp)   { this.motDePasse = mdp; }
    public String getRole()                 { return role; }
    public void setRole(String role)        { this.role = role; }

    @Override
    public String toString() {
        return nom + " " + prenom + " (" + email + ")";
    }
}