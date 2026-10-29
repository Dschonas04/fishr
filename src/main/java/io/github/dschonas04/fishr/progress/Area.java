package io.github.dschonas04.fishr.progress;

import io.github.dschonas04.fishr.fish.Habitat;

import java.util.Locale;

/**
 * Angelgebiete (Anforderung 19). Jeder Lebensraum gehört zu einem Gebiet; ein Gebiet über der Stufe
 * des Anglers gibt trotzdem Bisse, aber nur gewöhnliche und ungewöhnliche Fische, bis der Angler
 * die Stufe dafür hat.
 */
public enum Area {
    HOME_LAKE(0),
    FOREST_RIVER(3),
    CAVERNS(4),
    MIST_MOOR(5),
    COAST(7),
    LAVA_LAKES(8),
    DEEP_RESERVE(10),
    END_WATERS(12);

    private final int level;

    Area(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }

    public String translationKey() {
        return "area.fishr." + name().toLowerCase(Locale.ROOT);
    }

    public static Area of(Habitat habitat) {
        return switch (habitat) {
            case LAKE -> HOME_LAKE;
            case RIVER, COLD -> FOREST_RIVER;
            case SWAMP -> MIST_MOOR;
            case OCEAN, WARM_OCEAN -> COAST;
            case COLD_OCEAN, DEEP_OCEAN -> DEEP_RESERVE;
            case CAVE -> CAVERNS;
            case LAVA -> LAVA_LAKES;
            case END -> END_WATERS;
        };
    }

    public boolean open(int anglerLevel) {
        return anglerLevel >= level;
    }
}
