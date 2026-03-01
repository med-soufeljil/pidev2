package services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ExternalApiService {

    private static final String UUID_API = "https://www.uuidtools.com/api/generate/v4";
    private static final String GENDERIZE_API = "https://api.genderize.io/?name=";
    private static final String NATIONALIZE_API = "https://api.nationalize.io/?name=";
    private static final String REMOTIVE_API = "https://remotive.com/api/remote-jobs?search=";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public String generateMeetingLink() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(UUID_API))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonArray array = gson.fromJson(response.body(), JsonArray.class);
        String token = array.get(0).getAsString().substring(0, 12);
        return "https://meet.jit.si/recruitment-" + token;
    }

    public String getGenderPrediction(String firstName) throws IOException, InterruptedException {
        String encoded = URLEncoder.encode(firstName, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GENDERIZE_API + encoded))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject obj = gson.fromJson(response.body(), JsonObject.class);

        if (!obj.has("gender") || obj.get("gender").isJsonNull()) {
            return "No gender prediction found for this name.";
        }

        String gender = obj.get("gender").getAsString();
        double probability = obj.get("probability").getAsDouble() * 100;
        return String.format("Predicted gender: %s (%.1f%% confidence)", gender, probability);
    }

    public String getNationalityPrediction(String firstName) throws IOException, InterruptedException {
        String encoded = URLEncoder.encode(firstName, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(NATIONALIZE_API + encoded))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject obj = gson.fromJson(response.body(), JsonObject.class);

        if (!obj.has("country") || obj.getAsJsonArray("country").isEmpty()) {
            return "No nationality prediction found for this name.";
        }

        JsonObject country = obj.getAsJsonArray("country").get(0).getAsJsonObject();
        String countryId = country.get("country_id").getAsString();
        double probability = country.get("probability").getAsDouble() * 100;
        return String.format("Most probable nationality: %s (%.1f%% confidence)", countryId, probability);
    }

    public String fetchMarketJobs(String skill) throws IOException, InterruptedException {
        String encoded = URLEncoder.encode(skill, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(REMOTIVE_API + encoded))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject obj = gson.fromJson(response.body(), JsonObject.class);

        if (!obj.has("jobs") || obj.getAsJsonArray("jobs").isEmpty()) {
            return "No market jobs found for skill: " + skill;
        }

        StringBuilder out = new StringBuilder("Top market jobs for '").append(skill).append("':\n");
        int max = Math.min(3, obj.getAsJsonArray("jobs").size());
        for (int i = 0; i < max; i++) {
            JsonObject job = obj.getAsJsonArray("jobs").get(i).getAsJsonObject();
            out.append("- ").append(job.get("title").getAsString())
                    .append(" @ ").append(job.get("company_name").getAsString())
                    .append("\n");
        }
        return out.toString();
    }

}
