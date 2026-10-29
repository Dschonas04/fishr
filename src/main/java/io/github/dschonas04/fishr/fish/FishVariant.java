package io.github.dschonas04.fishr.fish;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Locale;
import java.util.Random;

/**
 * The colouring of one fish (after Fish of Thieves): most fish look like their species, but sun,
 * moon, thunder, darkness and chance bring out rare colourings. A rare colouring is worth more at
 * the market and in experience, and the lexicon keeps track of the colourings each angler has
 * caught.
 */
public enum FishVariant {
    /** As the species looks. */
    NORMAL(0xFFFFFFFF, false, 1.0),
    /** By day under open sky. */
    SUNLIT(0xFFFFC85A, true, 1.5),
    /** By night under open sky, more often at full moon. */
    MOONLIT(0xFF9DB4FF, true, 1.5),
    /** In a thunderstorm. */
    STORM(0xFF7C8AA6, false, 2.0),
    /** Deep down, in caves, lava and the End. */
    SHADOW(0xFF5E5872, false, 1.8),
    /** Rare everywhere: without any colour. */
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

    /** ARGB multiplied over the skin. */
    public int tint() {
        return tint;
    }

    /** Drawn over the pale skin instead of the species' own one. */
    public boolean pale() {
        return pale;
    }

    /** Multiplier on market price and experience. */
    public double worth() {
        return worth;
    }

    public static FishVariant byId(String id) {
        for (FishVariant variant : values()) if (variant.id().equals(id)) return variant;
        return NORMAL;
    }

    /** Where and when a fish is hooked or spawns; decides which colourings are possible. */
    public record Conditions(
            boolean sky, boolean day, boolean thunder, boolean fullMoon, boolean underworld) {}

    /** A colouring for a fish at this place and time. */
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
     * Nothing but water above: by the height map, so it holds for water the light has not reached
     * yet.
     */
    static boolean openSky(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos top = pos.mutable();
        while (top.getY() < level.getMaxY() && !level.getFluidState(top).isEmpty())
            top.move(0, 1, 0);
        return top.getY()
                >= level.getHeight(Heightmap.Types.MOTION_BLOCKING, top.getX(), top.getZ());
    }

    /** A colouring for a fish under these conditions. */
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
