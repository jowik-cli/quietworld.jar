package dev.sowik.quietworld;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Per-player story progress, stored in the player's persistent data (survives death and relogs). */
public final class StoryState {
    private static final String ROOT = Quietworld.MODID;
    private final CompoundTag tag;

    private StoryState(CompoundTag tag) {
        this.tag = tag;
    }

    public static StoryState of(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        CompoundTag persisted;
        if (data.contains(Player.PERSISTED_NBT_TAG, 10)) {
            persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        } else {
            persisted = new CompoundTag();
            data.put(Player.PERSISTED_NBT_TAG, persisted);
        }
        CompoundTag root;
        if (persisted.contains(ROOT, 10)) {
            root = persisted.getCompound(ROOT);
        } else {
            root = new CompoundTag();
            persisted.put(ROOT, root);
        }
        return new StoryState(root);
    }

    public boolean started() { return tag.getBoolean("started"); }

    public void start(long day, long gameTick) {
        tag.putBoolean("started", true);
        tag.putLong("startDay", day);
        tag.putLong("startTick", gameTick);
    }

    public long startTick() { return tag.getLong("startTick"); }
    public boolean introDone() { return tag.getBoolean("introDone"); }
    public void setIntroDone(boolean v) { tag.putBoolean("introDone", v); }

    public int nextNote() { return tag.getInt("nextNote"); }
    public void setNextNote(int i) { tag.putInt("nextNote", i); }

    public long lastNoteTick() { return tag.contains("lastNoteTick") ? tag.getLong("lastNoteTick") : -1L; }
    public void setLastNoteTick(long t) { tag.putLong("lastNoteTick", t); }

    public int siteCount() { return tag.getInt("siteCount"); }
    public void setSiteCount(int n) { tag.putInt("siteCount", n); }

    /** Events in the story can only move the phase forward. */
    public void raisePhaseFloor(int floor) {
        if (floor > tag.getInt("phaseFloor")) tag.putInt("phaseFloor", floor);
    }

    // ---- Archive (codex) ----
    public boolean addEntry(String id) {
        ListTag list = tag.getList("codex", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(id)) return false;
        }
        list.add(StringTag.valueOf(id));
        tag.put("codex", list);
        return true;
    }

    public boolean hasEntry(String id) {
        return entries().contains(id);
    }

    public Set<String> entries() {
        Set<String> out = new HashSet<>();
        ListTag list = tag.getList("codex", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            out.add(list.getString(i));
        }
        return out;
    }

    // ---- Journal (collected notes) ----
    public boolean hasNote(String id) {
        ListTag list = tag.getList("notes", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            if (list.getCompound(i).getString("id").equals(id)) return true;
        }
        return false;
    }

    public int fragCount() {
        int n = 0;
        ListTag list = tag.getList("notes", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            if (list.getCompound(i).getString("id").startsWith("frag:")) n++;
        }
        return n;
    }

    public void addNote(String id, String title, String author, List<String> pages) {
        ListTag list = tag.getList("notes", Tag.TAG_COMPOUND);
        CompoundTag t = new CompoundTag();
        t.putString("id", id);
        t.putString("title", title);
        t.putString("author", author);
        ListTag pl = new ListTag();
        for (String page : pages) {
            pl.add(StringTag.valueOf(page));
        }
        t.put("pages", pl);
        list.add(t);
        tag.put("notes", list);
    }

    public List<SyncPacket.NoteData> notes() {
        List<SyncPacket.NoteData> out = new ArrayList<>();
        ListTag list = tag.getList("notes", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            List<String> pages = new ArrayList<>();
            ListTag pl = t.getList("pages", Tag.TAG_STRING);
            for (int j = 0; j < pl.size(); j++) {
                pages.add(pl.getString(j));
            }
            out.add(new SyncPacket.NoteData(t.getString("id"), t.getString("title"), t.getString("author"), pages));
        }
        return out;
    }

    public int announcedPhase() { return tag.contains("announcedPhase") ? tag.getInt("announcedPhase") : -1; }
    public void setAnnouncedPhase(int p) { tag.putInt("announcedPhase", p); }

    public int syncedPhase() { return tag.contains("syncedPhase") ? tag.getInt("syncedPhase") : -1; }
    public void setSyncedPhase(int p) { tag.putInt("syncedPhase", p); }

    public boolean flag(String key) { return tag.getBoolean(key); }
    public void setFlag(String key) { tag.putBoolean(key, true); }

    public boolean hasHome() { return tag.contains("homeX"); }
    public double homeX() { return tag.getDouble("homeX"); }
    public double homeY() { return tag.getDouble("homeY"); }
    public double homeZ() { return tag.getDouble("homeZ"); }
    public void setHome(double x, double y, double z) {
        tag.putDouble("homeX", x);
        tag.putDouble("homeY", y);
        tag.putDouble("homeZ", z);
    }

    public long portalCooldown() { return tag.getLong("portalCooldown"); }
    public void setPortalCooldown(long t) { tag.putLong("portalCooldown", t); }

    public long hallSince() { return tag.getLong("hallSince"); }
    public void setHallSince(long t) { tag.putLong("hallSince", t); }

    public long lastEventTick() { return tag.contains("lastEventTick") ? tag.getLong("lastEventTick") : -1L; }
    public void setLastEventTick(long t) { tag.putLong("lastEventTick", t); }

    /** -1 clears the override and goes back to the automatic day-based phase. */
    public void setPhaseOverride(int phase) {
        if (phase < 0) tag.remove("phaseOverride");
        else tag.putInt("phaseOverride", phase);
    }

    public void reset() {
        for (String key : new ArrayList<>(tag.getAllKeys())) {
            tag.remove(key);
        }
    }

    /** 0 = first two days (helper), 1 = the turn, 2 = the helper is gone. */
    public int phase(ServerPlayer player) {
        if (tag.contains("phaseOverride")) return tag.getInt("phaseOverride");
        long day = player.server.overworld().getDayTime() / 24000L;
        long since = day - tag.getLong("startDay");
        int auto = since < 2 ? 0 : since < 4 ? 1 : since < 6 ? 2 : since < 8 ? 3 : since < 11 ? 4 : 5;
        return Math.max(auto, tag.getInt("phaseFloor"));
    }

    public static long currentDay(ServerPlayer player) {
        return player.server.overworld().getDayTime() / 24000L;
    }

    /**
     * Gives the next lore note if the player's phase allows it.
     * at == null puts it in the inventory (dropping at the feet if full); otherwise drops it at that position.
     */
    public static boolean deliverNext(ServerPlayer player, Vec3 at, boolean ignoreGap) {
        StoryState s = of(player);
        int index = s.nextNote();
        if (index >= Lore.count()) return false;
        Lore.Note note = Lore.get(index);
        if (note.phase() > s.phase(player)) return false;

        long now = player.level().getGameTime();
        long last = s.lastNoteTick();
        if (!ignoreGap && last >= 0 && now - last < Config.NOTE_GAP_SECONDS.get() * 20L) return false;

        ItemStack book = Lore.makeBook(note, player.getName().getString());
        Journal.collectFrom(player, book);
        Codex.unlock(player, "story_k");
        Vec3 dropAt = at;
        if (dropAt == null && !player.getInventory().add(book)) {
            dropAt = player.position();
        }
        if (dropAt != null) {
            ItemEntity item = new ItemEntity(player.level(), dropAt.x, dropAt.y + 0.3D, dropAt.z, book);
            item.setDeltaMovement(Vec3.ZERO);
            player.level().addFreshEntity(item);
        }
        s.setNextNote(index + 1);
        s.setLastNoteTick(now);
        return true;
    }
}
