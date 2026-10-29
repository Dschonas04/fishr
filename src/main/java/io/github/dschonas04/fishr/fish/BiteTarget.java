package io.github.dschonas04.fishr.fish;

/**
 * Implemented by the fishing float (see FishingHookMixin): lets a fish make the float bite like
 * vanilla does.
 */
public interface BiteTarget {
    /**
     * Dips the float, splashes and opens the vanilla window in which a right-click counts as a
     * catch.
     */
    void fishr$bite(int ticks);

    /** Server: a bite window is open. Client: the float is pulled under. */
    boolean fishr$isBiting();

    /** Ticks since the current bite began (for the strike timing); large when there is no bite. */
    int fishr$ticksSinceBite();
}
