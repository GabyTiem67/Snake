import javax.swing.JFrame;

public class GameFrame extends JFrame {

    public GameFrame() {
        // Create and add the game panel
        GamePanel gamePanel = new GamePanel();
        this.add(gamePanel);

        // Set window properties
        this.setTitle("Snake");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        
        // Optimize window sizing and positioning
        this.pack();
        this.setLocationRelativeTo(null);  // Center the window
        this.setVisible(true);  // Make visible after all setup
        
        // Optional: Ensure the game panel gets focus for keyboard input
        gamePanel.requestFocusInWindow();
    }

    // Optional: Main method if you want to run directly from this class
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new GameFrame());
    }
}