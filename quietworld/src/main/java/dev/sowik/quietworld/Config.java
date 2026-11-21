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
    public static final ForgeConfigSpec.IntValue MAX_RITUALS;

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
        MAX_RITUALS = b.comment("Maximum ritual sites the mod builds for each player")
                .defineInRange("maxRituals", 3, 0, 50);
        SPEC = b.build();
    }
}
