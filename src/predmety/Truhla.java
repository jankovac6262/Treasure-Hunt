package predmety;

import entity.Hrac;
import hlavnetriedy.GamePanel;

/**
 * Truhla — cieľový predmet hry.
 *
 * <p>Zber truhly spustí výhernú obrazovku, ale len ak má hráč všetky kľúče
 * z mapy. Ak ich nemá, {@link #prekryvaSaSHracom(Hrac)} vráti {@code false}
 * a hráč cez truhlu môže voľne prechádzať.</p>
 */
public class Truhla extends PredmetBase {

    /**
     * Vytvorí truhlu na zadanej pozícii v mape.
     *
     * @param gamePanel  herný panel (prístup k manažérom a veľkosti dlaždíc)
     * @param mapaX      svetová súradnica X v pixeloch
     * @param mapaY      svetová súradnica Y v pixeloch
     */
    public Truhla(GamePanel gamePanel, int mapaX, int mapaY) {
        super(gamePanel, mapaX, mapaY, "/predmety/chest.png");
    }

    /**
     * Vráti {@code true} len vtedy, ak hráč zozbiera všetky kľúče z mapy
     * a fyzicky stojí na truhle.
     *
     * @param hrac  hráč, voči ktorému sa testuje prekryv
     * @return {@code true} ak sú splnené podmienky výhry a hráč sa prekrýva s truhlou
     */
    @Override
    public boolean prekryvaSaSHracom(Hrac hrac) {
        int treba = this.getGamePanel().getPredmetManager().getPociatocnyPocetKlucov();
        if (hrac.getPocetKlucov() < treba) {
            return false;
        }
        return super.prekryvaSaSHracom(hrac);
    }

    /**
     * Signalizuje hernému panelu výhru hráča.
     *
     * @param hrac  hráč, ktorý truhlu otvoril (nevyužité, výhra je globálny stav)
     */
    @Override
    public void aplikujUcinok(Hrac hrac) {
        this.getGamePanel().nastavVyhru();
    }

    /**
     * Vráti identifikátor typu predmetu.
     *
     * @return {@code "truhla"}
     */
    @Override
    public String getTyp() {
        return "truhla";
    }
}