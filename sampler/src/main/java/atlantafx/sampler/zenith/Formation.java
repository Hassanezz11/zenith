package atlantafx.sampler.zenith;

public class Formation {

    private final int id;
    private final String titre;
    private final String etablissement;
    private final String annee;
    private final String description;

    public Formation(int id, String titre, String etablissement, String annee, String description) {
        this.id = id;
        this.titre = titre;
        this.etablissement = etablissement;
        this.annee = annee;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public String getEtablissement() {
        return etablissement;
    }

    public String getAnnee() {
        return annee;
    }

    public String getDescription() {
        return description;
    }
}
