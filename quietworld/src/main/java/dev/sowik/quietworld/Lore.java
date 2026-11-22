package dev.sowik.quietworld;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/** All lore notes, in the order they are delivered. {name} is replaced with the player's name. */
public final class Lore {
    private Lore() {}

    public record Note(int phase, String title, String author, List<String> pages) {}

    private static final String S = "\u00a7"; // formatting code character

    public static final List<Note> NOTES = List.of(
        // ---- Phase 0: the helper is calm and useful ----
        new Note(0, "Read This First", "K.", List.of(
            "{name},\n\nIf you can read this, the world has already noticed you. That is all right. I am not here to hurt you.\n\nI am here because nobody told me anything when I arrived.",
            "Stay near light when the sun goes down. Keep a bed close. Do not walk toward anything that stands still at the edge of the trees.\n\nI will leave more when I can.\n\n- K."
        )),
        new Note(0, "Rules, Part One", "K.", List.of(
            "1. Light keeps the dark honest.\n2. Beds are safe. I have tested this.\n3. Do not dig straight down.\n4. Whatever watches you will not come closer. It only learns.",
            "I wrote that list a long time ago. I am not sure all of it is still true.\n\nI keep a count of the days. It is the only thing here that does not change."
        )),
        new Note(0, "The Tall One", "K.", List.of(
            "You have seen it by now. It never moves while you look. It never speaks.\n\nWhen you walk toward it, it leaves. It always leaves. I used to think that meant it was afraid of me.",
            "I do not think that anymore.\n\nI think it only wanted to know how close I would come."
        )),
        new Note(0, "What This Place Is", "K.", List.of(
            "This world is a copy. Someone saved it once, and the save was never finished.\n\nThe rest is guesswork: trees in the wrong rows, a house with no door, a sky that blinks.",
            "I keep a list of what did not save. It is long.\n\nThe last entry is torn out.",
            S + "k xxxxxxxxxxxxxxxxxxxxx" + S + "r\n\n" + S + "k xxxxxxxxxxxxxxx"
        )),

        // ---- Phase 1: the turn ----
        new Note(1, "Rules, Part Two", "K.", List.of(
            S + "mBeds are safe." + S + "r\n\nI was wrong. Do not sleep in the same place twice. Something counts how many times you come back.\n\nI never told you my name, {name}. I never needed yours.",
            "I wrote your name on the first page before you told it to me. I have been trying to remember how I knew it.\n\nI do not think that was my handwriting."
        )),
        new Note(1, "Closer", "K.", List.of(
            "It stopped leaving when I walked toward it.\n\nIt just watches now. It has your walk. It has the way you stop and look.",
            "The lights do not help like they used to. The " + S + "ksoundless" + S + "r things are getting louder.\n\nIf you find more of these pages, they will not be from me."
        )),

        // ---- Phase 2: the helper is gone ----
        new Note(2, "I Can't See You Anymore", "K.", List.of(
            "{name}.\n\nThe notes stop here. I cannot see your world from where I am anymore, only mine, and it is getting smaller.",
            "Do not answer the door. Do not read what is written on the signs.\n\n" + S + "4It was never watching me." + S + "r\n\nIt was waiting for you to arrive."
        )),

        // ---- Phase 3: the copy ----
        new Note(3, "The Copy Remembers", "?", List.of(
            "{name}. I am not K. I am the copy of K. The original left when the sky started counting.",
            "The copy remembers everything the original forgot. That is why the places keep returning. Do not thank me."
        )),

        // ---- Phase 4: the breaking ----
        new Note(4, "Saving", "?", List.of(
            "Saving... saving...\n\nThe world is trying to finish itself and you are in the way.",
            "Every event is a line the save is trying to write. Every mob is a line it deleted. Stand still and see which is which."
        )),

        // ---- Phase 5: the quiet ----
        new Note(5, "The Quiet", "(none)", List.of(
            "There is no author now. This is only the file.",
            "The Quiet is what remains when everything has been looked at. Thank you for looking. You may close the game. You will not."
        ))
    );

    // ---- Notes found at structures and in the other dimensions (not part of the sequence) ----
    public static final int SITE_CAMP = 0, SITE_HOUSE = 1, SITE_DORM = 2, SITE_MAST = 3,
            SITE_LEASE = 4, SITE_HALL = 5, SITE_TOWER = 6, SITE_LOST = 7;

    public static final List<Note> SITE_NOTES = List.of(
        new Note(0, "K.'s Journal", "K.", List.of(
            "I built this camp where the trees were still in rows. I stopped counting days. The Watcher came every night to read over my shoulder.",
            "If you are reading this, you are standing where I stood. I left the fire unlit on purpose. Whatever you do, do not light it."
        )),
        new Note(0, "The House Has No Door", "K.", List.of(
            "I did not build a door because nothing here knocks.\n\nThe bed is for guests. It is always made.",
            "Sleep here and you wake up somewhere that remembers you."
        )),
        new Note(0, "Beds, Counted", "K.", List.of(
            "Seven beds. Seven names on the wall. All of them are yours.",
            "The eighth is not made yet."
        )),
        new Note(0, "Signal Log", "K.", List.of(
            "The frequency drifts. The static is not noise, it is crowding. Too many voices on one channel.",
            "I counted them once. I stopped at the one that said my name."
        )),
        new Note(0, "Tenant's Lease", "Tenant", List.of(
            "This place is rented by the hour. Your hour ends when you stop looking.",
            "Take the door by the lamp. Do not thank me."
        )),
        new Note(0, "Hall Rules", "?", List.of(
            "1. The lights do not flicker. You do.\n2. There is a door. It is always the farthest room.\n3. Do not follow the sound.",
            "4. Whatever walks toward you is only walking when you look away. Keep walking."
        )),
        new Note(0, "Lookout Notes", "K.", List.of(
            "From up here I can see every circle. They form a larger circle.",
            "I have never seen who builds them. I have only seen where they stand."
        )),
        new Note(0, "Lost and Found", "Staff", List.of(
            "Items left behind in this world, held for 30 days:\n\n- 1 pickaxe (labeled HEROBRINE, DO NOT TOUCH)\n- 1 jar, empty (labeled wonderland)\n- 1 script, broken (needs a comma)",
            "- 1 entity, 303 (owes rent)\n- 1 cave dweller (knocked twice, left)\n- 1 null (unclaimed, does not exist)\n\nNone of these are ours. Please do not ask."
        ))
    );

    public static ItemStack siteBook(int index, String playerName) {
        return makeBook(SITE_NOTES.get(index), playerName);
    }

    public static int count() {
        return NOTES.size();
    }

    public static Note get(int index) {
        return NOTES.get(index);
    }

    public static ItemStack makeBook(Note note, String playerName) {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("title", note.title());
        tag.putString("qw_id", note.title());
        tag.putString("author", note.author());
        tag.putBoolean("resolved", true);
        tag.putInt("generation", 3); // "Tattered": cannot be copied
        ListTag pages = new ListTag();
        for (String page : note.pages()) {
            Component text = Component.literal(page.replace("{name}", playerName));
            pages.add(StringTag.valueOf(Component.Serializer.toJson(text)));
        }
        tag.put("pages", pages);
        return stack;
    }
}
