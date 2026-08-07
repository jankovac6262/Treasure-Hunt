package vylepsenia;

import entity.Hrac;
import hlavnetriedy.GamePanel;

/**
 * Liečivý elixír — po použití z inventára obnoví hráčovi 2 životy.
 * Hodnota je orezaná na maximum, takže pretečenie nie je možné.
 */
public class ElixirHeal extends Elixir {

    /**
     * Vytvorí liečivý elixír na zadanej pozícii v mape.
     *
     * @param gamePanel  herný panel (prístup ku kamere a veľkosti dlaždíc)
     * @param mapaX      svetová súradnica X v pixeloch
     * @param mapaY      svetová súradnica Y v pixeloch
     */
    public ElixirHeal(GamePanel gamePanel, int mapaX, int mapaY) {
        super(gamePanel, mapaX, mapaY, "/predmety/elixir_heal.png", "Elixir liecenia (+2 HP)");
    }

    /**
     * Obnoví hráčovi 2 životy (orezané na maximum).
     *
     * @param hrac  hráč, ktorý elixír použil
     */
    @Override
    public void pouzi(Hrac hrac) {
        hrac.pridajZivoty(2);
    }
}