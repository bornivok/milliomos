package hu.milliomos.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Valódi "Legyen Ön is milliomos" TV-stúdió háttér:
 * - Tiszta sötétkék stúdióháttér (vízszintes és oldalsó sávok nélkül)
 * - Középen az autentikus "MILLIOMOS" logó a mellékelt képből
 * - Két oldalon a játékosok karaktere (characters.png)
 * - Ovális asztal szimbólumok nélkül
 */
public class StudioBackdropPane extends Pane {

    private static final String[] COMBINED_IMAGE_PATHS = {"/characters.png", "/characters.jpg"};

    private ImageView logoView;
    private final Group hostCharacter;
    private final Group playerCharacter;

    public StudioBackdropPane() {
        setPrefSize(960, 650);
        setStyle("-fx-background-color: #04091a;");

        // 1. Középső embléma a csatolt képből
        Image logoImg = loadLogoImage();
        if (logoImg != null) {
            logoView = new ImageView(logoImg);
            logoView.setFitWidth(310);
            logoView.setFitHeight(310);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
            getChildren().add(logoView);
        }

        // 2. Karakterek betöltése (characters.png)
        hostCharacter = loadCharacterGroup(true, 215, 230, 260, Color.web("#00d2ff"), Color.web("#1a2a6c"));
        playerCharacter = loadCharacterGroup(false, 230, 690, 260, Color.web("#f5af19"), Color.web("#3a1c71"));

        getChildren().addAll(hostCharacter, playerCharacter);

        widthProperty().addListener((obs, oldV, newV) -> updateLayoutPositions(newV.doubleValue(), getHeight()));
        heightProperty().addListener((obs, oldV, newV) -> updateLayoutPositions(getWidth(), newV.doubleValue()));

        startAmbientAnimations(hostCharacter, playerCharacter);
    }

