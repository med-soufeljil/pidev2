package org.soa.tp1.pi_dev_s2.mouhamd.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ExternalPublicApiService {

    public String fetchSuggestionTitle() {
        String endpoint = "https://jsonplaceholder.typicode.com/posts/1";
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2500);
            conn.setReadTimeout(4000);
            if (conn.getResponseCode() != 200) {
                return "Service externe indisponible";
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    body.append(line);
                }
                String content = body.toString();
                int idx = content.indexOf("\"title\":");
                if (idx == -1) return "Suggestion externe non trouvée";
                int start = content.indexOf('"', idx + 8) + 1;
                int end = content.indexOf('"', start);
                if (start <= 0 || end <= start) return "Suggestion externe non trouvée";
                return content.substring(start, end);
            }
        } catch (IOException e) {
            return "Suggestion externe indisponible";
        }
    }
}
