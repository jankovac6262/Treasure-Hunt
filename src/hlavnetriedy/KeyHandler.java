package hlavnetriedy;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Spracúva vstup z klávesnice — mapuje klávesy na herné akcie.
 *
 * <p>WASD ovládajú pohyb (spojité stavy), Q spúšťa útok mečom (edge-trigger),
 * T drží aktívny štít (spojitý stav). Útok sa číta cez {@link #consumeUtok()},
 * ktorý príznak zároveň resetuje — jedno stlačenie = jeden útok.</p>
 */
public class KeyHandler implements KeyListener {

    private boolean hore;
    private boolean dole;
    private boolean doprava;
    private boolean dolava;

    // Štít sa drží stlačený - je to spojitý stav, presne ako WASD.
    private boolean stit;

    /*
     * Útok mečom je jednorázová akcia (edge-trigger):
     *  - keyPressed nastaví príznak na true
     *  - Hrac.update() ho prečíta cez consumeUtok(), ktorý ho hneď vyresetuje
     * Vďaka tomu jedno stlačenie = jeden útok, nezáleží ako dlho klávesu držíš.
     */
    private boolean utokPending;

    /**
     * Nepoužívaná udalosť rozhrania {@link KeyListener}.
     *
     * @param e  udalosť klávesnice
     */
    @Override
    public void keyTyped(KeyEvent e) {

    }

    /**
     * Nastaví príslušný príznak pri stlačení klávesu.
     *
     * @param e  udalosť stlačenia klávesu
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_W) {
            this.hore = true;
        }
        if (keyCode == KeyEvent.VK_S) {
            this.dole = true;
        }
        if (keyCode == KeyEvent.VK_A) {
            this.dolava = true;
        }
        if (keyCode == KeyEvent.VK_D) {
            this.doprava = true;
        }
        if (keyCode == KeyEvent.VK_Q) {
            this.utokPending = true;
        }
        if (keyCode == KeyEvent.VK_T) {
            this.stit = true;
        }
    }

    /**
     * Resetuje príslušný príznak pri uvoľnení klávesu.
     *
     * @param e  udalosť uvoľnenia klávesu
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_W) {
            this.hore = false;

        }

        if (keyCode == KeyEvent.VK_S) {

            this.dole = false;

        }

        if (keyCode == KeyEvent.VK_A) {
            this.dolava = false;

        }
        if (keyCode == KeyEvent.VK_D) {
            this.doprava = false;

        }

        if (keyCode == KeyEvent.VK_T) {
            this.stit = false;
        }
    }

    /**
     * Indikuje, či je stlačená klávesa pohybu nahor (W).
     *
     * @return {@code true} ak je W stlačené
     */
    public boolean isHore() {
        return this.hore;
    }

    /**
     * Indikuje, či je stlačená klávesa pohybu nadol (S).
     *
     * @return {@code true} ak je S stlačené
     */
    public boolean isDole() {
        return this.dole;
    }

    /**
     * Indikuje, či je stlačená klávesa pohybu doprava (D).
     *
     * @return {@code true} ak je D stlačené
     */
    public boolean isDoprava() {
        return this.doprava;
    }

    /**
     * Indikuje, či je stlačená klávesa pohybu doľava (A).
     *
     * @return {@code true} ak je A stlačené
     */
    public boolean isDolava() {
        return this.dolava;
    }

    /**
     * Indikuje, či je stlačená klávesa štítu (T).
     *
     * @return {@code true} ak je T stlačené
     */
    public boolean isStit() {
        return this.stit;
    }

    /**
     * Prečíta a zároveň vyresetuje príznak útoku mečom.
     * Volanie v {@code Hrac.update()} zaručí, že útok sa spustí presne raz
     * na jedno stlačenie Q, bez ohľadu na to, ako dlho je klávesa držaná.
     *
     * @return {@code true} ak používateľ stlačil Q od posledného volania
     */
    public boolean consumeUtok() {
        boolean r = this.utokPending;
        this.utokPending = false;
        return r;
    }

    /**
     * Vynuluje všetky stlačené klávesy.
     * Volá sa pri otvorení modálneho dialógu (inventár), pretože {@code keyReleased}
     * neprichádza, kým je focus mimo herného panelu.
     */
    public void resetVsetky() {
        this.hore = false;
        this.dole = false;
        this.dolava = false;
        this.doprava = false;
        this.stit = false;
        this.utokPending = false;
    }
}