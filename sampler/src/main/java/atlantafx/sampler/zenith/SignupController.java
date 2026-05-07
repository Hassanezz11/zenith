package atlantafx.sampler.zenith;

import atlantafx.sampler.zenith.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class SignupController {

    @FXML private TextField     nameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label         errorLabel;

    @FXML
    private void onCreate() {
        String nom      = nameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm  = confirmField.getText();

        if (nom.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }
        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        try {
            if (UserDAO.getByEmail(email) != null) {
                errorLabel.setText("Email already registered.");
                return;
            }
            UserDAO.save(nom, email, password);
            ZenithApp.showLogin();
        } catch (SQLException e) {
            errorLabel.setText("Could not create account. Try again.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackToLogin() {
        ZenithApp.showLogin();
    }
}
