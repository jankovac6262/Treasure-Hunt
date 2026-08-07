package entity;

import hlavnetriedy.GamePanel;

/**
 * Abstraktný predok všetkých nepriateľov.
 *
 * <p>Využíva vzor Template Method: {@link #update()} je {@code final} a volá
 * buď {@link #aktivnaReakcia(Hrac)} alebo {@link #pasivnaReakcia()} podľa
 * vzdialenosti hráča od detekčného rádiusu. Každá podtrieda implementuje
 * tieto dve metódy vlastným spôsobom.</p>
 *
 * <p>Pridanie nového nepriateľa = nová podtrieda + riadok v konfiguračnom
 * súbore, bez zásahu do hernej slučky.</p>
 */
public abstract class Nepriatel extends Entity {

    private final GamePanel gamePanel;
    private final Hrac hrac;
    private int detekcnyRadius;

    // Životy spoločné pre všetkých nepriateľov - umožňuje útok mečom
    // jednotne uberať životy bez znalosti konkrétneho typu (polymorfizmus).
    // Predvolene 2 zásahy.
    private int zivoty;

    /**
     * Inicializuje spoločné atribúty nepriateľa.
     *
     * @param gamePanel  herný panel (prístup k manažérom a veľkosti dlaždíc)
     * @param hrac       hráč (cieľ útoku a referencia pre výpočet vzdialenosti)
     * @param mapaX      počiatočná svetová X-súradnica v pixeloch
     * @param mapaY      počiatočná svetová Y-súradnica v pixeloch
     */
    public Nepriatel(GamePanel gamePanel, Hrac hrac, int mapaX, int mapaY) {
        this.gamePanel = gamePanel;
        this.hrac = hrac;
        super.setMapaX(mapaX);
        super.setMapaY(mapaY);
        this.zivoty = 2;
    }

    /**
     * Vráti herný panel. Používajú ho podtriedy na prístup k manažérom.
     *
     * @return herný panel
     */
    protected GamePanel getGamePanel() {
        return this.gamePanel;
    }

    /**
     * Nastaví detekčný rádius, v ktorom nepriateľ vníma hráča.
     * Podtriedy ho volajú v konštruktore.
     *
     * @param detekcnyRadius  vzdialenosť v pixeloch, pri ktorej sa aktivuje {@code aktivnaReakcia}
     */
    protected void setDetekcnyRadius(int detekcnyRadius) {
        this.detekcnyRadius = detekcnyRadius;
    }

    /**
     * Uberie nepriateľovi zadaný počet životov.
     *
     * @param hodnota  počet životov na odobranie
     */
    public void uberZivot(int hodnota) {
        this.zivoty -= hodnota;
    }

    /**
     * Indikuje, či nepriateľ nemá žiadne životy (bol porazený mečom).
     *
     * @return {@code true} ak životy klesli na nulu alebo pod nulu
     */
    public boolean jeMrtvy() {
        return this.zivoty <= 0;
    }

    /**
     * Testuje, či sa zadaný obdĺžnik (útočný hitbox) prekrýva s nepriateľom.
     * Veľkosť hitboxu nepriateľa zodpovedá jednej dlaždici.
     *
     * @param worldX  ľavý okraj hitboxu v svetových súradniciach
     * @param worldY  horný okraj hitboxu v svetových súradniciach
     * @param sirka   šírka hitboxu v pixeloch
     * @param vyska   výška hitboxu v pixeloch
     * @return {@code true} ak nastáva prekryv
     */
    public boolean prekryvaSa(int worldX, int worldY, int sirka, int vyska) {
        int tileSize = this.gamePanel.getTileSize();
        int nx = super.getMapaX();
        int ny = super.getMapaY();
        return nx < worldX + sirka  && nx + tileSize > worldX
            && ny < worldY + vyska  && ny + tileSize > worldY;
    }

    /**
     * Vypočíta euklidovskú vzdialenosť k hráčovi v pixeloch.
     * Používa sa pri rozhodovaní, či prepnúť do aktívneho režimu.
     *
     * @return vzdialenosť k hráčovi v pixeloch
     */
    protected double vzdialenostOdHraca() {
        int dx = this.hrac.getMapaX() - super.getMapaX();
        int dy = this.hrac.getMapaY() - super.getMapaY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Vráti X-ovú obrazovkovú súradnicu nepriateľa (po odčítaní kamery).
     *
     * @return obrazovková súradnica X
     */
    protected int getScreenX() {
        int kameraX = this.hrac.getMapaX() - this.hrac.getOknoX();
        return super.getMapaX() - kameraX;
    }

    /**
     * Vráti Y-ovú obrazovkovú súradnicu nepriateľa (po odčítaní kamery).
     *
     * @return obrazovková súradnica Y
     */
    protected int getScreenY() {
        int kameraY = this.hrac.getMapaY() - this.hrac.getOknoY();
        return super.getMapaY() - kameraY;
    }

    /**
     * Herná slučka nepriateľa — Template Method.
     * Volá {@link #aktivnaReakcia(Hrac)} keď je hráč v dosahu,
     * inak {@link #pasivnaReakcia()}. Metóda je {@code final}; podtriedy
     * nesmú meniť toto rozhodovanie.
     */
    @Override
    public final void update() {
        if (this.vzdialenostOdHraca() < this.detekcnyRadius) {
            this.aktivnaReakcia(this.hrac);
        } else {
            this.pasivnaReakcia();
        }
    }

    /**
     * Prázdna implementácia — nepriatelia dostávajú súradnice v konštruktore,
     * táto metóda existuje len kvôli kontraktu {@link Entity}.
     */
    @Override
    public void nacitajZaciatocneSuradnice() {
    }

    /**
     * Správanie nepriateľa keď je hráč v detekčnom rádiuse.
     * Každá podtrieda definuje vlastnú agresívnu reakciu.
     *
     * @param hrac  hráč (cieľ útoku / pohybu)
     */
    protected abstract void aktivnaReakcia(Hrac hrac);

    /**
     * Správanie nepriateľa keď je hráč mimo detekčného rádiusu.
     * Každá podtrieda definuje vlastné pasívne správanie (stoj, hliadka...).
     */
    protected abstract void pasivnaReakcia();
}