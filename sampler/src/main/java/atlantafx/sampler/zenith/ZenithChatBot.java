package atlantafx.sampler.zenith;

import java.util.List;

final class ZenithChatBot {

    List<Jeu> recommanderDesJeux(Joueur joueur, List<Jeu> jeux) {
        return jeux.stream()
            .filter(game -> joueur.getPreferedGames().stream()
                .anyMatch(preferred -> preferred.getCategory().equalsIgnoreCase(game.getCategory())))
            .filter(game -> joueur.getOwnedGames().stream()
                .noneMatch(owned -> owned.getTitre().equalsIgnoreCase(game.getTitre())))
            .limit(3)
            .toList();
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
