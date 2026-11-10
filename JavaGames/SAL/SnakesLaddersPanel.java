package SAL;

import core.*;
import javax.swing.*;
//import java.awt.*;
import java.util.Random;
import java.util.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.function.BiConsumer;



class Snake { int head, tail; public Snake(int h, int t) { head=h; tail=t; } }
class Ladder { int start, end; public Ladder(int s,int e){start=s;end=e;} }

public class SnakesLaddersPanel extends BaseGamePanel {
    private Runnable backToMenu = () -> {};
    
    private Player[] players;
    private BoardPanel board;
    private JLabel infoLabel;
    private JLabel rollLabel;
    private JButton rollBtn;

    private int currentPlayer = 0;
    private boolean[] started;
    public SnakesLaddersPanel(ScoreBoard<? super PlayerBase> scoreboard) {
        super(scoreboard);
    }

    
    @Override
public JPanel getPanel(Runnable backToMenu) {
    this.backToMenu = backToMenu;
    JPanel slPanel = new JPanel(new BorderLayout(8, 8));

    // --- Initialize players ---
    int numPlayers = parseIntWithBounds("Number of Players (1-4):", 1, 4, 2);
    players = new Player[numPlayers];
    for (int i = 0; i < numPlayers; i++) {
        players[i] = new Player(promptName("Name for Player " + (i + 1)));
    }

    // --- Snakes and Ladders setup ---
    List<Snake> snakes = Arrays.asList(
        new Snake(16, 6), new Snake(48, 26), new Snake(62, 18), new Snake(95, 75)
    );
    List<Ladder> ladders = Arrays.asList(
        new Ladder(2, 38), new Ladder(7, 14), new Ladder(8, 31), new Ladder(28, 84)
    );

    // --- Initialize board and UI components ---
    board = new BoardPanel(players, snakes, ladders);
    rollBtn = new JButton("Roll Dice");
    infoLabel = new JLabel(players[0].getName() + "'s turn", SwingConstants.CENTER);
    rollLabel = new JLabel("Roll: -", SwingConstants.CENTER);

    JPanel south = new JPanel(new BorderLayout());
    south.add(rollBtn, BorderLayout.CENTER);
    south.add(rollLabel, BorderLayout.EAST);

    slPanel.add(board, BorderLayout.CENTER);
    slPanel.add(infoLabel, BorderLayout.NORTH);
    slPanel.add(south, BorderLayout.SOUTH);

    // --- Game state tracking ---
    currentPlayer = 0;
    started = new boolean[numPlayers];
    Dice dice = new Dice();

    // --- Roll button action ---
    rollBtn.addActionListener((ActionEvent e) -> {
        Player p = players[currentPlayer];
        int roll = dice.roll();
        rollLabel.setText("Roll: " + roll);

        BiConsumer<Player, Integer> afterWinCheck = (pl, at) -> {
            if (at == 100) {
                JOptionPane.showMessageDialog(slPanel, pl.getName() + " wins Snakes & Ladders!");
                scoreboard.addWin(pl);
                FileUtil.appendLog("scores.txt", "[S&L] Winner: " + pl.getName());
                rollBtn.setEnabled(false);
                backToMenu.run();
            } else {
                infoLabel.setText(players[currentPlayer].getName() + "'s turn");
            }
        };

        // --- Must roll 6 to start ---
        if (!started[currentPlayer]) {
            if (roll != 6) {
                currentPlayer = (currentPlayer + 1) % players.length;
                infoLabel.setText(players[currentPlayer].getName() + "'s turn");
                return;
            }

            started[currentPlayer] = true;
            List<Integer> toOne = Collections.singletonList(1);
            board.animatePath(p, toOne, () -> infoLabel.setText(p.getName() + "'s turn"));
            return;
        }

        // --- Normal move ---
        int start = p.getPosition();
        int dest = start + roll;

        if (dest > 100) {
            JOptionPane.showMessageDialog(slPanel,
                    p.getName() + " rolled " + roll + ". Need exact roll to reach 100. Stay at " + p.getPosition());
            currentPlayer = (currentPlayer + 1) % players.length;
            infoLabel.setText(players[currentPlayer].getName() + "'s turn");
            return;
        }

        List<Integer> stepPath = new ArrayList<>();
        for (int s2 = start + 1; s2 <= dest; s2++) stepPath.add(s2);
        boolean rolledSix = (roll == 6);

        board.animatePath(p, stepPath, () -> {
            int landing = dest;
            for (Ladder l : ladders) if (l.start == dest) landing = l.end;
            for (Snake s : snakes) if (s.head == dest) landing = s.tail;

            final int landed = landing;
            Runnable finishTurn = () -> {
                if (landed == 100) {
                    afterWinCheck.accept(p, landed);
                    return;
                }
                if (rolledSix) {
                    infoLabel.setText(p.getName() + "'s turn");
                } else {
                    currentPlayer = (currentPlayer + 1) % players.length;
                    infoLabel.setText(players[currentPlayer].getName() + "'s turn");
                }
            };

            if (landed != dest) {
                board.animatePath(p, Collections.singletonList(landed), finishTurn);
            } else {
                finishTurn.run();
            }
        });
    });

    acceptGeneric("S&L ready"); // generic method demo
    return slPanel;
}

