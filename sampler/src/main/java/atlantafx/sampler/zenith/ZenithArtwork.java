package atlantafx.sampler.zenith;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import javafx.application.Platform;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

final class ZenithArtwork {

    private static final HttpClient HTTP = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .build();

    private static final Semaphore IMAGE_SLOTS = new Semaphore(10);

    private ZenithArtwork() {
    }

    static void loadImageAsync(String url, double width, double height, ImageView target) {
        if (url == null || url.isBlank()) return;
        CompletableFuture.supplyAsync(() -> {
            try {
                IMAGE_SLOTS.acquire();
                try {
                    HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "Mozilla/5.0 Zenith/1.0")
                        .GET()
                        .build();
                    HttpResponse<byte[]> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofByteArray());
                    if (resp.statusCode() == 200 && resp.body().length > 0) {
                        return new Image(new ByteArrayInputStream(resp.body()), width, height, false, true);
                    }
                } finally {
                    IMAGE_SLOTS.release();
                }
            } catch (Exception e) {
                System.err.println("[IMG] " + e.getMessage());
            }
            return null;
        }).thenAccept(img -> {
            if (img != null && !img.isError()) {
                Platform.runLater(() -> target.setImage(img));
            }
        });
    }

    static Image getImage(Jeu jeu, double width, double height) {
        if (jeu != null && jeu.getBackgroundImageUrl() != null) {
            return new Image(jeu.getBackgroundImageUrl(), width, height, false, true, true);
        }
        return createPoster(jeu, width, height);
    }

    static Image getHeroImage(Jeu jeu, double width, double height) {
        if (jeu != null && jeu.getBackgroundImageUrl() != null) {
            return new Image(jeu.getBackgroundImageUrl(), width, height, false, true, true);
        }
        return createHero(jeu, width, height);
    }

    static Image createPoster(Jeu jeu, double width, double height) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Color accent = Color.web(jeu.getAccentColor());

        gc.setFill(Color.web("#0b0e11"));
        gc.fillRect(0, 0, width, height);

        gc.setFill(accent.deriveColor(0, 1, 1, 0.9));
        gc.fillRoundRect(18, 18, width - 36, height - 36, 28, 28);

        gc.setFill(Color.web("#11151c"));
        gc.fillRoundRect(26, 26, width - 52, height - 52, 22, 22);

        gc.setFill(accent.deriveColor(0, 1, 1.2, 0.25));
        gc.fillOval(width * 0.48, height * 0.14, width * 0.44, height * 0.34);

        gc.setFill(accent.deriveColor(0, 1, 1.1, 0.16));
        gc.fillOval(width * 0.08, height * 0.56, width * 0.84, height * 0.26);

        gc.setStroke(accent.deriveColor(0, 1, 1, 0.45));
        gc.setLineWidth(2);
        gc.strokeRoundRect(40, 44, width - 80, height - 88, 20, 20);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, width * 0.11));
        gc.fillText(jeu.getTitre().toUpperCase(), 30, height * 0.82, width - 60);

        gc.setFill(accent);
        gc.setFont(Font.font("System", FontWeight.SEMI_BOLD, width * 0.055));
        gc.fillText(jeu.getCategory().toUpperCase(), 32, height * 0.13);

        WritableImage image = new WritableImage((int) Math.ceil(width), (int) Math.ceil(height));
        return canvas.snapshot(new SnapshotParameters(), image);
    }

    static Image createHero(Jeu jeu, double width, double height) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Color accent = Color.web(jeu.getAccentColor());

        gc.setFill(Color.web("#0b0e11"));
        gc.fillRect(0, 0, width, height);

        gc.setFill(Color.web("#161b22"));
        gc.fillRect(0, 0, width, height);

        gc.setFill(accent.deriveColor(0, 1, 1, 0.5));
        gc.fillOval(width * 0.55, -height * 0.15, width * 0.4, height * 0.8);
        gc.fillOval(width * 0.08, height * 0.2, width * 0.32, height * 0.55);

        gc.setStroke(accent.deriveColor(0, 1, 1, 0.45));
        gc.setLineWidth(3);
        gc.strokeLine(width * 0.08, height * 0.75, width * 0.92, height * 0.75);
        gc.strokeLine(width * 0.48, height * 0.08, width * 0.83, height * 0.63);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, width * 0.075));
        gc.fillText(jeu.getTitre().toUpperCase(), width * 0.07, height * 0.3);

        gc.setFill(accent);
        gc.setFont(Font.font("System", FontWeight.BOLD, width * 0.03));
        gc.fillText(jeu.getCategory().toUpperCase(), width * 0.07, height * 0.18);

        gc.setFill(Color.rgb(255, 255, 255, 0.78));
        gc.setFont(Font.font("System", FontWeight.MEDIUM, width * 0.023));
        gc.fillText("Tactical co-op. Frictionless matchmaking. Neon-grade encounters.", width * 0.07, height * 0.42);

        WritableImage image = new WritableImage((int) Math.ceil(width), (int) Math.ceil(height));
        return canvas.snapshot(new SnapshotParameters(), image);
    }
}
