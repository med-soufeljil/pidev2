package services;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public final class OfferResponseHttpServer {

    private static volatile boolean started = false;

    private OfferResponseHttpServer() {
    }

    public static synchronized void ensureStarted() {
        if (started) return;
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8090), 0);
            server.createContext("/offer/accept", ex -> handle(ex, RecruitmentWorkflowService.STATUS_ACCEPTEE));
            server.createContext("/offer/reject", ex -> handle(ex, RecruitmentWorkflowService.STATUS_REJETEE));
            server.setExecutor(null);
            server.start();
            started = true;
        } catch (IOException ignored) {
        }
    }

    private static void handle(HttpExchange exchange, String response) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String token = extractToken(query);
        String body;
        try {
            if (token == null || token.isBlank()) {
                body = "Token manquant.";
            } else {
                RecruitmentWorkflowService workflowService = new RecruitmentWorkflowService();
                workflowService.recordOfferResponse(token, response);
                body = "Merci, votre réponse a été enregistrée avec succès: " + response;
            }
        } catch (Exception e) {
            body = "Erreur: " + e.getMessage();
        }

        byte[] bytes = ("<html><body style='font-family:Arial;padding:30px'><h2>RecruitFlow</h2><p>" + body + "</p></body></html>")
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String extractToken(String query) {
        if (query == null) return null;
        String[] parts = query.split("&");
        for (String part : parts) {
            if (part.startsWith("token=")) {
                return part.substring("token=".length());
            }
        }
        return null;
    }
}
