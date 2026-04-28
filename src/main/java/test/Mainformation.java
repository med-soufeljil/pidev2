package test;

import entities.Apprenant;
import entities.Formation;
import entities.Niveau;
import entities.Categorie;
import services.ApprenantService;
import services.FormationService;

import java.sql.SQLException;
import java.time.LocalDate;

public class Mainformation {

    public static void main(String[] args) {

        FormationService fs = new FormationService();

       /* try {
            fs.ajouter(new Formation(
                    "Java JDBC",
                    "Apprendre la connexion base de données",
                    Niveau.INTERMEDIAIRE,
                    30,
                    Categorie.DEV,
                    true
            ));
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }*/


        /*try {
            fs.modifier(new Formation(
                    1,
                    "Java JDBC Avancé",
                    "JDBC + PreparedStatement",
                    40,
                    Categorie.DEV,
                    Niveau.AVANCE,
                    true
            ));
        } catch (SQLException e) {
            e.printStackTrace();
        }*/

        try {
            System.out.println(fs.recuperer());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        ApprenantService as = new ApprenantService();
        /*try {
            as.ajouter(new Apprenant(
                    "Ali",
                    "Ben Salah",
                    "ali@gmail.com",
                    "Actif",
                    LocalDate.of(2026, 2, 1),
                    LocalDate.of(2026, 3, 1),
                    1
            ));
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }*/
        try {
            as.modifier(new Apprenant(
                    2,
                    "Mohamed",
                    "Ben Salah",
                    "ali@gmail.com",
                    "Actif",
                    LocalDate.of(2026, 2, 1),
                    LocalDate.of(2026, 3, 1),
                    1
            ));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            System.out.println(as.recuperer());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }



}
