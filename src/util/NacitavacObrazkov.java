package util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Pomocná trieda pre načítavanie obrázkov z classpath zdrojov.
 * Centralizuje spracovanie chýb — ak zdroj neexistuje, vypíše stack trace a vráti {@code null}.
 */
public class NacitavacObrazkov {

    /**
     * Načíta obrázok z classpath podľa zadanej cesty.
     *
     * @param cesta  cesta k obrazovému súboru v classpath (napr. {@code "/hrac/player_down1.png"})
     * @return načítaný {@link BufferedImage}, alebo {@code null} ak súbor neexistuje alebo sa nepodarilo načítať
     */
    public static BufferedImage nacitaj(String cesta) {
        try {
            InputStream is = NacitavacObrazkov.class.getResourceAsStream(cesta);
            if (is == null) {
                throw new IOException("Nepodarilo sa najst zdroj: " + cesta);
            }
            return ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}