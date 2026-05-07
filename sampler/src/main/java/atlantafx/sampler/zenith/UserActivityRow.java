package atlantafx.sampler.zenith;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class UserActivityRow {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    final int userId;
    final String nom;
    final String email;
    final String rang;
    final boolean isAdmin;
    final int ownedGames;
    final int reviewCount;
    final int messagesSent;
    final LocalDateTime joinDate;
    boolean banned;

    public UserActivityRow(int userId, String nom, String email, String rang, boolean isAdmin,
                           int ownedGames, int reviewCount, int messagesSent, LocalDateTime joinDate) {
        this.userId       = userId;
        this.nom          = nom;
        this.email        = email;
        this.rang         = rang;
        this.isAdmin      = isAdmin;
        this.ownedGames   = ownedGames;
        this.reviewCount  = reviewCount;
        this.messagesSent = messagesSent;
        this.joinDate     = joinDate;
    }

    String joinDateFormatted() {
        return joinDate != null ? FMT.format(joinDate) : "—";
    }
}
