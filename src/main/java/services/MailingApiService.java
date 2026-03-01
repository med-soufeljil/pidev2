package services;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MailingApiService {

    /**
     * Simulates sending registration email by calling an external API.
     * Uses jsonplaceholder as public API to emulate outbound mailing request.
     */
    public boolean sendRegistrationEmail(String email, String fullName, String formationTitle) {
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
                    + "\"message\":\"Bonjour " + escape(fullName) + ", votre inscription à la formation " + escape(formationTitle) + " est confirmée.\""
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

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
