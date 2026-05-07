package atlantafx.sampler.zenith;

import atlantafx.sampler.zenith.dao.MessageDAO;
import atlantafx.sampler.zenith.dao.UsersJeuxDAO;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class HomeController implements Initializable {

    @FXML private StackPane heroSection;
    @FXML private ImageView heroImage;
    @FXML private Label heroTitle;
    @FXML private Label heroSubtitle;
    @FXML private FlowPane quickStatsPane;
    @FXML private HBox aiSuggestionsBox;
    @FXML private Button playNowButton;

    private final ZenithStore store = ZenithStore.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Jeu featured = store.getFeaturedGame();
        heroImage.setImage(ZenithArtwork.createHero(featured, 1100, 340));
        heroTitle.setText(featured.getTitre());
        heroSubtitle.setText(featured.getDescription());
        playNowButton.setOnAction(event -> MainController.getInstance().showGameDetail(featured));

        buildQuickStats();
        buildSuggestions();
    }

    public void setCurrentUser(Joueur user) {
        buildQuickStats();
    }

    private void buildQuickStats() {
        Joueur user = Session.isLoggedIn() ? Session.getCurrentUser() : store.getCurrentUser();

        int ownedCount  = 0;
        int unreadCount = 0;
        if (user.getUserId() > 0) {
            try {
                ownedCount = UsersJeuxDAO.getOwnedGames(user.getUserId()).size();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                unreadCount = MessageDAO.getUnreadCount(user.getUserId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        quickStatsPane.getChildren().setAll(
            createStatCard("Owned Games",    String.valueOf(ownedCount)),
            createStatCard("Current Rank",   user.getRank()),
            createStatCard("Unread Messages", String.valueOf(unreadCount))
        );
    }

    private VBox createStatCard(String label, String value) {
        Label overline = new Label(label.toUpperCase());
        overline.getStyleClass().add("stat-overline");

        Label bigValue = new Label(value);
        bigValue.getStyleClass().add("stat-value");

        VBox card = new VBox(8, overline, bigValue);
        card.getStyleClass().addAll("dashboard-card", "elevated-1");
        card.setPrefWidth(240);
        return card;
    }

    private void buildSuggestions() {
        aiSuggestionsBox.getChildren().clear();
        for (Jeu jeu : store.getRecommendedGames()) {
            aiSuggestionsBox.getChildren().add(createSuggestionCard(jeu));
        }
    }

    private VBox createSuggestionCard(Jeu jeu) {
        ImageView poster = new ImageView(ZenithArtwork.createPoster(jeu, 220, 300));
        poster.setFitWidth(220);
        poster.setFitHeight(300);
        poster.setPreserveRatio(false);

        Label title = new Label(jeu.getTitre());
        title.getStyleClass().add("game-title");

        Label meta = new Label(jeu.getCategory() + "  |  " + jeu.getDisplayPrice());
        meta.getStyleClass().add("muted-copy");

        Button details = new Button("View Detail");
        details.getStyleClass().addAll("flat", "accent");
        details.setOnAction(event -> MainController.getInstance().showGameDetail(jeu));

        VBox card = new VBox(12, poster, title, meta, details);
        card.setAlignment(Pos.TOP_LEFT);
        card.getStyleClass().addAll("dashboard-card", "suggestion-card", "elevated-1");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }
}
