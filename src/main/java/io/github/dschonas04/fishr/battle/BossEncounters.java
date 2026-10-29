package io.github.dschonas04.fishr.battle;

import io.github.dschonas04.fishr.fish.Habitat;
import io.github.dschonas04.fishr.item.BaitType;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Wann ein Boss beißt. Jeder Boss braucht seinen eigenen Köder, ausgeworfen in seinem Gewässer zu
 * seiner Zeit:
 *
 * <ul>
 *   <li>Der Alte vom Grund: nachts, in einem See oder Sumpf, mindestens fünf Blöcke tief.
 *   <li>Der Schilfkönig: in einem Sumpf bei Regen oder in der Dämmerung.
 *   <li>Der Schwarze Stör: tagsüber, in einem Fluss, mindestens vier Blöcke tief.
 *   <li>Der Tiefenwächter: in verborgenem Wasser, unter y 0 und ohne Blick zum Himmel.
 * </ul>
 *
 * Selbst dann ist nur etwa jeder dritte Biss der Boss, und jeder Angler trifft jeden Boss einmal am
 * Tag.
 */
public final class BossEncounters {
    private static final double CHANCE = 0.4;
    private final Map<UUID, Map<Boss, Long>> lastCaught = new HashMap<>();

    public Boss check(ServerLevel level, BlockPos hook, BaitType bait, UUID angler, Random random) {
        if (bait == null || bait.boss() == null || !conditions(level, hook, bait.boss()))
            return null;
        Long day = lastCaught.getOrDefault(angler, Map.of()).get(bait.boss());
        if (day != null && day == day(level)) return null;
        return random.nextDouble() < CHANCE ? bait.boss() : null;
    }

    public void caught(ServerLevel level, UUID angler, Boss boss) {
        lastCaught.computeIfAbsent(angler, key -> new EnumMap<>(Boss.class)).put(boss, day(level));
    }

    public static boolean conditions(ServerLevel level, BlockPos hook, Boss boss) {
        long time = Math.floorMod(level.getOverworldClockTime(), 24000L);
        boolean night = time >= 13000 && time < 23000;
        boolean twilight = time >= 22500 || time < 1500 || (time >= 11500 && time < 13500);
        Habitat habitat = Habitat.at(level, hook);
        return switch (boss) {
            case ANCIENT_CATFISH ->
                    night
                            && (habitat == Habitat.LAKE || habitat == Habitat.SWAMP)
                            && depth(level, hook) >= 5;
            case REED_KING -> habitat == Habitat.SWAMP && (level.isRaining() || twilight);
            case BLACK_STURGEON ->
                    !night
                            && (habitat == Habitat.RIVER || habitat == Habitat.COLD)
                            && depth(level, hook) >= 4;
            case DEEP_WARDEN -> hook.getY() < 0 && !level.canSeeSkyFromBelowWater(hook);
        };
    }

    private static int depth(ServerLevel level, BlockPos hook) {
        int depth = 0;
        BlockPos pos = hook;
        while (depth < 16 && level.getFluidState(pos).is(FluidTags.WATER)) {
            depth++;
            pos = pos.below();
        }
        return depth;
    }

    private static long day(ServerLevel level) {
        return level.getOverworldClockTime() / 24000;
    }
}
