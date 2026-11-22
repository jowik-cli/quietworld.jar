package dev.sowik.quietworld;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/** The archive: tabs on the left, entries in the middle, details on the right. Locked entries show as ???. */
public class CodexScreen extends Screen {
    private static final Codex.Category[] CATS = Codex.Category.values();

    private record Row(String id, String label, boolean open) {}
    private record DLine(FormattedCharSequence text, int color) {}

    private final Screen parent;
    private int tab;
    private int scroll;
    private int detailScroll;
    private int maxDetailScroll;
    private String selected;

    private int px;
    private int py;
    private int pw;
    private int ph;
    private int tabsX;
    private int listX;
    private int listW;
    private int detailX;
    private int detailW;

    public CodexScreen(Screen parent, int tab) {
        super(Component.literal("Archive"));
        this.parent = parent;
        this.tab = Math.max(0, Math.min(tab, CATS.length));
    }

    @Override
    protected void init() {
        pw = Math.min(this.width - 24, 580);
        ph = this.height - 30;
        px = (this.width - pw) / 2;
        py = 15;
        tabsX = px + 10;
        listX = px + 118;
        listW = 152;
        detailX = px + 282;
        detailW = Math.max(60, pw - 292);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    private String tabLabel(int i) {
        if (i == CATS.length) {
            return "notes " + ClientProgress.notes().size();
        }
        List<Codex.Entry> all = Codex.entriesOf(CATS[i]);
        int open = 0;
        for (Codex.Entry e : all) {
            if (ClientProgress.has(e.id())) open++;
        }
        return CATS[i].label() + " " + open + "/" + all.size();
    }

    private List<Row> rows() {
        List<Row> rows = new ArrayList<>();
        if (tab == CATS.length) {
            for (SyncPacket.NoteData n : ClientProgress.notes()) {
                rows.add(new Row("note:" + n.id(), n.title(), true));
            }
        } else {
            for (Codex.Entry e : Codex.entriesOf(CATS[tab])) {
                boolean open = ClientProgress.has(e.id());
                rows.add(new Row(e.id(), open ? e.title() : "???", open));
            }
        }
        return rows;
    }

    private String desktopLabel() {
        return "[ remove desktop files (" + ClientProgress.desktopCount() + ") ]";
    }

    private int visibleRows() {
        return Math.max(1, (ph - 70) / 12);
    }

    private void wrap(List<DLine> out, String text, int color) {
        for (FormattedCharSequence s : this.font.split(FormattedText.of(text), detailW)) {
            out.add(new DLine(s, color));
        }
    }

    private void header(List<DLine> out, String text) {
        out.add(new DLine(FormattedCharSequence.forward(text, net.minecraft.network.chat.Style.EMPTY), 0xFFB03030));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();
        g.fill(0, 0, this.width, this.height, 0xFF050506);
        MenuFx.staticNoise(g, this.width, this.height, now, 220, 24);

        g.fill(px, py, px + pw, py + ph, 0xF0101012);
        g.fill(px, py, px + pw, py + 1, 0xFF5A0F0F);
        g.fill(px, py + ph - 1, px + pw, py + ph, 0xFF5A0F0F);
        g.fill(px, py, px + 1, py + ph, 0xFF5A0F0F);
        g.fill(px + pw - 1, py, px + pw, py + ph, 0xFF5A0F0F);

        MenuFx.glitchText(g, this.font, "ARCHIVE", px + 10, py + 8, 2.0F, 0xFFE0E0E0, now, this.width);
        String count = ClientProgress.count() + "/" + Codex.total() + " recorded";
        g.drawString(this.font, count, px + pw - 10 - this.font.width(count), py + 14, 0xFF777777, false);

        for (int i = 0; i <= CATS.length; i++) {
            boolean sel = i == tab;
            g.drawString(this.font, (sel ? "> " : "  ") + tabLabel(i), tabsX, py + 44 + i * 16,
                    sel ? 0xFFFFFFFF : 0xFF8A8A8A, false);
        }

        List<Row> rows = rows();
        int visible = visibleRows();
        scroll = Math.max(0, Math.min(scroll, Math.max(0, rows.size() - visible)));
        for (int r = 0; r < visible && r + scroll < rows.size(); r++) {
            Row row = rows.get(r + scroll);
            int y = py + 44 + r * 12;
            if (row.id().equals(selected)) {
                g.fill(listX - 2, y - 2, listX + listW, y + 10, 0x30FFFFFF);
            }
            String label = this.font.plainSubstrByWidth(row.label(), listW - 6);
            g.drawString(this.font, label, listX, y, row.open() ? 0xFFE0E0E0 : 0xFF555555, false);
        }
        if (rows.isEmpty()) {
            g.drawString(this.font, tab == CATS.length ? "no notes yet" : "nothing here", listX, py + 44, 0xFF555555, false);
        }

        g.fill(listX + listW + 4, py + 40, listX + listW + 5, py + ph - 24, 0xFF2A0808);
        drawDetail(g, now);

        g.drawString(this.font, "[ close ]", px + 10, py + ph - 16, 0xFF8A8A8A, false);
        if (ClientProgress.desktopCount() > 0) {
            String label = desktopLabel();
            g.drawString(this.font, label, px + pw - 10 - this.font.width(label), py + ph - 16, 0xFF8A4A4A, false);
        }
        MenuFx.scanlines(g, this.width, this.height);
    }

    private void drawDetail(GuiGraphics g, long now) {
        if (selected == null) {
            g.drawString(this.font, "select an entry", detailX, py + 44, 0xFF555555, false);
            return;
        }

        String title = "???";
        List<DLine> lines = new ArrayList<>();

        if (selected.startsWith("note:")) {
            String id = selected.substring(5);
            for (SyncPacket.NoteData n : ClientProgress.notes()) {
                if (n.id().equals(id)) {
                    title = n.title();
                    wrap(lines, "found note, signed " + n.author(), 0xFF777777);
                    for (String page : n.pages()) {
                        lines.add(new DLine(FormattedCharSequence.EMPTY, 0));
                        wrap(lines, page, 0xFFCFCFCF);
                    }
                }
            }
        } else {
            Codex.Entry e = Codex.get(selected);
            if (e == null) return;
            if (ClientProgress.has(e.id())) {
                title = e.title();
                wrap(lines, e.category().label(), 0xFF777777);
                lines.add(new DLine(FormattedCharSequence.EMPTY, 0));
                header(lines, "WHAT IT IS");
                wrap(lines, e.what(), 0xFFCFCFCF);
                lines.add(new DLine(FormattedCharSequence.EMPTY, 0));
                header(lines, "WHAT IT DOES");
                wrap(lines, e.does(), 0xFFCFCFCF);
                lines.add(new DLine(FormattedCharSequence.EMPTY, 0));
                header(lines, "LORE");
                wrap(lines, e.lore(), 0xFF9AA4C0);
            } else {
                wrap(lines, "Not yet encountered.", 0xFF777777);
                lines.add(new DLine(FormattedCharSequence.EMPTY, 0));
                wrap(lines, hint(e.category()), 0xFF555555);
            }
        }

        MenuFx.glitchText(g, this.font, title, detailX, py + 40, 1.5F, 0xFFE8E8E8, now, this.width);

        int top = py + 64;
        int bottom = py + ph - 24;
        int rowsShown = Math.max(1, (bottom - top) / 10);
        maxDetailScroll = Math.max(0, lines.size() - rowsShown);
        detailScroll = Math.max(0, Math.min(detailScroll, maxDetailScroll));

        g.enableScissor(detailX, top, detailX + detailW + 6, bottom);
        for (int i = detailScroll; i < lines.size() && (i - detailScroll) < rowsShown + 1; i++) {
            g.drawString(this.font, lines.get(i).text(), detailX, top + (i - detailScroll) * 10, lines.get(i).color(), false);
        }
        g.disableScissor();
        if (maxDetailScroll > 0) {
            g.drawString(this.font, "scroll", detailX + detailW - this.font.width("scroll"), py + 30, 0xFF444444, false);
        }
    }

    private static String hint(Codex.Category c) {
        switch (c) {
            case MOB: return "Meet it to record it.";
            case STRUCTURE: return "Find it in the world to record it.";
            case DIMENSION: return "Go there to record it.";
            case EVENT: return "Wait. It will happen.";
            case ITEM: return "Obtain it to record it.";
            default: return "Keep playing.";
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);

        for (int i = 0; i <= CATS.length; i++) {
            int y = py + 44 + i * 16;
            if (mx >= tabsX && mx <= tabsX + 105 && my >= y - 3 && my <= y + 11) {
                tab = i;
                scroll = 0;
                detailScroll = 0;
                selected = null;
                return true;
            }
        }

        List<Row> rows = rows();
        int visible = visibleRows();
        for (int r = 0; r < visible && r + scroll < rows.size(); r++) {
            int y = py + 44 + r * 12;
            if (mx >= listX - 2 && mx <= listX + listW && my >= y - 2 && my <= y + 10) {
                selected = rows.get(r + scroll).id();
                detailScroll = 0;
                return true;
            }
        }

        if (ClientProgress.desktopCount() > 0) {
            String label = desktopLabel();
            int lx = px + pw - 10 - this.font.width(label);
            if (mx >= lx && mx <= lx + this.font.width(label) && my >= py + ph - 20 && my <= py + ph - 6) {
                DesktopFiles.cleanup();
                return true;
            }
        }

        if (mx >= px + 8 && mx <= px + 70 && my >= py + ph - 20 && my <= py + ph - 6) {
            this.onClose();
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        int step = delta > 0 ? -1 : 1;
        if (mx >= detailX) {
            detailScroll = Math.max(0, Math.min(maxDetailScroll, detailScroll + step * 2));
        } else {
            scroll = Math.max(0, scroll + step);
        }
        return true;
    }
}
