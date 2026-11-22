package dev.sowik.quietworld;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Random;

/** Drawing helpers shared by the custom menus: TV static, scanlines, vignette and glitching text. */
public final class MenuFx {
    private MenuFx() {}

    /** Flickering grey static. The pattern changes about 14 times a second. */
    public static void staticNoise(GuiGraphics g, int w, int h, long timeMs, int density, int maxAlpha) {
        Random r = new Random((timeMs / 70L) * 7919L);
        for (int i = 0; i < density; i++) {
            int x = r.nextInt(Math.max(1, w));
            int y = r.nextInt(Math.max(1, h));
            int size = 1 + r.nextInt(3);
            int v = 40 + r.nextInt(120);
            int a = 10 + r.nextInt(Math.max(1, maxAlpha));
            g.fill(x, y, x + size * 2, y + size, (a << 24) | (v << 16) | (v << 8) | v);
        }
    }

    public static void scanlines(GuiGraphics g, int w, int h) {
        for (int y = 0; y < h; y += 3) {
            g.fill(0, y, w, y + 1, 0x22000000);
        }
    }

    public static void vignette(GuiGraphics g, int w, int h) {
        g.fillGradient(0, 0, w, h / 3, 0xCC000000, 0x00000000);
        g.fillGradient(0, h - h / 3, w, h, 0x00000000, 0xCC000000);
    }

    /** Text with a red and a cyan ghost, and now and then a shifted horizontal slice. */
    public static void glitchText(GuiGraphics g, Font font, String text, int x, int y, float scale,
                                  int color, long timeMs, int screenWidth) {
        Random r = new Random((timeMs / 90L) * 31L + text.hashCode());
        int off = r.nextInt(14) == 0 ? 3 + r.nextInt(5) : 2;

        PoseStack ps = g.pose();
        ps.pushPose();
        ps.translate(x, y, 0.0D);
        ps.scale(scale, scale, 1.0F);
        g.drawString(font, text, -off, 0, 0x88FF0044, false);
        g.drawString(font, text, off, 0, 0x8800E5FF, false);
        g.drawString(font, text, 0, 0, color, false);
        ps.popPose();

        if (r.nextInt(12) == 0) {
            int textHeight = (int) (font.lineHeight * scale);
            int sliceY = y + r.nextInt(Math.max(1, textHeight - 4));
            int shift = (r.nextBoolean() ? 1 : -1) * (6 + r.nextInt(14));
            g.enableScissor(0, sliceY, screenWidth, sliceY + 3 + r.nextInt(5));
            ps.pushPose();
            ps.translate(x + shift, y, 0.0D);
            ps.scale(scale, scale, 1.0F);
            g.drawString(font, text, 0, 0, color, false);
            ps.popPose();
            g.disableScissor();
        }
    }
}
