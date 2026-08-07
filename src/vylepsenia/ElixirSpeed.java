package vylepsenia;

import entity.Hrac;
import hlavnetriedy.GamePanel;

/**
 * Elixír rýchlosti — po použití zvyšuje pohybovú rýchlosť hráča o 2 px/frame
 * po dobu 10 sekúnd. Bázová rýchlosť v {@code Entity} ostáva nedotknutá.
 */
public class ElixirSpeed extends Elixir {

    private static final int TRVANIE_FRAMES = 60 * 10;

    /**
     * Vytvorí elixír rýchlosti na zadanej pozícii v mape.
     *
     * @param gamePanel  herný panel (prístup ku kamere a veľkosti dlaždíc)
     * @param mapaX      svetová súradnica X v pixeloch
     * @param mapaY      svetová súradnica Y v pixeloch
     */
    public ElixirSpeed(GamePanel gamePanel, int mapaX, int mapaY) {
        super(gamePanel, mapaX, mapaY, "/predmety/elixir_speed.png", "Elixir rychlosti (10s)");
    }

    /**
     * Aktivuje boost rýchlosti na {@value TRVANIE_FRAMES} framov (10 sekúnd pri 60 FPS).
     *
     * @param hrac  hráč, ktorý elixír použil
     */
    @Override
    public void pouzi(Hrac hrac) {
        hrac.aktivujBoostSpeed(TRVANIE_FRAMES);
    }
}