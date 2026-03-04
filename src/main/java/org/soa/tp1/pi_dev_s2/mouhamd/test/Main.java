package org.soa.tp1.pi_dev_s2.mouhamd.test;

import org.soa.tp1.pi_dev_s2.mouhamd.entities.Apprenant;
import org.soa.tp1.pi_dev_s2.mouhamd.entities.Formation;
import org.soa.tp1.pi_dev_s2.mouhamd.entities.Niveau;
import org.soa.tp1.pi_dev_s2.mouhamd.entities.Categorie;
import org.soa.tp1.pi_dev_s2.mouhamd.services.ApprenantService;
import org.soa.tp1.pi_dev_s2.mouhamd.services.FormationService;

import java.sql.SQLException;
import java.time.LocalDate;

public class Main {

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
