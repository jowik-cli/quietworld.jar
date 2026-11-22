package dev.sowik.quietworld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Leaves harmless text files on the player's desktop, as ARG props.
 *
 * Safety rules: the file name and text come from the fixed table below (never from the network), an existing file
 * is never overwritten, nothing is ever read, run or deleted except the files this class created, and at most
 * 14 files are ever written. Everything can be switched off with desktopFiles = false in the config.
 */
public final class DesktopFiles {
    private DesktopFiles() {}

    private static final int LIMIT = 14;
    private static final String FOOTER = "\n\n-- created by the Quiet World mod. harmless text file, safe to delete. --\n";

    private record Def(String name, String text) {}

    private static final Map<String, Def> FILES = new HashMap<>();
    private static final Set<String> KNOWN_NAMES = new HashSet<>();

    private static void add(String id, String name, String text) {
        FILES.put(id, new Def(name, text));
        KNOWN_NAMES.add(name);
    }

    static {
        add("phase1", "K_was_here.txt",
            "If you are reading this outside the game, it is already outside the game.\n\n{name},\n\n"
            + "I should have told you my name earlier. I am K. I was here before you.\n"
            + "Keep the lights on. Do not trust the second list of rules.");
        add("phase2", "i_cannot_see_you.txt",
            "{name}.\n\nI can't see your world from where I am anymore.\n"
            + "The notes stop here. Do not answer the door.");
        add("phase3", "the_copy.txt",
            "The copy is open.\n\nEverything that was lost is being rebuilt, a little wrong.\n"
            + "You were never meant to find the second door.");
        add("phase4", "saving.txt",
            "saving...\nsaving...\nsaving...\n\nthe world is trying to finish itself and you are in the way, {name}.");
        add("phase5", "the_quiet.txt",
            "There is no author now. This is only the file.\n\nThank you for looking.\nYou may close the game. You will not.");

        add("extra_do_not_open", "do_not_open.txt",
            "You opened it.\n\nThat is what it wanted. It is only a text file.\nIt is only a text file.\nIt is only a text file.");
        add("extra_kosiw", "kosiw.txt",
            "<kosiw> behind you\n<kosiw> (that is a joke. there is a wall behind you.)\n<kosiw> probably.");
        add("extra_resignation", "herobrine_resignation_letter.txt",
            "To whom it may concern,\n\nI am resigning from my position as the scariest thing in this game, effective immediately.\n"
            + "The Watcher is just a tall man and he is already getting better reviews.\n\nRegards,\nHerobrine\n"
            + "(please do not tell Entity 303)");
        add("extra_rent", "rent_notice.txt",
            "FINAL NOTICE\n\nEntity 303 has not paid rent in 4 saves.\nThe Tenant would like a word.\n\n- Management");
        add("extra_lost", "lost_and_found.txt",
            "Items held for 30 days: 1 pickaxe (HEROBRINE, DO NOT TOUCH), 1 jar (empty, labeled wonderland), "
            + "1 script (broken, needs a comma).\n\nNone of these are ours.");
        add("extra_static", "static.txt",
            "kzzzzt ... {name} ... kzzzt ... still there?\n\nkzzzzzzzt");
    }

    private static Path desktop() {
        String home = System.getProperty("user.home");
        if (home == null || home.isEmpty()) return null;
        Path[] candidates = {Paths.get(home, "OneDrive", "Desktop"), Paths.get(home, "Desktop")};
        for (Path p : candidates) {
            if (Files.isDirectory(p) && Files.isWritable(p)) return p;
        }
        return null;
    }

    private static String sanitize(String name) {
        String clean = name == null ? "" : name.replaceAll("[^A-Za-z0-9_ ]", "");
        if (clean.length() > 16) clean = clean.substring(0, 16);
        return clean.isEmpty() ? "you" : clean;
    }

    public static void write(String id, String playerName) {
        if (!Config.DESKTOP_FILES.get()) return;
        Def def = FILES.get(id);
        if (def == null) return;
        if (ClientProgress.desktopCount() >= LIMIT) return;
        Path dir = desktop();
        if (dir == null) return;

        String text = def.text().replace("{name}", sanitize(playerName)) + FOOTER;
        try {
            Files.writeString(dir.resolve(def.name()), text, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException e) {
            return; // already exists, or not writable: leave it alone
        }
        ClientProgress.addDesktop(def.name());
        SystemToast.addOrUpdate(Minecraft.getInstance().getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                Component.literal("Something left a file on your desktop"), Component.literal(def.name()));
    }

    /** Deletes only the files this class created. */
    public static void cleanup() {
        Path dir = desktop();
        if (dir != null) {
            for (String name : new ArrayList<>(ClientProgress.desktopFiles())) {
                if (!KNOWN_NAMES.contains(name)) continue;
                try {
                    Files.deleteIfExists(dir.resolve(name));
                } catch (IOException e) {
                    // leave it
                }
            }
        }
        ClientProgress.clearDesktop();
        SystemToast.addOrUpdate(Minecraft.getInstance().getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                Component.literal("Desktop files removed"), Component.literal("only the ones the mod made"));
    }
}
