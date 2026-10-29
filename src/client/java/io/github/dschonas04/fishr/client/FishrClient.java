package io.github.dschonas04.fishr.client;

import com.mojang.blaze3d.platform.InputConstants;

import io.github.dschonas04.fishr.Fishr;
import io.github.dschonas04.fishr.FishrSounds;
import io.github.dschonas04.fishr.battle.FishAction;
import io.github.dschonas04.fishr.battle.FishingBattle;
import io.github.dschonas04.fishr.block.FishrBlocks;
import io.github.dschonas04.fishr.item.FishrRodItem;
import io.github.dschonas04.fishr.network.BattlePayload;
import io.github.dschonas04.fishr.network.LexiconPayload;
import io.github.dschonas04.fishr.network.ReelPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Die Client-Seite eines Drills: zwei Tasten (R holt ein, F gibt Schnur), die Geräusche der Schnur
 * und die Anzeige über der Hotbar ({@link FightHud}). Die Sicht bleibt die ganze Zeit die des
 * Spielers.
 */
public final class FishrClient implements ClientModInitializer {
    /** Wie lange die Fangkarte zu sehen ist. */
    static final int TROPHY_TICKS = 80;

    static KeyMapping reelKey, giveKey;
    static BattlePayload state;

    /** Der zuletzt gelandete Fisch, für die Fangkarte. */
    static BattlePayload trophy;

    static int trophyTicks;
    static float displayedTension;
    static float displayedStamina;

    /** Ticks seit Beginn des laufenden Drills. */
    static int fightTicks;

    private static boolean reeling, giving;
    private static int outroTicks;
    private static boolean hintShown;
    private static int ticks;
    static int packetAge;

