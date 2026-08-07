package hlavnetriedy;

import entity.Hrac;
import entity.NepriatelManager;
import entity.StrelaManager;
import mapa.BlokManager;
import predmety.PredmetManager;
import vylepsenia.Inventar;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;

/**
 * Hlavný herný panel — centrálny bod celej hry.
 *
 * <p>Rozširuje {@code JPanel} a implementuje {@code Runnable}: herná slučka beží
 * vo vlastnom vlákne a riadi aktualizáciu stavu aj vykresľovanie pri cieľových
 * 60 FPS. Panel obsahuje všetkých manažérov ({@link BlokManager}, {@link NepriatelManager},
 * {@link StrelaManager}, {@link PredmetManager}) aj hráča ({@link Hrac}).</p>
 *
 * <p>Stav hry je riadený enumeráciou {@link HernyStav}: kým je {@code HRA},
 * slučka funguje normálne; po prechode do {@code VYHRA} alebo {@code PORAZKA}
 * sa aktualizácie zastavia a na obrazovku sa vykreslí záverečný overlay.</p>
 */
public class GamePanel extends JPanel implements Runnable {

    private final int originalnaVelkost = 16;
    private final int scale = 3;

    private final int tileSize = this.originalnaVelkost * this.scale;
    private final int maxPocetStlpcov = 16;
    private final int maxPocetRiadkov = 12;

    private final int screenWidth = this.tileSize * this.maxPocetStlpcov;
    private final int screenHeight = this.tileSize * this.maxPocetRiadkov;

    private KeyHandler keyHandler;
    private Thread gameThread;
    private BlokManager blokManager;
    private NepriatelManager nepriatelManager;
    private StrelaManager strelaManager;
    private PredmetManager predmetManager;
    private Hrac hrac;
    private JButton inventarButton;

    /**
     * Herný stav riadiaci priebeh hernej slučky a vykreslenia.
     * Prechody sú jednosmerné: {@code HRA} → {@code VYHRA} alebo {@code HRA} → {@code PORAZKA}.
     */
    public enum HernyStav {
        /** Hra beží normálne. */
        HRA,
        /** Hráč otvoril truhlu so všetkými kľúčmi — zobrazí sa výherná obrazovka. */
        VYHRA,
        /** Hráč prišiel o všetky životy — zobrazí sa game-over obrazovka. */
        PORAZKA
    }
    private HernyStav stav = HernyStav.HRA;

    private final int fps = 60;
    private int aktualnyFPS = 0;

    /**
     * Inicializuje všetkých manažérov, hráča, klávesnicu a tlačidlo inventára.
     * Panel je nastavený na dvojité bufferovanie a {@code null} layout pre fixné
     * umiestnenie tlačidla.
     */
    public GamePanel() {

        this.gameThread = new Thread(this);
        this.keyHandler = new KeyHandler();
        this.blokManager = new BlokManager(this);

        this.hrac = new Hrac(this, this.keyHandler);
        this.hrac.nacitajZaciatocneSuradnice();

        // StrelaManager musí vzniknúť pred NepriatelManager-om - Skeleton si
        // pri konštrukcii síce StrelaManager neberie, ale vyžaduje ho pri
        // streľbe. Necháme ho dostupný cez getter.
        this.strelaManager = new StrelaManager();
        this.nepriatelManager = new NepriatelManager(this, this.hrac);
        this.predmetManager = new PredmetManager(this);

        this.setPreferredSize(new Dimension(this.screenWidth, this.screenHeight));

        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(this.keyHandler);
        this.setFocusable(true);

        // Null layout - paintComponent kreslí svojvoľne a my potrebujeme
        // umiestniť tlačidlo inventára na fixnú pozíciu nad herným plátnom.
        this.setLayout(null);
        this.inventarButton = new JButton("Inventar");
        this.inventarButton.setBounds(this.screenWidth - 130, this.screenHeight - 45, 120, 30);
        // setFocusable(false): tlačidlo nikdy nezoberie keyboard focus, takže
        // KeyHandler ďalej dostáva WASD aj po kliknutí.
        this.inventarButton.setFocusable(false);
        this.inventarButton.addActionListener(e -> this.openInventar());
        this.add(this.inventarButton);
    }

