package io.github.dschonas04.fishr.item;

import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/**
 * Die Plätze einer Fishr-Rute, in der Reihenfolge, in der sie in ihrer Container-Komponente stehen.
 */
public enum GearSlot {
    REEL,
    LINE,
    BAIT;

    public String translationKey() {
        return "tooltip.fishr.gear." + name().toLowerCase(Locale.ROOT);
    }

    /** In welchen Platz ein Gegenstand gehört, oder null, wenn er kein Rutenzubehör ist. */
    public static GearSlot of(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (FishrItems.reel(stack) != null) return REEL;
        if (FishrItems.line(stack) != null) return LINE;
        if (FishrItems.bait(stack) != null) return BAIT;
        return null;
    }
}
