package dev.sowik.quietworld;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** World-wide registry of portal sites, radio masts, corrupted chunks, and whether the Static Hall is built. */
public class SiteData extends SavedData {
    public static final int GATE = 0;      // overworld doorway -> Hollow Copy
    public static final int RETURN = 1;    // doorway inside the Hollow Copy -> home
    public static final int HALL_EXIT = 2; // door at the end of the Static Hall -> home
    public static final int MAST = 3;      // radio mast, hums static
    public static final int STRUCT_BASE = 10; // 10 + structure type, for every other structure

    public record Site(String dim, BlockPos pos, int type) {}

    private final List<Site> sites = new ArrayList<>();
    private final Set<Long> corrupted = new HashSet<>();
    private boolean hallBuilt;

    public static SiteData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(SiteData::load, SiteData::new, "quietworld_sites");
    }

    public static SiteData load(CompoundTag tag) {
        SiteData data = new SiteData();
        data.hallBuilt = tag.getBoolean("hallBuilt");
        ListTag list = tag.getList("sites", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            data.sites.add(new Site(t.getString("dim"),
                    new BlockPos(t.getInt("x"), t.getInt("y"), t.getInt("z")), t.getInt("type")));
        }
        for (long l : tag.getLongArray("corrupted")) {
            data.corrupted.add(l);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("hallBuilt", hallBuilt);
        ListTag list = new ListTag();
        for (Site s : sites) {
            CompoundTag t = new CompoundTag();
            t.putString("dim", s.dim());
            t.putInt("x", s.pos().getX());
            t.putInt("y", s.pos().getY());
            t.putInt("z", s.pos().getZ());
            t.putInt("type", s.type());
            list.add(t);
        }
        tag.put("sites", list);
        long[] arr = new long[corrupted.size()];
        int i = 0;
        for (long l : corrupted) {
            arr[i++] = l;
        }
        tag.putLongArray("corrupted", arr);
        return tag;
    }

    public List<Site> sites() {
        return Collections.unmodifiableList(sites);
    }

    public void add(Site site) {
        sites.add(site);
        setDirty();
    }

    public boolean hallBuilt() { return hallBuilt; }

    public void setHallBuilt(boolean v) {
        hallBuilt = v;
        setDirty();
    }

    public boolean isCorrupted(long chunkKey) { return corrupted.contains(chunkKey); }
    public int corruptedCount() { return corrupted.size(); }

    public void addCorrupted(long chunkKey) {
        corrupted.add(chunkKey);
        setDirty();
    }
}
