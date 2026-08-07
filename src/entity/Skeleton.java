package entity;

import hlavnetriedy.GamePanel;
import util.NacitavacObrazkov;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Skeleton (lukostrelec) — nepriateľ útočiaci na diaľku.
 *
 * <p>Aktívne: ak je hráč príliš blízko, ustúpi opačným smerom; ak je v ideálnej
 * vzdialenosti, stojí a po cooldowne vystrelí projektil mierený priamo na hráča.</p>
 *
 * <p>Pasívne: stojí a nuluje stav strieľania.</p>
 *
 * <p>Demonštruje polymorfizmus: správanie je úplne odlišné od Zombie,
 * no manažér ho spravuje cez rovnaké rozhranie {@code Nepriatel}.</p>
 */
public class Skeleton extends Nepriatel {

    private final int idealnaVzdialenost;
    private final int cooldownMax;
    private int cooldownStrelby;

    // Dva framy animácie - prepínajú sa, kým sa kostlivec hýbe (ustupuje).
    private final BufferedImage down1;
    private final BufferedImage down2;
    private int spriteCounter;
    private int spriteNum;

    /**
     * Vytvorí skeleton na zadanej pozícii v mape.
     *
     * @param gamePanel  herný panel (prístup k manažérom a veľkosti dlaždíc)
     * @param hrac       hráč (cieľ strieľania)
     * @param mapaX      počiatočná svetová X-súradnica v pixeloch
     * @param mapaY      počiatočná svetová Y-súradnica v pixeloch
     */
    public Skeleton(GamePanel gamePanel, Hrac hrac, int mapaX, int mapaY) {
        super(gamePanel, hrac, mapaX, mapaY);
        super.setSpeed(3);
        super.setDetekcnyRadius(gamePanel.getTileSize() * 8);
        this.idealnaVzdialenost = gamePanel.getTileSize() * 4;
        this.cooldownMax = 90;
        this.cooldownStrelby = 0;

        this.down1 = NacitavacObrazkov.nacitaj("/nepriatelia/skeleton_down1.png");
        this.down2 = NacitavacObrazkov.nacitaj("/nepriatelia/skeleton_down2.png");
        this.spriteNum = 1;
        this.spriteCounter = 0;
    }

    @Override
    protected void aktivnaReakcia(Hrac hrac) {
        double vzd = super.vzdialenostOdHraca();

        if (vzd < this.idealnaVzdialenost) {
            // Hráč je príliš blízko - kostlivec ustúpi opačným smerom.
            this.kroknutie(hrac, -1);
        } else {
            // Ideálna vzdialenosť - stojí a strieľa po cooldowne.
            this.aktualizujAnimaciu(false);
            if (this.cooldownStrelby > 0) {
                this.cooldownStrelby--;
            } else {
                this.vystrel(hrac);
                this.cooldownStrelby = this.cooldownMax;
            }
        }
    }

    @Override
    protected void pasivnaReakcia() {
        this.cooldownStrelby = 0;
        this.aktualizujAnimaciu(false);
    }

    /*
     * Vytvorí novú Strelu mieriacu presne na hráča.
     *
     * Smer počítame ako normalizovaný vektor:
     *   dx = (hrac.X - moja.X) / vzdialenost
     *   dy = (hrac.Y - moja.Y) / vzdialenost
     * Tým získame jednotkový vektor mieriaci k hráčovi pod ľubovoľným uhlom,
     * nie len v 8 hlavných smeroch.
     */
    private void vystrel(Hrac hrac) {
        double dx = hrac.getMapaX() - super.getMapaX();
        double dy = hrac.getMapaY() - super.getMapaY();
        double vzd = Math.sqrt(dx * dx + dy * dy);

        // Poistka pre prípad, že kostlivec stojí presne na hráčovi - nedelíme nulou.
        if (vzd == 0) {
            return;
        }

        double smerX = dx / vzd;
        double smerY = dy / vzd;

        int tileSize = this.getGamePanel().getTileSize();
        int startX = super.getMapaX() + tileSize / 2;
        int startY = super.getMapaY() + tileSize / 2;

        Strela strela = new Strela(this.getGamePanel(), hrac, startX, startY, smerX, smerY);
        this.getGamePanel().getStrelaManager().pridajStrelu(strela);
    }

    /*
     * Posunie kostlivca o jeden krok smerom k hráčovi (smer = +1)
     * alebo preč od hráča (smer = -1). Rešpektuje mapu - cez stenu neprejde.
     */
    private void kroknutie(Hrac hrac, int smer) {
        int speed = super.getSpeed() * smer;
        int tileSize = this.getGamePanel().getTileSize();

        int novyX = super.getMapaX();
        int novyY = super.getMapaY();

        if (hrac.getMapaX() > super.getMapaX()) {
            novyX += speed;
        } else if (hrac.getMapaX() < super.getMapaX()) {
            novyX -= speed;
        }
        if (hrac.getMapaY() > super.getMapaY()) {
            novyY += speed;
        } else if (hrac.getMapaY() < super.getMapaY()) {
            novyY -= speed;
        }

        int staryX = super.getMapaX();
        int staryY = super.getMapaY();

        if (!this.getGamePanel().getBlokManager().koliduje(novyX, super.getMapaY(), tileSize, tileSize)
            && !hrac.prekryvaSa(novyX, super.getMapaY(), tileSize, tileSize)
            && !this.getGamePanel().getNepriatelManager().kolidujeSInym(this, novyX, super.getMapaY(), tileSize, tileSize)) {
            super.setMapaX(novyX);
        }
        if (!this.getGamePanel().getBlokManager().koliduje(super.getMapaX(), novyY, tileSize, tileSize)
            && !hrac.prekryvaSa(super.getMapaX(), novyY, tileSize, tileSize)
            && !this.getGamePanel().getNepriatelManager().kolidujeSInym(this, super.getMapaX(), novyY, tileSize, tileSize)) {
            super.setMapaY(novyY);
        }

        boolean pohyb = super.getMapaX() != staryX || super.getMapaY() != staryY;
        this.aktualizujAnimaciu(pohyb);
    }

    private void aktualizujAnimaciu(boolean pohyb) {
        if (pohyb) {
            this.spriteCounter++;
            if (this.spriteCounter > 10) {
                this.spriteNum = (this.spriteNum == 1) ? 2 : 1;
                this.spriteCounter = 0;
            }
        } else {
            this.spriteNum = 1;
            this.spriteCounter = 0;
        }
    }

    /**
     * Vykreslí skeleton na jeho aktuálnej pozícii s kamerovým posunom.
     *
     * @param g2d  grafický kontext Swing
     */
    @Override
    public void draw(Graphics2D g2d) {
        int tileSize = this.getGamePanel().getTileSize();
        BufferedImage obrazok = (this.spriteNum == 1) ? this.down1 : this.down2;
        g2d.drawImage(obrazok, super.getScreenX(), super.getScreenY(), tileSize, tileSize, null);
    }
}