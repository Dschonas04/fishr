// Autor: Dschonas04 (https://github.com/Dschonas04) – Fishr, Weiterführung von Atzen (jojiuchiha03)

package io.github.dschonas04.fishr.progress;

import io.github.dschonas04.fishr.fish.FishSpecies;

/**
 * Was ein Fang wert ist. Erfahrung und Marktpreis wachsen mit Seltenheit, Größe, der Kontrolle im
 * Drill und dem Zustand des Fisches; dieselbe Art am selben Tag ist jedes Mal weniger wert, damit
 * das Abfarmen eines kleinen Fisches nie der beste Weg ist.
 */
public final class Valuation {
    private Valuation() {}

    private static final double[] RARITY_XP = {5, 10, 25, 50, 100, 200};
    private static final double[] RARITY_PRICE = {0.25, 0.5, 1.2, 3, 8, 16};

    /** Abnehmender Ertrag für den n-ten Fisch einer Art heute (n beginnt bei 0). */
    public static double repetition(int sameToday) {
        return Math.pow(0.8, Math.max(0, sameToday));
    }

    public static int experience(
            FishSpecies.Rarity rarity, double sizeFraction, int quality, int sameToday) {
        double size = 0.6 + Math.clamp(sizeFraction, 0, 1);
        double control = 0.7 + 0.6 * Math.clamp(quality, 0, 100) / 100.0;
        return (int)
                Math.max(
                        1,
                        Math.round(
                                RARITY_XP[rarity.ordinal()]
                                        * size
                                        * control
                                        * repetition(sameToday)));
    }

    /**
     * Smaragde (mit Bruchteilen, der Markt behält das Wechselgeld) für einen Fisch dieser
     * Seltenheit, dieses Gewichts, dieser Qualität und dieses Zustands.
     */
    public static double price(
            FishSpecies.Rarity rarity,
            double weightKg,
            int quality,
            double condition,
            int soldToday) {
        double weight = Math.sqrt(Math.max(0.05, weightKg));
        double control = quality < 0 ? 0.8 : 0.7 + 0.6 * Math.clamp(quality, 0, 100) / 100.0;
        double fresh = 0.4 + 0.6 * Math.clamp(condition, 0, 100) / 100.0;
        return RARITY_PRICE[rarity.ordinal()] * weight * control * fresh * repetition(soldToday);
    }

    /** Anglerstufe aus der Erfahrung: 0, 40, 160, 360, 640 … (Stufe² × 40). */
    public static int level(long experience) {
        return (int) Math.floor(Math.sqrt(Math.max(0, experience) / 40.0));
    }

    public static long experienceFor(int level) {
        return 40L * level * level;
    }
}
