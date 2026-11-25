/**
 * Represents an apple object that can be collected by the player.
 * Inherits from GameObject and has a continuous animation.
 */
public class Apple extends GameObject {
    /**
     * Constructs a new Apple at the specified coordinates.
     * @param x X coordinate for spawning
     * @param y Y coordinate for spawning
     */
    public Apple(double x, double y) {
        super("assets/Apple(16x18x17).png", 16, 18);
        setLayoutX(x);
        setLayoutY(y);
    }

    /**
     * Updates the apple's idle animation.
     */
    @Override
    public void update() {
        // Play the 17-frame idle animation
        animate(17, 5);
    }
}