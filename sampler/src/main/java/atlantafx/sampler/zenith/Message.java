package atlantafx.sampler.zenith;

import java.time.LocalDateTime;

final class Message {

    private final String auteur;
    private final String contenu;
    private final LocalDateTime date;
    private final boolean fromCurrentUser;

    Message(String auteur, String contenu, LocalDateTime date, boolean fromCurrentUser) {
        this.auteur = auteur;
        this.contenu = contenu;
        this.date = date;
        this.fromCurrentUser = fromCurrentUser;
    }

    String getAuteur() {
        return auteur;
    }

    String getContenu() {
        return contenu;
    }

    LocalDateTime getDate() {
        return date;
    }

    boolean isFromCurrentUser() {
        return fromCurrentUser;
    }
}
