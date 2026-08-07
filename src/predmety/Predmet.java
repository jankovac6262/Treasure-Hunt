package predmety;

import entity.Hrac;

import java.awt.Graphics2D;

/**
 * Kontrakt pre všetky zberateľné predmety v hre.
 *
 * <p>{@code PredmetManager} iteruje {@code List<Predmet>}: pre každý predmet
 * otestuje prekryv s hráčom a pri zhode zavolá {@link #aplikujUcinok(Hrac)}.
 * Vďaka polymorfizmu manažér nepozná konkrétne typy — meč, štít, kľúč aj elixír
 * sa spravujú rovnakým kódom.</p>
 *
 * <p>Nový typ predmetu = nová trieda implementujúca toto rozhranie
 * + jeden {@code case} vo factory metóde {@code PredmetManager}.</p>
 */
public interface Predmet {

    /**
     * Testuje, či sa predmet prekrýva s hráčovým hitboxom.
     * Truhla a elixíry môžu pridať pred-podmienky (kľúče, plný inventár).
     *
     * @param hrac  hráč, voči ktorému sa test vykonáva
     * @return {@code true} ak predmet má byť zozbieraný v tomto tiku
     */
    boolean prekryvaSaSHracom(Hrac hrac);

    /**
     * Vykoná efekt predmetu na hráča (zoberie meč, štít, pridá kľúč, uloží elixír...).
     * Volá ho {@code PredmetManager} ihneď po pozitívnom výsledku {@link #prekryvaSaSHracom(Hrac)}.
     *
     * @param hrac  hráč, na ktorého sa efekt aplikuje
     */
    void aplikujUcinok(Hrac hrac);

    /**
     * Vykreslí predmet na obrazovku s kamerovým posunom.
     *
     * @param g2d  grafický kontext Swing
     */
    void draw(Graphics2D g2d);

    /**
     * Vráti krátky identifikátor typu predmetu (napr. {@code "kluc"}, {@code "mec"}).
     * Manažér ho používa na počítanie nazbieraných kľúčov.
     *
     * @return reťazec identifikujúci typ predmetu
     */
    String getTyp();
}