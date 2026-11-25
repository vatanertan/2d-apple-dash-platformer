/**
 * Represents a static block platform.
 */
public class Block extends GameObject {
    /**
     * Constructs a new Block platform.
     * @param x X coordinate
     * @param y Y coordinate
     */
    public Block(double x, double y) {
        super("assets/Block(20x20x1).png", 20, 20);
        setLayoutX(x);
        setLayoutY(y);
    }
}