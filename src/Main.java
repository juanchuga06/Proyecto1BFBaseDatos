import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Esto hace que las ventanas se vean más modernas (estilo Windows/Mac nativo)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Iniciamos la aplicación abriendo el Login en un hilo seguro
        SwingUtilities.invokeLater(() -> {
            new Login().setVisible(true);
        });
    }
}