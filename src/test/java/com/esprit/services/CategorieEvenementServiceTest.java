package com.esprit.services;

import com.esprit.models.CategorieEvenement;
import org.junit.jupiter.api.*;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CategorieEvenementServiceTest {
    private static CategorieEvenementService service;

    @BeforeAll
    static void setUp() {
        service = new CategorieEvenementService();
    }

    @Test  // ADD THIS MISSING ANNOTATION
    @Order(1)
    void ajouter() throws SQLException {
        CategorieEvenement c = new CategorieEvenement("webinaire", "Description Test");
        service.ajouter(c);
        assertFalse(service.recuperer().isEmpty());
    }

    @Test
    @Order(2)
    void modifier() throws SQLException {
        CategorieEvenement c = service.recuperer().get(service.recuperer().size() - 1);
        c.setNomCategorie("Cat Modified");  // FIX: Change from "conference" to "Cat Modified"
        service.modifier(c);
        assertEquals("Cat Modified",
                service.recuperer()
                        .get(service.recuperer().size() - 1)
                        .getNomCategorie());
    }

    @Test
    @Order(3)
    void supprimer() throws SQLException {
        CategorieEvenement c = service.recuperer().get(service.recuperer().size() - 1);
        service.supprimer(c.getIdCategorie());  // This uses the helper method
        assertFalse(
                service.recuperer()
                        .stream()
                        .anyMatch(cat -> cat.getIdCategorie() == c.getIdCategorie())
        );
    }
}