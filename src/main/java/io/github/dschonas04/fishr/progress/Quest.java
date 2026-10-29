// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr.progress;

import io.github.dschonas04.fishr.fish.FishSpecies;
import io.github.dschonas04.fishr.fish.Habitat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Tägliche und wöchentliche Angelaufträge. Sie sind für alle auf einem Server und an einem Tag
 * gleich, aus der Tageszahl erzeugt, und verlangen Abwechslung: eine bestimmte Art, einen schweren
 * Fisch, mehrere Fische aus einem Gewässer, etwas Seltenes, zurückgesetzte Fische.
 *
 * @param id fester Schlüssel für den Fortschritt (Art, Tag und Platz)
 * @param target Art-Kennung, Name des Lebensraums oder der Seltenheit; leer, wenn nicht nötig
 * @param amount kg für WEIGHT-Aufträge
 * @param count wie viele Fische
 */
public record Quest(
        String id,
        Kind kind,
        String target,
        double amount,
        int count,
        int emeralds,
        int experience,
        boolean weekly) {
    public enum Kind {
        SPECIES,
        WEIGHT,
        HABITAT,
        RARITY,
        RELEASE
    }

    /** Was passiert ist: Irgendwo wurde ein Fisch gefangen (oder zurückgesetzt). */
    public record Event(FishSpecies species, double weightKg, Habitat habitat, boolean released) {}

    public boolean counts(Event event) {
        return switch (kind) {
            case SPECIES -> !event.released() && event.species().id().equals(target);
            case WEIGHT -> !event.released() && event.weightKg() >= amount;
            case HABITAT ->
                    !event.released()
                            && event.habitat() != null
                            && event.habitat().name().equals(target);
            case RARITY ->
                    !event.released()
                            && event.species().rarity().ordinal()
                                    >= FishSpecies.Rarity.valueOf(target).ordinal();
            case RELEASE -> event.released();
        };
    }

    public String translationKey() {
        return "quest.fishr." + kind.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static final FishSpecies[] EVERYDAY = {
        FishSpecies.ROACH,
        FishSpecies.PERCH,
        FishSpecies.BREAM,
        FishSpecies.RUDD,
        FishSpecies.RIVER_TROUT,
        FishSpecies.CHUB,
        FishSpecies.HERRING,
        FishSpecies.MACKEREL,
        FishSpecies.PLAICE,
        FishSpecies.ATLANTIC_COD,
        FishSpecies.TENCH,
        FishSpecies.CARP,
        FishSpecies.PIKE,
        FishSpecies.CRUCIAN_CARP
    };

    public static List<Quest> daily(long day) {
        Random random = new Random(day * 7919L + 17);
        List<Quest> quests = new ArrayList<>();
        FishSpecies species = EVERYDAY[random.nextInt(EVERYDAY.length)];
        quests.add(
                new Quest(
                        "d" + day + "a",
                        Kind.SPECIES,
                        species.id(),
                        0,
                        1 + random.nextInt(3),
                        2,
                        30,
                        false));
        Habitat habitat = Habitat.SURFACE.get(random.nextInt(Habitat.SURFACE.size()));
        quests.add(
                new Quest(
                        "d" + day + "b",
                        Kind.HABITAT,
                        habitat.name(),
                        0,
                        3 + random.nextInt(3),
                        3,
                        40,
                        false));
        quests.add(
                random.nextBoolean()
                        ? new Quest(
                                "d" + day + "c",
                                Kind.WEIGHT,
                                "",
                                2 + random.nextInt(5),
                                1,
                                4,
                                50,
                                false)
                        : new Quest(
                                "d" + day + "c",
                                Kind.RELEASE,
                                "",
                                0,
                                2 + random.nextInt(3),
                                2,
                                40,
                                false));
        return quests;
    }

    public static List<Quest> weekly(long week) {
        Random random = new Random(week * 104729L + 3);
        List<Quest> quests = new ArrayList<>();
        quests.add(
                new Quest(
                        "w" + week + "a",
                        Kind.RARITY,
                        random.nextBoolean() ? "RARE" : "EPIC",
                        0,
                        1,
                        12,
                        200,
                        true));
        quests.add(
                new Quest(
                        "w" + week + "b",
                        Kind.WEIGHT,
                        "",
                        10 + random.nextInt(20),
                        1,
                        16,
                        250,
                        true));
        return quests;
    }
}
