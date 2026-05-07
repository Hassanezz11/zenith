package atlantafx.sampler.zenith;

import atlantafx.sampler.zenith.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void onLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        try {
            if (!UserDAO.checkPassword(email, password)) {
                errorLabel.setText("Invalid email or password.");
                return;
            }
            Joueur user = UserDAO.getByEmail(email);
            Session.setCurrentUser(user);
            ZenithApp.showMain();
        } catch (SQLException e) {
            errorLabel.setText("Connection error. Try again.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onSignup() {
        ZenithApp.showSignup();
    }
}
