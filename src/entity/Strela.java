package entity;

import hlavnetriedy.GamePanel;
import mapa.BlokManager;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Projektil vystrelený nepriateľom (napr. Skeleton).
 *
 * <p>Smer je zadaný ako normalizovaný vektor {@code (smerX, smerY)}, vďaka čomu
 * strela letí presne na hráča pod ľubovoľným uhlom (nie len v 8 smeroch).
 * Pozícia je uložená ako {@code double} — pri smere napr. 0.97 by sa pri
 * celočíselnom sčítaní presnosť strácala.</p>
 *
 * <p>Po náraze na nepriechodný blok alebo po zasiahnutí hráča sa strela
 * označí za neaktívnu a {@code StrelaManager} ju v nasledujúcom tiku odstráni.</p>
 */
public class Strela {

    private final GamePanel gamePanel;
    private final Hrac hrac;
    private final BlokManager blokManager;

    private double mapaX;
    private double mapaY;
    private final double smerX;
    private final double smerY;
    private final int speed;
    private final int velkost;
    private boolean aktivna;

    /**
     * Vytvorí novú strelu s daným smerom a štartovacou pozíciou.
     *
     * @param gamePanel  herný panel (prístup k manažérom)
     * @param hrac       hráč (cieľ strely a referencia kamery)
     * @param mapaX      počiatočná svetová X-súradnica strely v pixeloch
     * @param mapaY      počiatočná svetová Y-súradnica strely v pixeloch
     * @param smerX      X-zložka normalizovaného smerového vektora
     * @param smerY      Y-zložka normalizovaného smerového vektora
     */
    public Strela(GamePanel gamePanel, Hrac hrac, int mapaX, int mapaY, double smerX, double smerY) {
        this.gamePanel = gamePanel;
        this.hrac = hrac;
        this.blokManager = gamePanel.getBlokManager();
        this.mapaX = mapaX;
        this.mapaY = mapaY;
        this.smerX = smerX;
        this.smerY = smerY;
        this.speed = 4;
        this.velkost = gamePanel.getTileSize() / 3;
        this.aktivna = true;
    }

    /**
     * Posunie strelu o jeden krok a overí kolízie so stenou a hráčom.
     * Ak nastane zásah, strela sa označí za neaktívnu.
     */
    public void update() {
        if (!this.aktivna) {
            return;
        }

        this.mapaX += this.smerX * this.speed;
        this.mapaY += this.smerY * this.speed;

        // BlokManager / Hrac pracujú s int súradnicami - pri teste kolízie
        // celočíselne zaokrúhlime aktuálnu polohu strely.
        int ix = (int)this.mapaX;
        int iy = (int)this.mapaY;

        if (this.blokManager.koliduje(ix, iy, this.velkost, this.velkost)) {
            this.aktivna = false;
            return;
        }

        if (this.hrac.prekryvaSa(ix, iy, this.velkost, this.velkost)) {
            // Zdroj = aktuálna pozícia strely - hrac.prijmiZasah si overí
            // smer štítu (ak je aktívny) a buď útok zablokuje, alebo uberie život.
            this.hrac.prijmiZasah(1, ix, iy);
            this.aktivna = false;
        }
    }

    /**
     * Vykreslí strelu ako tmavú guľku s kamerovým posunom.
     *
     * @param g2d  grafický kontext Swing
     */
    public void draw(Graphics2D g2d) {
        int kameraX = this.hrac.getMapaX() - this.hrac.getOknoX();
        int kameraY = this.hrac.getMapaY() - this.hrac.getOknoY();
        int screenX = (int)this.mapaX - kameraX;
        int screenY = (int)this.mapaY - kameraY;

        g2d.setColor(Color.darkGray);
        g2d.fillOval(screenX, screenY, this.velkost, this.velkost);
    }

    /**
     * Indikuje, či strela ešte letí (nebola zastavená stenou ani hráčom).
     *
     * @return {@code true} kým strela nie je deaktivovaná
     */
    public boolean jeAktivna() {
        return this.aktivna;
    }
}