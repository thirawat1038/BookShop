package ui;

// วาดและตัดข้อความให้พอดีพื้นที่

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.util.Locale;
import javax.swing.JComponent;

final class TextDrawing extends JComponent {
    private final String text;
    private final int width;
    private final int maxLines;

    TextDrawing(String text, Font font, Color color, int width, int maxLines) {
        this.text = text == null ? "" : text;
        this.width = width;
        this.maxLines = maxLines;
        setFont(font);
        setForeground(color);
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    public Dimension getPreferredSize() {
        return new Dimension(width, lineHeight(getFontMetrics(getFont())) * maxLines + 2);
    }

    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ScreenStyle.antialias(g2);
        g2.setColor(getForeground());
        drawLines(g2, text, getFont(), 0, 1, width, maxLines, false);
        g2.dispose();
    }

    static int lineHeight(FontMetrics metrics) {
        return metrics.getAscent() + metrics.getDescent() + 2;
    }

    static void drawLines(Graphics2D g2, String text, Font font, int x, int top, int width,
                          int maxLines, boolean center) {
        if (text == null || text.isEmpty() || width <= 0 || maxLines <= 0) {
            return;
        }
        FontMetrics metrics = g2.getFontMetrics(font);
        int lineHeight = lineHeight(metrics);

        AttributedString attributedText = new AttributedString(text);
        attributedText.addAttribute(TextAttribute.FONT, font);
        AttributedCharacterIterator iterator = attributedText.getIterator();
        LineBreakMeasurer measurer = new LineBreakMeasurer(
                iterator, BreakIterator.getLineInstance(Locale.of("th", "TH")), g2.getFontRenderContext());

        int line = 0;
        while (measurer.getPosition() < iterator.getEndIndex() && line < maxLines) {
            int start = measurer.getPosition();
            TextLayout layout = measurer.nextLayout(width);
            boolean needsEllipsis = line == maxLines - 1 && measurer.getPosition() < iterator.getEndIndex();
            if (needsEllipsis) {
                layout = ellipsize(text.substring(start), font, g2, width);
            }
            float drawX = x;
            if (center) {
                drawX = x + (width - layout.getAdvance()) / 2f;
            }
            float baseline = top + line * lineHeight + metrics.getAscent();
            layout.draw(g2, drawX, baseline);
            line++;
        }
    }

    private static TextLayout ellipsize(String remainingText, Font font, Graphics2D g2, int width) {
        String candidate = remainingText.stripTrailing();
        while (candidate.length() > 1) {
            TextLayout layout = new TextLayout(candidate + "…", font, g2.getFontRenderContext());
            if (layout.getAdvance() <= width) {
                return layout;
            }
            candidate = candidate.substring(0, candidate.length() - 1);

            // ไม่ทิ้งสระหรือวรรณยุกต์ลอยที่ท้ายข้อความเมื่อย่อด้วย …
            while (candidate.length() > 1
                    && Character.getType(candidate.charAt(candidate.length() - 1)) == Character.NON_SPACING_MARK) {
                candidate = candidate.substring(0, candidate.length() - 1);
            }
        }
        return new TextLayout("…", font, g2.getFontRenderContext());
    }
}
