package atlantafx.sampler.zenith;

import java.util.List;

final class ZenithChatBot {

    private static final String RAWG_API_KEY = "5333d71d419145399087b9b8da4a1d20";
    private final RawgApiService rawgService = new RawgApiService(RAWG_API_KEY);

    List<Jeu> recommanderDesJeux(Joueur joueur, List<Jeu> jeux) {
        return jeux.stream()
            .filter(game -> joueur.getPreferedGames().stream()
                .anyMatch(preferred -> preferred.getCategory().equalsIgnoreCase(game.getCategory())))
            .filter(game -> joueur.getOwnedGames().stream()
                .noneMatch(owned -> owned.getTitre().equalsIgnoreCase(game.getTitre())))
            .limit(3)
            .toList();
    }

    List<RawgGame> fetchRawgGames(String genre) {
        return rawgService.fetchTopGamesByGenreAsObjects(genre);
    }

    List<Jeu> fetchGamesAsJeu(String genre, int count) {
        return rawgService.fetchGamesAsJeu(genre, count, 1);
    }

    List<Jeu> fetchGamesAsJeu(String genre, int count, int page) {
        return rawgService.fetchGamesAsJeu(genre, count, page);
    }

    List<Jeu> searchGamesAsJeu(String query, int page) {
        return rawgService.searchGamesAsJeu(query, page);
    }

    String recommanderDepuisRawg(Joueur joueur) {
        List<Jeu> preferred = joueur.getPreferedGames();
        String genre = preferred.isEmpty() ? "action" : preferred.get(0).getCategory();
        List<String> rawgGames = rawgService.fetchTopGamesByGenre(genre);

        StringBuilder sb = new StringBuilder();
        sb.append("Top rated ").append(genre).append(" games from RAWG.io:\n\n");
        rawgGames.forEach(line -> sb.append(line).append("\n"));
        return sb.toString();
    }

    String animerLaCommunaute() {
        return """
            Community pulse is active.
            - 3 guild tournaments are filling tonight.
            - Ranked scrims begin in 22 minutes.
            - Void Protocol squads are requesting support mains.
            """;
    }

    String assisterJoueur(Joueur joueur) {
        return """
            Technical support ready.
            - Cloud sync is healthy for %s.
            - GPU preset recommendation: Ultra textures, High shadows.
            - Next maintenance window: Sunday 02:00 UTC.
            """.formatted(joueur.getNom());
    }
}