    @Override
    public void onInitializeClient() {
        FishrFishRenderer.register();
        BlockEntityRendererRegistry.register(
                FishrBlocks.FISH_PLAQUE_ENTITY, FishPlaqueRenderer::new);
        var category = KeyMapping.Category.register(Fishr.id("fishing"));
        reelKey = key("reel", InputConstants.KEY_R, category);
        giveKey = key("give", InputConstants.KEY_F, category);
        ClientPlayNetworking.registerGlobalReceiver(
                BattlePayload.TYPE, (payload, context) -> receive(payload, context.client()));
        ClientPlayNetworking.registerGlobalReceiver(
                LexiconPayload.TYPE,
                (payload, context) ->
                        context.client().gui.setScreen(new LexiconScreen(payload.data())));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clear());
        // F ist in Vanilla auch die Taste zum Händetauschen: Im Drill gibt sie nur Schnur, die Rute
        // bleibt
        // in der Hand.
        ClientTickEvents.START_CLIENT_TICK.register(
                client -> {
                    if (fighting()) while (client.options.keySwapOffhand.consumeClick()) {}
                });
        ClientTickEvents.END_CLIENT_TICK.register(FishrClient::tick);
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT, Fishr.id("fight"), FightHud::render);
        ClientTooltipComponentCallback.EVENT.register(
                data ->
                        data instanceof FishrRodItem.GearTooltip gear
                                ? new GearSlotsTooltip(gear)
                                : null);
    }

    private static KeyMapping key(String name, int key, KeyMapping.Category category) {
        return KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.fishr." + name, key, category));
    }

    static boolean fighting() {
        return state != null && state.result() == FishingBattle.Result.ACTIVE;
    }

    private static void receive(BattlePayload payload, Minecraft client) {
        boolean newBattle = state == null || state.battleId() != payload.battleId();
        if (newBattle) {
            displayedTension = payload.tension();
            displayedStamina = payload.stamina();
            reeling = giving = false;
            fightTicks = 0;
            outroTicks = 0;
            trophyTicks = 0;
            if (client.player != null) {
                client.player.playSound(FishrSounds.HOOK_SET, 0.8f, 1.0f);
                if (!hintShown) {
                    hintShown = true;
                    client.player.sendSystemMessage(
                            Component.translatable(
                                    "message.fishr.controls_hint",
                                    reelKey.getTranslatedKeyMessage(),
                                    giveKey.getTranslatedKeyMessage()));
                }
            }
        } else if (state.pending() == null && payload.pending() != null && client.player != null) {
            client.player.playSound(
                    FishrSounds.WARNING, 0.7f, payload.pending() == FishAction.JUMP ? 1.3f : 1.0f);
        }
        if (payload.result() != FishingBattle.Result.ACTIVE
                && (state == null || state.result() == FishingBattle.Result.ACTIVE)) {
            outroTicks = 30;
            if (client.player != null) {
                client.player.sendOverlayMessage(
                        Component.translatable(
                                "result.fishr."
                                        + payload.result()
                                                .name()
                                                .toLowerCase(java.util.Locale.ROOT)));
            }
            if (payload.result() == FishingBattle.Result.CAUGHT
                    && !payload.has(BattlePayload.PRACTICE)) {
                trophy = payload;
                trophyTicks = TROPHY_TICKS;
            }
        }
        state = payload;
        packetAge = 0;
    }

    private static void tick(Minecraft client) {
        ticks++;
        if (trophyTicks > 0) trophyTicks--;
        // Die Klick-Warteschlangen leeren, obwohl diese Tasten den Gedrückt-Zustand nutzen.
        while (reelKey.consumeClick()) {}
        while (giveKey.consumeClick()) {}
        if (client.player == null || client.level == null) {
            clear();
            return;
        }
        if (state == null) return;
        fightTicks++;
        displayedTension += (state.tension() - displayedTension) * 0.5f;
        displayedStamina += (state.stamina() - displayedStamina) * 0.4f;
        if (state.result() != FishingBattle.Result.ACTIVE) {
            if (--outroTicks <= 0) state = null;
            return;
        }
        if (!client.isPaused() && ++packetAge > 100) {
            clear();
            return;
        }
        boolean free = client.gui.screen() == null;
        boolean nextGiving = free && giveKey.isDown();
        boolean nextReeling = free && reelKey.isDown() && !nextGiving;
        if ((nextReeling != reeling || nextGiving != giving || ticks % 4 == 0)
                && ClientPlayNetworking.canSend(ReelPayload.TYPE)) {
            ClientPlayNetworking.send(
                    new ReelPayload(state.battleId(), nextReeling, nextGiving, false));
        }
        reeling = nextReeling;
        giving = nextGiving;
        sounds(client);
    }

    static boolean reeling() {
        return reeling;
    }

    static boolean giving() {
        return giving;
    }

    /**
     * Auch die Schnur hat eine Stimme: Knarzen bei steigender Spannung, die Ratsche der Rolle, eine
     * kreischende Bremse, ein schlaffes Platschen.
     */
    private static void sounds(Minecraft client) {
        var player = client.player;
        float tension = displayedTension;
        int every = tension >= 0.6f ? 4 : tension >= 0.45f ? 8 : 0;
        if (every > 0 && ticks % every == 0)
            player.playSound(FishrSounds.CREAK, 0.2f + tension / 2.5f, 0.8f + tension / 2.5f);
        boolean slipping =
                tension >= state.drag() * 0.97f
                        && (state.action() == FishAction.RUN
                                || state.action() == FishAction.DIVE
                                || state.action() == FishAction.SIDE)
                        && state.pending() == null;
        if ((slipping || giving) && ticks % 2 == 0)
            player.playSound(FishrSounds.REEL, 0.45f, 1.6f + player.getRandom().nextFloat() * 0.2f);
        else if (reeling && ticks % 2 == 0)
            player.playSound(FishrSounds.REEL, 0.35f, 0.9f + (1 - displayedStamina) * 0.25f);
        if (state.has(BattlePayload.SLACK) && ticks % 12 == 0)
            player.playSound(FishrSounds.SLACK, 0.4f, 1.0f);
    }

    private static void clear() {
        state = null;
        reeling = giving = false;
        packetAge = 0;
        outroTicks = 0;
    }
}
