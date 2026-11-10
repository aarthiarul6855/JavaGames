package launcher;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import core.Game;
import core.ScoreBoard;
import TTT.TicTacToePanel;
import RPS.RockPaperScissorsPanel;
import SAL.SnakesLaddersPanel;
// in package one

public class GameLauncherGUI extends JFrame {
    private final CardLayout card = new CardLayout();
    private final JPanel main = new JPanel(card);
    private final ScoreBoard scoreboard = new ScoreBoard();
    private final Map<String, Game> games = new LinkedHashMap<>();

    public GameLauncherGUI() {
        super("Java Game Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);

        JPanel menu = new JPanel(new GridLayout(4,1,10,10));
        JButton tttBtn = new JButton("Tic Tac Toe");
        JButton rpsBtn = new JButton("Rock Paper Scissors");
        JButton slBtn  = new JButton("Snakes & Ladders");
        JButton exitBtn= new JButton("Exit");
        menu.add(tttBtn); menu.add(rpsBtn); menu.add(slBtn); menu.add(exitBtn);

        main.add(menu, "menu");
        add(main);

        // Register games via common interface (runtime polymorphism)
        games.put("Tic Tac Toe", new TicTacToePanel(scoreboard));
        games.put("Rock Paper Scissors", new RockPaperScissorsPanel(scoreboard));
        games.put("Snakes & Ladders", new SnakesLaddersPanel(scoreboard));

        tttBtn.addActionListener(e -> showGame("Tic Tac Toe"));
        rpsBtn.addActionListener(e -> showGame("Rock Paper Scissors"));
        slBtn.addActionListener(e -> showGame("Snakes & Ladders"));
        exitBtn.addActionListener(e -> System.exit(0));
    }

    private void showGame(String key) {
        Game g = games.get(key);
        JPanel panel = g.getPanel(() -> card.show(main, "menu"));
        main.add(panel, key);
        card.show(main, key);
    }

}
