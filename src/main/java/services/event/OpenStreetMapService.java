package services.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Service d'intégration avec l'API OpenStreetMap (Nominatim)
 * Permet de géocoder des adresses (convertir texte en coordonnées GPS)
 */
public class OpenStreetMapService {

    // ==================== ATTRIBUTS ====================
    private final HttpClient httpClient;                    // Client HTTP
    private final ObjectMapper objectMapper;                 // Mapper JSON
    private Map<String, Location> cache = new HashMap<>();  // Cache pour éviter de surcharger l'API
    private long lastRequestTime = 0;                        // Dernier temps de requête

    /**
     * Constructeur - initialise le client HTTP
     */
    public OpenStreetMapService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Recherche un lieu sur OpenStreetMap avec plusieurs tentatives
     * @param query L'adresse ou le lieu à rechercher
     * @return Un objet Location contenant les coordonnées et détails, ou null si non trouvé
     */
    public Location geocode(String query) {
        if (query == null || query.trim().isEmpty()) {
            System.out.println("⚠️ Adresse vide");
            return null;
        }

        // Vérifier le cache
        if (cache.containsKey(query)) {
            System.out.println("✅ Utilisation du cache pour: " + query);
            return cache.get(query);
        }

        // Nettoyer l'adresse
        String adressePropre = query.trim();

        // Essayer différentes combinaisons
        Location location = null;

        // Attendre pour respecter la limite de l'API
        attendreSiNecessaire();

        // 1. Essayer avec l'adresse complète + Tunisie
        location = tryGeocode(adressePropre + ", Tunisie");
        if (location != null) {
            cache.put(query, location);
            return location;
        }

        attendreSiNecessaire();

        // 2. Essayer avec seulement le premier mot (souvent la ville)
        String premierMot = adressePropre.split(" ")[0].trim();
        if (!premierMot.isEmpty() && !premierMot.equals(adressePropre)) {
            location = tryGeocode(premierMot + ", Tunisie");
            if (location != null) {
                cache.put(query, location);
                return location;
            }
        }

        attendreSiNecessaire();

        // 3. Essayer sans "Tunisie" pour les grandes villes
        location = tryGeocode(adressePropre);
        if (location != null) {
            cache.put(query, location);
            return location;
        }

        attendreSiNecessaire();

        // 4. Dernier recours : Tunis
        if (!adressePropre.toLowerCase().contains("tunis")) {
            location = tryGeocode("Tunis, Tunisie");
            if (location != null) {
                System.out.println("⚠️ Fallback sur Tunis pour: " + query);
                cache.put(query, location);
                return location;
            }
        }

        System.out.println("❌ Aucune localisation trouvée pour: " + query);
        return null;
    }

    /**
     * Attend le temps nécessaire pour respecter la limite de 1 requête par seconde
     */
    private void attendreSiNecessaire() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;

        if (timeSinceLastRequest < 1000) { // 1 seconde
            try {
                Thread.sleep(1000 - timeSinceLastRequest);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequestTime = System.currentTimeMillis();
    }

    /**
     * Tente un géocodage avec une requête spécifique
     * @param query La requête à envoyer à l'API
     * @return Location ou null si échec
     */
    private Location tryGeocode(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1&addressdetails=1",
                    encodedQuery
            );

            System.out.println("🗺️ Recherche OpenStreetMap: " + query);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "EventManagementApp/1.0") // Obligatoire pour Nominatim
                    .header("Accept-Language", "fr")
                    .header("Referer", "http://localhost")
                    .timeout(java.time.Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseResponse(response.body(), query);
            } else if (response.statusCode() == 429) {
                System.err.println("⚠️ Trop de requêtes API (429) - Attente plus longue...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                System.err.println("❌ Erreur API OpenStreetMap: " + response.statusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Exception recherche lieu: " + e.getMessage());
        }
        return null;
    }

    /**
     * Parse la réponse JSON de l'API Nominatim
     * @param json La réponse JSON
     * @param originalQuery La requête originale (pour logging)
     * @return Location ou null
     */
    private Location parseResponse(String json, String originalQuery) throws Exception {
        JsonNode array = objectMapper.readTree(json);

        if (array.size() > 0) {
            JsonNode first = array.get(0);
            double lat = first.path("lat").asDouble();
            double lon = first.path("lon").asDouble();
            String displayName = first.path("display_name").asText();

            // Extraction des détails d'adresse
            JsonNode address = first.path("address");
            String city = address.path("city").asText();
            String town = address.path("town").asText();
            String village = address.path("village").asText();
            String country = address.path("country").asText();

            // Déterminer la ville (priorité: city > town > village)
            String ville = city;
            if (ville == null || ville.isEmpty()) {
                ville = town;
            }
            if (ville == null || ville.isEmpty()) {
                ville = village;
            }

            System.out.println("✅ Trouvé: " + displayName);

            return new Location(lat, lon, displayName, ville, country);
        }

        System.out.println("❌ Aucun résultat pour: " + originalQuery);
        return null;
    }

    /**
     * Classe interne représentant un lieu géocodé
     */
    public static class Location {
        public final double lat;                // Latitude
        public final double lon;                // Longitude
        public final String displayName;        // Nom complet du lieu
        public final String city;               // Ville
        public final String country;             // Pays

        public Location(double lat, double lon, String displayName, String city, String country) {
            this.lat = lat;
            this.lon = lon;
            this.displayName = displayName;
            this.city = city != null ? city : "";
            this.country = country != null ? country : "";
        }

        /**
         * Génère une URL Google Maps pour ce lieu
         */
        public String getGoogleMapsUrl() {
            return String.format("https://www.google.com/maps?q=%f,%f", lat, lon);
        }

        /**
         * Génère une URL OpenStreetMap pour ce lieu
         */
        public String getOpenStreetMapUrl() {
            return String.format("https://www.openstreetmap.org/?mlat=%f&mlon=%f#map=15/%f/%f",
                    lat, lon, lat, lon);
        }

        @Override
        public String toString() {
            return String.format("%.6f, %.6f", lat, lon);
        }
    }
}