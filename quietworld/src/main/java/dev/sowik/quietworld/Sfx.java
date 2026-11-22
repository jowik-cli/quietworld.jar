package dev.sowik.quietworld;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

/** All mod sounds go through here so the volume config applies everywhere. */
public final class Sfx {
    private Sfx() {}

    public static void play(Level level, double x, double y, double z, RegistryObject<SoundEvent> sound,
                            SoundSource source, float volume, float pitch) {
        float v = volume * Config.SOUND_VOLUME.get().floatValue() * (Config.LOUD_SOUNDS.get() ? 1.0F : 0.35F);
        if (v <= 0.0F) return;
        level.playSound(null, x, y, z, sound.get(), source, v, pitch);
    }

    /** Plays at the player's position (other nearby players hear it too). */
    public static void at(ServerPlayer p, RegistryObject<SoundEvent> sound, float volume, float pitch) {
        play(p.level(), p.getX(), p.getY(), p.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }
}
