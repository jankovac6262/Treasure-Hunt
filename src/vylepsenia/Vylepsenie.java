package vylepsenia;

import entity.Hrac;

import java.awt.image.BufferedImage;

/**
 * Kontrakt pre predmety uložené v hráčovom inventári.
 *
 * <p>Na rozdiel od {@code Predmet} (okamžitý zber z mapy) sa {@code Vylepsenie}
 * aplikuje až keď hráč v dialógu inventára klikne <em>Použi</em>.
 * {@code Elixir} implementuje obe rozhrania — leží na mape aj putuje do inventára.</p>
 *
 * <p>Polymorfizmus: {@code Inventar} drží {@code List<Vylepsenie>} a zavolá
 * {@link #pouzi(Hrac)} bez znalosti konkrétneho typu.</p>
 */
public interface Vylepsenie {

    /**
     * Aplikuje efekt vylepšenia na hráča.
     * Volá ho dialóg inventára po stlačení tlačidla <em>Použi</em>.
     *
     * @param hrac  hráč, na ktorého sa efekt aplikuje
     */
    void pouzi(Hrac hrac);

    /**
     * Vráti čitateľný názov vylepšenia zobrazený v zozname inventára.
     *
     * @return názov vylepšenia (napr. {@code "Elixir liecenia (+2 HP)"})
     */
    String getNazov();

    /**
     * Vráti ikonu vylepšenia zobrazenú v zozname inventára.
     *
     * @return obrázok ikony, alebo {@code null} ak ikona nie je dostupná
     */
    BufferedImage getIkona();
}