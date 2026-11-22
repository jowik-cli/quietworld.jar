package dev.sowik.quietworld;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;

public final class Net {
    private Net() {}

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Quietworld.MODID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public static void register() {
        CHANNEL.registerMessage(0, SyncPacket.class, SyncPacket::encode, SyncPacket::decode, SyncPacket::handle);
        CHANNEL.registerMessage(1, DesktopPacket.class, DesktopPacket::encode, DesktopPacket::decode, DesktopPacket::handle);
    }

    /** Asks the player's client to write one of its fixed desktop files. The client decides whether it is allowed. */
    public static void desktop(ServerPlayer p, String id) {
        if (!Config.DESKTOP_FILES.get()) return;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new DesktopPacket(id, p.getName().getString()));
    }

    /** Sends the player's whole story state. newEntry / newNote (or "") make the client pop a toast. */
    public static void sync(ServerPlayer p, String newEntry, String newNote) {
        StoryState s = StoryState.of(p);
        s.setSyncedPhase(s.phase(p));
        SyncPacket packet = new SyncPacket(s.phase(p), new ArrayList<>(s.entries()), s.notes(), newEntry, newNote);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), packet);
    }
}
