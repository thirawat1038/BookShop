package ui;

// หน้าค้นหา ปรับสต๊อก และลบหนังสือ

import app.ShopSystem;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
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
import service.AdminAccounts;

final class AdminSearch extends AdminMenu.Page {
    AdminSearch(ShopSystem application, AdminAccounts.Session session,
            Runnable onInventoryChanged) {
        super(application, session, onInventoryChanged);
        add(buildSearch());
    }

    private JPanel buildSearch() {
        JPanel card = card("จัดการหนังสือ");
        JTextField query = searchHeader(card, "จัดการหนังสือ");
        DefaultTableModel model = ScreenParts.tableModel("รหัส", "ชื่อหนังสือ", "ผู้แต่ง", "ราคา (บาท)", "จำนวนคงเหลือ");
        JTable table = ScreenParts.table(model);
        List<Book> displayed = new ArrayList<>();
        JLabel count = new JLabel();
        Runnable refresh = () -> {
            displayed.clear();
            displayed.addAll(application.inventory.search(session, query.getText()));
            model.setRowCount(0);
            displayed.forEach(book -> model.addRow(new Object[]{book.getId(), book.getName(), author(book), money(book.getPrice()), book.getStock()}));
            count.setText("พบ " + displayed.size() + " รายการ");
        };
        ScreenParts.onChange(query, refresh);
        refresh.run();
        card.add(tableScroll(table), BorderLayout.CENTER);
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        actions.add(count, BorderLayout.WEST);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        JButton adjust = ScreenParts.button("เพิ่ม / ลดสต๊อก", ScreenStyle.BLUE, Color.WHITE);
        JButton delete = ScreenParts.button("ลบหนังสือ", new Color(0xE45858), Color.WHITE);
        adjust.addActionListener(event -> withSelected(table, displayed, book -> adjustStock(book, refresh)));
        delete.addActionListener(event -> withSelected(table, displayed, book -> deleteBook(book, refresh)));
        buttons.add(adjust);
        buttons.add(delete);
        actions.add(buttons, BorderLayout.EAST);
        card.add(actions, BorderLayout.SOUTH);
        return page("Search", "ค้นหาหนังสือ เพิ่มหรือลดสต๊อก และลบรายการหนังสือ", card);
    }

    private void withSelected(JTable table, List<Book> displayed, java.util.function.Consumer<Book> action) {
        int row = table.getSelectedRow();
        if (row < 0) { inform("กรุณาเลือกหนังสือก่อน"); return; }
        action.accept(displayed.get(table.convertRowIndexToModel(row)));
    }

    private void adjustStock(Book book, Runnable refresh) {
        javax.swing.JComboBox<String> operation = new javax.swing.JComboBox<>(new String[]{"เพิ่มจำนวน", "ลดจำนวน"});
        JSpinner quantity = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.add(new JLabel(book.getName() + " — คงเหลือ " + book.getStock() + " เล่ม"));
        form.add(operation);
        form.add(quantity);
        if (JOptionPane.showConfirmDialog(this, form, "ปรับจำนวนสต๊อก", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        perform(() -> {
            quantity.commitEdit();
            int change = (Integer) quantity.getValue();
            application.inventory.adjustStock(session, book.getId(), operation.getSelectedIndex() == 0 ? change : -change);
            inventoryChanged();
            refresh.run();
            return "บันทึกจำนวนคงเหลือแล้ว";
        });
    }

    private void deleteBook(Book book, Runnable refresh) {
        if (JOptionPane.showConfirmDialog(this, "ลบหนังสือ “" + book.getName() + "” ออกจากรายการสินค้า?\nประวัติการขายยังคงอยู่",
                "ยืนยันการลบ", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        perform(() -> {
            application.inventory.deleteBook(session, book.getId());
            inventoryChanged();
            refresh.run();
            return "ลบหนังสือแล้ว";
        });
    }
}
