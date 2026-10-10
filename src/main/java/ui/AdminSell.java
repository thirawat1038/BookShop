package ui;

// หน้าเลือกหนังสือและบันทึกการขาย

import app.ShopSystem;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import model.Book;
import model.Order;
import service.AdminAccounts;

final class AdminSell extends AdminMenu.Page {
    private final Map<String, Integer> saleItems;
    AdminSell(ShopSystem application, AdminAccounts.Session session,
            Runnable onInventoryChanged, Map<String, Integer> saleItems) {
        super(application, session, onInventoryChanged);
        this.saleItems = saleItems;
        add(buildSell());
    }

    private JPanel buildSell() {
        JPanel card = card("เลือกหนังสือ");
        card.setBorder(BorderFactory.createEmptyBorder(10, 18, 6, 18));
        ((BorderLayout) card.getLayout()).setVgap(4);
        JTextField query = searchHeader(card, "เลือกหนังสือ");
        DefaultTableModel model = new DefaultTableModel(new String[]{"ชื่อหนังสือ", "ผู้แต่ง", "ราคา (บาท)", "จำนวนคงเหลือ", ""}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 4; }
        };
        JTable table = ScreenParts.table(model);
        List<Book> displayed = new ArrayList<>();
        JLabel selection = new JLabel();
        selection.setFont(ScreenStyle.font(Font.PLAIN, 12f));
        Runnable refresh = () -> {
            displayed.clear();
            displayed.addAll(application.inventory.search(session, query.getText()));
            model.setRowCount(0);
            displayed.forEach(book -> model.addRow(new Object[]{book.getName(), author(book), money(book.getPrice()), book.getStock(),
                    saleItems.containsKey(book.getId()) ? "เลือกแล้ว" : "เลือก"}));
            long count = saleItems.values().stream().mapToLong(Integer::longValue).sum();
            selection.setText("เลือก " + count + " เล่ม  |  " + money(saleTotal()) + " บาท");
        };
        ScreenParts.addButtonColumn(table, 4, row -> {
            Book book = displayed.get(row);
            chooseSaleQuantity(book);
            refresh.run();
        });
        table.getColumnModel().getColumn(0).setPreferredWidth(190);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        ScreenParts.onChange(query, refresh);
        refresh.run();
        card.add(tableScroll(table), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(selection, BorderLayout.WEST);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        JButton clear = ScreenParts.button("ล้างรายการ", new Color(0xEEEEEE), ScreenStyle.TEXT);
        clear.addActionListener(event -> { saleItems.clear(); refresh.run(); });
        JButton sell = ScreenParts.button("วางขาย", new Color(0xAEF56D), ScreenStyle.TEXT);
        clear.setPreferredSize(new java.awt.Dimension(100, 28));
        sell.setPreferredSize(new java.awt.Dimension(80, 28));
        sell.setToolTipText("บันทึกการขายให้ลูกค้าและหักสต๊อก");
        sell.addActionListener(event -> sellSelected(refresh));
        buttons.add(clear);
        buttons.add(sell);
        bottom.add(buttons, BorderLayout.EAST);
        card.add(bottom, BorderLayout.SOUTH);
        return page("Sell Book", "ระบบขายหนังสือให้ลูกค้า", card);
    }

    private void chooseSaleQuantity(Book book) {
        if (book.getStock() == 0) { inform("หนังสือหมดสต๊อก"); return; }
        int previous = Math.min(saleItems.getOrDefault(book.getId(), 1), book.getStock());
        JSpinner quantity = new JSpinner(new SpinnerNumberModel(previous, 0, book.getStock(), 1));
        JPanel form = ScreenParts.labelled(book.getName() + " (กรอก 0 เพื่อนำออก)", quantity, false);
        if (JOptionPane.showConfirmDialog(this, form, "เลือกจำนวนหนังสือ", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            quantity.commitEdit();
            int count = (Integer) quantity.getValue();
            if (count == 0) saleItems.remove(book.getId());
            else saleItems.put(book.getId(), count);
        } catch (java.text.ParseException error) { inform("กรุณากรอกจำนวนเป็นตัวเลข"); }
    }

    private double saleTotal() {
        return saleItems.entrySet().stream().mapToDouble(entry -> application.products.getProductById(entry.getKey())
                .map(book -> book.getPrice() * entry.getValue()).orElse(0.0)).sum();
    }

    private void sellSelected(Runnable refresh) {
        if (saleItems.isEmpty()) { inform("กรุณาเลือกหนังสือก่อนวางขาย"); return; }
        StringBuilder summary = new StringBuilder();
        saleItems.forEach((id, quantity) -> application.products.getProductById(id)
                .ifPresent(book -> summary.append(book.getName()).append(" × ").append(quantity).append('\n')));
        summary.append("รวม ").append(money(saleTotal())).append(" บาท\nยืนยันการขายและหักสต๊อก?");
        if (JOptionPane.showConfirmDialog(this, summary.toString(), "ยืนยันการขาย", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) return;
        perform(() -> {
            Order order = application.inventory.sell(session, new LinkedHashMap<>(saleItems));
            saleItems.clear();
            inventoryChanged();
            refresh.run();
            return "ขายสำเร็จ\nเลขที่ออเดอร์: " + order.getOrderId() + "\nยอดรวม: " + money(order.getTotal()) + " บาท";
        });
    }
}
