package atlantafx.sampler.zenith;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class BrowseController implements Initializable, SearchAware {

    private static final int PAGE_SIZE = 40;

    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> priceFilter;
    @FXML private FlowPane gamesGrid;
    @FXML private Button loadMoreButton;

    private final ZenithStore store = ZenithStore.getInstance();
    private String currentGenre = "action";
    private String currentQuery = "";
    private boolean searchMode = false;
    private int currentPage = 1;
    private boolean loading = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoryFilter.getItems().setAll(
            "All", "Action", "RPG", "Strategy", "Shooter",
            "Adventure", "Racing", "Sports", "Puzzle", "Simulation", "Indie"
        );
        categoryFilter.getSelectionModel().selectFirst();
        priceFilter.getItems().setAll("All Prices", "Free", "Paid", "On Sale");
        priceFilter.getSelectionModel().selectFirst();

        categoryFilter.setOnAction(e -> {
            String sel = categoryFilter.getValue();
            currentGenre = (sel == null || sel.equals("All")) ? "action" : sel;
            searchMode = false;
            currentQuery = "";
            resetAndLoad();
        });
        priceFilter.setOnAction(e -> applyPriceFilter());

        loadMoreButton.setVisible(false);
        loadMoreButton.setManaged(false);
        loadMoreButton.setOnAction(e -> loadNextPage());

        resetAndLoad();
    }

    @Override
    public void onSearch(String query) {
        currentQuery = query == null ? "" : query.trim();
        if (currentQuery.isBlank()) {
            searchMode = false;
            resetAndLoad();
        } else {
            searchMode = true;
            currentPage = 1;
            gamesGrid.getChildren().clear();
            setLoadingLabel("Searching RAWG.io for \"" + currentQuery + "\"...");
            fetchPage(currentPage, true);
        }
    }

    private void resetAndLoad() {
        currentPage = 1;
        gamesGrid.getChildren().clear();
        setLoadingLabel("Loading " + currentGenre + " games from RAWG.io...");
        fetchPage(1, true);
    }

    private void loadNextPage() {
        if (loading) return;
        currentPage++;
        fetchPage(currentPage, false);
    }

    private void fetchPage(int page, boolean replace) {
        loading = true;
        loadMoreButton.setDisable(true);

        CompletableFuture.supplyAsync(() -> searchMode
                ? store.searchRawgGamesAsJeu(currentQuery, page)
                : store.fetchRawgGamesAsJeu(currentGenre, PAGE_SIZE, page))
            .thenAccept(games -> Platform.runLater(() -> {
                loading = false;
                if (replace) {
                    gamesGrid.getChildren().clear();
                }
                if (games.isEmpty() && replace) {
                    setLoadingLabel("No games found.");
                } else {
                    applyPriceFilterTo(games);
                }
                boolean hasMore = !games.isEmpty();
                loadMoreButton.setVisible(hasMore);
                loadMoreButton.setManaged(hasMore);
                loadMoreButton.setDisable(false);
            }));
    }

    private void applyPriceFilter() {
        // re-render current games with new price filter — just reload
        resetAndLoad();
    }

    private void applyPriceFilterTo(List<Jeu> games) {
        String price = priceFilter.getValue();
        List<Jeu> filtered = games.stream()
            .filter(j -> {
                if (price == null || "All Prices".equals(price)) return true;
                if ("Free".equals(price))    return j.isFree();
                if ("Paid".equals(price))    return !j.isFree();
                if ("On Sale".equals(price)) return j.isOnSale();
                return true;
            })
            .toList();
        filtered.forEach(j -> gamesGrid.getChildren().add(createGameCard(j)));
    }

    private void setLoadingLabel(String msg) {
        Label lbl = new Label(msg);
        lbl.getStyleClass().add("muted-copy");
        gamesGrid.getChildren().setAll(lbl);
    }

    private VBox createGameCard(Jeu jeu) {
        StackPane posterWrap = new StackPane();
        posterWrap.getStyleClass().add("poster-wrap");

        ImageView poster = new ImageView(ZenithArtwork.createPoster(jeu, 230, 320));
        poster.setFitWidth(230);
        poster.setFitHeight(320);
        poster.setPreserveRatio(false);
        ZenithArtwork.loadImageAsync(jeu.getBackgroundImageUrl(), 230, 320, poster);

        Label badge = new Label(jeu.isOnSale() ? jeu.getPromoLabel() : jeu.getDisplayPrice());
        badge.getStyleClass().addAll("price-badge", jeu.isOnSale() || jeu.isFree() ? "accent" : "flat");
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        posterWrap.getChildren().addAll(poster, badge);

        Label title = new Label(jeu.getTitre());
        title.getStyleClass().add("game-title");
        title.setWrapText(true);
        title.setMaxWidth(230);

        Label category = new Label(jeu.getCategory());
        category.getStyleClass().add("muted-copy");

        Button buyButton = new Button(jeu.isFree() ? "Get Free" : "Buy " + jeu.getDisplayPrice());
        buyButton.getStyleClass().add(jeu.isFree() ? "accent" : "success");
        buyButton.setMaxWidth(Double.MAX_VALUE);
        buyButton.setOnAction(e -> openDetail(jeu));

        VBox card = new VBox(12, posterWrap, title, category, buyButton);
        card.getStyleClass().addAll("game-card", "elevated-1");
        card.setPrefWidth(250);
        card.setAlignment(Pos.TOP_LEFT);
        card.setOnMouseClicked(e -> openDetail(jeu));
        return card;
    }

    private void openDetail(Jeu jeu) {
        store.setSelectedGame(jeu);
        MainController.getInstance().showGameDetail(jeu);
    }
}