package dev.sowik.quietworld;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraftforge.client.gui.ModListScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** The main menu. It reads your saved story progress and changes as the mod does. */
public class QuietTitleScreen extends Screen {
    private static final String[] FOOTERS = {
        "not affiliated with wonderland.jar. they asked nicely.",
        "The Broken Script is two doors down. do not knock.",
        "Herobrine is on his break.",
        "Entity 303 still owes rent.",
        "Null says hi. Null does not exist.",
        "the cave dweller knocked. you did not hear it.",
        "100% cursed, 0% backrooms (legally distinct)",
        "also try Terraria!",
        "this menu was not made by Mojang. probably."
    };
    private static final String[] FOOTERS_TURNED = {
        "K. is still writing. the handwriting is not K.'s.",
        "the Watcher is standing behind this menu.",
        "the notes contradict each other. that is on purpose.",
        "this menu remembers the last time you left.",
        "do not trust the second list of rules."
    };
    private static final String[] FOOTERS_GONE = {
        "k. is not here anymore.",
        "it knows which world you are about to open.",
        "the menu is the only place that is still safe. it is not safe.",
        "nobody is online. the list says one.",
        "you already pressed continue. this is the second time."
    };

    private record Entry(String label, Runnable action) {}

    private final List<Entry> entries = new ArrayList<>();
    private final int footerStart = new Random().nextInt(FOOTERS.length);
    private int hovered = -1;

    public QuietTitleScreen() {
        super(Component.literal("Quiet World"));
    }

    @Override
    protected void init() {
        entries.clear();
        Minecraft mc = Minecraft.getInstance();
        int phase = ClientProgress.phase();

        String cont = Phases.continueLabel(phase);
        entries.add(new Entry(cont, () -> mc.setScreen(new SelectWorldScreen(this))));
        entries.add(new Entry("join someone else's save", () -> mc.setScreen(new JoinMultiplayerScreen(this))));
        if (ClientProgress.count() > 0) {
            entries.add(new Entry("archive (" + ClientProgress.count() + "/" + Codex.total() + ")",
                    () -> mc.setScreen(new CodexScreen(this, 0))));
        }
        if (!ClientProgress.notes().isEmpty()) {
            entries.add(new Entry("journal (" + ClientProgress.notes().size() + ")",
                    () -> mc.setScreen(new JournalScreen())));
        }
        if (ClientProgress.has("mob_kosiw")) {
            entries.add(new Entry("kosiw is online", () -> mc.setScreen(new CodexScreen(this, 0))));
        }
        entries.add(new Entry("settings", () -> mc.setScreen(new OptionsScreen(this, mc.options))));
        entries.add(new Entry("the other jars", () -> mc.setScreen(new ModListScreen(this))));
        entries.add(new Entry(Phases.leaveLabel(phase), mc::stop));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private String footer(long now, int phase) {
        String[] pool = phase == 0 ? FOOTERS : phase == 1 ? FOOTERS_TURNED : phase == 2 ? FOOTERS_GONE : Phases.footers(phase);
        String line = pool[(int) ((footerStart + now / 6000L) % pool.length)];
        if ((now / 150L) % (phase >= 2 ? 11L : 23L) == 0L) {
            line = Corruption.garble(line, RandomSource.create(now / 150L), 0.2D);
        }
        return line;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();
        int phase = ClientProgress.phase();

        g.fill(0, 0, this.width, this.height, 0xFF050506);
        MenuFx.staticNoise(g, this.width, this.height, now, 380 + Math.min(phase, 3) * 220, 40 + Math.min(phase, 4) * 12);
        int tint = Phases.tint(phase);
        if (tint != 0) g.fill(0, 0, this.width, this.height, tint);
        MenuFx.vignette(g, this.width, this.height);

        // the Static Hall leaves a faint grid, the Hollow Copy leaves a doorway
        if (ClientProgress.has("dim_hall")) {
            for (int gx = 0; gx < 6; gx++) {
                for (int gy = 0; gy < 5; gy++) {
                    g.fill(this.width - 130 + gx * 20, this.height - 130 + gy * 20,
                            this.width - 130 + gx * 20 + 18, this.height - 130 + gy * 20 + 18, 0x14808080);
                }
            }
        }
        if (ClientProgress.has("dim_copy")) {
            int dx = this.width - 70;
            int dy = this.height / 2 - 50;
            g.fill(dx, dy, dx + 44, dy + 2, 0xFF3B1A5A);
            g.fill(dx, dy, dx + 2, dy + 90, 0xFF3B1A5A);
            g.fill(dx + 42, dy, dx + 44, dy + 90, 0xFF3B1A5A);
            g.fill(dx + 2, dy + 2, dx + 42, dy + 90, 0xFF000000);
        }

        String title = phase >= 2 ? Corruption.garble("QUIET WORLD", RandomSource.create(now / 200L), Math.min(0.5D, 0.1D * phase)) : "QUIET WORLD";
        int left = Math.max(40, this.width / 10);
        MenuFx.glitchText(g, this.font, title, left, this.height / 5, 4.0F, 0xFFE8E8E8, now, this.width);
        String sub = Phases.subtitle(phase);
        g.drawString(this.font, sub, left + 2, this.height / 5 + 40, phase >= 2 ? 0xFF8B2020 : 0xFF555555, false);

        hovered = -1;
        int y0 = this.height / 2 - 10;
        for (int i = 0; i < entries.size(); i++) {
            String label = entries.get(i).label();
            int y = y0 + i * 18;
            int boxWidth = this.font.width(label) + 24;
            boolean hover = mouseX >= left && mouseX <= left + boxWidth && mouseY >= y - 3 && mouseY <= y + 12;
            if (hover) hovered = i;

            String shown = label;
            if (hover && (now / 120L) % 9L == 0L) {
                shown = Corruption.garble(label, RandomSource.create(now / 120L + i), 0.25D);
            }
            g.drawString(this.font, (hover ? "> " : "  ") + shown, left, y, hover ? 0xFFFFFFFF : 0xFF8A8A8A, false);
        }

        g.drawString(this.font, footer(now, phase), left + 2, this.height - 24, 0xFF4A4A4A, false);
        String ver = "quietworld 0.3.0";
        g.drawString(this.font, ver, this.width - this.font.width(ver) - 8, this.height - 14, 0xFF333333, false);
        MenuFx.scanlines(g, this.width, this.height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hovered >= 0) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            entries.get(hovered).action().run();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
