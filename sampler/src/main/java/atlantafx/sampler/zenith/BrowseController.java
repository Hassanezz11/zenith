package atlantafx.sampler.zenith;

import java.net.URL;
import java.util.ResourceBundle;
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

    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> priceFilter;
    @FXML private FlowPane gamesGrid;

    private final ZenithStore store = ZenithStore.getInstance();
    private String query = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoryFilter.getItems().setAll("All", "RPG", "Shooter", "Racing", "Strategy");
        categoryFilter.getSelectionModel().selectFirst();

        priceFilter.getItems().setAll("All Prices", "Free", "Paid", "On Sale");
        priceFilter.getSelectionModel().selectFirst();

        categoryFilter.setOnAction(event -> refreshGrid());
        priceFilter.setOnAction(event -> refreshGrid());

        refreshGrid();
    }

    @Override
    public void onSearch(String query) {
        this.query = query == null ? "" : query;
        refreshGrid();
    }

    private void refreshGrid() {
        gamesGrid.getChildren().clear();
        for (Jeu jeu : store.getFilteredGames(
            categoryFilter.getValue(),
            priceFilter.getValue(),
            query
        )) {
            gamesGrid.getChildren().add(createGameCard(jeu));
        }
    }

    private VBox createGameCard(Jeu jeu) {
        StackPane posterWrap = new StackPane();
        posterWrap.getStyleClass().add("poster-wrap");

        ImageView poster = new ImageView(ZenithArtwork.createPoster(jeu, 230, 320));
        poster.setFitWidth(230);
        poster.setFitHeight(320);
        poster.setPreserveRatio(false);

        Label badge = new Label(jeu.isOnSale() ? jeu.getPromoLabel() : jeu.getDisplayPrice());
        badge.getStyleClass().addAll("price-badge", jeu.isOnSale() || jeu.isFree() ? "accent" : "flat");
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);

        posterWrap.getChildren().addAll(poster, badge);

        Label title = new Label(jeu.getTitre());
        title.getStyleClass().add("game-title");

        Label category = new Label(jeu.getCategory());
        category.getStyleClass().add("muted-copy");

        Button buyButton = new Button("Acheter");
        buyButton.getStyleClass().add(jeu.getPrix() != null ? "success" : "accent");
        buyButton.setMaxWidth(Double.MAX_VALUE);
        buyButton.setOnAction(event -> MainController.getInstance().showGameDetail(jeu));

        VBox card = new VBox(12, posterWrap, title, category, buyButton);
        card.getStyleClass().addAll("game-card", "elevated-1");
        card.setPrefWidth(250);
        card.setAlignment(Pos.TOP_LEFT);
        card.setOnMouseClicked(event -> MainController.getInstance().showGameDetail(jeu));
        return card;
    }
}
