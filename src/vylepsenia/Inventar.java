package vylepsenia;

import entity.Hrac;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * Dialógové okno inventára hráča.
 *
 * <p>Zobrazuje polymorfný {@code List<Vylepsenie>} v {@code JList} s ikonou
 * a názvom každej položky. Tlačidlo <em>Použi</em> zavolá {@link Vylepsenie#pouzi(Hrac)}
 * a predmet odoberie; <em>Zahoď</em> predmet odoberie bez efektu.</p>
 *
 * <p>Trieda nepozná konkrétne typy elixírov — pridanie nového vylepšenia
 * sa tohto okna vôbec nedotkne.</p>
 */
public class Inventar {
    private JPanel rootPanel;
    private JButton zahodButton;
    private JButton pouziButton;
    private JList<Vylepsenie> list1;

    private final Hrac hrac;
    private final DefaultListModel<Vylepsenie> model;

    /**
     * Vytvorí a inicializuje dialóg inventára pre daného hráča.
     *
     * @param hrac  hráč, ktorého inventár sa zobrazuje a modifikuje
     */
    public Inventar(Hrac hrac) {
        this.hrac = hrac;
        this.model = new DefaultListModel<>();
        this.list1.setModel(this.model);
        this.list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.list1.setCellRenderer(new VylepsenieRenderer());

        this.pouziButton.addActionListener(e -> this.pouziVybrane());
        this.zahodButton.addActionListener(e -> this.zahodVybrane());

        this.refresh();
    }

    /**
     * Vráti koreňový panel dialógu vygenerovaný IntelliJ UI Designerom.
     * Používa ho {@code GamePanel.openInventar()} ako obsah {@code JDialog}.
     *
     * @return koreňový {@link JPanel} dialógu
     */
    public JPanel getRootPanel() {
        return this.rootPanel;
    }

    /*
     * Pretiahne stav inventára z Hraca do JListu. Voláme po každej akcii,
     * aby UI odzrkadlilo aktuálne pole vylepšení.
     */
    private void refresh() {
        this.model.clear();
        for (Vylepsenie v : this.hrac.getInventar()) {
            this.model.addElement(v);
        }
        // Tlačidlá deaktivujeme, keď je inventár prázdny - menej spôsobov,
        // ako si používateľ klikne do prázdna.
        boolean maCo = !this.model.isEmpty();
        this.pouziButton.setEnabled(maCo);
        this.zahodButton.setEnabled(maCo);
    }

    private void pouziVybrane() {
        int idx = this.list1.getSelectedIndex();
        if (idx < 0) {
            return;
        }
        Vylepsenie v = this.model.get(idx);
        v.pouzi(this.hrac);
        this.hrac.vyhodZInventara(idx);
        this.refresh();
    }

    private void zahodVybrane() {
        int idx = this.list1.getSelectedIndex();
        if (idx < 0) {
            return;
        }
        this.hrac.vyhodZInventara(idx);
        this.refresh();
    }

    /*

     pri navrhovani tejto casti som si pomohol AI
     * Vykresľovač položiek - ikona elixíru zmenšená na 32x32 a vedľa nej
     * názov. Bez vlastného renderera by JList ukázal len toString() objektu.
     */
    private static class VylepsenieRenderer extends JLabel implements ListCellRenderer<Vylepsenie> {

        @Override
        public Component getListCellRendererComponent(JList<? extends Vylepsenie> list, Vylepsenie value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            this.setOpaque(true);
            this.setText(value.getNazov());

            BufferedImage ikona = value.getIkona();
            if (ikona != null) {
                this.setIcon(new ImageIcon(ikona.getScaledInstance(32, 32, Image.SCALE_SMOOTH)));
            } else {
                this.setIcon(null);
            }

            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            } else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }
            return this;
        }
    }
}