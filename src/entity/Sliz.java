package entity;

import hlavnetriedy.GamePanel;
import mapa.BlokManager;
import util.NacitavacObrazkov;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Sliz — nepriateľ pohybujúci sa horizontálnou hliadkou.
 *
 * <p>Chodí doprava, kým nenarazí na stenu alebo iného nepriateľa; vtedy
 * obráti smer. Pri prekryve s hráčom mu uberie život; rýchlosť zásahov
 * regulujú hráčove invuln-frames (nie vlastný cooldown).</p>
 *
 * <p>Detekčný rádius je nastavený na {@code Integer.MAX_VALUE} — sliz hliadkuje
 * stále a celá logika sa vykonáva v {@code aktivnaReakcia}.</p>
 */
public class Sliz extends Nepriatel {

    private static final int DAMAGE = 1;

    // Smer pohybu na osi X: +1 = doprava, -1 = doľava. Začíname doprava.
    private int smerX;

    // Dva framy animácie - prepínajú sa, kým sa sliz hýbe.
    private final BufferedImage frame1;
    private final BufferedImage frame2;
    private int spriteCounter;
    private int spriteNum;

    /**
     * Vytvorí sliz na zadanej pozícii v mape.
     *
     * @param gamePanel  herný panel (prístup k manažérom a veľkosti dlaždíc)
     * @param hrac       hráč (cieľ útoku a referencia pre výpočet vzdialenosti)
     * @param mapaX      počiatočná svetová X-súradnica v pixeloch
     * @param mapaY      počiatočná svetová Y-súradnica v pixeloch
     */
    public Sliz(GamePanel gamePanel, Hrac hrac, int mapaX, int mapaY) {
        super(gamePanel, hrac, mapaX, mapaY);
        super.setSpeed(2);
        // Vysoký rádius => template method vždy zvolí aktivnaReakcia.
        super.setDetekcnyRadius(Integer.MAX_VALUE);
        this.smerX = 1;

        this.frame1 = NacitavacObrazkov.nacitaj("/nepriatelia/slime_down.png");
        this.frame2 = NacitavacObrazkov.nacitaj("/nepriatelia/slime_up.png");
        this.spriteNum = 1;
        this.spriteCounter = 0;
    }

    @Override
    protected void aktivnaReakcia(Hrac hrac) {
        this.patroluj();
        this.skusZasiahnut(hrac);
    }

    @Override
    protected void pasivnaReakcia() {
        // Pre sliza nedosiahnuteľná vetva (detekcnyRadius = MAX_VALUE),
        // ale Nepriatel ju vyžaduje implementovať.
    }

    /*
     * Posun o jeden krok v aktuálnom smere. Ak by nový krok skončil v stene
     * alebo v inom nepriateľovi, otočíme smer a v tomto frame už nehýbeme.
     */
    private void patroluj() {
        int tileSize = this.getGamePanel().getTileSize();
        BlokManager bm = this.getGamePanel().getBlokManager();

        int novyX = super.getMapaX() + this.smerX * super.getSpeed();
        int mojaY = super.getMapaY();

        boolean naraz = bm.koliduje(novyX, mojaY, tileSize, tileSize)
                || this.getGamePanel().getNepriatelManager()
                       .kolidujeSInym(this, novyX, mojaY, tileSize, tileSize);

        if (naraz) {
            this.smerX = -this.smerX;
            this.aktualizujAnimaciu(false);
            return;
        }

        super.setMapaX(novyX);
        this.aktualizujAnimaciu(true);
    }

    /*
     * Ak sa sliz prekrýva s hráčom, pošle mu zásah. Rýchlosť zásahov regulujú
     * hráčove invuln-frames — vlastný cooldown nie je potrebný.
     */
    private void skusZasiahnut(Hrac hrac) {
        int tileSize = this.getGamePanel().getTileSize();
        if (hrac.prekryvaSa(super.getMapaX(), super.getMapaY(), tileSize, tileSize)) {
            hrac.prijmiZasah(DAMAGE, super.getMapaX(), super.getMapaY());
        }
    }

    private void aktualizujAnimaciu(boolean pohyb) {
        if (pohyb) {
            this.spriteCounter++;
            if (this.spriteCounter > 12) {
                this.spriteNum = (this.spriteNum == 1) ? 2 : 1;
                this.spriteCounter = 0;
            }
        } else {
            this.spriteNum = 1;
            this.spriteCounter = 0;
        }
    }

    /**
     * Vykreslí sliz na jeho aktuálnej pozícii s kamerovým posunom.
     *
     * @param g2d  grafický kontext Swing
     */
    @Override
    public void draw(Graphics2D g2d) {
        int tileSize = this.getGamePanel().getTileSize();
        BufferedImage obrazok = (this.spriteNum == 1) ? this.frame1 : this.frame2;
        g2d.drawImage(obrazok, super.getScreenX(), super.getScreenY(), tileSize, tileSize, null);
    }
}