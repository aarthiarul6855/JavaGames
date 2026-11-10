package core;

public class Player extends PlayerBase {
    private int position; // used by Snakes & Ladders

    public Player() { this("Player"); } // constructor chaining

    public Player(String name) { super(name); }

    public int getPosition() { return position; }

    public void setPosition(int position) {
        if (position < 0 || position > 100)
            throw new InvalidInputException("Position must be 0..100");
        this.position = position;
    }

    @Override
    public void incrementScore() {
        super.incrementScore(); // demonstrate super keyword usage
    }
}
