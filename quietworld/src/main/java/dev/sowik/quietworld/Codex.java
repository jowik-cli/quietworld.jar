package dev.sowik.quietworld;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The archive: every mob, structure, dimension, event and item, each unlocked the first time it is met. */
public final class Codex {
    private Codex() {}

    public enum Category {
        MOB("mobs"), STRUCTURE("structures"), DIMENSION("dimensions"), EVENT("events"), ITEM("items"), STORY("story");

        private final String label;

        Category(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public record Entry(String id, Category category, String title, String what, String does, String lore) {}

    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final Map<String, Entry> BY_ID = new HashMap<>();

    private static void add(String id, Category c, String title, String what, String does, String lore) {
        Entry e = new Entry(id, c, title, what, does, lore);
        ENTRIES.add(e);
        BY_ID.put(id, e);
    }

    static {
        // ---- Mobs ----
        add("mob_watcher", Category.MOB, "The Watcher",
            "A silent figure at the tree line. It never blinks.",
            "Stares at you and leaves when you approach, dropping the next note. It cannot be hurt.",
            "It is not guarding anything. It is learning your walk.");
        add("mob_kosiw", Category.MOB, "kosiw",
            "A fake player who joins the game in the early days.",
            "Hangs in the sky watching you, or visits, hands over a gift and answers questions.",
            "The notes signed K. read like his handwriting. He denies it.");
        add("mob_hollow", Category.MOB, "Hollow",
            "A pale figure that only moves while you are not looking.",
            "Touching you drags you into the Static Hall. Inside the Hall it throws you into a random room.",
            "They used to be players. They kept looking away.");
        add("mob_glitch", Category.MOB, "Glitch",
            "A small flickering thing that hops around you.",
            "Teleports every second or two, corrupting nearby blocks. Drops Corrupted Shards.",
            "It is not a creature. It is an error that learned to hide.");
        add("mob_echo", Category.MOB, "Echo",
            "A figure wearing your name.",
            "Repeats what you type in chat, garbled, as if you had said it. Leaves if you come close.",
            "Everything you say here is saved. This is the only one that reads it back.");
        add("mob_tall_one", Category.MOB, "The Tall One",
            "The Watcher's aggressive form, seen once the helper is gone.",
            "Chases you and hits hard. Drops Corrupted Shards and a corrupted book.",
            "It stood still for so long because it was waiting for permission.");
        add("mob_tenant", Category.MOB, "Tenant",
            "A faceless villager who lives in the Hollow Copy.",
            "Talks when right-clicked and hands over a lease note.",
            "He keeps the lights on for whoever is still looking.");
        add("mob_crawler", Category.MOB, "Crawler",
            "A fast black spider that climbs walls.",
            "Hunts at night and throughout the Hollow Copy.",
            "It walks on the ceiling of the save, where the render distance ends.");

        // ---- Structures ----
        add("struct_candle_ring", Category.STRUCTURE, "Candle Ring",
            "A blackstone circle with skulls and red candles.",
            "A lectern in the middle holds a corrupted book.",
            "Someone lit the candles. The wax is still warm.");
        add("struct_effigy", Category.STRUCTURE, "Hay Effigy",
            "A scarecrow with a carved head, ringed by candle posts.",
            "Marks a place where something stood for a long time.",
            "It always faces the way you came from.");
        add("struct_sigil_circle", Category.STRUCTURE, "Sunken Sigil",
            "A circle dug into the ground with a red sigil and a skull on a bone pillar.",
            "Nothing happens here. That is the unsettling part.",
            "The sigil is a map. Nobody has said of what.");
        add("struct_camp", Category.STRUCTURE, "K.'s Camp",
            "An abandoned camp with an unlit fire and a grey tent.",
            "Holds a journal on a lectern and a chest with food and a Lost and Found book.",
            "The fire is unlit on purpose.");
        add("struct_doorway", Category.STRUCTURE, "The Doorway",
            "A crying obsidian frame with a sign: the door is a picture of a door.",
            "Walk through it to reach the Hollow Copy. A matching doorway leads back.",
            "It was never a door. That is exactly why it works.");
        add("struct_house", Category.STRUCTURE, "House Without a Door",
            "A small oak house with windows and no way in.",
            "Holds a made bed, a chest of supplies and a lectern note.",
            "Nothing here knocks, so nothing here needed a door.");
        add("struct_dormitory", Category.STRUCTURE, "Dormitory",
            "An underground room of seven made beds, reached by a hatch and a ladder.",
            "The signs carry your name. A lectern holds a note.",
            "The eighth bed is not made yet.");
        add("struct_mast", Category.STRUCTURE, "Radio Mast",
            "A tall iron mast beside a small bunker.",
            "Hums static for anyone within 40 blocks. Holds a lectern note.",
            "Too many voices on one channel.");
        add("struct_graves", Category.STRUCTURE, "Graveyard",
            "Rows of stone markers, each with a sign.",
            "One grave carries your name, and a day marked with a question mark.",
            "Not yet is also a date.");
        add("struct_tower", Category.STRUCTURE, "Lookout Tower",
            "A tall stone tower with a ladder and a railing.",
            "Holds a lectern note and a soul campfire at the top.",
            "From up here every circle joins into one larger circle.");

        // ---- Dimensions ----
        add("dim_hall", Category.DIMENSION, "The Static Hall",
            "A maze of grey rooms under cold lamps.",
            "Hollows roam it. The only way out is the door in the far corner, or four minutes of waiting.",
            "A room it keeps for people it has finished with.");
        add("dim_copy", Category.DIMENSION, "The Hollow Copy",
            "Your own world's terrain, frozen in eternal night.",
            "Crawlers and Tenants live here. A doorway where you arrive leads home.",
            "The save was copied before it finished. This is the copy.");

        // ---- Items ----
        add("item_codex", Category.ITEM, "Codex",
            "This archive.",
            "Records what you encounter, with what it does and a piece of lore.",
            "It wrote the first page before you opened it.");
        add("item_journal", Category.ITEM, "Journal",
            "A book that collects every note you find.",
            "Right-click to read. It updates itself.",
            "The handwriting changes depending on who is reading.");
        add("item_radio", Category.ITEM, "Static Radio",
            "A crude radio with a bent antenna.",
            "Right-click to hear which way the nearest structure lies. It crackles when held near one.",
            "It does not receive stations. It receives places.");
        add("item_shard", Category.ITEM, "Corrupted Shard",
            "A fragment of glitched world.",
            "Right-click to tear a hole in the quiet and trigger a random event.",
            "Handle it like a sentence you were not supposed to read.");

        // ---- Story ----
        add("story_k", Category.STORY, "K.",
            "The author of the notes.",
            "Unlocked by the first note.",
            "Is K. kosiw? The notes will not say.");
        add("story_phase1", Category.STORY, "The Turn",
            "The day the helper stops being helpful.",
            "Notes contradict each other and the world gets stranger.",
            "I never told you my name. I never needed yours.");
        add("story_phase2", Category.STORY, "The Helper Is Gone",
            "The last stage. The notes have stopped.",
            "The Tall One walks. Darkness takes the light.",
            "It was never watching me. It was waiting for you to arrive.");

        // ---- Events ----
        add("event_distant_sound", Category.EVENT, "Distant Sounds",
            "A door or chest noise from a place with no door.",
            "Plays a muffled sound 14 to 26 blocks away.",
            "Something is opening things in the part of the world you are not looking at.");
        add("event_fake_join", Category.EVENT, "Phantom Join",
            "A join message for a player who is not here.",
            "Sometimes it is you. Sometimes somebody famous who leaves seconds later.",
            "The server counts one more than it should.");
        add("event_static_burst", Category.EVENT, "Static Burst",
            "A short blast of loud static.",
            "Plays at your position and can be very loud.",
            "The signal is not interference. It is crowding.");
        add("event_whisper", Category.EVENT, "Whispers",
            "Voices too quiet to read.",
            "Plays a whisper at your position.",
            "They all say your name, a little differently each time.");
        add("event_structure", Category.EVENT, "A New Place",
            "A structure appears while you are not looking.",
            "Builds a structure 60 to 120 blocks away.",
            "Nobody places these. They are remembered into existence.");
        add("event_torch_out", Category.EVENT, "Light Fails",
            "A torch near you goes out.",
            "Removes the nearest torch and plays a television switching off.",
            "Light keeps the dark honest. The dark keeps score.");
        add("event_sign", Category.EVENT, "Unsigned Sign",
            "A sign appears in front of you.",
            "Places a sign with a short message.",
            "Written in a hand you almost recognize.");
        add("event_heartbeat", Category.EVENT, "Heartbeat",
            "A slow pulse from nowhere.",
            "Plays a double heartbeat.",
            "It is not yours. Yours stops when this one starts.");
        add("event_corrupt_chunk", Category.EVENT, "Corruption Spreads",
            "A nearby chunk quietly changes.",
            "Turns a chunk into corrupted terrain. Standing in one crackles with static and can spawn Glitches.",
            "The world forgot what it looked like here, and guessed.");
        add("event_darken", Category.EVENT, "Darkness",
            "The light drains out of the world.",
            "Applies Darkness for ten seconds with a screech and static. Only after the helper is gone.",
            "It is not the dark. It is the edge of what was saved.");
        add("event_doors_open", Category.EVENT, "Doors Open",
            "Every door nearby swings open.",
            "Opens wooden doors within 12 blocks.",
            "Nothing here knocks. Everything here lets itself in.");
        add("event_fake_creeper", Category.EVENT, "Phantom Hiss",
            "A creeper fuse with no creeper.",
            "Plays a hiss behind you.",
            "It was never about the explosion.");
        add("event_footsteps", Category.EVENT, "Footsteps",
            "Steps that approach from behind and stop.",
            "Plays six footsteps, each closer than the last.",
            "They stop exactly when you turn around. That is the rule.");
        add("event_chat_glitch", Category.EVENT, "Chat Glitch",
            "Something types in chat.",
            "Sends a short message from nobody.",
            "The server is not supposed to talk. It is practicing.");
        add("event_thunder", Category.EVENT, "Thunder, No Rain",
            "Lightning under a clear sky.",
            "Strikes a harmless bolt a short distance away.",
            "The weather is also a copy.");
        add("event_bell", Category.EVENT, "The Bell",
            "A single low bell, far away.",
            "Plays a muffled bell.",
            "There is no village. There is a bell.");
        add("event_blink", Category.EVENT, "Blink",
            "The world blinks and you miss a second.",
            "Briefly blinds you with a burst of static.",
            "You did not miss anything. That is what it wants you to think.");
        add("event_lost_note", Category.EVENT, "Lost Note",
            "A lectern with a book appears nearby.",
            "Places a lectern holding a book 10 to 16 blocks away.",
            "Left for you. Or left by someone who did not want it.");
        add("event_mannequin", Category.EVENT, "Mannequin",
            "A pumpkin-headed armor stand watching you.",
            "Spawns a stand facing you that vanishes after five minutes.",
            "It was there before you looked. It will be there when you look away.");
        add("event_ash_fall", Category.EVENT, "Ash Fall",
            "White ash drifts down under a clear sky.",
            "Spawns ash particles around you for a few seconds.",
            "Something is burning in a version of the world you cannot reach.");
        add("event_mob_groan", Category.EVENT, "Groans",
            "Undead sounds from behind the trees.",
            "Plays zombie and skeleton sounds nearby with no mobs.",
            "They are rehearsing.");
        add("event_item_gift", Category.EVENT, "A Gift",
            "Something dropped at your feet while you were not looking.",
            "Drops a Corrupted Shard near you.",
            "It is not generous. It is a lure with good manners.");

        // ---- More mobs ----
        add("mob_gazer", Category.MOB, "Gazer",
            "A grey figure with enormous eyes that only moves while you look at it.",
            "The opposite of a Hollow: staring pulls it closer. On contact it hits you, darkens the world and slows you.",
            "It is not looking at you. It is being looked at, and it likes it.");
        add("mob_static_moth", Category.MOB, "Static Moth",
            "A dark moth that crackles when it flies.",
            "Eats torches within a few blocks and hums static near you.",
            "It is drawn to light the way the quiet is drawn to you.");
        add("mob_lamplighter", Category.MOB, "Lamplighter",
            "A figure carrying a soul lantern, walking away from you.",
            "Leads you to the nearest structure, waits if you fall behind, and fades when it arrives.",
            "Nobody asked for a guide. It keeps volunteering.");
        add("mob_archivist", Category.MOB, "Archivist",
            "A robed figure that writes things down.",
            "Right-click it to have one of your undiscovered archive entries written in. It then leaves.",
            "It is the only thing here that wants you to understand.");
        add("mob_wretch", Category.MOB, "Wretch",
            "A ragged, tough undead that hunts in packs.",
            "Hits hard and arrives in twos and threes once the copy has begun. May drop a shard.",
            "Former tenants who stayed past the lease.");
        add("mob_child", Category.MOB, "Lost Child",
            "A small pale figure that keeps its distance.",
            "Runs when watched, creeps closer when not, and gives you a shard if you speak to it.",
            "It is not lost. You are in its house.");

        // ---- More story ----
        add("story_phase3", Category.STORY, "The Copy",
            "The world begins to copy itself.",
            "Events come faster. Wretches, packs and corruption begin.",
            "The copy remembers what the original forgot.");
        add("story_phase4", Category.STORY, "The Breaking",
            "The save tries to finish and fails.",
            "Displacement, static storms and pairs of Tall Ones.",
            "Every event is a line the save is trying to write.");
        add("story_phase5", Category.STORY, "The Quiet",
            "The last stage.",
            "Everything is possible and everything is faster. The menus have stopped pretending.",
            "The Quiet is what is left when everything has been looked at.");

        // ---- More events ----
        add("event_hunger_pang", Category.EVENT, "Hollow Pang",
            "A sudden, unnatural hunger.",
            "Drains six points of food.",
            "Something ate the meal you did not finish.");
        add("event_storm", Category.EVENT, "Storm",
            "Clouds gather without warning.",
            "Starts a thunderstorm for three minutes.",
            "The weather is deciding who to be.");
        add("event_night_falls", Category.EVENT, "Night Falls",
            "The sun is skipped.",
            "Jumps the clock to nightfall if it is still day.",
            "The sun was never there. It was a rumor.");
        add("event_name_call", Category.EVENT, "Name Call",
            "Someone says your name in chat.",
            "Prints a line from K. addressed to you, and whispers.",
            "K. does not know how it knows.");
        add("event_desktop_file", Category.EVENT, "A File Outside",
            "A text file appears on your desktop, outside the game.",
            "Creates a harmless .txt file there. Turn it off with desktopFiles = false, or remove them from the Archive.",
            "The game is not the only place it can write. It is only the place it prefers.");
        add("event_fake_death", Category.EVENT, "Obituary",
            "A death message for you, from before it happened.",
            "Prints a fake death line in chat.",
            "It is practicing. It will not need to practice for long.");
        add("event_pickpocket", Category.EVENT, "Pickpocket",
            "An item vanishes from your inventory.",
            "Moves one stack out of your pack and leaves it on the ground nearby.",
            "It does not steal. It rearranges.");
        add("event_health_drain", Category.EVENT, "Drain",
            "A cold flash through your chest.",
            "Takes about a heart and a half of health.",
            "It is not hurting you. It is checking.");
        add("event_structure_cluster", Category.EVENT, "Cluster",
            "Several places appear at once.",
            "Builds up to three structures around you.",
            "The copy is filling in the blanks.");
        add("event_corrupt_storm", Category.EVENT, "Corruption Storm",
            "The ground changes in several places.",
            "Corrupts up to three chunks around you.",
            "It forgot what the whole world looked like.");
        add("event_swap_hotbar", Category.EVENT, "Muscle Memory",
            "Your hotbar is not how you left it.",
            "Swaps two hotbar slots.",
            "Your hands remember differently.");
        add("event_static_storm", Category.EVENT, "Static Storm",
            "Waves of static and darkness.",
            "Five static bursts with pulses of darkness, and slowness.",
            "The signal is not interference. It is a crowd arriving.");
        add("event_displacement", Category.EVENT, "Displacement",
            "You are not where you were standing.",
            "Teleports you a short way across the surface.",
            "It moved you. It could have moved the world.");
    }

    /** Unlocks a random entry the player has not found yet (not story). Returns its title, or null. */
    public static String unlockRandomLocked(ServerPlayer p) {
        Set<String> have = StoryState.of(p).entries();
        List<Entry> locked = new ArrayList<>();
        for (Entry e : ENTRIES) {
            if (!have.contains(e.id()) && e.category() != Category.STORY) {
                locked.add(e);
            }
        }
        if (locked.isEmpty()) return null;
        Entry pick = locked.get(p.getRandom().nextInt(locked.size()));
        unlock(p, pick.id());
        return pick.title();
    }

    public static List<Entry> all() {
        return ENTRIES;
    }

    public static int total() {
        return ENTRIES.size();
    }

    public static Entry get(String id) {
        return BY_ID.get(id);
    }

    public static List<Entry> entriesOf(Category c) {
        List<Entry> out = new ArrayList<>();
        for (Entry e : ENTRIES) {
            if (e.category() == c) out.add(e);
        }
        return out;
    }

    /** Unlocks an entry for a player and tells their client (which pops a toast). Unknown ids are ignored. */
    public static void unlock(ServerPlayer p, String id) {
        if (!BY_ID.containsKey(id)) return;
        if (StoryState.of(p).addEntry(id)) {
            Net.sync(p, id, "");
        }
    }

    /** Called from mob ticks: unlocks the entry when a player is near. */
    public static void encounter(Mob mob, String id) {
        if (mob.tickCount % 20 != 0) return;
        Player near = mob.level().getNearestPlayer(mob, 40.0D);
        if (near instanceof ServerPlayer sp) {
            unlock(sp, id);
        }
    }

    /** Which archive entry a registered site unlocks, or null. */
    public static String idForSite(int type) {
        switch (type) {
            case SiteData.GATE:
            case SiteData.RETURN:
                return "struct_doorway";
            case SiteData.MAST:
                return "struct_mast";
            default:
                String[] ids = {"struct_candle_ring", "struct_effigy", "struct_sigil_circle", "struct_camp", "struct_doorway",
                        "struct_house", "struct_dormitory", "struct_mast", "struct_graves", "struct_tower"};
                int t = type - SiteData.STRUCT_BASE;
                return t >= 0 && t < ids.length ? ids[t] : null;
        }
    }
}
