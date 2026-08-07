package entity;

import hlavnetriedy.GamePanel;
import hlavnetriedy.KeyHandler;
import mapa.BlokManager;
import vylepsenia.Vylepsenie;

import util.NacitavacObrazkov;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Hráčova postava — vstup z klávesnice prekladá na pohyb, útok a blokovanie.
 *
 * <p>Hráč je vždy vykreslený v strede obrazovky; kamera sleduje jeho svetové
 * súradnice. Drží polymorfný inventár {@code List<Vylepsenie>} (max 3 položky),
 * meč, štít, počítadlo kľúčov a dočasné boosty z elixírov.</p>
 */
public class Hrac extends Entity {

    private GamePanel gamePanel;
    private KeyHandler keyHandler;
    private int spriteCounter;
    private int spriteNum;
    private String directions;
    private BufferedImage up1;
    private BufferedImage up2;
    private BufferedImage down1;
    private BufferedImage down2;
    private BufferedImage left1;
    private BufferedImage left2;
    private BufferedImage right1;
    private BufferedImage right2;
    // Báza pre meč a štít - oba obrázky nakreslené v orientácii "doprava".
    // Pre ostatné smery sa otáčajú za behu (viď kresliUtok / kresliStit).
    private BufferedImage swordRight;
    private BufferedImage shieldRight;
    private final int oknoX;
    private final int oknoY;

    // Životy hráča + cooldown nezraniteľnosti.
    // Nezraniteľnosť zabraňuje, aby zombie počas jediného dotyku ubral
    // život v každom z 60 framov za sekundu (bez nej by hráč zomrel okamžite).
    private int zivoty;
    private final int maxZivotov;
    private int invulnFrames;
    private static final int INVULN_MAX = 60;

    // Padding hitboxu - kolízny štvorec je o 2*padding menší než sprite.
    // Vďaka tomu prejde hráč úžinou širokou presne ako jedno políčko,
    // namiesto toho, aby sa musel trafiť na pixel presne.
    private final int hitboxPadding = 10;

    // Inventár - zber predmetov nastavuje tieto príznaky / počítadlo.
    private boolean mamMec;
    private boolean mamStit;
    private int pocetKlucov;

    // Útok mečom - cooldown bráni rýchlemu spamu, utokFrames riadi vizualizáciu.
    private static final int UTOK_COOLDOWN_MAX = 25;
    private static final int UTOK_VIZUAL_FRAMES = 10;
    private int utokCooldown;
    private int utokFrames;

    // Štít - aktívny kým hráč drží T. Počas blokovania sa hráč nehýbe.
    private boolean blokuje;

    // Inventár vylepšení (elixírov). Polymorfný List<Vylepsenie> - Hrac
    // nevie aký konkrétny elixír drží, len že má metódu pouzi(). Maximálna
    // veľkosť 3 zabraňuje hromadeniu - hráč si musí premyslieť, kedy ho použiť.
    private static final int MAX_VYLEPSENI = 3;
    private final List<Vylepsenie> inventar;

    // Aktívne boosty - počítadlá zostávajúcich frames. Kým > 0, efekt platí.
    // Damage/speed sa nepripočítavajú do bázových hodnôt, ale do getDamage()
    // a getEffectiveSpeed(). Po vypršaní sa správanie automaticky vráti späť.
    private static final int BONUS_DAMAGE = 1;
    private static final int BONUS_SPEED = 2;
    private int boostDamageFrames;
    private int boostSpeedFrames;

    /**
     * Inicializuje hráča: načíta obrazy, nastaví životy, inventár a vypočíta
     * pevnú obrazovkovú pozíciu (stred okna).
     *
     * @param gamePanel   herný panel (prístup k manažérom a rozmerom obrazovky)
     * @param keyHandler  obsluha klávesnice (čítanie vstupov v {@link #update()})
     */
    public Hrac(GamePanel gamePanel, KeyHandler keyHandler) {
        this.gamePanel = gamePanel;
        this.keyHandler = keyHandler;

        this.oknoX = this.gamePanel.getScreenWidth() / 2 - (this.gamePanel.getTileSize() / 2);
        this.oknoY = this.gamePanel.getScreenHeight() / 2 - (this.gamePanel.getTileSize() / 2);

        this.spriteNum = 1;
        this.spriteCounter = 0;
        this.maxZivotov = 5;
        this.zivoty = this.maxZivotov;
        this.invulnFrames = 0;
        this.mamMec = false;
        this.mamStit = false;
        this.pocetKlucov = 0;
        this.utokCooldown = 0;
        this.utokFrames = 0;
        this.blokuje = false;
        this.inventar = new ArrayList<>();
        this.boostDamageFrames = 0;
        this.boostSpeedFrames = 0;
        this.nacitajObrazky();
    }

