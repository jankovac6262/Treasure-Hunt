package entity;

import hlavnetriedy.GamePanel;
import mapa.BlokManager;
import util.NacitavacObrazkov;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Zombie — nepriateľ útočiaci na blízko.
 *
 * <p>Aktívne: beží k hráčovi; keď sa dostane na útočný dosah, zastaví
 * a spôsobí zásah. Úspešný zásah vyvolá knockback (odhodenie od hráča),
 * aby mal hráč chvíľku na reakciu. Štít zablokuje útok a knockback nenastane.</p>
 *
 * <p>Pasívne: stojí na mieste a čaká.</p>
 */
public class Zombie extends Nepriatel {

    // Dva framy animácie chôdze - prepínajú sa, kým sa zombie pohybuje.
    private final BufferedImage down1;
    private final BufferedImage down2;
    private int spriteCounter;
    private int spriteNum;

    /**
     * Vytvorí zombie na zadanej pozícii v mape.
     *
     * @param gamePanel  herný panel (prístup k manažérom a veľkosti dlaždíc)
     * @param hrac       hráč (cieľ útoku)
     * @param mapaX      počiatočná svetová X-súradnica v pixeloch
     * @param mapaY      počiatočná svetová Y-súradnica v pixeloch
     */
    public Zombie(GamePanel gamePanel, Hrac hrac, int mapaX, int mapaY) {
        super(gamePanel, hrac, mapaX, mapaY);
        super.setSpeed(2);
        super.setDetekcnyRadius(gamePanel.getTileSize() * 6);

        this.down1 = NacitavacObrazkov.nacitaj("/nepriatelia/zombie_down1.png");
        this.down2 = NacitavacObrazkov.nacitaj("/nepriatelia/zombie_down2.png");
        this.spriteNum = 1;
        this.spriteCounter = 0;
    }

    /*
     * Zombie sa pohybuje smerom k hráčovi - pred posunom kontroluje:
     *   - kolíziu so stenami mapy
     *   - prekryv s hráčom (aby sa nepostavil presne na neho a nezamotal štít)
     *   - prekryv s inými nepriateľmi (aby viacerí zombíci nestáli cez seba)
     *
     * Keď sa dostane na "útočný dosah" (1 tile od hráča), zastane a útočí
     * z jasnej pozície vedľa hráča. Vďaka tomu má štít vždy jednoznačný
     * smer zdroja útoku.
     *
     * Po úspešnom zásahu (nie zablokovanom štítom) sa zombie odhodí preč
     * od hráča - knockback dá hráčovi sekundu na otočenie / útek.
     */
    @Override
    protected void aktivnaReakcia(Hrac hrac) {
        int tileSize = this.getGamePanel().getTileSize();
        double vzd = super.vzdialenostOdHraca();

        boolean pohyb = false;
        // Útočný dosah - zombie sa zastaví na túto vzdialenosť a útočí.
        // 1.1 * tileSize dáva malú toleranciu, aby pri "kĺzaní" pozdĺž steny
        // nezacyklil pohyb-zastav-pohyb každý frame.
        double utocnyDosah = tileSize * 1.1;

        if (vzd > utocnyDosah) {
            pohyb = this.pohniSaKHracovi(hrac);
        }

        this.aktualizujAnimaciu(pohyb);

        // Útok keď je dosť blízko - testujeme vzdialenosťou, nie prekryvom,
        // lebo prekryv kolízne testy aktívne nedovoľujú.
        if (vzd <= utocnyDosah) {
            boolean zasiahol = hrac.prijmiZasah(1, super.getMapaX(), super.getMapaY());
            if (zasiahol) {
                this.knockback(hrac);
            }
        }
    }

    /*
     * Posun o jeden krok k hráčovi s tromi kolíznymi testami (stena, hráč,
     * iný nepriateľ). Vracia true, ak sa podarilo reálne pohnúť aspoň o pixel.
     */
    private boolean pohniSaKHracovi(Hrac hrac) {
        int speed = super.getSpeed();
        int tileSize = this.getGamePanel().getTileSize();
        BlokManager bm = this.getGamePanel().getBlokManager();

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

        // Test X osi - stena, prekryv s hráčom, prekryv s iným nepriateľom.
        if (!bm.koliduje(novyX, super.getMapaY(), tileSize, tileSize)
            && !hrac.prekryvaSa(novyX, super.getMapaY(), tileSize, tileSize)
            && !this.getGamePanel().getNepriatelManager().kolidujeSInym(this, novyX, super.getMapaY(), tileSize, tileSize)) {
            super.setMapaX(novyX);
        }
        // Test Y osi - rovnaké tri kontroly.
        if (!bm.koliduje(super.getMapaX(), novyY, tileSize, tileSize)
            && !hrac.prekryvaSa(super.getMapaX(), novyY, tileSize, tileSize)
            && !this.getGamePanel().getNepriatelManager().kolidujeSInym(this, super.getMapaX(), novyY, tileSize, tileSize)) {
            super.setMapaY(novyY);
        }

        return super.getMapaX() != staryX || super.getMapaY() != staryY;
    }

    /*
     * Odhodí zombieho preč od hráča o pol tila (smer = signum rozdielu pozícií).
     * Rešpektuje steny aj iných nepriateľov - knockback ich nepretlačí.
     */
    private void knockback(Hrac hrac) {
        int tileSize = this.getGamePanel().getTileSize();
        int sila = tileSize / 2;
        BlokManager bm = this.getGamePanel().getBlokManager();

        int dx = Integer.signum(super.getMapaX() - hrac.getMapaX());
        int dy = Integer.signum(super.getMapaY() - hrac.getMapaY());

        int novyX = super.getMapaX() + dx * sila;
        int novyY = super.getMapaY() + dy * sila;

        if (!bm.koliduje(novyX, super.getMapaY(), tileSize, tileSize)
            && !this.getGamePanel().getNepriatelManager().kolidujeSInym(this, novyX, super.getMapaY(), tileSize, tileSize)) {
            super.setMapaX(novyX);
        }
        if (!bm.koliduje(super.getMapaX(), novyY, tileSize, tileSize)
            && !this.getGamePanel().getNepriatelManager().kolidujeSInym(this, super.getMapaX(), novyY, tileSize, tileSize)) {
            super.setMapaY(novyY);
        }
    }

    @Override
    protected void pasivnaReakcia() {
        this.aktualizujAnimaciu(false);
    }

    /*
     * Spoločná logika prepínania spritov:
     *   - pri pohybe každých ~10 framov vymeníme down1 <-> down2
     *   - v kľude resetujeme na základný sprite (down1)
     */
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
     * Vykreslí zombie na jeho aktuálnej pozícii s kamerovým posunom.
     *
     * @param g2d  grafický kontext Swing
     */
    @Override
    public void draw(Graphics2D g2d) {
        BufferedImage obrazok = (this.spriteNum == 1) ? this.down1 : this.down2;
        g2d.drawImage(
                obrazok,
                super.getScreenX(),
                super.getScreenY(),
                this.getGamePanel().getTileSize(),
                this.getGamePanel().getTileSize(),
                null
        );
    }


}