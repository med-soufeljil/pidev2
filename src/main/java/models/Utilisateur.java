package models;

public class Utilisateur {

    private int id;
    private String nom;
    private String prenom;
    private String mail;
    private Role role;
    private String password;

    public Utilisateur() {}

    public Utilisateur(String nom, String prenom, String mail, Role role, String password) {
        this.nom = nom;
        this.prenom = prenom;
        this.mail = mail;
        this.role = role;
        this.password = password;
    }

    public Utilisateur(int id, String nom, String prenom, String mail, Role role, String password) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.mail = mail;
        this.role = role;
        this.password = password;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    /** Convenience: full display name */
    public String getNomComplet() { return prenom + " " + nom; }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", mail='" + mail + '\'' +
                ", role=" + role +
                '}';
    }
}
