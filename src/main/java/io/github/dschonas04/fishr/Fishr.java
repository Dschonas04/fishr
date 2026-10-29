// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr;

import io.github.dschonas04.fishr.battle.BattleManager;
import io.github.dschonas04.fishr.block.FishrBlocks;
import io.github.dschonas04.fishr.ecology.Ecology;
import io.github.dschonas04.fishr.fish.FishrFishRegistry;
import io.github.dschonas04.fishr.holding.FishRelease;
import io.github.dschonas04.fishr.item.FishrItems;
import io.github.dschonas04.fishr.network.BattlePayload;
import io.github.dschonas04.fishr.network.LexiconPayload;
import io.github.dschonas04.fishr.network.ReelPayload;
import io.github.dschonas04.fishr.progress.Progression;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Fishr implements ModInitializer {
    public static final String MOD_ID = "fishr";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final BattleManager BATTLES = new BattleManager();

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        FishrSounds.initialize();
        ModItems.register();
        FishrItems.initialize();
        FishrFishRegistry.initialize();
        FishrBlocks.initialize();
        Ecology.initialize();
        Progression.initialize();
        PayloadTypeRegistry.serverboundPlay().register(ReelPayload.TYPE, ReelPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BattlePayload.TYPE, BattlePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(LexiconPayload.TYPE, LexiconPayload.CODEC);
        FishRelease.register();
        io.github.dschonas04.fishr.item.BaitFinds.register();
        ServerPlayNetworking.registerGlobalReceiver(
                ReelPayload.TYPE, (payload, context) -> BATTLES.input(context.player(), payload));
        ServerTickEvents.END_SERVER_TICK.register(BATTLES::tick);
        ServerTickEvents.END_SERVER_TICK.register(server -> Progression.tick(server, false));
        ServerLifecycleEvents.SERVER_STARTED.register(Progression::load);
        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> BATTLES.disconnect(handler.getPlayer()));
        // Bevor die Welt gespeichert wird: Gehakte Fische bekommen ihre KI zurück, Drill-Attrappen
        // verschwinden.
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> BATTLES.clear());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> BATTLES.clear());
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, access, environment) -> BATTLES.registerCommands(dispatcher));
        LOGGER.info("Fishr geladen: fishing battles initialized for Minecraft 26.2");
    }
}
