package services;

import models.Candidat;
import models.Offre;
import models.Recrutement;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfExportService {

    private final CandidatService candidatService = new CandidatService();
    private final OffreService offreService = new OffreService();
    private final RecrutementService recrutementService = new RecrutementService();

    public Path exportGlobalReport(Path destinationFile) throws IOException, SQLException {
        List<Candidat> candidats = candidatService.recuperer();
        List<Offre> offres = offreService.recuperer();
        List<Recrutement> recrutements = recrutementService.recuperer();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float y = 760;

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
                cs.newLineAtOffset(50, y);
                cs.showText("Recruitment System - Executive Report");
                cs.endText();

                y -= 25;
                writeLine(cs, 50, y, "Generated at: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                y -= 30;

                writeSectionTitle(cs, 50, y, "Global Metrics");
                y -= 20;
                writeLine(cs, 60, y, "- Total candidats: " + candidats.size());
                y -= 16;
                writeLine(cs, 60, y, "- Total offres: " + offres.size());
                y -= 16;
                writeLine(cs, 60, y, "- Total recrutements: " + recrutements.size());
                y -= 30;

                writeSectionTitle(cs, 50, y, "Latest Offers");
                y -= 20;
                for (int i = 0; i < Math.min(5, offres.size()); i++) {
                    Offre o = offres.get(i);
                    writeLine(cs, 60, y, "- " + o.getNomOffre() + " | " + o.getType() + " | salaire " + o.getSalaire());
                    y -= 14;
                }

                y -= 20;
                writeSectionTitle(cs, 50, y, "Latest Recrutements");
                y -= 20;
                for (int i = 0; i < Math.min(5, recrutements.size()); i++) {
                    Recrutement r = recrutements.get(i);
                    writeLine(cs, 60, y, "- " + r.getNomCandidat() + " -> " + r.getNomOffre());
                    y -= 14;
                }
            }

            document.save(destinationFile.toFile());
        }

        return destinationFile;
    }

    private void writeSectionTitle(PDPageContentStream cs, float x, float y, String value) throws IOException {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
        cs.newLineAtOffset(x, y);
        cs.showText(value);
        cs.endText();
    }

    private void writeLine(PDPageContentStream cs, float x, float y, String value) throws IOException {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 11);
        cs.newLineAtOffset(x, y);
        cs.showText(value);
        cs.endText();
    }
}
