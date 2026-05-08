package atlantafx.sampler.zenith;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class RawgApiService {

    private static final String BASE_URL = "https://api.rawg.io/api";

    private final String apiKey;
    private final HttpClient httpClient;

    RawgApiService(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    }

    List<String> fetchTopGamesByGenre(String genre) {
        String slug = toGenreSlug(genre);
        String url = BASE_URL + "/games?key=" + apiKey
            + "&genres=" + URLEncoder.encode(slug, StandardCharsets.UTF_8)
            + "&ordering=-rating&page_size=5";
        return fetch(url);
    }

    List<RawgGame> fetchTopGamesByGenreAsObjects(String genre) {
        String slug = toGenreSlug(genre);
        String url = BASE_URL + "/games?key=" + apiKey
            + "&genres=" + URLEncoder.encode(slug, StandardCharsets.UTF_8)
            + "&ordering=-rating&page_size=5";
        return fetchAsObjects(url, genre);
    }

    private List<RawgGame> fetchAsObjects(String url, String genre) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parseAsRawgGames(response.body(), genre);
        } catch (Exception e) {
            System.err.println("[RAWG] fetchAsObjects error: " + e.getMessage());
            return List.of();
        }
    }

    List<Jeu> fetchGamesAsJeu(String genre, int count) {
        return fetchGamesAsJeu(genre, count, 1);
    }

    List<Jeu> fetchGamesAsJeu(String genre, int count, int page) {
        String slug = toGenreSlug(genre);
        String url = BASE_URL + "/games?key=" + apiKey
            + "&genres=" + URLEncoder.encode(slug, StandardCharsets.UTF_8)
            + "&ordering=-rating&page_size=" + count
            + "&page=" + page;
        return fetchAsJeu(url, genre);
    }

    List<Jeu> searchGamesAsJeu(String query) {
        return searchGamesAsJeu(query, 1);
    }

    List<Jeu> searchGamesAsJeu(String query, int page) {
        String url = BASE_URL + "/games?key=" + apiKey
            + "&search=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
            + "&page_size=40&page=" + page;
        return fetchAsJeu(url, "search");
    }

    private List<Jeu> fetchAsJeu(String url, String genre) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return parseAsJeu(response.body(), genre);
            } catch (Exception e) {
                System.err.println("[RAWG] fetchAsJeu attempt " + attempt + " error: " + e.getMessage());
                if (attempt == 3) return List.of();
            }
        }
        return List.of();
    }

    private List<Jeu> parseAsJeu(String json, String genre) {
        List<Jeu> results = new ArrayList<>();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("detail")) return results;

            JsonArray games = root.getAsJsonArray("results");
            for (int i = 0; i < games.size(); i++) {
                JsonObject game = games.get(i).getAsJsonObject();
                int id = game.get("id").getAsInt();
                String name = game.get("name").getAsString();
                double rating = game.has("rating") ? game.get("rating").getAsDouble() : 0.0;
                String released = game.has("released") && !game.get("released").isJsonNull()
                    ? game.get("released").getAsString() : "TBA";
                String bgImage = game.has("background_image") && !game.get("background_image").isJsonNull()
                    ? game.get("background_image").getAsString() : null;

                String genreName = genre;
                if (game.has("genres") && game.get("genres").getAsJsonArray().size() > 0) {
                    genreName = game.get("genres").getAsJsonArray()
                        .get(0).getAsJsonObject().get("name").getAsString();
                }

                String description = "Rating: " + String.format("%.1f", rating) + "/5  •  Released: " + released;
                String promoLabel = rating >= 4.5 ? "Top Rated" : null;
                String accentColor = genreToColor(genre);
                Double prix = rating >= 4.5 ? 59.99 : rating >= 4.0 ? 39.99 : 29.99;

                results.add(new Jeu(id, name, genreName, description, prix, promoLabel, accentColor, bgImage, List.of()));
            }
        } catch (Exception e) {
            System.err.println("[RAWG] parseAsJeu error: " + e.getMessage());
        }
        return results;
    }

    private static String genreToColor(String genre) {
        return switch (genre.toLowerCase()) {
            case "action" -> "#e74c3c";
            case "role-playing-games-rpg", "rpg" -> "#9b59b6";
            case "strategy" -> "#3498db";
            case "shooter" -> "#e67e22";
            case "adventure" -> "#2ecc71";
            case "racing" -> "#f1c40f";
            case "sports" -> "#1abc9c";
            case "simulation" -> "#34495e";
            case "puzzle" -> "#e91e63";
            default -> "#6c63ff";
        };
    }

    private List<RawgGame> parseAsRawgGames(String json, String genre) {
        List<RawgGame> results = new ArrayList<>();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("detail")) {
                return results;
            }
            JsonArray games = root.getAsJsonArray("results");
            for (int i = 0; i < Math.min(games.size(), 5); i++) {
                JsonObject game = games.get(i).getAsJsonObject();
                String name = game.get("name").getAsString();
                double rating = game.has("rating") ? game.get("rating").getAsDouble() : 0.0;
                String released = game.has("released") && !game.get("released").isJsonNull()
                    ? game.get("released").getAsString() : "TBA";
                String bgImage = game.has("background_image") && !game.get("background_image").isJsonNull()
                    ? game.get("background_image").getAsString() : null;
                results.add(new RawgGame(name, rating, released, bgImage, genre));
            }
        } catch (Exception e) {
            System.err.println("[RAWG] parse error: " + e.getMessage());
        }
        return results;
    }

    List<String> searchGames(String query) {
        String url = BASE_URL + "/games?key=" + apiKey
            + "&search=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
            + "&page_size=5";
        return fetch(url);
    }

    private List<String> fetch(String url) {
        try {
            System.out.println("[RAWG] Calling: " + url);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[RAWG] Status: " + response.statusCode());
            System.out.println("[RAWG] Body (first 300 chars): " + response.body().substring(0, Math.min(300, response.body().length())));
            return parseResults(response.body());
        } catch (Exception e) {
            System.err.println("[RAWG] Exception: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return List.of("Error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private List<String> parseResults(String json) {
        List<String> results = new ArrayList<>();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            if (root.has("detail")) {
                results.add("RAWG API error: " + root.get("detail").getAsString());
                return results;
            }

            JsonArray games = root.getAsJsonArray("results");
            for (int i = 0; i < Math.min(games.size(), 5); i++) {
                JsonObject game = games.get(i).getAsJsonObject();
                String name = game.get("name").getAsString();
                double rating = game.has("rating") ? game.get("rating").getAsDouble() : 0.0;
                String released = game.has("released") && !game.get("released").isJsonNull()
                    ? game.get("released").getAsString() : "TBA";
                results.add(String.format("• %s  |  Rating: %.1f  |  Released: %s", name, rating, released));
            }

            if (results.isEmpty()) {
                results.add("No games found for this genre.");
            }
        } catch (Exception e) {
            results.add("Failed to parse RAWG response.");
        }
        return results;
    }

    private static String toGenreSlug(String category) {
        return switch (category.toLowerCase().trim()) {
            case "rpg" -> "role-playing-games-rpg";
            case "fps", "shooter", "first person shooter" -> "shooter";
            case "strategy", "rts" -> "strategy";
            case "sports" -> "sports";
            case "racing" -> "racing";
            case "puzzle" -> "puzzle";
            case "simulation", "sim" -> "simulation";
            case "indie" -> "indie";
            case "mmo", "mmorpg" -> "massively-multiplayer";
            case "adventure" -> "adventure";
            case "fighting" -> "fighting";
            case "arcade" -> "arcade";
            default -> category.toLowerCase().replace(" ", "-");
        };
    }
}