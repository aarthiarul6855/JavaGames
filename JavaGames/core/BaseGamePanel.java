package core;

import javax.swing.*;
import java.awt.*;
// Abstraction via abstract class + method overloading (compile-time polymorphism)
public abstract class BaseGamePanel extends JPanel implements Game {
    protected final ScoreBoard<? super PlayerBase> scoreboard;

    protected BaseGamePanel(ScoreBoard<? super PlayerBase> scoreboard) {
        this.scoreboard = scoreboard;
        setLayout(new BorderLayout());
    }

    // Overloaded helpers (compile-time polymorphism)
    protected void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    protected void showInfo(String title, String msg) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // Example generic method accepting any T (explicit checklist item)
    protected <T> void acceptGeneric(T data) {
        
    }
}
