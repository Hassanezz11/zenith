package atlantafx.sampler.zenith;

import java.util.List;

public final class Jeu {

    private final int jeuId;
    private final String titre;
    private final String category;
    private final String description;
    private final Double prix;
    private final String promoLabel;
    private final String accentColor;
    private final String backgroundImageUrl;
    private final List<Review> reviews;

    public Jeu(
        int jeuId,
        String titre,
        String category,
        String description,
        Double prix,
        String promoLabel,
        String accentColor,
        String backgroundImageUrl,
        List<Review> reviews
    ) {
        this.jeuId = jeuId;
        this.titre = titre;
        this.category = category;
        this.description = description;
        this.prix = prix;
        this.promoLabel = promoLabel;
        this.accentColor = accentColor;
        this.backgroundImageUrl = backgroundImageUrl;
        this.reviews = List.copyOf(reviews);
    }

    public Jeu(
        int jeuId,
        String titre,
        String category,
        String description,
        Double prix,
        String promoLabel,
        String accentColor,
        List<Review> reviews
    ) {
        this(jeuId, titre, category, description, prix, promoLabel, accentColor, null, reviews);
    }

    public Jeu(
        String titre,
        String category,
        String description,
        Double prix,
        String promoLabel,
        String accentColor,
        List<Review> reviews
    ) {
        this(0, titre, category, description, prix, promoLabel, accentColor, null, reviews);
    }

    public int getJeuId() {
        return jeuId;
    }

    public String getTitre() {
        return titre;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrix() {
        return prix;
    }

    public String getPromoLabel() {
        return promoLabel;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public String getBackgroundImageUrl() {
        return backgroundImageUrl;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    boolean isFree() {
        return prix == null || prix.doubleValue() == 0.0;
    }

    boolean isOnSale() {
        return promoLabel != null && !promoLabel.isBlank();
    }

    String getDisplayPrice() {
        if (isFree()) {
            return "Free";
        }

        return String.format("$%.2f", prix);
    }

    boolean matchesQuery(String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String normalized = query.toLowerCase();
        return titre.toLowerCase().contains(normalized)
            || category.toLowerCase().contains(normalized)
            || description.toLowerCase().contains(normalized);
    }
}
