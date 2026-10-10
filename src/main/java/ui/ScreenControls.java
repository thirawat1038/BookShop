package ui;

// คอมโพเนนต์ช่องค้นหา ปุ่ม และแถบเมนู

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

final class ScreenControls {
    private ScreenControls() { }

    static final class HintTextField extends JTextField {
        private final String hint;

        HintTextField(String hint) {
            this.hint = hint;
            setBorder(new EmptyBorder(6, 4, 6, 4));
            setOpaque(false);
            setFont(ScreenStyle.font(Font.PLAIN, 14f));
            setForeground(ScreenStyle.TEXT);
            setCaretColor(ScreenStyle.TEXT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                ScreenStyle.antialias(g2);
                g2.setColor(ScreenStyle.MUTED);
                Insets in = getInsets();
                Font font = getFont();
                int lineH = TextDrawing.lineHeight(g2.getFontMetrics(font));
                TextDrawing.drawLines(g2, hint, font, in.left, (getHeight() - lineH) / 2,
                        getWidth() - in.left - in.right, 1, false);
                g2.dispose();
            }
        }
    }

    static final class LinkButton extends JButton {
        LinkButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setMargin(new Insets(2, 2, 2, 2));
            setRolloverEnabled(true);
            setFont(ScreenStyle.font(Font.PLAIN, 14f));
            setForeground(ScreenStyle.TEXT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            ScreenStyle.antialias(g2);
            g2.setColor(getModel().isRollover() ? ScreenStyle.BLUE_DARK : ScreenStyle.TEXT);
            Font font = getFont();
            int lineH = TextDrawing.lineHeight(g2.getFontMetrics(font));
            TextDrawing.drawLines(g2, getText(), font, 0, (getHeight() - lineH) / 2, getWidth(), 1, true);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 20, 36);
        }
    }

    static final class PillButton extends JButton {
        private final Color base;
        private final Color hover;
        private final Color textColor;

        PillButton(String text, Color base, Color hover, Color textColor) {
            super(text);
            this.base = base;
            this.hover = hover;
            this.textColor = textColor;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setFont(ScreenStyle.font(Font.BOLD, 14f));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            ScreenStyle.antialias(g2);
            g2.setColor(getModel().isRollover() || getModel().isPressed() ? hover : base);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
            g2.setColor(textColor);
            Font font = getFont();
            int lineH = TextDrawing.lineHeight(g2.getFontMetrics(font));
            TextDrawing.drawLines(g2, getText(), font, 8, (getHeight() - lineH) / 2, getWidth() - 16, 1, true);
            g2.dispose();
        }
    }

    static final class SearchBox extends JPanel {
        private final JTextField field;

        SearchBox(JTextField field) {
            super(new BorderLayout());
            this.field = field;
            setOpaque(false);
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setPreferredSize(new Dimension(100, 46));

            JComponent icon = new JComponent() {
                {
                    setPreferredSize(new Dimension(30, 30));
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    ScreenStyle.antialias(g2);
                    g2.setColor(ScreenStyle.TEXT);
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int cx = getWidth() / 2 - 2;
                    int cy = getHeight() / 2 - 2;
                    g2.draw(new Ellipse2D.Float(cx - 7, cy - 7, 14, 14));
                    g2.draw(new Line2D.Float(cx + 5, cy + 5, cx + 11, cy + 11));
                    g2.dispose();
                }
            };
            add(icon, BorderLayout.WEST);
            add(field, BorderLayout.CENTER);
            BookCard.onClick(this, field::requestFocusInWindow);
            BookCard.onClick(icon, field::requestFocusInWindow);

            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            ScreenStyle.antialias(g2);
            RoundRectangle2D shape = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.setColor(ScreenStyle.WHITE);
            g2.fill(shape);
            g2.setColor(field.hasFocus() ? ScreenStyle.BLUE : ScreenStyle.LINE);
            g2.setStroke(new BasicStroke(field.hasFocus() ? 1.8f : 1f));
            g2.draw(shape);
            g2.dispose();
        }
    }

    static final class ScrollablePanel extends JPanel implements Scrollable {
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(40, (int) (visibleRect.height * 0.9));
        }

        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    static final class ShopTabBar extends JComponent {
        private final String[] labels;
        private final java.util.function.IntConsumer onSelect;
        private int selectedIndex;

        void setSelectedIndex(int index) { selectedIndex = index; repaint(); }

        ShopTabBar(String[] labels, java.util.function.IntConsumer onSelect) {
            this.labels = labels.clone();
            this.onSelect = onSelect;
            setPreferredSize(new Dimension(100, 46));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && contains(e.getPoint())) {
                        int index = Math.min(labels.length - 1, e.getX() * labels.length / Math.max(1, getWidth()));
                        onSelect.accept(index);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            ScreenStyle.antialias(g2);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(ScreenStyle.WHITE);
            g2.fillRect(0, 0, w, h);

            g2.setColor(ScreenStyle.LINE);
            g2.draw(new Line2D.Float(0, 0.5f, w, 0.5f));
            g2.draw(new Line2D.Float(0, h - 0.5f, w, h - 0.5f));

            int tabW = w / labels.length;
            for (int i = 0; i < labels.length; i++) {
                int x = i * tabW;
                int width = (i == labels.length - 1) ? w - x : tabW;
                boolean selected = i == selectedIndex;

                if (i > 0) {
                    g2.setColor(ScreenStyle.LINE);
                    g2.draw(new Line2D.Float(x + 0.5f, 0, x + 0.5f, h));
                }
                Font font = ScreenStyle.font(selected ? Font.BOLD : Font.PLAIN, 15f);
                int lineH = TextDrawing.lineHeight(g2.getFontMetrics(font));
                g2.setColor(ScreenStyle.TEXT);
                TextDrawing.drawLines(g2, labels[i] + "    •", font, x, (h - lineH) / 2, width, 1, true);

                if (selected) {
                    g2.setColor(new Color(0x222222));
                    g2.fillRect(x, h - 4, width, 4);
                }
            }
            g2.dispose();
        }
    }
}
