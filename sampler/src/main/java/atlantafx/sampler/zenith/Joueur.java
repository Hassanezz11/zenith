package atlantafx.sampler.zenith;

import java.util.ArrayList;
import java.util.List;

public class Joueur {

    private int userId;
    private String nom;
    private final String email;
    private String motDePasse;
    private final String rank;
    private final List<Jeu> ownedGames;
    private final List<Jeu> preferedGames;

    public Joueur(
        String nom,
        String email,
        String rank,
        List<Jeu> ownedGames,
        List<Jeu> preferedGames
    ) {
        this(0, nom, email, "", rank, ownedGames, preferedGames);
    }

    public Joueur(
        int userId,
        String nom,
        String email,
        String motDePasse,
        String rank,
        List<Jeu> ownedGames,
        List<Jeu> preferedGames
    ) {
        this.userId = userId;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.rank = rank;
        this.ownedGames = new ArrayList<>(ownedGames);
        this.preferedGames = new ArrayList<>(preferedGames);
    }

    public int getUserId() {
        return userId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    String getRank() {
        return rank;
    }

    List<Jeu> getOwnedGames() {
        return ownedGames;
    }

    List<Jeu> getPreferedGames() {
        return preferedGames;
    }

    boolean isAdministrateur() {
        return false;
    }
}
