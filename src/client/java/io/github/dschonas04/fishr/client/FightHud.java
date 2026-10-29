// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr.client;

import io.github.dschonas04.fishr.battle.Boss;
import io.github.dschonas04.fishr.battle.FishAction;
import io.github.dschonas04.fishr.battle.FishingBattle;
import io.github.dschonas04.fishr.battle.Landing;
import io.github.dschonas04.fishr.fish.FishCatch;
import io.github.dschonas04.fishr.fish.FishSpecies;
import io.github.dschonas04.fishr.fish.FishVariant;
import io.github.dschonas04.fishr.fish.FishrFishRegistry;
import io.github.dschonas04.fishr.network.BattlePayload;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/**
 * Die Drillanzeige über der Hotbar, so breit wie die Hotbar: der Fisch mit Name und Entfernung, die
 * Schnurspannung (grün bis zur Bremse, rot darüber) und die Kraft des Fisches. Darüber steht eine
 * Anweisung, welche Taste jetzt dran ist – R zum Einholen, F für Schnur, Rechtsklick zum Landen.
 * Ein Boss zeigt oben Namen und Phase, ein gelandeter Fisch eine Fangkarte.
 */
final class FightHud {
    private static final int WIDTH = 182;
    private static final int HEIGHT = 36;
    private static final int PANEL = 0xC0121820;
    private static final int TRACK = 0xFF26303A;
    private static final int GREEN = 0xFF4FD08A;
    private static final int ORANGE = 0xFFFFA040;
    private static final int RED = 0xFFFF5B6E;

    private FightHud() {}

    /** Was jetzt zu tun ist: eine Taste, ein Wort. */
    enum Prompt {
        LAND(GREEN),
        NET(GREEN),
        WARN(ORANGE),
        WAIT(ORANGE),
        GIVE(ORANGE),
        GIVE_NOW(RED),
        REEL(GREEN),
        TIGHTEN(0xFFFFD45B);

        final int colour;

        Prompt(int colour) {
            this.colour = colour;
        }

        String key() {
            return "hud.fishr.prompt." + name().toLowerCase(Locale.ROOT);
        }

        boolean blinks() {
            return this == GIVE_NOW || this == WARN || this == LAND || this == NET;
        }
    }

    /**
     * Dieselben Regeln, nach denen der Drill läuft (siehe FishingBattle): einholen, solange er
     * ruht, aufhören, wenn er zieht, bei Sprüngen nachgeben.
     */
    static Prompt prompt(BattlePayload state) {
        if (state.has(BattlePayload.LANDABLE))
            return state.netWindow() >= 0 ? Prompt.NET : Prompt.LAND;
        boolean giving = state.has(BattlePayload.GIVING);
        boolean away =
                state.pending() == null
                        && (state.action() == FishAction.RUN
                                || state.action() == FishAction.DIVE
                                || state.action() == FishAction.SIDE);
        if (state.has(BattlePayload.TWIST)
                || away && !giving && FishrClient.displayedTension >= state.drag() * 0.95f)
            return Prompt.GIVE_NOW;
        if (state.pending() != null) return Prompt.WARN;
        return switch (state.action()) {
            case JUMP, SHAKE -> Prompt.GIVE;
            case RUN, DIVE, SIDE -> Prompt.WAIT;
            default -> state.has(BattlePayload.SLACK) ? Prompt.TIGHTEN : Prompt.REEL;
        };
    }

    private static KeyMapping keyOf(Prompt prompt) {
        return switch (prompt) {
            case GIVE, GIVE_NOW -> FishrClient.giveKey;
            case REEL, TIGHTEN -> FishrClient.reelKey;
            default -> null;
        };
    }

    static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        Font font = client.font;
        if (FishrClient.trophyTicks > 0 && FishrClient.trophy != null)
            catchCard(graphics, font, FishrClient.trophy);
        var state = FishrClient.state;
        if (state == null || state.result() != FishingBattle.Result.ACTIVE) return;
        int x = (graphics.guiWidth() - WIDTH) / 2;
        int y = graphics.guiHeight() - 59 - HEIGHT;
        FishSpecies species = state.species();
        int rarity = rarityColour(species);

