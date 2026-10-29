package io.github.dschonas04.fishr.item;

/**
 * Die Schnur: Tragkraft (kg) und Dehnung (schluckt Stöße). Ohne Schnur angelt die Rute mit
 * einfachem Faden.
 */
public enum LineType {
    STRING(null, 3.5, 0.25),
    LINE("fishing_line", 20, 0.2);

    private final String id;
    private final double strength, stretch;

    LineType(String id, double strength, double stretch) {
        this.id = id;
        this.strength = strength;
        this.stretch = stretch;
    }

    public String id() {
        return id;
    }

    public double strength() {
        return strength;
    }

    public double stretch() {
        return stretch;
    }
}
