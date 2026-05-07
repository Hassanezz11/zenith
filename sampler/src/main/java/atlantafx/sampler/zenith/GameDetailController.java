package atlantafx.sampler.zenith;

import atlantafx.sampler.zenith.dao.AvisDAO;
import atlantafx.sampler.zenith.dao.UserDAO;
import atlantafx.sampler.zenith.dao.UsersJeuxDAO;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public class GameDetailController implements Initializable {

    @FXML private ImageView coverImage;
    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label priceLabel;
    @FXML private Button addToLibraryButton;
    @FXML private Button addToWishlistButton;
    @FXML private Label actionStatusLabel;
    @FXML private VBox reviewsBox;
    @FXML private ComboBox<Integer> ratingBox;
    @FXML private TextArea reviewInput;
    @FXML private Button submitReviewButton;
    @FXML private Label reviewStatusLabel;

    private final ZenithStore store = ZenithStore.getInstance();
    private Jeu jeu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        jeu = store.getSelectedGame();

        coverImage.setImage(ZenithArtwork.createPoster(jeu, 360, 470));
        coverImage.setFitWidth(360);
        coverImage.setFitHeight(470);
        coverImage.setPreserveRatio(false);

        titleLabel.setText(jeu.getTitre());
        categoryLabel.setText(jeu.getCategory());
        descriptionLabel.setText(jeu.getDescription());
        priceLabel.setText(jeu.getDisplayPrice());

        ratingBox.getItems().setAll(1, 2, 3, 4, 5);
        ratingBox.getSelectionModel().select(Integer.valueOf(5));

        loadReviews();
        refreshActionButtons();

        addToLibraryButton.setOnAction(e -> addToLibrary());
        addToWishlistButton.setOnAction(e -> addToWishlist());
        submitReviewButton.setOnAction(e -> submitReview());
    }

    private void loadReviews() {
        reviewsBox.getChildren().clear();
        if (jeu.getJeuId() <= 0) return;
        try {
            List<Review> reviews = AvisDAO.getByJeuId(jeu.getJeuId());
            if (reviews.isEmpty()) {
                Label none = new Label("No reviews yet. Be the first!");
                none.getStyleClass().add("muted-copy");
                reviewsBox.getChildren().add(none);
            } else {
                for (Review r : reviews) {
                    reviewsBox.getChildren().add(createReviewNode(r));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshActionButtons() {
        Joueur user = store.getCurrentUser();
        if (user.getUserId() <= 0 || jeu.getJeuId() <= 0) {
            addToLibraryButton.setText(jeu.isFree() ? "Get Free" : "Buy " + jeu.getDisplayPrice());
            return;
        }
        try {
            boolean owned      = UsersJeuxDAO.isOwned(user.getUserId(), jeu.getJeuId());
            boolean wishlisted = UsersJeuxDAO.isWishlisted(user.getUserId(), jeu.getJeuId());
            if (owned) {
                addToLibraryButton.setText("In Library");
                addToLibraryButton.setDisable(true);
                addToWishlistButton.setDisable(true);
            } else {
                addToLibraryButton.setText(jeu.isFree() ? "Get Free" : "Buy " + jeu.getDisplayPrice());
                if (wishlisted) {
                    addToWishlistButton.setText("In Wishlist");
                    addToWishlistButton.setDisable(true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addToLibrary() {
        Joueur user = store.getCurrentUser();
        if (user.getUserId() <= 0) { actionStatusLabel.setText("Please log in first."); return; }
        if (jeu.getJeuId() <= 0)   { actionStatusLabel.setText("Game not found in DB."); return; }
        try {
            UsersJeuxDAO.addToLibrary(user.getUserId(), jeu.getJeuId());
            recalculateRank(user.getUserId());
            actionStatusLabel.setText("Added to your library!");
            addToLibraryButton.setText("In Library");
            addToLibraryButton.setDisable(true);
            addToWishlistButton.setDisable(true);
        } catch (SQLException e) {
            e.printStackTrace();
            actionStatusLabel.setText("Could not add to library.");
        }
    }

    private void recalculateRank(int userId) {
        try {
            int count = UsersJeuxDAO.countOwned(userId);
            String newRank = RankCalculator.compute(count);
            UserDAO.updateRank(userId, newRank);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addToWishlist() {
        Joueur user = store.getCurrentUser();
        if (user.getUserId() <= 0) { actionStatusLabel.setText("Please log in first."); return; }
        if (jeu.getJeuId() <= 0)   { actionStatusLabel.setText("Game not found in DB."); return; }
        try {
            UsersJeuxDAO.addToWishlist(user.getUserId(), jeu.getJeuId());
            actionStatusLabel.setText("Added to wishlist!");
            addToWishlistButton.setText("In Wishlist");
            addToWishlistButton.setDisable(true);
        } catch (SQLException e) {
            e.printStackTrace();
            actionStatusLabel.setText("Could not add to wishlist.");
        }
    }

    private void submitReview() {
        Joueur user = store.getCurrentUser();
        if (user.getUserId() <= 0) { reviewStatusLabel.setText("Please log in to review."); return; }
        if (jeu.getJeuId() <= 0)   { reviewStatusLabel.setText("Game not found in DB."); return; }
        Integer rating  = ratingBox.getValue();
        String  comment = reviewInput.getText().trim();
        if (rating == null || comment.isEmpty()) {
            reviewStatusLabel.setText("Please select a rating and write a comment.");
            return;
        }
        try {
            AvisDAO.save(jeu.getJeuId(), user.getUserId(), rating, comment);
            reviewInput.clear();
            reviewStatusLabel.setText("Review submitted!");
            loadReviews();
        } catch (SQLException e) {
            e.printStackTrace();
            reviewStatusLabel.setText("Could not submit review.");
        }
    }

    private VBox createReviewNode(Review review) {
        Label author = new Label(review.getAuteur());
        author.getStyleClass().add("review-author");

        Label rankBadge = new Label(review.getRang() != null ? review.getRang() : "Bronze");
        rankBadge.getStyleClass().addAll("rank-badge", RankCalculator.cssClass(review.getRang()));

        HBox header = new HBox(8, author, rankBadge);
        header.setAlignment(Pos.CENTER_LEFT);

        if (review.isVerified()) {
            Label verified = new Label("Verified Purchase");
            verified.getStyleClass().add("verified-badge");
            header.getChildren().add(verified);
        }

        HBox ratingRow = new HBox(4);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < 5; i++) {
            FontIcon star = new FontIcon(Feather.STAR);
            star.getStyleClass().add(i < review.getRating() ? "review-star-active" : "review-star");
            ratingRow.getChildren().add(star);
        }

        Label comment = new Label(review.getComment());
        comment.setWrapText(true);
        comment.getStyleClass().add("review-bubble");
        return new VBox(8, header, ratingRow, comment);
    }
}
