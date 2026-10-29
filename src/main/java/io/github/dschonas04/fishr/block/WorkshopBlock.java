// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr.block;

import io.github.dschonas04.fishr.item.FishrItems;
import io.github.dschonas04.fishr.item.RodMods;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Werkstatt (Anforderung 20): Rute in die Haupthand, Material in die andere. Smaragde reparieren
 * (je ein Viertel der Haltbarkeit), Perlmutt verstärkt den Blank (bis zu dreimal).
 */
public final class WorkshopBlock extends Block {

    public WorkshopBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack rod,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (!(rod.getItem() instanceof FishingRodItem) || hand != InteractionHand.MAIN_HAND)
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;
        ItemStack material = player.getOffhandItem();
        String message;
        if (material.is(Items.EMERALD)) {
            if (!rod.isDamaged()) message = "workshop.fishr.not_damaged";
            else {
                rod.setDamageValue(
                        Math.max(0, rod.getDamageValue() - Math.max(1, rod.getMaxDamage() / 4)));
                material.consume(1, player);
                message = "workshop.fishr.repaired";
            }
        } else if (material.is(FishrItems.MOTHER_OF_PEARL)) {
            if (!RodMods.upgrade(rod)) message = "workshop.fishr.max_upgrades";
            else {
                material.consume(1, player);
                message = "workshop.fishr.upgraded";
            }
        } else {
            message = "workshop.fishr.help";
        }
        boolean worked = message.endsWith("repaired") || message.endsWith("upgraded");
        if (worked)
            level.playSound(
                    null, pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.8f, 1.0f);
        serverPlayer.sendSystemMessage(
                Component.translatable(message, RodMods.upgrades(rod), RodMods.MAX_UPGRADES)
                        .withStyle(worked ? ChatFormatting.GREEN : ChatFormatting.GRAY),
                !message.endsWith("help"));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer)
            serverPlayer.sendSystemMessage(Component.translatable("workshop.fishr.help"));
        return InteractionResult.SUCCESS;
    }
}
