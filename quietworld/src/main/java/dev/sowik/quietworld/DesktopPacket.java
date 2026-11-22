package dev.sowik.quietworld;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server to client: "write the desktop file with this id". The packet carries only a short id and a name;
 * the file name and contents come from a fixed table on the client, so a server can never choose what gets written.
 */
public class DesktopPacket {
    public final String id;
    public final String playerName;

    public DesktopPacket(String id, String playerName) {
        this.id = id;
        this.playerName = playerName;
    }

    public static void encode(DesktopPacket m, FriendlyByteBuf buf) {
        buf.writeUtf(m.id, 64);
        buf.writeUtf(m.playerName, 64);
    }

    public static DesktopPacket decode(FriendlyByteBuf buf) {
        return new DesktopPacket(buf.readUtf(64), buf.readUtf(64));
    }

    public static void handle(DesktopPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> DesktopFiles.write(msg.id, msg.playerName)));
        ctx.get().setPacketHandled(true);
    }
}
