package io.github.dschonas04.fishr.fish;

/**
 * Von der Angelpose umgesetzt (siehe FishingHookMixin): Lässt einen Fisch die Pose so beißen
 * lassen, wie es Vanilla tut.
 */
public interface BiteTarget {
    /**
     * Taucht die Pose ab, spritzt und öffnet das Vanilla-Fenster, in dem ein Rechtsklick als Fang
     * zählt.
     */
    void fishr$bite(int ticks);

    /** Server: Ein Bissfenster ist offen. Client: Die Pose ist untergezogen. */
    boolean fishr$isBiting();

    /**
     * Ticks seit Beginn des laufenden Bisses (für den Zeitpunkt des Anschlags); groß, wenn kein
     * Biss da ist.
     */
    int fishr$ticksSinceBite();
}
