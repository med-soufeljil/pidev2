package test;

<<<<<<< HEAD
import entities.Apprenant;
import entities.Formation;
import entities.Niveau;
import entities.Categorie;
import services.ApprenantService;
import services.FormationService;

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
=======
import services.CandidatService;
import services.OffreService;
import services.ReunionService;

import java.sql.SQLException;

public class Main {


    public static void main(String[] args) {
        OffreService ps = new OffreService();

        /*try {
            ps.ajouter(new Offre("poste Ing","Senior","SQL,C++",1600));
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }*/

       /* try {
            ps.modifier(new Offre(2,"poste Ing","Senior","SQL,C++",1500));
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
//

        try {
            System.out.println(ps.recuperer());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        CandidatService cs = new CandidatService();

        /*try {
            cs.ajouter(new Candidat("Ali", "Ben Salah", 12345678, 22113344, "Tunis", "ali@gmail.com", "cv.pdf"));
>>>>>>> feature-mohamed
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }*/

<<<<<<< HEAD

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
=======
       /* try {
            cs.modifier(new Candidat(2,"Ali",  12345678,"Ben Salah", 22113344, "Kelibia", "ali@gmail.com", "cv.pdf"));
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
//

        try {
            System.out.println(cs.recuperer());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        ReunionService rs = new ReunionService();
        /*try {
            rs.ajouter(new Reunion(1,2,LocalDateTime.of(2026, 2, 10, 10, 30), "https://meet.google.com/abc-defg-hij"));
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }*/

        /*try {
            rs.modifier(new Reunion(2, 1, 2, LocalDateTime.of(2026, 2, 10, 11, 0), "https://meet.google.com/updated-link"));
>>>>>>> feature-mohamed
        } catch (SQLException e) {
            e.printStackTrace();
        }*/

        try {
<<<<<<< HEAD
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



=======
            System.out.println(rs.recuperer());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }


    }
>>>>>>> feature-mohamed
}