    /*
     * Otvorí modálny dialóg s inventárom. Resetneme KeyHandler, aby hráč
     * neostal "stláčať" smer počas dialógu (keyReleased prichádza len keď
     * je focus na hernom paneli, ten teraz prevzal dialóg).
     *
     * Game thread medzitým beží ďalej - nepriatelia sa pohybujú, čo je
     * úmyselná cena za otvorenie inventára uprostred boja.
     */
    private void openInventar() {
        this.keyHandler.resetVsetky();

        Inventar inv = new Inventar(this.hrac);

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Inventar", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(inv.getRootPanel());
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        // Po zatvorení dialógu vrátime focus späť na herný panel,
        // inak by WASD nereagovali.
        this.requestFocusInWindow();
    }

    /**
     * Vytvorí nové vlákno hernej slučky a spustí ho.
     * Volá sa z {@code Main} po tom, ako je okno viditeľné.
     */
    public void startGameThread() {
        this.gameThread = new Thread(this);
        this.gameThread.start();
    }

    /**
     * pri vytvarani tejto slučky som si pomohol tutorialom : https://www.youtube.com/watch?v=aFS9Whsoecc
     * Telo hernej slučky bežiacej vo vlákne {@code gameThread}.
     * Udržiava pevných {@value fps} snímok za sekundu pomocou delta-time
     * akumulátora; pri náhlom oneskorení obmedzuje počet dobiehacích
     * aktualizácií na maximálne 5 za snímok.
     */
    @Override
    public void run() {

        double drawInterval = 1_000_000_000.0 / this.fps;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCounter = 0;

        final int maxUpdates = 5;

        while (this.gameThread.isAlive()) {

            currentTime = System.nanoTime();
            long elapsed = currentTime - lastTime;
            lastTime = currentTime;

            delta += elapsed / drawInterval;
            timer += elapsed;


            if (delta > maxUpdates) {
                delta = 1;
            }

            int updates = 0;
            while (delta >= 1 && updates < maxUpdates) {
                this.update();
                delta--;
                updates++;
            }

            if (updates > 0) {
                this.repaint();
                drawCounter++;
            }

            if (timer >= 1_000_000_000L) {
                this.aktualnyFPS = drawCounter;
                drawCounter = 0;
                timer -= 1_000_000_000L;
            }


            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

        }

    }

    /**
     * Vykreslí jeden frame: mapa → predmety → nepriatelia → strely → hráč → HUD
     * → záverečný overlay (ak hra skončila).
     *
     * @param g  grafický kontext Swing (pretypovaný interne na {@link Graphics2D})
     */
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;

        // Poradie vykresľovania určuje vrstvy (od najspodnejšej po najvrchnejšiu):
        //   1. mapa (terén)  - cez kameru okolo hráča
        //   2. predmety      - na zemi, pred kreslením entít
        //   3. nepriatelia   - relatívne ku kamere
        //   4. strely        - nad nepriateľmi, ale pod hráčom
        //   5. hráč          - vždy v strede obrazovky
        this.blokManager.draw(g2d);
        this.predmetManager.draw(g2d);
        this.nepriatelManager.draw(g2d);
        this.strelaManager.draw(g2d);
        this.hrac.draw(g2d);

        // HUD - pevne ukotvený na obrazovke, bez kamerovej transformácie.
        g2d.setColor(Color.yellow);
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.drawString("FPS: " + this.aktualnyFPS, 10, 25);
        g2d.setColor(Color.red);
        g2d.drawString("Zivoty: " + this.hrac.getZivoty() + "/" + this.hrac.getMaxZivotov(), 10, 50);
        g2d.setColor(Color.white);
        g2d.drawString("Kluce: " + this.hrac.getPocetKlucov() + "/" + this.predmetManager.getPociatocnyPocetKlucov(), 10, 75);
        // Indikátory inventára - vidieť, či hráč už našiel meč / štít.
        if (this.hrac.mamMec()) {
            g2d.setColor(Color.lightGray);
            g2d.drawString("Mec", 10, 100);
        }
        if (this.hrac.mamStit()) {
            g2d.setColor(new Color(80, 160, 255));
            g2d.drawString("Stit", 80, 100);
        }

        // Inventár - počet/limit; hráč vidí, koľko vylepšení ešte môže pozbierať.
        g2d.setColor(Color.white);
        g2d.drawString("Inv: " + this.hrac.getInventar().size() + "/" + this.hrac.getMaxVylepseni(), 150, 100);

        // Aktívne boosty z elixírov - zostávajúce sekundy.
        int y = 125;
        if (this.hrac.jeBoostDamageAktivny()) {
            g2d.setColor(Color.orange);
            g2d.drawString("Damage+ " + this.hrac.getBoostDamageSekundy() + "s", 10, y);
            y += 25;
        }
        if (this.hrac.jeBoostSpeedAktivny()) {
            g2d.setColor(Color.cyan);
            g2d.drawString("Speed+ " + this.hrac.getBoostSpeedSekundy() + "s", 10, y);
        }

