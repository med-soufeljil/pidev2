package services;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class MailingApiService {

    /**
     * Sends registration email through external API.
     * If network/API fails, it stores a local outbox record and returns true (queued).
     */
    public boolean sendRegistrationEmail(String email, String fullName, String formationTitle) {
        if (sendViaExternalApi(email, fullName, formationTitle)) {
            return true;
        }
        return queueLocally(email, fullName, formationTitle);
    }

    private boolean sendViaExternalApi(String email, String fullName, String formationTitle) {
        try {
            URL url = new URL("https://jsonplaceholder.typicode.com/posts");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(5000);

            String payload = "{"
                    + "\"to\":\"" + escape(email) + "\","
                    + "\"subject\":\"Bienvenue sur la plateforme\","
                    + "\"message\":\"Bonjour " + escape(fullName) + ", votre inscription a la formation " + escape(formationTitle) + " est confirmee.\""
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            return code >= 200 && code < 300;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean queueLocally(String email, String fullName, String formationTitle) {
        try {
            Path outbox = Path.of("mail-outbox.log");
            String line = "to=" + email + "|name=" + fullName + "|formation=" + formationTitle + "|status=QUEUED\n";
            Files.writeString(outbox, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
