// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr;

import io.github.dschonas04.fishr.fish.FishSpecies;
import io.github.dschonas04.fishr.fish.FishrFishRegistry;
import io.github.dschonas04.fishr.item.FishrItems;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Der Kreativ-Tab „Fishr – Angeln“: alles aus der Mod an einer Stelle, in fester Reihenfolge
 * (Angeln, Köder, Bücher, Fische, Eimer, Spawn-Eier). Neue Items gehören in {@link #ordered()}.
 */
public final class ModItems {
    private ModItems() {}

    static List<Item> ordered() {
        List<Item> items = new ArrayList<>(FishrItems.ALL);
        items.addAll(io.github.dschonas04.fishr.block.FishrBlocks.ITEMS);
        for (FishSpecies species : FishSpecies.values())
            items.add(FishrFishRegistry.fishItem(species));
        for (FishSpecies species : FishSpecies.values())
            items.add(FishrFishRegistry.bucket(species));
        for (FishSpecies species : FishSpecies.values())
            items.add(FishrFishRegistry.spawnEgg(species));
        return items;
    }

    static void register() {
        ResourceKey<CreativeModeTab> tabKey =
                ResourceKey.create(Registries.CREATIVE_MODE_TAB, Fishr.id("fishr"));
        CreativeModeTab tab =
                FabricCreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.fishr"))
                        .icon(() -> new ItemStack(FishrItems.CARBON_ROD))
                        .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, tabKey, tab);
        // Der Vanilla-Builder hält seine Ausgabe geschützt, daher über Fabrics Event.
        CreativeModeTabEvents.modifyOutputEvent(tabKey)
                .register(
                        output -> {
                            for (Item item : ordered()) {
                                output.getDisplayStacks().add(new ItemStack(item));
                                output.getSearchTabStacks().add(new ItemStack(item));
                            }
                        });
    }
}
