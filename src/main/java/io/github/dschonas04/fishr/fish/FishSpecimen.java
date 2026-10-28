package io.github.dschonas04.fishr.fish;

import io.github.dschonas04.fishr.battle.FishStyle;
import io.github.dschonas04.fishr.battle.Fighter;

public record FishSpecimen(FishSpecies species, double lengthCm, double weightKg, FishVariant variant) {
    public FishSpecimen {
        if (species == null || !Double.isFinite(lengthCm) || !Double.isFinite(weightKg)
                || lengthCm < species.minLength() || lengthCm > species.maxLength() || weightKg <= 0) {
            throw new IllegalArgumentException("Invalid fish measurements");
        }
        if (variant == null) variant = FishVariant.NORMAL;
    }

    public FishSpecimen(FishSpecies species, double lengthCm, double weightKg) { this(species, lengthCm, weightKg, FishVariant.NORMAL); }

    public FishSpecimen withVariant(FishVariant colouring) { return new FishSpecimen(species, lengthCm, weightKg, colouring); }

    /**
     * The same fish, grown (fed with worms, as in Fish of Thieves): longer by a share of its species' range, never
     * beyond the largest; the weight follows the length cubed.
     */
    public FishSpecimen grown(double share) {
        double length = Math.min(species.maxLength(), lengthCm + (species.maxLength() - species.minLength()) * share);
        length = Math.round(length * 10) / 10.0;
        double weight = Math.round(weightKg * Math.pow(length / lengthCm, 3) * 100) / 100.0;
        return new FishSpecimen(species, length, Math.max(0.01, weight), variant);
    }

    public double sizeFraction() { return (lengthCm - species.minLength()) / (species.maxLength() - species.minLength()); }
    /** The largest fish of a species: the top 15 % of its length range. */
    public boolean trophy() { return sizeFraction() >= 0.85; }

    public String sizeKey() {
        return "size.fishr." + (sizeFraction() >= 0.85 ? "trophy" : sizeFraction() >= 0.60 ? "large"
                : sizeFraction() >= 0.25 ? "normal" : "small");
    }

    /**
     * How this fish fights. Strength grows with weight (a 1 kg perch pulls about 1.5 kg, a 12 kg carp about
     * 7 kg, a 50 kg wels about 15 kg, a 200 kg tuna about 24 kg), endurance and speed more slowly; the fighting style shifts all three.
     */
    public Fighter fighter() {
        if (species.boss() != null) return Fighter.of(species.boss(), weightKg);
        FishStyle style = species.style();
        double power = (0.6 + 0.9 * Math.pow(weightKg, 0.75) / (1 + weightKg / 150)) * switch (style) {
            case DARTER -> 0.85; case FIGHTER -> 1.0; case BRUISER -> 1.15;
        };
        double endurance = (0.6 + 0.3 * Math.pow(weightKg, 0.33)) * switch (style) {
            case DARTER -> 0.9; case FIGHTER -> 1.0; case BRUISER -> 1.15;
        };
        double speed = (0.12 + 0.04 * Math.log1p(weightKg)) * switch (style) {
            case DARTER -> 1.3; case FIGHTER -> 1.1; case BRUISER -> 0.9;
        };
        return new Fighter(power, endurance, speed, weightKg, species.actionWeights(), species.diet().strikeIdeal(),
                species.salt(), species.teeth(), null);
    }
}
