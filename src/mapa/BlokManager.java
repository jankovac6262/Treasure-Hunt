package mapa;

import entity.Hrac;
import hlavnetriedy.GamePanel;
import util.NacitavacObrazkov;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Spravuje hernú mapu: načíta dlaždice a ich kolízne príznaky, vykresľuje
 * viditeľný výrez mapy okolo hráča a vyhodnocuje kolízie entít s terénom.
 *
 * <p>Mapa sa číta z textového súboru {@code /mapa/mapa.txt}; každé číslo
 * odkazuje na index v poli {@link Blok}. Vykreslenie používa kamerovú
 * transformáciu — hráč je vždy fixovaný v strede obrazovky.</p>
 */
public class BlokManager {

    private GamePanel gamePanel;
    private Blok[] bloky;
    private int[][] mapData;

    /**
     * Inicializuje manažér mapy: vytvorí pole dlaždíc, načíta typy blokov
     * a dáta mapy zo súboru.
     *
     * @param gamePanel  herný panel (prístup k rozmerom a hráčovi)
     */
    public BlokManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.bloky = new Blok[10];
        this.nacitajBloky();
        this.nacitajMapu();
    }

    private void nacitajBloky() {
        this.bloky[0] = new Blok(NacitavacObrazkov.nacitaj("/tiles/grass.png"), false);
        this.bloky[1] = new Blok(NacitavacObrazkov.nacitaj("/tiles/wall.png"),  true);
        this.bloky[2] = new Blok(NacitavacObrazkov.nacitaj("/tiles/tree_1.png"), true);
        this.bloky[3] = new Blok(NacitavacObrazkov.nacitaj("/tiles/water.png"), true);
        this.bloky[4] = new Blok(NacitavacObrazkov.nacitaj("/tiles/sand.png"),  false);
    }

    private void nacitajMapu() {
        try {
            InputStream is = getClass().getResourceAsStream("/mapa/mapa.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int riadok = 0;
            String line = br.readLine();

            String[] prvyRiadok = line.trim().split(" ");
            int pocetStlpcov = prvyRiadok.length;

            // Spočítaj počet riadkov
            int pocetRiadkov = 1;
            while (br.readLine() != null) {
                pocetRiadkov++;
            }
            br.close();




            this.mapData = new int[pocetRiadkov][pocetStlpcov];

            // Druhý prechod — načítaj hodnoty
            is = getClass().getResourceAsStream("/mapa/mapa.txt");
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                String[] tokeny = line.trim().split(" ");
                for (int stlpec = 0; stlpec < tokeny.length; stlpec++) {
                    this.mapData[riadok][stlpec] = Integer.parseInt(tokeny[stlpec]);
                }
                riadok++;
            }
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Vykreslí viditeľnú časť mapy okolo hráča (tzv. "first person" kamera).
     *
     * Princíp:
     *   - Mapa má pevné súradnice (svet) - každý blok má pozíciu (stlpec*tileSize, riadok*tileSize).
     *   - Hráč drží dve dvojice súradníc:
     *       mapaX/mapaY  ... kde sa nachádza v rámci celej mapy (svetové súradnice)
     *       oknoX/oknoY  ... fixná pozícia stredu obrazovky, kde sa hráč vždy vykresľuje
     *   - Aby hráč zostal opticky v strede okna, posunieme celý "svet" o opačnú hodnotu.
     *     Tento posun voláme KAMERA - jej ľavý horný roh v svetových súradniciach je:
     *         kameraX = hracMapaX - hracOknoX
     *         kameraY = hracMapaY - hracOknoY
     *   - Ľubovoľný bod sveta (worldX, worldY) sa potom premietne na obrazovku ako:
     *         screenX = worldX - kameraX
     *         screenY = worldY - kameraY
     *
     * Optimalizácia (frustum culling):
     *   - Mapa je veľká (70x70 blokov), no obrazovka pojme len malý výrez (~16x12).
     *   - Preto nevykresľujeme všetky bloky, ale len tie, ktorých index spadá
     *     do intervalu daného polohou kamery a rozmermi obrazovky.
     */
    /**
     * pri navrhu tohot algoritmu som si pomohol tutorialom :
     * https://www.youtube.com/watch?v=Ny_YHoTYcxo&list=PL_QPQmz5C6WUF-pOQDsbsKbaBZqXj4qSq&index=6
     * Vykreslí viditeľnú časť mapy okolo hráča pomocou kamerovej transformácie.
     * Bloky mimo obrazovky sa nevykresľujú (frustum culling).
     *
     * @param g2d  grafický kontext na kreslenie
     */
    public void draw(Graphics2D g2d) {
        int tileSize     = this.gamePanel.getTileSize();
        int screenWidth  = this.gamePanel.getScreenWidth();
        int screenHeight = this.gamePanel.getScreenHeight();

        // Zapuzdrený prístup k hráčovi cez GamePanel - BlokManager nepotrebuje
        // poznať implementačné detaily hráča, len verejné metódy z Entity/Hrac.
        Hrac hrac = this.gamePanel.getHrac();

        // Svetová pozícia hráča (kde sa nachádza v rámci celej mapy).
        int hracMapaX = hrac.getMapaX();
        int hracMapaY = hrac.getMapaY();

        // Pevná obrazovková pozícia hráča (stred okna).
        int hracOknoX = hrac.getOknoX();
        int hracOknoY = hrac.getOknoY();

        // Ľavý-horný roh kamery v súradniciach mapy.
        // Posun, ktorý musíme aplikovať na každý blok, aby bol hráč v strede.
        int kameraX = hracMapaX - hracOknoX;
        int kameraY = hracMapaY - hracOknoY;

        // Vypočítame, ktoré bloky mapy môžu byť reálne viditeľné na obrazovke.
        // Math.max/min slúži ako poistka na okraje mapy, aby sme neindexovali mimo poľa.
        int prvyStlpec    = Math.max(0, kameraX / tileSize);
        int prvyRiadok    = Math.max(0, kameraY / tileSize);
        int poslednyStlpec = Math.min(this.mapData[0].length, (kameraX + screenWidth)  / tileSize + 1);
        int poslednyRiadok = Math.min(this.mapData.length,    (kameraY + screenHeight) / tileSize + 1);

        for (int riadok = prvyRiadok; riadok < poslednyRiadok; riadok++) {
            for (int stlpec = prvyStlpec; stlpec < poslednyStlpec; stlpec++) {
                // Pozícia bloku v rámci celej mapy (svet).
                int worldX = stlpec * tileSize;
                int worldY = riadok * tileSize;

                // Prepočet svetových súradníc na obrazovkové (odčítame kameru).
                int screenX = worldX - kameraX;
                int screenY = worldY - kameraY;

                int index = this.mapData[riadok][stlpec];
                g2d.drawImage(
                        this.bloky[index].getObrazok(),
                        screenX, screenY,
                        tileSize, tileSize,
                        null
                );
            }
        }
    }

    /*
     * Kolízna kontrola obdĺžnika v svetových súradniciach.
     *
     * Princíp:
     *   - Z polohy a rozmerov obdĺžnika vypočítame, ktoré tily mapy zaberá
     *     (od ľavého-horného po pravý-spodný roh).
     *   - Ak ktorýkoľvek z týchto tilov má príznak kolízie (Blok.isKolizia()),
     *     vrátime true = pohyb tam nie je možný.
     *   - Mimo mapy taktiež vraciame true, aby entity nevypadli zo sveta.
     */
    /**
     *
     * Zistí, či obdĺžnik v svetových súradniciach koliduje s nepriechodným blokom mapy.
     * Mimo hraníc mapy sa vždy vracia {@code true} (entita by vypadla zo sveta).
     *
     * @param worldX  x-ová svetová súradnica ľavého horného rohu obdĺžnika
     * @param worldY  y-ová svetová súradnica ľavého horného rohu obdĺžnika
     * @param sirka   šírka obdĺžnika v pixeloch
     * @param vyska   výška obdĺžnika v pixeloch
     * @return {@code true} ak obdĺžnik zasahuje do kolízneho bloku alebo mimo mapy
     */
    public boolean koliduje(int worldX, int worldY, int sirka, int vyska) {
        int tileSize = this.gamePanel.getTileSize();

        int lavyStlpec   = worldX / tileSize;
        int pravyStlpec  = (worldX + sirka  - 1) / tileSize;
        int hornyRiadok  = worldY / tileSize;
        int spodnyRiadok = (worldY + vyska  - 1) / tileSize;

        if (lavyStlpec  < 0 || hornyRiadok  < 0 ||
            pravyStlpec >= this.mapData[0].length ||
            spodnyRiadok >= this.mapData.length) {
            return true;
        }

        for (int r = hornyRiadok; r <= spodnyRiadok; r++) {
            for (int s = lavyStlpec; s <= pravyStlpec; s++) {
                if (this.bloky[this.mapData[r][s]].isKolizia()) {
                    return true;
                }
            }
        }
        return false;
    }

}