package dev.sowik.quietworld;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** The pause menu. It changes with the story phase and carries shortcuts to the archive and journal. */
public class QuietPauseScreen extends Screen {
    private static final String[] LINES = {
        "paused. it is not.",
        "pausing does not stop Herobrine.",
        "the world is holding its breath.",
        "wonderland.jar has no pause button. this is unrelated.",
        "the Watcher is also paused. suspiciously still.",
        "the Broken Script says hi. it is not paused.",
        "Entity 303 would like his rent."
    };
    private static final String[] LINES_TURNED = {
        "the notes are still being written while you read this.",
        "something is walking while you are paused.",
        "do not trust the second list of rules.",
        "the Hollow only moves when you stop. you just stopped."
    };
    private static final String[] LINES_GONE = {
        "k. cannot see your pause screen anymore.",
        "the game is not paused. it is waiting.",
        "when you resume, it will already be there.",
        "do not look behind you. the menu cannot help."
    };

    private record Entry(String label, Runnable action) {}

    private final List<Entry> entries = new ArrayList<>();
    private final Random pick = new Random();
    private final int lineIndex = pick.nextInt(100);
    private int hovered = -1;
    private boolean leaving;

    public QuietPauseScreen() {
        super(Component.literal("Paused"));
    }

    @Override
    protected void init() {
        entries.clear();
        Minecraft mc = Minecraft.getInstance();
        entries.add(new Entry("resume", this::onClose));
        entries.add(new Entry("progress", () -> {
            if (mc.player != null) mc.setScreen(new AdvancementsScreen(mc.player.connection.getAdvancements()));
        }));
        entries.add(new Entry("tally", () -> {
            if (mc.player != null) mc.setScreen(new StatsScreen(this, mc.player.getStats()));
        }));
        entries.add(new Entry("archive (" + ClientProgress.count() + "/" + Codex.total() + ")",
                () -> mc.setScreen(new CodexScreen(this, 0))));
        entries.add(new Entry("journal (" + ClientProgress.notes().size() + ")",
                () -> mc.setScreen(new JournalScreen())));
        entries.add(new Entry("settings", () -> mc.setScreen(new OptionsScreen(this, mc.options))));
        entries.add(new Entry("leave the save", this::leaveWorld));
    }

    /** Same sequence the vanilla pause menu runs for "Save and Quit to Title". */
    private void leaveWorld() {
        if (leaving) return;
        leaving = true;
        Minecraft mc = Minecraft.getInstance();
        boolean local = mc.isLocalServer();
        if (mc.level != null) {
            mc.level.disconnect();
        }
        if (local) {
            mc.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
        } else {
            mc.clearLevel();
        }
        QuietTitleScreen title = new QuietTitleScreen();
        if (local) {
            mc.setScreen(title);
        } else {
            mc.setScreen(new JoinMultiplayerScreen(title));
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();
        int phase = ClientProgress.phase();

        g.fill(0, 0, this.width, this.height, 0xB0000000 + (phase >= 2 ? 0x10000000 : 0));
        MenuFx.staticNoise(g, this.width, this.height, now, 260 + Math.min(phase, 3) * 140, 30 + Math.min(phase, 4) * 10);
        int tint = Phases.tint(phase);
        if (tint != 0) g.fill(0, 0, this.width, this.height, tint);
        MenuFx.vignette(g, this.width, this.height);

        int left = Math.max(40, this.width / 8);
        int top = this.height / 2 - 50;
        String title = phase >= 2 ? Corruption.garble("PAUSED", RandomSource.create(now / 200L), 0.3D) : "PAUSED";
        MenuFx.glitchText(g, this.font, title, left, top - 36, 3.0F, 0xFFD8D8D8, now, this.width);

        int barColor = Phases.barColor(phase);
        int y0 = top + 4;
        g.fill(left - 14, y0 - 6, left - 12, y0 + entries.size() * 18, barColor);

        hovered = -1;
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
            g.drawString(this.font, (hover ? "> " : "  ") + shown, left, y, hover ? 0xFFFFFFFF : 0xFF9A9A9A, false);
        }

        String[] pool = phase == 0 ? LINES : phase == 1 ? LINES_TURNED : phase == 2 ? LINES_GONE : Phases.pauseLines(phase);
        String line = pool[lineIndex % pool.length];
        if ((now / 150L) % 31L == 0L) {
            line = Corruption.garble(line, RandomSource.create(now / 150L), 0.2D);
        }
        g.drawString(this.font, line, left - 12, this.height - 30, 0xFF555555, false);
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
