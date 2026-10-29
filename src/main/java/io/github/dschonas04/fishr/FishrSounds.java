package io.github.dschonas04.fishr;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * The fishing sounds, synthesised by scripts/sounds.py; files in assets/fishr/sounds, listed in
 * sounds.json.
 */
public final class FishrSounds {
    private FishrSounds() {}

    public static final SoundEvent BITE = register("bite");
    public static final SoundEvent HOOK_SET = register("hook_set");
    public static final SoundEvent WARNING = register("warning");
    public static final SoundEvent REEL = register("reel");
    public static final SoundEvent CREAK = register("creak");
    public static final SoundEvent SLACK = register("slack");
    public static final SoundEvent LEAP = register("leap");
    public static final SoundEvent SNAP = register("snap");
    public static final SoundEvent CATCH = register("catch");
    public static final SoundEvent ESCAPE = register("escape");
    public static final SoundEvent BAIT = register("bait");

    private static SoundEvent register(String name) {
        Identifier id = Fishr.id(name);
        return Registry.register(
                BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    /** Loads the class, which registers the sounds. */
    public static void initialize() {}
}
