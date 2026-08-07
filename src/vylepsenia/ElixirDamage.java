package vylepsenia;

import entity.Hrac;
import hlavnetriedy.GamePanel;

/**
 * Elixír sily — po použití zvyšuje poškodenie hráča o 1 po dobu 10 sekúnd.
 * Opätovné použitie pred vypršaním predĺži trvanie (nie skráti).
 */
public class ElixirDamage extends Elixir {

    private static final int TRVANIE_FRAMES = 60 * 10;

    /**
     * Vytvorí elixír sily na zadanej pozícii v mape.
     *
     * @param gamePanel  herný panel (prístup ku kamere a veľkosti dlaždíc)
     * @param mapaX      svetová súradnica X v pixeloch
     * @param mapaY      svetová súradnica Y v pixeloch
     */
    public ElixirDamage(GamePanel gamePanel, int mapaX, int mapaY) {
        super(gamePanel, mapaX, mapaY, "/predmety/elixir_damage.png", "Elixir sily (10s)");
    }

    /**
     * Aktivuje boost poškodenia na {@value TRVANIE_FRAMES} framov (10 sekúnd pri 60 FPS).
     *
     * @param hrac  hráč, ktorý elixír použil
     */
    @Override
    public void pouzi(Hrac hrac) {
        hrac.aktivujBoostDamage(TRVANIE_FRAMES);
    }
}