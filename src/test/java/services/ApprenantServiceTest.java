package services;

import entities.Apprenant;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApprenantServiceTest {

    private static ApprenantService service;

    @BeforeAll
    static void setUp() {
        System.out.println("Setting up ApprenantService tests...");
        service = new ApprenantService();
    }

    @Test
    @Order(1)
    void ajouter() throws SQLException {
        System.out.println("Running ajouter apprenant test...");
        Apprenant a = new Apprenant();
        a.setNom("TestNom");
        a.setPrenom("TestPrenom");
        a.setEmail("test@mail.com");
        a.setStatut("Actif");
        a.setDateDebut(LocalDate.now());
        a.setDateFin(LocalDate.now().plusMonths(3));
        a.setId_formation(1); // ⚠️ doit exister dans la table formation

        service.ajouter(a);

        List<Apprenant> list = service.recuperer();
        assertFalse(list.isEmpty());
        assertEquals("TestNom", list.get(list.size() - 1).getNom());
    }

    @Test
    @Order(2)
    void modifier() throws SQLException {
        System.out.println("Running modifier apprenant test...");
        Apprenant a = service.recuperer().get(service.recuperer().size() - 1);
        a.setNom("TestNomModifie");
        a.setPrenom("TestPrenomModifie");

        service.modifier(a);

        Apprenant modifie = service.recuperer().get(service.recuperer().size() - 1);
        assertEquals("TestNomModifie", modifie.getNom());
    }

    @Test
    @Order(3)
    void supprimer() throws SQLException {
        System.out.println("Running supprimer apprenant test...");
        Apprenant a = service.recuperer().get(service.recuperer().size() - 1);

        service.supprimer(a.getIdApprenant());

        boolean existeEncore = service.recuperer()
                .stream()
                .anyMatch(x -> x.getIdApprenant() == a.getIdApprenant());

        assertFalse(existeEncore);
    }
}