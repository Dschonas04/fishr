// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr.battle;

/** Kampfstile, unabhängig von der Vanilla-Beute, die beim Landen ausgewürfelt wird. */
public enum FishStyle {
    DARTER(1.0, 1.15),
    FIGHTER(1.15, 1.0),
    BRUISER(1.3, 0.9);

    private final double pull;
    private final double catchSpeed;

    FishStyle(double pull, double catchSpeed) {
        this.pull = pull;
        this.catchSpeed = catchSpeed;
    }

    public double pull() {
        return pull;
    }

    public double catchSpeed() {
        return catchSpeed;
    }

    public String translationKey() {
        return "fish.fishr." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
