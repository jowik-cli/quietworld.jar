package dev.sowik.quietworld;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Copies every Quiet World book the player finds into their Journal. */
public final class Journal {
    private Journal() {}

    /** Collects any unread notes sitting in the inventory. */
    public static void scan(ServerPlayer p) {
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            collectFrom(p, inv.getItem(i));
        }
    }

    public static boolean collectFrom(ServerPlayer p, ItemStack stack) {
        if (stack.isEmpty() || !stack.is(Items.WRITTEN_BOOK)) return false;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("qw_id", Tag.TAG_STRING)) return false;

        String id = tag.getString("qw_id");
        StoryState s = StoryState.of(p);
        if (s.hasNote(id)) return false;
        if (id.startsWith("frag:") && s.fragCount() >= 8) return false;

        List<String> pages = new ArrayList<>();
        ListTag raw = tag.getList("pages", Tag.TAG_STRING);
        for (int i = 0; i < raw.size(); i++) {
            pages.add(plain(raw.getString(i)));
        }
        s.addNote(id, tag.getString("title"), tag.getString("author"), pages);
        Net.sync(p, "", tag.getString("title"));
        return true;
    }

    private static String plain(String json) {
        try {
            Component c = Component.Serializer.fromJson(json);
            return c == null ? json : c.getString();
        } catch (RuntimeException e) {
            return json;
        }
    }
}
