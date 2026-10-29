package io.github.dschonas04.fishr.network;

import io.github.dschonas04.fishr.Fishr;
import io.github.dschonas04.fishr.battle.FishAction;
import io.github.dschonas04.fishr.battle.FishingBattle;
import io.github.dschonas04.fishr.fish.FishSpecies;
import io.github.dschonas04.fishr.fish.FishSpecimen;
import io.github.dschonas04.fishr.fish.FishVariant;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Der Zustand eines Drills, wie ihn der Client des Anglers für Anzeige und Kamera braucht. Spannung
 * und Bremse sind Anteile der Tragkraft der Schnur; Entfernungen in Metern.
 */
public record BattlePayload(
        int battleId,
        FishingBattle.Result result,
        FishAction action,
        FishAction pending,
        float tension,
        float drag,
        float stamina,
        float distance,
        float lineOut,
        float capacity,
        float hookHold,
        int sideDir,
        int flags,
        int secondsLeft,
        int quality,
        FishSpecies species,
        float lengthCm,
        float weightKg,
        int bossPhase,
        int fishEntityId,
        int strike,
        int landTicks,
        float netWindow,
        FishVariant variant)
        implements CustomPacketPayload {
    public static final int SLACK = 1,
            LANDABLE = 2,
            PRACTICE = 8,
            FAKING = 16,
            TWIST = 32,
            GIVING = 64;
    public static final Type<BattlePayload> TYPE = new Type<>(Fishr.id("battle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BattlePayload> CODEC =
            new StreamCodec<>() {
                public BattlePayload decode(RegistryFriendlyByteBuf buf) {
                    int id = buf.readVarInt();
                    var result = buf.readEnum(FishingBattle.Result.class);
                    var action = buf.readEnum(FishAction.class);
                    int pending = buf.readVarInt();
                    return new BattlePayload(
                            id,
                            result,
                            action,
                            pending == 0
                                    ? null
                                    : FishAction.values()[
                                            Math.clamp(
                                                    pending - 1,
                                                    0,
                                                    FishAction.values().length - 1)],
                            buf.readFloat(),
                            buf.readFloat(),
                            buf.readFloat(),
                            buf.readFloat(),
                            buf.readFloat(),
                            buf.readFloat(),
                            buf.readFloat(),
                            buf.readByte(),
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readEnum(FishSpecies.class),
                            buf.readFloat(),
                            buf.readFloat(),
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readFloat(),
                            buf.readEnum(FishVariant.class));
                }

                public void encode(RegistryFriendlyByteBuf buf, BattlePayload value) {
                    buf.writeVarInt(value.battleId());
                    buf.writeEnum(value.result());
                    buf.writeEnum(value.action());
                    buf.writeVarInt(value.pending() == null ? 0 : value.pending().ordinal() + 1);
                    buf.writeFloat(value.tension());
                    buf.writeFloat(value.drag());
                    buf.writeFloat(value.stamina());
                    buf.writeFloat(value.distance());
                    buf.writeFloat(value.lineOut());
                    buf.writeFloat(value.capacity());
                    buf.writeFloat(value.hookHold());
                    buf.writeByte(value.sideDir());
                    buf.writeVarInt(value.flags());
                    buf.writeVarInt(value.secondsLeft());
                    buf.writeVarInt(value.quality());
                    buf.writeEnum(value.species());
                    buf.writeFloat(value.lengthCm());
                    buf.writeFloat(value.weightKg());
                    buf.writeVarInt(value.bossPhase());
                    buf.writeVarInt(value.fishEntityId());
                    buf.writeVarInt(value.strike());
                    buf.writeVarInt(value.landTicks());
                    buf.writeFloat(value.netWindow());
                    buf.writeEnum(value.variant());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public boolean has(int flag) {
        return (flags & flag) != 0;
    }

    public static BattlePayload of(
            int id,
            FishingBattle battle,
            boolean practice,
            FishSpecimen specimen,
            int fishEntityId,
            int strike,
            float netWindow) {
        double strength = battle.rig().lineStrength();
        int flags =
                (battle.slack() ? SLACK : 0)
                        | (battle.landable() ? LANDABLE : 0)
                        | (practice ? PRACTICE : 0)
                        | (battle.faking() ? FAKING : 0)
                        | (battle.twisting() ? TWIST : 0)
                        | (battle.giving() ? GIVING : 0);
        return new BattlePayload(
                id,
                battle.result(),
                battle.action(),
                battle.pending(),
                (float) (battle.tension() / strength),
                (float) (battle.dragKg() / strength),
                (float) battle.stamina(),
                (float) battle.distance(),
                (float) battle.distance(),
                (float) battle.rig().capacity(),
                (float) battle.hookHold(),
                battle.sideDir(),
                flags,
                Math.max(0, (battle.maxTicks() - battle.elapsed() + 19) / 20),
                battle.quality(),
                specimen.species(),
                (float) specimen.lengthCm(),
                (float) specimen.weightKg(),
                battle.bossPhase(),
                fishEntityId,
                strike,
                battle.landableTicks(),
                netWindow,
                specimen.variant());
    }
}
