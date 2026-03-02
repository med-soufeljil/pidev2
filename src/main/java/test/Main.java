package test;

import services.CandidatService;
import services.OffreService;
import services.ReunionService;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        OffreService offreService = new OffreService();
        CandidatService candidatService = new CandidatService();
        ReunionService reunionService = new ReunionService();

        try {
            System.out.println(offreService.recuperer());
            System.out.println(candidatService.recuperer());
            System.out.println(reunionService.recuperer());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
