package ui;

// หน้ากรอกข้อมูลหนังสือใหม่

import app.ShopSystem;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import model.Book;
import service.AdminAccounts;

final class AdminAddBook extends AdminMenu.Page {
    private final java.util.function.IntConsumer onNavigate;
    AdminAddBook(ShopSystem application, AdminAccounts.Session session,
            Runnable onInventoryChanged, java.util.function.IntConsumer onNavigate) {
        super(application, session, onInventoryChanged);
        this.onNavigate = onNavigate;
        add(buildAddBook());
    }

    private JPanel buildAddBook() {
        JPanel card = card("ข้อมูลหนังสือ");
        card.setBorder(BorderFactory.createEmptyBorder(10, 18, 6, 18));
        ((BorderLayout) card.getLayout()).setVgap(4);
        JTextField name = ScreenParts.input("เช่น วันพีซ, Harry Potter");
        JTextField author = ScreenParts.input("เช่น J.K. Rowling");
        JComboBox<String> category = new JComboBox<>(Book.CATEGORIES.toArray(String[]::new));
        category.setFont(ScreenStyle.font(Font.PLAIN, 12f));
        category.setBackground(Color.WHITE);
        category.setSelectedIndex(-1);
        category.setToolTipText("เลือกหมวดหมู่");
        JTextField publisher = ScreenParts.input("เช่น Oxford, Nanmeebooks");
        JTextField price = ScreenParts.input("เช่น 250");
        JTextField quantity = ScreenParts.input("เช่น 10");
        List.of(name, author, category, publisher, price, quantity).forEach(field -> {
            field.setPreferredSize(new Dimension(160, 28));
            field.setMinimumSize(new Dimension(100, 28));
        });
        JTextArea description = new JTextArea(0, 30) {
            @Override protected void paintComponent(java.awt.Graphics graphics) {
                super.paintComponent(graphics);
                if (getText().isEmpty()) {
                    java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
                    ScreenStyle.antialias(g);
                    g.setColor(new Color(0xAAAAAA));
                    TextDrawing.drawLines(g, "เพิ่มรายละเอียดเกี่ยวกับหนังสือ (ถ้ามี)", getFont(), 8, 6,
                            getWidth() - 16, 1, false);
                    g.dispose();
                }
            }
        };
        description.setFont(ScreenStyle.font(Font.PLAIN, 12f));
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        description.setToolTipText("เพิ่มรายละเอียดเกี่ยวกับหนังสือ (ถ้ามี)");
        ((AbstractDocument) description.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override public void insertString(FilterBypass bypass, int offset, String text, AttributeSet attributes) throws BadLocationException {
                replace(bypass, offset, 0, text, attributes);
            }
            @Override public void replace(FilterBypass bypass, int offset, int length, String text, AttributeSet attributes) throws BadLocationException {
                String replacement = text == null ? "" : text;
                int capacity = 500 - (bypass.getDocument().getLength() - length);
                if (capacity >= 0) super.replace(bypass, offset, length, replacement.substring(0, Math.min(capacity, replacement.length())), attributes);
            }
        });
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        addFormField(fields, ScreenParts.labelled("ชื่อหนังสือ", name, true), 0, 0);
        addFormField(fields, ScreenParts.labelled("ผู้แต่ง", author, true), 1, 0);
        addFormField(fields, ScreenParts.labelled("หมวดหมู่", category, true), 2, 0);
        addFormField(fields, ScreenParts.labelled("สำนักพิมพ์", publisher, true), 0, 1);
        addFormField(fields, ScreenParts.labelled("ราคา (บาท)", price, true), 1, 1);
        addFormField(fields, ScreenParts.labelled("จำนวน (เล่ม)", quantity, true), 2, 1);
        JScrollPane descriptionScroll = new JScrollPane(description);
        descriptionScroll.setBorder(BorderFactory.createEmptyBorder());
        descriptionScroll.setOpaque(false);
        descriptionScroll.getViewport().setOpaque(false);
        description.setOpaque(false);
        JPanel descriptionBox = ScreenParts.roundedPanel(Color.WHITE);
        descriptionBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        descriptionBox.setLayout(new BorderLayout());
        descriptionBox.add(descriptionScroll);
        JPanel details = ScreenParts.labelled("รายละเอียด", descriptionBox, false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 3;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 0, 0);
        fields.add(details, constraints);
        JLabel counter = new JLabel("0/500", SwingConstants.RIGHT);
        counter.setFont(ScreenStyle.font(Font.PLAIN, 11f));
        counter.setForeground(ScreenStyle.MUTED);
        counter.setPreferredSize(new Dimension(100, 12));
        ScreenParts.onChange(description.getDocument(), () -> counter.setText(description.getText().length() + "/500"));
        constraints.gridy = 3;
        constraints.weighty = 0;
        constraints.insets = new Insets(2, 0, 0, 0);
        fields.add(counter, constraints);
        card.add(fields, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 0));
        buttons.setOpaque(false);
        JButton cancel = ScreenParts.button("ยกเลิก", new Color(0xEEEEEE), ScreenStyle.TEXT);
        JButton save = ScreenParts.button("บันทึก", ScreenStyle.BLUE, Color.WHITE);
        cancel.setPreferredSize(new Dimension(140, 28));
        save.setPreferredSize(new Dimension(140, 28));
        cancel.addActionListener(event -> {
            boolean entered = List.of(name, author, publisher, price, quantity).stream().anyMatch(field -> !field.getText().isBlank())
                    || category.getSelectedItem() != null || !description.getText().isBlank();
            if (!entered || JOptionPane.showConfirmDialog(this, "ยกเลิกข้อมูลหนังสือที่กรอกไว้?", "ยกเลิก",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) onNavigate.accept(1);
        });
        save.addActionListener(event -> perform(() -> {
            if (category.getSelectedItem() == null) throw new IllegalArgumentException("กรุณาเลือกหมวดหมู่");
            double amount;
            int stock;
            try {
                amount = Double.parseDouble(price.getText().trim());
                stock = Integer.parseInt(quantity.getText().trim());
            } catch (NumberFormatException error) { throw new IllegalArgumentException("กรุณากรอกราคาเป็นตัวเลข และจำนวนเป็นจำนวนเต็ม"); }
            Book book = application.inventory.addBook(session, name.getText(), author.getText(), (String) category.getSelectedItem(),
                    publisher.getText(), amount, stock, description.getText());
            inventoryChanged();
            onNavigate.accept(3);
            return "เพิ่มหนังสือแล้ว\nรหัสสินค้า: " + book.getId();
        }));
        buttons.add(cancel);
        buttons.add(save);
        card.add(buttons, BorderLayout.SOUTH);
        return page("Add Book", "เพิ่มหนังสือใหม่เข้าสู่ระบบ", card);
    }

    private void addFormField(JPanel form, JPanel field, int column, int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = row;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 8, column == 2 ? 0 : 24);
        form.add(field, constraints);
    }
}
