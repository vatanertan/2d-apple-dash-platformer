/**
 * Represents a static terrain platform.
 */
public class Terrain extends GameObject {
    /**
     * Constructs a new Terrain platform.
     * @param x X coordinate
     * @param y Y coordinate
     */
    public Terrain(double x, double y) {
        super("assets/Terrain(48x48x1).png", 48, 48);
        setLayoutX(x);
        setLayoutY(y);
    }
}