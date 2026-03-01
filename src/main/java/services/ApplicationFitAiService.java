package services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.Candidat;
import models.Offre;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ApplicationFitAiService {

    public record FitResult(int score, String analysis, String engine) {}

    private static final String HF_MODEL_URL = "https://api-inference.huggingface.co/models/google/flan-t5-base";
    private static final int MAX_TEXT_LENGTH = 12000;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public FitResult analyze(Candidat candidat, Offre offre) {
        String cvText = readCvText(candidat.getCv());
        FitResult local = localHeuristic(candidat, offre, cvText);

        String token = System.getenv("HF_API_TOKEN");
        if (token == null || token.isBlank()) {
            return local;
        }

        try {
            String prompt = "Tu es un recruteur RH. Donne uniquement: 'Score: X/100' puis 2 phrases de justification en français. " +
                    "Offre=" + offerSnapshot(offre) + ". " +
                    "Candidat=" + candidatSnapshot(candidat) + ". " +
                    "CV=" + truncate(cvText, 3500);

            JsonObject payload = new JsonObject();
            payload.addProperty("inputs", prompt);

            HttpRequest request = HttpRequest.newBuilder(URI.create(HF_MODEL_URL))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonArray array = JsonParser.parseString(response.body()).getAsJsonArray();
                if (!array.isEmpty()) {
                    String generated = array.get(0).getAsJsonObject().get("generated_text").getAsString();
                    int aiScore = extractScore(generated);
                    int blended = blendScores(local.score, aiScore);
                    String analysis = "Score=" + blended + "/100. IA: " + truncate(generated, 260) +
                            " | Local: " + local.analysis;
                    return new FitResult(blended, analysis, "HuggingFace + heuristique");
                }
            }
        } catch (Exception ignored) {
        }

        return local;
    }

    private FitResult localHeuristic(Candidat candidat, Offre offre, String cvText) {
        Set<String> requiredSkills = tokenize(offre.getCompetences());
        Set<String> offerTerms = tokenize(offerSnapshot(offre));
        Set<String> cvTerms = tokenize(cvText);

        int skillMatches = countMatches(requiredSkills, cvTerms);
        int titleMatches = countMatches(tokenize(offre.getNomOffre()), cvTerms);
        double skillCoverage = requiredSkills.isEmpty() ? 0.0 : (double) skillMatches / requiredSkills.size();

        Set<String> overlap = new LinkedHashSet<>(cvTerms);
        overlap.retainAll(offerTerms);
        double jaccard = offerTerms.isEmpty() ? 0.0 : (double) overlap.size() / offerTerms.size();

        int cvLengthBonus = Math.min(12, cvTerms.size() / 8);
        int score = (int) Math.round((skillCoverage * 62) + (jaccard * 23) + (titleMatches * 6) + cvLengthBonus);
        score = Math.max(0, Math.min(100, score));

        List<String> matched = requiredSkills.stream().filter(cvTerms::contains).limit(10).toList();
        String analysis = "Compétences requises trouvées=" + skillMatches + "/" + requiredSkills.size() +
                ", recouvrement offre=" + Math.round(jaccard * 100) + "%" +
                ", mots-clés titre=" + titleMatches +
                ", skills match=" + (matched.isEmpty() ? "aucune" : String.join(", ", matched));

        return new FitResult(score, analysis, "Heuristique locale");
    }

    private int blendScores(int localScore, int aiScore) {
        if (aiScore <= 0) return localScore;
        return (int) Math.round((localScore * 0.65) + (aiScore * 0.35));
    }

    private int countMatches(Set<String> expected, Set<String> actual) {
        if (expected == null || expected.isEmpty() || actual == null || actual.isEmpty()) return 0;
        int count = 0;
        for (String token : expected) {
            if (actual.contains(token)) count++;
        }
        return count;
    }

    private int extractScore(String text) {
        if (text == null) return 0;
        var matcher = Pattern.compile("(?i)score\\D{0,8}(\\d{1,3})|\\b(\\d{1,3})\\s*/\\s*100").matcher(text);
        while (matcher.find()) {
            String v = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            int score = Integer.parseInt(v);
            if (score <= 100) return score;
        }
        return 0;
    }

    private Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) return Set.of();
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{Nd}]+"))
                .filter(token -> token.length() > 2)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String readCvText(String cvPath) {
        if (cvPath == null || cvPath.isBlank()) return "";
        try {
            Path path = Path.of(cvPath);
            if (!Files.exists(path) || Files.isDirectory(path)) return "";

            String ext = getExtension(path.getFileName().toString()).toLowerCase(Locale.ROOT);
            String text;
            switch (ext) {
                case "pdf" -> text = readPdf(path);
                case "docx" -> text = readDocx(path);
                case "txt" -> text = Files.readString(path, StandardCharsets.UTF_8);
                default -> text = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            }
            return normalizeWhitespace(truncate(text, MAX_TEXT_LENGTH));
        } catch (Exception e) {
            return "";
        }
    }

    private String readPdf(Path path) throws IOException {
        try (PDDocument doc = PDDocument.load(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private String readDocx(Path path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (ZipFile zip = new ZipFile(path.toFile(), StandardCharsets.UTF_8)) {
            ZipEntry entry = zip.getEntry("word/document.xml");
            if (entry == null) return "";
            String xml = new String(zip.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
            String text = xml.replaceAll("</w:p>", "\n")
                    .replaceAll("<[^>]+>", " ");
            sb.append(text);
        }
        return sb.toString();
    }

    private String normalizeWhitespace(String text) {
        if (text == null) return "";
        return text.replaceAll("\\s+", " ").trim();
    }

    private String offerSnapshot(Offre offre) {
        if (offre == null) return "";
        return String.join(" ",
                Optional.ofNullable(offre.getNomOffre()).orElse(""),
                Optional.ofNullable(offre.getCompetences()).orElse(""),
                Optional.ofNullable(offre.getType()).map(Enum::name).orElse(""));
    }

    private String candidatSnapshot(Candidat candidat) {
        if (candidat == null) return "";
        return String.join(" ",
                Optional.ofNullable(candidat.getNom()).orElse(""),
                Optional.ofNullable(candidat.getPrenom()).orElse(""),
                Optional.ofNullable(candidat.getEmail()).orElse(""),
                Optional.ofNullable(candidat.getAdresse()).orElse(""));
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        if (text.length() <= max) return text;
        return text.substring(0, max);
    }

    private String getExtension(String name) {
        int idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx + 1) : "";
    }
}
