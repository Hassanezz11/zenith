package atlantafx.sampler.zenith;

public class Session {
    private static Joueur currentUser;

    public static Joueur getCurrentUser() { return currentUser; }
    public static void setCurrentUser(Joueur user) { currentUser = user; }
    public static boolean isLoggedIn() { return currentUser != null; }
    public static void logout() { currentUser = null; }
}
