package ui;

// แสดงรายละเอียดหนังสือที่เลือก

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import model.Book;
import ui.BookCard.BookCover;
import ui.ScreenControls.LinkButton;
import ui.ScreenControls.PillButton;

final class BookDetails extends JPanel {
    BookDetails(Book product, Runnable onBack, java.util.function.Consumer<Book> onAddToCart) {
        super(new BorderLayout());
        setBackground(ScreenStyle.PAGE_BG);
        LinkButton back = new LinkButton("< ย้อนกลับ");
        back.setForeground(ScreenStyle.MUTED);
        back.addActionListener(e -> onBack.run());
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 8));
        backRow.setOpaque(false);
        backRow.add(back);

        final int infoWidth = 460;
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(new TextDrawing(product.getName(), ScreenStyle.font(Font.BOLD, 16f), ScreenStyle.TEXT, infoWidth, 2));
        info.add(new TextDrawing("รหัสสินค้า : " + product.getId(), ScreenStyle.font(Font.PLAIN, 12f), ScreenStyle.MUTED, infoWidth, 1));
        info.add(new TextDrawing("ประเภท : " + (product.getCategory().isBlank() ? "Books" : product.getCategory()), ScreenStyle.font(Font.PLAIN, 12f), ScreenStyle.MUTED, infoWidth, 1));
        if (!product.getAuthor().isBlank()) info.add(new TextDrawing("ผู้แต่ง : " + product.getAuthor(), ScreenStyle.font(Font.PLAIN, 12f), ScreenStyle.MUTED, infoWidth, 1));
        if (!product.getPublisher().isBlank()) info.add(new TextDrawing("สำนักพิมพ์ : " + product.getPublisher(), ScreenStyle.font(Font.PLAIN, 12f), ScreenStyle.MUTED, infoWidth, 1));
        info.add(Box.createVerticalStrut(8));

        JLabel price = new JLabel(String.format(Locale.US, "%,.2f บาท", product.getPrice()));
        price.setFont(ScreenStyle.font(Font.BOLD, 30f));
        price.setForeground(ScreenStyle.TEXT);
        price.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(price);
        info.add(Box.createVerticalStrut(10));

        boolean available = product.getStock() > 0;
        PillButton addToCart = new PillButton(available ? "เพิ่มลงตะกร้า" : "สินค้าหมด",
                available ? Color.BLACK : new Color(0xC9C9C9), new Color(0x333333), Color.WHITE);
        addToCart.setFont(ScreenStyle.font(Font.BOLD, 20f));
        addToCart.setPreferredSize(new Dimension(infoWidth - 160, 56));
        addToCart.setMaximumSize(new Dimension(infoWidth - 160, 56));
        addToCart.setAlignmentX(Component.LEFT_ALIGNMENT);
        addToCart.setEnabled(available);
        addToCart.addActionListener(event -> onAddToCart.accept(product));
        info.add(addToCart);
        info.add(Box.createVerticalStrut(14));

        info.add(new TextDrawing("รายละเอียด : " + (product.getDescription().isBlank() ? product.getName() : product.getDescription()), ScreenStyle.font(Font.PLAIN, 12f), ScreenStyle.TEXT, infoWidth, 12));
        info.add(Box.createVerticalStrut(4));
        info.add(new TextDrawing(available ? "คงเหลือ " + product.getStock() + " เล่ม" : "สินค้าหมด",
                ScreenStyle.font(Font.PLAIN, 12f), available ? ScreenStyle.TEXT : new Color(0xD64545), infoWidth, 1));

        BookCover cover = new BookCover(product, 260, 370);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints layout = new GridBagConstraints();
        layout.anchor = GridBagConstraints.NORTHWEST;
        layout.gridx = 0;
        layout.insets = new Insets(0, 0, 0, 40);
        content.add(cover, layout);
        layout.gridx = 1;
        layout.insets = new Insets(0, 0, 0, 0);
        content.add(info, layout);

        JPanel centerWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        centerWrap.setOpaque(false);
        centerWrap.add(content);

        JScrollPane scroll = new JScrollPane(centerWrap,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        add(backRow, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }
}
