// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr.item;

import io.github.dschonas04.fishr.battle.Rig;
import io.github.dschonas04.fishr.fish.FishSpecies;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Macht aus Rute, Rolle, Schnur und Köder das {@link Rig}, mit dem der Drill läuft. */
public final class BattleEquipment {
    private BattleEquipment() {}

    /** Das Gerät, wie der Tooltip es zeigt. */
    public static Rig preview(ItemStack rod) {
        return rig(rod, FishrRodItem.bait(rod));
    }

    public static Rig rig(ItemStack rod, ItemStack bait) {
        RodType rodType = FishrItems.rod(rod);
        boolean fishr = rod.getItem() instanceof FishrRodItem;
        ReelType reel = fishr ? FishrItems.reel(FishrRodItem.get(rod, GearSlot.REEL)) : null;
        LineType line = fishr ? FishrItems.line(FishrRodItem.get(rod, GearSlot.LINE)) : null;
        if (line == null) line = LineType.STRING;
        double speed = reel == null ? Rig.VANILLA.reelSpeed() : reel.speed();
        double force = reel == null ? Rig.VANILLA.reelForce() : reel.force();
        double capacity = reel == null ? Rig.VANILLA.capacity() : reel.capacity();
        Rig rig =
                new Rig(
                        line.strength(),
                        line.stretch(),
                        rodType.flex(),
                        rodType.load(),
                        speed * rodType.speed(),
                        force,
                        capacity,
                        1,
                        1);
        BaitType baitType = FishrItems.bait(bait);
        if (baitType != null) rig = rig.scaled(baitType.strain(), baitType.drain(), baitType.run());
        return rig.upgraded(RodMods.upgrades(rod));
    }

    /** Feingefühl beim Anschlag: wie gut die Rute den Angler den Biss spüren lässt. */
    public static double sensitivity(ItemStack rod) {
        return rodType(rod).sensitivity();
    }

    /** Glück des Meeres (Vanilla) macht seltene Fische wahrscheinlicher. */
    public static int luck(ServerPlayer player, ItemStack rod) {
        return vanillaLevel(player, rod, "luck_of_the_sea");
    }

    /** Köder (Vanilla-Verzauberung) verkürzt die Wartezeit am Rutenhalter. */
    public static int lure(ServerPlayer player, ItemStack rod) {
        return vanillaLevel(player, rod, "lure");
    }

    /** Die Art der Rute, für Feingefühl und Wurf. */
    public static RodType rodType(ItemStack rod) {
        return FishrItems.rod(rod);
    }

    /** Wie weit die Pose fliegt. */
    public static double castFactor(ItemStack rod) {
        return FishrItems.rod(rod).cast();
    }

    public static boolean isBait(ItemStack stack) {
        return FishrItems.bait(stack) != null;
    }

    /**
     * Wie gern ein Fisch diesen Köder nimmt (ein blanker Haken: {@link BaitType#BARE_HOOK}); ein
     * Leuchtköder leuchtet nachts.
     */
    public static double appeal(ItemStack bait, ItemStack rod, FishSpecies species, boolean night) {
        BaitType type = FishrItems.bait(bait);
        if (type == null) return BaitType.BARE_HOOK;
        double appeal = type.appeal(species);
        if (type.lure() && bait.getMaxDamage() > 0)
            appeal *= 0.6 + 0.4 * (1 - bait.getDamageValue() / (double) bait.getMaxDamage());
        if (type == BaitType.GLOW && night) appeal *= 1.6;
        return appeal;
    }

    private static int vanillaLevel(ServerPlayer player, ItemStack rod, String name) {
        return player.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(
                        ResourceKey.create(
                                Registries.ENCHANTMENT,
                                net.minecraft.resources.Identifier.withDefaultNamespace(name)))
                .map(
                        holder ->
                                Math.clamp(
                                        EnchantmentHelper.getItemEnchantmentLevel(holder, rod),
                                        0,
                                        5))
                .orElse(0);
    }
}
