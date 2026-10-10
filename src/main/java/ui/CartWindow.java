package ui;

// หน้าต่างตะกร้าและยืนยันการซื้อ

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.Locale;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.OrderLine;
import ui.ScreenControls.PillButton;

final class CartWindow {
    private CartWindow() { }
    static void show(JFrame owner, List<OrderLine> cart, Runnable onCartChanged,
                     java.util.function.Predicate<Component> checkout) {
        JDialog dialog = new JDialog(owner, "ตะกร้าสินค้า", true);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"ชื่อหนังสือ", "จำนวน", "ยอดรวม (บาท)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = ScreenParts.table(model);
        JLabel totalLabel = new JLabel();
        totalLabel.setFont(ScreenStyle.font(Font.BOLD, 16f));

        Runnable refresh = () -> {
            model.setRowCount(0);
            double total = 0;
            for (OrderLine item : cart) {
                model.addRow(new Object[]{item.getProduct().getName(), item.getQuantity(),
                        String.format(Locale.US, "%,.2f", item.getSubtotal())});
                total += item.getSubtotal();
            }
            totalLabel.setText(String.format(Locale.US, "ยอดรวม: %,.2f บาท", total));
            onCartChanged.run();
        };
        refresh.run();

        PillButton removeButton = new PillButton("ลบรายการที่เลือก", new Color(0xD9D9D9), new Color(0xC8C8C8), ScreenStyle.TEXT);
        removeButton.setPreferredSize(new Dimension(160, 40));
        removeButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(dialog, "กรุณาเลือกรายการที่ต้องการลบ",
                        "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                return;
            }
            cart.remove(row);
            refresh.run();
        });

        PillButton clearButton = new PillButton("ลบทั้งหมด", new Color(0xF6DADA), new Color(0xEEC0C0), new Color(0xB03030));
        clearButton.setPreferredSize(new Dimension(120, 40));
        clearButton.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "ตะกร้าว่างอยู่แล้ว",
                        "แจ้งเตือน", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int ok = JOptionPane.showConfirmDialog(dialog, "ต้องการลบสินค้าทั้งหมดในตะกร้าใช่หรือไม่",
                    "ลบทั้งหมด", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
                cart.clear();
                refresh.run();
            }
        });

        PillButton checkoutButton = new PillButton("ชำระเงิน", ScreenStyle.BLUE, ScreenStyle.BLUE_DARK, Color.WHITE);
        checkoutButton.setPreferredSize(new Dimension(150, 40));
        checkoutButton.addActionListener(e -> {
            if (checkout.test(dialog)) {
                dialog.dispose();
            }
        });

        JPanel bottom = new JPanel(new BorderLayout(10, 10));
        bottom.setBorder(new EmptyBorder(12, 0, 0, 0));
        JPanel bottomLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bottomLeft.add(removeButton);
        bottomLeft.add(clearButton);
        JPanel bottomRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        bottomRight.add(totalLabel);
        bottomRight.add(checkoutButton);
        bottom.add(bottomLeft, BorderLayout.WEST);
        bottom.add(bottomRight, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(16, 16, 16, 16));
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setSize(760, 440);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }
}
