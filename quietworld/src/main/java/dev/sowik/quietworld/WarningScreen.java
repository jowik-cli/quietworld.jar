package dev.sowik.quietworld;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Black intro screen shown once per launch, before the title screen. */
public class WarningScreen extends Screen {
    private static final String[] LINES = {
        "This game contains horror themes,",
        "unsettling sounds, and sudden events.",
        "Play somewhere quiet. Headphones recommended.",
        "You can turn events down in the config at any time.",
        "It already knows you are here."
    };
    private static final long LINE_DELAY = 1100L;
    private static final long LINE_FADE = 900L;
    private static final long FADE_OUT = 1200L;

    private final Screen next;
    private long start = -1L;
    private long leaveAt = -1L;

    public WarningScreen(Screen next) {
        super(Component.empty());
        this.next = next;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private long readyAfter() {
        return LINES.length * LINE_DELAY + 800L;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();
        if (start < 0L) start = now;
        long t = now - start;

        float master = 1.0F;
        if (leaveAt >= 0L) {
            master = 1.0F - (now - leaveAt) / (float) FADE_OUT;
            if (master <= 0.0F) {
                Minecraft.getInstance().setScreen(next);
                return;
            }
        }

        g.fill(0, 0, this.width, this.height, 0xFF000000);

        int y = this.height / 2 - LINES.length * 7;
        for (int i = 0; i < LINES.length; i++) {
            float a = Math.max(0.0F, Math.min(1.0F, (t - i * LINE_DELAY) / (float) LINE_FADE)) * master;
            int alpha = (int) (a * 255.0F);
            if (alpha >= 8) {
                boolean last = i == LINES.length - 1;
                int rgb = last ? 0x8B0000 : 0xDDDDDD;
                g.drawCenteredString(this.font, LINES[i], this.width / 2, y + i * 14 + (last ? 8 : 0), (alpha << 24) | rgb);
            }
        }

        if (t > readyAfter() && leaveAt < 0L) {
            int blink = (int) (90 + 60 * Math.sin(t / 400.0D));
            g.drawCenteredString(this.font, "Press any key to continue", this.width / 2, this.height - 40, (blink << 24) | 0x888888);
        }
    }

    private boolean tryLeave() {
        long now = Util.getMillis();
        if (start >= 0L && now - start > readyAfter() && leaveAt < 0L) {
            leaveAt = now;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return tryLeave();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return tryLeave();
    }
}
