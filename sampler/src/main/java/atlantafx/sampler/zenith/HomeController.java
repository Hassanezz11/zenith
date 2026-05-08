package atlantafx.sampler.zenith;

import atlantafx.sampler.zenith.dao.MessageDAO;
import atlantafx.sampler.zenith.dao.UsersJeuxDAO;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
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
        if (featured != null) {
            heroImage.setImage(ZenithArtwork.createHero(featured, 1100, 340));
            heroTitle.setText(featured.getTitre());
            heroSubtitle.setText(featured.getDescription());
            playNowButton.setOnAction(event -> MainController.getInstance().showGameDetail(featured));
        }

        buildQuickStats();
        buildSuggestions();
        loadHeroFromRawg();
    }

    private void loadHeroFromRawg() {
        CompletableFuture.supplyAsync(() -> store.fetchRawgGamesAsJeu("action", 1))
            .thenAccept(games -> Platform.runLater(() -> {
                if (!games.isEmpty()) {
                    Jeu featured = games.get(0);
                    heroTitle.setText(featured.getTitre());
                    heroSubtitle.setText(featured.getDescription());
                    ZenithArtwork.loadImageAsync(featured.getBackgroundImageUrl(), 1100, 340, heroImage);
                    playNowButton.setOnAction(event -> {
                        store.setSelectedGame(featured);
                        MainController.getInstance().showGameDetail(featured);
                    });
                }
            }));
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
        Label loading = new Label("Loading top games from RAWG.io...");
        loading.getStyleClass().add("muted-copy");
        aiSuggestionsBox.getChildren().add(loading);

        Joueur user = Session.isLoggedIn() ? Session.getCurrentUser() : store.getCurrentUser();
        List<Jeu> preferred = user.getPreferedGames();
        String genre = preferred.isEmpty() ? "action" : preferred.get(0).getCategory();

        CompletableFuture.supplyAsync(() -> store.getRawgGames(genre))
            .thenAccept(rawgGames -> Platform.runLater(() -> {
                aiSuggestionsBox.getChildren().clear();
                if (rawgGames.isEmpty()) {
                    aiSuggestionsBox.getChildren().add(new Label("No recommendations available."));
                } else {
                    rawgGames.forEach(g -> aiSuggestionsBox.getChildren().add(createRawgCard(g)));
                }
            }));
    }

    private VBox createRawgCard(RawgGame game) {
        ImageView poster = new ImageView();
        poster.setFitWidth(220);
        poster.setFitHeight(300);
        poster.setPreserveRatio(false);
        ZenithArtwork.loadImageAsync(game.backgroundImageUrl(), 220, 300, poster);

        Label title = new Label(game.name());
        title.getStyleClass().add("game-title");
        title.setWrapText(true);
        title.setMaxWidth(220);

        Label meta = new Label(game.genre().toUpperCase() + "  |  ★ " + String.format("%.1f", game.rating()));
        meta.getStyleClass().add("muted-copy");

        Label released = new Label("Released: " + game.released());
        released.getStyleClass().add("muted-copy");

        VBox card = new VBox(12, poster, title, meta, released);
        card.setAlignment(Pos.TOP_LEFT);
        card.getStyleClass().addAll("dashboard-card", "suggestion-card", "elevated-1");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }
}
