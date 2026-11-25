/**
 * Represents the main player character in the game.
 * Inherits from GameObject and handles its own animation.
 */
public class Player extends GameObject {
    /**
     * Constructs a new Player at the specified coordinates.
     * @param x Initial X coordinate
     * @param y Initial Y coordinate
     */
    public Player(double x, double y) {
        super("assets/Player(22x26x11).png", 22, 26);
        setLayoutX(x);
        setLayoutY(y);
    }

    /**
     * Updates the player's animation state.
     */
    @Override
    public void update() {
        // Play the 11-frame animation at speed 5
        animate(11, 5);
    }
}