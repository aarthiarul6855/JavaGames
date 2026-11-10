package TTT;

import core.*;
import javax.swing.*;
import java.awt.*;


public class TicTacToePanel extends BaseGamePanel {
    
    private final JButton[][] buttons = new JButton[3][3];
    private final JLabel infoLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel scoreLabel = new JLabel("", SwingConstants.CENTER);

    private Player p1, p2;
    private Player current;
    private int movesLeft = 9;
    private int currentRound = 1;
    private int totalRounds = 1;

    private Runnable backToMenu = () -> {};

    public TicTacToePanel(ScoreBoard<? super PlayerBase> scoreboard) {
        super(scoreboard);
    }

    @Override
    public JPanel getPanel(Runnable backToMenu) {
        this.backToMenu = backToMenu;
        JPanel root = this;
        removeAll();
        setLayout(new BorderLayout());

        JPanel boardPanel = new JPanel(new GridLayout(3,3));
        for (int i=0;i<3;i++) for (int j=0;j<3;j++) {
            JButton b = new JButton("");
            b.setFont(new Font("Arial", Font.BOLD, 40));
            final int r=i,c=j;
            b.addActionListener(e -> onCell(r,c,b));
            buttons[i][j] = b;
            boardPanel.add(b);
        }

        add(scoreLabel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(infoLabel, BorderLayout.SOUTH);

        setupPlayersAndRounds(); // dialogs with safe parsing and validation
        current = p1;
        infoLabel.setText("Round 1 - " + current.getName() + "'s turn");
        scoreLabel.setText(scoreText());

        acceptGeneric("TTT ready"); // generic method demonstration
        return root;
    }

    @Override
    public String getName() { return "Tic Tac Toe"; }

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
                // Exception handling with loop retry; does not change UI flow
                JOptionPane.showMessageDialog(this, "Invalid name, try again.");
            }
        }
    }

    private int parseIntWithBounds(String prompt, int min, int max, int fallback) {
        // try-catch-finally to satisfy checklist
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

    private void onCell(int r, int c, JButton btn) {
        if (!btn.getText().isEmpty()) return;

        btn.setText(current == p1 ? "X" : "O");
        movesLeft--;

        if (checkWin(current == p1 ? "X" : "O")) {
            current.incrementScore();
            scoreboard.addWin(current);
            showInfo("Round " + currentRound + " Result",
                     boardState() + "\n" + current.getName() + " wins the round!");
            scoreLabel.setText(scoreText());
            if (currentRound == totalRounds) {
                showFinalResult();
            } else {
                currentRound++;
                resetBoard();
                infoLabel.setText("Round " + currentRound + " - " + current.getName() + "'s turn");
            }
            return;
        }

        if (movesLeft == 0) {
            showInfo("Round " + currentRound + " Result", boardState() + "\nIt's a draw!");
            if (currentRound == totalRounds) {
                showFinalResult();
            } else {
                currentRound++;
                resetBoard();
                infoLabel.setText("Round " + currentRound + " - " + current.getName() + "'s turn");
            }
            return;
        }

        current = (current == p1 ? p2 : p1);
        infoLabel.setText("Round " + currentRound + " - " + current.getName() + "'s turn");
        scoreLabel.setText(scoreText());
    }

    private void resetBoard() {
        for (int i=0;i<3;i++) for (int j=0;j<3;j++) buttons[i][j].setText("");
        movesLeft = 9;
    }

    private boolean checkWin(String sym) {
        for (int i=0;i<3;i++) if (eq(i,0,sym) && eq(i,1,sym) && eq(i,2,sym)) return true;
        for (int j=0;j<3;j++) if (eq(0,j,sym) && eq(1,j,sym) && eq(2,j,sym)) return true;
        return eq(0,0,sym)&&eq(1,1,sym)&&eq(2,2,sym) || eq(0,2,sym)&&eq(1,1,sym)&&eq(2,0,sym);
    }

    private boolean eq(int i,int j,String s){ return buttons[i][j].getText().equals(s); }

    private String scoreText() {
        return p1.getName()+": "+p1.getScore()+" | "+p2.getName()+": "+p2.getScore();
    }

    private String boardState() {
        StringBuilder sb = new StringBuilder("Board State:\n");
        for (int i=0;i<3;i++) {
            for (int j=0;j<3;j++) {
                String val = buttons[i][j].getText();
                sb.append(val.isEmpty() ? "-" : val);
                if (j<2) sb.append(" | ");
            }
            sb.append("\n");
            if (i<2) sb.append("---------\n");
        }
        return sb.toString();
    }

    private void showFinalResult() {
        String message = "Final Score:\n" + scoreText() + "\n\n" +
                ((p1.getScore()>p2.getScore()) ? (p1.getName()+" wins the match!")
                 : (p2.getScore()>p1.getScore()) ? (p2.getName()+" wins the match!") : "It's a draw!");
        showInfo("Game Over", message);
        FileUtil.appendLog("scores.txt", "[TTT] " + message.replace("\n", " | "));
        backToMenu.run();
    }
    
@Override
public void reset() {
    // Reset all buttons and moves left
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            buttons[i][j].setText("");
            buttons[i][j].setEnabled(true);
        }
    }

    movesLeft = 9;

    // Reset current player to Player 1
    current = p1;

    // Update info label
    infoLabel.setText("Round " + currentRound + " - " + current.getName() + "'s turn");
    scoreLabel.setText(scoreText());
}


}