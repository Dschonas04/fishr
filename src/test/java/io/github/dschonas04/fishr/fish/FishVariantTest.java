package io.github.dschonas04.fishr.fish;

import io.github.dschonas04.fishr.block.FilletTableBlock;
import io.github.dschonas04.fishr.holding.FishStore;
import io.github.dschonas04.fishr.item.BaitType;
import com.mojang.serialization.JsonOps;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FishVariantTest {
    private static Map<FishVariant, Integer> count(FishVariant.Conditions conditions) {
        Random random = new Random(16);
        Map<FishVariant, Integer> counts = new EnumMap<>(FishVariant.class);
        for (int i = 0; i < 20000; i++) counts.merge(FishVariant.pick(conditions, random), 1, Integer::sum);
        return counts;
    }

    @Test
    void colouringsFollowSkyTimeAndWeather() {
        var day = count(new FishVariant.Conditions(true, true, false, false, false));
        assertFalse(day.containsKey(FishVariant.MOONLIT));
        assertFalse(day.containsKey(FishVariant.STORM));
        assertTrue(day.getOrDefault(FishVariant.SUNLIT, 0) > 1500);
        var night = count(new FishVariant.Conditions(true, false, false, false, false));
        var fullMoon = count(new FishVariant.Conditions(true, false, false, true, false));
        assertFalse(night.containsKey(FishVariant.SUNLIT));
        assertTrue(fullMoon.get(FishVariant.MOONLIT) > night.get(FishVariant.MOONLIT) * 1.6);
        var storm = count(new FishVariant.Conditions(true, false, true, false, false));
        assertTrue(storm.get(FishVariant.STORM) > 6000);
        var cave = count(new FishVariant.Conditions(false, true, true, false, true));
        assertEquals(Map.of(), Map.copyOf(cave).entrySet().stream()
                .filter(e -> e.getKey() != FishVariant.NORMAL && e.getKey() != FishVariant.SHADOW && e.getKey() != FishVariant.ALBINO)
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        // albinos are rare everywhere, plain fish the most common
        for (var counts : java.util.List.of(day, night, storm, cave)) {
            assertTrue(counts.get(FishVariant.ALBINO) > 200 && counts.get(FishVariant.ALBINO) < 700);
            assertTrue(counts.get(FishVariant.NORMAL) > 10000);
        }
    }

    @Test
    void rareColouringsAreWorthMore() {
        for (FishVariant variant : FishVariant.values()) {
            if (variant != FishVariant.NORMAL) assertTrue(variant.worth() > FishVariant.NORMAL.worth(), variant.name());
            assertEquals(variant, FishVariant.byId(variant.id()));
        }
        assertEquals(FishVariant.NORMAL, FishVariant.byId("unknown"));
    }

    @Test
    void feedingGrowsUpToTheLargestAndKeepsTheColouring() {
        FishSpecimen fish = new FishSpecimen(FishSpecies.CARP, 30, 0.7, FishVariant.MOONLIT);
        FishSpecimen grown = fish.grown(0.05);
        assertTrue(grown.lengthCm() > fish.lengthCm());
        assertTrue(grown.weightKg() > fish.weightKg());
        assertEquals(FishVariant.MOONLIT, grown.variant());
        int feedings = 0;
        while (!fish.trophy()) {
            fish = fish.grown(0.05);
            feedings++;
        }
        assertTrue(feedings > 10 && feedings < 25, "feedings " + feedings);
        for (int i = 0; i < 30; i++) fish = fish.grown(0.05);
        assertEquals(FishSpecies.CARP.maxLength(), fish.lengthCm());
    }

    @Test
    void keptFishKeepTheirColouring() {
        var kept = new FishStore.Kept(UUID.randomUUID(), FishSpecies.PIKE, 80, 4, 90, 70, "Tester", FishVariant.STORM);
        var json = FishStore.Kept.CODEC.encodeStart(JsonOps.INSTANCE, kept).getOrThrow();
        assertEquals(kept, FishStore.Kept.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
        json.getAsJsonObject().remove("variant");
        assertEquals(FishVariant.NORMAL, FishStore.Kept.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow().variant());
        assertEquals(FishVariant.STORM, kept.withCondition(50).variant());
    }

    @Test
    void filletsGrowWithWeight() {
        assertEquals(1, FilletTableBlock.fillets(0.1));
        assertTrue(FilletTableBlock.fillets(5) > FilletTableBlock.fillets(1));
        assertEquals(12, FilletTableBlock.fillets(300));
    }

    @Test
    void foundBaitsSuitTheirFish() {
        assertTrue(BaitType.CATERPILLAR.appeal(FishSpecies.RIVER_TROUT) > BaitType.WORM.appeal(FishSpecies.RIVER_TROUT));
        assertTrue(BaitType.LEECH.appeal(FishSpecies.CATFISH) > BaitType.WORM.appeal(FishSpecies.CATFISH));
        assertTrue(BaitType.GRUB.appeal(FishSpecies.ROACH) > BaitType.WORM.appeal(FishSpecies.ROACH));
        assertTrue(BaitType.LEECH.sizeBias() > BaitType.GRUB.sizeBias());
        for (BaitType bait : BaitType.values()) {
            assertEquals(bait.natural(), !bait.lure() && bait.boss() == null && bait != BaitType.GLOW, bait.name());
        }
    }
}
