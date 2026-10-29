// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr.fish;

import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Wo ein Fisch lebt. Jedes Biom gehört zu genau einem der Oberflächen-Lebensräume: Der, in dem der
 * Haken landet, entscheidet, welche Fische beißen, und Fische spawnen in den Biomen der
 * Lebensräume, in denen sie leben. Die letzten drei sind keine Biome, sondern Orte: Wasser tief
 * unter der Erde, Lava (im Nether und in der Oberwelt) und das Wasser des End. Dort spawnen keine
 * Fische von selbst; sie beißen nur.
 */
public enum Habitat {
    RIVER,
    LAKE,
    SWAMP,
    COLD,
    OCEAN,
    WARM_OCEAN,
    COLD_OCEAN,
    DEEP_OCEAN,
    CAVE,
    LAVA,
    END;

    /** Die Lebensräume an der Oberfläche der Oberwelt, in Biomen. */
    public static final List<Habitat> SURFACE =
            List.of(RIVER, LAKE, SWAMP, COLD, OCEAN, WARM_OCEAN, COLD_OCEAN, DEEP_OCEAN);

    /** Wasser so weit unter dem Meeresspiegel und ohne Blick zum Himmel ist eine Höhle. */
    public static final int CAVE_BELOW = 50;

    public String translationKey() {
        return "habitat.fishr." + name().toLowerCase(Locale.ROOT);
    }

    /**
     * Höhle, Lava und End: keine natürlichen Spawns, der Fang geht direkt an den Angler (kein
     * Vanilla-Müll).
     */
    public boolean underworld() {
        return ordinal() >= CAVE.ordinal();
    }

    public static Habitat at(ServerLevel level, BlockPos pos) {
        if (level.getFluidState(pos).is(FluidTags.LAVA)) return LAVA;
        if (level.dimension() == Level.NETHER) return LAVA;
        if (level.dimension() == Level.END) return END;
        if (pos.getY() < CAVE_BELOW && !level.canSeeSkyFromBelowWater(pos)) return CAVE;
        return of(level.getBiome(pos));
    }

    public static Habitat of(Holder<Biome> biome) {
        if (biome.is(Biomes.WARM_OCEAN)
                || biome.is(Biomes.LUKEWARM_OCEAN)
                || biome.is(Biomes.DEEP_LUKEWARM_OCEAN)) return WARM_OCEAN;
        if (biome.is(Biomes.COLD_OCEAN)
                || biome.is(Biomes.DEEP_COLD_OCEAN)
                || biome.is(Biomes.FROZEN_OCEAN)
                || biome.is(Biomes.DEEP_FROZEN_OCEAN)) return COLD_OCEAN;
        if (biome.is(BiomeTags.IS_DEEP_OCEAN)) return DEEP_OCEAN;
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_BEACH)) return OCEAN;
        if (biome.is(Biomes.FROZEN_RIVER)) return COLD;
        if (biome.is(BiomeTags.IS_RIVER)) return RIVER;
        if (biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP)) return SWAMP;
        if (biome.is(BiomeTags.IS_TAIGA)
                || biome.is(BiomeTags.IS_MOUNTAIN)
                || biome.is(Biomes.SNOWY_PLAINS)
                || biome.is(Biomes.ICE_SPIKES)) return COLD;
        return LAKE;
    }

    /**
     * Die Oberwelt-Biome dieses Lebensraums, für die Spawn-Registrierung; dieselbe Aufteilung wie
     * {@link #of}.
     */
    public Predicate<BiomeSelectionContext> biomes() {
        return BiomeSelectors.foundInOverworld()
                .and(context -> of(context.getBiomeHolder()) == this);
    }
}
