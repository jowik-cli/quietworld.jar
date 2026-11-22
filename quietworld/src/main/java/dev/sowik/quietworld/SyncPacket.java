package dev.sowik.quietworld;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Server to client: story phase, unlocked archive entries and collected notes. */
public class SyncPacket {
    public record NoteData(String id, String title, String author, List<String> pages) {}

    public final int phase;
    public final List<String> unlocked;
    public final List<NoteData> notes;
    public final String newEntry;
    public final String newNote;

    public SyncPacket(int phase, List<String> unlocked, List<NoteData> notes, String newEntry, String newNote) {
        this.phase = phase;
        this.unlocked = unlocked;
        this.notes = notes;
        this.newEntry = newEntry;
        this.newNote = newNote;
    }

    public static void encode(SyncPacket m, FriendlyByteBuf buf) {
        buf.writeVarInt(m.phase);
        buf.writeVarInt(m.unlocked.size());
        for (String id : m.unlocked) {
            buf.writeUtf(id);
        }
        buf.writeVarInt(m.notes.size());
        for (NoteData n : m.notes) {
            buf.writeUtf(n.id());
            buf.writeUtf(n.title());
            buf.writeUtf(n.author());
            buf.writeVarInt(n.pages().size());
            for (String page : n.pages()) {
                buf.writeUtf(page);
            }
        }
        buf.writeUtf(m.newEntry);
        buf.writeUtf(m.newNote);
    }

    public static SyncPacket decode(FriendlyByteBuf buf) {
        int phase = buf.readVarInt();
        int count = buf.readVarInt();
        List<String> unlocked = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            unlocked.add(buf.readUtf());
        }
        int noteCount = buf.readVarInt();
        List<NoteData> notes = new ArrayList<>();
        for (int i = 0; i < noteCount; i++) {
            String id = buf.readUtf();
            String title = buf.readUtf();
            String author = buf.readUtf();
            int pageCount = buf.readVarInt();
            List<String> pages = new ArrayList<>();
            for (int j = 0; j < pageCount; j++) {
                pages.add(buf.readUtf());
            }
            notes.add(new NoteData(id, title, author, pages));
        }
        return new SyncPacket(phase, unlocked, notes, buf.readUtf(), buf.readUtf());
    }

    public static void handle(SyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientProgress.apply(msg)));
        ctx.get().setPacketHandled(true);
    }
}
