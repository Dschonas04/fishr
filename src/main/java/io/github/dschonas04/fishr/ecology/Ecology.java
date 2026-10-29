// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr.ecology;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.dschonas04.fishr.Fishr;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Die vereinfachte Gewässerökologie (Anforderung 17), je Chunk gespeichert, damit sie dort nichts
 * kostet, wo niemand angelt:
 *
 * <ul>
 *   <li>Fangdruck: Jeder aus einem Chunk entnommene Fisch erhöht ihn, mit der Zeit sinkt er wieder
 *       (siehe {@link Pressure}). Solange er hoch ist, werden Bisse und neue Fische seltener; die
 *       Fische dort sind scheuer.
 *   <li>Futterplätze: Futtermischung lockt Friedfische an und lässt sie eine Weile schneller
 *       beißen.
 * </ul>
 *
 * Raubfische fressen kleinere Fische, und Schwärme ziehen mit der Tageszeit (siehe das Verhalten
 * der Fische); was in einem Gewässerabschnitt lebt, ändert sich also mit Angeln, Futter und Räubern
 * und erholt sich von selbst.
 */
public final class Ecology {
    private Ecology() {}

    /** Fangdruck eines Chunks und wann er zuletzt aktualisiert wurde (Spielzeit). */
    public record ChunkPressure(double value, long time) {
        public static final Codec<ChunkPressure> CODEC =
                RecordCodecBuilder.create(
                        instance ->
                                instance.group(
                                                Codec.DOUBLE
                                                        .fieldOf("value")
                                                        .forGetter(ChunkPressure::value),
                                                Codec.LONG
                                                        .fieldOf("time")
                                                        .forGetter(ChunkPressure::time))
                                        .apply(instance, ChunkPressure::new));
    }

    public static final AttachmentType<ChunkPressure> PRESSURE =
            AttachmentRegistry.createPersistent(Fishr.id("pressure"), ChunkPressure.CODEC);

    private record Chum(BlockPos pos, long until) {}

    private static final Map<ResourceKey<Level>, List<Chum>> CHUM = new HashMap<>();

    public static void initialize() {}

    public static double pressure(ServerLevel level, BlockPos pos) {
        LevelChunk chunk = level.getChunkAt(pos);
        ChunkPressure stored = chunk.getAttached(PRESSURE);
        return stored == null
                ? 0
                : Pressure.decayed(stored.value(), level.getGameTime() - stored.time());
    }

    /** Hier wurde ein Fisch entnommen. */
    public static void caught(ServerLevel level, BlockPos pos, boolean released) {
        double now = Pressure.afterCatch(pressure(level, pos), released);
        level.getChunkAt(pos).setAttached(PRESSURE, new ChunkPressure(now, level.getGameTime()));
    }

    /**
     * Hier wurde ein Fisch zurückgesetzt: Die Hälfte dessen, was sein Fang das Gewässer gekostet
     * hat, kommt zurück.
     */
    public static void released(ServerLevel level, BlockPos pos) {
        double now = Math.max(0, pressure(level, pos) - Pressure.PER_CATCH / 2);
        level.getChunkAt(pos).setAttached(PRESSURE, new ChunkPressure(now, level.getGameTime()));
    }

    /** Faktor auf Bisse und Spawns hier. */
    public static double factor(ServerLevel level, BlockPos pos) {
        return Pressure.biteFactor(pressure(level, pos));
    }

    /** Hier wurde Futtermischung geworfen: ein Futterplatz für fünf Minuten. */
    public static void chum(ServerLevel level, BlockPos pos, int ticks) {
        List<Chum> spots = CHUM.computeIfAbsent(level.dimension(), key -> new ArrayList<>());
        spots.removeIf(spot -> spot.until() < level.getGameTime());
        if (spots.size() > 256) spots.removeFirst();
        spots.add(new Chum(pos.immutable(), level.getGameTime() + ticks));
    }

    public static BlockPos nearestChum(ServerLevel level, BlockPos pos, int radius) {
        List<Chum> spots = CHUM.get(level.dimension());
        if (spots == null) return null;
        BlockPos best = null;
        double bestDistance = radius * radius;
        for (Chum spot : spots) {
            if (spot.until() < level.getGameTime()) continue;
            double distance = spot.pos().distSqr(pos);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = spot.pos();
            }
        }
        return best;
    }

    /** In der Nähe eines Futterplatzes beißen Friedfische bereitwilliger. */
    public static boolean nearChum(ServerLevel level, BlockPos pos) {
        return nearestChum(level, pos, 10) != null;
    }
}
