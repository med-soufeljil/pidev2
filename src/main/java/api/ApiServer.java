package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dto.DashboardStats;
import entities.Apprenant;
import entities.Formation;
import services.ApprenantService;
import services.DashboardService;
import services.FormationService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;

public class ApiServer {

    private HttpServer server;

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/formations", this::handleFormations);
        server.createContext("/api/apprenants", this::handleApprenants);
        server.createContext("/api/dashboard", this::handleDashboard);
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("API server started on http://localhost:" + port);
    }

    private void handleFormations(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            send(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        try {
            List<Formation> formations = new FormationService().recuperer();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < formations.size(); i++) {
                Formation f = formations.get(i);
                json.append(String.format("{\"id\":%d,\"titre\":\"%s\",\"duree\":%d,\"niveau\":\"%s\",\"categorie\":\"%s\",\"certification\":%s}",
                        f.getId_formation(), escape(f.getTitre()), f.getDuree(), f.getNiveau(), f.getCategorie(), f.isCertification()));
                if (i < formations.size() - 1) json.append(',');
            }
            json.append(']');
            send(exchange, 200, json.toString());
        } catch (SQLException e) {
            send(exchange, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private void handleApprenants(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            send(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        try {
            List<Apprenant> apprenants = new ApprenantService().recuperer();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < apprenants.size(); i++) {
                Apprenant a = apprenants.get(i);
                json.append(String.format("{\"id\":%d,\"nom\":\"%s\",\"prenom\":\"%s\",\"email\":\"%s\",\"formationId\":%d}",
                        a.getIdApprenant(), escape(a.getNom()), escape(a.getPrenom()), escape(a.getEmail()), a.getId_formation()));
                if (i < apprenants.size() - 1) json.append(',');
            }
            json.append(']');
            send(exchange, 200, json.toString());
        } catch (SQLException e) {
            send(exchange, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private void handleDashboard(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            send(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        try {
            DashboardStats stats = new DashboardService().loadStats();
            String json = String.format("{\"totalFormations\":%d,\"totalApprenants\":%d,\"averageDuration\":%.2f,\"certifiedFormations\":%d}",
                    stats.getTotalFormations(), stats.getTotalApprenants(), stats.getAverageDuration(), stats.getCertifiedFormations());
            send(exchange, 200, json);
        } catch (SQLException e) {
            send(exchange, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private void send(HttpExchange exchange, int status, String body) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
