package io.github.dschonas04.fishr.item;

import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/** The slots of an Fishr rod, in the order they sit in its container component. */
public enum GearSlot {
    REEL,
    LINE,
    BAIT;

    public String translationKey() {
        return "tooltip.fishr.gear." + name().toLowerCase(Locale.ROOT);
    }

    /** Which slot an item goes into, or null if it is no rod gear. */
    public static GearSlot of(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (FishrItems.reel(stack) != null) return REEL;
        if (FishrItems.line(stack) != null) return LINE;
        if (FishrItems.bait(stack) != null) return BAIT;
        return null;
    }
}
