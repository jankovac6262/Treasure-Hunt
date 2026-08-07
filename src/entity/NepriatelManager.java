package entity;

import hlavnetriedy.GamePanel;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manažér nepriateľov — centralizuje ich životný cyklus.
 *
 * <p>Pri štarte načíta {@code /nepriatelia/nepriatelia.txt} (formát:
 * {@code <typ> <riadok> <stlpec>}) a cez factory metódu vytvorí konkrétne
 * podtriedy {@link Nepriatel}. Vďaka polymorfizmu pracuje ďalej len
 * s referenciou {@code Nepriatel} — pri {@link #update()} a {@link #draw(Graphics2D)}
 * nepozná skutočný typ.</p>
 *
 * <p>Pridanie nového nepriateľa = nová podtrieda + jeden {@code case} vo factory.</p>
 */
public class NepriatelManager {

    private final GamePanel gamePanel;
    private final Hrac hrac;
    private final List<Nepriatel> nepriatelia;

    /**
     * Vytvorí manažér a načíta nepriateľov z konfiguračného súboru.
     *
     * @param gamePanel  herný panel (prístup k manažérom a veľkosti dlaždíc)
     * @param hrac       hráč (odovzdáva sa konštruktoru každého nepriateľa)
     */
    public NepriatelManager(GamePanel gamePanel, Hrac hrac) {
        this.gamePanel = gamePanel;
        this.hrac = hrac;
        this.nepriatelia = new ArrayList<>();
        this.nacitajNepriatelov();
    }

    /*
     * Načíta konfiguráciu nepriateľov z textového súboru.
     * Prázdne riadky a riadky začínajúce '#' (komentár) sa ignorujú.
     */
    private void nacitajNepriatelov() {
        InputStream is = getClass().getResourceAsStream("/nepriatelia/nepriatelia.txt");
        if (is == null) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] tokeny = line.split(" ");
                String typ = tokeny[0];
                int riadok = Integer.parseInt(tokeny[1]);
                int stlpec = Integer.parseInt(tokeny[2]);

                int mapaX = stlpec * this.gamePanel.getTileSize();
                int mapaY = riadok * this.gamePanel.getTileSize();

                Nepriatel n = this.vyrobiNepriatela(typ, mapaX, mapaY);
                if (n != null) {
                    this.nepriatelia.add(n);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Jednoduchá factory metóda - mapuje typ z konfigu na konkrétnu triedu.
     * Pridanie nového nepriateľa = nová podtrieda + nový case.
     */
    private Nepriatel vyrobiNepriatela(String typ, int mapaX, int mapaY) {
        switch (typ) {
            case "zombie":
                return new Zombie(this.gamePanel, this.hrac, mapaX, mapaY);
            case "skeleton":
                return new Skeleton(this.gamePanel, this.hrac, mapaX, mapaY);
            case "sliz":
                return new Sliz(this.gamePanel, this.hrac, mapaX, mapaY);
            default:
                System.err.println("Neznamy typ nepriatela: " + typ);
                return null;
        }
    }

    /**
     * Posunie všetkých nepriateľov o jeden tik a odstráni tých, čo zomreli.
     */
    public void update() {
        Iterator<Nepriatel> it = this.nepriatelia.iterator();
        while (it.hasNext()) {
            Nepriatel n = it.next();
            n.update();
            if (n.jeMrtvy()) {
                it.remove();
            }
        }
    }

    /**
     * Vykreslí všetkých živých nepriateľov na obrazovku.
     *
     * @param g2d  grafický kontext Swing
     */
    public void draw(Graphics2D g2d) {
        for (Nepriatel n : this.nepriatelia) {
            n.draw(g2d);
        }
    }

    /**
     * Aplikuje útok mečom (obdĺžnikový hitbox) na všetkých nepriateľov
     * v dosahu — každému, kto sa s hitboxom prekrýva, uberie {@code damage} životov.
     *
     * @param worldX  ľavý okraj útočného hitboxu v svetových súradniciach
     * @param worldY  horný okraj útočného hitboxu v svetových súradniciach
     * @param sirka   šírka hitboxu v pixeloch
     * @param vyska   výška hitboxu v pixeloch
     * @param damage  počet životov na odobranie každému zasiahnutému nepriateľovi
     */
    public void zasahniHitbox(int worldX, int worldY, int sirka, int vyska, int damage) {
        for (Nepriatel n : this.nepriatelia) {
            if (n.prekryvaSa(worldX, worldY, sirka, vyska)) {
                n.uberZivot(damage);
            }
        }
    }

    /**
     * Testuje, či by sa zadaný obdĺžnik prekrýval s niektorým nepriateľom
     * okrem {@code ja}. Využívajú ho nepriatelia pri kolíznych testoch pohybu,
     * aby sa nepretlačili cez seba.
     *
     * @param ja      nepriateľ, ktorý sa testuje (vylúčený z porovnania)
     * @param worldX  ľavý okraj testovaného obdĺžnika
     * @param worldY  horný okraj testovaného obdĺžnika
     * @param sirka   šírka obdĺžnika v pixeloch
     * @param vyska   výška obdĺžnika v pixeloch
     * @return {@code true} ak nastáva kolízia s iným nepriateľom
     */
    public boolean kolidujeSInym(Nepriatel ja, int worldX, int worldY, int sirka, int vyska) {
        for (Nepriatel n : this.nepriatelia) {
            if (n == ja) {
                continue;
            }
            if (n.prekryvaSa(worldX, worldY, sirka, vyska)) {
                return true;
            }
        }
        return false;
    }
}