    private void nacitajObrazky() {
        this.up1    = NacitavacObrazkov.nacitaj("/hrac/player_up1.png");
        this.up2    = NacitavacObrazkov.nacitaj("/hrac/player_up2.png");
        this.down1  = NacitavacObrazkov.nacitaj("/hrac/player_down1.png");
        this.down2  = NacitavacObrazkov.nacitaj("/hrac/player_down2.png");
        this.left1  = NacitavacObrazkov.nacitaj("/hrac/player_left1.png");
        this.left2  = NacitavacObrazkov.nacitaj("/hrac/player_left2.png");
        this.right1 = NacitavacObrazkov.nacitaj("/hrac/player_right1.png");
        this.right2 = NacitavacObrazkov.nacitaj("/hrac/player_right2.png");
        this.swordRight  = NacitavacObrazkov.nacitaj("/hrac/sword_right.png");
        this.shieldRight = NacitavacObrazkov.nacitaj("/hrac/shield_right.png");
    }

    /**
     * Nastaví počiatočnú polohu hráča na mape, bázovú rýchlosť a smer pohľadu.
     */
    @Override
    public void nacitajZaciatocneSuradnice() {
        super.setMapaY(this.gamePanel.getTileSize() * 35);
        super.setMapaX(this.gamePanel.getTileSize() * 35);
        super.setSpeed(4);
        this.directions = "down";
    }

    /*
     * Pred každým pohybom kontrolujeme kolíziu s mapou. Pohyb sa
     * vykoná len ak nová pozícia neprekrýva nepriechodný blok.
     * X a Y testujeme oddelene, aby sa hráč mohol "klznúť" pozdĺž steny.
     *
     * Pre kolíziu používame zmenšený hitbox (offsetnutý o hitboxPadding),
     * nie celý sprite - inak by úzke prechody boli neprejazdné.
     */
    /**
     * Spracuje vstup z klávesnice, pohne hráčom (s kolíznym testom), vykoná útok
     * mečom a odpočíta časovače nezraniteľnosti a boostov.
     */
    @Override
    public void update() {
        // Štít je aktívny iba ak hráč drží T a má štít zo zberu.
        // Počas blokovania sa hráč nemôže pohnúť ani útočiť.
        this.blokuje = this.mamStit && this.keyHandler.isStit();

        boolean pohyb = false;
        int speed = this.getEffectiveSpeed();
        BlokManager bm = this.gamePanel.getBlokManager();

        int novyX = super.getMapaX();
        int novyY = super.getMapaY();

        if (!this.blokuje) {
            if (this.keyHandler.isHore()) {
                novyY -= speed;
                this.directions = "up";
                pohyb = true;
            } else if (this.keyHandler.isDole()) {
                novyY += speed;
                this.directions = "down";
                pohyb = true;
            } else if (this.keyHandler.isDolava()) {
                novyX -= speed;
                this.directions = "left";
                pohyb = true;
            } else if (this.keyHandler.isDoprava()) {
                novyX += speed;
                this.directions = "right";
                pohyb = true;
            }
        }

        int hbSize = this.getHitboxSize();
        int hbPad  = this.hitboxPadding;

        // Test X-osi samostatne, potom Y-osi - umožňuje plynulý pohyb pozdĺž stien.
        if (!bm.koliduje(novyX + hbPad, super.getMapaY() + hbPad, hbSize, hbSize)) {
            super.setMapaX(novyX);
        }
        if (!bm.koliduje(super.getMapaX() + hbPad, novyY + hbPad, hbSize, hbSize)) {
            super.setMapaY(novyY);
        }

        // Útok mečom (jednorázový edge-trigger z KeyHandler). Počas blokovania
        // útok ignorujeme - jedna z dvoch akcií naraz.
        boolean utokRequest = this.keyHandler.consumeUtok();
        if (utokRequest && !this.blokuje && this.mamMec && this.utokCooldown == 0) {
            this.zautoc();
            this.utokCooldown = UTOK_COOLDOWN_MAX;
            this.utokFrames = UTOK_VIZUAL_FRAMES;
        }

        if (this.utokCooldown > 0) {
            this.utokCooldown--;
        }
        if (this.utokFrames > 0) {
            this.utokFrames--;
        }

        if (pohyb) {
            this.spriteCounter++;
            if (this.spriteCounter > 10) {
                if (this.spriteNum == 1) {
                    this.spriteNum = 2;
                } else {
                    this.spriteNum = 1;
                }
                this.spriteCounter = 0;
            }
        } else {
            this.spriteNum = 1;
            this.spriteCounter = 0;
        }

        if (this.invulnFrames > 0) {
            this.invulnFrames--;
        }

        if (this.boostDamageFrames > 0) {
            this.boostDamageFrames--;
        }
        if (this.boostSpeedFrames > 0) {
            this.boostSpeedFrames--;
        }
    }

