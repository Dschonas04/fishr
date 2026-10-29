package io.github.dschonas04.fishr.block;

import io.github.dschonas04.fishr.fish.FishCatch;
import io.github.dschonas04.fishr.fish.FishSpecies;
import io.github.dschonas04.fishr.fish.FishSpecimen;
import io.github.dschonas04.fishr.fish.FishVariant;
import io.github.dschonas04.fishr.fish.FishrFishEntity;
import io.github.dschonas04.fishr.holding.WaterConditions;
import io.github.dschonas04.fishr.item.BattleEquipment;
import io.github.dschonas04.fishr.item.FishrItems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Die Fische in einem Netz sind echte Fisch-Entities, die an es gebunden sind (sie behalten
 * Identität, Alter, Hunger und Gesundheit); das Netz prüft nur jede Minute das Wasser und lässt
 * gesunde Paare sich vermehren.
 */
public final class FishPenBlockEntity extends BlockEntity {
    /** Wasserblöcke, die ein Fisch braucht. */
    public static final int WATER_PER_FISH = 24;

    private double quality = 1;
    private long lastCheck = -1;
    private long lastBreeding;

    public FishPenBlockEntity(BlockPos pos, BlockState state) {
        super(FishrBlocks.FISH_PEN_ENTITY, pos, state);
    }

    List<FishrFishEntity> fish(ServerLevel level) {
        return level.getEntitiesOfClass(
                FishrFishEntity.class,
                new AABB(worldPosition).inflate(10),
                fish -> worldPosition.equals(fish.pen()) && fish.isAlive());
    }

    int capacity(ServerLevel level) {
        return Holding.survey(level, worldPosition, 3, 2).water() / WATER_PER_FISH;
    }

    static void tick(Level level, BlockPos pos, BlockState state, FishPenBlockEntity pen) {
        if (!(level instanceof ServerLevel server) || level.getGameTime() % 20 != 0) return;
        if (pen.lastCheck < 0) pen.lastCheck = level.getGameTime();
        if (level.getGameTime() - pen.lastCheck < 1200) return;
        double minutes = (level.getGameTime() - pen.lastCheck) / 1200.0;
        pen.lastCheck = level.getGameTime();
        pen.check(server, minutes);
    }

    private WaterConditions conditions(ServerLevel level, List<FishrFishEntity> fish) {
        Holding.Survey survey = Holding.survey(level, worldPosition, 3, 2);
        int capacity = Math.max(1, survey.water() / WATER_PER_FISH);
        double temperature =
                Holding.temperature(
                        level, worldPosition, fish.stream().map(FishrFishEntity::species).toList());
        WaterConditions water =
                Holding.conditions(
                        survey,
                        fish.size() * WATER_PER_FISH,
                        fish.size() / (double) capacity,
                        temperature,
                        quality);
        // Hungrige Fische sind gestresste Fische: Füttern zählt wie die Wasserqualität.
        double hunger = fish.stream().mapToDouble(FishrFishEntity::hunger).average().orElse(0);
        return new WaterConditions(
                water.volume(),
                water.oxygen(),
                water.temperature(),
                water.density(),
                Math.min(water.quality(), 1.2 - hunger));
    }

