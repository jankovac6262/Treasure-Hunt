package hlavnetriedy;

import javax.swing.JFrame;

/**
 * Vstupný bod aplikácie. Vytvorí hlavné okno, pridá herný panel a spustí hernú slučku.
 */
public class Main {

    /**
     * Hlavná metóda — inicializuje Swing okno a spúšťa hru.
     *
     * @param args  argumenty príkazového riadku (nevyužité)
     */
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Treasure hunt");

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        window.pack();

        window.setLocationRelativeTo(null);

        window.setVisible(true);


        gamePanel.startGameThread();
    }
}