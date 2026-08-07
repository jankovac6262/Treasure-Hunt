package predmety;

import entity.Hrac;
import hlavnetriedy.GamePanel;
import util.NacitavacObrazkov;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Abstraktná báza pre všetky zberateľné predmety.
 *
 * <p>Drží pozíciu predmetu vo svete, obrázok textúry a poskytuje
 * spoločnú implementáciu  overlap-testu a kamerovo korektného
 * vykresľovania. Podtriedy dopĺňajú iba efekt ({@code aplikujUcinok})
 * a identifikátor ({@code getTyp}).</p>
 */
public abstract class PredmetBase implements Predmet {

    // Polia ostávajú private - potomkovia k nim pristupujú cez protected
    // gettery. mapaX/mapaY nie sú exponované, lebo sa používajú výhradne
    // v metódach tejto bázy (prekryvaSaSHracom, draw).
    private final GamePanel gamePanel;
    private final int mapaX;
    private final int mapaY;
    private final BufferedImage obrazok;

    /**
     * Inicializuje pozíciu predmetu a načíta textúru z classpath.
     *
     * @param gamePanel      herný panel (prístup ku kamere a veľkosti dlaždíc)
     * @param mapaX          svetová X-súradnica predmetu v pixeloch
     * @param mapaY          svetová Y-súradnica predmetu v pixeloch
     * @param cestaObrazka   cesta k textúre v classpath (napr. {@code "/predmety/key.png"})
     */
    public PredmetBase(GamePanel gamePanel, int mapaX, int mapaY, String cestaObrazka) {
        this.gamePanel = gamePanel;
        this.mapaX = mapaX;
        this.mapaY = mapaY;
        this.obrazok = NacitavacObrazkov.nacitaj(cestaObrazka);
    }

    /**
     * Vráti herný panel. Používajú ho podtriedy (napr. {@code Truhla}).
     *
     * @return herný panel
     */
    protected GamePanel getGamePanel() {
        return this.gamePanel;
    }

    /**
     * Vráti textúru predmetu. Používa ju napr. {@code Elixir.getIkona()}.
     *
     * @return textúra predmetu
     */
    protected BufferedImage getObrazok() {
        return this.obrazok;
    }

    /**
     * Testuje, či hráčov hitbox (s paddingom) zasahuje do plochy predmetu.
     * Predmet zaberá plochu jednej dlaždice.
     *
     * @param hrac  hráč, voči ktorému sa test vykonáva
     * @return {@code true} ak hráč stojí na predmete
     */
    @Override
    public boolean prekryvaSaSHracom(Hrac hrac) {
        int tileSize = this.gamePanel.getTileSize();
        return hrac.prekryvaSa(this.mapaX, this.mapaY, tileSize, tileSize);
    }

    /**
     * Vykreslí predmet na obrazovku s kamerovým posunom.
     *
     * @param g2d  grafický kontext Swing
     */
    @Override
    public void draw(Graphics2D g2d) {
        Hrac hrac = this.gamePanel.getHrac();
        int kameraX = hrac.getMapaX() - hrac.getOknoX();
        int kameraY = hrac.getMapaY() - hrac.getOknoY();
        int screenX = this.mapaX - kameraX;
        int screenY = this.mapaY - kameraY;

        int tileSize = this.gamePanel.getTileSize();
        g2d.drawImage(this.obrazok, screenX, screenY, tileSize, tileSize, null);
    }
}