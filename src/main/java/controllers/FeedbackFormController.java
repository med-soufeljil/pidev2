package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FeedbackFormController {
    @FXML private TextField tfAuteur;
    @FXML private TextArea taCommentaire;
    @FXML private Slider slNote;

    private String auteur;
    private String commentaire;
    private int note;
    private boolean saved;

    @FXML
    public void save() {
        if (tfAuteur.getText().isBlank() || taCommentaire.getText().isBlank()) return;
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
}
