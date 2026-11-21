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
        ))
    );

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
