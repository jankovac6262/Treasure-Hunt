package predmety;

import entity.Hrac;
import hlavnetriedy.GamePanel;
import vylepsenia.ElixirDamage;
import vylepsenia.ElixirHeal;
import vylepsenia.ElixirSpeed;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manažér zberateľných predmetov — centralizuje ich životný cyklus.
 *
 * <p>Pri štarte načíta {@code /predmety/predmety.txt} (formát:
 * {@code <typ> <riadok> <stlpec>}) a cez factory metódu vytvorí konkrétne
 * podtriedy {@link Predmet}. Vďaka polymorfizmu manažér pracuje len
 * s rozhraním {@code Predmet} — konkrétny efekt si rieši každá trieda sama.</p>
 *
 * <p>Pamätá si počiatočný počet kľúčov, aby UI mohlo zobrazovať stav
 * {@code X/Y} a {@link Truhla} vedela, kedy sú splnené podmienky výhry.</p>
 */
public class PredmetManager {

    private final GamePanel gamePanel;
    private final List<Predmet> predmety;
    private int pociatocnyPocetKlucov;

    /**
     * Vytvorí manažér a načíta predmety z konfiguračného súboru.
     *
     * @param gamePanel  herný panel (prístup ku kamere a veľkosti dlaždíc)
     */
    public PredmetManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;

        this.predmety = new ArrayList<>();
        this.pociatocnyPocetKlucov = 0;
        this.nacitajPredmety();
    }

    private void nacitajPredmety() {
        InputStream is = getClass().getResourceAsStream("/predmety/predmety.txt");
        if (is == null) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] tokeny = line.split(" ");
                String typ = tokeny[0];
                int riadok = Integer.parseInt(tokeny[1]);
                int stlpec = Integer.parseInt(tokeny[2]);

                int mapaX = stlpec * this.gamePanel.getTileSize();
                int mapaY = riadok * this.gamePanel.getTileSize();

                Predmet p = this.vyrobiPredmet(typ, mapaX, mapaY);
                if (p != null) {
                    this.predmety.add(p);
                    if ("kluc".equals(p.getTyp())) {
                        this.pociatocnyPocetKlucov++;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Predmet vyrobiPredmet(String typ, int mapaX, int mapaY) {
        switch (typ) {
            case "mec":
                return new Mec(this.gamePanel, mapaX, mapaY);
            case "stit":
                return new Stit(this.gamePanel, mapaX, mapaY);
            case "kluc":
                return new Kluc(this.gamePanel, mapaX, mapaY);
            case "elixir_heal":
                return new ElixirHeal(this.gamePanel, mapaX, mapaY);
            case "elixir_damage":
                return new ElixirDamage(this.gamePanel, mapaX, mapaY);
            case "elixir_speed":
                return new ElixirSpeed(this.gamePanel, mapaX, mapaY);
            case "truhla":
                return new Truhla(this.gamePanel, mapaX, mapaY);
            default:
                System.err.println("Neznamy typ predmetu: " + typ);
                return null;
        }
    }

    /**
     * Testuje prekryv hráča s každým predmetom; pri zhode zavolá
     * {@code aplikujUcinok} a predmet odstráni zo zoznamu.
     */
    public void update() {
        Hrac hrac = this.gamePanel.getHrac();
        Iterator<Predmet> it = this.predmety.iterator();
        while (it.hasNext()) {
            Predmet p = it.next();
            if (p.prekryvaSaSHracom(hrac)) {
                p.aplikujUcinok(hrac);
                it.remove();
            }
        }
    }

    /**
     * Vykreslí všetky predmety, ktoré ešte ležia na mape.
     *
     * @param g2d  grafický kontext Swing
     */
    public void draw(Graphics2D g2d) {
        for (Predmet p : this.predmety) {
            p.draw(g2d);
        }
    }

    /**
     * Vráti celkový počet kľúčov, ktoré boli na mape pri štarte hry.
     * Používa ho HUD (zobrazenie {@code X/Y}) aj {@link Truhla} pri kontrole výhry.
     *
     * @return počiatočný počet kľúčov na mape
     */
    public int getPociatocnyPocetKlucov() {
        return this.pociatocnyPocetKlucov;
    }
}