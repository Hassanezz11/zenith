package atlantafx.sampler.zenith;

import atlantafx.sampler.zenith.dao.FriendRequestDAO;
import atlantafx.sampler.zenith.dao.MessageDAO;
import atlantafx.sampler.zenith.dao.UserDAO;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MessagesController implements Initializable {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("MMM dd  HH:mm");

    @FXML private TabPane mainTabPane;

    // Direct Messages
    @FXML private ListView<Conversation> conversationList;
    @FXML private VBox messageContent;
    @FXML private ScrollPane messageScrollPane;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;

    // Friends
    @FXML private TextField friendSearchField;
    @FXML private Button searchFriendButton;
    @FXML private VBox searchResultsBox;
    @FXML private VBox pendingRequestsBox;
    @FXML private VBox friendsListBox;

    private final ZenithStore store = ZenithStore.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initDirectMessages();
        loadFriends();
        searchFriendButton.setOnAction(e -> searchFriends());
        friendSearchField.setOnAction(e -> searchFriends());

        mainTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, old, idx) -> {
            if (idx.intValue() == 1) loadFriends();
        });
    }

    // ── Direct Messages ──────────────────────────────────────────────────────

    private void initDirectMessages() {
        Joueur currentUser = store.getCurrentUser();
        try {
            List<Joueur> others = UserDAO.getAllOtherUsers(currentUser.getUserId());
            List<Conversation> convs = new ArrayList<>();
            for (Joueur other : others) {
                convs.add(new Conversation(other, new ArrayList<>()));
            }
            conversationList.getItems().setAll(convs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        conversationList.setCellFactory(list -> new ConversationCell());
        conversationList.getSelectionModel().selectedItemProperty()
            .addListener((obs, oldVal, newVal) -> loadAndRenderConversation(newVal));
        conversationList.getSelectionModel().selectFirst();
        sendButton.setOnAction(event -> sendMessage());
        messageInput.setOnAction(event -> sendMessage());
    }

    private void loadAndRenderConversation(Conversation conversation) {
        if (conversation == null) {
            messageContent.getChildren().clear();
            return;
        }
        Joueur currentUser = store.getCurrentUser();
        conversation.getMessages().clear();
        try {
            List<String[]> rows = MessageDAO.getConversation(
                currentUser.getUserId(), conversation.getParticipant().getUserId());
            for (String[] row : rows) {
                String auteur  = row[0];
                String contenu = row[1];
                LocalDateTime date = java.sql.Timestamp.valueOf(row[2]).toLocalDateTime();
                boolean mine = auteur.equals(currentUser.getNom());
                conversation.getMessages().add(new Message(auteur, contenu, date, mine));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        renderConversation(conversation);
    }

    private void renderConversation(Conversation conversation) {
        messageContent.getChildren().clear();
        if (conversation == null) return;
        for (Message msg : conversation.getMessages()) {
            messageContent.getChildren().add(createBubble(msg));
        }
        messageScrollPane.layout();
        messageScrollPane.setVvalue(1.0);
    }

    private void sendMessage() {
        Conversation conv = conversationList.getSelectionModel().getSelectedItem();
        String input = messageInput.getText().trim();
        if (conv == null || input.isEmpty()) return;
        Joueur currentUser = store.getCurrentUser();
        try {
            MessageDAO.save(currentUser.getUserId(), conv.getParticipant().getUserId(), input);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        conv.getMessages().add(new Message(currentUser.getNom(), input, LocalDateTime.now(), true));
        messageInput.clear();
        renderConversation(conv);
        conversationList.refresh();
    }

    // ── Friends ───────────────────────────────────────────────────────────────

    private void loadFriends() {
        Joueur currentUser = store.getCurrentUser();

        pendingRequestsBox.getChildren().clear();
        try {
            List<Joueur> pending = FriendRequestDAO.getPendingRequests(currentUser.getUserId());
            if (pending.isEmpty()) {
                pendingRequestsBox.getChildren().add(muted("No pending requests."));
            } else {
                for (Joueur sender : pending) {
                    pendingRequestsBox.getChildren().add(createRequestCard(sender, currentUser));
                }
            }
        } catch (SQLException e) {
            pendingRequestsBox.getChildren().add(muted("Friend requests unavailable — run community_schema.sql in SSMS."));
        }

        friendsListBox.getChildren().clear();
        try {
            List<Joueur> friends = FriendRequestDAO.getFriends(currentUser.getUserId());
            if (friends.isEmpty()) {
                friendsListBox.getChildren().add(muted("No friends yet. Search above to add some!"));
            } else {
                for (Joueur friend : friends) {
                    friendsListBox.getChildren().add(createFriendCard(friend));
                }
            }
        } catch (SQLException e) {
            friendsListBox.getChildren().add(muted("Friends list unavailable — run community_schema.sql in SSMS."));
        }
    }

    private void searchFriends() {
        String query = friendSearchField.getText().trim();
        searchResultsBox.getChildren().clear();
        if (query.isEmpty()) return;
        Joueur currentUser = store.getCurrentUser();
        try {
            List<Joueur> results = FriendRequestDAO.searchUsers(currentUser.getUserId(), query);
            if (results.isEmpty()) {
                searchResultsBox.getChildren().add(muted("No users found."));
            } else {
                for (Joueur user : results) {
                    searchResultsBox.getChildren().add(createSearchResultCard(user, currentUser));
                }
            }
        } catch (SQLException e) {
            searchResultsBox.getChildren().add(muted("Search unavailable — run community_schema.sql in SSMS."));
        }
    }

    private HBox createRequestCard(Joueur sender, Joueur currentUser) {
        Label name = new Label(sender.getNom());
        name.getStyleClass().add("conversation-name");
        HBox.setHgrow(name, Priority.ALWAYS);

        Button accept = new Button("Accept");
        accept.getStyleClass().add("success");
        accept.setOnAction(e -> {
            try {
                FriendRequestDAO.acceptRequest(sender.getUserId(), currentUser.getUserId());
                loadFriends();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        Button reject = new Button("Reject");
        reject.getStyleClass().add("danger");
        reject.setOnAction(e -> {
            try {
                FriendRequestDAO.rejectRequest(sender.getUserId(), currentUser.getUserId());
                loadFriends();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        HBox card = new HBox(12, name, accept, reject);
        card.getStyleClass().add("friend-card");
        card.setAlignment(Pos.CENTER_LEFT);
        return card;
    }

    private HBox createFriendCard(Joueur friend) {
        Label name = new Label(friend.getNom());
        name.getStyleClass().add("conversation-name");
        HBox.setHgrow(name, Priority.ALWAYS);

        Label email = new Label(friend.getEmail());
        email.getStyleClass().add("muted-copy");

        HBox card = new HBox(12, name, email);
        card.getStyleClass().add("friend-card");
        card.setAlignment(Pos.CENTER_LEFT);
        return card;
    }

    private HBox createSearchResultCard(Joueur user, Joueur currentUser) {
        Label name  = new Label(user.getNom());
        name.getStyleClass().add("conversation-name");
        Label email = new Label(user.getEmail());
        email.getStyleClass().add("muted-copy");
        VBox info = new VBox(2, name, email);
        HBox.setHgrow(info, Priority.ALWAYS);

        Button addBtn = new Button("Add Friend");
        addBtn.getStyleClass().add("accent");
        try {
            if (FriendRequestDAO.areFriends(currentUser.getUserId(), user.getUserId())) {
                addBtn.setText("Friends");
                addBtn.setDisable(true);
            } else if (FriendRequestDAO.hasPendingRequest(currentUser.getUserId(), user.getUserId())) {
                addBtn.setText("Request Sent");
                addBtn.setDisable(true);
            } else {
                addBtn.setOnAction(e -> {
                    try {
                        FriendRequestDAO.sendRequest(currentUser.getUserId(), user.getUserId());
                        addBtn.setText("Request Sent");
                        addBtn.setDisable(true);
                    } catch (SQLException ex) { ex.printStackTrace(); }
                });
            }
        } catch (SQLException e) {
            addBtn.setDisable(true);
        }

        HBox card = new HBox(12, info, addBtn);
        card.getStyleClass().add("friend-card");
        card.setAlignment(Pos.CENTER_LEFT);
        return card;
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    private VBox createBubble(Message message) {
        Label meta = new Label(message.getAuteur() + "  |  " + TIME_FORMAT.format(message.getDate()));
        meta.getStyleClass().add("message-meta");

        Label copy = new Label(message.getContenu());
        copy.setWrapText(true);
        copy.getStyleClass().addAll("message-bubble",
            message.isFromCurrentUser() ? "message-outgoing" : "message-incoming");

        VBox wrapper = new VBox(6, meta, copy);
        wrapper.setMaxWidth(480);
        wrapper.setAlignment(message.isFromCurrentUser() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return wrapper;
    }

    private Label muted(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("muted-copy");
        return l;
    }

    private static final class ConversationCell extends ListCell<Conversation> {
        @Override
        protected void updateItem(Conversation item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            Label name = new Label(item.getParticipant().getNom());
            name.getStyleClass().add("conversation-name");
            String preview = item.getLastMessage() == null
                ? "No messages yet." : item.getLastMessage().getContenu();
            Label previewLabel = new Label(preview);
            previewLabel.getStyleClass().add("muted-copy");
            previewLabel.setWrapText(true);
            setGraphic(new VBox(4, name, previewLabel));
        }
    }
}
