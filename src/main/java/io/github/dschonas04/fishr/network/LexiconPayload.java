// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr.network;

import io.github.dschonas04.fishr.Fishr;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Alles, was das Fischlexikon zeigt: der Fortschritt des Anglers, die heutigen Aufträge, Rekorde,
 * Rangliste und Turnier.
 */
public record LexiconPayload(CompoundTag data) implements CustomPacketPayload {
    public static final Type<LexiconPayload> TYPE = new Type<>(Fishr.id("lexicon"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LexiconPayload> CODEC =
            ByteBufCodecs.COMPOUND_TAG.map(LexiconPayload::new, LexiconPayload::data).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
