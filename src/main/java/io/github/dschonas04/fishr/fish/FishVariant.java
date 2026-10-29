package io.github.dschonas04.fishr.fish;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Locale;
import java.util.Random;

/**
 * Die Färbung eines Fisches (nach Fish of Thieves): Die meisten Fische sehen aus wie ihre Art, aber
 * Sonne, Mond, Gewitter, Dunkelheit und Zufall bringen seltene Färbungen hervor. Eine seltene
 * Färbung bringt am Markt und an Erfahrung mehr, und das Lexikon führt Buch über die Färbungen, die
 * jeder Angler gefangen hat.
 */
public enum FishVariant {
    /** So, wie die Art aussieht. */
    NORMAL(0xFFFFFFFF, false, 1.0),
    /** Tagsüber unter freiem Himmel. */
    SUNLIT(0xFFFFC85A, true, 1.5),
    /** Nachts unter freiem Himmel, bei Vollmond öfter. */
    MOONLIT(0xFF9DB4FF, true, 1.5),
    /** Bei Gewitter. */
    STORM(0xFF7C8AA6, false, 2.0),
    /** Tief unten, in Höhlen, Lava und dem End. */
    SHADOW(0xFF5E5872, false, 1.8),
    /** Überall selten: ganz ohne Farbe. */
    ALBINO(0xFFFFFFFF, true, 3.0);

    private final int tint;
    private final boolean pale;
    private final double worth;

    FishVariant(int tint, boolean pale, double worth) {
        this.tint = tint;
        this.pale = pale;
        this.worth = worth;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "variant.fishr." + id();
    }

    /** ARGB, über die Haut multipliziert. */
    public int tint() {
        return tint;
    }

    /** Über die helle Haut gezeichnet statt über die eigene der Art. */
    public boolean pale() {
        return pale;
    }

    /** Faktor auf Marktpreis und Erfahrung. */
    public double worth() {
        return worth;
    }

    public static FishVariant byId(String id) {
        for (FishVariant variant : values()) if (variant.id().equals(id)) return variant;
        return NORMAL;
    }

    /**
     * Wo und wann ein Fisch gehakt wird oder spawnt; entscheidet, welche Färbungen möglich sind.
     */
    public record Conditions(
            boolean sky, boolean day, boolean thunder, boolean fullMoon, boolean underworld) {}

    /** Eine Färbung für einen Fisch an diesem Ort zu dieser Zeit. */
    public static FishVariant at(ServerLevel level, BlockPos pos, Random random) {
        return pick(
                new Conditions(
                        openSky(level, pos),
                        level.isBrightOutside(),
                        level.isThundering(),
                        level.getMoonBrightness(pos) > 0.99f,
                        Habitat.at(level, pos).underworld()),
                random);
    }

    /**
     * Über ihm nur Wasser: nach der Höhenkarte, damit es auch für Wasser gilt, das das Licht noch
     * nicht erreicht hat.
     */
    static boolean openSky(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos top = pos.mutable();
        while (top.getY() < level.getMaxY() && !level.getFluidState(top).isEmpty())
            top.move(0, 1, 0);
        return top.getY()
                >= level.getHeight(Heightmap.Types.MOTION_BLOCKING, top.getX(), top.getZ());
    }

    /** Eine Färbung für einen Fisch unter diesen Bedingungen. */
    public static FishVariant pick(Conditions conditions, Random random) {
        double roll = random.nextDouble();
        if (roll < 0.02) return ALBINO;
        roll = random.nextDouble();
        if (conditions.underworld()) return roll < 0.2 ? SHADOW : NORMAL;
        if (conditions.thunder() && conditions.sky() && roll < 0.35) return STORM;
        if (!conditions.sky()) return roll < 0.1 ? SHADOW : NORMAL;
        if (conditions.day()) return roll < 0.1 ? SUNLIT : NORMAL;
        return roll < (conditions.fullMoon() ? 0.3 : 0.15) ? MOONLIT : NORMAL;
    }
}
