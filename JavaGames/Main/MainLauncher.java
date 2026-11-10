package Main;
import javax.swing.SwingUtilities;
import launcher.GameLauncherGUI;


public class MainLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameLauncherGUI().setVisible(true));
    }
}
