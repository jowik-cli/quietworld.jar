package dev.sowik.quietworld;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/** A worn, stained book that holds every note the player has found. */
public class JournalScreen extends Screen {
    private int page = 0;
    private int x0;
    private int y0;
    private int bw;
    private int bh;

    public JournalScreen() {
        super(Component.literal("Journal"));
    }

    @Override
    protected void init() {
        bw = Math.min(this.width - 40, 260);
        bh = Math.min(this.height - 40, 300);
        x0 = (this.width - bw) / 2;
        y0 = (this.height - bh) / 2;
    }

    private List<String> pages() {
        List<String> pages = new ArrayList<>();
        List<SyncPacket.NoteData> notes = ClientProgress.notes();
        if (notes.isEmpty()) {
            pages.add("The journal is empty.\n\nNotes you find are copied here.");
            return pages;
        }
        pages.add("\u00a7lJOURNAL\u00a7r\n\n" + notes.size() + " notes\n\n\u00a77they were all written by someone. most of them were not you.\u00a7r");
        for (SyncPacket.NoteData n : notes) {
            for (int i = 0; i < n.pages().size(); i++) {
                String head = i == 0 ? "\u00a7l" + n.title() + "\u00a7r\n\u00a77by " + n.author() + "\u00a7r\n\n" : "";
                pages.add(head + n.pages().get(i));
            }
        }
        return pages;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();
        g.fill(0, 0, this.width, this.height, 0xFF050506);
        MenuFx.staticNoise(g, this.width, this.height, now, 140, 20);

        g.fill(x0 - 6, y0 - 6, x0 + bw + 6, y0 + bh + 6, 0xFF1A1208);
        g.fill(x0, y0, x0 + bw, y0 + bh, 0xFFD6C9A8);
        g.fill(x0, y0, x0 + 3, y0 + bh, 0xFFB8A982);
        g.fill(x0 + bw - 3, y0, x0 + bw, y0 + bh, 0xFFB8A982);

        List<String> pages = pages();
        page = Math.max(0, Math.min(page, pages.size() - 1));

        int y = y0 + 14;
        for (FormattedCharSequence line : this.font.split(FormattedText.of(pages.get(page)), bw - 28)) {
            if (y > y0 + bh - 30) break;
            g.drawString(this.font, line, x0 + 14, y, 0xFF2B2216, false);
            y += 10;
        }

        String counter = (page + 1) + " / " + pages.size();
        g.drawString(this.font, counter, x0 + (bw - this.font.width(counter)) / 2, y0 + bh - 16, 0xFF5A4A30, false);
        g.drawString(this.font, "<", x0 + 12, y0 + bh - 16, page > 0 ? 0xFF2B2216 : 0xFF9A8C6C, false);
        g.drawString(this.font, ">", x0 + bw - 18, y0 + bh - 16, page < pages.size() - 1 ? 0xFF2B2216 : 0xFF9A8C6C, false);
        MenuFx.scanlines(g, this.width, this.height);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && my >= y0 + bh - 22 && my <= y0 + bh - 6) {
            if (mx >= x0 + 6 && mx <= x0 + 30) {
                page = Math.max(0, page - 1);
                return true;
            }
            if (mx >= x0 + bw - 30 && mx <= x0 + bw - 6) {
                page++;
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 263) { // left arrow
            page = Math.max(0, page - 1);
            return true;
        }
        if (keyCode == 262) { // right arrow
            page++;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