    private void check(ServerLevel level, double minutes) {
        List<FishrFishEntity> fish = fish(level);
        if (fish.isEmpty()) {
            quality = Math.min(1, quality + 0.05 * minutes);
            return;
        }
        Holding.Survey survey = Holding.survey(level, worldPosition, 3, 2);
        quality =
                survey.moving()
                        ? Math.min(1, quality + 0.05 * minutes)
                        : Math.max(0, quality - 0.002 * fish.size() * minutes);
        WaterConditions water = conditions(level, fish);
        double change = water.perMinute() * minutes;
        for (FishrFishEntity one : fish) {
            // Der Zustand in Prozent der Gesundheit des Fisches.
            float health = (float) (one.getHealth() + one.getMaxHealth() * change / 100);
            if (health <= 0) one.kill(level);
            else one.setHealth(Math.min(one.getMaxHealth(), health));
        }
        if (!water.problems().isEmpty()) {
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(Vec3.atCenterOf(worldPosition)) < 32 * 32) {
                    player.sendSystemMessage(
                            Component.translatable(
                                            "holding.fishr.pen_warning",
                                            Component.translatable(water.problems().getFirst()))
                                    .withStyle(ChatFormatting.GOLD),
                            true);
                }
            }
        }
        breed(level, fish, survey.water() / WATER_PER_FISH);
        setChanged();
    }

    /**
     * Alle zehn Minuten darf ein sattes, gesundes Paar einer Art Junge bekommen, wenn Platz ist.
     */
    private void breed(ServerLevel level, List<FishrFishEntity> fish, int capacity) {
        if (level.getGameTime() - lastBreeding < 12000 || fish.size() >= capacity) return;
        for (FishrFishEntity a : fish) {
            if (!ready(a)) continue;
            for (FishrFishEntity b : fish) {
                if (a == b || b.species() != a.species() || !ready(b)) continue;
                FishSpecies species = a.species();
                Random random = new Random(level.getRandom().nextLong());
                FishSpecimen young = species.roll(random, -1);
                // die Jungen übernehmen ab und zu die Färbung eines Elternteils: seltene Färbungen
                // lassen sich
                // züchten
                FishVariant colouring =
                        random.nextBoolean()
                                ? (random.nextBoolean() ? a : b).specimen().variant()
                                : FishVariant.NORMAL;
                FishSpecimen small =
                        new FishSpecimen(
                                species,
                                species.minLength(),
                                young.weightKg()
                                        * Math.pow(species.minLength() / young.lengthCm(), 3),
                                colouring);
                FishrFishEntity child =
                        FishrFishEntity.spawn(level, small, UUID.randomUUID(), 100, a.position());
                if (child != null) child.setPen(worldPosition);
                a.startle(0.1f);
                b.startle(0.1f);
                lastBreeding = level.getGameTime();
                level.sendParticles(
                        ParticleTypes.HEART,
                        a.getX(),
                        a.getY() + 0.5,
                        a.getZ(),
                        4,
                        0.3,
                        0.3,
                        0.3,
                        0);
                return;
            }
        }
    }

    private static boolean ready(FishrFishEntity fish) {
        return fish.species().boss() == null
                && fish.hunger() < 0.35f
                && fish.condition() > 80
                && fish.ageDays() >= 1;
    }

    boolean put(ServerPlayer player, InteractionHand hand) {
        ServerLevel level = (ServerLevel) this.level;
        ItemStack stack = player.getItemInHand(hand);
        List<FishrFishEntity> fish = fish(level);
        if (BattleEquipment.isBait(stack) || stack.is(FishrItems.GROUNDBAIT)) {
            if (fish.isEmpty()) return false;
            fish.forEach(one -> one.feed(stack.is(FishrItems.GROUNDBAIT) ? 1f : 0.4f));
            stack.consume(1, player);
            level.sendParticles(
                    ParticleTypes.SPLASH,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1,
                    worldPosition.getZ() + 0.5,
                    12,
                    1,
                    0.2,
                    1,
                    0.1);
            player.sendSystemMessage(
                    Component.translatable("holding.fishr.fed", fish.size()), true);
            return true;
        }
        FishCatch.Catch caught = FishCatch.read(stack);
        if (caught == null) return false;
        if (caught.condition() <= 0) {
            player.sendSystemMessage(Component.translatable("holding.fishr.add.dead"), true);
            return true;
        }
        if (fish.size() >= capacity(level)) {
            player.sendSystemMessage(
                    Component.translatable("holding.fishr.pen_full", fish.size(), capacity(level)),
                    true);
            return true;
        }
        // Jedes Individuum gibt es nur einmal: Der Gegenstand wird zum Fisch.
        for (FishrFishEntity other : fish) if (other.identity().equals(caught.id())) return true;
        FishrFishEntity entity =
                FishrFishEntity.spawn(
                        level,
                        caught.specimen(),
                        caught.id(),
                        caught.condition(),
                        Vec3.atCenterOf(worldPosition).add(0, 0.6, 0));
        if (entity == null) return true;
        entity.setPen(worldPosition);
        stack.shrink(1);
        level.playSound(null, worldPosition, SoundEvents.FISH_SWIM, SoundSource.BLOCKS, 0.8f, 1.0f);
        player.sendSystemMessage(
                Component.translatable(
                        "holding.fishr.pen_added",
                        Component.translatable(caught.specimen().species().translationKey()),
                        fish.size() + 1,
                        capacity(level)),
                true);
        return true;
    }

    void status(ServerPlayer player) {
        ServerLevel level = (ServerLevel) this.level;
        List<FishrFishEntity> fish = fish(level);
        WaterConditions water = conditions(level, fish);
        Component problems =
                water.problems().isEmpty()
                        ? Component.translatable("holding.fishr.water_good")
                        : Component.translatable(water.problems().getFirst());
        double condition =
                fish.stream().mapToDouble(FishrFishEntity::condition).average().orElse(100);
        player.sendSystemMessage(
                Component.translatable(
                        "holding.fishr.pen_status",
                        fish.size(),
                        capacity(level),
                        Math.round(condition),
                        problems),
                true);
    }

    void releaseAll(ServerPlayer player) {
        List<FishrFishEntity> fish = fish((ServerLevel) level);
        fish.forEach(one -> one.setPen(null));
        player.sendSystemMessage(
                Component.translatable("holding.fishr.released_all", fish.size()), true);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel server) fish(server).forEach(one -> one.setPen(null));
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putDouble("Quality", quality);
        output.putLong("LastCheck", lastCheck);
        output.putLong("LastBreeding", lastBreeding);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        quality = input.getDoubleOr("Quality", 1);
        lastCheck = input.getLongOr("LastCheck", -1);
        lastBreeding = input.getLongOr("LastBreeding", 0);
    }
}
