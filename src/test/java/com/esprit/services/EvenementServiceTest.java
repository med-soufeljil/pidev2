package com.esprit.services;

import com.esprit.models.Evenement;  // FIX: Correct import
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EvenementServiceTest {

    private static EvenementService service;

    @BeforeAll
    static void setUp() {
        service = new EvenementService();
    }

    @Test
    @Order(1)
    void ajouter() throws SQLException {
        // FIX: Use correct class with proper package
        Evenement e = new Evenement(
                0,  // ID will be auto-generated
                "Test Event",
                1,  // category ID
                LocalDate.of(2026, 5, 10),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                "Salle Test",
                50,
                0,
                "planifie",
                "Description Test"
        );

        service.ajouter(e);
        assertFalse(service.recuperer().isEmpty());
    }

    @Test
    @Order(2)
    void modifier() throws SQLException {
        // Get the last added event
        Evenement e = service.recuperer().get(service.recuperer().size() - 1);
        e.setTitre("Event Modified");
        service.modifier(e);

        // Verify the modification
        assertEquals("Event Modified",
                service.recuperer()
                        .get(service.recuperer().size() - 1)
                        .getTitre());
    }

    @Test
    @Order(3)
    void supprimer() throws SQLException {
        // Get the last added event
        Evenement e = service.recuperer().get(service.recuperer().size() - 1);
        int idToDelete = e.getIdEvenement();  // Store ID before deletion
        service.supprimer(e);

        // Verify deletion
        assertFalse(
                service.recuperer()
                        .stream()
                        .anyMatch(ev -> ev.getIdEvenement() == idToDelete)  // FIX: Compare with stored ID
        );
    }
}