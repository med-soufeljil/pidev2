package services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class MailingApiService {

    private String lastError = "";
    private static final Properties FILE_CONFIG = loadFileConfig();

    /**
     * Real email sending through MailerSend API.
     * Credential sources (priority):
     * 1) Environment variables
     * 2) JVM system properties
     * 3) local file ./mailing.properties
     */
    public boolean sendRegistrationEmail(String email, String fullName, String formationTitle) {
        String apiKey = pick(
                envOrNull("MAILERSEND_API_KEY"),
                envOrNull("MAILSENDER_API_KEY"),
                System.getProperty("mailersend.api.key"),
                FILE_CONFIG.getProperty("mailersend.api.key")
        );

        String fromEmail = pick(
                envOrNull("MAILERSEND_FROM_EMAIL"),
                envOrNull("MAILSENDER_FROM_EMAIL"),
                System.getProperty("mailersend.from.email"),
                FILE_CONFIG.getProperty("mailersend.from.email")
        );

        String fromName = pick(
                envOrNull("MAILERSEND_FROM_NAME"),
                envOrNull("MAILSENDER_FROM_NAME"),
                System.getProperty("mailersend.from.name"),
                FILE_CONFIG.getProperty("mailersend.from.name"),
                "PIDEV Formation"
        );

        if (isBlank(apiKey) || isBlank(fromEmail)) {
            lastError = "Configuration MailerSend manquante. Définissez MAILERSEND_API_KEY + MAILERSEND_FROM_EMAIL "
                    + "(ou MAILSENDER_*), ou ajoutez ./mailing.properties avec mailersend.api.key et mailersend.from.email.";
            return false;
        }

        try {
            URL url = new URL("https://api.mailersend.com/v1/email");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(8000);

            String payload = "{"
                    + "\"from\":{\"email\":\"" + escape(fromEmail) + "\",\"name\":\"" + escape(fromName) + "\"},"
                    + "\"to\":[{\"email\":\"" + escape(email) + "\",\"name\":\"" + escape(fullName) + "\"}],"
                    + "\"subject\":\"Bienvenue sur la plateforme PIDEV\","
                    + "\"text\":\"Bonjour " + escape(fullName) + ", votre inscription a la formation " + escape(formationTitle) + " est confirmee.\""
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                lastError = "";
                return true;
            }

            String body = readErrorBody(conn);
            lastError = "MailerSend HTTP status: " + code + (body.isBlank() ? "" : " | " + body);
            return false;
        } catch (IOException e) {
            lastError = e.getMessage();
            return false;
        }
    }

    public String getLastError() {
        return lastError;
    }

    private static Properties loadFileConfig() {
        Properties p = new Properties();
        Path path = Path.of("mailing.properties");
        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                p.load(in);
            } catch (IOException ignored) {
            }
        }
        return p;
    }

    private String readErrorBody(HttpURLConnection conn) {
        try (InputStream in = conn.getErrorStream()) {
            if (in == null) return "";
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("\n", " ").trim();
        } catch (IOException e) {
            return "";
        }
    }

    private String envOrNull(String key) {
        String value = System.getenv(key);
        return isBlank(value) ? null : value;
    }

    private String pick(String... values) {
        for (String value : values) {
            if (!isBlank(value)) return value;
        }
        return null;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
