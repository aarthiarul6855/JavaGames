package core;

// Inheritance base class with encapsulation and validation
public abstract class PlayerBase {
    private String name;
    private int score;

    public PlayerBase() {
        this("Player");
    }

    public PlayerBase(String name) {
        setName(name);
        this.score = 0;
    }

    public final String getName() { return name; }

    public final void setName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new InvalidInputException("Name cannot be blank");
        this.name = name.trim();
    }

    public int getScore() { return score; }

    // Overridable hook (runtime polymorphism via overriding)
    public void incrementScore() {
        score++;
    }
}
