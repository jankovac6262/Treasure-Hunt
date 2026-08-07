package predmety;

import entity.Hrac;
import hlavnetriedy.GamePanel;

/**
 * Zberateľný štít. Po zozbieraní môže hráč klávesou T blokovať útoky
 * prichádzajúce zo smeru, kam práve pozerá.
 */
public class Stit extends PredmetBase {

    /**
     * Vytvorí štít na zadanej pozícii v mape.
     *
     * @param gamePanel  herný panel (prístup ku kamere a veľkosti dlaždíc)
     * @param mapaX      svetová súradnica X v pixeloch
     * @param mapaY      svetová súradnica Y v pixeloch
     */
    public Stit(GamePanel gamePanel, int mapaX, int mapaY) {
        super(gamePanel, mapaX, mapaY, "/predmety/shield_wood.png");
    }

    /**
     * Odovzdá štít hráčovi — od tejto chvíle môže blokovať.
     *
     * @param hrac  hráč, ktorý štít zozbiera
     */
    @Override
    public void aplikujUcinok(Hrac hrac) {
        hrac.zoberStit();
    }

    /**
     * Vráti identifikátor typu predmetu.
     *
     * @return {@code "stit"}
     */
    @Override
    public String getTyp() {
        return "stit";
    }
}