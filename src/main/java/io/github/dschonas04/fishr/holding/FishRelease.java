package io.github.dschonas04.fishr.holding;

import io.github.dschonas04.fishr.ecology.Ecology;
import io.github.dschonas04.fishr.fish.FishCatch;
import io.github.dschonas04.fishr.fish.FishrFishEntity;
import io.github.dschonas04.fishr.fish.Habitat;
import io.github.dschonas04.fishr.item.FishingTools;
import io.github.dschonas04.fishr.progress.Progression;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Fangen und Zurücksetzen (Anforderung 15): Ein gefangener Fisch, über Wasser gehalten und benutzt,
 * kehrt als dasselbe Individuum zurück, mit Identität und Zustand. Das bringt etwas Erfahrung,
 * zählt für Rücksetz-Aufträge und senkt den Fangdruck. Überall sonst funktioniert der Gegenstand
 * wie zuvor (er lässt sich essen).
 */
public final class FishRelease {
    private FishRelease() {}

    public static void register() {
        UseItemCallback.EVENT.register(
                (player, level, hand) -> {
                    ItemStack stack = player.getItemInHand(hand);
                    FishCatch.Catch fish = FishCatch.read(stack);
                    if (fish == null || player.isSpectator()) return InteractionResult.PASS;
                    BlockPos water = FishingTools.water(level, player, 5);
                    if (water == null) return InteractionResult.PASS;
                    if (!(level instanceof ServerLevel server)
                            || !(player instanceof ServerPlayer serverPlayer))
                        return InteractionResult.SUCCESS;
                    if (fish.condition() <= 0) return InteractionResult.PASS;
                    FishrFishEntity released =
                            FishrFishEntity.spawn(
                                    server,
                                    fish.specimen(),
                                    fish.id(),
                                    fish.condition(),
                                    Vec3.atCenterOf(water));
                    if (released == null) return InteractionResult.FAIL;
                    stack.shrink(1);
                    server.sendParticles(
                            ParticleTypes.SPLASH,
                            water.getX() + 0.5,
                            water.getY() + 1,
                            water.getZ() + 0.5,
                            16,
                            0.3,
                            0.1,
                            0.3,
                            0.1);
                    server.playSound(
                            null, water, SoundEvents.FISH_SWIM, SoundSource.PLAYERS, 0.8f, 1.1f);
                    Ecology.released(server, water);
                    // Belohnt wird nur der Angler, der ihn gefangen hat, Fische herumzureichen
                    // bringt also nichts.
                    if (fish.angler().equals(serverPlayer.getName().getString())) {
                        Progression.released(
                                serverPlayer, fish.specimen(), Habitat.at(server, water));
                    }
                    return InteractionResult.SUCCESS;
                });
    }
}
