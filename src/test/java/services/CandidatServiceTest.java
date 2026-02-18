package services;

import models.Candidat;
import org.junit.jupiter.api.*;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CandidatServiceTest {

    private static CandidatService service;

    @BeforeAll
    static void setUp() {
        System.out.println("Setting up CandidatService...");
        service = new CandidatService();
    }
    @AfterAll
    static void tearDown() {
        System.out.println("Tearing down CandidatService...");
    }

    @Test
    @Order(1)
    void ajouter() throws SQLException {
        System.out.println("Running ajouter Candidat...");

        Candidat c = new Candidat("TestNom", "TestPrenom",
                12345678, 99887766,
                "Tunis", "test@mail.com", "cv.pdf");

        service.ajouter(c);

        assertFalse(service.recuperer().isEmpty());
        assertEquals("TestNom",
                service.recuperer().get(service.recuperer().size() - 1).getNom());
    }

    @Test
    @Order(2)
    void modifier() throws SQLException {
        System.out.println("Running modifier Candidat...");

        Candidat c = service.recuperer().get(service.recuperer().size() - 1);
        c.setNom("NomModifie");
        c.setPrenom("PrenomModifie");

        service.modifier(c);

        assertEquals("NomModifie",
                service.recuperer().get(service.recuperer().size() - 1).getNom());
    }

    @Test
    @Order(3)
    void supprimer() throws SQLException {
        System.out.println("Running supprimer Candidat...");

        Candidat c = service.recuperer().get(service.recuperer().size() - 1);

        service.supprimer(c.getIdCandidat());

        assertFalse(service.recuperer()
                .stream()
                .anyMatch(p -> p.getIdCandidat() == c.getIdCandidat()));
    }
}
