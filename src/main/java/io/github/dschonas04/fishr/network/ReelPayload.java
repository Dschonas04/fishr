// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr.network;

import io.github.dschonas04.fishr.Fishr;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Was der Angler tut: einholen (R), Schnur geben (F) oder aufgeben. */
public record ReelPayload(int battleId, boolean reeling, boolean giving, boolean cancel)
        implements CustomPacketPayload {
    public static final Type<ReelPayload> TYPE = new Type<>(Fishr.id("reel"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReelPayload> CODEC =
            new StreamCodec<>() {
                public ReelPayload decode(RegistryFriendlyByteBuf buf) {
                    return new ReelPayload(
                            buf.readVarInt(),
                            buf.readBoolean(),
                            buf.readBoolean(),
                            buf.readBoolean());
                }

                public void encode(RegistryFriendlyByteBuf buf, ReelPayload value) {
                    buf.writeVarInt(value.battleId());
                    buf.writeBoolean(value.reeling());
                    buf.writeBoolean(value.giving());
                    buf.writeBoolean(value.cancel());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
