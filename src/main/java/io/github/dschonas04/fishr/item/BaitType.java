// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr.item;

import io.github.dschonas04.fishr.battle.Boss;
import io.github.dschonas04.fishr.fish.FishSpecies;

/**
 * Alle Köder und Kunstköder. Naturköder stapeln sich, und pro Drill wird einer verbraucht;
 * Kunstköder stapeln sich nicht, nutzen sich mit jedem Drill ab (ein abgenutzter läuft schlechter)
 * und gehen verloren, wenn die Schnur reißt. Die Wirkung sagt, wie sehr jede Ernährungsweise ihn
 * mag: Ein Fisch beachtet einen Köder nicht, den er nicht mag, ein großer Köder wählt größere
 * Fische.
 */
public enum BaitType {
    // Naturköder; Wirkung für: Friedfisch, Raubfisch, Salmonide, Meeresräuber, Meeresgrund, Schwarm
    /** Für die friedlichen Fische: Friedfische, Forellen, Grundfische, Schwärme. */
    WORM("worm_bait", 0, new double[] {1.3, 0.4, 1.2, 0.3, 1.1, 1.0}, 0, 1, 1, 1),
    /** Für die Raubfische, im Süßwasser und im Meer. */
    BAITFISH("baitfish_bait", 0, new double[] {0, 1.5, 0.6, 1.4, 0.6, 0}, 0.5, 1, 1, 1),
    /** Lockt jeden Fisch, nachts am stärksten; Fluchten ziehen schwächer. */
    GLOW("glow_bait", 0, new double[] {0.8, 0.8, 0.8, 0.8, 0.8, 0.8}, 0, 1, 0.8, 1),
    // in der Welt zu finden (nach You Need Bait und Fish of Thieves): aus Sand, Schlamm und Laub
    // gegraben
    /** Maden aus Sand: der Klassiker für Rotauge, Brasse und jeden kleinen Friedfisch. */
    GRUB("grub_bait", 0, new double[] {1.4, 0.2, 1.0, 0, 0.6, 1.3}, -0.3, 1, 1, 1),
    /**
     * Blutegel aus Schlamm und Flusssand: Wels, Aal und Zander lieben sie; bringt größere Fische.
     */
    LEECH("leech_bait", 0, new double[] {0.6, 1.3, 0.8, 0.6, 0.9, 0}, 0.4, 1, 1, 1),
    /** Raupen aus dem Laub: wofür Forelle und Äsche steigen. */
    CATERPILLAR("caterpillar_bait", 0, new double[] {0.9, 0.2, 1.7, 0, 0, 0.5}, 0, 1, 1, 1),
    // Bossköder: für sich ein starker Köder, am richtigen Ort der Ruf eines Bosses
    ANCIENT("ancient_bait", 0, new double[] {0, 1.4, 0, 0, 0, 0}, 0.8, 1, 1, 1),
    REED_KING("reed_king_lure", 60, new double[] {0, 1.6, 0.4, 0, 0, 0}, 0.8, 1, 1, 1),
    CURRENT("current_bait", 0, new double[] {1.2, 0, 0, 0, 1.0, 0}, 0.8, 1, 1, 1),
    DEEP_LIGHT("deep_light_bait", 0, new double[] {0.8, 0.8, 0.8, 0.8, 0.8, 0.8}, 0.6, 1, 1, 1);

    private final String id;
    private final int durability;
    private final double[] appeal;
    private final double sizeBias, drain, run, strain;

    BaitType(
            String id,
            int durability,
            double[] appeal,
            double sizeBias,
            double drain,
            double run,
            double strain) {
        this.id = id;
        this.durability = durability;
        this.appeal = appeal;
        this.sizeBias = sizeBias;
        this.drain = drain;
        this.run = run;
        this.strain = strain;
    }

    public String id() {
        return id;
    }

    public boolean lure() {
        return durability > 0;
    }

    /**
     * Würmer und Verwandte, kein Kunstköder und kein Bossköder: Fische folgen ihnen und lassen sich
     * damit füttern.
     */
    public boolean natural() {
        return !lure() && boss() == null && this != GLOW;
    }

    public int durability() {
        return durability;
    }

    public double sizeBias() {
        return sizeBias;
    }

    /** Faktor darauf, wie schnell der Fisch ermüdet. */
    public double drain() {
        return drain;
    }

    /** Faktor darauf, wie hart der Fisch in Fluchten zieht. */
    public double run() {
        return run;
    }

    /** Faktor auf die Schnurbelastung (über 1 ist schlechter). */
    public double strain() {
        return strain;
    }

    public Boss boss() {
        return switch (this) {
            case ANCIENT -> Boss.ANCIENT_CATFISH;
            case REED_KING -> Boss.REED_KING;
            case CURRENT -> Boss.BLACK_STURGEON;
            case DEEP_LIGHT -> Boss.DEEP_WARDEN;
            default -> null;
        };
    }

    /** Wie gern diese Art diesen Köder nimmt; unter 0.5 beachtet sie ihn nicht. */
    public double appeal(FishSpecies species) {
        return appeal[species.diet().ordinal()];
    }

    /** Ohne Köder fängt der Haken nur ab und zu etwas. */
    public static final double BARE_HOOK = 0.4;

    public String translationKey() {
        return "item.fishr." + id;
    }
}
