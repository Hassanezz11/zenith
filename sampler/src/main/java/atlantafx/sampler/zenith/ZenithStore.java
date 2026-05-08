package atlantafx.sampler.zenith;
import atlantafx.sampler.zenith.dao.JeuDAO;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class ZenithStore {

    private static final ZenithStore INSTANCE = new ZenithStore();

    private final ZenithChatBot chatBot = new ZenithChatBot();
    private List<Jeu> games;
    private final List<Conversation> conversations;
    private final Joueur currentUser;

    private Jeu selectedGame;
    private ProfileTab selectedProfileTab = ProfileTab.OWNED;

    private ZenithStore() {
        // Load games from database
        List<Jeu> loadedGames = new ArrayList<>();
        try {
            loadedGames = JeuDAO.getAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        games = loadedGames;

        // Default user
        currentUser = new Administrateur(
                "Zenith X99",
                "zenith.x99@voidmail.gg",
                "Diamond II",
                new ArrayList<>(),
                new ArrayList<>()
        );

        conversations = new ArrayList<>();
        selectedGame = games.isEmpty() ? null : games.get(0);
    }

    static ZenithStore getInstance() {
        return INSTANCE;
    }

    Joueur getCurrentUser() {
        return Session.isLoggedIn() ? Session.getCurrentUser() : currentUser;
    }

    List<Jeu> getGames() {
        return games;
    }

    List<Conversation> getConversations() {
        return conversations;
    }

    List<Jeu> getRecommendedGames() {
        return chatBot.recommanderDesJeux(currentUser, games);
    }

    List<RawgGame> getRawgGames(String genre) {
        return chatBot.fetchRawgGames(genre);
    }

    List<Jeu> fetchRawgGamesAsJeu(String genre, int count) {
        return chatBot.fetchGamesAsJeu(genre, count, 1);
    }

    List<Jeu> fetchRawgGamesAsJeu(String genre, int count, int page) {
        return chatBot.fetchGamesAsJeu(genre, count, page);
    }

    List<Jeu> searchRawgGamesAsJeu(String query, int page) {
        return chatBot.searchGamesAsJeu(query, page);
    }

    ZenithChatBot getChatBot() {
        return chatBot;
    }

    int getUnviewedMessages() {
        return (int) conversations.stream()
            .map(Conversation::getLastMessage)
            .filter(message -> message != null && !message.isFromCurrentUser())
            .count();
    }

    Jeu getFeaturedGame() {
        return games.isEmpty() ? null : games.get(0);
    }

    List<Jeu> getFilteredGames(String category, String priceFilter, String query) {
        return games.stream()
            .filter(game -> category == null || "All".equalsIgnoreCase(category)
                || game.getCategory().equalsIgnoreCase(category))
            .filter(game -> {
                if (priceFilter == null || "All Prices".equalsIgnoreCase(priceFilter)) {
                    return true;
                }
                if ("Free".equalsIgnoreCase(priceFilter)) {
                    return game.isFree();
                }
                if ("Paid".equalsIgnoreCase(priceFilter)) {
                    return !game.isFree();
                }
                if ("On Sale".equalsIgnoreCase(priceFilter)) {
                    return game.isOnSale();
                }
                return true;
            })
            .filter(game -> game.matchesQuery(query))
            .sorted(Comparator.comparing(Jeu::getTitre))
            .toList();
    }

    Jeu getSelectedGame() {
        return selectedGame;
    }

    void setSelectedGame(Jeu selectedGame) {
        this.selectedGame = selectedGame;
    }

    ProfileTab getSelectedProfileTab() {
        return selectedProfileTab;
    }

    void setSelectedProfileTab(ProfileTab selectedProfileTab) {
        this.selectedProfileTab = selectedProfileTab;
    }
}
