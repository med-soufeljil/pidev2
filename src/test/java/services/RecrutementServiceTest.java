package services;

import models.Recrutement;
import org.junit.jupiter.api.*;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecrutementServiceTest {

    private static RecrutementService service;

    @BeforeAll
    static void setUp() {
        System.out.println("Setting up RecrutementService...");
        service = new RecrutementService();
    }

    @Test
    @Order(1)
    void ajouter() throws SQLException {
        Recrutement r = new Recrutement(1, 1);
        service.ajouter(r);

        assertFalse(service.recuperer().isEmpty());
    }

    @Test
    @Order(2)
    void modifier() throws SQLException {
        Recrutement r = service.recuperer().get(service.recuperer().size() - 1);
        r.setIdOffre(1);
        r.setIdCandidat(1);

        service.modifier(r);

        assertEquals(1,
                service.recuperer().get(service.recuperer().size() - 1).getIdOffre());
    }

    @Test
    @Order(3)
    void supprimer() throws SQLException {
        Recrutement r = service.recuperer().get(service.recuperer().size() - 1);
        service.supprimer(r.getIdRec());

        assertFalse(service.recuperer()
                .stream()
                .anyMatch(rec -> rec.getIdRec() == r.getIdRec()));
    }
}
