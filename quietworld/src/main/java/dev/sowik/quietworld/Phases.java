package dev.sowik.quietworld;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** The six story phases, and everything that changes with them. */
public final class Phases {
    private Phases() {}

    public static final int COUNT = 6;
    // 0 Arrival, 1 The Turn, 2 The Silence, 3 The Copy, 4 The Breaking, 5 The Quiet

    private static final String[] NAMES = {"Arrival", "The Turn", "The Silence", "The Copy", "The Breaking", "The Quiet"};

    private static final String[] CHAT = {
        "",
        "<K.> something changed. keep the lights on.",
        "<K.> I can't see you anymore.",
        "[Server] the copy is open to new tenants.",
        "[Server] saving... saving... saving...",
        "[Server] this world has been closed. you are still in it."
    };

    private static final String[] CONTINUE = {
        "continue", "continue?", "continue (it will know)", "continue (again)", "continue (do not)", "continue"
    };
    private static final String[] LEAVE = {
        "leave", "leave", "leave (try)", "leave (try harder)", "leave (it will follow)", "leave"
    };
    private static final String[] SUBTITLE = {
        "save.dat  (unfinished)", "save.dat  (corrupted)", "save.dat  (it is not yours)",
        "save.dat  (copied)", "save.dat  (breaking)", "save.dat  (quiet)"
    };
    private static final int[] TINT = {0, 0x10003838, 0x22660000, 0x2A660000, 0x38660000, 0x48000000};
    private static final int[] BAR = {0xFF8B0000, 0xFF6A1B9A, 0xFFFF1E1E, 0xFFFF4500, 0xFFFFFFFF, 0xFF555555};

    private static final String[] FOOTERS_COPY = {
        "the copy is open. you are the first tenant it does not remember.",
        "this menu has been duplicated. check the other one.",
        "the Wretches were tenants too.",
        "some of these entries were not here yesterday."
    };
    private static final String[] FOOTERS_BREAKING = {
        "saving... saving... saving...",
        "every event is a line the save is trying to write.",
        "the Tall One has a brother now.",
        "do not press continue. press it once more, then do not."
    };
    private static final String[] FOOTERS_QUIET = {
        "the quiet is not silence. it is everything having been looked at.",
        "thank you for looking.",
        "there is no author now. this is only the file.",
        "close the game. you will not."
    };

    private static final String[] PAUSE_COPY = {
        "the copy is also paused. it is not as patient.",
        "the Wretches do not respect pause screens.",
        "something is rebuilding the world while you read this."
    };
    private static final String[] PAUSE_BREAKING = {
        "saving... saving...",
        "the Gazer moves while you look at this menu.",
        "pausing does not stop the save from trying."
    };
    private static final String[] PAUSE_QUIET = {
        "there is nothing left to pause.",
        "it is quiet because everything is already here.",
        "you can leave. the file is still open."
    };

    private static int clamp(int p) {
        return Math.max(0, Math.min(COUNT - 1, p));
    }

    public static String name(int p) { return NAMES[clamp(p)]; }
    public static String continueLabel(int p) { return CONTINUE[clamp(p)]; }
    public static String leaveLabel(int p) { return LEAVE[clamp(p)]; }
    public static String subtitle(int p) { return SUBTITLE[clamp(p)]; }
    public static int tint(int p) { return TINT[clamp(p)]; }
    public static int barColor(int p) { return BAR[clamp(p)]; }

    public static String[] footers(int p) {
        return p == 3 ? FOOTERS_COPY : p == 4 ? FOOTERS_BREAKING : FOOTERS_QUIET;
    }

    public static String[] pauseLines(int p) {
        return p == 3 ? PAUSE_COPY : p == 4 ? PAUSE_BREAKING : PAUSE_QUIET;
    }

    /** Events fire more often the further the story has gone. */
    public static double chanceMultiplier(int phase) {
        return 1.0D + 0.3D * clamp(phase);
    }

    public static double cooldownMultiplier(int phase) {
        return Math.max(0.4D, 1.0D - 0.12D * clamp(phase));
    }

    /** Called the first time a player reaches a new phase. */
    public static void onChange(ServerPlayer p, int phase) {
        phase = clamp(phase);
        Codex.unlock(p, "story_phase" + phase);
        if (phase < 1) return;

        p.displayClientMessage(Component.literal(NAMES[phase]).withStyle(ChatFormatting.DARK_RED), true);
        p.sendSystemMessage(Component.literal(CHAT[phase]));
        switch (phase) {
            case 1:
                Sfx.at(p, ModSounds.HEARTBEAT, 1.0F, 0.9F);
                break;
            case 2:
                Sfx.at(p, ModSounds.SCREECH, 0.9F, 0.8F);
                break;
            case 3:
                Sfx.at(p, ModSounds.DRONE, 1.0F, 0.6F);
                break;
            case 4:
                Sfx.at(p, ModSounds.STATIC_BURST, 1.0F, 0.9F);
                break;
            default:
                Sfx.at(p, ModSounds.STATIC_LONG, 1.0F, 0.7F);
                break;
        }
        Net.desktop(p, "phase" + phase);
        Net.sync(p, "", "");
    }
}
