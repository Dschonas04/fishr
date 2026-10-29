package io.github.dschonas04.fishr.item;

/**
 * Die Ruten, von der Anfänger- bis zur besten.
 *
 * @param flex 0..1, wie stark sich der Blank biegt und Stöße abfedert
 * @param speed Faktor auf das Einholen der Rolle
 * @param load kg, die die Rute trägt; die Bremse stellt sich nie darüber ein
 * @param sensitivity 0..1, wie gut der Angler den Biss spürt (Anschlagfenster)
 * @param cast Faktor auf die Wurfweite
 */
public enum RodType {
    VANILLA(0.5, 1.0, 5, 0.6, 1.0),
    /** Leicht und feinfühlig, für Fische bis zu ein paar Kilo. */
    BAMBOO(0.8, 1.0, 7, 0.9, 0.9),
    /** Die Allrounderin, stark genug für die Bosse. */
    CARBON(0.6, 1.1, 24, 0.8, 1.35),
    /** Die einzige Rute, die in Lava angelt; die stärkste. */
    NETHERITE(0.45, 1.05, 30, 0.7, 1.25);
    private final double flex, speed, load, sensitivity, cast;

    RodType(double flex, double speed, double load, double sensitivity, double cast) {
        this.flex = flex;
        this.speed = speed;
        this.load = load;
        this.sensitivity = sensitivity;
        this.cast = cast;
    }

    public double flex() {
        return flex;
    }

    public double speed() {
        return speed;
    }

    public double load() {
        return load;
    }

    public double sensitivity() {
        return sensitivity;
    }

    public double cast() {
        return cast;
    }
}
