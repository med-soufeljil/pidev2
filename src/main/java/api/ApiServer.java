package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dto.DashboardStats;
import entities.Apprenant;
import entities.Formation;
import entities.FormationFeedback;
import services.ApprenantService;
import services.DashboardService;
import services.FeedbackService;
import services.FormationService;
import utils.SimplePdfExporter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;

public class ApiServer {

    private HttpServer server;

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/formations", this::handleFormations);
        server.createContext("/api/apprenants", this::handleApprenants);
        server.createContext("/api/dashboard", this::handleDashboard);
        server.createContext("/api/dashboard/pdf", this::handleDashboardPdf);
        server.createContext("/api/feedbacks", this::handleFeedbacks);
        server.setExecutor(Executors.newFixedThreadPool(6));
        server.start();
        System.out.println("API server started on http://localhost:" + port);
    }

    private void handleFormations(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        try {
            Map<String, String> query = parseQuery(exchange.getRequestURI());
            String q = query.getOrDefault("q", "").toLowerCase();
            String sortBy = query.getOrDefault("sortBy", "titre");
            String order = query.getOrDefault("order", "asc");

            List<Formation> formations = new FormationService().recuperer();
            formations.removeIf(f -> !q.isEmpty() && !(f.getTitre().toLowerCase().contains(q)
                    || f.getDescription().toLowerCase().contains(q)
                    || f.getNiveau().name().toLowerCase().contains(q)
                    || f.getCategorie().name().toLowerCase().contains(q)));

            Comparator<Formation> comparator = switch (sortBy.toLowerCase()) {
                case "duree" -> Comparator.comparingInt(Formation::getDuree);
                case "niveau" -> Comparator.comparing(f -> f.getNiveau().name());
                default -> Comparator.comparing(Formation::getTitre, String.CASE_INSENSITIVE_ORDER);
            };
            if ("desc".equalsIgnoreCase(order)) comparator = comparator.reversed();
            formations.sort(comparator);

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < formations.size(); i++) {
                Formation f = formations.get(i);
                json.append(String.format("{\"id\":%d,\"titre\":\"%s\",\"description\":\"%s\",\"duree\":%d,\"niveau\":\"%s\",\"categorie\":\"%s\",\"certification\":%s}",
                        f.getId_formation(), escape(f.getTitre()), escape(f.getDescription()), f.getDuree(), f.getNiveau(), f.getCategorie(), f.isCertification()));
                if (i < formations.size() - 1) json.append(',');
            }
            json.append(']');
            sendJson(exchange, 200, json.toString());
        } catch (SQLException e) {
            sendJson(exchange, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private void handleApprenants(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        try {
            List<Apprenant> apprenants = new ApprenantService().recuperer();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < apprenants.size(); i++) {
                Apprenant a = apprenants.get(i);
                json.append(String.format("{\"id\":%d,\"nom\":\"%s\",\"prenom\":\"%s\",\"email\":\"%s\",\"statut\":\"%s\",\"formationId\":%d}",
                        a.getIdApprenant(), escape(a.getNom()), escape(a.getPrenom()), escape(a.getEmail()), escape(a.getStatut()), a.getId_formation()));
                if (i < apprenants.size() - 1) json.append(',');
            }
            json.append(']');
            sendJson(exchange, 200, json.toString());
        } catch (SQLException e) {
            sendJson(exchange, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private void handleDashboard(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        try {
            DashboardStats stats = new DashboardService().loadStats();
            String json = String.format("{\"totalFormations\":%d,\"totalApprenants\":%d,\"averageDuration\":%.2f,\"certifiedFormations\":%d}",
                    stats.getTotalFormations(), stats.getTotalApprenants(), stats.getAverageDuration(), stats.getCertifiedFormations());
            sendJson(exchange, 200, json);
        } catch (SQLException e) {
            sendJson(exchange, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private void handleDashboardPdf(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        try {
            DashboardStats stats = new DashboardService().loadStats();
            Path tmp = Files.createTempFile("dashboard-report", ".pdf");
            List<String> lines = List.of(
                    "Total formations: " + stats.getTotalFormations(),
                    "Total apprenants: " + stats.getTotalApprenants(),
                    String.format("Duree moyenne: %.2f h", stats.getAverageDuration()),
                    "Formations certifiees: " + stats.getCertifiedFormations()
            );
            SimplePdfExporter.writeSimpleReport(tmp, "Rapport Dashboard", lines);
            byte[] pdf = Files.readAllBytes(tmp);
            Files.deleteIfExists(tmp);
            exchange.getResponseHeaders().add("Content-Type", "application/pdf");
            exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=dashboard-report.pdf");
            exchange.sendResponseHeaders(200, pdf.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(pdf);
            }
        } catch (SQLException e) {
            sendJson(exchange, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private void handleFeedbacks(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        try {
            Map<String, String> query = parseQuery(exchange.getRequestURI());
            int formationId = Integer.parseInt(query.getOrDefault("formationId", "0"));
            if (formationId <= 0) {
                sendJson(exchange, 400, "{\"error\":\"formationId is required\"}");
                return;
            }
            FeedbackService feedbackService = new FeedbackService();
            List<FormationFeedback> list = feedbackService.getByFormation(formationId);
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                FormationFeedback fb = list.get(i);
                json.append(String.format("{\"id\":%d,\"author\":\"%s\",\"rating\":%d,\"comment\":\"%s\",\"createdAt\":\"%s\"}",
                        fb.getId(), escape(fb.getAuthor()), fb.getRating(), escape(fb.getComment()), fb.getCreatedAt() == null ? "" : fb.getCreatedAt()));
                if (i < list.size() - 1) json.append(',');
            }
            json.append(']');
            sendJson(exchange, 200, json.toString());
        } catch (Exception e) {
            sendJson(exchange, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private Map<String, String> parseQuery(URI uri) {
        Map<String, String> map = new HashMap<>();
        String query = uri.getRawQuery();
        if (query == null || query.isBlank()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String v = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(k, v);
        }
        return map;
    }

    private void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}
