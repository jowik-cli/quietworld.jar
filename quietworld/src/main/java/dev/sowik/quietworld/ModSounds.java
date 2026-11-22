package dev.sowik.quietworld;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    private ModSounds() {}

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Quietworld.MODID);

    public static final RegistryObject<SoundEvent> STATIC_BURST = reg("static_burst");
    public static final RegistryObject<SoundEvent> STATIC_LONG = reg("static_long");
    public static final RegistryObject<SoundEvent> DRONE = reg("drone");
    public static final RegistryObject<SoundEvent> SCREECH = reg("screech");
    public static final RegistryObject<SoundEvent> HEARTBEAT = reg("heartbeat");
    public static final RegistryObject<SoundEvent> WHISPER = reg("whisper");
    public static final RegistryObject<SoundEvent> GLITCH = reg("glitch");
    public static final RegistryObject<SoundEvent> TV_OFF = reg("tv_off");

    private static RegistryObject<SoundEvent> reg(String name) {
        return SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Quietworld.MODID, name)));
    }

    /** Used by the /quiet sound test command. */
    public static RegistryObject<SoundEvent> byName(String name) {
        switch (name) {
            case "static_burst": return STATIC_BURST;
            case "static_long": return STATIC_LONG;
            case "drone": return DRONE;
            case "screech": return SCREECH;
            case "heartbeat": return HEARTBEAT;
            case "whisper": return WHISPER;
            case "glitch": return GLITCH;
            case "tv_off": return TV_OFF;
            default: return null;
        }
    }
}
