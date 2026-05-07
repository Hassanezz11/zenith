package atlantafx.sampler.zenith;

public final class Review {

    private final String auteur;
    private final String rang;
    private final int rating;
    private final String comment;
    private final boolean verified;

    public Review(String auteur, String rang, int rating, String comment, boolean verified) {
        this.auteur   = auteur;
        this.rang     = rang;
        this.rating   = rating;
        this.comment  = comment;
        this.verified = verified;
    }

    String getAuteur()  { return auteur; }
    String getRang()    { return rang; }
    int    getRating()  { return rating; }
    String getComment() { return comment; }
    boolean isVerified(){ return verified; }
}
