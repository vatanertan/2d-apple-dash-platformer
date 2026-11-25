import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Main engine class responsible for game logic, physics, and rendering.
 */
public class GameEngine extends Application {
    private final Pane gameRoot = new Pane();
    private final StackPane mainRoot = new StackPane();
    private static final int GAME_WIDTH = 960, GAME_HEIGHT = 480;
    private static final int VIEWPORT_WIDTH = 480, VIEWPORT_HEIGHT = 480;
    private Label scoreLabel;
    private Label spikesLabel;
    private Label applesLabel;

    // Movement controls
    private boolean leftPressed, rightPressed, jumpPressed, skillPressed;
    private boolean spaceReleased = true;
    private boolean jumpReleased = true; 
    
    // Game object lists
    private List<Terrain> terrains = new ArrayList<>();
    private List<Block> blocks = new ArrayList<>();
    private List<Spike> spikes = new ArrayList<>();
    private List<Apple> apples = new ArrayList<>();
    private Player player;

    // Game state and physics
    private int score = 0, spikeCount = 0, appleCount = 0, collectedApples = 0;
    private double playerVelocityY = 0;
    private boolean isGrounded = false;
    private long lastSpawnTime = 0;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initializes the game stage, scene, and event listeners.
     */
    @Override
    public void start(Stage stage) {
        mainRoot.getChildren().add(gameRoot);

        StackPane.setAlignment(gameRoot, Pos.TOP_LEFT);
        gameRoot.setMinSize(GAME_WIDTH, GAME_HEIGHT);
        gameRoot.setMaxSize(GAME_WIDTH, GAME_HEIGHT);
        
        reset();

        gameRoot.setBackground(new Background(new BackgroundImage(
                new Image(new File("assets/Background(960x480x1).png").toURI().toString()), null, null, null, null)));

        Scene scene = new Scene(mainRoot, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);

        // Register key events
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT) leftPressed = true;
            if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) rightPressed = true;
            if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP) jumpPressed = true;
            if (e.getCode() == KeyCode.SPACE) skillPressed = true;
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT) leftPressed = false;
            if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) rightPressed = false;
            if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP) jumpPressed = false; jumpReleased = true;
            if (e.getCode() == KeyCode.SPACE) {
                skillPressed = false;
                spaceReleased = true;
            }
        });

        stage.setTitle("JavaFX 2D Apple Dash Platformer");
        stage.setScene(scene);
        stage.show();
        gameRoot.requestFocus();

        new GameLoop().start();
    }

    /**
     * Resets the game to its initial state using level.txt data.
     */
    private void reset() {
        gameRoot.getChildren().clear();
        gameRoot.setTranslateX(0);
        gameRoot.setTranslateY(0);

        // Initialize or update UI labels
        if (scoreLabel == null) {
            scoreLabel = new Label("Score: 0");
            scoreLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");
            StackPane.setAlignment(scoreLabel, Pos.BOTTOM_LEFT);
            StackPane.setMargin(scoreLabel, new Insets(0, 0, 5, 10));
            mainRoot.getChildren().add(scoreLabel);

            spikesLabel = new Label("Spikes: 0");
            spikesLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");
            StackPane.setAlignment(spikesLabel, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(spikesLabel, new Insets(0, 10, 5, 0));
            mainRoot.getChildren().add(spikesLabel);

            applesLabel = new Label("Apples: 0");
            applesLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");
            StackPane.setAlignment(applesLabel, Pos.BOTTOM_CENTER);
            StackPane.setMargin(applesLabel, new Insets(0, 0, 5, 0));
            mainRoot.getChildren().add(applesLabel);
        }

        // Clear object lists and reset state variables
        terrains.clear();
        blocks.clear();
        spikes.clear();
        apples.clear();
        score = 0;
        spikeCount = 0;
        appleCount = 0;
        collectedApples = 0;
        playerVelocityY = 0;
        lastSpawnTime = System.currentTimeMillis();

        updateLabels();

        // Parse level.txt
        try {
            Scanner scanner = new Scanner(new File("level.txt"));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                
                String[] parts = line.split(",");
                String type = parts[0];
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);

                GameObject obj = null;
                switch (type) {
                    case "Terrain":
                        obj = new Terrain(x, y);
                        terrains.add((Terrain) obj);
                        break;
                    case "Block":
                        obj = new Block(x, y);
                        blocks.add((Block) obj);
                        break;
                    case "Player":
                        player = new Player(x, y);
                        obj = player;
                        break;
                }
                if (obj != null) gameRoot.getChildren().add(obj);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("Error: level.txt not found.");
        }
        
        scoreLabel.toFront();
        spikesLabel.toFront();
        applesLabel.toFront();
    }

    /**
     * Checks if a GameObject's current position is invalid (out of bounds or colliding).
     * @param obj The object to check
     * @return true if position is invalid, false otherwise
     */
    private boolean isInvalid(GameObject obj) {
        // Boundary check
        if (obj.getLayoutX() < 0 || obj.getLayoutX() + obj.getObjectWidth() > GAME_WIDTH ||
            obj.getLayoutY() < 0 || obj.getLayoutY() + obj.getObjectHeight() > GAME_HEIGHT) {
            return true;
        }

        // Collision check with platforms using small tolerance
        double tolerance = 0.1;
        for (Terrain t : terrains) {
            if (obj.getBoundsInParent().intersects(t.getLayoutX()+tolerance, t.getLayoutY()+tolerance, t.getObjectWidth()-2*tolerance, t.getObjectHeight()-2*tolerance)) return true;
        }
        for (Block b : blocks) {
            if (obj.getBoundsInParent().intersects(b.getLayoutX()+tolerance, b.getLayoutY()+tolerance, b.getObjectWidth()-2*tolerance, b.getObjectHeight()-2*tolerance)) return true;
        }
        return false;
    }

    /**
     * Spawns 1 spike and 2 apples at valid random positions.
     */
    private void spawn() {
        Random rand = new Random();
        
        // Spawn 2 Apples in air
        for (int i = 0; i < 2; i++) {
            boolean spawned = false;
            int attempts = 0;
            while (!spawned && attempts < 15) {
                double ax = rand.nextDouble() * (GAME_WIDTH - 16);
                double ay = rand.nextDouble() * (GAME_HEIGHT - 18);
                Apple apple = new Apple(ax, ay);
                
                if (!isInvalid(apple)) {
                    apples.add(apple);
                    gameRoot.getChildren().add(apple);
                    appleCount++;
                    spawned = true;
                }
                attempts++;
            }
        }

        // Spawn 1 Spike on a platform
        List<GameObject> platforms = new ArrayList<>();
        platforms.addAll(terrains);
        platforms.addAll(blocks);
        
        if (!platforms.isEmpty()) {
            boolean spawned = false;
            int attempts = 0;
            while (!spawned && attempts < 15) {
                GameObject p = platforms.get(rand.nextInt(platforms.size()));
                // Avoid topmost terrains
                if (p.getLayoutY() > 50) {
                    double spikesx = p.getLayoutX() + rand.nextDouble() * (p.getObjectWidth() - 14);
                    double spikesy = p.getLayoutY() - 7;
                    Spike spike = new Spike(spikesx, spikesy);
                    if (spikesx >= 0 && spikesx + 14 <= GAME_WIDTH && spikesy >= 0 && !isInvalid(spike)) {
                        spikes.add(spike);
                        gameRoot.getChildren().add(spike);
                        spikeCount++;
                        spawned = true;
                    }
                }
                attempts++;
            }
        }
        updateLabels();
    }

    private void updateLabels() {
        scoreLabel.setText("Score: " + score);
        spikesLabel.setText("Spikes: " + spikeCount);
        applesLabel.setText("Apples: " + appleCount);
    }

    /**
     * Main update loop handling movement, physics, and collisions.
     */
    private void update() {
        for (javafx.scene.Node node : gameRoot.getChildren()) {
            if (node instanceof GameObject) ((GameObject) node).update();
        }

        if (player == null) return;

        // Horizontal Movement
        double oldX = player.getLayoutX();
        if (leftPressed) {
            player.setScaleX(-1);
            player.setLayoutX(oldX - 1);
        } else if (rightPressed) {
            player.setScaleX(1);
            player.setLayoutX(oldX + 1);
        }
        if (isInvalid(player)) player.setLayoutX(oldX);

        // Vertical Movement (Physics)
        if (jumpPressed && jumpReleased && isGrounded) {
            playerVelocityY = -3.4; // Initial velocity for ~58px jump height
            isGrounded = false;
            jumpReleased = false;
        }
        double oldY = player.getLayoutY();
        playerVelocityY += 0.1; // Gravity constant
        player.setLayoutY(oldY + playerVelocityY);

        if (isInvalid(player)) {
            player.setLayoutY(oldY);
            if (playerVelocityY > 0) isGrounded = true;
            playerVelocityY = 0;
        } else {
            isGrounded = false;
        }

        // Spike Collisions
        for (Spike s : spikes) {
            if (player.getBoundsInParent().intersects(s.getBoundsInParent())) {
                reset();
                return;
            }
        }

        // Apple Collection
        Iterator<Apple> it = apples.iterator();
        while (it.hasNext()) {
            Apple a = it.next();
            if (player.getBoundsInParent().intersects(a.getBoundsInParent())) {
                gameRoot.getChildren().remove(a);
                it.remove();
                appleCount--;
                collectedApples++;
                
                if (collectedApples % 2 == 0) {
                    score++;
                    if (!spikes.isEmpty()) {
                        Spike rs = spikes.remove(new Random().nextInt(spikes.size()));
                        gameRoot.getChildren().remove(rs);
                        spikeCount--;
                    }
                }
                updateLabels();
            }
        }

        // Spawning Timer
        if (System.currentTimeMillis() - lastSpawnTime >= 3000) {
            spawn();
            lastSpawnTime = System.currentTimeMillis();
        }

        // Skill: Spike Destroyer
        if (skillPressed && spaceReleased && score >= 5) {
            spaceReleased = false;
            if (!spikes.isEmpty()) {
                Spike closest = null;
                double minDist = Double.MAX_VALUE;
                for (Spike s : spikes) {
                    double d = Math.hypot(s.getLayoutX() - player.getLayoutX(), s.getLayoutY() - player.getLayoutY());
                    if (d < minDist) {
                        minDist = d;
                        closest = s;
                    }
                }
                if (closest != null) {
                    gameRoot.getChildren().remove(closest);
                    spikes.remove(closest);
                    spikeCount--;
                    score -= 5;
                    updateLabels();
                }
            }
        }

        // Camera System
        double tx = (VIEWPORT_WIDTH / 2.0) - player.getLayoutX() - (player.getObjectWidth() / 2.0);
        tx = Math.max(Math.min(tx, 0), VIEWPORT_WIDTH - GAME_WIDTH);
        gameRoot.setTranslateX(tx);
    }

    private class GameLoop extends AnimationTimer {
        @Override
        public void handle(long now) {
            update();
        }
    }
}