package io.github.dschonas04.fishr.fish;

import io.github.dschonas04.fishr.battle.Boss;
import io.github.dschonas04.fishr.battle.Fighter;
import io.github.dschonas04.fishr.battle.FishStyle;

import java.util.Locale;
import java.util.Random;
import java.util.function.ToDoubleFunction;

/**
 * Die Fische von Fishr: die Süßwasserfische Mitteleuropas (Salmoniden, Raubfische, Friedfische),
 * die Speisefische des Nordatlantiks und des Mittelmeers und der Goldkoi. Jeder hat einen
 * Größenbereich, eine Körperform für das 3D-Modell, einen Kampfstil und wie häufig er in jedem
 * {@link Habitat} ist. Ein Gewicht von 0 heißt, dass er dort nicht lebt.
 */
public enum FishSpecies {
    // Gewichte je Lebensraum: Fluss, See, Sumpf, kalt, Ozean, warmer Ozean, kalter Ozean, Tiefsee
    // Salmoniden
    RIVER_TROUT(
            18,
            80,
            0.000014,
            FishStyle.DARTER,
            Rarity.COMMON,
            2.0f,
            3.5f,
            8f,
            1f,
            4f,
            new int[] {8, 0, 0, 14, 0, 0, 0, 0}),
    RAINBOW_TROUT(
            20,
            80,
            0.0000115,
            FishStyle.DARTER,
            Rarity.UNCOMMON,
            2.0f,
            3.5f,
            8f,
            1f,
            4f,
            new int[] {7, 4, 0, 6, 0, 0, 0, 0}),
    GRAYLING(
            20,
            55,
            0.00001,
            FishStyle.DARTER,
            Rarity.UNCOMMON,
            1.8f,
            3.2f,
            8f,
            1f,
            4f,
            new int[] {4, 0, 0, 8, 0, 0, 0, 0}),
    SALMON(
            50,
            150,
            0.0000105,
            FishStyle.FIGHTER,
            Rarity.RARE,
            2.4f,
            4.0f,
            10f,
            1.4f,
            4.5f,
            new int[] {4, 0, 0, 3, 2, 0, 4, 0}),
    SEA_TROUT(
            40,
            110,
            0.0000105,
            FishStyle.FIGHTER,
            Rarity.RARE,
            2.2f,
            3.8f,
            9.5f,
            1.2f,
            4.5f,
            new int[] {4, 0, 0, 2, 3, 0, 3, 0}),
    // Raubfische im Süßwasser
    PIKE(
            35,
            150,
            0.000008,
            FishStyle.FIGHTER,
            Rarity.UNCOMMON,
            1.6f,
            2.8f,
            10f,
            2.5f,
            4f,
            new int[] {5, 8, 8, 3, 0, 0, 0, 0}),
    ZANDER(
            30,
            100,
            0.0000085,
            FishStyle.FIGHTER,
            Rarity.RARE,
            1.8f,
            3.2f,
            9f,
            1.5f,
            4f,
            new int[] {6, 6, 2, 0, 0, 0, 0, 0}),
    PERCH(
            12,
            55,
            0.000018,
            FishStyle.FIGHTER,
            Rarity.COMMON,
            2.0f,
            4.8f,
            6.5f,
            1f,
            4f,
            new int[] {10, 14, 10, 6, 0, 0, 0, 0}),
    CATFISH(
            40,
            220,
            0.000009,
            FishStyle.BRUISER,
            Rarity.RARE,
            3.0f,
            2.8f,
            9f,
            1f,
            4f,
            new int[] {3, 2, 8, 0, 0, 0, 0, 0}),
    EEL(
            40,
            130,
            0.0000016,
            FishStyle.DARTER,
            Rarity.RARE,
            1.2f,
            1.6f,
            13f,
            1f,
            3f,
            new int[] {5, 3, 8, 0, 1, 0, 0, 0}),
    ASP(
            30,
            90,
            0.0000105,
            FishStyle.DARTER,
            Rarity.UNCOMMON,
            1.8f,
            3.4f,
            9f,
            1.2f,
            4f,
            new int[] {6, 1, 0, 0, 0, 0, 0, 0}),
    // Friedfische
    CARP(
            25,
            110,
            0.000026,
            FishStyle.BRUISER,
            Rarity.UNCOMMON,
            3.0f,
            4.8f,
            6.5f,
            1f,
            4f,
            new int[] {3, 12, 14, 0, 0, 0, 0, 0}),
    BARBEL(
            25,
            90,
            0.0000105,
            FishStyle.FIGHTER,
            Rarity.UNCOMMON,
            2.0f,
            3.0f,
            9f,
            1.5f,
            4f,
            new int[] {7, 0, 0, 2, 0, 0, 0, 0}),
    ROACH(
            10,
            45,
            0.000015,
            FishStyle.DARTER,
            Rarity.COMMON,
            1.6f,
            3.6f,
            6f,
            1f,
            3.5f,
            new int[] {12, 14, 10, 4, 0, 0, 0, 0}),
    RUDD(
            10,
            45,
            0.000016,
            FishStyle.DARTER,
            Rarity.COMMON,
            1.6f,
            3.8f,
            6f,
            1f,
            3.5f,
            new int[] {6, 10, 10, 0, 0, 0, 0, 0}),
    BREAM(
            25,
            75,
            0.000017,
            FishStyle.BRUISER,
            Rarity.COMMON,
            1.4f,
            5.2f,
            7f,
            1f,
            4f,
            new int[] {6, 12, 6, 0, 0, 0, 0, 0}),
    TENCH(
            20,
            65,
            0.000016,
            FishStyle.BRUISER,
            Rarity.UNCOMMON,
            2.2f,
            3.6f,
            7f,
            1f,
            4f,
            new int[] {2, 6, 12, 0, 0, 0, 0, 0}),
    CRUCIAN_CARP(
            10,
            45,
            0.00003,
            FishStyle.BRUISER,
            Rarity.COMMON,
            2.6f,
            4.6f,
            6f,
            1f,
            3.5f,
            new int[] {1, 10, 12, 0, 0, 0, 0, 0}),
    STURGEON(
            80,
            250,
            0.0000045,
            FishStyle.FIGHTER,
            Rarity.EPIC,
            2.4f,
            2.4f,
            13f,
            3f,
            5f,
            new int[] {2, 0, 0, 1, 0, 0, 0, 0}),
    CHUB(
            20,
            70,
            0.000013,
            FishStyle.FIGHTER,
            Rarity.COMMON,
            2.2f,
            3.4f,
            8f,
            1f,
            4f,
            new int[] {8, 3, 0, 2, 0, 0, 0, 0}),
    // Dorschartige
    ATLANTIC_COD(
            30,
            150,
            0.0000105,
            FishStyle.FIGHTER,
            Rarity.COMMON,
            2.4f,
            4.0f,
            9f,
            1.2f,
            4f,
            new int[] {0, 0, 0, 0, 8, 0, 12, 6}),
    SAITHE(
            30,
            120,
            0.0000095,
            FishStyle.FIGHTER,
            Rarity.UNCOMMON,
            2.2f,
            3.6f,
            9f,
            1.2f,
            4f,
            new int[] {0, 0, 0, 0, 6, 0, 8, 4}),
    ALASKA_POLLOCK(
            25,
            90,
            0.0000085,
            FishStyle.DARTER,
            Rarity.COMMON,
            2.0f,
            3.4f,
            8.5f,
            1.2f,
            4f,
            new int[] {0, 0, 0, 0, 3, 0, 10, 3}),
    HADDOCK(
            25,
            90,
            0.0000095,
            FishStyle.DARTER,
            Rarity.UNCOMMON,
            2.2f,
            3.6f,
            8.5f,
            1.2f,
            4f,
            new int[] {0, 0, 0, 0, 6, 0, 8, 3}),
    HAKE(
            30,
            130,
            0.0000075,
            FishStyle.FIGHTER,
            Rarity.UNCOMMON,
            1.8f,
            3.0f,
            10f,
            1.6f,
            4f,
            new int[] {0, 0, 0, 0, 5, 2, 3, 6}),
    WHITING(
            20,
            60,
            0.0000085,
            FishStyle.DARTER,
            Rarity.COMMON,
            1.8f,
            3.0f,
            8f,
            1.2f,
            3.5f,
            new int[] {0, 0, 0, 0, 7, 1, 4, 0}),
    // Heringsartige
    HERRING(
            18,
            40,
            0.0000085,
            FishStyle.DARTER,
            Rarity.COMMON,
            1.4f,
            2.8f,
            7f,
            1f,
            3.5f,
            new int[] {0, 0, 0, 0, 12, 0, 14, 0}),
    SARDINE(
            12,
            25,
            0.0000095,
            FishStyle.DARTER,
            Rarity.COMMON,
            1.2f,
            2.4f,
            6f,
            1f,
            3f,
            new int[] {0, 0, 0, 0, 6, 14, 0, 0}),
    ANCHOVY(
            9,
            20,
            0.0000065,
            FishStyle.DARTER,
            Rarity.COMMON,
            1.0f,
            1.8f,
            6f,
            1f,
            3f,
            new int[] {0, 0, 0, 0, 6, 14, 0, 0}),
    SPRAT(
            8,
            18,
            0.0000075,
            FishStyle.DARTER,
            Rarity.COMMON,
            1.0f,
            2.0f,
            5f,
            0.8f,
            3f,
            new int[] {0, 0, 0, 0, 10, 2, 8, 0}),
    // barschartige Meeresfische
    SEA_BASS(
            25,
            100,
            0.000012,
            FishStyle.FIGHTER,
            Rarity.UNCOMMON,
            2.0f,
            3.8f,
            8.5f,
            1.2f,
            4f,
            new int[] {0, 0, 0, 0, 6, 8, 0, 0}),
    GILTHEAD_BREAM(
            20,
            70,
            0.000017,
            FishStyle.FIGHTER,
            Rarity.UNCOMMON,
            1.6f,
            4.8f,
            6.5f,
            1f,
            4f,
            new int[] {0, 0, 0, 0, 3, 10, 0, 0}),
    RED_SNAPPER(
            30,
            100,
            0.000014,
            FishStyle.FIGHTER,
            Rarity.RARE,
            2.2f,
            4.6f,
            7.5f,
            1.2f,
            4f,
            new int[] {0, 0, 0, 0, 0, 8, 0, 2}),
    RED_MULLET(
            12,
            40,
            0.000013,
            FishStyle.DARTER,
            Rarity.UNCOMMON,
            1.8f,
            3.2f,
            7f,
            1.2f,
            3.5f,
            new int[] {0, 0, 0, 0, 3, 8, 0, 0}),
    GROUPER(
            40,
            200,
            0.000014,
            FishStyle.BRUISER,
            Rarity.EPIC,
            3.4f,
            5.0f,
            9f,
            1.6f,
            4f,
            new int[] {0, 0, 0, 0, 0, 3, 0, 2}),
    BARRACUDA(
            50,
            180,
            0.0000045,
            FishStyle.DARTER,
            Rarity.RARE,
            1.6f,
            2.6f,
            13f,
            2.5f,
            4f,
            new int[] {0, 0, 0, 0, 0, 3, 0, 1}),
    // Makrelen und Thunfische
    MACKEREL(
            20,
            55,
            0.0000085,
            FishStyle.DARTER,
            Rarity.COMMON,
            1.8f,
            3.2f,
            8f,
            1.2f,
            4f,
            new int[] {0, 0, 0, 0, 12, 8, 6, 0}),
    TUNA(
            80,
            300,
            0.000015,
            FishStyle.BRUISER,
            Rarity.EPIC,
            4.0f,
            5.6f,
            10f,
            1.6f,
            5f,
            new int[] {0, 0, 0, 0, 1, 2, 1, 3}),
    SKIPJACK(
            40,
            100,
            0.000013,
            FishStyle.FIGHTER,
            Rarity.UNCOMMON,
            2.8f,
            4.2f,
            8f,
            1.2f,
            4.5f,
            new int[] {0, 0, 0, 0, 2, 6, 0, 2}),
    SWORDFISH(
            100,
            400,
            0.0000035,
            FishStyle.FIGHTER,
            Rarity.EPIC,
            2.6f,
            4.4f,
            11f,
            6f,
            5f,
            new int[] {0, 0, 0, 0, 0, 1, 0, 1}),
    // Plattfische
    PLAICE(
            20,
            70,
            0.0000115,
            FishStyle.BRUISER,
            Rarity.COMMON,
            5.5f,
            1.0f,
            7f,
            0.5f,
            3f,
            new int[] {0, 0, 0, 0, 7, 1, 6, 0}),
    FLOUNDER(
            20,
            55,
            0.000012,
            FishStyle.BRUISER,
            Rarity.COMMON,
            5.0f,
            1.0f,
            6.5f,
            0.5f,
            3f,
            new int[] {1, 0, 0, 0, 6, 1, 5, 0}),
    SOLE(
            20,
            60,
            0.0000095,
            FishStyle.BRUISER,
            Rarity.UNCOMMON,
            4.0f,
            0.8f,
            8f,
            0.5f,
            2.5f,
            new int[] {0, 0, 0, 0, 5, 4, 1, 0}),
    TURBOT(
            30,
            100,
            0.000014,
            FishStyle.BRUISER,
            Rarity.RARE,
            6.5f,
            1.2f,
            7f,
            0.5f,
            3f,
            new int[] {0, 0, 0, 0, 3, 2, 2, 1}),
    HALIBUT(
            60,
            250,
            0.0000105,
            FishStyle.BRUISER,
            Rarity.EPIC,
            6.0f,
            1.4f,
            10f,
            0.8f,
            3.5f,
            new int[] {0, 0, 0, 0, 0, 0, 4, 3}),
    // andere Meeresfische
    REDFISH(
            25,
            90,
            0.000014,
            FishStyle.FIGHTER,
            Rarity.UNCOMMON,
            2.4f,
            4.4f,
            7.5f,
            1.2f,
            4f,
            new int[] {0, 0, 0, 0, 0, 0, 6, 5}),
    MONKFISH(
            30,
            180,
            0.000014,
            FishStyle.BRUISER,
            Rarity.EPIC,
            6.0f,
            2.4f,
            7f,
            0.5f,
            3.5f,
            new int[] {0, 0, 0, 0, 2, 1, 2, 4}),
    GURNARD(
            20,
            60,
            0.0000095,
            FishStyle.FIGHTER,
            Rarity.UNCOMMON,
            2.2f,
            3.2f,
            8f,
            1.6f,
            3.5f,
            new int[] {0, 0, 0, 0, 4, 4, 2, 1}),
    WOLFFISH(
            40,
            150,
            0.000009,
            FishStyle.BRUISER,
            Rarity.RARE,
            2.4f,
            3.6f,
            11f,
            1.6f,
            3.5f,
            new int[] {0, 0, 0, 0, 0, 0, 3, 2}),
    // der Besondere
    GOLDEN_KOI(
            15,
            75,
            0.000019,
            FishStyle.DARTER,
            Rarity.LEGENDARY,
            2.0f,
            3.5f,
            6.5f,
            1f,
            5f,
            new int[] {1, 1, 1, 0, 0, 0, 0, 0}),
    // unter der Erde: Wasser tief in Höhlen, ohne Blick zum Himmel (Gewichte: Höhle, Lava, End am
    // Ende)
    CAVE_TETRA(
            6,
            16,
            0.000012,
            FishStyle.DARTER,
            Rarity.COMMON,
            1.4f,
            2.6f,
            5.5f,
            1f,
            3f,
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 0}),
    OLM(
            18,
            32,
            0.0000045,
            FishStyle.DARTER,
            Rarity.UNCOMMON,
            1.0f,
            1.4f,
            11f,
            1.4f,
            3f,
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0}),
    AMETHYST_PERCH(
            15,
            45,
            0.000017,
            FishStyle.FIGHTER,
            Rarity.RARE,
            2.0f,
            4.4f,
            6.5f,
            1f,
            4f,
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0}),
    DRIPSTONE_EEL(
            40,
            95,
            0.0000016,
            FishStyle.DARTER,
            Rarity.RARE,
            1.2f,
            1.6f,
            13f,
            1f,
            3f,
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0}),
    GLOW_CARP(
            30,
            85,
            0.000024,
            FishStyle.BRUISER,
            Rarity.EPIC,
            3.0f,
            4.6f,
            6.5f,
            1f,
            4f,
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0}),
    // in Lava: nur mit der Netheritrute
    EMBER_CARP(
            25,
            75,
            0.000024,
            FishStyle.BRUISER,
            Rarity.COMMON,
            3.0f,
            4.6f,
            6.5f,
            1f,
            4f,
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0}),
    MAGMA_EEL(
            40,
            110,
            0.0000016,
            FishStyle.DARTER,
            Rarity.COMMON,
            1.2f,
            1.6f,
            13f,
            1f,
            3f,
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0}),
    BLAZE_PIKE(
            35,
            120,
            0.000008,
            FishStyle.FIGHTER,
            Rarity.UNCOMMON,
            1.6f,
            2.8f,
            10f,
            2.5f,
            4f,
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0}),
    BASALT_CATFISH(
            60,
            190,
            0.000009,
            FishStyle.BRUISER,
            Rarity.RARE,
            3.0f,
            2.8f,
            9f,
            1f,
            4f,
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0}),
    OBSIDIAN_STURGEON(
            90,
            220,
            0.0000045,
            FishStyle.FIGHTER,
            Rarity.EPIC,
            2.4f,
            2.4f,
            13f,
            3f,
            5f,
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0}),
    // das Wasser des End
    CHORUS_FISH(
            15,
            40,
            0.000016,
            FishStyle.DARTER,
            Rarity.COMMON,
            1.6f,
            3.6f,
            6f,
            1f,
            3.5f,
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12}),
    ENDER_EEL(
            40,
            110,
            0.0000016,
            FishStyle.DARTER,
            Rarity.UNCOMMON,
            1.2f,
            1.6f,
            13f,
            1f,
            3f,
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7}),
    VOID_RAY(
            50,
            160,
            0.000012,
            FishStyle.BRUISER,
            Rarity.RARE,
            6.5f,
            1.2f,
            7f,
            0.5f,
            3f,
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4}),
    SHULKER_PUFFER(
            20,
            50,
            0.00003,
            FishStyle.BRUISER,
            Rarity.EPIC,
            3.6f,
            4.0f,
            5f,
            0.8f,
            2.5f,
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2}),
    // die Bosse: Sie spawnen nie von selbst und nehmen nur ihren eigenen Köder (siehe
    // BossEncounters)
    ANCIENT_CATFISH(
            240,
            320,
            0.0000075,
            FishStyle.BRUISER,
            Rarity.LEGENDARY,
            3.4f,
            3.0f,
            10f,
            1f,
            4.5f,
            new int[] {0, 0, 0, 0, 0, 0, 0, 0}),
    REED_KING(
            130,
            170,
            0.0000085,
            FishStyle.FIGHTER,
            Rarity.LEGENDARY,
            1.8f,
            3.0f,
            11f,
            2.8f,
            4.5f,
            new int[] {0, 0, 0, 0, 0, 0, 0, 0}),
    BLACK_STURGEON(
            250,
            350,
            0.0000045,
            FishStyle.FIGHTER,
            Rarity.LEGENDARY,
            2.8f,
            2.8f,
            14f,
            3.4f,
            5.5f,
            new int[] {0, 0, 0, 0, 0, 0, 0, 0}),
    DEEP_WARDEN(
            150,
            220,
            0.000012,
            FishStyle.BRUISER,
            Rarity.MYTHIC,
            4.0f,
            4.6f,
            9f,
            2.0f,
            5f,
            new int[] {0, 0, 0, 0, 0, 0, 0, 0});

    /** Gewöhnlich, Ungewöhnlich, Selten, Episch, Legendär, Mythisch. */
    public enum Rarity {
        COMMON,
        UNCOMMON,
        RARE,
        EPIC,
        LEGENDARY,
        MYTHIC
    }

    /** Was ein Fisch frisst; entscheidet, welche Köder er nimmt. */
    public enum Diet {
        /** Friedfische: Würmer, Maden, Mais, Brot, Boilies. */
        COARSE(14),
        /** Raubfische im Süßwasser: Köderfische und Kunstköder. */
        PREDATOR(18),
        /** Forellen und ihre Verwandten: Insekten, Fliegen, kleine Spinner. */
        SALMONID(6),
        /** Raubfische im Meer: Köderfische, Fetzen, Kunstköder. */
        SEA_PREDATOR(10),
        /** Fische des Meeresgrunds: Garnelen, Fetzen, Würmer. */
        SEA_BOTTOM(14),
        /** Schwarmfische der offenen See: winzige Köder. */
        SHOAL(6);

        private final int strikeIdeal;

        Diet(int strikeIdeal) {
            this.strikeIdeal = strikeIdeal;
        }

        /**
         * Ticks nach dem Biss, bei denen der Anschlag den Haken am besten setzt: Raubfische drehen
         * den Köder erst, Forellen spucken schnell aus.
         */
        public int strikeIdeal() {
            return strikeIdeal;
        }

        /** Jagt andere Fische. */
        public boolean predatory() {
            return this == PREDATOR || this == SEA_PREDATOR;
        }
    }

    private final double minLength, maxLength, weightFactor;
    private final FishStyle style;
    private final Rarity rarity;
    private final float width, height, body, nose, tail;
    private final int[] habitatWeights;

    FishSpecies(
            double minLength,
            double maxLength,
            double weightFactor,
            FishStyle style,
            Rarity rarity,
            float width,
            float height,
            float body,
            float nose,
            float tail,
            int[] habitatWeights) {
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.weightFactor = weightFactor;
        this.style = style;
        this.rarity = rarity;
        this.width = width;
        this.height = height;
        this.body = body;
        this.nose = nose;
        this.tail = tail;
        // Kürzere Listen lassen die Lebensräume am Ende (Höhle, Lava, End) leer.
        if (habitatWeights.length > Habitat.values().length)
            throw new IllegalArgumentException(name());
        this.habitatWeights = java.util.Arrays.copyOf(habitatWeights, Habitat.values().length);
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "entity.fishr." + id();
    }

    public double minLength() {
        return minLength;
    }

    public double maxLength() {
        return maxLength;
    }

    public FishStyle style() {
        return style;
    }

    public Rarity rarity() {
        return rarity;
    }

    public float modelWidth() {
        return width;
    }

    public float modelHeight() {
        return height;
    }

    public float modelBodyLength() {
        return body;
    }

    public float modelNoseLength() {
        return nose;
    }

    public float modelTailLength() {
        return tail;
    }

    /** Barteln am Maul: der Wels, die Barbe und die Kinnbartel der Dorschartigen. */
    public boolean hasBarbels() {
        return switch (this) {
            case CATFISH,
                    BARBEL,
                    ATLANTIC_COD,
                    HADDOCK,
                    STURGEON,
                    ANCIENT_CATFISH,
                    BLACK_STURGEON,
                    BASALT_CATFISH,
                    OBSIDIAN_STURGEON ->
                    true;
            default -> false;
        };
    }

    /** Plattfische liegen auf der Seite: ein breiter, flacher Körper. */
    public boolean flat() {
        return width > height * 2;
    }

    public double modelLengthCm() {
        return (modelBodyLength() + 3 + modelNoseLength() + modelTailLength()) * 100.0 / 16;
    }

    public String rarityKey() {
        return "rarity.fishr." + rarity.name().toLowerCase(Locale.ROOT);
    }

    /** Spawn- und Bissgewicht in einem Lebensraum; 0, wo der Fisch nicht lebt. */
    public int weight(Habitat habitat) {
        return habitatWeights[habitat.ordinal()];
    }

    /** Schwarmfische schwimmen in Gruppen, die großen Raubfische und die seltenen allein. */
    public int maxGroup() {
        if (rarity.ordinal() >= Rarity.RARE.ordinal() || this == ZANDER || this == EEL || flat())
            return 1;
        return switch (this) {
            case HERRING, SARDINE, ANCHOVY, SPRAT, MACKEREL -> 8;
            case ROACH, RUDD, BREAM -> 6;
            default -> 4;
        };
    }

    public FishSpecimen roll(Random random) {
        return roll(random, 0);
    }

    /**
     * Ein zufälliges Exemplar; {@code sizeBias} 0..1 bevorzugt größere Fische (große Köder fangen
     * große Fische).
     */
    public FishSpecimen roll(Random random, double sizeBias) {
        double fraction = Math.pow(random.nextDouble(), 2 - Math.clamp(sizeBias, 0, 1));
        double length = Math.round((minLength + (maxLength - minLength) * fraction) * 10) / 10.0;
        double weight =
                Math.max(
                        0.01,
                        Math.round(
                                        weightFactor
                                                * Math.pow(length, 3)
                                                * (0.88 + random.nextDouble() * 0.24)
                                                * 100)
                                / 100.0);
        return new FishSpecimen(this, length, weight);
    }

    public Diet diet() {
        return switch (this) {
            case CARP,
                    CRUCIAN_CARP,
                    BARBEL,
                    ROACH,
                    RUDD,
                    BREAM,
                    TENCH,
                    CHUB,
                    GOLDEN_KOI,
                    STURGEON,
                    BLACK_STURGEON,
                    CAVE_TETRA,
                    OLM,
                    GLOW_CARP,
                    EMBER_CARP,
                    OBSIDIAN_STURGEON,
                    CHORUS_FISH,
                    SHULKER_PUFFER ->
                    Diet.COARSE;
            case PIKE,
                    ZANDER,
                    PERCH,
                    CATFISH,
                    EEL,
                    ASP,
                    ANCIENT_CATFISH,
                    REED_KING,
                    AMETHYST_PERCH,
                    DRIPSTONE_EEL,
                    MAGMA_EEL,
                    BLAZE_PIKE,
                    BASALT_CATFISH,
                    ENDER_EEL ->
                    Diet.PREDATOR;
            case RIVER_TROUT, RAINBOW_TROUT, GRAYLING, SALMON, SEA_TROUT -> Diet.SALMONID;
            case HADDOCK,
                    GILTHEAD_BREAM,
                    RED_MULLET,
                    PLAICE,
                    FLOUNDER,
                    SOLE,
                    TURBOT,
                    HALIBUT,
                    MONKFISH,
                    GURNARD,
                    VOID_RAY ->
                    Diet.SEA_BOTTOM;
            case HERRING, SARDINE, ANCHOVY, SPRAT -> Diet.SHOAL;
            default -> Diet.SEA_PREDATOR;
        };
    }

    /** Der Boss hinter dieser Art oder null. */
    public Boss boss() {
        return switch (this) {
            case ANCIENT_CATFISH -> Boss.ANCIENT_CATFISH;
            case REED_KING -> Boss.REED_KING;
            case BLACK_STURGEON -> Boss.BLACK_STURGEON;
            case DEEP_WARDEN -> Boss.DEEP_WARDEN;
            default -> null;
        };
    }

    public static FishSpecies of(Boss boss) {
        for (FishSpecies species : values()) if (species.boss() == boss) return species;
        throw new IllegalArgumentException(boss.name());
    }

    /** Lebt nur im Meer. */
    public boolean salt() {
        for (int i = 0; i < Habitat.OCEAN.ordinal(); i++) if (habitatWeights[i] > 0) return false;
        boolean sea = false;
        for (int i = Habitat.OCEAN.ordinal(); i <= Habitat.DEEP_OCEAN.ordinal(); i++)
            sea |= habitatWeights[i] > 0;
        return sea && boss() == null;
    }

    /** Jeder Lebensraum, in dem dieser Fisch lebt. */
    public java.util.List<Habitat> habitats() {
        java.util.List<Habitat> habitats = new java.util.ArrayList<>();
        for (Habitat habitat : Habitat.values()) if (weight(habitat) > 0) habitats.add(habitat);
        return habitats;
    }

    /**
     * Wann er am besten frisst, für Fundzettel und Lexikon: Dämmerung, Nacht, Tag oder jederzeit
     * (siehe {@link #activity}).
     */
    public String feedingTime() {
        if (weight(Habitat.CAVE) > 0 || lava() || weight(Habitat.END) > 0) return "always";
        if (this == CATFISH || this == EEL || this == ZANDER) return "night";
        return switch (diet()) {
            case PREDATOR, SALMONID -> "twilight";
            case COARSE -> "day";
            default -> "always";
        };
    }

    /** Lebt in Lava und nirgends sonst. */
    public boolean lava() {
        return habitatWeights[Habitat.LAVA.ordinal()] > 0;
    }

    /**
     * Wo in der Wassersäule der Fisch gern ist: -1 am Grund, 0 in der Mitte, 1 knapp unter der
     * Oberfläche.
     */
    public int preferredDepth() {
        return switch (this) {
            case HERRING, SARDINE, ANCHOVY, SPRAT, ASP, MACKEREL, RUDD, GOLDEN_KOI, SKIPJACK -> 1;
            case CARP,
                    CRUCIAN_CARP,
                    TENCH,
                    BARBEL,
                    CATFISH,
                    EEL,
                    BREAM,
                    STURGEON,
                    ATLANTIC_COD,
                    HADDOCK,
                    PLAICE,
                    FLOUNDER,
                    SOLE,
                    TURBOT,
                    HALIBUT,
                    MONKFISH,
                    GURNARD,
                    WOLFFISH,
                    RED_MULLET,
                    ZANDER ->
                    -1;
            default -> 0;
        };
    }

    /** Scharfe Zähne: Ohne Stahlvorfach können sie die Schnur durchbeißen. */
    public boolean teeth() {
        return switch (this) {
            case PIKE, ZANDER, BARRACUDA, WOLFFISH, MONKFISH, REED_KING, DEEP_WARDEN, BLAZE_PIKE ->
                    true;
            default -> false;
        };
    }

    /** Scheue Fische bemerken ein sichtbares Vorfach und einen großen Haken. */
    public boolean shy() {
        return diet() == Diet.SALMONID
                || this == CARP
                || this == TENCH
                || this == GRAYLING
                || this == SEA_BASS;
    }

    private boolean jumper() {
        return switch (this) {
            case RIVER_TROUT,
                    RAINBOW_TROUT,
                    GRAYLING,
                    SALMON,
                    SEA_TROUT,
                    PIKE,
                    ASP,
                    BARRACUDA,
                    SWORDFISH,
                    SEA_BASS,
                    MACKEREL,
                    SKIPJACK,
                    BLAZE_PIKE,
                    CHORUS_FISH ->
                    true;
            default -> false;
        };
    }

    private boolean diver() {
        return switch (this) {
            case CARP,
                    CRUCIAN_CARP,
                    TENCH,
                    CATFISH,
                    BARBEL,
                    BREAM,
                    ATLANTIC_COD,
                    SAITHE,
                    HADDOCK,
                    GROUPER,
                    HALIBUT,
                    PLAICE,
                    FLOUNDER,
                    SOLE,
                    TURBOT,
                    MONKFISH,
                    WOLFFISH,
                    REDFISH,
                    STURGEON,
                    TUNA,
                    BASALT_CATFISH,
                    OBSIDIAN_STURGEON,
                    VOID_RAY,
                    GLOW_CARP ->
                    true;
            default -> false;
        };
    }

    private boolean snagger() {
        return switch (this) {
            case PIKE,
                    ZANDER,
                    EEL,
                    TENCH,
                    CARP,
                    CATFISH,
                    GROUPER,
                    WOLFFISH,
                    CHUB,
                    PERCH,
                    BARBEL,
                    DRIPSTONE_EEL,
                    AMETHYST_PERCH,
                    MAGMA_EEL,
                    ENDER_EEL ->
                    true;
            default -> false;
        };
    }

    private boolean shaker() {
        return switch (this) {
            case PIKE,
                    ZANDER,
                    CATFISH,
                    BARRACUDA,
                    WOLFFISH,
                    EEL,
                    PERCH,
                    SEA_BASS,
                    SALMON,
                    HAKE,
                    MONKFISH ->
                    true;
            default -> false;
        };
    }

    /**
     * Wie oft dieser Fisch am Haken flüchtet, abtaucht, in Deckung zieht, den Kopf schüttelt oder
     * springt.
     */
    public int[] actionWeights() {
        return Fighter.weights(
                3,
                style == FishStyle.BRUISER ? 3 : 4,
                diver() ? 4 : 1,
                snagger() ? 4 : 1,
                shaker() ? 4 : 1,
                jumper() ? 3 : 0);
    }

    /**
     * Wie gern dieser Fisch zu dieser Tageszeit frisst: Raubfische jagen in der Morgen- und
     * Abenddämmerung, Wels, Aal und Zander nachts, Friedfische tagsüber, Forellen in der Dämmerung.
     * Regen belebt das Süßwasser.
     */
    public double activity(long dayTime, boolean raining) {
        long time = Math.floorMod(dayTime, 24000L);
        boolean night = time >= 13000 && time < 23000;
        boolean twilight = time >= 22500 || time < 1500 || (time >= 11500 && time < 13500);
        double activity =
                switch (diet()) {
                    case PREDATOR -> twilight ? 1.5 : night ? 1.0 : 0.8;
                    case COARSE -> night ? 0.7 : 1.15;
                    case SALMONID -> twilight ? 1.4 : night ? 0.5 : 1.0;
                    default -> 1.0;
                };
        if (night && (this == CATFISH || this == EEL || this == ZANDER)) activity = 1.8;
        // Weder Tag noch Regen erreichen Höhlen, Lava oder das End.
        if (weight(Habitat.CAVE) > 0 || lava() || weight(Habitat.END) > 0) return 1.0;
        if (raining && !salt()) activity *= 1.2;
        return activity;
    }

    /**
     * Welcher Fisch in diesem Lebensraum einen Köder nimmt, gewichtet danach, wie häufig er dort
     * ist.
     */
    public static FishSpecies choose(Habitat habitat, Random random) {
        return choose(habitat, random, species -> 1);
    }

    /**
     * Wie {@link #choose(Habitat, Random)}, jedes Gewicht mit einem Faktor multipliziert (Köder,
     * Tageszeit, Wetter).
     */
    public static FishSpecies choose(
            Habitat habitat, Random random, ToDoubleFunction<FishSpecies> factor) {
        double total = 0;
        for (FishSpecies species : values())
            total += species.weight(habitat) * Math.max(0, factor.applyAsDouble(species));
        if (total <= 0)
            return habitat == Habitat.RIVER ? PERCH : choose(Habitat.RIVER, random, factor);
        double choice = random.nextDouble() * total;
        for (FishSpecies species : values()) {
            choice -= species.weight(habitat) * Math.max(0, factor.applyAsDouble(species));
            if (choice < 0) return species;
        }
        return PERCH;
    }

    public static FishSpecies choose(boolean swamp, Random random) {
        return choose(swamp ? Habitat.SWAMP : Habitat.RIVER, random);
    }
}
