package io.github.dschonas04.fishr.fish;

import io.github.dschonas04.fishr.Fishr;
import io.github.dschonas04.fishr.item.BaitType;
import io.github.dschonas04.fishr.item.FishrItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.fish.AbstractSchoolingFish;
import net.minecraft.world.entity.animal.fish.Cod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Ein Fisch in der Welt, ein Individuum: Art, Identität, Länge, Gewicht, Alter, Hunger, Scheu und
 * Gesundheit. Er hält seine bevorzugte Tiefe, jagt als Raubfisch kleinere Fische, flieht vor
 * Räubern, schwimmt im Schwarm mit dem Verhalten der Stunde (siehe {@link FishBehaviour}), bemerkt
 * Posen und beißt ({@link BiteHookGoal}) und kann in einem Unterwasser-Netz leben. Fern von allen
 * Spielern ruht sein zusätzliches Verhalten, damit große Gewässer günstig bleiben.
 */
public final class FishrFishEntity extends Cod {
    /** Die Färbung, für den Renderer. */
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(FishrFishEntity.class, EntityDataSerializers.INT);

    /** Anteil des Längenbereichs der Art, um den ein Fisch pro Fütterung mit Würmern wächst. */
    private static final double GROWTH = 0.05;

    private final FishSpecies species;
    private FishSpecimen specimen;

    /**
     * Wird im Drill an der Schnur gezeigt; nie gespeichert, nie in einen Eimer gesteckt, lässt nie
     * etwas fallen.
     */
    private boolean battleDummy;

    /**
     * Die Identität des Fisches: Sie bleibt in der Hand, im Setzkescher, im Netz und im Wasser
     * dieselbe.
     */
    private UUID identity = UUID.randomUUID();

    private long ageTicks;
    private float hunger;
    private float shyness;
    private BlockPos pen;
    private boolean nearPlayer;

    public FishrFishEntity(EntityType<? extends Cod> type, Level level, FishSpecies species) {
        super(type, level);
        this.species = species;
        Random random = new Random(getRandom().nextLong());
        setSpecimen(species.roll(random));
        hunger = random.nextFloat() * 0.6f;
        shyness = random.nextFloat() * 0.3f;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
    }

    /** Die Färbung; auf dem Client aus den synchronisierten Daten. */
    public FishVariant variant() {
        FishVariant[] variants = FishVariant.values();
        return variants[Math.clamp(entityData.get(VARIANT), 0, variants.length - 1)];
    }

