package io.github.dschonas04.fishr.holding;

import java.util.ArrayList;
import java.util.List;

/**
 * Das Wasser, in dem ein gehaltener Fisch lebt, jeder Wert 0 (schlecht) .. 1 (gut): genug Wasser
 * ringsum, Sauerstoff (Strömung, Pflanzen, wenige Fische), eine Temperatur, die zur Art passt, eine
 * Besatzdichte, die Platz lässt, und eine Wasserqualität, die mit Enge und Zeit sinkt. Schlechtes
 * Wasser stresst Fische zuerst (sie verlieren an Zustand), erst Vernachlässigung tötet sie.
 */
public record WaterConditions(
        double volume, double oxygen, double temperature, double density, double quality) {
    public WaterConditions {
        volume = clamp(volume);
        oxygen = clamp(oxygen);
        temperature = clamp(temperature);
        density = clamp(density);
        quality = clamp(quality);
    }

    private static double clamp(double value) {
        return Double.isFinite(value) ? Math.clamp(value, 0, 1) : 0;
    }

    /**
     * Der schlechteste Wert zählt am meisten: Ein schlechter Faktor genügt, um einen Fisch zu
     * stressen.
     */
    public double score() {
        double worst =
                Math.min(
                        Math.min(volume, oxygen),
                        Math.min(Math.min(temperature, density), quality));
        double mean = (volume + oxygen + temperature + density + quality) / 5;
        return worst * 0.6 + mean * 0.4;
    }

    /** Zustandsänderung pro Minute: Gutes Wasser heilt langsam, schlechtes stresst schnell. */
    public double perMinute() {
        double score = score();
        return score >= 0.6 ? 3 * (score - 0.6) / 0.4 : -12 * (0.6 - score) / 0.6;
    }

    /**
     * Übersetzungsschlüssel dessen, was nicht stimmt, das Schlimmste zuerst, damit der Spieler es
     * früh sieht.
     */
    public List<String> problems() {
        List<String> problems = new ArrayList<>();
        if (volume < 0.5) problems.add("holding.fishr.problem.volume");
        if (oxygen < 0.5) problems.add("holding.fishr.problem.oxygen");
        if (temperature < 0.5) problems.add("holding.fishr.problem.temperature");
        if (density < 0.5) problems.add("holding.fishr.problem.density");
        if (quality < 0.5) problems.add("holding.fishr.problem.quality");
        return problems;
    }
}
