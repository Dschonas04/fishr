// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr.fish;

import io.github.dschonas04.fishr.battle.Fighter;
import io.github.dschonas04.fishr.battle.FishStyle;

public record FishSpecimen(
        FishSpecies species, double lengthCm, double weightKg, FishVariant variant) {
    public FishSpecimen {
        if (species == null
                || !Double.isFinite(lengthCm)
                || !Double.isFinite(weightKg)
                || lengthCm < species.minLength()
                || lengthCm > species.maxLength()
                || weightKg <= 0) {
            throw new IllegalArgumentException("Invalid fish measurements");
        }
        if (variant == null) variant = FishVariant.NORMAL;
    }

    public FishSpecimen(FishSpecies species, double lengthCm, double weightKg) {
        this(species, lengthCm, weightKg, FishVariant.NORMAL);
    }

    public FishSpecimen withVariant(FishVariant colouring) {
        return new FishSpecimen(species, lengthCm, weightKg, colouring);
    }

    /**
     * Derselbe Fisch, gewachsen (mit Würmern gefüttert, wie in Fish of Thieves): um einen Anteil
     * des Größenbereichs seiner Art länger, nie über den größten hinaus; das Gewicht folgt der
     * Länge hoch drei.
     */
    public FishSpecimen grown(double share) {
        double length =
                Math.min(
                        species.maxLength(),
                        lengthCm + (species.maxLength() - species.minLength()) * share);
        length = Math.round(length * 10) / 10.0;
        double weight = Math.round(weightKg * Math.pow(length / lengthCm, 3) * 100) / 100.0;
        return new FishSpecimen(species, length, Math.max(0.01, weight), variant);
    }

    public double sizeFraction() {
        return (lengthCm - species.minLength()) / (species.maxLength() - species.minLength());
    }

    /** Die größten Fische einer Art: die oberen 15 % ihres Längenbereichs. */
    public boolean trophy() {
        return sizeFraction() >= 0.85;
    }

    public String sizeKey() {
        return "size.fishr."
                + (sizeFraction() >= 0.85
                        ? "trophy"
                        : sizeFraction() >= 0.60
                                ? "large"
                                : sizeFraction() >= 0.25 ? "normal" : "small");
    }

    /**
     * Wie dieser Fisch kämpft. Die Kraft wächst mit dem Gewicht (ein Barsch von 1 kg zieht etwa 1,5
     * kg, ein Karpfen von 12 kg etwa 7 kg, ein Wels von 50 kg etwa 15 kg, ein Thunfisch von 200 kg
     * etwa 24 kg), Ausdauer und Geschwindigkeit langsamer; der Kampfstil verschiebt alle drei.
     */
    public Fighter fighter() {
        if (species.boss() != null) return Fighter.of(species.boss(), weightKg);
        FishStyle style = species.style();
        double power =
                (0.6 + 0.9 * Math.pow(weightKg, 0.75) / (1 + weightKg / 150))
                        * switch (style) {
                            case DARTER -> 0.85;
                            case FIGHTER -> 1.0;
                            case BRUISER -> 1.15;
                        };
        double endurance =
                (0.6 + 0.3 * Math.pow(weightKg, 0.33))
                        * switch (style) {
                            case DARTER -> 0.9;
                            case FIGHTER -> 1.0;
                            case BRUISER -> 1.15;
                        };
        double speed =
                (0.12 + 0.04 * Math.log1p(weightKg))
                        * switch (style) {
                            case DARTER -> 1.3;
                            case FIGHTER -> 1.1;
                            case BRUISER -> 0.9;
                        };
        return new Fighter(
                power,
                endurance,
                speed,
                weightKg,
                species.actionWeights(),
                species.diet().strikeIdeal(),
                species.salt(),
                species.teeth(),
                null);
    }
}
