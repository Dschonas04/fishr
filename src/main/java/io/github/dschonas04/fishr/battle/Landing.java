// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr.battle;

/**
 * Landen mit dem Kescher: Der erschöpfte Fisch windet sich noch vor dem Kescher. Eine Marke
 * schwingt hin und her; die Öffnung des Keschers ist ein Fenster um die Mitte, breiter bei einem
 * geräumigen Kescher und einem kleinen Fisch. Keschern mit der Marke im Fenster landet sicher,
 * außerhalb reißt sich der Fisch oft los. Server und Client nutzen dieselbe Formel, die Anzeige,
 * die der Spieler sieht, ist also die, nach der der Server urteilt.
 */
public final class Landing {
    private Landing() {}

    /** Die Lage des Fisches vor dem Kescher, -1 .. 1, Ticks, seit der Fisch landbar wurde. */
    public static double marker(int ticks) {
        return Math.sin(ticks * 0.16) * 0.75 + Math.sin(ticks * 0.37) * 0.25;
    }

    /**
     * Halbe Breite der Kescheröffnung, 0.12 .. 0.6: die Größe des Keschers gegenüber der Länge des
     * Fisches.
     */
    public static double window(double netLengthCm, double fishLengthCm) {
        if (fishLengthCm > netLengthCm) return 0;
        return Math.clamp(0.6 * (1 - fishLengthCm / netLengthCm) + 0.12, 0.12, 0.6);
    }

    public static boolean aligned(int ticks, double window) {
        return Math.abs(marker(ticks)) <= window;
    }

    /**
     * Landechance beim Keschern; gut gezieltes Keschern eines erschöpften Fisches klappt fast
     * immer.
     */
    public static double chance(boolean aligned, double stamina, boolean boss) {
        double chance = (aligned ? 0.97 : 0.35) - stamina * 1.5;
        return Math.clamp(boss ? chance - 0.1 : chance, 0.1, 0.97);
    }
}
