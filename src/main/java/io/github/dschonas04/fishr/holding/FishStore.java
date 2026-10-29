package io.github.dschonas04.fishr.holding;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.dschonas04.fishr.fish.FishSpecies;
import io.github.dschonas04.fishr.fish.FishSpecimen;
import io.github.dschonas04.fishr.fish.FishVariant;

import net.minecraft.core.UUIDUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Die Fische in einem Setzkescher: begrenzt in Anzahl, Gesamtgewicht und Größe. Jeder Fisch behält
 * seine Identität (eine Kennung, seine Maße, seinen Zustand); derselbe Fisch kann nie doppelt
 * liegen, damit nichts verdoppelt wird, wenn Fische zwischen Welt, Hand und Kescher wechseln.
 */
public final class FishStore {
    /** Ein gehaltener Fisch. Zustand 0..100; bei 0 ist er tot. */
    public record Kept(
            UUID id,
            FishSpecies species,
            double lengthCm,
            double weightKg,
            double condition,
            int quality,
            String angler,
            FishVariant variant) {
        public Kept(
                UUID id,
                FishSpecies species,
                double lengthCm,
                double weightKg,
                double condition,
                int quality,
                String angler) {
            this(id, species, lengthCm, weightKg, condition, quality, angler, FishVariant.NORMAL);
        }

        public static final Codec<FishSpecies> SPECIES =
                Codec.STRING.comapFlatMap(
                        id -> {
                            for (FishSpecies species : FishSpecies.values())
                                if (species.id().equals(id)) return DataResult.success(species);
                            return DataResult.error(() -> "Unknown fish " + id);
                        },
                        FishSpecies::id);
        public static final Codec<Kept> CODEC =
                RecordCodecBuilder.create(
                        instance ->
                                instance.group(
                                                UUIDUtil.STRING_CODEC
                                                        .fieldOf("id")
                                                        .forGetter(Kept::id),
                                                SPECIES.fieldOf("species").forGetter(Kept::species),
                                                Codec.DOUBLE
                                                        .fieldOf("length")
                                                        .forGetter(Kept::lengthCm),
                                                Codec.DOUBLE
                                                        .fieldOf("weight")
                                                        .forGetter(Kept::weightKg),
                                                Codec.DOUBLE
                                                        .fieldOf("condition")
                                                        .forGetter(Kept::condition),
                                                Codec.INT
                                                        .optionalFieldOf("quality", -1)
                                                        .forGetter(Kept::quality),
                                                Codec.STRING
                                                        .optionalFieldOf("angler", "")
                                                        .forGetter(Kept::angler),
                                                Codec.STRING
                                                        .xmap(FishVariant::byId, FishVariant::id)
                                                        .optionalFieldOf(
                                                                "variant", FishVariant.NORMAL)
                                                        .forGetter(Kept::variant))
                                        .apply(instance, Kept::new));

        public Kept withCondition(double value) {
            return new Kept(
                    id,
                    species,
                    lengthCm,
                    weightKg,
                    Math.clamp(value, 0, 100),
                    quality,
                    angler,
                    variant);
        }

        public FishSpecimen specimen() {
            return new FishSpecimen(species, lengthCm, weightKg, variant);
        }
    }

    public enum Add {
        OK,
        FULL,
        TOO_HEAVY,
        TOO_BIG,
        DUPLICATE,
        DEAD
    }

    private final int capacity;
    private final double maxWeight, maxLength;
    private final List<Kept> fish = new ArrayList<>();

    public FishStore(int capacity, double maxWeight, double maxLength) {
        this.capacity = capacity;
        this.maxWeight = maxWeight;
        this.maxLength = maxLength;
    }

    public Add add(Kept kept) {
        if (kept.condition() <= 0) return Add.DEAD;
        if (fish.stream().anyMatch(other -> other.id().equals(kept.id()))) return Add.DUPLICATE;
        if (fish.size() >= capacity) return Add.FULL;
        if (kept.lengthCm() > maxLength) return Add.TOO_BIG;
        if (totalWeight() + kept.weightKg() > maxWeight) return Add.TOO_HEAVY;
        fish.add(kept);
        return Add.OK;
    }

    public Kept takeLast() {
        return fish.isEmpty() ? null : fish.removeLast();
    }

    public List<Kept> takeAll() {
        List<Kept> all = List.copyOf(fish);
        fish.clear();
        return all;
    }

    public List<Kept> fish() {
        return Collections.unmodifiableList(fish);
    }

    public int size() {
        return fish.size();
    }

    public int capacity() {
        return capacity;
    }

    public double maxWeight() {
        return maxWeight;
    }

    public double totalWeight() {
        return fish.stream().mapToDouble(Kept::weightKg).sum();
    }

    public double density() {
        return Math.max(fish.size() / (double) capacity, totalWeight() / maxWeight);
    }

    public double averageCondition() {
        return fish.stream().mapToDouble(Kept::condition).average().orElse(100);
    }

    /** Lässt in diesem Wasser Zeit vergehen; gibt die Fische zurück, die gestorben sind. */
    public List<Kept> age(WaterConditions water, double minutes) {
        List<Kept> dead = new ArrayList<>();
        double change = water.perMinute() * minutes;
        for (int i = fish.size() - 1; i >= 0; i--) {
            Kept kept = fish.get(i).withCondition(fish.get(i).condition() + change);
            if (kept.condition() <= 0) dead.add(fish.remove(i));
            else fish.set(i, kept);
        }
        return dead;
    }

    /**
     * Stellt gespeicherte Fische wieder her (aus den Speicherdaten der Block-Entity); überspringt
     * Doppelte.
     */
    public void restore(Kept kept) {
        if (fish.stream().noneMatch(other -> other.id().equals(kept.id()))) fish.add(kept);
    }
}
