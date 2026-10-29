package io.github.dschonas04.fishr.battle;

import java.util.Locale;

/**
 * Was der gehakte Fisch gerade tut. Jede Aktion außer CALM und TIRED kündigt sich zuerst kurz an
 * (der Fisch schlägt um sich), damit ein guter Angler reagieren kann: vor einer Flucht die Bremse
 * lockern, vor einem Sprung die Rute senken, gegen eine Seitenflucht steuern.
 */
public enum FishAction {
    /** Hält gegen die Schnur; die Zeit zum Einholen und Pumpen. */
    CALM(0.35, false),
    /** Flüchtet vom Angler weg: Nimmt der Rolle Schnur ab, wenn er stärker zieht als die Bremse. */
    RUN(1.0, true),
    /** Zieht zum Grund. */
    DIVE(0.85, true),
    /**
     * Flüchtet seitwärts in Deckung; dagegen steuern, sonst scheuert die Schnur an Hindernissen.
     */
    SIDE(0.75, true),
    /**
     * Kopfschütteln: Rucke an der Schnur, die den Haken lockern, wenn die Schnur zu straff oder zu
     * locker ist.
     */
    SHAKE(0.6, true),
    /** Springt aus dem Wasser, um den Haken abzuschütteln. */
    JUMP(0.15, true),
    /** Für einen Moment erschöpft. */
    TIRED(0.15, false);

    private final double pull;
    private final boolean announced;

    FishAction(double pull, boolean announced) {
        this.pull = pull;
        this.announced = announced;
    }

    /** Anteil seiner vollen Kraft, den der Fisch auf die Schnur gibt. */
    public double pull() {
        return pull;
    }

    public boolean announced() {
        return announced;
    }

    public String translationKey() {
        return "action.fishr." + name().toLowerCase(Locale.ROOT);
    }
}
