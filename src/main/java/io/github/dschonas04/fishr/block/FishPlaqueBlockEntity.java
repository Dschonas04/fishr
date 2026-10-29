package io.github.dschonas04.fishr.block;

import io.github.dschonas04.fishr.fish.FishCatch;
import io.github.dschonas04.fishr.fish.FishVariant;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Locale;

/**
 * Der Fisch auf einer Fischtafel; wird an die Clients geschickt, die ihn zeichnen (siehe den
 * Renderer der Tafel).
 */
public final class FishPlaqueBlockEntity extends BlockEntity {
    private ItemStack fish = ItemStack.EMPTY;

    public FishPlaqueBlockEntity(BlockPos pos, BlockState state) {
        super(FishrBlocks.FISH_PLAQUE_ENTITY, pos, state);
    }

    public ItemStack fish() {
        return fish;
    }

    void hang(ServerPlayer player, ItemStack stack) {
        if (!fish.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("message.fishr.plaque_taken"));
            return;
        }
        fish = stack.copyWithCount(1);
        stack.shrink(1);
        level.playSound(
                null,
                worldPosition,
                SoundEvents.ITEM_FRAME_ADD_ITEM,
                SoundSource.BLOCKS,
                1.0f,
                1.0f);
        changed();
        describe(player);
    }

    void takeDown(ServerPlayer player) {
        if (fish.isEmpty()) return;
        player.getInventory().placeItemBackInInventory(fish, Prediction.SERVER_ONLY);
        fish = ItemStack.EMPTY;
        level.playSound(
                null,
                worldPosition,
                SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                SoundSource.BLOCKS,
                1.0f,
                1.0f);
        changed();
    }

    /** Die Karte unter dem Fisch: Art und Färbung, Größe und wer ihn wann gefangen hat. */
    void describe(ServerPlayer player) {
        FishCatch.Catch caught = FishCatch.read(fish);
        if (caught == null) {
            player.sendOverlayMessage(Component.translatable("message.fishr.plaque_empty"));
            return;
        }
        var specimen = caught.specimen();
        MutableComponent name =
                Component.translatable(specimen.species().translationKey())
                        .withStyle(FishCatch.colour(specimen.species().rarity()));
        if (specimen.variant() != FishVariant.NORMAL) {
            name.append(
                    Component.literal(" · ")
                            .append(Component.translatable(specimen.variant().translationKey()))
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        player.sendSystemMessage(
                Component.translatable(
                        "message.fishr.plaque_card",
                        name,
                        String.format(Locale.GERMAN, "%.1f", specimen.lengthCm()),
                        String.format(Locale.GERMAN, "%.2f", specimen.weightKg()),
                        caught.angler().isEmpty() ? "?" : caught.angler()));
    }

    private void changed() {
        setChanged();
        if (level != null)
            level.sendBlockUpdated(
                    worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !fish.isEmpty())
            Containers.dropItemStack(
                    level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, fish);
        fish = ItemStack.EMPTY;
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!fish.isEmpty()) output.store("Fish", ItemStack.CODEC, fish);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        fish = input.read("Fish", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }
}