    /*
     * Vyrobí útočný hitbox o veľkosti jedného tilu v smere, kam hráč pozerá,
     * a pošle ho NepriatelManager-u na vyhodnotenie zásahu. Manažér iteruje
     * cez Nepriatel (polymorfný typ) - útok funguje na akéhokoľvek nepriateľa.
     */
    private void zautoc() {
        int tileSize = this.gamePanel.getTileSize();
        int hx = super.getMapaX();
        int hy = super.getMapaY();

        switch (this.directions) {
            case "up":
                hy -= tileSize;
                break;
            case "down":
                hy += tileSize;
                break;
            case "left":
                hx -= tileSize;
                break;
            case "right":
                hx += tileSize;
                break;
        }

        this.gamePanel.getNepriatelManager().zasahniHitbox(hx, hy, tileSize, tileSize, this.getDamage());
    }

    /**
     * Vykreslí hráča: vyberie správny animačný frame podľa smeru a pohybu,
     * prípadne nakreslí vizuál švihu mečom alebo aktívneho štítu.
     * Počas nezraniteľnosti hráč bliká (každý druhý päťframový blok sa preskočí).
     *
     * @param g2d  grafický kontext na kreslenie
     */
    @Override
    public void draw(Graphics2D g2d) {
        // Počas nezraniteľnosti hráča "blikáme" - vynecháme každý druhý frame.
        // Vizuálna spätná väzba pre hráča, že práve dostal zásah.
        if (this.invulnFrames > 0 && (this.invulnFrames / 5) % 2 == 0) {
            return;
        }

        BufferedImage obrazok = null;

        switch (this.directions) {
            case "up":
                obrazok = (this.spriteNum == 1) ? this.up1 : this.up2;
                break;
            case "down":
                obrazok = (this.spriteNum == 1) ? this.down1 : this.down2;
                break;
            case "left":
                obrazok = (this.spriteNum == 1) ? this.left1 : this.left2;
                break;
            case "right":
                obrazok = (this.spriteNum == 1) ? this.right1 : this.right2;
                break;
        }

        g2d.drawImage(obrazok, this.oknoX, this.oknoY,
                this.gamePanel.getTileSize(), this.gamePanel.getTileSize(), null);

        // Vizualizácia švihu mečom - krátkodobá biela čiara/obdĺžnik v smere útoku.
        // Trvá utokFrames, takže hráč vidí, kedy a kam zasiahol.
        if (this.utokFrames > 0) {
            this.kresliUtok(g2d);
        }

        // Štít - modrý rámček okolo hráča, kým drží T.
        if (this.blokuje) {
            this.kresliStit(g2d);
        }
    }

    private void kresliUtok(Graphics2D g2d) {
        // Meč nakreslíme v susednom tile v smere pohľadu - vizuál sedí
        // s útočným hitboxom, ktorý zautoc() posiela do NepriatelManager.
        int tileSize = this.gamePanel.getTileSize();
        int sx = this.oknoX;
        int sy = this.oknoY;
        switch (this.directions) {
            case "up":
                sy -= tileSize;
                break;
            case "down":
                sy += tileSize;
                break;
            case "left":
                sx -= tileSize;
                break;
            case "right":
                sx += tileSize;
                break;
        }
        this.kresliOtoceny(g2d, this.swordRight, sx, sy, tileSize);
    }

