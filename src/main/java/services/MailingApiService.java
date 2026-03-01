package services;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MailingApiService {

    private String lastError = "";

    /**
     * Real email sending through MailerSend API.
     * Requires env vars:
     * - MAILERSEND_API_KEY
     * - MAILERSEND_FROM_EMAIL
     * Optional:
     * - MAILERSEND_FROM_NAME
     */
    public boolean sendRegistrationEmail(String email, String fullName, String formationTitle) {
        String apiKey = System.getenv("MAILERSEND_API_KEY");
        String fromEmail = System.getenv("MAILERSEND_FROM_EMAIL");
        String fromName = System.getenv().getOrDefault("MAILERSEND_FROM_NAME", "PIDEV Formation");

        if (isBlank(apiKey) || isBlank(fromEmail)) {
            lastError = "MAILERSEND_API_KEY or MAILERSEND_FROM_EMAIL is missing.";
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
            lastError = "MailerSend HTTP status: " + code;
            return false;
        } catch (IOException e) {
            lastError = e.getMessage();
            return false;
        }
    }

    public String getLastError() {
        return lastError;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
