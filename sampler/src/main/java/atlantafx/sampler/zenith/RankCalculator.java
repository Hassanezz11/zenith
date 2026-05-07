package atlantafx.sampler.zenith;

public final class RankCalculator {

    private RankCalculator() {}

    // Tiers are based on number of games owned (EstWishlist = 0 in UsersJeux)
    public static String compute(int ownedCount) {
        if (ownedCount >= 10) return "Diamond";
        if (ownedCount >= 6)  return "Platinum";
        if (ownedCount >= 3)  return "Gold";
        if (ownedCount >= 1)  return "Silver";
        return "Bronze";
    }

    // CSS class name for the rank, used to colour the badge
    public static String cssClass(String rank) {
        if (rank == null) return "rank-bronze";
        return switch (rank) {
            case "Diamond" -> "rank-diamond";
            case "Platinum" -> "rank-platinum";
            case "Gold"    -> "rank-gold";
            case "Silver"  -> "rank-silver";
            default        -> "rank-bronze";
        };
    }
}
