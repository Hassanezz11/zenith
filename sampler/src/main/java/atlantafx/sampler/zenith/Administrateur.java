package atlantafx.sampler.zenith;

import java.util.List;

public final class Administrateur extends Joueur {

    public Administrateur(
        String nom,
        String email,
        String rank,
        List<Jeu> ownedGames,
        List<Jeu> preferedGames
    ) {
        super(nom, email, rank, ownedGames, preferedGames);
    }

    public Administrateur(
        int userId,
        String nom,
        String email,
        String motDePasse,
        String rank,
        List<Jeu> ownedGames,
        List<Jeu> preferedGames
    ) {
        super(userId, nom, email, motDePasse, rank, ownedGames, preferedGames);
    }

    @Override
    boolean isAdministrateur() {
        return true;
    }

    String gererLesJeux() {
        return "Game moderation queue loaded. 6 titles need publishing review.";
    }

    String gererLesUtilisateurs() {
        return "User integrity center ready. 3 reports require attention.";
    }
}
