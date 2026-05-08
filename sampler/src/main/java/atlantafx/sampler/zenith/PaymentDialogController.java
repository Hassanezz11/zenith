package atlantafx.sampler.zenith;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PaymentDialogController implements Initializable {

    @FXML private VBox formView;
    @FXML private VBox successView;
    @FXML private Label gameTitleLabel;
    @FXML private Label gameCategoryLabel;
    @FXML private Label gamePriceLabel;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private TextField cvvField;
    @FXML private TextField cardNameField;
    @FXML private Label errorLabel;
    @FXML private Button payButton;
    @FXML private Label successGameLabel;
    @FXML private Button steamButton;
    @FXML private Button epicButton;
    @FXML private Button closeButton;

    private Jeu jeu;
    private Runnable onPaymentSuccess;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupCardNumberFormatter();
        setupExpiryFormatter();
        setupCvvFormatter();
    }

    void setup(Jeu jeu, Runnable onPaymentSuccess) {
        this.jeu = jeu;
        this.onPaymentSuccess = onPaymentSuccess;

        gameTitleLabel.setText(jeu.getTitre());
        gameCategoryLabel.setText(jeu.getCategory());
        gamePriceLabel.setText(jeu.getDisplayPrice());
        payButton.setText("Pay " + jeu.getDisplayPrice());
        successGameLabel.setText(jeu.getTitre() + " has been added to your library!\nUse the links below to activate or download your game.");
    }

    @FXML
    private void processPayment() {
        hideError();
        String cardNum  = cardNumberField.getText().replaceAll("\\s", "");
        String expiry   = expiryField.getText().trim();
        String cvv      = cvvField.getText().trim();
        String name     = cardNameField.getText().trim();

        if (cardNum.length() != 16) { showError("Invalid card number. Enter 16 digits."); return; }
        if (!expiry.matches("\\d{2}/\\d{2}")) { showError("Invalid expiry date. Use MM/YY format."); return; }
        if (cvv.length() < 3) { showError("Invalid CVV. Enter 3 or 4 digits."); return; }
        if (name.isBlank()) { showError("Please enter the cardholder name."); return; }

        int month = Integer.parseInt(expiry.substring(0, 2));
        if (month < 1 || month > 12) { showError("Invalid expiry month (01–12)."); return; }

        payButton.setText("Processing…");
        payButton.setDisable(true);
        cardNumberField.setDisable(true);
        expiryField.setDisable(true);
        cvvField.setDisable(true);
        cardNameField.setDisable(true);

        PauseTransition processing = new PauseTransition(Duration.seconds(2));
        processing.setOnFinished(e -> showSuccess());
        processing.play();
    }

    private void showSuccess() {
        if (onPaymentSuccess != null) onPaymentSuccess.run();

        formView.setVisible(false);
        formView.setManaged(false);
        successView.setVisible(true);
        successView.setManaged(true);
    }

    @FXML
    private void openSteam() {
        String query = URLEncoder.encode(jeu.getTitre(), StandardCharsets.UTF_8);
        openUrl("https://store.steampowered.com/search/?term=" + query);
    }

    @FXML
    private void openEpic() {
        String query = URLEncoder.encode(jeu.getTitre(), StandardCharsets.UTF_8);
        openUrl("https://store.epicgames.com/browse?q=" + query);
    }

    @FXML
    private void closeDialog() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void openUrl(String url) {
        try {
            new ProcessBuilder("cmd", "/c", "start", "", url).start();
        } catch (Exception e) {
            System.err.println("[Payment] Cannot open URL: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void setupCardNumberFormatter() {
        cardNumberField.setTextFormatter(new TextFormatter<>(change -> {
            String raw = change.getControlNewText().replaceAll("[^0-9]", "");
            if (raw.length() > 16) return null;
            StringBuilder fmt = new StringBuilder();
            for (int i = 0; i < raw.length(); i++) {
                if (i > 0 && i % 4 == 0) fmt.append(' ');
                fmt.append(raw.charAt(i));
            }
            change.setText(fmt.toString());
            change.setRange(0, change.getControlText().length());
            change.setCaretPosition(fmt.length());
            change.setAnchor(fmt.length());
            return change;
        }));
    }

    private void setupExpiryFormatter() {
        expiryField.setTextFormatter(new TextFormatter<>(change -> {
            String raw = change.getControlNewText().replaceAll("[^0-9]", "");
            if (raw.length() > 4) return null;
            String fmt = raw.length() > 2 ? raw.substring(0, 2) + "/" + raw.substring(2) : raw;
            change.setText(fmt);
            change.setRange(0, change.getControlText().length());
            change.setCaretPosition(fmt.length());
            change.setAnchor(fmt.length());
            return change;
        }));
    }

    private void setupCvvFormatter() {
        cvvField.setTextFormatter(new TextFormatter<>(change -> {
            String raw = change.getControlNewText().replaceAll("[^0-9]", "");
            if (raw.length() > 4) return null;
            change.setText(raw);
            change.setRange(0, change.getControlText().length());
            change.setCaretPosition(raw.length());
            change.setAnchor(raw.length());
            return change;
        }));
    }
}