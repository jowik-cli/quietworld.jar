package dev.sowik.quietworld;

import net.minecraft.client.Minecraft;

/** Client-only entry points the items call through DistExecutor. */
public final class ClientHooks {
    private ClientHooks() {}

    public static void openCodex() {
        Minecraft.getInstance().setScreen(new CodexScreen(null, 0));
    }

    public static void openJournal() {
        Minecraft.getInstance().setScreen(new JournalScreen());
    }
}
