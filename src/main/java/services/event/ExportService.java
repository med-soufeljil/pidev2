package services.event;

import models.event.Evenement;
import models.event.Participation;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExportService {

    private final EvenementService evenementService = new EvenementService();
    private final ParticipationService participationService = new ParticipationService() {
        @Override
        public void supprimer(int id) throws SQLException {

        }
    };
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    // ==================== MÉTIER AVANCÉ 1 : EXPORT PDF STANDARD ====================

    public void exportEvenementsPDF(String cheminFichier) throws Exception {
        List<Evenement> evenements = evenementService.recuperer();

        PdfWriter writer = new PdfWriter(cheminFichier);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(20, 20, 20, 20);

        document.add(new Paragraph("LISTE DES ÉVÉNEMENTS")
                .setBold()
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Généré le " + LocalDateTime.now().format(DATETIME_FORMAT))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Résumé : " + evenements.size() + " événements au total")
                .setFontSize(12)
                .setBold());

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
            table.addCell(e.getHeureDebut() != null ? e.getHeureDebut().format(TIME_FORMAT) : "-");
            table.addCell(e.getHeureFin() != null ? e.getHeureFin().format(TIME_FORMAT) : "-");
            table.addCell(e.getLieu());
            table.addCell(String.valueOf(e.getNombrePlacesMax()));
            table.addCell(String.valueOf(e.getNombreInscrits()));
            table.addCell(e.getStatut());
        }

        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Document généré automatiquement")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER));

        document.close();
        System.out.println("✅ PDF standard créé: " + cheminFichier);
    }

    // ==================== MÉTIER AVANCÉ 2 : EXPORT PDF STATISTIQUES ====================

    public void exportStatistiquesPDF(String cheminFichier) throws Exception {
        List<Evenement> evenements = evenementService.recuperer();
        List<Participation> participations = participationService.recuperer();

        PdfWriter writer = new PdfWriter(cheminFichier);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(20, 20, 20, 20);

        // ===== PAGE DE GARDE =====
        document.add(new Paragraph("RAPPORT STATISTIQUE")
                .setBold()
                .setFontSize(24)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Gestion des Événements")
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Généré le : " + LocalDateTime.now().format(DATETIME_FORMAT))
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Par : " + System.getProperty("user.name"))
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // ===== PAGE 1 : KPIs =====
        document.add(new Paragraph("INDICATEURS CLÉS")
                .setBold()
                .setFontSize(18)
                .setFontColor(ColorConstants.BLUE));

        document.add(new Paragraph(" "));

        int totalEvents = evenements.size();
        int totalParticipations = participations.size();
        double tauxRemplissageMoyen = calculerTauxRemplissageMoyen(evenements);
        long eventsFuturs = evenements.stream()
                .filter(e -> e.getDateEvenement() != null && !e.getDateEvenement().isBefore(LocalDate.now()))
                .count();
        long eventsPasses = totalEvents - eventsFuturs;

        Table kpiTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        kpiTable.setWidth(UnitValue.createPercentValue(80));
        kpiTable.setHorizontalAlignment(HorizontalAlignment.CENTER);

        kpiTable.addCell(createCell("📊 Total événements", true));
        kpiTable.addCell(createCell(String.valueOf(totalEvents), false));
        kpiTable.addCell(createCell("👥 Total participations", true));
        kpiTable.addCell(createCell(String.valueOf(totalParticipations), false));
        kpiTable.addCell(createCell("📈 Taux remplissage moyen", true));
        kpiTable.addCell(createCell(String.format("%.1f%%", tauxRemplissageMoyen), false));
        kpiTable.addCell(createCell("📅 Événements à venir", true));
        kpiTable.addCell(createCell(String.valueOf(eventsFuturs), false));
        kpiTable.addCell(createCell("✅ Événements passés", true));
        kpiTable.addCell(createCell(String.valueOf(eventsPasses), false));

        document.add(kpiTable);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // ===== PAGE 2 : TOP ÉVÉNEMENTS =====
        document.add(new Paragraph("TOP 5 DES ÉVÉNEMENTS LES PLUS POPULAIRES")
                .setBold()
                .setFontSize(18)
                .setFontColor(ColorConstants.ORANGE));

        document.add(new Paragraph(" "));

        var topEvents = getTopEvenements(evenements, participations);

        Table topTable = new Table(UnitValue.createPercentArray(new float[]{10, 40, 20, 30}));
        topTable.setWidth(UnitValue.createPercentValue(100));

        topTable.addHeaderCell(createCell("Rang", true));
        topTable.addHeaderCell(createCell("Événement", true));
        topTable.addHeaderCell(createCell("Participants", true));
        topTable.addHeaderCell(createCell("Taux remplissage", true));

        int rang = 1;
        for (var entry : topEvents) {
            Evenement e = entry.getKey();
            Long nbParticipants = entry.getValue();
            double taux = e.getNombrePlacesMax() > 0 ?
                    (double) nbParticipants / e.getNombrePlacesMax() * 100 : 0;

            topTable.addCell(createCell("#" + rang++, false));
            topTable.addCell(createCell(e.getTitre(), false));
            topTable.addCell(createCell(String.valueOf(nbParticipants), false));
            topTable.addCell(createCell(String.format("%.1f%%", taux), false));
        }

        document.add(topTable);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // ===== PAGE 3 : RÉPARTITION PAR STATUT =====
        document.add(new Paragraph("RÉPARTITION DES ÉVÉNEMENTS PAR STATUT")
                .setBold()
                .setFontSize(18)
                .setFontColor(ColorConstants.GREEN));

        document.add(new Paragraph(" "));

        long planifies = evenements.stream().filter(e -> "planifie".equals(e.getStatut())).count();
        long enCours = evenements.stream().filter(e -> "en cours".equals(e.getStatut())).count();
        long termines = evenements.stream().filter(e -> "termine".equals(e.getStatut())).count();

        Table statutTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        statutTable.setWidth(UnitValue.createPercentValue(60));

        statutTable.addCell(createCell("📅 Planifiés", true));
        statutTable.addCell(createCell(String.valueOf(planifies), false));
        statutTable.addCell(createCell("🔴 En cours", true));
        statutTable.addCell(createCell(String.valueOf(enCours), false));
        statutTable.addCell(createCell("✅ Terminés", true));
        statutTable.addCell(createCell(String.valueOf(termines), false));

        document.add(statutTable);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // ===== PAGE 4 : LISTE DÉTAILLÉE =====
        document.add(new Paragraph("LISTE DÉTAILLÉE DES ÉVÉNEMENTS")
                .setBold()
                .setFontSize(18));

        document.add(new Paragraph(" "));

        Table detailTable = new Table(UnitValue.createPercentArray(new float[]{10, 25, 15, 15, 15, 20}));
        detailTable.setWidth(UnitValue.createPercentValue(100));

        String[] detailHeaders = {"ID", "Titre", "Date", "Lieu", "Places", "Statut"};
        for (String header : detailHeaders) {
            detailTable.addHeaderCell(createCell(header, true));
        }

        for (Evenement e : evenements) {
            detailTable.addCell(createCell(String.valueOf(e.getIdEvenement()), false));
            detailTable.addCell(createCell(e.getTitre(), false));
            detailTable.addCell(createCell(e.getDateEvenement() != null ? e.getDateEvenement().format(DATE_FORMAT) : "-", false));
            detailTable.addCell(createCell(e.getLieu(), false));
            detailTable.addCell(createCell(e.getNombreInscrits() + "/" + e.getNombrePlacesMax(), false));
            detailTable.addCell(createCell(e.getStatut(), false));
        }

        document.add(detailTable);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Rapport généré le " + LocalDateTime.now().format(DATETIME_FORMAT))
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Page " + pdf.getPageNumber(pdf.getLastPage()) + " sur " + pdf.getNumberOfPages())
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER));

        document.close();
        System.out.println("✅ PDF statistiques avancé créé: " + cheminFichier);
    }

    // ==================== MÉTIER AVANCÉ 3 : EXPORT PDF FILTRÉ ====================

    public void exportEvenementsFiltresPDF(String cheminFichier, String statut) throws Exception {
        List<Evenement> evenements = evenementService.recuperer().stream()
                .filter(e -> statut != null && statut.equals(e.getStatut()))
                .collect(Collectors.toList());

        System.out.println("📊 Export filtré - Statut: '" + statut + "', Nombre: " + evenements.size());

        if (evenements.isEmpty()) {
            throw new Exception("Aucun événement avec le statut '" + statut + "' n'a été trouvé.");
        }

        PdfWriter writer = new PdfWriter(cheminFichier);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);

        document.add(new Paragraph("ÉVÉNEMENTS " + statut.toUpperCase())
                .setBold()
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Généré le " + LocalDateTime.now().format(DATETIME_FORMAT))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Résumé : " + evenements.size() + " événements " + statut)
                .setFontSize(12));

        document.add(new Paragraph(" "));

        Table table = new Table(UnitValue.createPercentArray(new float[]{15, 25, 15, 15, 15, 15}));
        table.setWidth(UnitValue.createPercentValue(100));

        String[] headers = {"ID", "Titre", "Date", "Lieu", "Places", "Statut"};
        for (String header : headers) {
            table.addHeaderCell(new Paragraph(header).setBold());
        }

        for (Evenement e : evenements) {
            table.addCell(String.valueOf(e.getIdEvenement()));
            table.addCell(e.getTitre());
            table.addCell(e.getDateEvenement() != null ? e.getDateEvenement().format(DATE_FORMAT) : "-");
            table.addCell(e.getLieu());
            table.addCell(e.getNombreInscrits() + "/" + e.getNombrePlacesMax());
            table.addCell(e.getStatut());
        }

        document.add(table);
        document.close();

        System.out.println("✅ PDF filtré créé: " + cheminFichier);
    }

    // ==================== MÉTIER AVANCÉ 4 : EXPORT EXCEL AVANCÉ ====================

    public void exportStatistiquesExcel(String cheminFichier) throws Exception {
        List<Evenement> evenements = evenementService.recuperer();
        List<Participation> participations = participationService.recuperer();

        Workbook workbook = new XSSFWorkbook();

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        // ===== FEUILLE 1 : RÉSUMÉ =====
        Sheet sheetResume = workbook.createSheet("Résumé");
        createResumeSheet(sheetResume, headerStyle, titleStyle, dateStyle, evenements, participations);

        // ===== FEUILLE 2 : ÉVÉNEMENTS =====
        Sheet sheetEvents = workbook.createSheet("Événements");
        createEventsSheet(sheetEvents, headerStyle, dateStyle, evenements);

        // ===== FEUILLE 3 : PARTICIPATIONS =====
        Sheet sheetParts = workbook.createSheet("Participations");
        createParticipationsSheet(sheetParts, headerStyle, dateStyle, participations);

        // ===== FEUILLE 4 : STATISTIQUES AVANCÉES =====
        Sheet sheetStats = workbook.createSheet("Statistiques avancées");
        createStatsSheet(sheetStats, headerStyle, titleStyle, evenements, participations);

        for (int i = 0; i < 4; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet.getRow(0) != null) {
                for (int j = 0; j < sheet.getRow(0).getLastCellNum(); j++) {
                    sheet.autoSizeColumn(j);
                }
            }
        }

        try (FileOutputStream out = new FileOutputStream(cheminFichier)) {
            workbook.write(out);
        }
        workbook.close();

        System.out.println("✅ Excel avancé créé: " + cheminFichier);
    }

    // ==================== MÉTHODES UTILITAIRES POUR PDF ====================

    private Cell createCell(String text, boolean isHeader) {
        Cell cell = new Cell().add(new Paragraph(text));
        if (isHeader) {
            cell.setBold();
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        }
        return cell;
    }

    private double calculerTauxRemplissageMoyen(List<Evenement> evenements) {
        return evenements.stream()
                .filter(e -> e.getNombrePlacesMax() > 0)
                .mapToDouble(e -> (double) e.getNombreInscrits() / e.getNombrePlacesMax() * 100)
                .average()
                .orElse(0);
    }

    private List<Map.Entry<Evenement, Long>> getTopEvenements(
            List<Evenement> evenements, List<Participation> participations) {

        Map<Integer, Long> participationsCount = participations.stream()
                .collect(Collectors.groupingBy(
                        Participation::getId_e,
                        Collectors.counting()
                ));

        return evenements.stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e,
                        participationsCount.getOrDefault(e.getIdEvenement(), 0L)))
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .collect(Collectors.toList());
    }

    // ==================== MÉTHODES UTILITAIRES POUR EXCEL ====================

    private org.apache.poi.ss.usermodel.CellStyle createHeaderStyle(Workbook workbook) {
        org.apache.poi.ss.usermodel.CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private org.apache.poi.ss.usermodel.CellStyle createTitleStyle(Workbook workbook) {
        org.apache.poi.ss.usermodel.CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private org.apache.poi.ss.usermodel.CellStyle createDateStyle(Workbook workbook) {
        org.apache.poi.ss.usermodel.CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
        return style;
    }

    private void createResumeSheet(Sheet sheet, org.apache.poi.ss.usermodel.CellStyle headerStyle,
                                   org.apache.poi.ss.usermodel.CellStyle titleStyle,
                                   org.apache.poi.ss.usermodel.CellStyle dateStyle,
                                   List<Evenement> evenements, List<Participation> participations) {
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("RAPPORT STATISTIQUE - GESTION D'ÉVÉNEMENTS");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        rowNum++;

        Row kpi1Row = sheet.createRow(rowNum++);
        kpi1Row.createCell(0).setCellValue("Total événements:");
        kpi1Row.createCell(1).setCellValue(evenements.size());

        Row kpi2Row = sheet.createRow(rowNum++);
        kpi2Row.createCell(0).setCellValue("Total participations:");
        kpi2Row.createCell(1).setCellValue(participations.size());

        double tauxMoyen = calculerTauxRemplissageMoyen(evenements);
        Row kpi3Row = sheet.createRow(rowNum++);
        kpi3Row.createCell(0).setCellValue("Taux remplissage moyen:");
        kpi3Row.createCell(1).setCellValue(String.format("%.1f%%", tauxMoyen));

        long eventsFuturs = evenements.stream()
                .filter(e -> e.getDateEvenement() != null && !e.getDateEvenement().isBefore(LocalDate.now()))
                .count();
        Row kpi4Row = sheet.createRow(rowNum++);
        kpi4Row.createCell(0).setCellValue("Événements à venir:");
        kpi4Row.createCell(1).setCellValue(eventsFuturs);

        rowNum += 2;

        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("Rapport généré le:");
        org.apache.poi.ss.usermodel.Cell dateCell = dateRow.createCell(1);
        dateCell.setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dateCell.setCellStyle(dateStyle);
    }

    private void createEventsSheet(Sheet sheet, org.apache.poi.ss.usermodel.CellStyle headerStyle,
                                   org.apache.poi.ss.usermodel.CellStyle dateStyle,
                                   List<Evenement> evenements) {
        String[] headers = {"ID", "Titre", "Date", "Heure début", "Heure fin", "Lieu",
                "Places max", "Inscrits", "Taux %", "Statut", "Description"};

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Evenement e : evenements) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(e.getIdEvenement());
            row.createCell(1).setCellValue(e.getTitre());
            row.createCell(2).setCellValue(e.getDateEvenement() != null ? e.getDateEvenement().format(DATE_FORMAT) : "-");
            row.createCell(3).setCellValue(e.getHeureDebut() != null ? e.getHeureDebut().format(TIME_FORMAT) : "-");
            row.createCell(4).setCellValue(e.getHeureFin() != null ? e.getHeureFin().format(TIME_FORMAT) : "-");
            row.createCell(5).setCellValue(e.getLieu());
            row.createCell(6).setCellValue(e.getNombrePlacesMax());
            row.createCell(7).setCellValue(e.getNombreInscrits());

            double taux = e.getNombrePlacesMax() > 0 ?
                    (double) e.getNombreInscrits() / e.getNombrePlacesMax() * 100 : 0;
            row.createCell(8).setCellValue(String.format("%.1f%%", taux));

            row.createCell(9).setCellValue(e.getStatut());
            row.createCell(10).setCellValue(e.getDescription() != null ? e.getDescription() : "");
        }
    }

    private void createParticipationsSheet(Sheet sheet, org.apache.poi.ss.usermodel.CellStyle headerStyle,
                                           org.apache.poi.ss.usermodel.CellStyle dateStyle,
                                           List<Participation> participations) {
        String[] headers = {"ID Participation", "ID Événement", "Date inscription", "Statut", "Présence", "Date création"};

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Participation p : participations) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(p.getId_p());
            row.createCell(1).setCellValue(p.getId_e());
            row.createCell(2).setCellValue(p.getDateInscription() != null ? p.getDateInscription().format(DATE_FORMAT) : "-");
            row.createCell(3).setCellValue(p.getStatut());
            row.createCell(4).setCellValue(p.isPresence() ? "Présent" : "Absent");
            row.createCell(5).setCellValue(p.getDateCreation() != null ? p.getDateCreation().format(DATETIME_FORMAT) : "-");
        }
    }

    private void createStatsSheet(Sheet sheet, org.apache.poi.ss.usermodel.CellStyle headerStyle,
                                  org.apache.poi.ss.usermodel.CellStyle titleStyle,
                                  List<Evenement> evenements, List<Participation> participations) {
        int rowNum = 0;

        Row topTitle = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell topTitleCell = topTitle.createCell(0);
        topTitleCell.setCellValue("TOP 5 DES ÉVÉNEMENTS LES PLUS POPULAIRES");
        topTitleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        rowNum++;

        String[] topHeaders = {"Rang", "Événement", "Participants", "Taux remplissage"};
        Row topHeaderRow = sheet.createRow(rowNum++);
        for (int i = 0; i < topHeaders.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = topHeaderRow.createCell(i);
            cell.setCellValue(topHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        var topEvents = getTopEvenements(evenements, participations);
        int rang = 1;
        for (var entry : topEvents) {
            Evenement e = entry.getKey();
            Long nbParticipants = entry.getValue();
            double taux = e.getNombrePlacesMax() > 0 ?
                    (double) nbParticipants / e.getNombrePlacesMax() * 100 : 0;

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("#" + rang++);
            row.createCell(1).setCellValue(e.getTitre());
            row.createCell(2).setCellValue(nbParticipants);
            row.createCell(3).setCellValue(String.format("%.1f%%", taux));
        }

        rowNum += 2;

        Row statutTitle = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell statutTitleCell = statutTitle.createCell(0);
        statutTitleCell.setCellValue("RÉPARTITION PAR STATUT");
        statutTitleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 1));

        rowNum++;

        long planifies = evenements.stream().filter(e -> "planifie".equals(e.getStatut())).count();
        long enCours = evenements.stream().filter(e -> "en cours".equals(e.getStatut())).count();
        long termines = evenements.stream().filter(e -> "termine".equals(e.getStatut())).count();

        Row statutRow1 = sheet.createRow(rowNum++);
        statutRow1.createCell(0).setCellValue("Planifiés:");
        statutRow1.createCell(1).setCellValue(planifies);

        Row statutRow2 = sheet.createRow(rowNum++);
        statutRow2.createCell(0).setCellValue("En cours:");
        statutRow2.createCell(1).setCellValue(enCours);

        Row statutRow3 = sheet.createRow(rowNum++);
        statutRow3.createCell(0).setCellValue("Terminés:");
        statutRow3.createCell(1).setCellValue(termines);
    }

    // ==================== MÉTHODES EXISTANTES ====================

    public void exportEvenementsCSV(String cheminFichier) throws Exception {
        List<Evenement> evenements = evenementService.recuperer();
        try (java.io.PrintWriter writer = new java.io.PrintWriter(cheminFichier)) {
            writer.println("ID;Titre;Date;Lieu;Places;Inscrits;Statut");
            for (Evenement e : evenements) {
                writer.printf("%d;%s;%s;%s;%d;%d;%s%n",
                        e.getIdEvenement(),
                        e.getTitre().replace(";", ","),
                        e.getDateEvenement() != null ? e.getDateEvenement().format(DATE_FORMAT) : "",
                        e.getLieu().replace(";", ","),
                        e.getNombrePlacesMax(),
                        e.getNombreInscrits(),
                        e.getStatut());
            }
        }
        System.out.println("✅ CSV créé: " + cheminFichier);
    }

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
                (event.getHeureDebut() != null ? event.getHeureDebut().format(TIME_FORMAT) : "-") + " - " +
                (event.getHeureFin() != null ? event.getHeureFin().format(TIME_FORMAT) : "-")));
        document.add(new Paragraph("Lieu : " + event.getLieu()));
        document.add(new Paragraph("Places : " + event.getNombreInscrits() + " / " + event.getNombrePlacesMax()));
        document.add(new Paragraph("Statut : " + event.getStatut()));

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
        System.out.println("✅ PDF détail créé: " + cheminFichier);
    }
}