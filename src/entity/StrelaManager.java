package entity;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manažér aktívnych striel v hre.
 *
 * <p>Centralizuje životný cyklus projektilu: nepriatelia strelu pridajú
 * cez {@link #pridajStrelu(Strela)}, každý tik ju {@link #update()} posunie
 * a po zásahu alebo kolízii so stenou automaticky odstráni.
 * {@link #draw(Graphics2D)} vykreslí všetky živé strely.</p>
 */
public class StrelaManager {

    private final List<Strela> strely;

    /**
     * Vytvorí prázdny manažér striel.
     */
    public StrelaManager() {
        this.strely = new ArrayList<>();
    }

    /**
     * Zaregistruje novú strelu do herného sveta.
     *
     * @param strela  strela, ktorá má začať letieť
     */
    public void pridajStrelu(Strela strela) {
        this.strely.add(strela);
    }

    /**
     * Posunie všetky aktívne strely a odstráni tie, ktoré skončili
     * (zasiahli hráča alebo narazili na stenu).
     */
    public void update() {
        Iterator<Strela> it = this.strely.iterator();
        while (it.hasNext()) {
            Strela s = it.next();
            s.update();
            if (!s.jeAktivna()) {
                it.remove();
            }
        }
    }

    /**
     * Vykreslí všetky aktívne strely na obrazovku.
     *
     * @param g2d  grafický kontext Swing
     */
    public void draw(Graphics2D g2d) {
        for (Strela s : this.strely) {
            s.draw(g2d);
        }
    }
}