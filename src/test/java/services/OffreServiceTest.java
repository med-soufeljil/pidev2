package services;

import models.Offre;
import models.TypeOffre;
import org.junit.jupiter.api.*;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OffreServiceTest {

    private static OffreService service;

    @BeforeAll
    static void setUp() {
        System.out.println("Setting up OffreService...");
        service = new OffreService();
    }

    @Test
    @Order(1)
    void ajouter() throws SQLException {

        Offre o = new Offre("Dev Java", TypeOffre.JUNIOR, "Java,SQL", 3000);
        service.ajouter(o);

        assertFalse(service.recuperer().isEmpty());

        Offre last = service.recuperer()
                .get(service.recuperer().size() - 1);

        assertEquals("Dev Java", last.getNomOffre());
        assertEquals(TypeOffre.JUNIOR, last.getType()); // ✅ vérification enum
    }

    @Test
    @Order(2)
    void modifier() throws SQLException {

        Offre o = service.recuperer()
                .get(service.recuperer().size() - 1);

        o.setNomOffre("Dev Java Senior");
        o.setType(TypeOffre.SENIOR); // ✅ changement enum

        service.modifier(o);

        Offre last = service.recuperer()
                .get(service.recuperer().size() - 1);

        assertEquals("Dev Java Senior", last.getNomOffre());
        assertEquals(TypeOffre.SENIOR, last.getType()); // ✅ vérification
    }

    @Test
    @Order(3)
    void supprimer() throws SQLException {

        Offre o = service.recuperer()
                .get(service.recuperer().size() - 1);

        service.supprimer(o.getIdOffre());

        assertFalse(service.recuperer()
                .stream()
                .anyMatch(offre -> offre.getIdOffre() == o.getIdOffre()));
    }
}
