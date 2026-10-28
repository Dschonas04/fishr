package io.github.dschonas04.fishr.item;

import io.github.dschonas04.fishr.fish.Habitat;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Bait is found while digging (after Fish of Thieves and You Need Bait): earthworms in dry earth (twice as often in
 * the rain), maggots in dry sand, leeches in wet sand and mud by rivers, beaches and swamps, caterpillars in leaves.
 */
public final class BaitFinds {
    static final double WORM = 0.04, GRUB = 0.03, LEECH = 0.04, CATERPILLAR = 0.03;

    private BaitFinds() { }

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (!(level instanceof ServerLevel server) || player.hasInfiniteMaterials()) return;
            Item found = find(server, pos, state, server.getRandom().nextDouble());
            if (found != null) Block.popResource(server, pos, new ItemStack(found));
        });
    }

    /** What a broken block gives away, or null. */
    static Item find(ServerLevel level, BlockPos pos, BlockState state, double roll) {
        boolean wet = wet(level, pos);
        if (earth(state)) {
            if (wet) return null;
            return roll < WORM * (level.isRainingAt(pos.above()) ? 2 : 1) ? FishrItems.WORM_BAIT : null;
        }
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            if (!wet) return roll < GRUB ? FishrItems.GRUB_BAIT : null;
            return roll < LEECH && leechWater(level, pos) ? FishrItems.LEECH_BAIT : null;
        }
        if (state.is(Blocks.MUD) || state.is(Blocks.MUDDY_MANGROVE_ROOTS) || state.is(Blocks.CLAY)) {
            return roll < LEECH && (wet || Habitat.of(level.getBiome(pos)) == Habitat.SWAMP) ? FishrItems.LEECH_BAIT : null;
        }
        if (state.is(BlockTags.LEAVES)) return roll < CATERPILLAR ? FishrItems.CATERPILLAR_BAIT : null;
        return null;
    }

    private static boolean earth(BlockState state) {
        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM) || state.is(Blocks.FARMLAND);
    }

    /** Water next to the block or right above it. */
    private static boolean wet(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (direction != Direction.DOWN && level.getFluidState(pos.relative(direction)).is(FluidTags.WATER)) return true;
        }
        return false;
    }

    /** Leeches live by rivers, at the beach and in swamps. */
    private static boolean leechWater(ServerLevel level, BlockPos pos) {
        Habitat habitat = Habitat.of(level.getBiome(pos));
        return habitat == Habitat.RIVER || habitat == Habitat.SWAMP || habitat == Habitat.OCEAN || habitat == Habitat.LAKE;
    }
}
