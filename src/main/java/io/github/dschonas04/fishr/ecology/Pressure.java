// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr.ecology;

/**
 * Fangdruck auf einem Gewässerabschnitt (einem Chunk), 0 (unberührt) .. 1 (leergefischt). Jeder
 * entnommene Fisch erhöht ihn; mit der Zeit sinkt er wieder, weil Fische zuziehen und heranwachsen.
 * Solange er hoch ist, sind die Fische dort scheuer und weniger.
 */
public final class Pressure {
    private Pressure() {}

    /** Wird für jeden entnommenen Fisch addiert (zurückgesetzte zählen halb). */
    public static final double PER_CATCH = 0.08;

    /** Erholung pro Spielstunde (1000 Ticks). */
    public static final double PER_HOUR = 0.025;

    public static double decayed(double pressure, long ticksPassed) {
        return Math.clamp(pressure - PER_HOUR * Math.max(0, ticksPassed) / 1000.0, 0, 1);
    }

    public static double afterCatch(double pressure, boolean released) {
        return Math.clamp(pressure + (released ? PER_CATCH / 2 : PER_CATCH), 0, 1);
    }

    /** Faktor auf Bisse und Spawns: Ein leergefischter Platz gibt noch Fische, nur viel weniger. */
    public static double biteFactor(double pressure) {
        return 1 - 0.65 * Math.clamp(pressure, 0, 1);
    }
}
