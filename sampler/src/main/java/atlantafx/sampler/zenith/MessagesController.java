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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MessagesController implements Initializable {

    private static final DateTimeFormatter TIME_FMT  = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("MMM dd");

    @FXML private TabPane mainTabPane;
    @FXML private ListView<Conversation> conversationList;
    @FXML private VBox messageContent;
    @FXML private ScrollPane messageScrollPane;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private TextField friendSearchField;
    @FXML private Button searchFriendButton;
    @FXML private VBox searchResultsBox;
    @FXML private VBox pendingRequestsBox;
    @FXML private VBox friendsListBox;

    private final ZenithStore store = ZenithStore.getInstance();
    private ScheduledExecutorService poller;
    private volatile int knownMessageCount = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initDirectMessages();
        loadFriends();
        searchFriendButton.setOnAction(e -> searchFriends());
        friendSearchField.setOnAction(e -> searchFriends());
        mainTabPane.getSelectionModel().selectedIndexProperty()
            .addListener((obs, old, idx) -> { if (idx.intValue() == 1) loadFriends(); });

        // Auto-reload friends list whenever NotificationService detects a change in pending requests
        NotificationService ns = NotificationService.getInstance();
        ns.pendingRequestsProperty().addListener((obs, o, n) -> {
            if (n.intValue() != o.intValue()) Platform.runLater(this::loadFriends);
        });

        startPolling();
        messageContent.sceneProperty().addListener((obs, o, n) -> { if (n == null) stopPolling(); });
    }

    // ── Sidebar init ─────────────────────────────────────────────────────────

    private void initDirectMessages() {
        Joueur me = store.getCurrentUser();
        try {
            List<Joueur> others = UserDAO.getAllOtherUsers(me.getUserId());
            List<Conversation> convs = new ArrayList<>();
            for (Joueur other : others) {
                Conversation conv = new Conversation(other, new ArrayList<>());
                loadPreview(conv, me);
                convs.add(conv);
            }
            // Sort: conversations with messages first, then by unread
            convs.sort((a, b) -> {
                if (a.getLastMessage() == null && b.getLastMessage() != null) return 1;
                if (a.getLastMessage() != null && b.getLastMessage() == null) return -1;
                return Integer.compare(b.getUnreadCount(), a.getUnreadCount());
            });
            conversationList.getItems().setAll(convs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        conversationList.setCellFactory(lv -> new ConversationCell());
        conversationList.getSelectionModel().selectedItemProperty()
            .addListener((obs, oldVal, newVal) -> onConversationSelected(newVal));
        conversationList.getSelectionModel().selectFirst();
        sendButton.setOnAction(ev -> sendMessage());
        messageInput.setOnAction(ev -> sendMessage());
    }

    private void loadPreview(Conversation conv, Joueur me) {
        try {
            String[] last = MessageDAO.getLastMessage(me.getUserId(), conv.getParticipant().getUserId());
            if (last != null) {
                LocalDateTime date = java.sql.Timestamp.valueOf(last[2]).toLocalDateTime();
                boolean mine = last[0].equals(me.getNom());
                conv.getMessages().clear();
                conv.getMessages().add(new Message(last[0], last[1], date, mine));
            }
            int unread = MessageDAO.getUnreadFromSender(me.getUserId(), conv.getParticipant().getUserId());
            conv.setUnreadCount(unread);
        } catch (SQLException ignored) {}
    }

    // ── Conversation open ─────────────────────────────────────────────────────

    private void onConversationSelected(Conversation conv) {
        if (conv == null) { messageContent.getChildren().clear(); return; }
        loadFullConversation(conv);
        markAsRead(conv);
    }

    private void loadFullConversation(Conversation conv) {
        Joueur me = store.getCurrentUser();
        conv.getMessages().clear();
        try {
            List<String[]> rows = MessageDAO.getConversation(me.getUserId(), conv.getParticipant().getUserId());
            for (String[] row : rows) {
                LocalDateTime date = java.sql.Timestamp.valueOf(row[2]).toLocalDateTime();
                boolean mine = row[0].equals(me.getNom());
                conv.getMessages().add(new Message(row[0], row[1], date, mine));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        knownMessageCount = conv.getMessages().size();
        renderMessages(conv);
    }

    private void renderMessages(Conversation conv) {
        messageContent.getChildren().clear();
        if (conv == null) return;
        for (Message msg : conv.getMessages()) {
            messageContent.getChildren().add(createBubble(msg));
        }
        messageScrollPane.layout();
        messageScrollPane.setVvalue(1.0);
    }

    private void markAsRead(Conversation conv) {
        Joueur me = store.getCurrentUser();
        if (me.getUserId() <= 0) return;
        try {
            MessageDAO.markAsRead(me.getUserId(), conv.getParticipant().getUserId());
            conv.setUnreadCount(0);
            Platform.runLater(() -> conversationList.refresh());
            NotificationService.getInstance().refresh();
        } catch (SQLException ignored) {}
    }

    // ── Send ─────────────────────────────────────────────────────────────────

    private void sendMessage() {
        Conversation conv = conversationList.getSelectionModel().getSelectedItem();
        String text = messageInput.getText().trim();
        if (conv == null || text.isEmpty()) return;
        Joueur me = store.getCurrentUser();
        try {
            MessageDAO.save(me.getUserId(), conv.getParticipant().getUserId(), text);
        } catch (SQLException e) { e.printStackTrace(); return; }
        Message sent = new Message(me.getNom(), text, LocalDateTime.now(), true);
        conv.getMessages().add(sent);
        knownMessageCount = conv.getMessages().size();
        messageInput.clear();
        renderMessages(conv);
        conversationList.refresh();
    }

    // ── Polling ───────────────────────────────────────────────────────────────

    private void startPolling() {
        poller = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "zenith-chat-poller");
            t.setDaemon(true);
            return t;
        });
        poller.scheduleAtFixedRate(this::poll, 2, 2, TimeUnit.SECONDS);
    }

    private void stopPolling() {
        if (poller != null) { poller.shutdownNow(); poller = null; }
    }

    private void poll() {
        Joueur me = store.getCurrentUser();
        if (me.getUserId() <= 0) return;
        Conversation selected = conversationList.getSelectionModel().getSelectedItem();
        boolean listNeedsRefresh = false;

        for (Conversation conv : conversationList.getItems()) {
            try {
                int total = MessageDAO.getMessageCount(me.getUserId(), conv.getParticipant().getUserId());
                int known = (conv == selected) ? knownMessageCount : conv.getMessages().size();
                if (total > known) {
                    // Reload preview for sidebar
                    String[] last = MessageDAO.getLastMessage(me.getUserId(), conv.getParticipant().getUserId());
                    if (last != null) {
                        LocalDateTime date = java.sql.Timestamp.valueOf(last[2]).toLocalDateTime();
                        boolean mine = last[0].equals(me.getNom());
                        if (conv != selected) {
                            conv.getMessages().clear();
                            conv.getMessages().add(new Message(last[0], last[1], date, mine));
                        }
                    }
                    int unread = MessageDAO.getUnreadFromSender(me.getUserId(), conv.getParticipant().getUserId());
                    conv.setUnreadCount(unread);
                    listNeedsRefresh = true;

                    if (conv == selected) {
                        final Conversation c = conv;
                        Platform.runLater(() -> {
                            loadFullConversation(c);
                            markAsRead(c);
                        });
                    }
                }
            } catch (SQLException ignored) {}
        }
        if (listNeedsRefresh) {
            Platform.runLater(() -> conversationList.refresh());
        }
    }

    // ── Friends tab ───────────────────────────────────────────────────────────

    private void loadFriends() {
        Joueur me = store.getCurrentUser();
        pendingRequestsBox.getChildren().clear();
        try {
            List<Joueur> pending = FriendRequestDAO.getPendingRequests(me.getUserId());
            if (pending.isEmpty()) {
                pendingRequestsBox.getChildren().add(muted("No pending requests."));
            } else {
                for (Joueur sender : pending)
                    pendingRequestsBox.getChildren().add(createRequestCard(sender, me));
            }
        } catch (SQLException e) {
            pendingRequestsBox.getChildren().add(muted("Friend requests unavailable — run community_schema.sql."));
        }
        friendsListBox.getChildren().clear();
        try {
            List<Joueur> friends = FriendRequestDAO.getFriends(me.getUserId());
            if (friends.isEmpty()) {
                friendsListBox.getChildren().add(muted("No friends yet. Search above to add some!"));
            } else {
                for (Joueur f : friends) friendsListBox.getChildren().add(createFriendCard(f));
            }
        } catch (SQLException e) {
            friendsListBox.getChildren().add(muted("Friends list unavailable — run community_schema.sql."));
        }
    }

    private void searchFriends() {
        String query = friendSearchField.getText().trim();
        searchResultsBox.getChildren().clear();
        if (query.isEmpty()) return;
        Joueur me = store.getCurrentUser();
        try {
            List<Joueur> results = FriendRequestDAO.searchUsers(me.getUserId(), query);
            if (results.isEmpty()) {
                searchResultsBox.getChildren().add(muted("No users found."));
            } else {
                for (Joueur u : results) searchResultsBox.getChildren().add(createSearchResultCard(u, me));
            }
        } catch (SQLException e) {
            searchResultsBox.getChildren().add(muted("Search unavailable."));
        }
    }

    private HBox createRequestCard(Joueur sender, Joueur me) {
        Label avatar = makeAvatar(sender.getNom());
        Label name = new Label(sender.getNom());
        name.getStyleClass().add("conversation-name");
        HBox.setHgrow(name, Priority.ALWAYS);
        Button accept = new Button("Accept"); accept.getStyleClass().add("success");
        Button reject = new Button("Reject"); reject.getStyleClass().add("danger");
        accept.setOnAction(e -> {
            try { FriendRequestDAO.acceptRequest(sender.getUserId(), me.getUserId());
                  NotificationService.getInstance().refresh(); loadFriends();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
        reject.setOnAction(e -> {
            try { FriendRequestDAO.rejectRequest(sender.getUserId(), me.getUserId());
                  NotificationService.getInstance().refresh(); loadFriends();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
        HBox card = new HBox(10, avatar, name, accept, reject);
        card.getStyleClass().add("friend-card"); card.setAlignment(Pos.CENTER_LEFT);
        return card;
    }

    private HBox createFriendCard(Joueur friend) {
        Label avatar = makeAvatar(friend.getNom());
        Label name = new Label(friend.getNom()); name.getStyleClass().add("conversation-name");
        HBox.setHgrow(name, Priority.ALWAYS);
        Label email = new Label(friend.getEmail()); email.getStyleClass().add("muted-copy");
        VBox info = new VBox(2, name, email);
        HBox.setHgrow(info, Priority.ALWAYS);
        HBox card = new HBox(10, avatar, info);
        card.getStyleClass().add("friend-card"); card.setAlignment(Pos.CENTER_LEFT);
        return card;
    }

    private HBox createSearchResultCard(Joueur user, Joueur me) {
        Label avatar = makeAvatar(user.getNom());
        Label name = new Label(user.getNom()); name.getStyleClass().add("conversation-name");
        Label email = new Label(user.getEmail()); email.getStyleClass().add("muted-copy");
        VBox info = new VBox(2, name, email); HBox.setHgrow(info, Priority.ALWAYS);
        Button addBtn = new Button("Add Friend"); addBtn.getStyleClass().add("accent");
        try {
            if (FriendRequestDAO.areFriends(me.getUserId(), user.getUserId())) {
                addBtn.setText("Friends"); addBtn.setDisable(true);
            } else if (FriendRequestDAO.hasPendingRequest(me.getUserId(), user.getUserId())) {
                addBtn.setText("Request Sent"); addBtn.setDisable(true);
            } else {
                addBtn.setOnAction(e -> {
                    try { FriendRequestDAO.sendRequest(me.getUserId(), user.getUserId());
                          addBtn.setText("Request Sent"); addBtn.setDisable(true);
                    } catch (SQLException ex) { ex.printStackTrace(); }
                });
            }
        } catch (SQLException e) { addBtn.setDisable(true); }
        HBox card = new HBox(10, avatar, info, addBtn);
        card.getStyleClass().add("friend-card"); card.setAlignment(Pos.CENTER_LEFT);
        return card;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private VBox createBubble(Message msg) {
        Label copy = new Label(msg.getContenu());
        copy.setWrapText(true);
        copy.getStyleClass().addAll("message-bubble",
            msg.isFromCurrentUser() ? "message-outgoing" : "message-incoming");

        String timeStr = msg.getDate().toLocalDate().equals(java.time.LocalDate.now())
            ? TIME_FMT.format(msg.getDate()) : DATE_FMT.format(msg.getDate());
        Label time = new Label(timeStr);
        time.getStyleClass().add("message-meta");

        VBox wrapper = new VBox(3, copy, time);
        wrapper.setMaxWidth(500);
        wrapper.setAlignment(msg.isFromCurrentUser() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        time.setAlignment(msg.isFromCurrentUser() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        VBox.setMargin(wrapper, msg.isFromCurrentUser()
            ? new Insets(2, 0, 2, 80) : new Insets(2, 80, 2, 0));
        return wrapper;
    }

    private Label makeAvatar(String name) {
        String initial = name != null && !name.isBlank()
            ? String.valueOf(name.charAt(0)).toUpperCase() : "?";
        Label av = new Label(initial);
        av.getStyleClass().add("conv-avatar");
        return av;
    }

    private Label muted(String text) {
        Label l = new Label(text); l.getStyleClass().add("muted-copy"); return l;
    }

    // ── Conversation cell ─────────────────────────────────────────────────────

    private static final class ConversationCell extends ListCell<Conversation> {
        private static final DateTimeFormatter CELL_TIME = DateTimeFormatter.ofPattern("HH:mm");
        private static final DateTimeFormatter CELL_DATE = DateTimeFormatter.ofPattern("MMM d");

        @Override
        protected void updateItem(Conversation item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setText(null); setGraphic(null); return; }

            // Avatar
            String initial = item.getParticipant().getNom() != null
                ? String.valueOf(item.getParticipant().getNom().charAt(0)).toUpperCase() : "?";
            Label avatar = new Label(initial);
            avatar.getStyleClass().add("conv-avatar");

            // Name
            Label name = new Label(item.getParticipant().getNom());
            name.getStyleClass().add("conversation-name");
            if (item.getUnreadCount() > 0) {
                name.setStyle("-fx-font-weight: 900;");
            }

            // Last message preview
            Message last = item.getLastMessage();
            String previewText = last != null ? last.getContenu() : "Start a conversation…";
            if (last != null && last.isFromCurrentUser()) previewText = "You: " + previewText;
            if (previewText.length() > 40) previewText = previewText.substring(0, 38) + "…";
            Label preview = new Label(previewText);
            preview.getStyleClass().add("muted-copy");
            preview.setStyle("-fx-font-size: 11px;");

            // Time label
            Label timeLabel = new Label("");
            if (last != null) {
                String t = last.getDate().toLocalDate().equals(java.time.LocalDate.now())
                    ? CELL_TIME.format(last.getDate()) : CELL_DATE.format(last.getDate());
                timeLabel.setText(t);
                timeLabel.getStyleClass().add("muted-copy");
                timeLabel.setStyle("-fx-font-size: 10px;");
            }

            // Unread badge
            Label badge = new Label(String.valueOf(item.getUnreadCount()));
            badge.getStyleClass().add("conv-unread-badge");
            badge.setVisible(item.getUnreadCount() > 0);
            badge.setManaged(item.getUnreadCount() > 0);

            // Right column: time + badge
            VBox right = new VBox(4, timeLabel, badge);
            right.setAlignment(Pos.TOP_RIGHT);
            right.setMinWidth(40);

            // Center column: name + preview
            VBox center = new VBox(3, name, preview);
            HBox.setHgrow(center, Priority.ALWAYS);

            HBox cell = new HBox(10, avatar, center, right);
            cell.setAlignment(Pos.CENTER_LEFT);
            cell.setPadding(new Insets(6, 4, 6, 4));

            setGraphic(cell);
            setText(null);
        }
    }
}