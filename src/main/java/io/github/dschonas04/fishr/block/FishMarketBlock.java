package io.github.dschonas04.fishr.block;

import io.github.dschonas04.fishr.fish.FishCatch;
import io.github.dschonas04.fishr.progress.Progression;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Fischmarkt (Anforderung 20): verkauft Fische für Smaragde. Seltenheit, Gewicht, Kontrolle im
 * Drill und Frische bestimmen den Preis; dieselbe Art am selben Tag bringt weniger. Schleichend
 * klicken verkauft alle Fische im Inventar.
 */
public final class FishMarketBlock extends Block {

    public FishMarketBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (FishCatch.read(stack) == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;
        int sold = 0, emeralds = 0;
        if (player.isShiftKeyDown()) {
            for (ItemStack fish : player.getInventory().getNonEquipmentItems()) {
                FishCatch.Catch caught = FishCatch.read(fish);
                if (caught == null) continue;
                emeralds +=
                        Progression.sell(
                                serverPlayer,
                                caught.specimen(),
                                caught.quality(),
                                caught.condition());
                fish.shrink(1);
                sold++;
            }
        } else {
            FishCatch.Catch caught = FishCatch.read(stack);
            emeralds +=
                    Progression.sell(
                            serverPlayer, caught.specimen(), caught.quality(), caught.condition());
            stack.shrink(1);
            sold++;
        }
        level.playSound(null, pos, SoundEvents.VILLAGER_TRADE, SoundSource.BLOCKS, 0.8f, 1.0f);
        serverPlayer.sendSystemMessage(
                Component.translatable(
                                "market.fishr.sold",
                                sold,
                                emeralds,
                                String.format(
                                        java.util.Locale.GERMAN,
                                        "%.2f",
                                        Progression.data(serverPlayer).getDoubleOr("credit", 0)))
                        .withStyle(ChatFormatting.GREEN),
                true);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer)
            serverPlayer.sendSystemMessage(Component.translatable("market.fishr.help"));
        return InteractionResult.SUCCESS;
    }
}
