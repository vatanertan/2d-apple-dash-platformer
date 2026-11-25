/**
 * Represents a static spike obstacle.
 * Collision with this object resets the game.
 */
public class Spike extends GameObject {
    /**
     * Constructs a new Spike at the specified coordinates.
     * @param x X coordinate
     * @param y Y coordinate
     */
    public Spike(double x, double y) {
        super("assets/Spike(14x7x1).png", 14, 7);
        setLayoutX(x);
        setLayoutY(y);
    }
}