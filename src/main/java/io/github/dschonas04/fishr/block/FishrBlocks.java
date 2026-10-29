// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr.block;

import io.github.dschonas04.fishr.Fishr;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Die Blöcke des Anglers: Setzkescher, Rutenhalter, Unterwasser-Netz, Fischmarkt, Werkstatt,
 * Köderreuse, Filetiertisch und Fischtafel.
 */
public final class FishrBlocks {
    private FishrBlocks() {}

    public static final List<Item> ITEMS = new ArrayList<>();

    public static final KeepnetBlock KEEPNET =
            block(
                    "keepnet",
                    KeepnetBlock::new,
                    BlockBehaviour.Properties.of()
                            .strength(0.6f)
                            .sound(SoundType.WOOL)
                            .noOcclusion());
    public static final RodHolderBlock ROD_HOLDER =
            block(
                    "rod_holder",
                    p -> new RodHolderBlock(p, RodHolderBlock.Kind.SINGLE),
                    BlockBehaviour.Properties.of()
                            .strength(0.8f)
                            .sound(SoundType.WOOD)
                            .noOcclusion());
    public static final RodHolderBlock DOUBLE_ROD_HOLDER =
            block(
                    "double_rod_holder",
                    p -> new RodHolderBlock(p, RodHolderBlock.Kind.DOUBLE),
                    BlockBehaviour.Properties.of()
                            .strength(0.8f)
                            .sound(SoundType.WOOD)
                            .noOcclusion());
    public static final RodHolderBlock ROD_POD =
            block(
                    "rod_pod",
                    p -> new RodHolderBlock(p, RodHolderBlock.Kind.POD),
                    BlockBehaviour.Properties.of()
                            .strength(1.2f)
                            .sound(SoundType.METAL)
                            .noOcclusion());
    public static final FishPenBlock FISH_PEN =
            block(
                    "fish_pen",
                    FishPenBlock::new,
                    BlockBehaviour.Properties.of()
                            .strength(1.0f)
                            .sound(SoundType.WOOL)
                            .noOcclusion());
    public static final FishMarketBlock FISH_MARKET =
            block(
                    "fish_market",
                    FishMarketBlock::new,
                    BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.WOOD));
    public static final WorkshopBlock WORKSHOP =
            block(
                    "workshop",
                    WorkshopBlock::new,
                    BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.WOOD));
    public static final BaitTrapBlock BAIT_TRAP =
            block(
                    "bait_trap",
                    BaitTrapBlock::new,
                    BlockBehaviour.Properties.of()
                            .strength(0.8f)
                            .sound(SoundType.WOOD)
                            .noOcclusion());
    public static final FilletTableBlock FILLET_TABLE =
            block(
                    "fillet_table",
                    FilletTableBlock::new,
                    BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.WOOD));
    public static final FishPlaqueBlock FISH_PLAQUE =
            block(
                    "fish_plaque",
                    FishPlaqueBlock::new,
                    BlockBehaviour.Properties.of()
                            .strength(1.0f)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                            .noCollision());

    public static final BlockEntityType<KeepnetBlockEntity> KEEPNET_ENTITY =
            entity("keepnet", KeepnetBlockEntity::new, KEEPNET);
    public static final BlockEntityType<RodHolderBlockEntity> ROD_HOLDER_ENTITY =
            entity("rod_holder", RodHolderBlockEntity::new, ROD_HOLDER, DOUBLE_ROD_HOLDER, ROD_POD);
    public static final BlockEntityType<FishPenBlockEntity> FISH_PEN_ENTITY =
            entity("fish_pen", FishPenBlockEntity::new, FISH_PEN);
    public static final BlockEntityType<BaitTrapBlockEntity> BAIT_TRAP_ENTITY =
            entity("bait_trap", BaitTrapBlockEntity::new, BAIT_TRAP);
    public static final BlockEntityType<FishPlaqueBlockEntity> FISH_PLAQUE_ENTITY =
            entity("fish_plaque", FishPlaqueBlockEntity::new, FISH_PLAQUE);

    private static <B extends Block> B block(
            String name,
            Function<BlockBehaviour.Properties, B> factory,
            BlockBehaviour.Properties properties) {
        var id = Fishr.id(name);
        B block =
                Registry.register(
                        BuiltInRegistries.BLOCK,
                        id,
                        factory.apply(properties.setId(ResourceKey.create(Registries.BLOCK, id))));
        Item.Properties itemProperties =
                new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id));
        itemProperties.component(
                DataComponents.LORE,
                new ItemLore(List.of(Component.translatable("tooltip.fishr." + name))));
        ITEMS.add(
                Registry.register(
                        BuiltInRegistries.ITEM, id, new BlockItem(block, itemProperties)));
        return block;
    }

    private static <T extends BlockEntity> BlockEntityType<T> entity(
            String name, BlockEntityType.BlockEntitySupplier<T> factory, Block... blocks) {
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Fishr.id(name),
                new BlockEntityType<>(factory, Set.of(blocks)));
    }

    public static void initialize() {}
}
