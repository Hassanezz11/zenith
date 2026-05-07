package atlantafx.sampler.zenith;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

final class ZenithArtwork {

    private ZenithArtwork() {
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
