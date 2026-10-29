// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr.block;

import io.github.dschonas04.fishr.fish.FishCatch;
import io.github.dschonas04.fishr.fish.FishSpecies;
import io.github.dschonas04.fishr.item.FishrItems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Filetiertisch (nach der Fischputzstation aus You Need Bait): Fisch in die Haupthand,
 * Filetiermesser in die andere. Ein Fisch gibt Filets nach seinem Gewicht, ab und zu eine Gräte
 * (wie in Fish of Thieves), und jeder zehnte hat etwas im Magen.
 */
public final class FilletTableBlock extends Block {
    /** Chance auf einen Fund im Magen. */
    static final double FIND = 0.1;

    static final double BONE = 0.2;

    public FilletTableBlock(Properties properties) {
        super(properties);
    }

    /**
     * Filets aus einem Fisch dieses Gewichts: eines bei einem kleinen Fisch, bis zu zwölf bei einem
     * Riesen.
     */
    public static int fillets(double weightKg) {
        return (int) Math.clamp(1 + Math.floor(Math.sqrt(Math.max(0, weightKg)) * 1.6), 1, 12);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack fish,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        FishCatch.Catch caught = FishCatch.read(fish);
        if (hand != InteractionHand.MAIN_HAND
                || (caught == null && !fish.is(Items.COD) && !fish.is(Items.SALMON)))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;
        ItemStack knife = player.getOffhandItem();
        if (!knife.is(FishrItems.FILLET_KNIFE)) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.fishr.fillet_help")
                            .withStyle(ChatFormatting.GRAY),
                    true);
            return InteractionResult.SUCCESS;
        }
        if (caught != null && caught.specimen().species().boss() != null) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.fishr.fillet_boss")
                            .withStyle(ChatFormatting.GOLD),
                    true);
            return InteractionResult.SUCCESS;
        }
        RandomSource random = level.getRandom();
        double weight = caught != null ? caught.specimen().weightKg() : 0.8;
        List<ItemStack> out = new ArrayList<>();
        out.add(new ItemStack(FishrItems.RAW_FILLET, fillets(weight)));
        if (random.nextDouble() < BONE)
            out.add(new ItemStack(FishrItems.FISH_BONE, 1 + random.nextInt(2)));
        if (random.nextDouble() < FIND)
            out.add(find(caught == null ? null : caught.specimen().species(), random));
        fish.shrink(1);
        knife.hurtAndBreak(1, player, InteractionHand.OFF_HAND);
        for (ItemStack stack : out)
            player.getInventory().placeItemBackInInventory(stack, Prediction.SERVER_ONLY);
        level.playSound(null, pos, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.BLOCKS, 0.8f, 1.2f);
        serverPlayer.sendSystemMessage(
                Component.translatable("message.fishr.filleted", out.getFirst().getCount()), true);
        return InteractionResult.SUCCESS;
    }

    /**
     * Was ein Fisch verschluckt hatte: Meeresfische anderes als Flussfische, seltene Fische ab und
     * zu Perlmutt.
     */
    private static ItemStack find(FishSpecies species, RandomSource random) {
        if (species != null
                && species.rarity().ordinal() >= FishSpecies.Rarity.RARE.ordinal()
                && random.nextDouble() < 0.15) {
            return new ItemStack(FishrItems.MOTHER_OF_PEARL);
        }
        Item[] finds =
                species != null && species.salt()
                        ? new Item[] {
                            Items.PRISMARINE_CRYSTALS,
                            Items.INK_SAC,
                            Items.GOLD_NUGGET,
                            Items.NAUTILUS_SHELL,
                            Items.KELP
                        }
                        : new Item[] {
                            Items.IRON_NUGGET,
                            Items.GOLD_NUGGET,
                            Items.CLAY_BALL,
                            Items.STRING,
                            FishrItems.WORM_BAIT
                        };
        Item item = finds[random.nextInt(finds.length)];
        return new ItemStack(item, item == Items.NAUTILUS_SHELL ? 1 : 1 + random.nextInt(3));
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer)
            serverPlayer.sendSystemMessage(Component.translatable("message.fishr.fillet_help"));
        return InteractionResult.SUCCESS;
    }
}
