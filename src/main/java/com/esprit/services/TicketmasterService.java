package com.esprit.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TicketmasterService {

    // 🔴 REMPLACE PAR TA VRAIE CLÉ API (obtenue sur developer.ticketmaster.com)
    private static final String API_KEY = "VOTRE_CLE_API_ICI";

    private static final String BASE_URL = "https://app.ticketmaster.com/discovery/v2";

    private final OkHttpClient client;
    private final ObjectMapper mapper;
    private final Random random;

    public TicketmasterService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
        this.mapper = new ObjectMapper();
        this.random = new Random();
        System.out.println("✅ TicketmasterService initialisé");
    }

    /**
     * Recherche des événements par mot-clé avec fallback sur des données fictives
     */
    public List<TicketmasterEvent> searchEvents(String keyword, String city, String countryCode) {
        List<TicketmasterEvent> events = new ArrayList<>();

        // Essayer d'abord l'API réelle
        try {
            StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/events.json?apikey=" + API_KEY);
            urlBuilder.append("&size=20");

            if (keyword != null && !keyword.isEmpty()) {
                urlBuilder.append("&keyword=").append(keyword);
            }

            if (city != null && !city.isEmpty()) {
                urlBuilder.append("&city=").append(city);
            }

            if (countryCode != null && !countryCode.isEmpty()) {
                urlBuilder.append("&countryCode=").append(countryCode);
            } else {
                urlBuilder.append("&countryCode=TN,FR,IT,ES");
            }

            String url = urlBuilder.toString();
            System.out.println("📡 Appel Ticketmaster: " + url);

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonNode root = mapper.readTree(responseBody);

                    if (root.has("_embedded")) {
                        JsonNode embedded = root.path("_embedded");
                        JsonNode eventsNode = embedded.path("events");

                        if (eventsNode.isArray()) {
                            for (JsonNode eventNode : eventsNode) {
                                TicketmasterEvent event = parseEvent(eventNode);
                                if (event != null) {
                                    events.add(event);
                                }
                            }
                        }
                    }

                    System.out.println("✅ " + events.size() + " événements trouvés via API");
                } else {
                    System.out.println("⚠️ API Ticketmaster non disponible (code " + response.code() + ")");
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur réseau: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }

        // Si aucun événement trouvé, générer des données fictives
        if (events.isEmpty()) {
            System.out.println("📋 Génération de données fictives...");
            events = generateMockEvents(keyword, city);
        }

        return events;
    }

    /**
     * Génère des événements fictifs pour la démonstration
     */
    private List<TicketmasterEvent> generateMockEvents(String keyword, String city) {
        List<TicketmasterEvent> events = new ArrayList<>();

        String ville = (city != null && !city.isEmpty()) ? city : "Tunis";
        String pays = "Tunisie";

        // Événements de musique
        if (keyword == null || keyword.contains("music") || keyword.contains("concert") || keyword.contains("festival")) {
            events.add(createMockEvent(
                    "Concert de musique classique",
                    "Acropolium de Carthage",
                    ville, pays,
                    getFutureDate(5),
                    "20:00",
                    45.0, 120.0,
                    "music",
                    "classique"
            ));

            events.add(createMockEvent(
                    "Festival international de Carthage",
                    "Théâtre romain de Carthage",
                    ville, pays,
                    getFutureDate(15),
                    "21:30",
                    30.0, 150.0,
                    "festival",
                    "musique"
            ));

            events.add(createMockEvent(
                    "Concert de rock alternatif",
                    "Salle Le Colisée",
                    ville, pays,
                    getFutureDate(8),
                    "22:00",
                    25.0, 80.0,
                    "music",
                    "rock"
            ));
        }

        // Événements sportifs
        if (keyword == null || keyword.contains("sport") || keyword.contains("match") || keyword.contains("foot")) {
            events.add(createMockEvent(
                    "Match de football: EST - CSS",
                    "Stade Olympique de Radès",
                    ville, pays,
                    getFutureDate(3),
                    "18:00",
                    15.0, 50.0,
                    "sports",
                    "football"
            ));

            events.add(createMockEvent(
                    "Tournoi de tennis",
                    "Tennis Club de Tunis",
                    ville, pays,
                    getFutureDate(10),
                    "10:00",
                    20.0, 60.0,
                    "sports",
                    "tennis"
            ));
        }

        // Événements culturels
        if (keyword == null || keyword.contains("theatre") || keyword.contains("spectacle")) {
            events.add(createMockEvent(
                    "Pièce de théâtre: Le Médecin malgré lui",
                    "Théâtre municipal de Tunis",
                    ville, pays,
                    getFutureDate(7),
                    "19:30",
                    25.0, 70.0,
                    "theatre",
                    "comédie"
            ));

            events.add(createMockEvent(
                    "Exposition d'art contemporain",
                    "Musée d'Art Moderne de Tunis",
                    ville, pays,
                    getFutureDate(12),
                    "10:00",
                    10.0, 20.0,
                    "arts",
                    "exhibition"
            ));
        }

        // Événements famille
        if (keyword == null || keyword.contains("famille") || keyword.contains("enfant")) {
            events.add(createMockEvent(
                    "Cirque de Tunis",
                    "Parc des Expositions du Kram",
                    ville, pays,
                    getFutureDate(20),
                    "15:00",
                    15.0, 40.0,
                    "family",
                    "circus"
            ));
        }

        // Mélanger les résultats
        java.util.Collections.shuffle(events);

        // Limiter à 8 événements max
        return events.subList(0, Math.min(8, events.size()));
    }

    /**
     * Crée un événement fictif
     */
    private TicketmasterEvent createMockEvent(String name, String venue, String city, String country,
                                              String date, String time, double minPrice, double maxPrice,
                                              String segment, String genre) {
        TicketmasterEvent event = new TicketmasterEvent();

        event.id = "MOCK_" + System.currentTimeMillis() + "_" + random.nextInt(1000);
        event.name = name;
        event.url = "https://www.ticketmaster.com";
        event.date = date;
        event.time = time;
        event.venueName = venue;
        event.city = city;
        event.country = country;
        event.countryCode = "TN";
        event.minPrice = minPrice;
        event.maxPrice = maxPrice;
        event.currency = "TND";
        event.segment = segment;
        event.genre = genre;
        event.imageUrl = "https://via.placeholder.com/300x200?text=Event";
        event.status = "onsale";

        return event;
    }

    /**
     * Retourne une date future au format YYYY-MM-DD
     */
    private String getFutureDate(int daysFromNow) {
        return LocalDate.now().plusDays(daysFromNow).toString();
    }

    /**
     * Parse un nœud JSON en objet TicketmasterEvent
     */
    private TicketmasterEvent parseEvent(JsonNode node) {
        try {
            TicketmasterEvent event = new TicketmasterEvent();

            event.id = node.path("id").asText();
            event.name = node.path("name").asText("Sans titre");
            event.url = node.path("url").asText();

            JsonNode dates = node.path("dates").path("start");
            event.date = dates.path("localDate").asText();
            event.time = dates.path("localTime").asText();

            JsonNode priceRanges = node.path("priceRanges");
            if (priceRanges.isArray() && priceRanges.size() > 0) {
                JsonNode price = priceRanges.get(0);
                event.minPrice = price.path("min").asDouble(0);
                event.maxPrice = price.path("max").asDouble(0);
                event.currency = price.path("currency").asText("USD");
            }

            JsonNode embedded = node.path("_embedded");
            JsonNode venues = embedded.path("venues");
            if (venues.isArray() && venues.size() > 0) {
                JsonNode venue = venues.get(0);
                event.venueName = venue.path("name").asText("Lieu inconnu");
                event.city = venue.path("city").path("name").asText("");
                event.country = venue.path("country").path("name").asText("");
                event.countryCode = venue.path("country").path("countryCode").asText("");
            }

            JsonNode classifications = node.path("classifications");
            if (classifications.isArray() && classifications.size() > 0) {
                JsonNode classif = classifications.get(0);
                event.segment = classif.path("segment").path("name").asText("Autre");
                event.genre = classif.path("genre").path("name").asText("");
            }

            return event;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Classe interne représentant un événement
     */
    public static class TicketmasterEvent {
        public String id;
        public String name;
        public String url;
        public String date;
        public String time;
        public String venueName;
        public String city;
        public String country;
        public String countryCode;
        public double minPrice;
        public double maxPrice;
        public String currency;
        public String segment;
        public String genre;
        public String imageUrl;
        public String status;

        public String getShortDisplay() {
            StringBuilder sb = new StringBuilder();
            sb.append("🎫 **").append(name).append("**\n");
            sb.append("📅 ").append(formatDate(date));
            if (time != null && !time.isEmpty()) {
                sb.append(" à ").append(time.substring(0, 5));
            }
            sb.append("\n");
            sb.append("📍 ").append(venueName);
            if (city != null && !city.isEmpty()) {
                sb.append(", ").append(city);
            }
            sb.append("\n");
            if (minPrice > 0) {
                sb.append("💰 ").append((int)minPrice).append(" - ").append((int)maxPrice).append(" ").append(currency);
            }
            return sb.toString();
        }

        public String getDetailedDisplay() {
            StringBuilder sb = new StringBuilder();
            sb.append("🎫 **").append(name).append("**\n\n");
            sb.append("📅 **Date :** ").append(formatDate(date));
            if (time != null && !time.isEmpty()) {
                sb.append(" à ").append(time);
            }
            sb.append("\n");
            sb.append("📍 **Lieu :** ").append(venueName).append("\n");
            sb.append("🏙️ **Ville :** ").append(city).append(", ").append(country).append("\n");
            sb.append("🎭 **Catégorie :** ").append(segment);
            if (genre != null && !genre.isEmpty()) {
                sb.append(" > ").append(genre);
            }
            sb.append("\n");
            if (minPrice > 0) {
                sb.append("💰 **Prix :** ").append((int)minPrice).append(" - ").append((int)maxPrice).append(" ").append(currency).append("\n");
            }
            sb.append("🔗 **Billetterie :** ").append(url);
            return sb.toString();
        }

        private String formatDate(String dateStr) {
            if (dateStr == null || dateStr.isEmpty()) return "Date inconnue";
            try {
                LocalDate date = LocalDate.parse(dateStr);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
                return date.format(formatter);
            } catch (Exception e) {
                return dateStr;
            }
        }
    }
}