package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.util.Locale;

/**
 * ข้อความหลายบรรทัดที่วาดด้วย TextLayout (จัดวางอักษรไทยซ้อนสระ/วรรณยุกต์ถูกต้อง)
 * ตัดคำตามหลักภาษาไทย และใส่ "…" เมื่อยาวเกินจำนวนบรรทัดที่กำหนด
 * ความสูงคงที่ = จำนวนบรรทัดสูงสุด จึงทำให้การ์ดหนังสือทุกใบสูงเท่ากัน
 */
final class TextBlock extends JComponent {

    private final String text;
    private final int width;
    private final int maxLines;

    TextBlock(String text, Font font, Color color, int width, int maxLines) {
        this.text = text == null ? "" : text;
        this.width = width;
        this.maxLines = maxLines;
        setFont(font);
        setForeground(color);
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width, lineHeight(getFontMetrics(getFont())) * maxLines + 2);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.antialias(g2);
        g2.setColor(getForeground());
        drawLines(g2, text, getFont(), 0, 1, width, maxLines, false);
        g2.dispose();
    }

    static int lineHeight(FontMetrics fm) {
        return fm.getAscent() + fm.getDescent() + 2;
    }

    /**
     * วาดข้อความแบบตัดบรรทัด
     *
     * @param top ตำแหน่ง y ของขอบบนของบรรทัดแรก
     */
    static void drawLines(Graphics2D g2, String text, Font font, int x, int top, int width,
                          int maxLines, boolean center) {
        if (text == null || text.isEmpty()) {
            return;
        }
        FontMetrics fm = g2.getFontMetrics(font);
        int lineH = lineHeight(fm);

        AttributedString as = new AttributedString(text);
        as.addAttribute(TextAttribute.FONT, font);
        AttributedCharacterIterator it = as.getIterator();
        LineBreakMeasurer measurer = new LineBreakMeasurer(
                it, BreakIterator.getLineInstance(Locale.of("th", "TH")), g2.getFontRenderContext());

        int line = 0;
        while (measurer.getPosition() < it.getEndIndex() && line < maxLines) {
            int start = measurer.getPosition();
            TextLayout layout = measurer.nextLayout(width);
            boolean needsEllipsis = line == maxLines - 1 && measurer.getPosition() < it.getEndIndex();
            if (needsEllipsis) {
                layout = ellipsize(text.substring(start), font, g2, width);
            }
            float lx = x;
            if (center) {
                lx = x + (width - layout.getAdvance()) / 2f;
            }
            float baseline = top + line * lineH + fm.getAscent();
            layout.draw(g2, lx, baseline);
            line++;
        }
    }

    private static TextLayout ellipsize(String rest, Font font, Graphics2D g2, int width) {
        String candidate = rest.stripTrailing();
        while (candidate.length() > 1) {
            TextLayout layout = new TextLayout(candidate + "…", font, g2.getFontRenderContext());
            if (layout.getAdvance() <= width) {
                return layout;
            }
            candidate = candidate.substring(0, candidate.length() - 1);
            // ไม่ทิ้งสระ/วรรณยุกต์ไว้ลอย ๆ ท้ายคำ
            while (candidate.length() > 1
                    && Character.getType(candidate.charAt(candidate.length() - 1)) == Character.NON_SPACING_MARK) {
                candidate = candidate.substring(0, candidate.length() - 1);
            }
        }
        return new TextLayout("…", font, g2.getFontRenderContext());
    }
}