    private void kresliStit(Graphics2D g2d) {
        // Štít sa drží pred hráčom (v susednom tile v smere pohľadu) -
        // sedí s logikou prijmiZasah, ktorý odraží len útoky zo smeru pohľadu.
        int tileSize = this.gamePanel.getTileSize();
        int sx = this.oknoX;
        int sy = this.oknoY;
        switch (this.directions) {
            case "up":
                sy -= tileSize;
                break;
            case "down":
                sy += tileSize;
                break;
            case "left":
                sx -= tileSize;
                break;
            case "right":
                sx += tileSize;
                break;
        }
        this.kresliOtoceny(g2d, this.shieldRight, sx, sy, tileSize);
    }

    /*
     * Nakreslí daný obrázok na cieľový štvorec (sx,sy) o veľkosti tileSize,
     * pootočený podľa aktuálneho smeru hráča.
     *
     * Princíp:
     *   1. Zdrojový obrázok je nakreslený v orientácii "doprava" = 0 rad.
     *   2. Pre ostatné smery vypočítame uhol rotácie:
     *        right =  0
     *        down  =  +π/2  (90° v smere hodinových ručičiek)
     *        left  =  +π    (180° - hore nohami z pohľadu "right")
     *        up    =  -π/2  (90° proti smeru hodinových ručičiek)
     *      Pozn.: v Swingu je os Y obrátená (smerom dole), preto kladný
     *      uhol = rotácia v smere hodinových ručičiek.
     *
     *   3. g2d.rotate(uhol, cx, cy) pridá rotáciu okolo bodu (cx, cy) -
     *      stredu cieľového tilu. Bez explicitného stredu by sa otáčalo
     *      okolo (0, 0) a obrázok by odletel mimo plochy.
     *
     *   4. Pred rotáciou si uložíme aktuálny AffineTransform (g2d.getTransform)
     *      a po nakreslení ho obnovíme cez setTransform. Bez tohto by každý
     *      ďalší kresliaci úkon  bol stále otočený.
     *
     * 90° násobky sú v pixel-arte čisté - každý pixel zdroja sa premapuje
     * na presne jeden pixel cieľa, žiadne rozmazanie / interpolácia.
     */
    private void kresliOtoceny(Graphics2D g2d, BufferedImage obrazok, int sx, int sy, int tileSize) {
        if (obrazok == null) {
            return;
        }

        double uhol;
        switch (this.directions) {
            case "up":
                uhol = -Math.PI / 2;
                break;
            case "down":
                uhol = Math.PI / 2;
                break;
            case "left":
                uhol = Math.PI;
                break;
            case "right":
            default:
                uhol = 0;
                break;
        }

        AffineTransform povodny = g2d.getTransform();
        if (uhol != 0) {
            g2d.rotate(uhol, sx + tileSize / 2.0, sy + tileSize / 2.0);
        }
        g2d.drawImage(obrazok, sx, sy, tileSize, tileSize, null);
        g2d.setTransform(povodny);
    }

    /**
     * Zistí, či zadaný obdĺžnik vo svetových súradniciach prekrýva hráčov hitbox.
     * Hitbox je zmenšený o {@code hitboxPadding} zo všetkých strán.
     *
     * @param worldX  x-ová súradnica testovaného obdĺžnika (svetové súradnice)
     * @param worldY  y-ová súradnica testovaného obdĺžnika (svetové súradnice)
     * @param sirka   šírka testovaného obdĺžnika v pixeloch
     * @param vyska   výška testovaného obdĺžnika v pixeloch
     * @return {@code true} ak sa obdĺžniky prekrývajú
     */
    public boolean prekryvaSa(int worldX, int worldY, int sirka, int vyska) {
        int hbSize = this.getHitboxSize();
        int hx = super.getMapaX() + this.hitboxPadding;
        int hy = super.getMapaY() + this.hitboxPadding;
        return hx < worldX + sirka  && hx + hbSize > worldX
            && hy < worldY + vyska  && hy + hbSize > worldY;
    }

    private int getHitboxSize() {
        return this.gamePanel.getTileSize() - 2 * this.hitboxPadding;
    }

