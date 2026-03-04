package org.soa.tp1.pi_dev_s2.mouhamd.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import org.soa.tp1.pi_dev_s2.mouhamd.utils.ApiRuntime;
import javafx.stage.Stage;

public class FeedbackFormController {
    @FXML private TextField tfAuteur;
    @FXML private TextArea taCommentaire;
    @FXML private Slider slNote;
    @FXML private Label lblStars;

    private String auteur;
    private String commentaire;
    private int note;
    private boolean saved;

    @FXML
    public void initialize() {
        slNote.valueProperty().addListener((o,ov,nv)-> lblStars.setText(toStars((int)Math.round(nv.doubleValue()))));
    }

    @FXML
    public void save() {
        if (tfAuteur.getText().isBlank() || taCommentaire.getText().isBlank()) return;
        if (!isCommentClean(taCommentaire.getText().trim())) return;
        auteur = tfAuteur.getText().trim();
        commentaire = taCommentaire.getText().trim();
        note = (int) Math.round(slNote.getValue());
        saved = true;
        close();
    }

    @FXML
    public void cancel() { close(); }

    private void close() { ((Stage) tfAuteur.getScene().getWindow()).close(); }
    public boolean isSaved() { return saved; }
    public String getAuteur() { return auteur; }
    public String getCommentaire() { return commentaire; }
    public int getNote() { return note; }

    private String toStars(int value){
        int v=Math.max(0,Math.min(5,value));
        return "★".repeat(v)+"☆".repeat(5-v);
    }

    private boolean isCommentClean(String text) {
        try {
            URL url = new URL(ApiRuntime.getBaseUrl() + "/api/moderation/badwords?text=" + java.net.URLEncoder.encode(text, StandardCharsets.UTF_8));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            try (InputStream in = conn.getInputStream()) {
                String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                boolean clean = body.contains("\"clean\":true");
                if (!clean) {
                    Alert a = new Alert(Alert.AlertType.WARNING);
                    a.setTitle("Modération");
                    a.setHeaderText("Attention");
                    a.setContentText("Votre commentaire contient des mots interdits (FR/EN). Merci de reformuler.");
                    a.showAndWait();
                }
                return clean;
            }
        } catch (Exception ignored) {
            return true;
        }
    }
}
