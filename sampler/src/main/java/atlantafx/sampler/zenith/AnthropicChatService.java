package atlantafx.sampler.zenith;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Streaming chat service backed by Ollama (http://localhost:11434).
 * Tokens stream in real-time so the UI updates word by word.
 */
class AnthropicChatService {

    private static final String OLLAMA_URL = "http://localhost:11434/api/chat";
    private static final String MODEL = "llama3.2";

    private final HttpClient http;
    private final List<AiChatMessage> history = new ArrayList<>();
    private String systemPrompt;

    AnthropicChatService(String ignored) {
        this.http = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    void initContext(Joueur user) {
        String name = user.getNom();
        String rank = user.getRank() != null ? user.getRank() : "Bronze";
        int owned = user.getOwnedGames().size();
        List<String> genres = user.getPreferedGames().stream()
            .map(Jeu::getCategory).distinct().toList();

        systemPrompt = "You are Zenith AI, a gaming assistant. Be brief (2-3 sentences max). "
            + "User: " + name + ", Rank: " + rank + ", Games owned: " + owned + ", "
            + "Preferred genres: " + (genres.isEmpty() ? "none" : String.join(", ", genres)) + ". "
            + "Platform: Home=featured, Browse=discover games, Library=owned, Messages=friends, Profile=settings. "
            + "Ranks by library size: Bronze(0-4) Silver(5-9) Gold(10-19) Platinum(20-49) Diamond(50-99) Legendary(100+).";
    }

    /**
     * Streams the response token by token.
     * onToken is called for each word fragment on a background thread — caller must Platform.runLater.
     * onComplete is called when streaming finishes.
     * onError is called if Ollama is unreachable.
     */
    void sendStreaming(String userMessage,
                       Consumer<String> onToken,
                       Runnable onComplete,
                       Consumer<String> onError) {
        history.add(new AiChatMessage("user", userMessage));

        JsonObject body = buildBody(true);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_URL))
                .header("content-type", "application/json")
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

            HttpResponse<java.io.InputStream> response =
                http.send(request, HttpResponse.BodyHandlers.ofInputStream());

            StringBuilder full = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    try {
                        JsonObject chunk = JsonParser.parseString(line).getAsJsonObject();
                        if (chunk.has("message")) {
                            String token = chunk.getAsJsonObject("message").get("content").getAsString();
                            if (!token.isEmpty()) {
                                full.append(token);
                                onToken.accept(token);
                            }
                        }
                        if (chunk.has("done") && chunk.get("done").getAsBoolean()) break;
                    } catch (Exception ignored2) {}
                }
            }

            history.add(new AiChatMessage("assistant", full.toString()));
            onComplete.run();

        } catch (java.net.ConnectException e) {
            history.remove(history.size() - 1);
            onError.accept("Ollama is not running.\nStart it: install from ollama.com, then run:\n  ollama pull llama3.2");
        } catch (Exception e) {
            history.remove(history.size() - 1);
            System.err.println("[Ollama] " + e.getMessage());
            onError.accept("Could not reach Ollama. Make sure it is running.");
        }
    }

    private JsonObject buildBody(boolean stream) {
        JsonObject body = new JsonObject();
        body.addProperty("model", MODEL);
        body.addProperty("stream", stream);

        com.google.gson.JsonArray messages = new com.google.gson.JsonArray();

        if (systemPrompt != null) {
            JsonObject sys = new JsonObject();
            sys.addProperty("role", "system");
            sys.addProperty("content", systemPrompt);
            messages.add(sys);
        }

        for (AiChatMessage msg : history) {
            JsonObject m = new JsonObject();
            m.addProperty("role", msg.role());
            m.addProperty("content", msg.content());
            messages.add(m);
        }
        body.add("messages", messages);
        return body;
    }

    void clearHistory() {
        history.clear();
    }
}