        // Overlay konca hry - prekreslí celé plátno tmavým závojom a vypíše
        // veľký nadpis. Kreslíme až nakoniec, aby bolo nad HUDom aj hráčom.
        if (this.stav != HernyStav.HRA) {
            this.kresliKoniecHry(g2d);
        }

        // Žiadne g2d.dispose() - g2d je iba pretypovaný parameter g, ktorý
        // patrí Swingu. Po paintComponent Swing volá paintChildren (kreslí
        // JButton inventára) na tom istom Graphicse - ak ho disposneme,
        // tlačidlo sa nevykreslí.
    }

    private void kresliKoniecHry(Graphics2D g2d) {
        // Polopriehľadný čierny závoj cez celý obraz - zvýrazní nadpis
        // a opticky "zhasne" hru pod ním.
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, this.screenWidth, this.screenHeight);

        String text;
        Color farba;
        if (this.stav == HernyStav.VYHRA) {
            text = "Vyhral si!";
            farba = Color.yellow;
        } else {
            text = "Game Over";
            farba = Color.red;
        }

        g2d.setColor(farba);
        g2d.setFont(new Font("Arial", Font.BOLD, 70));
        // FontMetrics nám dá presnú šírku textu - vystreďujeme cez (width - textWidth) / 2.
        FontMetrics fm = g2d.getFontMetrics();
        int x = (this.screenWidth - fm.stringWidth(text)) / 2;
        int y = this.screenHeight / 2;
        g2d.drawString(text, x, y);
    }

    /**
     * Posunie stav hry o jeden tik: aktualizuje hráča, nepriateľov, strely
     * a predmety. Po prechode do {@code VYHRA}/{@code PORAZKA} sa dalšie
     * aktualizácie preskočia — frame zostane "zamrznutý" pod overlayom.
     */
    public void update() {
        if (this.stav != HernyStav.HRA) {
            return;
        }

        this.hrac.update();
        this.nepriatelManager.update();
        this.strelaManager.update();
        this.predmetManager.update();

        // Porážka má prednosť pred výhrou - ak hráč zomrie v rovnakom tiku,
        // v ktorom by sa nazbieral posledný kľúč, vyhráva smrť.
        if (this.hrac.getZivoty() <= 0) {
            this.stav = HernyStav.PORAZKA;
            this.inventarButton.setEnabled(false);
        }
    }

    /**
     * Prepne stav hry do {@link HernyStav#VYHRA}.
     * Volá ho {@code Truhla} po splnení podmienok výhry.
     * Prechod je jednosmerný — z {@code VYHRA} späť do {@code HRA} nie je možné.
     */
    public void nastavVyhru() {
        if (this.stav == HernyStav.HRA) {
            this.stav = HernyStav.VYHRA;
            this.inventarButton.setEnabled(false);
        }
    }

    /**
     * Vráti veľkosť jednej dlaždice v pixeloch (základná veľkosť × mierka).
     *
     * @return veľkosť dlaždice v pixeloch
     */
    public int getTileSize() {
        return this.tileSize;
    }

    /**
     * Vráti šírku hernej obrazovky v pixeloch.
     *
     * @return šírka obrazovky
     */
    public int getScreenWidth() {
        return this.screenWidth;
    }

    /**
     * Vráti výšku hernej obrazovky v pixeloch.
     *
     * @return výška obrazovky
     */
    public int getScreenHeight() {
        return this.screenHeight;
    }

    /**
     * Vráti hráča.
     *
     * @return inštancia {@link Hrac}
     */
    public Hrac getHrac() {
        return this.hrac;
    }

    /**
     * Vráti manažér mapy.
     *
     * @return inštancia {@link BlokManager}
     */
    public BlokManager getBlokManager() {
        return this.blokManager;
    }

    /**
     * Vráti manažér striel.
     *
     * @return inštancia {@link StrelaManager}
     */
    public StrelaManager getStrelaManager() {
        return this.strelaManager;
    }

    /**
     * Vráti manažér nepriateľov.
     *
     * @return inštancia {@link NepriatelManager}
     */
    public NepriatelManager getNepriatelManager() {
        return this.nepriatelManager;
    }

    /**
     * Vráti manažér predmetov.
     *
     * @return inštancia {@link PredmetManager}
     */
    public PredmetManager getPredmetManager() {
        return this.predmetManager;
    }
}