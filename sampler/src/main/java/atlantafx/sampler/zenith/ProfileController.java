package atlantafx.sampler.zenith;

import atlantafx.sampler.zenith.dao.UserDAO;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ProfileController implements Initializable {

    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label rankLabel;
    @FXML private TextField nameField;
    @FXML private PasswordField passwordField;
    @FXML private Button saveProfileButton;
    @FXML private Label profileStatus;

    private final ZenithStore store = ZenithStore.getInstance();
    private Joueur currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = loadUserFromBackend();

        nameLabel.setText(currentUser.getNom());
        emailLabel.setText(currentUser.getEmail());

        String rank = currentUser.getRank();
        rankLabel.setText(rank != null ? rank : "Bronze");
        rankLabel.getStyleClass().addAll("rank-badge", RankCalculator.cssClass(rank));

        nameField.setText(currentUser.getNom());
        saveProfileButton.setOnAction(event -> saveProfile());
    }

    private Joueur loadUserFromBackend() {
        Joueur sessionUser = store.getCurrentUser();
        if (sessionUser == null) return null;
        try {
            Joueur fresh = sessionUser.getUserId() > 0
                ? UserDAO.getById(sessionUser.getUserId())
                : UserDAO.getByEmail(sessionUser.getEmail());
            if (fresh != null) {
                if (Session.isLoggedIn()) Session.setCurrentUser(fresh);
                return fresh;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sessionUser;
    }

    private void saveProfile() {
        if (currentUser == null || currentUser.getUserId() <= 0) {
            profileStatus.setText("Utilisateur non identifie.");
            return;
        }
        String newName     = nameField.getText() == null ? "" : nameField.getText().trim();
        String newPassword = passwordField.getText();

        if (newName.isEmpty()) {
            profileStatus.setText("Le nom ne peut pas etre vide.");
            return;
        }

        try {
            boolean changed = false;
            if (!newName.equals(currentUser.getNom())) {
                UserDAO.updateName(currentUser.getUserId(), newName);
                currentUser.setNom(newName);
                nameLabel.setText(newName);
                changed = true;
            }
            if (newPassword != null && !newPassword.isEmpty()) {
                UserDAO.updatePassword(currentUser.getUserId(), newPassword);
                currentUser.setMotDePasse(newPassword);
                passwordField.clear();
                changed = true;
            }
            profileStatus.setText(changed ? "Profil mis a jour." : "Aucun changement.");
        } catch (SQLException e) {
            e.printStackTrace();
            profileStatus.setText("Erreur lors de la mise a jour.");
        }
    }
}
