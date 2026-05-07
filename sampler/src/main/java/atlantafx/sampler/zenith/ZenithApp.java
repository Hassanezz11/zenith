package atlantafx.sampler.zenith;

import atlantafx.base.theme.Dracula;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ZenithApp extends Application {

    private static Stage primaryStage;
    private static double stageWidth;
    private static double stageHeight;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());

        // TEMP TEST — remove after confirming DB connection
        try {
            var games = atlantafx.sampler.zenith.dao.JeuDAO.getAll();
            System.out.println("DB connected. Games found: " + games.size());
        } catch (Exception e) {
            System.err.println("DB FAILED: " + e.getMessage());
        }

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stageWidth  = Math.min(1280, bounds.getWidth()  - 80);
        stageHeight = Math.min(820,  bounds.getHeight() - 80);

        primaryStage = stage;
        primaryStage.setTitle("Zenith");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.centerOnScreen();

        showLogin();
        primaryStage.show();
    }

    public static void showLogin() {
        switchScene("/zenith/fxml/Login.fxml");
    }

    public static void showSignup() {
        switchScene("/zenith/fxml/Signup.fxml");
    }

    public static void showMain() {
        switchScene("/zenith/fxml/MainLayout.fxml");
    }

    private static void switchScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(ZenithApp.class.getResource(fxmlPath));
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                primaryStage.setScene(new Scene(root, stageWidth, stageHeight));
            } else {
                scene.setRoot(root);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to switch to: " + fxmlPath, e);
        }
    }
}
