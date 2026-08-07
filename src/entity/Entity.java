package entity;

import java.awt.Graphics2D;

/**
 * Spoločný predok všetkých entít v hre ({@code Hrac}, {@code Nepriatel}, ...).
 *
 * <p>Drží svetové súradnice ({@code mapaX}/{@code mapaY}) a pohybovú rýchlosť.
 * Obrazovkové súradnice sú špecifické pre konkrétny typ entity a nie sú tu.</p>
 */
public abstract class Entity {
    private int mapaX;
    private int mapaY;
    private int speed;

    /**
     * Vráti X-ovú svetovú súradnicu entity v pixeloch.
     *
     * @return svetová súradnica X
     */
    public int getMapaX() {
        return this.mapaX;
    }

    /**
     * Vráti Y-ovú svetovú súradnicu entity v pixeloch.
     *
     * @return svetová súradnica Y
     */
    public int getMapaY() {
        return this.mapaY;
    }

    /**
     * Vráti pohybovú rýchlosť entity v pixeloch za frame.
     *
     * @return rýchlosť pohybu
     */
    public int getSpeed() {
        return this.speed;
    }

    /**
     * Nastaví X-ovú svetovú súradnicu entity.
     *
     * @param mapaX  nová svetová súradnica X v pixeloch
     */
    public void setMapaX(int mapaX) {
        this.mapaX = mapaX;
    }

    /**
     * Nastaví Y-ovú svetovú súradnicu entity.
     *
     * @param mapaY  nová svetová súradnica Y v pixeloch
     */
    public void setMapaY(int mapaY) {
        this.mapaY = mapaY;
    }

    /**
     * Nastaví pohybovú rýchlosť entity.
     *
     * @param speed  rýchlosť v pixeloch za frame
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /**
     * Nastaví počiatočné svetové súradnice a ďalšie parametre špecifické
     * pre daný typ entity (volaná pri inicializácii).
     */
    public abstract void nacitajZaciatocneSuradnice();

    /**
     * Aktualizuje stav entity o jeden herný tik (pohyb, animácia, kolízie...).
     */
    public abstract void update();

    /**
     * Vykreslí entitu na obrazovku s kamerovým posunom.
     *
     * @param g2d  grafický kontext Swing
     */
    public abstract void draw(Graphics2D g2d);
}