package services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.HashMap;
import java.util.Map;

/**✅ No API key required.
 */
public class HolidayService {

    private static final String NAGER_URL = "https://date.nager.at/api/v3/PublicHolidays/%d/TN";


    private final Map<Integer, Map<String, String>> cache = new HashMap<>();

    // ── Fixed Tunisian public holidays (month-day → name) ────────────────────
    private static final Map<MonthDay, String> FIXED_HOLIDAYS = new HashMap<>();
    static {
        FIXED_HOLIDAYS.put(MonthDay.of(1, 1), "Jour de l'An");
        FIXED_HOLIDAYS.put(MonthDay.of(3, 20), "Fête de l'Indépendance");
        FIXED_HOLIDAYS.put(MonthDay.of(4, 9), "Journée des Martyrs");
        FIXED_HOLIDAYS.put(MonthDay.of(5, 1), "Fête du Travail");
        FIXED_HOLIDAYS.put(MonthDay.of(7, 25), "Fête de la République");
        FIXED_HOLIDAYS.put(MonthDay.of(8, 13), "Fête de la Femme");
        FIXED_HOLIDAYS.put(MonthDay.of(10, 15), "Fête de l'Évacuation");
    }

    // ─────────────────────────────────────────────────────────────────────────

    public String getHolidayNote(LocalDate date) {
        // 1. Check hardcoded fixed holidays first (instant, always works)
        MonthDay md = MonthDay.from(date);
        if (FIXED_HOLIDAYS.containsKey(md)) {
            return "🔴 Jour férié : " + FIXED_HOLIDAYS.get(md);
        }

        // 2. Try Nager.Date API for variable holidays (Eid, etc.)
        try {
            Map<String, String> yearMap = getHolidaysFromApi(date.getYear());
            String name = yearMap.get(date.toString());
            if (name != null)
                return "🔴 Jour férié : " + name;
        } catch (Exception ignored) {
            // API unavailable — already checked fixed list above
        }

        return "";
    }

    // ── Nager.Date API ────────────────────────────────────────────────────────

    private Map<String, String> getHolidaysFromApi(int year) throws Exception {
        if (cache.containsKey(year))
            return cache.get(year);

        String json = httpGet(String.format(NAGER_URL, year));
        Map<String, String> holidays = parseNagerJson(json);
        cache.put(year, holidays);
        return holidays;
    }

    /**
     * Simple parser for Nager.Date JSON array.
     * Sample: [{"date":"2026-03-20","localName":"Independence Day",...}, ...]
     */
    private Map<String, String> parseNagerJson(String json) {
        Map<String, String> result = new HashMap<>();
        if (json == null || json.isBlank() || json.equals("[]"))
            return result;

        String[] entries = json.split("\\{");
        for (String entry : entries) {
            String date = extractJsonString(entry, "date");
            String name = extractJsonString(entry, "localName");
            if (date != null && name != null && !date.isEmpty()) {
                result.put(date, name);
            }
        }
        return result;
    }

    /**
     * Extracts a JSON string value: "key":"value" → value.
     * Handles both "key":"value" and "key" : "value" spacing.
     */
    private String extractJsonString(String fragment, String key) {
        // Search for "key" followed by ":"
        String search = "\"" + key + "\"";
        int k = fragment.indexOf(search);
        if (k == -1)
            return null;
        int colon = fragment.indexOf(':', k + search.length());
        if (colon == -1)
            return null;
        int q1 = fragment.indexOf('"', colon + 1);
        if (q1 == -1)
            return null;
        int q2 = fragment.indexOf('"', q1 + 1);
        if (q2 == -1)
            return null;
        return fragment.substring(q1 + 1, q2);
    }

    // ── HTTP helper ───────────────────────────────────────────────────────────

    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(4000);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "JavaFX-App/1.0");
        int code = conn.getResponseCode();
        if (code != 200)
            throw new Exception("HTTP " + code);
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
