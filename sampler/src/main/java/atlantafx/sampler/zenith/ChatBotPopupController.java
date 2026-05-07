package atlantafx.sampler.zenith;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class ChatBotPopupController implements Initializable {

    @FXML private VBox root;
    @FXML private Button minimizeButton;
    @FXML private Button communityButton;
    @FXML private Button supportButton;
    @FXML private TextArea outputArea;

    private final ZenithStore store = ZenithStore.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        communityButton.setOnAction(event -> outputArea.setText(store.getChatBot().animerLaCommunaute()));
        supportButton.setOnAction(event -> outputArea.setText(
            store.getChatBot().assisterJoueur(store.getCurrentUser())
        ));
        outputArea.setText(store.getChatBot().animerLaCommunaute());
    }

    void setMinimizeAction(Runnable minimizeAction) {
        minimizeButton.setOnAction(event -> minimizeAction.run());
    }

    void setExpanded(boolean expanded) {
        root.setVisible(expanded);
        root.setManaged(expanded);
    }
}
