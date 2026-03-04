package services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

/**
 * Provides a weather note for a given date using the Open-Meteo API.
 *
 * ✅ Completely FREE — No API key required.
 * ✅ Coordinates pre-set to Tunis, Tunisia.
 * ✅ Covers up to 7 days ahead; returns "non disponible" for other dates.
 *
 * API: https://open-meteo.com/
 */
public class WeatherService {

    // Tunis, Tunisia (lat/lon)
    private static final String URL_TEMPLATE = "https://api.open-meteo.com/v1/forecast"
            + "?latitude=36.82&longitude=10.17"
            + "&daily=weathercode,temperature_2m_max"
            + "&timezone=Africa%2FTunis&forecast_days=7";

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a one-line weather note for the given date (within 7 days).
     * Example: "🌧 Pluie (14°C) — TT conseillé"
     */
    public String getWeatherNote(LocalDate date) {
        try {
            String json = httpGet(URL_TEMPLATE);
            return parseNote(json, date);
        } catch (Exception e) {
            return "☁ Météo non disponible";
        }
    }

    // ── Parsing ───────────────────────────────────────────────────────────────

    private String parseNote(String json, LocalDate target) {
        // Response format:
        // {"daily":{"time":["2026-03-04","2026-03-05",...]
        // "weathercode":[0,61,...]
        // "temperature_2m_max":[22.5,14.1,...]}}

        String targetStr = target.toString(); // "yyyy-MM-dd"

        // ── Extract the "time" array ──────────────────────────────────────────
        int timeIdx = json.indexOf("\"time\":[");
        if (timeIdx == -1)
            return "☁ Météo non disponible";

        int bracketOpen = json.indexOf('[', timeIdx);
        int bracketClose = json.indexOf(']', bracketOpen);
        String timeArray = json.substring(bracketOpen + 1, bracketClose); // "2026-03-04","2026-03-05",...

        String[] dates = timeArray.split(",");
        int dayIndex = -1;
        for (int i = 0; i < dates.length; i++) {
            if (dates[i].contains(targetStr)) {
                dayIndex = i;
                break;
            }
        }
        if (dayIndex == -1)
            return "☁ Météo non disponible pour ce jour";

        // ── Extract weathercode at dayIndex ───────────────────────────────────
        int wc = extractIntAtIndex(json, "\"weathercode\":[", dayIndex);

        // ── Extract max temperature at dayIndex ───────────────────────────────
        double temp = extractDoubleAtIndex(json, "\"temperature_2m_max\":[", dayIndex);
        String tempStr = temp != Double.MIN_VALUE ? String.format(" (%.0f°C)", temp) : "";

        return buildNote(wc, tempStr);
    }

    /**
     * Maps WMO weather code → human-readable note.
     * Reference: https://open-meteo.com/en/docs/weathercode
     */
    private String buildNote(int code, String tempStr) {
        if (code == 0)
            return "☀ Ciel dégagé" + tempStr;
        else if (code <= 3)
            return "⛅ Nuageux" + tempStr;
        else if (code <= 48)
            return "🌫 Brouillard" + tempStr;
        else if (code <= 67 || (code >= 80 && code <= 82))
            return "🌧 Pluie" + tempStr + " — TT conseillé";
        else if (code <= 77 || (code >= 85 && code <= 86))
            return "🌨 Neige" + tempStr + " — TT conseillé";
        else if (code >= 95)
            return "⛈ Orage" + tempStr + " — TT fortement conseillé";
        else
            return "🌡 Météo inconnue" + tempStr;
    }

    // ── Array index extraction helpers ────────────────────────────────────────

    private int extractIntAtIndex(String json, String arrayKey, int index) {
        try {
            int keyIdx = json.indexOf(arrayKey);
            if (keyIdx == -1)
                return -1;
            int start = json.indexOf('[', keyIdx) + 1;
            int end = json.indexOf(']', start);
            String[] vals = json.substring(start, end).split(",");
            return Integer.parseInt(vals[index].trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private double extractDoubleAtIndex(String json, String arrayKey, int index) {
        try {
            int keyIdx = json.indexOf(arrayKey);
            if (keyIdx == -1)
                return Double.MIN_VALUE;
            int start = json.indexOf('[', keyIdx) + 1;
            int end = json.indexOf(']', start);
            String[] vals = json.substring(start, end).split(",");
            return Double.parseDouble(vals[index].trim());
        } catch (Exception e) {
            return Double.MIN_VALUE;
        }
    }

    // ── HTTP helper ───────────────────────────────────────────────────────────

    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("User-Agent", "JavaFX-App/1.0");
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line);
        }
        return sb.toString();
    }
}
