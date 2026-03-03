package com.esprit.services;

import com.esprit.models.Participation;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServiceParticipationTest {

    private static ParticipationService service;

    @BeforeAll
    static void setUp() {
        service = new ParticipationService();
    }

    @Test
    @Order(1)
    void ajouter() throws SQLException {

        Participation p
                = new Participation(
                5,                       // id_e
                LocalDate.now(),
                "INSCRIT",
                false
        );


        service.ajouter(p);

        assertFalse(service.recuperer().isEmpty());
    }

    @Test
    @Order(2)
    void modifier() throws SQLException {

        Participation p =
                service.recuperer().get(service.recuperer().size() - 1);

        p.setStatut("refuse");
        service.modifier(p);

        assertEquals("refuse",
                service.recuperer()
                        .get(service.recuperer().size() - 1)
                        .getStatut());
    }

   /* @Test
    @Order(3)
    void supprimer() throws SQLException {

        Participation p =
                service.recuperer().get(service.recuperer().size() - 1);

        service.supprimer(p);

        assertFalse(
                service.recuperer()
                        .stream()
                        .anyMatch(part ->
                                part.getId_p() ==
                                        p.getId_p())
        );
    }*/
}