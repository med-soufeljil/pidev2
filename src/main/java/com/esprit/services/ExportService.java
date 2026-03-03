package com.esprit.services;

import com.esprit.models.Evenement;
import com.esprit.models.Participation;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportService {

    private final EvenementService evenementService = new EvenementService();
    private final ParticipationService participationService = new ParticipationService();
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter EXCEL_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter EXCEL_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * EXPORT PDF - Liste complète des événements
     */
    public void exportEvenementsPDF(String cheminFichier) throws Exception {
        List<Evenement> evenements = evenementService.recuperer();

        PdfWriter writer = new PdfWriter(cheminFichier);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("LISTE DES ÉVÉNEMENTS")
                .setBold()
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Généré le " + LocalDateTime.now().format(DATETIME_FORMAT))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Résumé : " + evenements.size() + " événements au total")
                .setFontSize(12));
        document.add(new Paragraph(" "));

        Table table = new Table(UnitValue.createPercentArray(new float[]{15, 10, 8, 8, 15, 8, 8, 8, 10}));
        table.setWidth(UnitValue.createPercentValue(100));

        String[] headers = {"ID", "Titre", "Date", "Début", "Fin", "Lieu", "Places", "Inscrits", "Statut"};
        for (String header : headers) {
            table.addHeaderCell(new Paragraph(header).setBold());
        }

        for (Evenement e : evenements) {
            table.addCell(String.valueOf(e.getIdEvenement()));
            table.addCell(e.getTitre());
            table.addCell(e.getDateEvenement() != null ? e.getDateEvenement().format(DATE_FORMAT) : "-");
            table.addCell(e.getHeureDebut() != null ? e.getHeureDebut().format(DateTimeFormatter.ofPattern("HH:mm")) : "-");
            table.addCell(e.getHeureFin() != null ? e.getHeureFin().format(DateTimeFormatter.ofPattern("HH:mm")) : "-");
            table.addCell(e.getLieu());
            table.addCell(String.valueOf(e.getNombrePlacesMax()));
            table.addCell(String.valueOf(e.getNombreInscrits()));
            table.addCell(e.getStatut());
        }

        document.add(table);
        document.close();

        System.out.println("✅ PDF créé: " + new java.io.File(cheminFichier).getAbsolutePath());
    }

    /**
     * EXPORT EXCEL - Statistiques complètes
     */
    public void exportStatistiquesExcel(String cheminFichier) throws Exception {
        List<Evenement> evenements = evenementService.recuperer();
        List<Participation> participations = participationService.recuperer();

        Workbook workbook = new XSSFWorkbook();

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Style pour les cellules de date
        CellStyle dateStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));

        CellStyle timeStyle = workbook.createCellStyle();
        timeStyle.setDataFormat(createHelper.createDataFormat().getFormat("hh:mm"));

        // ===== FEUILLE 1 : Événements =====
        Sheet sheet1 = workbook.createSheet("Événements");

        String[] eventHeaders = {"ID", "Titre", "Date", "Début", "Fin", "Lieu", "Places Max", "Inscrits", "Taux %", "Statut", "Description"};
        Row headerRow1 = sheet1.createRow(0);

        for (int i = 0; i < eventHeaders.length; i++) {
            Cell cell = headerRow1.createCell(i);
            cell.setCellValue(eventHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Evenement e : evenements) {
            Row row = sheet1.createRow(rowNum++);

            row.createCell(0).setCellValue(e.getIdEvenement());
            row.createCell(1).setCellValue(e.getTitre());

            // Date - format texte simple pour éviter l'erreur
            row.createCell(2).setCellValue(e.getDateEvenement() != null ? e.getDateEvenement().format(EXCEL_DATE_FORMAT) : "-");

            // Heures - format texte
            row.createCell(3).setCellValue(e.getHeureDebut() != null ? e.getHeureDebut().format(EXCEL_TIME_FORMAT) : "-");
            row.createCell(4).setCellValue(e.getHeureFin() != null ? e.getHeureFin().format(EXCEL_TIME_FORMAT) : "-");

            row.createCell(5).setCellValue(e.getLieu());
            row.createCell(6).setCellValue(e.getNombrePlacesMax());
            row.createCell(7).setCellValue(e.getNombreInscrits());

            double taux = e.getNombrePlacesMax() > 0 ?
                    (double) e.getNombreInscrits() / e.getNombrePlacesMax() * 100 : 0;
            row.createCell(8).setCellValue(String.format("%.1f%%", taux));

            row.createCell(9).setCellValue(e.getStatut());
            row.createCell(10).setCellValue(e.getDescription() != null ? e.getDescription() : "");
        }

        // ===== FEUILLE 2 : Participations =====
        Sheet sheet2 = workbook.createSheet("Participations");

        String[] partHeaders = {"ID Participation", "ID Événement", "Statut", "Présence", "Date Création"};
        Row headerRow2 = sheet2.createRow(0);

        for (int i = 0; i < partHeaders.length; i++) {
            Cell cell = headerRow2.createCell(i);
            cell.setCellValue(partHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        rowNum = 1;
        for (Participation p : participations) {
            Row row = sheet2.createRow(rowNum++);

            row.createCell(0).setCellValue(p.getId_p());
            row.createCell(1).setCellValue(p.getId_e());
            row.createCell(2).setCellValue(p.getStatut());
            row.createCell(3).setCellValue(p.isPresence() ? "Présent" : "Absent");

            // Date création - format texte
            row.createCell(4).setCellValue(p.getDateCreation() != null ?
                    p.getDateCreation().format(EXCEL_DATE_FORMAT) : "-");
        }

        // ===== FEUILLE 3 : Résumé =====
        Sheet sheet3 = workbook.createSheet("Résumé");

        String[][] resume = {
                {"Statistique", "Valeur"},
                {"Total événements", String.valueOf(evenements.size())},
                {"Total participations", String.valueOf(participations.size())},
                {"Événements à venir", String.valueOf(evenements.stream()
                        .filter(e -> e.getDateEvenement() != null && !e.getDateEvenement().isBefore(LocalDate.now())).count())},
                {"Taux de présence moyen", String.format("%.1f%%", calculerTauxPresenceMoyen())}
        };

        for (int i = 0; i < resume.length; i++) {
            Row row = sheet3.createRow(i);
            row.createCell(0).setCellValue(resume[i][0]);
            row.createCell(1).setCellValue(resume[i][1]);

            if (i == 0) {
                row.getCell(0).setCellStyle(headerStyle);
                row.getCell(1).setCellStyle(headerStyle);
            }
        }

        // Ajuster la largeur des colonnes
        for (int i = 0; i < sheet1.getRow(0).getLastCellNum(); i++) {
            sheet1.autoSizeColumn(i);
        }
        for (int i = 0; i < sheet2.getRow(0).getLastCellNum(); i++) {
            sheet2.autoSizeColumn(i);
        }
        for (int i = 0; i < 2; i++) {
            sheet3.autoSizeColumn(i);
        }

        // Sauvegarde
        try (FileOutputStream out = new FileOutputStream(cheminFichier)) {
            workbook.write(out);
        }
        workbook.close();

        System.out.println("✅ Excel créé: " + new java.io.File(cheminFichier).getAbsolutePath());
    }

    /**
     * EXPORT CSV - Export simple en CSV
     */
    public void exportEvenementsCSV(String cheminFichier) throws Exception {
        List<Evenement> evenements = evenementService.recuperer();

        try (PrintWriter writer = new PrintWriter(cheminFichier)) {
            writer.println("ID;Titre;Date;Lieu;Places;Inscrits;Statut");

            for (Evenement e : evenements) {
                writer.printf("%d;%s;%s;%s;%d;%d;%s%n",
                        e.getIdEvenement(),
                        e.getTitre().replace(";", ","),
                        e.getDateEvenement() != null ? e.getDateEvenement().format(DATE_FORMAT) : "",
                        e.getLieu().replace(";", ","),
                        e.getNombrePlacesMax(),
                        e.getNombreInscrits(),
                        e.getStatut()
                );
            }
        }

        System.out.println("✅ CSV créé: " + new java.io.File(cheminFichier).getAbsolutePath());
    }

    /**
     * EXPORT PDF - Détail d'un événement avec participants
     */
    public void exportDetailEvenementPDF(int idEvenement, String cheminFichier) throws Exception {
        Evenement event = evenementService.recupererParId(idEvenement);
        if (event == null) return;

        List<Participation> participations = participationService.recupererParEvenement(idEvenement);

        PdfWriter writer = new PdfWriter(cheminFichier);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("DÉTAIL DE L'ÉVÉNEMENT")
                .setBold()
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Généré le " + LocalDateTime.now().format(DATETIME_FORMAT))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));

        document.add(new Paragraph(" "));

        document.add(new Paragraph("Informations générales").setBold().setFontSize(14));

        document.add(new Paragraph("Titre : " + event.getTitre()));
        document.add(new Paragraph("Date : " + (event.getDateEvenement() != null ?
                event.getDateEvenement().format(DATE_FORMAT) : "-")));
        document.add(new Paragraph("Heure : " +
                (event.getHeureDebut() != null ? event.getHeureDebut().format(DateTimeFormatter.ofPattern("HH:mm")) : "-") + " - " +
                (event.getHeureFin() != null ? event.getHeureFin().format(DateTimeFormatter.ofPattern("HH:mm")) : "-")));
        document.add(new Paragraph("Lieu : " + event.getLieu()));
        document.add(new Paragraph("Places : " + event.getNombreInscrits() + " / " + event.getNombrePlacesMax()));
        document.add(new Paragraph("Statut : " + event.getStatut()));
        document.add(new Paragraph("Description : " + (event.getDescription() != null ? event.getDescription() : "Aucune")));

        document.add(new Paragraph(" "));

        if (!participations.isEmpty()) {
            document.add(new Paragraph("Participants (" + participations.size() + ")")
                    .setBold()
                    .setFontSize(14));

            Table table = new Table(UnitValue.createPercentArray(new float[]{10, 20, 25, 15, 30}));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell(new Paragraph("ID").setBold());
            table.addHeaderCell(new Paragraph("Statut").setBold());
            table.addHeaderCell(new Paragraph("Date inscription").setBold());
            table.addHeaderCell(new Paragraph("Présence").setBold());
            table.addHeaderCell(new Paragraph("Description").setBold());

            for (Participation p : participations) {
                table.addCell(String.valueOf(p.getId_p()));
                table.addCell(p.getStatut());
                table.addCell(p.getDateCreation() != null ?
                        p.getDateCreation().format(DATETIME_FORMAT) : "-");
                table.addCell(p.isPresence() ? "Présent" : "Absent");
                table.addCell("-");
            }

            document.add(table);
        } else {
            document.add(new Paragraph("Aucun participant pour cet événement."));
        }

        document.close();

        System.out.println("✅ PDF détail créé: " + new java.io.File(cheminFichier).getAbsolutePath());
    }

    private double calculerTauxPresenceMoyen() throws SQLException {
        List<Participation> toutes = participationService.recuperer();
        if (toutes.isEmpty()) return 0;

        long presents = toutes.stream().filter(Participation::isPresence).count();
        return (double) presents / toutes.size() * 100;
    }
}