package predmety;

import entity.Hrac;
import hlavnetriedy.GamePanel;

/**
 * Zberateľný kľúč. Každý kľúč zvyšuje hráčovo počítadlo; po nazbieraní
 * všetkých kľúčov z mapy odomkne truhlu a umožní výhru.
 */
public class Kluc extends PredmetBase {

    /**
     * Vytvorí kľúč na zadanej pozícii v mape.
     *
     * @param gamePanel  herný panel (prístup ku kamere a veľkosti dlaždíc)
     * @param mapaX      svetová súradnica X v pixeloch
     * @param mapaY      svetová súradnica Y v pixeloch
     */
    public Kluc(GamePanel gamePanel, int mapaX, int mapaY) {
        super(gamePanel, mapaX, mapaY, "/predmety/key.png");
    }

    /**
     * Pridá jeden kľúč hráčovi.
     *
     * @param hrac  hráč, ktorý kľúč zozbiera
     */
    @Override
    public void aplikujUcinok(Hrac hrac) {
        hrac.pridajKluc();
    }

    /**
     * Vráti identifikátor typu predmetu.
     *
     * @return {@code "kluc"}
     */
    @Override
    public String getTyp() {
        return "kluc";
    }
}