package ui;

// การ์ดหนังสือ ปก ราคา และปุ่มเพิ่มลงตะกร้า

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import model.Book;

final class BookCard extends JPanel {
    static final int CARD_W = 130;
    static final int COVER_H = 182;

    BookCard(Book product, Consumer<Book> onAddToCart, Consumer<Book> onOpen) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        BookCover cover = new BookCover(product, CARD_W, COVER_H);
        cover.setAlignmentX(LEFT_ALIGNMENT);
        add(cover);
        add(Box.createVerticalStrut(8));

        TextDrawing nameBlock = new TextDrawing(product.getName(), ScreenStyle.font(Font.PLAIN, 13f), ScreenStyle.TEXT, CARD_W, 2);
        add(nameBlock);
        add(new TextDrawing(product.getStock() > 0 ? "คงเหลือ " + product.getStock() + " เล่ม" : "สินค้าหมด",
                ScreenStyle.font(Font.PLAIN, 12f), product.getStock() > 0 ? ScreenStyle.MUTED : new Color(0xD64545), CARD_W, 1));
        add(new TextDrawing(product.getCategory().isBlank() ? "Books" : product.getCategory(), ScreenStyle.font(Font.PLAIN, 12f), ScreenStyle.MUTED, CARD_W, 1));
        add(Box.createVerticalStrut(2));

        JPanel priceRow = new JPanel(new BorderLayout());
        priceRow.setOpaque(false);
        priceRow.setAlignmentX(LEFT_ALIGNMENT);
        priceRow.setMaximumSize(new Dimension(CARD_W, 38));
        priceRow.setPreferredSize(new Dimension(CARD_W, 38));

        JLabel price = new JLabel(String.format(Locale.US, "%,.2f บาท", product.getPrice()));
        price.setFont(ScreenStyle.font(Font.BOLD, 14f));
        price.setForeground(ScreenStyle.TEXT);
        priceRow.add(price, BorderLayout.WEST);

        CartIconButton cartButton = new CartIconButton(product.getStock() > 0);
        cartButton.setToolTipText(product.getStock() > 0 ? "เพิ่มลงตะกร้า" : "สินค้าหมด");
        cartButton.addActionListener(e -> onAddToCart.accept(product));
        priceRow.add(cartButton, BorderLayout.EAST);
        add(priceRow);

        for (JComponent c : new JComponent[]{cover, nameBlock}) {
            onClick(c, () -> onOpen.accept(product));
            c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    static void onClick(JComponent c, Runnable action) {
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && c.contains(e.getPoint())) {
                    action.run();
                }
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(CARD_W, d.height);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    static final class BookCover extends JComponent {
        private final Book product;
        private final int coverWidth;
        private final int coverHeight;
        private BufferedImage image;

        BookCover(Book product, int coverWidth, int coverHeight) {
            this.product = product;
            this.coverWidth = coverWidth;
            this.coverHeight = coverHeight;
            setOpaque(false);
            String path = product.getImagePath();
            if (path != null && !path.isBlank()) {
                File file = new app.DataFiles().asset(path).toFile();
                if (file.isFile()) {
                    try {
                        image = ImageIO.read(file);
                    } catch (Exception ignored) {
                        image = null;
                    }
                }
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(coverWidth, coverHeight);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            ScreenStyle.antialias(g2);
            int w = getWidth();
            int h = getHeight();
            Shape shape = new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 10, 10);
            g2.setClip(shape);

            if (image != null) {
                double scale = Math.max((double) w / image.getWidth(), (double) h / image.getHeight());
                int dw = (int) Math.round(image.getWidth() * scale);
                int dh = (int) Math.round(image.getHeight() * scale);
                g2.drawImage(image, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
            } else {
                paintGenerated(g2, w, h);
            }

            g2.setClip(null);
            g2.setColor(new Color(0, 0, 0, 40));
            g2.draw(shape);
            g2.dispose();
        }

        private void paintGenerated(Graphics2D g2, int w, int h) {
            Color bottom;
            Color textColor = Color.WHITE;
            if (product.getColor() != null) {
                bottom = Color.decode(product.getColor());
                double brightness = (0.299 * bottom.getRed() + 0.587 * bottom.getGreen() + 0.114 * bottom.getBlue()) / 255;
                if (brightness > 0.7) {
                    textColor = new Color(0x222222);
                }
            } else {
                double colorFraction = (product.getId().hashCode() * 0.6180339887) % 1.0;
                float hue = (float) (colorFraction < 0 ? colorFraction + 1.0 : colorFraction);
                bottom = Color.getHSBColor((hue + 0.08f) % 1f, 0.65f, 0.80f);
            }
            g2.setColor(bottom);
            g2.fillRect(0, 0, w, h);

            g2.setColor(new Color(255, 255, 255, 60));
            g2.fillRect(0, h - 34, w, 2);

            g2.setColor(textColor);
            TextDrawing.drawLines(g2, product.getName(), ScreenStyle.font(Font.BOLD, 15f), 12, 28, w - 24, 5, true);
        }
    }

    static final class CartIconButton extends JButton {
        private final boolean enabledLook;

        CartIconButton(boolean available) {
            this.enabledLook = available;
            setEnabled(available);
            setPreferredSize(new Dimension(36, 36));
            setMinimumSize(new Dimension(36, 36));
            setMaximumSize(new Dimension(36, 36));
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(available ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            ScreenStyle.antialias(g2);
            int size = Math.min(getWidth(), getHeight()) - 2;
            g2.translate((getWidth() - size) / 2.0, (getHeight() - size) / 2.0);

            Color fill = !enabledLook ? new Color(0xC9C9C9)
                    : getModel().isPressed() ? ScreenStyle.BLUE_DARK
                    : getModel().isRollover() ? new Color(0x4FB8F5) : ScreenStyle.BLUE;
            g2.setColor(fill);
            g2.fill(new Ellipse2D.Float(0, 0, size, size));

            double s = size / 28.0;
            g2.scale(s, s);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D cart = new Path2D.Float();
            cart.moveTo(6, 8);
            cart.lineTo(9, 8);
            cart.lineTo(11.2, 17.5);
            cart.lineTo(19.8, 17.5);
            cart.lineTo(21.8, 11);
            cart.lineTo(9.6, 11);
            g2.draw(cart);
            g2.fill(new Ellipse2D.Float(10.6f, 19.6f, 3.2f, 3.2f));
            g2.fill(new Ellipse2D.Float(17.4f, 19.6f, 3.2f, 3.2f));
            g2.dispose();
        }
    }
}
