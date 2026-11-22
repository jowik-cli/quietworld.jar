package dev.sowik.quietworld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * What the client knows about the player's story: phase, unlocked archive entries and collected notes.
 * Saved to quietworld_progress.dat so the title screen can react before any world is loaded.
 */
public final class ClientProgress {
    private ClientProgress() {}

    private static int phase = 0;
    private static final Set<String> UNLOCKED = new HashSet<>();
    private static List<SyncPacket.NoteData> notes = new ArrayList<>();
    private static final List<String> DESKTOP = new ArrayList<>();
    private static boolean loaded = false;

    private static File file() {
        return FMLPaths.GAMEDIR.get().resolve("quietworld_progress.dat").toFile();
    }

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        File f = file();
        if (!f.exists()) return;
        try {
            CompoundTag root = NbtIo.readCompressed(f);
            phase = root.getInt("phase");
            ListTag ids = root.getList("ids", Tag.TAG_STRING);
            for (int i = 0; i < ids.size(); i++) {
                UNLOCKED.add(ids.getString(i));
            }
            ListTag ns = root.getList("notes", Tag.TAG_COMPOUND);
            for (int i = 0; i < ns.size(); i++) {
                CompoundTag t = ns.getCompound(i);
                List<String> pages = new ArrayList<>();
                ListTag pl = t.getList("pages", Tag.TAG_STRING);
                for (int j = 0; j < pl.size(); j++) {
                    pages.add(pl.getString(j));
                }
                notes.add(new SyncPacket.NoteData(t.getString("id"), t.getString("title"), t.getString("author"), pages));
            }
            ListTag dl = root.getList("desktop", Tag.TAG_STRING);
            for (int i = 0; i < dl.size(); i++) {
                DESKTOP.add(dl.getString(i));
            }
        } catch (IOException | RuntimeException e) {
            // a broken progress file just means the menu starts fresh
        }
    }

    private static void save() {
        CompoundTag root = new CompoundTag();
        root.putInt("phase", phase);
        ListTag ids = new ListTag();
        for (String id : UNLOCKED) {
            ids.add(StringTag.valueOf(id));
        }
        root.put("ids", ids);
        ListTag ns = new ListTag();
        for (SyncPacket.NoteData n : notes) {
            CompoundTag t = new CompoundTag();
            t.putString("id", n.id());
            t.putString("title", n.title());
            t.putString("author", n.author());
            ListTag pl = new ListTag();
            for (String page : n.pages()) {
                pl.add(StringTag.valueOf(page));
            }
            t.put("pages", pl);
            ns.add(t);
        }
        root.put("notes", ns);
        ListTag dl = new ListTag();
        for (String n : DESKTOP) {
            dl.add(StringTag.valueOf(n));
        }
        root.put("desktop", dl);
        try {
            NbtIo.writeCompressed(root, file());
        } catch (IOException e) {
            // not fatal
        }
    }

    public static int phase() {
        ensureLoaded();
        return phase;
    }

    public static boolean has(String id) {
        ensureLoaded();
        return UNLOCKED.contains(id);
    }

    public static int count() {
        ensureLoaded();
        return UNLOCKED.size();
    }

    public static List<SyncPacket.NoteData> notes() {
        ensureLoaded();
        return notes;
    }

    public static int desktopCount() {
        ensureLoaded();
        return DESKTOP.size();
    }

    public static List<String> desktopFiles() {
        ensureLoaded();
        return DESKTOP;
    }

    public static void addDesktop(String name) {
        ensureLoaded();
        if (!DESKTOP.contains(name)) {
            DESKTOP.add(name);
            save();
        }
    }

    public static void clearDesktop() {
        ensureLoaded();
        DESKTOP.clear();
        save();
    }

    public static void apply(SyncPacket p) {
        ensureLoaded();
        phase = p.phase;
        UNLOCKED.clear();
        UNLOCKED.addAll(p.unlocked);
        notes = new ArrayList<>(p.notes);
        save();

        Minecraft mc = Minecraft.getInstance();
        if (!p.newEntry.isEmpty()) {
            Codex.Entry e = Codex.get(p.newEntry);
            if (e != null) {
                SystemToast.addOrUpdate(mc.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                        Component.literal("Archive updated"), Component.literal(e.title()));
            }
        }
        if (!p.newNote.isEmpty()) {
            SystemToast.addOrUpdate(mc.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                    Component.literal("Journal updated"), Component.literal(p.newNote));
        }
    }
}
