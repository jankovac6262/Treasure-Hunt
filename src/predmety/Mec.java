package predmety;

import entity.Hrac;
import hlavnetriedy.GamePanel;

/**
 * Zberateľný meč. Po zozbieraní môže hráč klávesou Q útočiť na nepriateľov.
 */
public class Mec extends PredmetBase {

    /**
     * Vytvorí meč na zadanej pozícii v mape.
     *
     * @param gamePanel  herný panel (prístup ku kamere a veľkosti dlaždíc)
     * @param mapaX      svetová súradnica X v pixeloch
     * @param mapaY      svetová súradnica Y v pixeloch
     */
    public Mec(GamePanel gamePanel, int mapaX, int mapaY) {
        super(gamePanel, mapaX, mapaY, "/predmety/sword_normal.png");
    }

    /**
     * Odovzdá meč hráčovi — od tejto chvíle môže hráč útočiť.
     *
     * @param hrac  hráč, ktorý meč zozbiera
     */
    @Override
    public void aplikujUcinok(Hrac hrac) {
        hrac.zoberMec();
    }

    /**
     * Vráti identifikátor typu predmetu.
     *
     * @return {@code "mec"}
     */
    @Override
    public String getTyp() {
        return "mec";
    }
}