package io.github.dschonas04.fishr.fish;

import io.github.dschonas04.fishr.Fishr;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public final class FishrFishRegistry {
    private static final Map<FishSpecies, EntityType<FishrFishEntity>> ENTITIES =
            new EnumMap<>(FishSpecies.class);
    private static final Map<FishSpecies, Item> FISH = new EnumMap<>(FishSpecies.class);
    private static final Map<FishSpecies, Item> BUCKETS = new EnumMap<>(FishSpecies.class);
    private static final Map<FishSpecies, Item> EGGS = new EnumMap<>(FishSpecies.class);

    private FishrFishRegistry() {}

    public static EntityType<FishrFishEntity> entity(FishSpecies species) {
        return ENTITIES.get(species);
    }

    public static Item fishItem(FishSpecies species) {
        return FISH.get(species);
    }

    public static Item bucket(FishSpecies species) {
        return BUCKETS.get(species);
    }

    public static Item spawnEgg(FishSpecies species) {
        return EGGS.get(species);
    }

    public static void initialize() {
        for (FishSpecies species : FishSpecies.values()) {
            var id = Fishr.id(species.id());
            EntityType<FishrFishEntity> type =
                    Registry.register(
                            BuiltInRegistries.ENTITY_TYPE,
                            id,
                            EntityType.Builder.<FishrFishEntity>of(
                                            (entityType, level) ->
                                                    new FishrFishEntity(entityType, level, species),
                                            MobCategory.WATER_AMBIENT)
                                    .sized(0.5f, 0.3f)
                                    .eyeHeight(0.15f)
                                    .clientTrackingRange(6)
                                    .noLootTable()
                                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
            ENTITIES.put(species, type);
            // Lockreichweite: Fische folgen einem Spieler, der Würmer hält (TemptGoal braucht das
            // Attribut)
            FabricDefaultAttributeRegistry.register(
                    type, AbstractFish.createAttributes().add(Attributes.TEMPT_RANGE, 8));
            SpawnPlacements.register(
                    type,
                    SpawnPlacementTypes.IN_WATER,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    FishrFishRegistry::canSpawn);
            FISH.put(
                    species,
                    item(
                            species.id(),
                            Item::new,
                            new Item.Properties()
                                    .stacksTo(1)
                                    .food(
                                            new FoodProperties.Builder()
                                                    .nutrition(2)
                                                    .saturationModifier(0.1f)
                                                    .build())
                                    .rarity(
                                            switch (species.rarity()) {
                                                case COMMON -> Rarity.COMMON;
                                                case UNCOMMON -> Rarity.UNCOMMON;
                                                case RARE -> Rarity.RARE;
                                                default -> Rarity.EPIC;
                                            })));
            BUCKETS.put(
                    species,
                    item(
                            species.id() + "_bucket",
                            properties ->
                                    new MobBucketItem(
                                            type,
                                            Fluids.WATER,
                                            SoundEvents.BUCKET_EMPTY_FISH,
                                            properties),
                            new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
            EGGS.put(
                    species,
                    item(
                            species.id() + "_spawn_egg",
                            SpawnEggItem::new,
                            new Item.Properties().spawnEgg(type)));
            // Jeder Fisch spawnt in den Biomen aller Lebensräume, in denen er lebt, so oft, wie er
            // dort beißt.
            for (Habitat habitat : Habitat.values()) {
                int weight = species.weight(habitat);
                if (weight > 0) {
                    BiomeModifications.addSpawn(
                            habitat.biomes(),
                            MobCategory.WATER_AMBIENT,
                            type,
                            weight,
                            1,
                            species.maxGroup());
                }
            }
        }
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS)
                .register(entries -> EGGS.values().forEach(entries::accept));
    }

    /**
     * Die Oberflächenregel von Vanilla erlaubt nur den Streifen zwischen Meeresspiegel - 13 und
     * Meeresspiegel, Seen und Flüsse über dem Meeresspiegel bekamen also nie Fische. Stattdessen
     * zählt jedes Wasser mit freiem Himmel darüber; Höhlen bleiben leer.
     */
    static boolean canSpawn(
            EntityType<FishrFishEntity> type,
            LevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        if (!level.getFluidState(pos).is(FluidTags.WATER)) return false;
        boolean deepEnough =
                level.getFluidState(pos.below()).is(FluidTags.WATER)
                        || level.getBlockState(pos.above()).is(Blocks.WATER);
        // Ein leergefischter Abschnitt füllt sich nur langsam wieder.
        if (level instanceof net.minecraft.server.level.ServerLevel server
                && reason == EntitySpawnReason.NATURAL
                && random.nextDouble()
                        > io.github.dschonas04.fishr.ecology.Ecology.factor(server, pos))
            return false;
        return deepEnough && level.canSeeSkyFromBelowWater(pos);
    }

    private static Item item(
            String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        var id = Fishr.id(name);
        return Registry.register(
                BuiltInRegistries.ITEM,
                id,
                factory.apply(properties.setId(ResourceKey.create(Registries.ITEM, id))));
    }
}
