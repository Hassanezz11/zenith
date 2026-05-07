package atlantafx.sampler.zenith;

import atlantafx.sampler.zenith.dao.UsersJeuxDAO;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class LibraryController implements Initializable {

    @FXML private TabPane libraryTabs;
    @FXML private FlowPane ownedPane;
    @FXML private FlowPane wishlistPane;
    @FXML private Label ownedEmpty;
    @FXML private Label wishlistEmpty;

    private final ZenithStore store = ZenithStore.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Joueur user = Session.isLoggedIn() ? Session.getCurrentUser() : store.getCurrentUser();

        if (user.getUserId() <= 0) {
            ownedEmpty.setVisible(true);
            ownedEmpty.setManaged(true);
            wishlistEmpty.setVisible(true);
            wishlistEmpty.setManaged(true);
            return;
        }

        try {
            List<Jeu> owned = UsersJeuxDAO.getOwnedGames(user.getUserId());
            if (owned.isEmpty()) {
                ownedEmpty.setVisible(true);
                ownedEmpty.setManaged(true);
            } else {
                ownedEmpty.setVisible(false);
                ownedEmpty.setManaged(false);
                owned.forEach(j -> ownedPane.getChildren().add(createTile(j)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ownedEmpty.setText("Could not load library.");
            ownedEmpty.setVisible(true);
            ownedEmpty.setManaged(true);
        }

        try {
            List<Jeu> wishlist = UsersJeuxDAO.getWishlistGames(user.getUserId());
            if (wishlist.isEmpty()) {
                wishlistEmpty.setVisible(true);
                wishlistEmpty.setManaged(true);
            } else {
                wishlistEmpty.setVisible(false);
                wishlistEmpty.setManaged(false);
                wishlist.forEach(j -> wishlistPane.getChildren().add(createTile(j)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            wishlistEmpty.setText("Could not load wishlist.");
            wishlistEmpty.setVisible(true);
            wishlistEmpty.setManaged(true);
        }

        ProfileTab tab = store.getSelectedProfileTab();
        if (tab == ProfileTab.WISHLIST) {
            libraryTabs.getSelectionModel().select(1);
        }
    }

    private VBox createTile(Jeu jeu) {
        ImageView poster = new ImageView(ZenithArtwork.createPoster(jeu, 180, 240));
        poster.setFitWidth(180);
        poster.setFitHeight(240);
        poster.setPreserveRatio(false);

        Label title = new Label(jeu.getTitre());
        title.getStyleClass().add("game-title");
        title.setWrapText(true);
        title.setMaxWidth(180);

        Label meta = new Label(jeu.getCategory());
        meta.getStyleClass().add("muted-copy");

        VBox tile = new VBox(10, poster, title, meta);
        tile.setAlignment(Pos.TOP_LEFT);
        tile.getStyleClass().addAll("inventory-tile", "elevated-1");
        tile.setOnMouseClicked(event -> MainController.getInstance().showGameDetail(jeu));
        return tile;
    }
}
