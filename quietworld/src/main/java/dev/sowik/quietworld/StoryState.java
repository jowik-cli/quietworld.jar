package dev.sowik.quietworld;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

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

    public int ritualCount() { return tag.getInt("ritualCount"); }
    public void setRitualCount(int n) { tag.putInt("ritualCount", n); }

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
        if (since < 2) return 0;
        if (since < 4) return 1;
        return 2;
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