        // die Anzeige: oben ein dünner Strich in der Seltenheitsfarbe des Fisches
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, PANEL);
        graphics.fill(x, y, x + WIDTH, y + 1, rarity);
        graphics.item(new ItemStack(FishrFishRegistry.fishItem(species)), x + 3, y + 3);
        graphics.text(
                font,
                Component.translatable(species.translationKey()),
                x + 22,
                y + 4,
                rarity,
                true);
        Component right =
                state.hookHold() < 35
                        ? Component.translatable("hud.fishr.hook_loose")
                        : Component.translatable(
                                "hud.fishr.distance", Math.round(state.distance()));
        graphics.text(
                font,
                right,
                x + WIDTH - 4 - font.width(right),
                y + 4,
                state.hookHold() < 35 ? RED : 0xFF9AA8B4,
                true);

        // die Schnur: grün bis zur Bremse, rot darüber; die weiße Marke ist die Spannung
        int barX = x + 22, barW = WIDTH - 26, barY = y + 15;
        float tension = Math.clamp(FishrClient.displayedTension, 0, 1);
        int drag = Math.clamp(Math.round(barW * state.drag()), 1, barW - 1);
        graphics.fill(barX, barY, barX + barW, barY + 6, TRACK);
        graphics.fill(barX + drag, barY, barX + barW, barY + 6, 0xFF4A2630);
        int fill = Math.round(barW * tension);
        float share = state.drag() <= 0 ? 1 : tension / state.drag();
        int colour = share < 0.75f ? GREEN : share < 0.97f ? 0xFFE8C04A : RED;
        graphics.fill(barX, barY + 1, barX + Math.min(fill, barW), barY + 5, colour);
        graphics.fill(barX + drag, barY - 1, barX + drag + 1, barY + 7, 0xFF8AE8F0);
        int mark = barX + Math.clamp(fill, 1, barW - 1);
        graphics.fill(mark - 1, barY - 1, mark + 1, barY + 7, 0xFFFFFFFF);

        // die Kraft des Fisches
        int stamina = Math.round(barW * Math.clamp(FishrClient.displayedStamina, 0, 1));
        graphics.fill(barX, y + 25, barX + barW, y + 28, TRACK);
        graphics.fill(barX, y + 25, barX + stamina, y + 28, 0xFF4CC8E0);
        graphics.text(
                font,
                Component.translatable("hud.fishr.strength"),
                x + 3,
                y + 23,
                0xFF6A7A88,
                false);

        // die Anweisung: eine Tastenkappe und ein Wort; blinkt, wenn es eilt
        Prompt prompt = prompt(state);
        boolean hidden = prompt.blinks() && (FishrClient.fightTicks / 5) % 2 == 1;
        if (!hidden) prompt(graphics, font, prompt, graphics.guiWidth() / 2, y - 14);

        if (FishrClient.fightTicks < 50 && state.strike() > 0) {
            String quality =
                    state.strike() >= 90 ? "perfect" : state.strike() >= 72 ? "good" : "late";
            Component strike = Component.translatable("hud.fishr.strike." + quality);
            graphics.centeredText(font, strike, graphics.guiWidth() / 2, y - 27, 0xFFE0E6EA);
        }
        if (state.has(BattlePayload.LANDABLE) && state.netWindow() >= 0)
            netGauge(graphics, x, y - 28, state);
        Boss boss = species.boss();
        if (boss != null) bossBar(graphics, font, state, boss);
    }

    /** [R] Einholen – eine Tastenkappe wie auf der Tastatur, dann das Wort. */
    private static void prompt(
            GuiGraphicsExtractor graphics, Font font, Prompt prompt, int centre, int y) {
        KeyMapping key = keyOf(prompt);
        Component word = Component.translatable(prompt.key());
        Component cap = key == null ? null : key.getTranslatedKeyMessage();
        int capWidth = cap == null ? 0 : Math.max(11, font.width(cap) + 6);
        int total = capWidth + (cap == null ? 0 : 4) + font.width(word);
        int x = centre - total / 2;
        if (cap != null) {
            graphics.fill(x, y - 2, x + capWidth, y + 10, 0xFF0C1014);
            graphics.outline(x, y - 2, capWidth, 12, prompt.colour);
            graphics.centeredText(font, cap, x + capWidth / 2, y, 0xFFFFFFFF);
            x += capWidth + 4;
        }
        graphics.text(font, word, x, y, prompt.colour, true);
    }

    /**
     * Die Landeanzeige: Der Fisch windet sich vor dem Kescher; das grüne Feld ist die Öffnung des
     * Keschers. Keschern (Rechtsklick), solange die Marke darin steht. Dieselbe Formel wie auf dem
     * Server, also zählt, was man sieht.
     */
    private static void netGauge(GuiGraphicsExtractor graphics, int x, int y, BattlePayload state) {
        int half = WIDTH / 2, centre = x + half;
        int window = Math.round(half * state.netWindow());
        graphics.fill(x, y, x + WIDTH, y + 6, 0xFF3A2A2A);
        graphics.fill(centre - window, y, centre + window, y + 6, 0xFF2F8A5A);
        double ticks = state.landTicks() + FishrClient.packetAge;
        int marker = centre + (int) Math.round(Landing.marker((int) ticks) * half);
        boolean inside = Math.abs(Landing.marker((int) ticks)) <= state.netWindow();
        graphics.fill(marker - 2, y - 2, marker + 2, y + 8, inside ? 0xFFFFFFFF : 0xFFFF7A5C);
    }

    private static void bossBar(
            GuiGraphicsExtractor graphics, Font font, BattlePayload state, Boss boss) {
        int width = 182;
        int x = (graphics.guiWidth() - width) / 2;
        int y = 16;
        Component name =
                Component.translatable(boss.translationKey())
                        .append(" – ")
                        .append(
                                Component.translatable(
                                        boss.phase(state.bossPhase()).translationKey(boss)));
        graphics.centeredText(font, name, graphics.guiWidth() / 2, y - 11, 0xFFFFC94A);
        graphics.fill(x - 1, y - 1, x + width + 1, y + 5, 0xC0101820);
        graphics.fill(
                x,
                y,
                x + Math.round(width * Math.clamp(FishrClient.displayedStamina, 0, 1)),
                y + 4,
                0xFFC0392B);
        for (int phase = 0; phase < boss.phases(); phase++) {
            int pip = x + width / 2 + (phase - boss.phases() / 2) * 10;
            graphics.fill(
                    pip,
                    y + 7,
                    pip + 6,
                    y + 9,
                    phase < state.bossPhase()
                            ? 0xFF505050
                            : phase == state.bossPhase() ? 0xFFFFC94A : 0xFFC0392B);
        }
    }

    /**
     * Der Fang: oben eine Karte mit dem Fisch, seiner Seltenheit, seiner Größe und wie sauber er
     * gedrillt wurde.
     */
    private static void catchCard(GuiGraphicsExtractor graphics, Font font, BattlePayload state) {
        FishSpecies species = state.species();
        int colour = rarityColour(species);
        int width = 210, x = (graphics.guiWidth() - width) / 2, y = 30;
        graphics.fill(x, y, x + width, y + 44, 0xD0101418);
        graphics.outline(x, y, width, 44, colour);
        graphics.fill(x + 6, y + 8, x + 34, y + 36, 0xFF1C242C);
        ItemStack icon = new ItemStack(FishrFishRegistry.fishItem(species));
        FishCatch.colour(icon, state.variant());
        graphics.item(icon, x + 12, y + 14);
        int text = x + 42;
        graphics.text(
                font, Component.translatable(species.translationKey()), text, y + 6, colour, true);
        // eine seltene Färbung steht an Stelle der Seltenheit
        Component rarity =
                state.variant() == FishVariant.NORMAL
                        ? Component.translatable(species.rarityKey())
                        : Component.translatable(state.variant().translationKey());
        graphics.text(
                font,
                rarity,
                x + width - 6 - font.width(rarity),
                y + 6,
                state.variant() == FishVariant.NORMAL ? 0xFF8A98A4 : 0xFFE08CFF,
                false);
        graphics.text(
                font,
                Component.translatable(
                        "catch.fishr.measurements",
                        String.format(Locale.GERMAN, "%.1f", state.lengthCm()),
                        String.format(Locale.GERMAN, "%.2f", state.weightKg())),
                text,
                y + 18,
                0xFFFFFFFF,
                true);
        graphics.text(
                font,
                Component.translatable("hud.fishr.trophy_quality", state.quality()),
                text,
                y + 30,
                0xFF7FD8C8,
                false);
    }

    static int rarityColour(FishSpecies species) {
        return switch (species.rarity()) {
            case COMMON -> 0xFFE8ECEF;
            case UNCOMMON -> 0xFF6CE07A;
            case RARE -> 0xFF5BC8FF;
            case EPIC -> 0xFFC880FF;
            case LEGENDARY -> 0xFFFFC94A;
            case MYTHIC -> 0xFFFF6060;
        };
    }
}