    /** Ein wilder Fisch bekommt die Färbung des Ortes und der Stunde, an dem er erscheint. */
    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason reason,
            SpawnGroupData data) {
        if (reason != EntitySpawnReason.BUCKET && !battleDummy) {
            setSpecimen(
                    specimen.withVariant(
                            FishVariant.at(
                                    level.getLevel(),
                                    blockPosition(),
                                    new Random(getRandom().nextLong()))));
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    /** Lavafische brennen nicht, weder an der Schnur noch im Wasser des Eimers. */
    @Override
    public boolean fireImmune() {
        return (species != null && species.lava()) || super.fireImmune();
    }

    public FishSpecimen specimen() {
        return specimen;
    }

    public FishSpecies species() {
        return species;
    }

    public UUID identity() {
        return identity;
    }

    public float hunger() {
        return hunger;
    }

    public float shyness() {
        return shyness;
    }

    public long ageDays() {
        return ageTicks / 24000;
    }

    public BlockPos pen() {
        return pen;
    }

    /** Zusätzliches Verhalten nur in der Nähe von Spielern (Leistungsstufen). */
    boolean active() {
        return nearPlayer;
    }

    public void feed(float amount) {
        hunger = Math.max(0, hunger - amount);
    }

    public void startle(float amount) {
        shyness = Math.min(1, shyness + amount);
    }

    /** 0..100, aus der Gesundheit. */
    public double condition() {
        return 100.0 * getHealth() / getMaxHealth();
    }

    public void setPen(BlockPos pos) {
        pen = pos;
        if (pos != null) {
            setPersistenceRequired();
            setHomeTo(pos, 3);
        } else {
            clearHome();
        }
    }

    /**
     * Bringt einen Fisch (aus einer Hand, einem Setzkescher oder einem Netz) als genau dieses
     * Individuum zurück in die Welt.
     */
    public static FishrFishEntity spawn(
            ServerLevel level, FishSpecimen specimen, UUID identity, double condition, Vec3 at) {
        FishrFishEntity fish =
                FishrFishRegistry.entity(specimen.species())
                        .create(level, EntitySpawnReason.BUCKET);
        if (fish == null) return null;
        fish.setSpecimen(specimen);
        fish.identity = identity;
        fish.setHealth(
                (float) Math.max(1, fish.getMaxHealth() * Math.clamp(condition, 1, 100) / 100));
        fish.startle(0.5f);
        fish.setFromBucket(true);
        fish.snapTo(at.x, at.y, at.z, level.getRandom().nextFloat() * 360, 0);
        level.addFreshEntity(fish);
        return fish;
    }

    public void becomeBattleDummy(FishSpecimen hooked) {
        battleDummy = true;
        setNoAi(true);
        setPermanentlyInvulnerable(true);
        setSilent(true);
        setSpecimen(hooked);
    }

    public boolean isBattleDummy() {
        return battleDummy;
    }

    @Override
    public void tick() {
        // Eine Attrappe, deren Drill weg ist (Serverneustart, Absturz der Sitzung), darf nicht
        // liegen
        // bleiben.
        if (battleDummy && !level().isClientSide() && !Fishr.BATTLES.ownsDummy(this)) {
            discard();
            return;
        }
        super.tick();
        if (level().isClientSide() || battleDummy) return;
        ageTicks++;
        if (tickCount % 40 == 0) {
            nearPlayer =
                    level().getNearestPlayer(
                                            this,
                                            io.github.dschonas04.fishr.Performance.current()
                                                    .activeRadius())
                            != null;
            // Der Hunger wächst, die Scheu lässt nach; beides ändert sich langsam.
            hunger = Math.min(1, hunger + 0.004f);
            shyness = Math.max(0, shyness - 0.003f);
        }
        if (pen != null && tickCount % 20 == 0 && distanceToSqr(Vec3.atCenterOf(pen)) > 36) {
            // Aus dem Netz heraus: Das Netz hält ihn zurück.
            Vec3 home = Vec3.atCenterOf(pen).add(0, -1.5, 0);
            setDeltaMovement(home.subtract(position()).normalize().scale(0.2));
            if (distanceToSqr(home) > 144) snapTo(home.x, home.y, home.z, getYRot(), 0);
        }
    }

    @Override
    public boolean shouldBeSaved() {
        return !battleDummy && super.shouldBeSaved();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !battleDummy && pen == null && super.removeWhenFarAway(distance);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (battleDummy) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        if (feedsOn(stack)) {
            if (level() instanceof ServerLevel server) feed(server, player, stack);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    /** Würmer, Maden, Blutegel und Raupen: Fische folgen ihnen und wachsen davon. */
    static boolean feedsOn(ItemStack stack) {
        BaitType bait = FishrItems.bait(stack);
        return bait != null && bait.natural();
    }

    /**
     * Mit der Hand gefüttert (wie in Fish of Thieves): Ein hungriger Fisch frisst und wächst ein
     * wenig, bis zur Trophäengröße und weiter bis zum größten seiner Art; ein satter dreht ab.
     */
    private void feed(ServerLevel level, Player player, ItemStack stack) {
        if (hunger < 0.15f) {
            player.sendOverlayMessage(Component.translatable("message.fishr.fish_full"));
            return;
        }
        boolean wasTrophy = specimen.trophy();
        FishSpecimen grown = specimen.grown(GROWTH);
        float health = getHealth();
        setSpecimen(grown);
        setHealth(Math.min(getMaxHealth(), health + 2));
        feed(0.6f);
        setPersistenceRequired();
        stack.consume(1, player);
        level.sendParticles(ParticleTypes.HEART, getX(), getY() + 0.3, getZ(), 3, 0.2, 0.2, 0.2, 0);
        level.playSound(
                null,
                blockPosition(),
                SoundEvents.GENERIC_EAT.value(),
                SoundSource.NEUTRAL,
                0.5f,
                1.4f);
        player.sendOverlayMessage(
                Component.translatable(
                        !wasTrophy && grown.trophy()
                                ? "message.fishr.fish_trophy"
                                : "message.fishr.fish_fed",
                        Component.translatable(species.translationKey()),
                        String.format(java.util.Locale.GERMAN, "%.1f", grown.lengthCm())));
    }

    void setSpecimen(FishSpecimen specimen) {
        this.specimen = specimen;
        if (!level().isClientSide()) {
            entityData.set(VARIANT, specimen.variant().ordinal());
            // Die Größe ist ein synchronisiertes Vanilla-Attribut: Darstellung und Hitbox folgen
            // derselben Größe.
            getAttribute(Attributes.SCALE)
                    .setBaseValue(specimen.lengthCm() / species.modelLengthCm());
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(3 + specimen.sizeFraction() * 5);
            setHealth(getMaxHealth());
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new BiteHookGoal(this));
        goalSelector.addGoal(1, new TemptGoal(this, 1.25, FishrFishEntity::feedsOn, false));
        // Kleine Fische fliehen vor Räubern, die sie verschlucken könnten.
        goalSelector.addGoal(
                0,
                new AvoidEntityGoal<>(
                        this,
                        FishrFishEntity.class,
                        6,
                        1.4,
                        1.7,
                        other ->
                                other instanceof FishrFishEntity predator
                                        && active()
                                        && predator.species.diet().predatory()
                                        && predator.specimen.lengthCm()
                                                > specimen.lengthCm() * 2.5));
        goalSelector.addGoal(2, new FishBehaviour.HuntGoal(this));
        goalSelector.addGoal(3, new FishBehaviour.SchoolGoal(this));
        goalSelector.addGoal(4, new FishBehaviour.DepthGoal(this));
    }

    @Override
    public int getMaxSchoolSize() {
        return species.maxGroup() > 1 ? species.maxGroup() + 1 : 1;
    }

    @Override
    public AbstractSchoolingFish startFollowing(AbstractSchoolingFish leader) {
        return getMaxSchoolSize() > 1 && leader.getType() == getType()
                ? super.startFollowing(leader)
                : this;
    }

    @Override
    public void addFollowers(Stream<? extends AbstractSchoolingFish> fish) {
        super.addFollowers(fish.filter(other -> other.getType() == getType()));
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(FishrFishRegistry.bucket(species));
    }

    @Override
    public void saveToBucketTag(ItemStack bucket) {
        super.saveToBucketTag(bucket);
        CustomData.update(
                DataComponents.BUCKET_ENTITY_DATA,
                bucket,
                tag -> {
                    tag.putDouble("FishrLength", specimen.lengthCm());
                    tag.putDouble("FishrWeight", specimen.weightKg());
                    tag.putString("FishrIdentity", identity.toString());
                    tag.putLong("FishrAge", ageTicks);
                    tag.putString("FishrVariant", specimen.variant().id());
                });
        bucket.set(DataComponents.LORE, new ItemLore(FishCatch.measurementLore(specimen)));
        // Ein Fisch im Eimer ist nicht mehr in seinem Netz.
        pen = null;
    }

    @Override
    public void loadFromBucketTag(CompoundTag tag) {
        restore(
                tag.getDoubleOr("FishrLength", specimen.lengthCm()),
                tag.getDoubleOr("FishrWeight", specimen.weightKg()),
                FishVariant.byId(tag.getStringOr("FishrVariant", "")));
        identity = parse(tag.getStringOr("FishrIdentity", ""), identity);
        ageTicks = tag.getLongOr("FishrAge", ageTicks);
        // Die maximale Gesundheit wiederherstellen, bevor Vanilla die gespeicherte aktuelle
        // Gesundheit
        // wiederherstellt.
        super.loadFromBucketTag(tag);
    }

    private static UUID parse(String text, UUID fallback) {
        try {
            return text.isEmpty() ? fallback : UUID.fromString(text);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putDouble("FishrLength", specimen.lengthCm());
        output.putDouble("FishrWeight", specimen.weightKg());
        output.putString("FishrIdentity", identity.toString());
        output.putLong("FishrAge", ageTicks);
        output.putString("FishrVariant", specimen.variant().id());
        output.putFloat("FishrHunger", hunger);
        output.putFloat("FishrShyness", shyness);
        if (pen != null) output.putLong("FishrPen", pen.asLong());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        restore(
                input.getDoubleOr("FishrLength", specimen.lengthCm()),
                input.getDoubleOr("FishrWeight", specimen.weightKg()),
                FishVariant.byId(input.getStringOr("FishrVariant", "")));
        identity = parse(input.getStringOr("FishrIdentity", ""), identity);
        ageTicks = input.getLongOr("FishrAge", 0);
        hunger = input.getFloatOr("FishrHunger", hunger);
        shyness = input.getFloatOr("FishrShyness", shyness);
        input.getLong("FishrPen").ifPresent(pos -> setPen(BlockPos.of(pos)));
    }

    private void restore(double length, double weight, FishVariant variant) {
        if (Double.isFinite(length) && Double.isFinite(weight) && weight > 0) {
            float health = getHealth();
            setSpecimen(
                    new FishSpecimen(
                            species,
                            Math.clamp(length, species.minLength(), species.maxLength()),
                            weight,
                            variant));
            setHealth(Math.min(health, getMaxHealth()));
        }
    }

    @Override
    protected void dropCustomDeathLoot(
            ServerLevel level, DamageSource source, boolean causedByPlayer) {
        if (battleDummy) return;
        super.dropCustomDeathLoot(level, source, causedByPlayer);
        spawnAtLocation(level, FishCatch.create(specimen, level, blockPosition(), -1, ""));
    }
}
