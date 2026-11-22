package dev.sowik.quietworld;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue SHOW_WARNING;
    public static final ForgeConfigSpec.BooleanValue EVENTS_ENABLED;
    public static final ForgeConfigSpec.DoubleValue EVENT_CHANCE;
    public static final ForgeConfigSpec.IntValue EVENT_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue NOTE_GAP_SECONDS;
    public static final ForgeConfigSpec.BooleanValue KOSIW_ENABLED;
    public static final ForgeConfigSpec.IntValue MAX_SITES;
    public static final ForgeConfigSpec.DoubleValue SOUND_VOLUME;
    public static final ForgeConfigSpec.BooleanValue LOUD_SOUNDS;
    public static final ForgeConfigSpec.BooleanValue CUSTOM_MENUS;
    public static final ForgeConfigSpec.BooleanValue FLASHES;
    public static final ForgeConfigSpec.BooleanValue DESKTOP_FILES;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        SHOW_WARNING = b.comment("Show the intro warning screen once each time the game launches")
                .define("showWarning", true);
        EVENTS_ENABLED = b.comment("Turn random events on or off")
                .define("eventsEnabled", true);
        EVENT_CHANCE = b.comment("Chance that an event fires at each check (checks run every 10 seconds)")
                .defineInRange("eventChance", 0.15D, 0.0D, 1.0D);
        EVENT_COOLDOWN_SECONDS = b.comment("Minimum seconds between random events for one player")
                .defineInRange("eventCooldownSeconds", 150, 10, 36000);
        NOTE_GAP_SECONDS = b.comment("Minimum seconds between lore notes for one player")
                .defineInRange("noteGapSeconds", 240, 0, 36000);
        KOSIW_ENABLED = b.comment("Allow kosiw to appear")
                .define("kosiwEnabled", true);
        MAX_SITES = b.comment("Maximum structures the mod builds for each player")
                .defineInRange("maxSites", 12, 0, 100);
        SOUND_VOLUME = b.comment("Master volume for every mod sound (0.0 = silent, 1.0 = full)")
                .defineInRange("soundVolume", 1.0D, 0.0D, 1.0D);
        LOUD_SOUNDS = b.comment("Set to false to play all mod sounds at about a third of their volume")
                .define("loudSounds", true);
        CUSTOM_MENUS = b.comment("Replace the main menu and pause menu with the Quiet World ones")
                .define("customMenus", true);
        FLASHES = b.comment("Allow events that flash (thunder) or briefly blind the player")
                .define("flashingEffects", true);
        DESKTOP_FILES = b.comment("Allow the mod to leave harmless text files on your desktop (at most 14, never overwrites)")
                .define("desktopFiles", true);
        SPEC = b.build();
    }
}
