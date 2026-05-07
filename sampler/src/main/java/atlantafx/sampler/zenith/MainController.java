package atlantafx.sampler.zenith;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public class MainController implements Initializable {

    private static MainController instance;

    private static final String FXML_BASE = "/zenith/fxml/";
    private static final String FXML_HOME = FXML_BASE + "Home.fxml";
    private static final String FXML_BROWSE = FXML_BASE + "Browse.fxml";
    private static final String FXML_DETAIL = FXML_BASE + "GameDetail.fxml";
    private static final String FXML_MESSAGES = FXML_BASE + "Messages.fxml";
    private static final String FXML_PROFILE = FXML_BASE + "Profile.fxml";
    private static final String FXML_LIBRARY = FXML_BASE + "Library.fxml";

    @FXML private StackPane contentArea;
    @FXML private Button btnHome;
    @FXML private Button btnBrowse;
    @FXML private Button btnLibrary;
    @FXML private Button btnMessages;
    @FXML private Button btnProfile;
    @FXML private Button chatLauncherButton;
    @FXML private TextField searchBar;
    @FXML private Label lblPageTitle;
    @FXML private MenuButton userMenuButton;
    @FXML private ChatBotPopupController chatPopupController;

    private final ZenithStore store = ZenithStore.getInstance();
    private Button activeNavBtn;
    private SearchAware searchAwareController;
    private boolean chatExpanded;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        configureIcons();
        configureUserMenu();
        chatPopupController.setMinimizeAction(this::toggleChatOverlay);
        chatPopupController.setExpanded(false);
        activeNavBtn = btnHome;
        switchView(FXML_HOME);
        searchBar.textProperty().addListener((obs, oldVal, newVal) -> onSearch(newVal));
    }

    @FXML
    private void onHome() {
        setActiveNavBtn(btnHome);
        lblPageTitle.setText("Home");
        switchView(FXML_HOME);
    }

    @FXML
    private void onBrowse() {
        setActiveNavBtn(btnBrowse);
        lblPageTitle.setText("Browse Games");
        switchView(FXML_BROWSE);
    }

    @FXML
    private void onLibrary() {
        store.setSelectedProfileTab(ProfileTab.OWNED);
        setActiveNavBtn(btnLibrary);
        lblPageTitle.setText("My Library");
        switchView(FXML_LIBRARY);
    }

    @FXML
    private void onMessages() {
        setActiveNavBtn(btnMessages);
        lblPageTitle.setText("Messages");
        switchView(FXML_MESSAGES);
    }

    @FXML
    private void onProfile() {
        setActiveNavBtn(btnProfile);
        lblPageTitle.setText("Profile");
        switchView(FXML_PROFILE);
    }

    @FXML
    private void onToggleChat() {
        toggleChatOverlay();
    }

    public void switchView(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                throw new IllegalStateException("FXML not found: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Node view = loader.load();
            Object controller = loader.getController();
            searchAwareController = controller instanceof SearchAware searchable ? searchable : null;

            contentArea.getChildren().setAll(view);

            FadeTransition fade = new FadeTransition(Duration.millis(180), view);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();

            onSearch(searchBar.getText());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load Zenith view: " + fxmlPath, e);
        }
    }

    public void showGameDetail(Jeu jeu) {
        store.setSelectedGame(jeu);
        lblPageTitle.setText("Game Detail");
        switchView(FXML_DETAIL);
    }

    static MainController getInstance() {
        return instance;
    }

    private void configureIcons() {
        btnHome.setGraphic(new FontIcon(Feather.HOME));
        btnBrowse.setGraphic(new FontIcon(Feather.GRID));
        btnLibrary.setGraphic(new FontIcon(Feather.BOOK_OPEN));
        btnMessages.setGraphic(new FontIcon(Feather.MESSAGE_SQUARE));
        btnProfile.setGraphic(new FontIcon(Feather.USER));
        chatLauncherButton.setGraphic(new FontIcon(Feather.MESSAGE_CIRCLE));
    }

    public void setCurrentUser(Joueur user) {
        configureUserMenu();
    }

    private void configureUserMenu() {
        Joueur user = Session.isLoggedIn() ? Session.getCurrentUser() : store.getCurrentUser();
        userMenuButton.setText(user.getNom());
        MenuItem signOut = new MenuItem("Sign Out");
        signOut.setOnAction(e -> {
            Session.logout();
            ZenithApp.showLogin();
        });
        userMenuButton.getItems().setAll(
            new MenuItem("Preferences"),
            new MenuItem("Notifications"),
            signOut
        );
    }

    private void setActiveNavBtn(Button next) {
        if (activeNavBtn != null) {
            activeNavBtn.getStyleClass().remove("nav-button-active");
        }
        next.getStyleClass().add("nav-button-active");
        activeNavBtn = next;
    }

    private void onSearch(String query) {
        if (searchAwareController != null) {
            searchAwareController.onSearch(query);
        }
    }

    private void toggleChatOverlay() {
        chatExpanded = !chatExpanded;
        chatPopupController.setExpanded(chatExpanded);
    }
}
