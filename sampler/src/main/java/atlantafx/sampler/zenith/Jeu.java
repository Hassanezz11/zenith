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
    private final List<Review> reviews;

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
        this.jeuId = jeuId;
        this.titre = titre;
        this.category = category;
        this.description = description;
        this.prix = prix;
        this.promoLabel = promoLabel;
        this.accentColor = accentColor;
        this.reviews = List.copyOf(reviews);
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
        this(0, titre, category, description, prix, promoLabel, accentColor, reviews);
    }

    int getJeuId() {
        return jeuId;
    }

    String getTitre() {
        return titre;
    }

    String getCategory() {
        return category;
    }

    String getDescription() {
        return description;
    }

    Double getPrix() {
        return prix;
    }

    String getPromoLabel() {
        return promoLabel;
    }

    String getAccentColor() {
        return accentColor;
    }

    List<Review> getReviews() {
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