    @Override
    public String getName() { return "Snakes & Ladders"; }

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

    // Visual board panel
    static class BoardPanel extends JPanel {
        private final Player[] players;
        private final List<Snake> snakes;
        private final List<Ladder> ladders;

        private javax.swing.Timer anim;
        private List<Integer> path = Collections.emptyList();
        private int pathIdx = 0;
        private Runnable onAnimDone = () -> {};

        BoardPanel(Player[] players, List<Snake> snakes, List<Ladder> ladders) {
            this.players = players;
            this.snakes = snakes;
            this.ladders = ladders;
            setPreferredSize(new Dimension(520, 520));
            setBackground(Color.WHITE);
        }

        private Point squareToCell(int square) {
            if (square < 1) square = 1;
            if (square > 100) square = 100;
            int idx = square - 1;
            int row = idx / 10;
            int col = idx % 10;
            if (row % 2 == 1) col = 9 - col;  // serpentine
            int drawRow = 9 - row;
            return new Point(col, drawRow);
        }

        private Point cellCenterPx(int col, int row) {
            int w = getWidth(), h = getHeight();
            int pad = 20;
            int cell = Math.min((w - 2*pad)/10, (h - 2*pad)/10);
            int startX = (w - 10*cell)/2;
            int startY = (h - 10*cell)/2;
            int cx = startX + col*cell + cell/2;
            int cy = startY + row*cell + cell/2;
            return new Point(cx, cy);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int pad = 20;
            int cell = Math.min((w - 2*pad)/10, (h - 2*pad)/10);
            int startX = (w - 10*cell)/2;
            int startY = (h - 10*cell)/2;

            // Grid
            g2.setColor(Color.LIGHT_GRAY);
            for (int i = 0; i <= 10; i++) {
                g2.drawLine(startX, startY + i*cell, startX + 10*cell, startY + i*cell);
                g2.drawLine(startX + i*cell, startY, startX + i*cell, startY + 10*cell);
            }

            // Numbers
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(getFont().deriveFont(12f));
            for (int s = 1; s <= 100; s++) {
                Point rc = squareToCell(s);
                int x = startX + rc.x*cell + 4;
                int y = startY + rc.y*cell + 14;
                g2.drawString(String.valueOf(s), x, y);
            }

            // Ladders
            g2.setStroke(new BasicStroke(4f));
            g2.setColor(new Color(0, 150, 0));
            for (Ladder l : ladders) {
                Point aRC = squareToCell(l.start);
                Point bRC = squareToCell(l.end);
                Point a = cellCenterPx(aRC.x, aRC.y);
                Point b = cellCenterPx(bRC.x, bRC.y);
                g2.drawLine(a.x, a.y, b.x, b.y);
            }

            // Snakes
            g2.setColor(new Color(180, 30, 30));
            for (Snake s : snakes) {
                Point hRC = squareToCell(s.head);
                Point tRC = squareToCell(s.tail);
                Point a = cellCenterPx(hRC.x, hRC.y);
                Point b = cellCenterPx(tRC.x, tRC.y);
                g2.draw(new java.awt.geom.QuadCurve2D.Float(a.x, a.y, (a.x+b.x)/2f + 20f, (a.y+b.y)/2f - 20f, b.x, b.y));
            }

            // Player coins
            int r = Math.max(10, cell/4);
            Color[] colors = {
                new Color(50, 100, 220), new Color(240, 160, 0),
                new Color(120, 180, 80), new Color(170, 70, 160)
            };
            for (int i = 0; i < players.length; i++) {
                Player p = players[i];
                int sq = (p.getPosition() == 0) ? 1 : p.getPosition();
                Point rc = squareToCell(sq);
                Point cxy = cellCenterPx(rc.x, rc.y);
                int dx = (i % 2 == 0) ? -r/2 : r/2;
                int dy = (i / 2 == 0) ? -r/2 : r/2;
                g2.setColor(colors[i % colors.length]);
                g2.fillOval(cxy.x - r + dx, cxy.y - r + dy, 2*r, 2*r);
                g2.setColor(Color.BLACK);
                g2.drawOval(cxy.x - r + dx, cxy.y - r + dy, 2*r, 2*r);
            }
        }

        // Animate along a list of squares, then callback
        void animatePath(Player p, List<Integer> squares, Runnable onDone) {
            if (anim != null && anim.isRunning()) anim.stop();
            this.path = squares;
            this.pathIdx = 0;
            this.onAnimDone = onDone;
            anim = new javax.swing.Timer(120, e -> {
                if (pathIdx < path.size()) {
                    p.setPosition(path.get(pathIdx++));
                    repaint();
                } else {
                    ((javax.swing.Timer) e.getSource()).stop();
                    onAnimDone.run();
                }
            });
            anim.start();
        }
    }
    
@Override
public void reset() {
    // Reset player positions
    if (players != null) {
        for (Player p : players) {
            if (p != null) p.setPosition(0);
        }
    }

    // Reset board animation
    if (board != null) {
        board.path = java.util.Collections.emptyList();
        board.pathIdx = 0;
        board.onAnimDone = () -> {};
        board.repaint();
    }

    // Reset UI
    if (infoLabel != null) infoLabel.setText("");
    if (rollLabel != null) rollLabel.setText("");
    if (rollBtn != null) rollBtn.setEnabled(true);
}


}
