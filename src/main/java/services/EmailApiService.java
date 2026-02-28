package services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class EmailApiService {

    private static final String MAILERSEND_ENDPOINT = "https://api.mailersend.com/v1/email";
    private static final String FROM_EMAIL = "hr@recruitflow.app";
    private static final String FROM_NAME = "RecruitFlow RH";

    private final HttpClient client = HttpClient.newHttpClient();

    public void sendOfferEmail(String toEmail, String toName, int salary, String acceptUrl, String rejectUrl)
            throws IOException, InterruptedException {

        String apiKey = resolveApiKey();

        JsonObject body = new JsonObject();

        JsonObject from = new JsonObject();
        from.addProperty("email", FROM_EMAIL);
        from.addProperty("name", FROM_NAME);
        body.add("from", from);

        JsonArray to = new JsonArray();
        JsonObject recipient = new JsonObject();
        recipient.addProperty("email", toEmail);
        recipient.addProperty("name", toName);
        to.add(recipient);
        body.add("to", to);

        body.addProperty("subject", "Offre d'emploi - RecruitFlow");

        String html = """
                <html><body style='font-family:Arial,sans-serif'>
                  <h2>Offre d'emploi</h2>
                  <p>Bonjour %s,</p>
                  <p>Nous vous proposons un poste avec un salaire mensuel de <b>%d</b>.</p>
                  <p>Merci de répondre via les boutons ci-dessous :</p>
                  <p>
                    <a href='%s' style='background:#16a34a;color:white;padding:10px 16px;border-radius:8px;text-decoration:none;'>Accepter</a>
                    &nbsp;
                    <a href='%s' style='background:#dc2626;color:white;padding:10px 16px;border-radius:8px;text-decoration:none;'>Rejeter</a>
                  </p>
                  <p>Cordialement,<br/>Equipe RH</p>
                </body></html>
                """.formatted(toName, salary, acceptUrl, rejectUrl);

        body.addProperty("html", html);
        body.addProperty("text", "Offre salaire: " + salary + " | Accepter: " + acceptUrl + " | Rejeter: " + rejectUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MAILERSEND_ENDPOINT))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new IllegalStateException("Email API error " + response.statusCode() + ": " + response.body());
        }
    }

    private String resolveApiKey() {
        String env = System.getenv("MAILERSEND_API_KEY");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }

        String property = System.getProperty("mailersend.api.key");
        if (property != null && !property.isBlank()) {
            return property.trim();
        }

        throw new IllegalStateException(
                "MailerSend API key is missing. Set MAILERSEND_API_KEY env var OR JVM property -Dmailersend.api.key=..."
        );
    }
}