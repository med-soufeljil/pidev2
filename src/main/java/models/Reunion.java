package models;

import java.time.LocalDateTime;

public class Reunion {
    private int idReunion;
    private int idRH;
    private int idCandidat;
    private LocalDateTime date;
    private String link;
    private String nomCandidat;

    public Reunion(int idReunion, int idRH, int idCandidat, LocalDateTime date, String link) {
        this.idReunion = idReunion;
        this.idRH = idRH;
        this.idCandidat = idCandidat;
        this.date = date;
        this.link = link;
    }

    public Reunion(int idRH, int idCandidat, LocalDateTime date, String link) {
        this.idRH = idRH;
        this.idCandidat = idCandidat;
        this.date = date;
        this.link = link;
    }

    public Reunion() {
    }

    public int getIdReunion() {
        return idReunion;
    }

    public void setIdReunion(int idReunion) {
        this.idReunion = idReunion;
    }

    public int getIdRH() {
        return idRH;
    }

    public void setIdRH(int idRH) {
        this.idRH = idRH;
    }

    public int getIdCandidat() {
        return idCandidat;
    }

    public void setIdCandidat(int idCandidat) {
        this.idCandidat = idCandidat;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "Reunion{" +
                "idReunion=" + idReunion +
                ", idRH=" + idRH +
                ", idCandidat=" + idCandidat +
                ", date=" + date +
                ", link='" + link + '\'' +
                '}';
    }

    public String getNomCandidat() {
        return nomCandidat;
    }

    public void setNomCandidat(String nomCandidat) {
        this.nomCandidat = nomCandidat;
    }
}
