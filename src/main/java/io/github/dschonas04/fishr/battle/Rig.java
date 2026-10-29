package io.github.dschonas04.fishr.battle;

/**
 * Das ganze Angelgerät, wie der Drill es sieht, vom Server beim Anschlag festgehalten: Rute, Rolle,
 * Schnur und Köder zu physikalischen Werten zusammengefasst.
 *
 * @param lineStrength Last in kg, die die Schnur trägt, bevor sie reißt
 * @param stretch 0..1, wie stark die Schnur sich dehnt und Stöße schluckt
 * @param rodFlex 0..1, wie stark sich der Blank biegt und Stöße abfedert
 * @param rodLoad kg, die die Rute trägt; die Bremse stellt sich nie darüber ein
 * @param reelSpeed Meter Schnur pro Tick, die die Rolle ohne Last einholt
 * @param reelForce kg, gegen die die Rolle kurbeln kann
 * @param capacity Meter Schnur auf der Spule
 * @param staminaDrain wie schnell der Fisch ermüdet (Köder), 1 = normal
 * @param runDamping wie hart Fluchten ziehen (Köder), 1 = normal, weniger ist besser
 */
public record Rig(
        double lineStrength,
        double stretch,
        double rodFlex,
        double rodLoad,
        double reelSpeed,
        double reelForce,
        double capacity,
        double staminaDrain,
        double runDamping) {
    /** Eine einfache Vanilla-Angel: Faden, ein Stock und keine Rolle, die den Namen verdient. */
    public static final Rig VANILLA = new Rig(3.5, 0.25, 0.5, 5, 0.09, 4, 60, 1, 1);

    public Rig {
        double[] values = {
            lineStrength,
            stretch,
            rodFlex,
            rodLoad,
            reelSpeed,
            reelForce,
            capacity,
            staminaDrain,
            runDamping
        };
        for (double value : values)
            if (!Double.isFinite(value))
                throw new IllegalArgumentException("Rig values must be finite");
        if (lineStrength <= 0
                || rodLoad <= 0
                || reelSpeed <= 0
                || reelForce <= 0
                || capacity <= 0
                || staminaDrain <= 0
                || runDamping <= 0) {
            throw new IllegalArgumentException("Rig values must be positive");
        }
    }

    /**
     * Köderwirkungen: mehr Belastung (schwächere Schnur), schnelleres Ermüden, schwächere Fluchten.
     */
    public Rig scaled(double strainFactor, double drainFactor, double runFactor) {
        return new Rig(
                lineStrength / strainFactor,
                stretch,
                rodFlex,
                rodLoad,
                reelSpeed,
                reelForce,
                capacity,
                staminaDrain * drainFactor,
                runDamping * runFactor);
    }

    /**
     * Verstärkungen der Rute (Perlmutt in der Werkstatt): Die Rute trägt mehr, und die Rolle zieht
     * kräftiger.
     */
    public Rig upgraded(int level) {
        if (level <= 0) return this;
        double factor = 1 + 0.06 * level;
        return new Rig(
                lineStrength,
                stretch,
                rodFlex,
                rodLoad * factor,
                reelSpeed,
                reelForce * factor,
                capacity,
                staminaDrain,
                runDamping);
    }
}
