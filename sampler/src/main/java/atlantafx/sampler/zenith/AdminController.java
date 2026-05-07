package atlantafx.sampler.zenith;

import atlantafx.sampler.zenith.dao.UserDAO;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AdminController implements Initializable {

    @FXML private VBox summaryBox;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private VBox userListBox;

    private final ZenithStore store = ZenithStore.getInstance();
    private List<UserActivityRow> allUsers;
    private Set<Integer> bannedIds = new HashSet<>();
    private boolean banFeatureAvailable = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadData();
        searchField.textProperty().addListener((obs, old, q) -> applyFilter(q.trim()));
    }

    private void loadData() {
        try {
            allUsers = UserDAO.getAllUsersWithActivity();
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Could not load users.");
            return;
        }

        try {
            bannedIds = UserDAO.getBannedUserIds();
        } catch (SQLException e) {
            banFeatureAvailable = false;
            statusLabel.setText(
                "Ban feature disabled — run admin_migration.sql in SSMS to enable it.");
        }

        for (UserActivityRow row : allUsers) {
            row.banned = bannedIds.contains(row.userId);
        }

        buildSummary();
        applyFilter("");
    }

    private void buildSummary() {
        long totalUsers  = allUsers.size();
        long adminCount  = allUsers.stream().filter(r -> r.isAdmin).count();
        long bannedCount = bannedIds.size();
        long activeCount = totalUsers - bannedCount;

        FlowPane row = new FlowPane(16, 12,
            statCard("Total Users",  String.valueOf(totalUsers)),
            statCard("Active",       String.valueOf(activeCount)),
            statCard("Admins",       String.valueOf(adminCount)),
            statCard("Banned",       String.valueOf(bannedCount))
        );
        summaryBox.getChildren().setAll(row);
    }

    private VBox statCard(String label, String value) {
        Label over = new Label(label.toUpperCase());
        over.getStyleClass().add("stat-overline");
        Label big = new Label(value);
        big.getStyleClass().add("stat-value");
        VBox card = new VBox(6, over, big);
        card.getStyleClass().addAll("dashboard-card");
        card.setPrefWidth(180);
        return card;
    }

    private void applyFilter(String query) {
        userListBox.getChildren().clear();
        String q = query.toLowerCase();
        for (UserActivityRow row : allUsers) {
            if (q.isEmpty()
                    || row.nom.toLowerCase().contains(q)
                    || row.email.toLowerCase().contains(q)) {
                userListBox.getChildren().add(buildUserCard(row));
            }
        }
        if (userListBox.getChildren().isEmpty()) {
            Label none = new Label("No users match your search.");
            none.getStyleClass().add("muted-copy");
            userListBox.getChildren().add(none);
        }
    }

    private HBox buildUserCard(UserActivityRow row) {
        // Identity
        Label name = new Label(row.nom);
        name.getStyleClass().add("conversation-name");

        Label email = new Label(row.email);
        email.getStyleClass().add("muted-copy");

        Label rankBadge = new Label(row.rang != null ? row.rang : "Bronze");
        rankBadge.getStyleClass().addAll("rank-badge", RankCalculator.cssClass(row.rang));

        if (row.isAdmin) {
            Label adminBadge = new Label("ADMIN");
            adminBadge.getStyleClass().add("verified-badge");
            VBox identity = new VBox(4, new HBox(8, name, rankBadge, adminBadge), email);
            HBox.setHgrow(identity, Priority.ALWAYS);
            return assembleCard(row, identity);
        }

        VBox identity = new VBox(4, new HBox(8, name, rankBadge), email);
        HBox.setHgrow(identity, Priority.ALWAYS);
        return assembleCard(row, identity);
    }

    private HBox assembleCard(UserActivityRow row, VBox identity) {
        // Activity stats
        String statsText = "Games: " + row.ownedGames
            + "  |  Reviews: " + row.reviewCount
            + "  |  Messages: " + row.messagesSent
            + "  |  Joined: " + row.joinDateFormatted();
        Label stats = new Label(statsText);
        stats.getStyleClass().add("muted-copy");

        VBox infoBlock = new VBox(6, identity, stats);
        HBox.setHgrow(infoBlock, Priority.ALWAYS);

        // Actions
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Joueur currentAdmin = store.getCurrentUser();

        if (!row.isAdmin) {
            Button promoteBtn = new Button("Make Admin");
            promoteBtn.getStyleClass().add("warning");
            promoteBtn.setOnAction(e -> promoteUser(row, promoteBtn));
            actions.getChildren().add(promoteBtn);
        }

        if (!row.isAdmin && row.userId != currentAdmin.getUserId()) {
            if (banFeatureAvailable) {
                Button banBtn = buildBanButton(row);
                actions.getChildren().add(banBtn);
            }
        }

        HBox card = new HBox(16, infoBlock, actions);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("friend-card");

        if (row.banned) {
            card.setStyle("-fx-opacity: 0.55;");
        }

        return card;
    }

    private Button buildBanButton(UserActivityRow row) {
        Button btn = new Button(row.banned ? "Unban" : "Ban");
        btn.getStyleClass().add(row.banned ? "success" : "danger");
        btn.setOnAction(e -> toggleBan(row, btn));
        return btn;
    }

    private void promoteUser(UserActivityRow row, Button btn) {
        Joueur admin = store.getCurrentUser();
        try {
            UserDAO.promoteToAdmin(admin.getUserId(), row.userId);
            btn.setText("Done");
            btn.setDisable(true);
            statusLabel.setText(row.nom + " is now an admin. They must re-login to see the panel.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            statusLabel.setText("Could not promote user.");
        }
    }

    private void toggleBan(UserActivityRow row, Button btn) {
        Joueur admin = store.getCurrentUser();
        try {
            if (row.banned) {
                UserDAO.unbanUser(row.userId);
                row.banned = false;
                bannedIds.remove(row.userId);
                btn.setText("Ban");
                btn.getStyleClass().setAll("button", "danger");
                statusLabel.setText(row.nom + " has been unbanned.");
            } else {
                UserDAO.banUser(row.userId, admin.getUserId());
                row.banned = true;
                bannedIds.add(row.userId);
                btn.setText("Unban");
                btn.getStyleClass().setAll("button", "success");
                statusLabel.setText(row.nom + " has been banned.");
            }
            buildSummary();
        } catch (SQLException ex) {
            ex.printStackTrace();
            statusLabel.setText("Could not update ban status.");
        }
    }
}
