// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr.battle;

/**
 * Wie ein gehakter Fisch kämpft, abgeleitet aus Art und Gewicht (oder aus seinem Bossprofil).
 *
 * @param power kg, die der Fisch bei voller Kraft in einer Flucht zieht
 * @param endurance wie lange er durchhält; 1 ist ein fitter Fisch von etwa 8 kg
 * @param speed Meter pro Tick, die er in einer Flucht schwimmt
 * @param weight kg, für die Landung
 * @param weights wie oft er jede {@link FishAction} wählt, nach Ordinalzahl
 * @param strikeIdeal Ticks nach dem Biss, bei denen ein Anschlag den Haken am besten setzt
 * @param salt ein Meeresfisch (für die Gezeitenrolle)
 * @param teeth scharfe Zähne, die eine Schnur ohne Stahlvorfach durchbeißen
 * @param boss das Bossprofil oder null
 */
public record Fighter(
        double power,
        double endurance,
        double speed,
        double weight,
        int[] weights,
        int strikeIdeal,
        boolean salt,
        boolean teeth,
        Boss boss) {
    public Fighter {
        if (!(power > 0)
                || !(endurance > 0)
                || !(speed > 0)
                || !(weight > 0)
                || weights.length != FishAction.values().length) {
            throw new IllegalArgumentException("Invalid fighter");
        }
    }

    public static Fighter of(Boss boss, double weight) {
        return new Fighter(
                boss.power(),
                boss.endurance(),
                boss.speed(),
                weight,
                boss.phase(0).weights(),
                12,
                false,
                boss == Boss.REED_KING || boss == Boss.DEEP_WARDEN,
                boss);
    }

    /** Baut das Gewichtungsfeld: CALM, RUN, DIVE, SIDE, SHAKE, JUMP, TIRED. */
    public static int[] weights(int calm, int run, int dive, int side, int shake, int jump) {
        return new int[] {calm, run, dive, side, shake, jump, 0};
    }
}
