package atlantafx.sampler.zenith;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ChatBotPopupController implements Initializable {

    @FXML private VBox root;
    @FXML private Button minimizeButton;
    @FXML private Button clearButton;
    @FXML private Button suggestButton;
    @FXML private Button navigateButton;
    @FXML private Button supportButton;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox chatMessagesBox;
    @FXML private TextField chatInput;
    @FXML private Button sendButton;

    private final ZenithStore store = ZenithStore.getInstance();
    private AnthropicChatService aiService;
    private Label typingIndicator;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        aiService = new AnthropicChatService("");
        Joueur user = store.getCurrentUser();
        aiService.initContext(user);

        typingIndicator = new Label("Zenith AI is thinking…");
        typingIndicator.getStyleClass().add("muted-copy");
        typingIndicator.setStyle("-fx-font-style: italic; -fx-font-size: 12px; -fx-padding: 4 8 4 8;");

        sendButton.setOnAction(e -> sendMessage());
        chatInput.setOnAction(e -> sendMessage());
        clearButton.setOnAction(e -> clearChat());

        suggestButton.setOnAction(e -> sendToAi("Suggest games for me based on my preferences."));
        navigateButton.setOnAction(e -> sendToAi("How do I navigate the Zenith platform?"));
        supportButton.setOnAction(e -> sendToAi("What can you help me with? Give me a quick overview."));

        addAiMessage("Hey " + user.getNom() + "! I'm Zenith AI 🎮\nAsk me for game recommendations, platform help, or rank tips!");
    }

    private void sendMessage() {
        String text = chatInput.getText().trim();
        if (text.isEmpty()) return;
        chatInput.clear();
        sendToAi(text);
    }

    private void sendToAi(String userText) {
        addUserMessage(userText);
        setInputEnabled(false);
        showTyping();

        // streaming bubble — created on first token
        Label[] streamBubble = {null};
        StringBuilder content = new StringBuilder();

        CompletableFuture.runAsync(() ->
            aiService.sendStreaming(
                userText,
                token -> Platform.runLater(() -> {
                    if (streamBubble[0] == null) {
                        hideTyping();
                        streamBubble[0] = createStreamingBubble();
                    }
                    content.append(token);
                    streamBubble[0].setText(content.toString());
                    scrollToBottom();
                }),
                () -> Platform.runLater(() -> {
                    store.updateFromChat(userText, content.toString());
                    setInputEnabled(true);
                }),
                err -> Platform.runLater(() -> {
                    hideTyping();
                    addAiMessage(err);
                    setInputEnabled(true);
                })
            )
        );
    }

    private Label createStreamingBubble() {
        Label bubble = new Label("");
        bubble.setWrapText(true);
        bubble.setMaxWidth(250);
        bubble.getStyleClass().add("chat-message-ai");

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(bubble, new Insets(0, 40, 0, 2));

        chatMessagesBox.getChildren().add(row);
        scrollToBottom();
        return bubble;
    }

    private void addUserMessage(String text) {
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(250);
        bubble.getStyleClass().add("chat-message-user");

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_RIGHT);
        HBox.setMargin(bubble, new Insets(0, 2, 0, 40));

        chatMessagesBox.getChildren().add(row);
        scrollToBottom();
    }

    private void addAiMessage(String text) {
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(250);
        bubble.getStyleClass().add("chat-message-ai");

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(bubble, new Insets(0, 40, 0, 2));

        chatMessagesBox.getChildren().add(row);
        scrollToBottom();
    }

    private void showTyping() {
        chatMessagesBox.getChildren().add(typingIndicator);
        scrollToBottom();
    }

    private void hideTyping() {
        chatMessagesBox.getChildren().remove(typingIndicator);
    }

    private void clearChat() {
        chatMessagesBox.getChildren().clear();
        aiService.clearHistory();
        aiService.initContext(store.getCurrentUser());
        addAiMessage("Chat cleared! What can I help you with?");
    }

    private void setInputEnabled(boolean enabled) {
        chatInput.setDisable(!enabled);
        sendButton.setDisable(!enabled);
        suggestButton.setDisable(!enabled);
        navigateButton.setDisable(!enabled);
        supportButton.setDisable(!enabled);
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    void setMinimizeAction(Runnable minimizeAction) {
        minimizeButton.setOnAction(event -> minimizeAction.run());
    }

    void setExpanded(boolean expanded) {
        root.setVisible(expanded);
        root.setManaged(expanded);
    }
}