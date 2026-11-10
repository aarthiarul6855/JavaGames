package core;

import java.util.Random;

public class Dice {
    private final Random rnd = new Random();
    public int roll() { return roll(6); }
    public int roll(int sides) { return rnd.nextInt(sides) + 1; }
}
