package dev.sowik.quietworld;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Text and book corruption helpers. */
public final class Corruption {
    private Corruption() {}

    private static final String GLITCH = "#%&@?!/|<>=+*~^";
    private static final char[] BLOCKS = {'\u2588', '\u2593', '\u2592', '\u2591'};

    private static final String[] FRAGMENTS = {
        "the save did not finish",
        "do not read this twice",
        "it counts how many times you come back",
        "nobody asked us to leave",
        "the door is a picture of a door",
        "stay in the light",
        "who is holding the camera",
        "you are not the first",
        "it learned your walk",
        "nothing here is old",
        "keep the fire lit",
        "k was here before the sky",
        "herobrine has a lawyer now",
        "this is not wonderland.jar (legal asked)",
        "the broken script needs a comma",
        "entity 303 sends regards",
        "null is not a valid name",
        "the cave dweller knocked twice"
    };

    private static final String[] TITLES = {
        "backup_02", "unsent", "(untitled)", "save.tmp", "do not open", "page 3"
    };

    /** Replaces roughly `ratio` of the letters and digits with glitch characters. */
    public static String garble(String text, RandomSource rnd, double ratio) {
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isLetterOrDigit(c) && rnd.nextDouble() < ratio) {
                if (rnd.nextInt(3) == 0) {
                    sb.append(BLOCKS[rnd.nextInt(BLOCKS.length)]);
                } else {
                    sb.append(GLITCH.charAt(rnd.nextInt(GLITCH.length())));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static ItemStack makeBook(RandomSource rnd) {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("title", garble(TITLES[rnd.nextInt(TITLES.length)], rnd, 0.35D));
        tag.putString("author", rnd.nextBoolean() ? "kosiw" : "\u00a7k?????");
        tag.putBoolean("resolved", true);
        tag.putInt("generation", 3);

        ListTag pages = new ListTag();
        int pageCount = 2 + rnd.nextInt(2);
        for (int p = 0; p < pageCount; p++) {
            StringBuilder page = new StringBuilder();
            int parts = 2 + rnd.nextInt(2);
            for (int i = 0; i < parts; i++) {
                String frag = garble(FRAGMENTS[rnd.nextInt(FRAGMENTS.length)], rnd, 0.16D);
                if (rnd.nextInt(4) == 0) {
                    frag = "\u00a7k" + frag + "\u00a7r";
                }
                if (i > 0) page.append("\n\n");
                page.append(frag);
            }
            pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(page.toString()))));
        }
        tag.put("pages", pages);
        tag.putString("qw_id", "frag:" + Integer.toHexString(pages.toString().hashCode()));
        return stack;
    }
}
