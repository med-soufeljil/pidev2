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
    private static final String REQUIRED_SENDER_DOMAIN = "test-3m5jgroevrxgdpyo.mlsender.net";
    private static final String DEFAULT_FROM_EMAIL = "hr@" + REQUIRED_SENDER_DOMAIN;
    private static final String DEFAULT_FROM_NAME = "RecruitFlow RH";

    private final HttpClient client = HttpClient.newHttpClient();

    public void sendOfferEmail(String toEmail, String toName, int salary, String acceptUrl, String rejectUrl)
            throws IOException, InterruptedException {

        String apiKey = resolveApiKey();
        String fromEmail = resolveFromEmail();
        String fromName = resolveFromName();

        JsonObject body = new JsonObject();

        JsonObject from = new JsonObject();
        from.addProperty("email", fromEmail);
        from.addProperty("name", fromName);
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
            throw buildEmailApiException(response.statusCode(), response.body(), fromEmail);
        }
    }

    private IllegalStateException buildEmailApiException(int statusCode, String responseBody, String fromEmail) {
        if (statusCode == 422 && responseBody != null && responseBody.toLowerCase().contains("domain must be verified")) {
            return new IllegalStateException(
                    "MailerSend rejected the sender address (422): domain/email not verified for from='" + fromEmail + "'. "
                            + "Verify the sender domain in MailerSend, create a verified sender email, then configure "
                            + "MAILERSEND_FROM_EMAIL (or -Dmailersend.from.email). The sender domain must be " + REQUIRED_SENDER_DOMAIN + " and optionally MAILERSEND_FROM_NAME "
                            + "(or -Dmailersend.from.name). Raw API response: " + responseBody
            );
        }

        return new IllegalStateException("Email API error " + statusCode + ": " + responseBody);
    }

    private String resolveApiKey() {
        String value = resolveConfig("MAILERSEND_API_KEY", "mailersend.api.key", null);
        if (value != null) {
            return value;
        }

        throw new IllegalStateException(
                "MailerSend API key is missing. Set MAILERSEND_API_KEY env var OR JVM property -Dmailersend.api.key=..."
        );
    }

    private String resolveFromEmail() {
        String configured = resolveConfig("MAILERSEND_FROM_EMAIL", "mailersend.from.email", DEFAULT_FROM_EMAIL);

        if (configured == null || configured.isBlank()) {
            return DEFAULT_FROM_EMAIL;
        }

        String trimmed = configured.trim().toLowerCase();
        if (trimmed.endsWith("@" + REQUIRED_SENDER_DOMAIN)) {
            return trimmed;
        }

        String localPart = trimmed.contains("@") ? trimmed.substring(0, trimmed.indexOf("@")) : trimmed;
        if (localPart.isBlank()) {
            localPart = "hr";
        }

        return localPart + "@" + REQUIRED_SENDER_DOMAIN;
    }

    private String resolveFromName() {
        return resolveConfig("MAILERSEND_FROM_NAME", "mailersend.from.name", DEFAULT_FROM_NAME);
    }

    private String resolveConfig(String envKey, String systemPropertyKey, String defaultValue) {
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        String propertyValue = System.getProperty(systemPropertyKey);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }

        return defaultValue;
    }
}
