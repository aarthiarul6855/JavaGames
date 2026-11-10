package core;

import javax.swing.*;

public interface Game {
    JPanel getPanel(Runnable backToMenu);
    void reset();
    String getName();
}
