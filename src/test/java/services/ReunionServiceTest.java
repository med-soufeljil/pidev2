package services;

import models.Reunion;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReunionServiceTest {

    private static ReunionService service;

    @BeforeAll
    static void setUp() {
        System.out.println("Setting up ReunionService...");
        service = new ReunionService();
    }

    @Test
    @Order(1)
    void ajouter() throws SQLException {
        System.out.println("Running ajouter Reunion...");

        Reunion r = new Reunion();
        r.setIdRH(1);              // ⚠ doit exister en base
        r.setIdCandidat(1);        // ⚠ doit exister en base
        r.setDate(LocalDateTime.now().plusDays(1));
        r.setLink("https://meet.google.com/test");

        service.ajouter(r);

        assertFalse(service.recuperer().isEmpty());

        Reunion last = service.recuperer()
                .get(service.recuperer().size() - 1);

        assertEquals("https://meet.google.com/test", last.getLink());
    }

    @Test
    @Order(2)
    void modifier() throws SQLException {
        System.out.println("Running modifier Reunion...");

        Reunion r = service.recuperer()
                .get(service.recuperer().size() - 1);

        r.setLink("https://zoom.us/modified");
        r.setDate(LocalDateTime.now().plusDays(2));

        service.modifier(r);

        Reunion updated = service.recuperer()
                .get(service.recuperer().size() - 1);

        assertEquals("https://zoom.us/modified", updated.getLink());
    }

    @Test
    @Order(3)
    void supprimer() throws SQLException {
        System.out.println("Running supprimer Reunion...");

        Reunion r = service.recuperer()
                .get(service.recuperer().size() - 1);

        service.supprimer(r.getIdReunion());

        assertFalse(service.recuperer()
                .stream()
                .anyMatch(reunion -> reunion.getIdReunion() == r.getIdReunion()));
    }
}
