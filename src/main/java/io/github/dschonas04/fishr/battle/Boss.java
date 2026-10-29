package io.github.dschonas04.fishr.battle;

import java.util.List;
import java.util.Locale;

/**
 * Die vier Bossfische. Ihre Drills sind nicht nur größere Zahlen: Jeder hat Phasen mit eigenem
 * Verhalten und einen Kniff. Der Alte vom Grund täuscht kurz vor der Landung Erschöpfung vor; der
 * Schilfkönig springt wieder und wieder; der Schwarze Stör zieht mit der Strömung und bricht aus
 * langen Ruhephasen aus; der Tiefenwächter lernt: Er antwortet auf die Technik, die der Angler
 * immer wieder benutzt.
 */
public enum Boss {
    ANCIENT_CATFISH(
            20,
            2.2,
            0.2,
            List.of(
                    new Phase(
                            "awakening",
                            Fighter.weights(2, 5, 5, 1, 1, 0),
                            1.0,
                            1.0,
                            22,
                            1.0,
                            0,
                            1),
                    new Phase(
                            "deadwood", Fighter.weights(2, 2, 2, 7, 2, 0), 1.0, 1.0, 18, 1.8, 0, 1),
                    new Phase(
                            "deception",
                            Fighter.weights(2, 4, 3, 3, 2, 0),
                            0.9,
                            1.0,
                            16,
                            1.4,
                            0,
                            1))),
    REED_KING(
            12,
            2.4,
            0.26,
            List.of(
                    new Phase("hunt", Fighter.weights(2, 5, 1, 5, 2, 2), 1.0, 1.1, 18, 1.5, 0, 1),
                    new Phase("dance", Fighter.weights(2, 2, 0, 1, 1, 8), 1.0, 1.0, 14, 1.0, 0, 1),
                    new Phase(
                            "last_stand",
                            Fighter.weights(2, 3, 1, 3, 5, 4),
                            1.1,
                            1.1,
                            12,
                            1.3,
                            0,
                            1))),
    BLACK_STURGEON(
            17,
            3.8,
            0.2,
            List.of(
                    new Phase(
                            "current",
                            Fighter.weights(6, 4, 1, 1, 1, 0),
                            1.0,
                            1.0,
                            20,
                            1.0,
                            0.25,
                            2.2),
                    new Phase(
                            "bottom",
                            Fighter.weights(5, 3, 5, 1, 2, 0),
                            1.1,
                            1.0,
                            18,
                            1.2,
                            0.3,
                            2.0),
                    new Phase(
                            "eruption",
                            Fighter.weights(4, 6, 2, 1, 2, 1),
                            1.35,
                            1.4,
                            14,
                            1.0,
                            0.3,
                            1.8))),
    DEEP_WARDEN(
            16,
            3.8,
            0.24,
            List.of(
                    new Phase("hunter", Fighter.weights(2, 5, 2, 1, 3, 1), 1.0, 1.0, 16, 1.0, 0, 1),
                    new Phase("shadow", Fighter.weights(2, 2, 4, 5, 2, 0), 1.0, 1.1, 10, 1.5, 0, 1),
                    new Phase(
                            "frenzy",
                            Fighter.weights(1, 4, 2, 3, 4, 4),
                            1.15,
                            1.2,
                            8,
                            1.3,
                            0,
                            0.8)));

    /**
     * Eine Phase eines Bossdrills; die Ausdauer des Bosses wird gleichmäßig auf seine Phasen
     * verteilt.
     *
     * @param power Faktor auf den Zug des Bosses
     * @param speed Faktor auf seine Schwimmgeschwindigkeit
     * @param warning Ticks Vorwarnung vor einer Aktion
     * @param snags wie schnell Seitenfluchten Deckung erreichen
     * @param current Anteil der Kraft des Bosses, den das Wasser ständig auf die Schnur gibt
     * @param calm Faktor darauf, wie lange er zwischen Aktionen ruht
     */
    public record Phase(
            String id,
            int[] weights,
            double power,
            double speed,
            int warning,
            double snags,
            double current,
            double calm) {
        public String translationKey(Boss boss) {
            return "boss.fishr." + boss.id() + ".phase." + id;
        }
    }

    private final double power, endurance, speed;
    private final List<Phase> phases;

    Boss(double power, double endurance, double speed, List<Phase> phases) {
        this.power = power;
        this.endurance = endurance;
        this.speed = speed;
        this.phases = phases;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "boss.fishr." + id();
    }

    public double power() {
        return power;
    }

    public double endurance() {
        return endurance;
    }

    public double speed() {
        return speed;
    }

    public int phases() {
        return phases.size();
    }

    public Phase phase(int index) {
        return phases.get(Math.clamp(index, 0, phases.size() - 1));
    }
}