    /**
     * Uberie hráčovi zadaný počet životov. Zásah sa ignoruje, ak je hráč
     * v stave nezraniteľnosti. Po úspešnom zásahu sa spustí cooldown
     * nezraniteľnosti ({@value INVULN_MAX} framov).
     *
     * @param hodnota  počet životov, ktorý sa má ubrať
     */
    public void uberZivot(int hodnota) {
        if (this.invulnFrames > 0 || this.zivoty <= 0) {
            return;
        }
        this.zivoty -= hodnota;
        if (this.zivoty < 0) {
            this.zivoty = 0;
        }
        this.invulnFrames = INVULN_MAX;
    }

    /**
     * Prijme zásah od externého zdroja (zombie dotyk, strela).
     * Ak hráč blokuje štítom a útok prichádza zo smeru pohľadu, zásah sa odrazí.
     * V opačnom prípade sa zavolá {@link #uberZivot(int)}.
     *
     * @param hodnota  počet životov, o ktoré útok poškodzuje
     * @param zdrojX   x-ová svetová súradnica útočníka (na určenie smeru)
     * @param zdrojY   y-ová svetová súradnica útočníka (na určenie smeru)
     * @return {@code false} ak bol útok odrazený štítom, inak {@code true}
     */
    public boolean prijmiZasah(int hodnota, int zdrojX, int zdrojY) {
        if (this.blokuje && this.zdrojJeVoSmerePohladu(zdrojX, zdrojY)) {
            return false;
        }
        this.uberZivot(hodnota);
        return true;
    }

    /*
     * Test, či útočiaci objekt prichádza spredu (zo smeru, kam pozerá).
     * Stred hráča je orientačný bod, voči ktorému porovnávame zdroj.
     */
    private boolean zdrojJeVoSmerePohladu(int zdrojX, int zdrojY) {
        int tileSize = this.gamePanel.getTileSize();
        int stredX = super.getMapaX() + tileSize / 2;
        int stredY = super.getMapaY() + tileSize / 2;

        switch (this.directions) {
            case "up":    return zdrojY < stredY;
            case "down":  return zdrojY > stredY;
            case "left":  return zdrojX < stredX;
            case "right": return zdrojX > stredX;
            default:      return false;
        }
    }

    /**
     * Označí hráča ako vlastníka meča (zbraň zo zberu predmetu {@code Mec}).
     */
    public void zoberMec() {
        this.mamMec = true;
    }

    /**
     * Označí hráča ako vlastníka štítu (predmet zo zberu {@code Stit}).
     */
    public void zoberStit() {
        this.mamStit = true;
    }

    /**
     * Pripočíta jeden kľúč k hráčovmu počítadlu kľúčov.
     */
    public void pridajKluc() {
        this.pocetKlucov++;
    }

    /**
     * Pridá hráčovi životy (heal). Oreže na {@code maxZivotov}, aby pretečenie
     * neumožnilo skladovať extra HP. Mŕtveho hráča nelieči.
     *
     * @param hodnota  počet životov, ktorý sa má pridať
     */
    public void pridajZivoty(int hodnota) {
        if (this.zivoty <= 0) {
            return;
        }
        this.zivoty = Math.min(this.maxZivotov, this.zivoty + hodnota);
    }

    /**
     * Pridá vylepšenie do inventára hráča.
     * Ak je inventár plný, predmet sa nepridá a metóda vráti {@code false}.
     *
     * @param v  vylepšenie na pridanie
     * @return {@code true} ak bolo vylepšenie úspešne pridané, inak {@code false}
     */
    public boolean pridajDoInventara(Vylepsenie v) {
        if (this.jeInventarPlny()) {
            return false;
        }
        this.inventar.add(v);
        return true;
    }

    /**
     * Odoberie a vráti vylepšenie na danom indexe z inventára.
     * Ak je index mimo rozsahu, vráti {@code null}.
     *
     * @param index  pozícia vylepšenia v inventári (0-based)
     * @return odobraté vylepšenie, alebo {@code null} pri neplatnom indexe
     */
    public Vylepsenie vyhodZInventara(int index) {
        if (index < 0 || index >= this.inventar.size()) {
            return null;
        }
        return this.inventar.remove(index);
    }

    /**
     * Vráti nemodifikovateľný pohľad na inventár hráča.
     *
     * @return read-only zoznam vylepšení
     */
    public List<Vylepsenie> getInventar() {
        return Collections.unmodifiableList(this.inventar);
    }

