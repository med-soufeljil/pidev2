package services.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class WeatherService {

    private static final String API_KEY = "c8326e0c84aa48ed8227159acb71beba";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Dictionnaire des villes tunisiennes et leurs variantes
    private final Map<String, String> VILLES_TUNISIENNES = new HashMap<>() {{
        // Grand Tunis
        put("tunis", "Tunis");
        put("tunisie", "Tunis");
        put("la goulette", "Tunis");
        put("le bardo", "Tunis");
        put("carthage", "Tunis");
        put("sidi bou said", "Tunis");
        put("la marsa", "Tunis");
        put("el menzah", "Tunis");
        put("el manar", "Tunis");
        put("mutuelleville", "Tunis");
        put("notre dame", "Tunis");
        put("montplaisir", "Tunis");
        put("belvedere", "Tunis");
        put("charguia", "Tunis");
        put("ennasr", "Tunis");
        put("ariana", "Ariana");
        put("soukra", "Ariana");
        put("raoued", "Ariana");
        put("ben arous", "Ben Arous");
        put("mohammedia", "Ben Arous");
        put("manouba", "Manouba");
        put("oued ellil", "Manouba");

        // Grand Sousse
        put("sousse", "Sousse");
        put("hammam sousse", "Sousse");
        put("kalâa kebira", "Sousse");
        put("msaken", "Sousse");
        put("akouda", "Sousse");

        // Grand Sfax
        put("sfax", "Sfax");
        put("sakiet ezzit", "Sfax");
        put("sakiet eddaier", "Sfax");
        put("thyna", "Sfax");
        put("gremda", "Sfax");

        // Nord
        put("bizerte", "Bizerte");
        put("menzel bourguiba", "Bizerte");
        put("mateur", "Bizerte");
        put("raf raf", "Bizerte");

        // Cap Bon
        put("nabeul", "Nabeul");
        put("hammamet", "Hammamet");
        put("korba", "Nabeul");
        put("kelibia", "Nabeul");
        put("menzel temime", "Nabeul");
        put("dar chaabane", "Nabeul");

        // Sahel
        put("monastir", "Monastir");
        put("mahdia", "Mahdia");
        put("kairouan", "Kairouan");

        // Ouest
        put("jendouba", "Jendouba");
        put("tabarka", "Jendouba");
        put("ain draham", "Jendouba");
        put("béja", "Béja");
        put("le kef", "Le Kef");
        put("siliana", "Siliana");
        put("seliana", "Siliana");

        // Centre
        put("kasserine", "Kasserine");
        put("sidi bouzid", "Sidi Bouzid");
        put("gafsa", "Gafsa");
        put("tozeur", "Tozeur");
        put("kébili", "Kébili");
        put("gabès", "Gabès");
        put("médenine", "Médenine");
        put("tataouine", "Tataouine");

        // Îles
        put("djerba", "Djerba");
        put("houmt souk", "Djerba");
        put("zarzis", "Zarzis");
        put("kerkennah", "Kerkennah");
    }};

    public WeatherService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Extrait le nom de la ville à partir d'une adresse complète
     */
    private String extraireVille(String adresse) {
        if (adresse == null || adresse.trim().isEmpty()) {
            System.out.println("⚠️ Adresse vide, utilisation de Tunis par défaut");
            return "Tunis";
        }

        String adresseLower = adresse.toLowerCase().trim();
        System.out.println("🔍 Analyse de l'adresse: " + adresse);

        // 1. Vérifier les lieux emblématiques
        String[] lieuxEmblematiques = {
                "esprit", "technopole", "technopôle", "ghazala", "charguia",
                "carthage", "la marsa", "sidi bou said", "hammamet",
                "djerba", "tozeur", "tabarka", "ain draham"
        };

        for (String lieu : lieuxEmblematiques) {
            if (adresseLower.contains(lieu)) {
                for (Map.Entry<String, String> entry : VILLES_TUNISIENNES.entrySet()) {
                    if (entry.getKey().contains(lieu) || lieu.contains(entry.getKey())) {
                        System.out.println("✅ Lieu emblématique détecté: " + lieu + " → " + entry.getValue());
                        return entry.getValue();
                    }
                }
            }
        }

        // 2. Chercher une correspondance dans le dictionnaire des villes
        for (Map.Entry<String, String> entry : VILLES_TUNISIENNES.entrySet()) {
            String key = entry.getKey();
            if (adresseLower.contains(key)) {
                System.out.println("✅ Ville détectée: " + key + " → " + entry.getValue());
                return entry.getValue();
            }
        }

        // 3. Chercher des patterns de codes postaux tunisiens
        Pattern codePostalPattern = Pattern.compile("\\b([1-9]\\d{3})\\b");
        var cpMatcher = codePostalPattern.matcher(adresse);
        if (cpMatcher.find()) {
            String cp = cpMatcher.group(1);
            String villeParCP = getVilleParCodePostal(cp);
            if (villeParCP != null) {
                System.out.println("✅ Code postal détecté: " + cp + " → " + villeParCP);
                return villeParCP;
            }
        }

        // 4. Chercher des mots qui pourraient être des noms de villes
        String[] mots = adresse.split("[,\\s]+");
        for (String mot : mots) {
            if (mot.length() > 3 && Character.isUpperCase(mot.charAt(0))) {
                // Vérifier si ce mot est dans notre dictionnaire (insensible à la casse)
                for (String ville : VILLES_TUNISIENNES.keySet()) {
                    if (ville.equalsIgnoreCase(mot)) {
                        System.out.println("✅ Mot détecté comme ville: " + mot);
                        return VILLES_TUNISIENNES.get(ville);
                    }
                }
            }
        }

        // 5. Par défaut, prendre le premier mot significatif
        for (String mot : mots) {
            if (mot.length() > 2 && !mot.matches("\\d+")) {
                System.out.println("⚠️ Premier mot significatif: " + mot + " (par défaut)");
                // Capitaliser la première lettre
                return mot.substring(0, 1).toUpperCase() + mot.substring(1).toLowerCase();
            }
        }

        System.out.println("⚠️ Aucune ville détectée, utilisation de Tunis");
        return "Tunis";
    }

    private String getVilleParCodePostal(String cp) {
        // Mapping des codes postaux tunisiens (simplifié)
        Map<String, String> cpMapping = new HashMap<>() {{
            // Tunis
            put("1000", "Tunis"); put("1001", "Tunis"); put("1002", "Tunis");
            put("1003", "Tunis"); put("1004", "Tunis"); put("1005", "Tunis");
            put("1006", "Tunis"); put("1007", "Tunis"); put("1008", "Tunis");
            put("1009", "Tunis"); put("1010", "Tunis"); put("1011", "Tunis");
            put("1012", "Tunis"); put("1013", "Tunis"); put("1014", "Tunis");
            put("1015", "Tunis"); put("1016", "Tunis"); put("1017", "Tunis");
            put("1018", "Tunis"); put("1019", "Tunis"); put("1020", "Tunis");

            // Ariana
            put("2080", "Ariana"); put("2081", "Ariana"); put("2082", "Ariana");
            put("2083", "Ariana"); put("2084", "Ariana"); put("2085", "Ariana");
            put("2086", "Ariana"); put("2087", "Ariana"); put("2088", "Ariana");

            // Ben Arous
            put("2013", "Ben Arous"); put("2014", "Ben Arous"); put("2015", "Ben Arous");

            // La Marsa
            put("2070", "La Marsa"); put("2071", "La Marsa"); put("2072", "La Marsa");
            put("2073", "La Marsa"); put("2074", "La Marsa"); put("2075", "La Marsa");

            // Sousse
            put("4000", "Sousse"); put("4001", "Sousse"); put("4002", "Sousse");
            put("4003", "Sousse"); put("4004", "Sousse"); put("4005", "Sousse");
            put("4006", "Sousse"); put("4011", "Sousse"); put("4012", "Sousse");
            put("4013", "Sousse"); put("4014", "Sousse"); put("4015", "Sousse");
            put("4016", "Sousse"); put("4021", "Sousse"); put("4022", "Sousse");

            // Sfax
            put("3000", "Sfax"); put("3001", "Sfax"); put("3002", "Sfax");
            put("3003", "Sfax"); put("3004", "Sfax"); put("3005", "Sfax");

            // Nabeul
            put("8000", "Nabeul"); put("8001", "Nabeul"); put("8002", "Nabeul");
            put("8011", "Nabeul"); put("8012", "Nabeul"); put("8013", "Nabeul");

            // Hammamet
            put("8050", "Hammamet"); put("8051", "Hammamet"); put("8052", "Hammamet");

            // Monastir
            put("5000", "Monastir"); put("5001", "Monastir"); put("5002", "Monastir");
            put("5011", "Monastir"); put("5012", "Monastir"); put("5013", "Monastir");
            put("5014", "Monastir"); put("5015", "Monastir"); put("5016", "Monastir");
            put("5021", "Monastir"); put("5022", "Monastir"); put("5023", "Monastir");
        }};

        return cpMapping.get(cp);
    }

    /**
     * Récupère les prévisions météo pour un lieu et une date
     */
    public WeatherInfo getPrevisionEvenement(String lieu, LocalDate date) {
        try {
            // Extraire la ville de l'adresse complète
            String ville = extraireVille(lieu);

            System.out.println("📍 Lieu original: " + lieu);
            System.out.println("🏙️ Ville extraite: " + ville);

            // URL avec la ville extraite
            String url = String.format("%s/forecast?q=%s,TN&appid=%s&units=metric&lang=fr&cnt=40",
                    BASE_URL, URLEncoder.encode(ville, "UTF-8"), API_KEY);

            System.out.println("🌐 Appel API Météo: " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "EventManagementApp/1.0")
                    .timeout(java.time.Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                return parseForecast(root, date, ville);
            } else {
                System.err.println("Erreur API météo pour " + ville + ": Code " + response.statusCode());

                // Essayer avec Tunis si la première tentative échoue
                if (!ville.equals("Tunis")) {
                    System.out.println("🔄 Tentative avec Tunis...");
                    return getPrevisionEvenement("Tunis", date);
                }

                return getMeteoParDefaut(ville, "API Error " + response.statusCode());
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel météo: " + e.getMessage());
            return getMeteoParDefaut(lieu, e.getMessage());
        }
    }

    private WeatherInfo parseForecast(JsonNode root, LocalDate targetDate, String ville) {
        try {
            JsonNode list = root.path("list");
            JsonNode city = root.path("city");

            String cityName = city.path("name").asText(ville);
            String country = city.path("country").asText("TN");

            String targetDateStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Chercher la prévision pour la date cible
            JsonNode bestForecast = null;
            int minHourDiff = 24;

            for (JsonNode forecast : list) {
                String dtTxt = forecast.path("dt_txt").asText();
                if (dtTxt.startsWith(targetDateStr)) {
                    String timeStr = dtTxt.substring(11, 16);
                    LocalTime forecastTime = LocalTime.parse(timeStr);

                    // Préférer les prévisions vers midi
                    int hourDiff = Math.abs(forecastTime.getHour() - 12);
                    if (hourDiff < minHourDiff) {
                        minHourDiff = hourDiff;
                        bestForecast = forecast;
                    }
                }
            }

            if (bestForecast != null) {
                WeatherInfo info = createWeatherInfo(bestForecast, cityName, country, targetDate);
                System.out.println("✅ Météo trouvée: " + info.temperature + "°C à " + cityName);
                return info;
            } else {
                System.out.println("⚠️ Aucune prévision pour " + targetDateStr + " à " + cityName);
                return getMeteoParDefaut(cityName, "Aucune prévision pour cette date");
            }

        } catch (Exception e) {
            System.err.println("Erreur parseForecast: " + e.getMessage());
            return getMeteoParDefaut(ville, "Erreur de parsing");
        }
    }

    private WeatherInfo createWeatherInfo(JsonNode forecast, String cityName, String country, LocalDate date) {
        WeatherInfo info = new WeatherInfo();

        JsonNode main = forecast.path("main");
        JsonNode weather = forecast.path("weather").get(0);
        JsonNode wind = forecast.path("wind");
        JsonNode clouds = forecast.path("clouds");

        info.cityName = cityName;
        info.country = country;
        info.date = date;
        info.temperature = main.path("temp").asDouble();
        info.ressenti = main.path("feels_like").asDouble();
        info.tempMin = main.path("temp_min").asDouble();
        info.tempMax = main.path("temp_max").asDouble();
        info.humidite = main.path("humidity").asInt();
        info.pression = main.path("pressure").asInt();
        info.description = weather.path("description").asText();
        info.iconeCode = weather.path("icon").asText();
        info.ventVitesse = wind.path("speed").asDouble();
        info.nuages = clouds.path("all").asInt();

        String mainCondition = weather.path("main").asText();
        info.iconeEmoji = getEmoji(mainCondition);
        info.couleur = getCouleur(mainCondition);
        info.conseil = getConseil(info.temperature,
                mainCondition.toLowerCase().contains("rain") ||
                        mainCondition.toLowerCase().contains("drizzle") ||
                        mainCondition.toLowerCase().contains("thunderstorm"));

        return info;
    }

    private String getEmojiFromIcon(String icon) {
        if (icon == null || icon.isEmpty()) return "🌡️";
        String code = icon.substring(0, 1);
        switch (code) {
            case "0": return "☀️";
            case "1": return "🌤️";
            case "2": return "⛅";
            case "3":
            case "4": return "☁️";
            case "9": return "🌧️";
            case "10": return "🌦️";
            case "11": return "⛈️";
            case "13": return "❄️";
            case "50": return "🌫️";
            default: return "🌡️";
        }
    }

    private String getCouleurFromIcon(String icon) {
        if (icon == null || icon.isEmpty()) return "#6b7280";
        String code = icon.substring(0, 1);
        switch (code) {
            case "0": return "#f59e0b";
            case "1": return "#84cc16";
            case "2": return "#a3e635";
            case "3":
            case "4": return "#64748b";
            case "9":
            case "10": return "#3b82f6";
            case "11": return "#8b5cf6";
            case "13": return "#06b6d4";
            case "50": return "#94a3b8";
            default: return "#6b7280";
        }
    }

    private String getEmoji(String main) {
        switch (main.toLowerCase()) {
            case "clear": return "☀️";
            case "clouds": return "☁️";
            case "rain": return "🌧️";
            case "drizzle": return "🌦️";
            case "thunderstorm": return "⛈️";
            case "snow": return "❄️";
            case "mist":
            case "fog": return "🌫️";
            default: return "🌡️";
        }
    }

    private String getCouleur(String main) {
        switch (main.toLowerCase()) {
            case "clear": return "#f59e0b";
            case "clouds": return "#64748b";
            case "rain":
            case "drizzle": return "#3b82f6";
            case "thunderstorm": return "#8b5cf6";
            case "snow": return "#06b6d4";
            case "mist":
            case "fog": return "#94a3b8";
            default: return "#6b7280";
        }
    }

    private String getConseil(double temp, boolean rain) {
        if (rain) return "☔ Risque de pluie - Prévoyez un parapluie !";
        if (temp < 5) return "❄️ Très froid - Manteau chaud recommandé !";
        if (temp < 10) return "🧥 Froid - Prenez une veste";
        if (temp < 15) return "🍂 Frais - Un pull suffira";
        if (temp < 20) return "🌤️ Doux - Température agréable";
        if (temp < 25) return "🌞 Chaud - Idéal pour un événement extérieur";
        if (temp < 30) return "☀️ Très chaud - Pensez à vous hydrater";
        return "🔥 Canicule - Événement en intérieur recommandé";
    }

    private WeatherInfo getMeteoParDefaut(String lieu, String raison) {
        WeatherInfo info = new WeatherInfo();
        info.cityName = lieu;
        info.country = "TN";
        info.date = LocalDate.now();
        info.temperature = 20.0;
        info.ressenti = 20.0;
        info.tempMin = 18.0;
        info.tempMax = 22.0;
        info.humidite = 50;
        info.pression = 1013;
        info.description = "Information non disponible";
        info.iconeEmoji = "🌡️";
        info.ventVitesse = 5.0;
        info.nuages = 0;
        info.couleur = "#94a3b8";
        info.conseil = "⚠️ " + raison;
        return info;
    }

    public static class WeatherInfo {
        public String cityName;
        public String country;
        public LocalDate date;
        public double temperature;
        public double ressenti;
        public double tempMin;
        public double tempMax;
        public int humidite;
        public int pression;
        public String description;
        public String iconeCode;
        public String iconeEmoji;
        public double ventVitesse;
        public int nuages;
        public String couleur;
        public String conseil;

        public String getFormatted() {
            return String.format("%s %.1f°C - %s", iconeEmoji, temperature, description);
        }

        public String getDetailFormatted() {
            return String.format("%s %.1f°C (ressenti %.1f°C) • %s • 💧 %d%% • 💨 %.1f m/s",
                    iconeEmoji, temperature, ressenti, description, humidite, ventVitesse);
        }

        public String getCityDisplay() {
            return cityName + (country.isEmpty() ? "" : ", " + country);
        }

        public String getTemperatureRange() {
            return String.format("%.1f°C / %.1f°C", tempMin, tempMax);
        }

        @Override
        public String toString() {
            return getFormatted();
        }
    }
}