    private Image loadLogoImage() {
        try (InputStream is = getClass().getResourceAsStream("/logo.png")) {
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception ignored) {
        }
        try {
            File f = new File("src/main/resources/logo.png");
            if (f.exists()) {
                return new Image(f.toURI().toString());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Group loadCharacterGroup(boolean isHost, double targetHeight, double x, double y,
                                     Color accentNeon, Color suitColor) {
        String singlePath = isHost ? "/kobold_host.png" : "/dragonborn_player.png";
        try (InputStream is = getClass().getResourceAsStream(singlePath)) {
            if (is != null) {
                Image rawImg = new Image(is);
                Image cleanImg = autoCropAndRemoveWhiteBg(rawImg, 0, (int) rawImg.getWidth(), isHost);
                return buildImageRig(cleanImg, targetHeight, x, y);
            }
        } catch (Exception ignored) {
        }

        for (String combinedPath : COMBINED_IMAGE_PATHS) {
            try (InputStream is = getClass().getResourceAsStream(combinedPath)) {
                if (is != null) {
                    Image fullImg = new Image(is);
                    int fullW = (int) fullImg.getWidth();
                    if (fullW > 0) {
                        int startX = isHost ? 0 : fullW / 2;
                        int subW = isHost ? (fullW / 2) : (fullW - startX);
                        Image croppedChar = autoCropAndRemoveWhiteBg(fullImg, startX, subW, isHost);
                        return buildImageRig(croppedChar, targetHeight, x, y);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        try {
            File f = new File("src/main/resources/characters.png");
            if (f.exists()) {
                Image fullImg = new Image(f.toURI().toString());
                int fullW = (int) fullImg.getWidth();
                if (fullW > 0) {
                    int startX = isHost ? 0 : fullW / 2;
                    int subW = isHost ? (fullW / 2) : (fullW - startX);
                    Image croppedChar = autoCropAndRemoveWhiteBg(fullImg, startX, subW, isHost);
                    return buildImageRig(croppedChar, targetHeight, x, y);
                }
            }
        } catch (Exception ignored) {
        }

        // Tartalék vektoros figura, ha a kép nem érhető el
        return createCharacterRig(x, y, accentNeon, suitColor, isHost);
    }

    private Image autoCropAndRemoveWhiteBg(Image src, int offsetX, int width, boolean isHost) {
        int height = (int) src.getHeight();
        PixelReader reader = src.getPixelReader();
        if (reader == null || width <= 0 || height <= 0) return src;

        boolean[][] transparent = new boolean[width][height];
        Queue<int[]> queue = new ArrayDeque<>();

        for (int x = 0; x < width; x++) {
            queue.add(new int[]{x, 0});
            queue.add(new int[]{x, height - 1});
        }
        for (int y = 0; y < height; y++) {
            queue.add(new int[]{0, y});
            queue.add(new int[]{width - 1, y});
        }

        while (!queue.isEmpty()) {
            int[] p = queue.poll();
            int x = p[0], y = p[1];
            if (x < 0 || x >= width || y < 0 || y >= height || transparent[x][y]) continue;

            boolean isCornerWatermark = !isHost && (x > width * 0.82 && y > height * 0.88);
            Color c = reader.getColor(offsetX + x, y);

            if (isCornerWatermark || isBackgroundWhite(c, 0.88)) {
                transparent[x][y] = true;
                queue.add(new int[]{x + 1, y});
                queue.add(new int[]{x - 1, y});
                queue.add(new int[]{x, y + 1});
                queue.add(new int[]{x, y - 1});
            }
        }

        int minX = width, minY = height, maxX = 0, maxY = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!transparent[x][y]) {
                    Color c = reader.getColor(offsetX + x, y);
                    boolean inKoboldShirtZone = isHost
                            && (x > width * 0.20 && x < width * 0.65)
                            && (y > height * 0.35 && y < height * 0.62);

                    if (!inKoboldShirtZone && isBackgroundWhite(c, 0.94)) {
                        transparent[x][y] = true;
                    }
                }

                if (!transparent[x][y]) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }

        if (maxX <= minX || maxY <= minY) return src;

        int cropW = maxX - minX + 1;
        int cropH = maxY - minY + 1;
        WritableImage out = new WritableImage(cropW, cropH);
        PixelWriter writer = out.getPixelWriter();

        for (int y = 0; y < cropH; y++) {
            for (int x = 0; x < cropW; x++) {
                int srcX = minX + x;
                int srcY = minY + y;
                if (transparent[srcX][srcY]) {
                    writer.setArgb(x, y, 0x00000000);
                } else {
                    writer.setColor(x, y, reader.getColor(offsetX + srcX, srcY));
                }
            }
        }
        return out;
    }

    private boolean isBackgroundWhite(Color c, double threshold) {
        if (c.getOpacity() < 0.1) return true;
        return c.getRed() >= threshold
                && c.getGreen() >= threshold
                && c.getBlue() >= threshold
                && Math.abs(c.getRed() - c.getGreen()) < 0.06
                && Math.abs(c.getGreen() - c.getBlue()) < 0.06;
    }

    private Group buildImageRig(Image img, double targetHeight, double x, double y) {
        ImageView iv = new ImageView(img);
        iv.setFitHeight(targetHeight);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        double renderedWidth = (img.getHeight() > 0)
                ? (img.getWidth() / img.getHeight()) * targetHeight
                : targetHeight * 0.85;

        iv.setTranslateX(-renderedWidth / 2.0);
        iv.setTranslateY(-targetHeight * 0.62);

        Group spriteGroup = new Group(iv);
        Group rig = new Group(spriteGroup);
        rig.setTranslateX(x);
        rig.setTranslateY(y);
        return rig;
    }

    private Group createCharacterRig(double x, double y, Color accentNeon, Color suitColor, boolean facingRight) {
        Group rig = new Group();
        rig.setTranslateX(x);
        rig.setTranslateY(y);

        int dir = facingRight ? 1 : -1;
        Group bodyGroup = new Group();
        Polygon torso = new Polygon(-35, 45, 35, 45, 26, -15, -26, -15);
        torso.setFill(suitColor);
        torso.setStroke(accentNeon);
        torso.setStrokeWidth(1.5);

        Polygon collar = new Polygon(-8, -15, 8, -15, 0, 12);
        collar.setFill(Color.web("#e6f2ff"));

        Circle head = new Circle(0, -38, 20);
        head.setFill(Color.web("#d9a17c"));

        Path arm = new Path(new MoveTo(dir * 12, -5), new LineTo(dir * 32, 18), new LineTo(dir * 52, 12));
        arm.setStroke(suitColor);
        arm.setStrokeWidth(11);
        arm.setStrokeLineCap(StrokeLineCap.ROUND);

        bodyGroup.getChildren().addAll(torso, collar, arm, head);
        rig.getChildren().add(bodyGroup);
        return rig;
    }

    private void updateLayoutPositions(double w, double h) {
        double centerX = w / 2.0;
        double stageY = h * 0.40;

        if (logoView != null) {
            logoView.setTranslateX(centerX - 155.0);
            logoView.setTranslateY(stageY - 150.0);
        }

        hostCharacter.setTranslateX(centerX - 215.0);
        hostCharacter.setTranslateY(stageY - 10.0);

        playerCharacter.setTranslateX(centerX + 215.0);
        playerCharacter.setTranslateY(stageY - 10.0);
    }

    private void startAmbientAnimations(Group host, Group player) {
        if (!host.getChildren().isEmpty() && !player.getChildren().isEmpty()) {
            Node hostBody = host.getChildren().get(0);
            Node playerBody = player.getChildren().get(0);

            Timeline idleTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(hostBody.translateYProperty(), 0),
                            new KeyValue(playerBody.translateYProperty(), -3)),
                    new KeyFrame(Duration.seconds(2.2),
                            new KeyValue(hostBody.translateYProperty(), -4),
                            new KeyValue(playerBody.translateYProperty(), 1))
            );
            idleTimeline.setAutoReverse(true);
            idleTimeline.setCycleCount(Animation.INDEFINITE);
            idleTimeline.play();
        }
    }
}
