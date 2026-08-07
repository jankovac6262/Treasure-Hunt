package mapa;

import java.awt.image.BufferedImage;

/**
 * Jeden dlaždičkový blok mapy.
 * Drží obrázok textúry a príznak kolízie, ktorý rozhoduje, či entita môže
 * na dané políčko vstúpiť.
 */
public class Blok {

    private BufferedImage obrazok;
    private boolean kolizia;

    /**
     * Vytvorí blok so zadanou textúrou a príznakom kolízie.
     *
     * @param obrazok  textúra bloku
     * @param kolizia  {@code true} ak blok nepriepustný (stena, voda)
     */
    public Blok(BufferedImage obrazok, boolean kolizia) {
        this.obrazok = obrazok;
        this.kolizia = kolizia;
    }

    /**
     * Vráti textúru bloku použitú pri vykresľovaní.
     *
     * @return textúra bloku
     */
    public BufferedImage getObrazok() {
        return this.obrazok;
    }

    /**
     * Indikuje, či blok tvorí fyzickú prekážku pre pohyb entít.
     *
     * @return {@code true} ak blok zabraňuje vstupu
     */
    public boolean isKolizia() {
        return this.kolizia;
    }
}