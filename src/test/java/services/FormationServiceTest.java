package services;

import entities.Formation;
import entities.Niveau;
import entities.Categorie;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FormationServiceTest {

    private static FormationService service;

    @BeforeAll
    static void setUp() {
        System.out.println("Setting up FormationService tests...");
        service = new FormationService();
    }

    @Test
    @Order(1)
    void ajouter() throws SQLException {
        System.out.println("Running ajouter formation test...");
        Formation f = new Formation();
        f.setTitre("TestFormation");
        f.setDescription("Description Test");
        f.setDuree(10);
        f.setNiveau(Niveau.DEBUTANT);
        f.setCategorie(Categorie.DEV);
        f.setCertification(true);

        service.ajouter(f);

        List<Formation> list = service.recuperer();
        assertFalse(list.isEmpty());
        assertEquals("TestFormation", list.get(list.size() - 1).getTitre());//on a comparé le titre de la derniere ligne de la base par le titre indiqué
    }

    @Test
    @Order(2)
    void modifier() throws SQLException {
        System.out.println("Running modifier formation test...");
        Formation f = service.recuperer().get(service.recuperer().size() - 1);
        f.setTitre("TestFormationModifiee");
        f.setDescription("Desc modifiée");

        service.modifier(f);

        Formation modifiee = service.recuperer().get(service.recuperer().size() - 1);
        assertEquals("TestFormationModifiee", modifiee.getTitre());
    }

    @Test
    @Order(3)
    void supprimer() throws SQLException {
        System.out.println("Running supprimer formation test...");
        Formation f = service.recuperer().get(service.recuperer().size() - 1);

        service.supprimer(f.getId_formation());

        boolean existeEncore = service.recuperer()
                .stream()
                .anyMatch(x -> x.getId_formation() == f.getId_formation());

        assertFalse(existeEncore);
    }
}