    /**
     * Zistí, či je inventár plný (dosiahol maximálny počet vylepšení).
     *
     * @return {@code true} ak inventár nedokáže prijať ďalšie vylepšenie
     */
    public boolean jeInventarPlny() {
        return this.inventar.size() >= MAX_VYLEPSENI;
    }

    /**
     * Vráti maximálny povolený počet vylepšení v inventári.
     *
     * @return maximálny počet vylepšení
     */
    public int getMaxVylepseni() {
        return MAX_VYLEPSENI;
    }

    /**
     * Aktivuje damage boost na zadaný počet framov.
     * Ak boost už beží, trvanie sa predĺži na maximum z aktuálneho a nového.
     *
     * @param frames  počet framov, počas ktorých boost platí
     */
    public void aktivujBoostDamage(int frames) {
        this.boostDamageFrames = Math.max(this.boostDamageFrames, frames);
    }

    /**
     * Aktivuje speed boost na zadaný počet framov.
     * Ak boost už beží, trvanie sa predĺží na maximum z aktuálneho a nového.
     *
     * @param frames  počet framov, počas ktorých boost platí
     */
    public void aktivujBoostSpeed(int frames) {
        this.boostSpeedFrames = Math.max(this.boostSpeedFrames, frames);
    }

    /**
     * Zistí, či je damage boost momentálne aktívny.
     *
     * @return {@code true} ak boost ešte trvá
     */
    public boolean jeBoostDamageAktivny() {
        return this.boostDamageFrames > 0;
    }

    /**
     * Zistí, či je speed boost momentálne aktívny.
     *
     * @return {@code true} ak boost ešte trvá
     */
    public boolean jeBoostSpeedAktivny() {
        return this.boostSpeedFrames > 0;
    }

    /**
     * Vráti zostatok trvania damage boostu zaokrúhlený nahor na celé sekundy.
     *
     * @return zostatok v sekundách (0 ak boost nie je aktívny)
     */
    public int getBoostDamageSekundy() {
        return (this.boostDamageFrames + 59) / 60;
    }

    /**
     * Vráti zostatok trvania speed boostu zaokrúhlený nahor na celé sekundy.
     *
     * @return zostatok v sekundách (0 ak boost nie je aktívny)
     */
    public int getBoostSpeedSekundy() {
        return (this.boostSpeedFrames + 59) / 60;
    }

    /**
     * Vráti efektívne poškodenie hráča vrátane prípadného damage boostu.
     *
     * @return aktuálne poškodenie za jeden útok
     */
    public int getDamage() {
        return 1 + (this.jeBoostDamageAktivny() ? BONUS_DAMAGE : 0);
    }

    private int getEffectiveSpeed() {
        return super.getSpeed() + (this.jeBoostSpeedAktivny() ? BONUS_SPEED : 0);
    }


    /**
     * Zistí, či hráč vlastní meč.
     *
     * @return {@code true} ak hráč má meč
     */
    public boolean mamMec() {
        return this.mamMec;
    }

    /**
     * Zistí, či hráč vlastní štít.
     *
     * @return {@code true} ak hráč má štít
     */
    public boolean mamStit() {
        return this.mamStit;
    }

    /**
     * Vráti počet kľúčov, ktoré hráč doteraz pozbiera.
     *
     * @return počet kľúčov
     */
    public int getPocetKlucov() {
        return this.pocetKlucov;
    }

    /**
     * Vráti aktuálny počet životov hráča.
     *
     * @return aktuálne životy
     */
    public int getZivoty() {
        return this.zivoty;
    }

    /**
     * Vráti maximálny počet životov hráča.
     *
     * @return maximálne životy
     */
    public int getMaxZivotov() {
        return this.maxZivotov;
    }

    /**
     * Vráti x-ovú obrazovkovú súradnicu stredu hráčovho spritu (fixná pozícia).
     *
     * @return x-ová súradnica hráča na obrazovke v pixeloch
     */
    public int getOknoX() {
        return this.oknoX;
    }

    /**
     * Vráti y-ovú obrazovkovú súradnicu stredu hráčovho spritu (fixná pozícia).
     *
     * @return y-ová súradnica hráča na obrazovke v pixeloch
     */
    public int getOknoY() {
        return this.oknoY;
    }
}