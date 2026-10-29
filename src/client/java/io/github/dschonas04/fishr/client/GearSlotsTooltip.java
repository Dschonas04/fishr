// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr.client;

import io.github.dschonas04.fishr.item.FishrRodItem;
import io.github.dschonas04.fishr.item.GearSlot;
import io.github.dschonas04.fishr.item.RodMods;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/**
 * Der Tooltip einer Rute: drei Plätze nebeneinander (Rolle, Schnur, Köder) mit einer Beschriftung
 * darunter, dann zwei kurze Zeilen, was die Rute hält und wie schnell sie einholt, und die
 * Verstärkungen aus der Werkstatt als Sterne.
 */
final class GearSlotsTooltip implements ClientTooltipComponent {
    private static final int SLOT = 20;
    private static final int COLUMN = 54;
    private final FishrRodItem.GearTooltip data;

    GearSlotsTooltip(FishrRodItem.GearTooltip data) {
        this.data = data;
    }

    private ItemStack stack(GearSlot slot) {
        return data.slots()[slot.ordinal()];
    }

    private static String number(double value) {
        return String.format(Locale.GERMAN, value >= 10 ? "%.0f" : "%.1f", value);
    }

    private Component[] lines() {
        var rig = data.rig();
        String stars =
                "★".repeat(data.upgrades()) + "☆".repeat(RodMods.MAX_UPGRADES - data.upgrades());
        return new Component[] {
            Component.translatable(
                    "tooltip.fishr.gear.holds",
                    number(Math.min(rig.lineStrength(), rig.rodLoad()))),
            Component.translatable(
                    "tooltip.fishr.gear.speed",
                    number(rig.reelSpeed() * 20),
                    number(rig.reelForce()),
                    number(rig.capacity())),
            Component.translatable("tooltip.fishr.gear.upgrades", stars)
        };
    }

    @Override
    public int getHeight(Font font) {
        return SLOT + 12 + lines().length * 10 + 4;
    }

    @Override
    public int getWidth(Font font) {
        int width = GearSlot.values().length * COLUMN;
        for (Component line : lines()) width = Math.max(width, font.width(line));
        return width;
    }

    @Override
    public void extractImage(
            Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        for (GearSlot slot : GearSlot.values()) {
            int left = x + slot.ordinal() * COLUMN + (COLUMN - SLOT) / 2;
            int top = y + 2;
            ItemStack stack = stack(slot);
            graphics.fill(
                    left, top, left + SLOT, top + SLOT, stack.isEmpty() ? 0xFF2A2F36 : 0xFF3A4652);
            graphics.outline(left, top, SLOT, SLOT, stack.isEmpty() ? 0xFF4A525C : 0xFF7FD8C8);
            if (!stack.isEmpty()) {
                graphics.item(stack, left + 2, top + 2);
                graphics.itemDecorations(font, stack, left + 2, top + 2);
            }
        }
    }

    @Override
    public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        for (GearSlot slot : GearSlot.values()) {
            Component label = Component.translatable(slot.translationKey());
            int centre = x + slot.ordinal() * COLUMN + COLUMN / 2;
            graphics.text(
                    font,
                    label,
                    centre - font.width(label) / 2,
                    y + SLOT + 4,
                    stack(slot).isEmpty() ? 0xFF7A828C : 0xFFD8E0E6,
                    false);
        }
        int[] colours = {0xFF7FD8C8, 0xFFB8C4CC, 0xFFFFC94A};
        Component[] lines = lines();
        for (int i = 0; i < lines.length; i++)
            graphics.text(font, lines[i], x, y + SLOT + 16 + i * 10, colours[i], true);
    }
}
