package RPS;

import core.*;
import javax.swing.*;
//import java.awt.*;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.*;             // JPanel, JButton, JLabel, JOptionPane, Timer
import java.awt.*;                // Layouts, Graphics, Color, Font, FlowLayout, BorderLayout
import java.awt.event.*;          // ActionEvent, ActionListener
import java.awt.geom.*;           // QuadCurve2D
import java.awt.GraphicsEnvironment; // Font detection



public class RockPaperScissorsPanel extends BaseGamePanel {
    private final JLabel roundLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel scoreLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel turnLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 12));

    private Player p1, p2;
    private String p1Choice = null;
    private String p2Choice = null;
    private boolean isP1Turn = true;
    private int totalRounds = 1;
    private int currentRound = 1;

    private Runnable backToMenu = () -> {};

    public RockPaperScissorsPanel(ScoreBoard<? super PlayerBase> scoreboard) {
        super(scoreboard);
    }

    @Override
    public JPanel getPanel(Runnable backToMenu) {
        this.backToMenu = backToMenu;
        JPanel root = this;
        removeAll();
        setLayout(new BorderLayout(12,12));

        setupPlayersAndRounds();

        roundLabel.setText("Round 1 of " + totalRounds);
        roundLabel.setFont(roundLabel.getFont().deriveFont(Font.BOLD, 18f));

        turnLabel.setText(p1.getName() + "'s turn");
        turnLabel.setFont(turnLabel.getFont().deriveFont(Font.BOLD, 22f));

        statusLabel.setText(p1.getName() + ": choose Rock, Paper, or Scissors");
        scoreLabel.setText(scoreText());

        JPanel top = new JPanel(new GridLayout(2,1,4,4));
        top.add(roundLabel);
        top.add(turnLabel);

        buttonPanel.removeAll();
        Font emoji = pickEmojiFont(32);
        addChoiceButton("✊", "Rock", emoji);
        addChoiceButton("✋", "Paper", emoji);
        addChoiceButton("✌️", "Scissors", emoji);

        add(top, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        add(scoreLabel, BorderLayout.PAGE_END);

        acceptGeneric("RPS ready"); // generic method demonstration
        return root;
    }

    @Override
    public String getName() { return "Rock Paper Scissors"; }

    private void setupPlayersAndRounds() {
        p1 = new Player(promptName("Player 1 Name:"));
        p2 = new Player(promptName("Player 2 Name:"));
        totalRounds = parseIntWithBounds("Number of Rounds (max 10):", 1, 10, 1);
    }

    private String promptName(String prompt) {
        while (true) {
            String n = JOptionPane.showInputDialog(this, prompt);
            try {
                Player tmp = new Player(n);
                return tmp.getName();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Invalid name, try again.");
            }
        }
    }

    private int parseIntWithBounds(String prompt, int min, int max, int fallback) {
        int val = fallback;
        boolean ok = false;
        try {
            String s = JOptionPane.showInputDialog(this, prompt);
            val = Integer.parseInt(s);
            ok = true;
        } catch (Exception ignored) {
        } finally {
            if (!ok) val = fallback;
        }
        return Math.max(min, Math.min(max, val));
    }

    private void addChoiceButton(String emoji, String choice, Font emojiFont) {
        JButton btn = new JButton(emoji);
        btn.setFont(emojiFont);
        btn.setActionCommand(choice);
        btn.setToolTipText(choice + " " + emoji);
        btn.addActionListener(e -> handlePlayerChoice(e.getActionCommand()));
        buttonPanel.add(btn);
    }

    private Font pickEmojiFont(int size) {
        String[] candidates = {
            "Segoe UI Emoji", "Apple Color Emoji", "Noto Color Emoji", "Segoe UI Symbol", "SansSerif"
        };
        List<String> installed = Arrays.asList(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()
        );
        for (String fam : candidates) {
            if (installed.contains(fam)) {
                return new Font(fam, Font.PLAIN, size);
            }
        }
        return new Font("SansSerif", Font.PLAIN, size);
    }

    private void handlePlayerChoice(String choice) {
        if (isP1Turn) {
            p1Choice = choice;
            isP1Turn = false;
            turnLabel.setText(p2.getName() + "'s turn");
            statusLabel.setText(p2.getName() + ": choose Rock, Paper, or Scissors");
        } else {
            p2Choice = choice;
            evaluateRound();
            isP1Turn = true;
            turnLabel.setText(p1.getName() + "'s turn");
            statusLabel.setText(p1.getName() + ": choose Rock, Paper, or Scissors");
        }
    }

    private void evaluateRound() {
        int outcome = compare(p1Choice, p2Choice);
        String choiceSummary = p1.getName() + " chose: " + p1Choice + "\n" +
                               p2.getName() + " chose: " + p2Choice + "\n";
        String resultText;
        if (outcome == 0) {
            resultText = "It's a Draw!";
        } else if (outcome == 1) {
            p1.incrementScore();
            resultText = p1.getName() + " wins! " + p1Choice + " beats " + p2Choice;
        } else {
            p2.incrementScore();
            resultText = p2.getName() + " wins! " + p2Choice + " beats " + p1Choice;
        }

        JOptionPane.showMessageDialog(this,
                choiceSummary + "\n" + resultText,
                "Round " + currentRound + " Result", JOptionPane.INFORMATION_MESSAGE);

        // Update scoreboard only on round win to reflect meaningful wins
        if (outcome == 1) scoreboard.addWin(p1);
        if (outcome == 2) scoreboard.addWin(p2);

        p1Choice = null;
        p2Choice = null;
        scoreLabel.setText(scoreText());

        if (currentRound >= totalRounds) {
            String finalResult;
            if (p1.getScore() > p2.getScore()) {
                finalResult = p1.getName() + " wins the match!";
                scoreboard.addWin(p1); // add match win as well
            } else if (p2.getScore() > p1.getScore()) {
                finalResult = p2.getName() + " wins the match!";
                scoreboard.addWin(p2);
            } else {
                finalResult = "The match ends in a tie!";
            }

            JOptionPane.showMessageDialog(this,
                    "Final Scores:\n" + scoreText() + "\n\n" + finalResult,
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);

            FileUtil.appendLog("scores.txt", "[RPS] " + ("Final Scores: " + scoreText() + " | " + finalResult).replace("\n"," | "));
            backToMenu.run();
        } else {
            currentRound++;
            roundLabel.setText("Round " + currentRound + " of " + totalRounds);
        }
    }

    private int compare(String p1c, String p2c) {
        if (p1c.equals(p2c)) return 0;
        if ((p1c.equals("Rock") && p2c.equals("Scissors")) ||
            (p1c.equals("Paper") && p2c.equals("Rock")) ||
            (p1c.equals("Scissors") && p2c.equals("Paper"))) return 1;
        return 2;
    }

    private String scoreText() {
        return p1.getName() + ": " + p1.getScore() + " | " + p2.getName() + ": " + p2.getScore();
    }
        @Override
public void reset() {
    // Optional: reset game state
    p1 = null;
    p2 = null;
    p1Choice = null;
    p2Choice = null;
    isP1Turn = true;
    totalRounds = 1;
    currentRound = 1;
    scoreLabel.setText("");
    roundLabel.setText("");
    statusLabel.setText("");
    buttonPanel.removeAll();
    repaint();
}

}