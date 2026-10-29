package io.github.dschonas04.fishr.block;

import net.minecraft.util.Prediction;

import io.github.dschonas04.fishr.fish.Habitat;
import io.github.dschonas04.fishr.item.FishrItems;
import io.github.dschonas04.fishr.item.BaitType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** What a bait trap has caught; every minute or so something may swim in, up to {@link #CAPACITY}. */
public final class BaitTrapBlockEntity extends BlockEntity {
    public static final int CAPACITY = 16;
    /** Ticks between two looks into the trap. */
    private static final int INTERVAL = 1200;
    private final List<ItemStack> contents = new ArrayList<>();

    public BaitTrapBlockEntity(BlockPos pos, BlockState state) { super(FishrBlocks.BAIT_TRAP_ENTITY, pos, state); }

    static void tick(Level level, BlockPos pos, BlockState state, BaitTrapBlockEntity trap) {
        if (!(level instanceof ServerLevel server) || Math.floorMod(level.getGameTime() + pos.asLong(), INTERVAL) != 0) return;
        if (!state.getValue(BaitTrapBlock.WATERLOGGED) || trap.count() >= CAPACITY || server.getRandom().nextFloat() > 0.6f) return;
        Item catchItem = trap.lure(Habitat.at(server, pos), server.getRandom().nextFloat());
        if (catchItem == null) return;
        trap.add(new ItemStack(catchItem));
        trap.setChanged();
    }

    /** Baitfish everywhere in the water of the overworld; leeches in fresh water, most of all in rivers and swamps. */
    private Item lure(Habitat habitat, float roll) {
        double leeches = switch (habitat) {
            case RIVER, SWAMP -> 0.4;
            case LAKE, COLD -> 0.2;
            default -> 0;
        };
        if (habitat.underworld()) return null;
        return FishrItems.item(roll < leeches ? BaitType.LEECH : BaitType.BAITFISH);
    }

    private int count() { return contents.stream().mapToInt(ItemStack::getCount).sum(); }

    private void add(ItemStack stack) {
        for (ItemStack stored : contents) {
            if (ItemStack.isSameItemSameComponents(stored, stack) && stored.getCount() < stored.getMaxStackSize()) {
                stored.grow(stack.getCount());
                return;
            }
        }
        contents.add(stack);
    }

    void empty(ServerPlayer player) {
        if (contents.isEmpty()) {
            player.sendOverlayMessage(Component.translatable(getBlockState().getValue(BaitTrapBlock.WATERLOGGED)
                    ? "message.fishr.bait_trap_empty" : "message.fishr.bait_trap_dry"));
            return;
        }
        for (ItemStack stack : contents) player.getInventory().placeItemBackInInventory(stack, Prediction.SERVER_ONLY);
        contents.clear();
        level.playSound(null, worldPosition, SoundEvents.FISH_SWIM, SoundSource.BLOCKS, 0.7f, 1.2f);
        setChanged();
    }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) for (ItemStack stack : contents) Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        contents.clear();
        super.preRemoveSideEffects(pos, state);
    }

    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Catch", ItemStack.CODEC.listOf(), List.copyOf(contents));
    }

    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        contents.clear();
        input.read("Catch", ItemStack.CODEC.listOf()).ifPresent(list -> list.forEach(stack -> contents.add(stack.copy())));
    }
}
