package vylepsenia;

import entity.Hrac;
import hlavnetriedy.GamePanel;
import predmety.PredmetBase;

import java.awt.image.BufferedImage;

/**
 * Abstraktná báza pre všetky elixíry.
 *
 * <p>Elixír implementuje obe rozhrania: {@code Predmet} (leží na mape a dá sa
 * zobrať) aj {@code Vylepsenie} (použije sa neskôr z inventára).
 * Zber {@link #aplikujUcinok(Hrac)} len vloží tento objekt do inventára;
 * skutočný efekt nastane až pri volaní {@link #pouzi(Hrac)} v konkrétnej podtriede.</p>
 *
 * <p>Ak je inventár plný, {@link #prekryvaSaSHracom(Hrac)} vráti {@code false}
 * a elixír zostane ležať, kým hráč nevytvorí miesto.</p>
 */
public abstract class Elixir extends PredmetBase implements Vylepsenie {

    private final String nazov;

    /**
     * Inicializuje elixír s textúrou a názvom zobrazeným v inventári.
     *
     * @param gamePanel      herný panel (prístup ku kamere a veľkosti dlaždíc)
     * @param mapaX          svetová X-súradnica v pixeloch
     * @param mapaY          svetová Y-súradnica v pixeloch
     * @param cestaObrazka   cesta k textúre v classpath
     * @param nazov          čitateľný názov zobrazený v zozname inventára
     */
    public Elixir(GamePanel gamePanel, int mapaX, int mapaY, String cestaObrazka, String nazov) {
        super(gamePanel, mapaX, mapaY, cestaObrazka);
        this.nazov = nazov;
    }

    /**
     * Vráti {@code false} ak je hráčov inventár plný — elixír zostáva na zemi.
     * Inak deleguje na {@code PredmetBase.prekryvaSaSHracom}.
     *
     * @param hrac  hráč, voči ktorému sa test vykonáva
     * @return {@code true} ak má byť elixír zozbieraný
     */
    @Override
    public boolean prekryvaSaSHracom(Hrac hrac) {
        if (hrac.jeInventarPlny()) {
            return false;
        }
        return super.prekryvaSaSHracom(hrac);
    }

    /**
     * Pridá tento elixír do hráčovho inventára (bez okamžitého efektu).
     *
     * @param hrac  hráč, ktorý elixír zozbiera
     */
    @Override
    public void aplikujUcinok(Hrac hrac) {
        hrac.pridajDoInventara(this);
    }

    /**
     * Vráti identifikátor typu predmetu.
     *
     * @return {@code "elixir"}
     */
    @Override
    public String getTyp() {
        return "elixir";
    }

    /**
     * Vráti čitateľný názov elixíru zobrazený v inventári.
     *
     * @return názov elixíru
     */
    @Override
    public String getNazov() {
        return this.nazov;
    }

    /**
     * Vráti obrázok elixíru použitý ako ikona v zozname inventára.
     *
     * @return textúra elixíru
     */
    @Override
    public BufferedImage getIkona() {
        return this.getObrazok();
    }
}