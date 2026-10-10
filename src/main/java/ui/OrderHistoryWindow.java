package ui;

// หน้าต่างประวัติคำสั่งซื้อ

import java.awt.Font;
import java.util.Locale;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Member;
import model.Order;
import model.OrderLine;
import service.OrderManager;

final class OrderHistoryWindow {
    private OrderHistoryWindow() { }
    static void show(JFrame owner, Member member, OrderManager orders) {
        String memberId = member.getId();
        JDialog dialog = new JDialog(owner, "ประวัติคำสั่งซื้อของ " + member.getUsername(), true);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"เลขที่ออเดอร์", "รหัสสมาชิก", "วันที่", "ยอดรวม (บาท)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Order order : orders.getOrdersByMemberId(memberId)) {
            model.addRow(new Object[]{order.getOrderId(), order.getMemberId(), order.getDate(),
                    String.format(Locale.US, "%,.2f", order.getTotal())});
        }
        JTable table = ScreenParts.table(model);

        JTextArea detail = new JTextArea(8, 20);
        detail.setEditable(false);
        detail.setBorder(new EmptyBorder(8, 8, 8, 8));
        detail.setFont(ScreenStyle.font(Font.PLAIN, 14f));

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || table.getSelectedRow() == -1) {
                return;
            }
            String orderId = (String) model.getValueAt(table.getSelectedRow(), 0);
            Optional<Order> orderOpt = orders.getOrderById(orderId);
            if (orderOpt.isEmpty()) {
                return;
            }
            Order order = orderOpt.get();
            StringBuilder sb = new StringBuilder();
            sb.append("เลขที่ออเดอร์: ").append(order.getOrderId()).append("\n");
            sb.append("รหัสสมาชิก: ").append(order.getMemberId()).append("\n");
            sb.append("วันที่: ").append(order.getDate()).append("\n\n");
            for (OrderLine item : order.getItems()) {
                sb.append(String.format(Locale.US, "- %s x%d = %,.2f บาท%n",
                        item.getProductName(), item.getQuantity(), item.getSubtotal()));
            }
            sb.append(String.format(Locale.US, "%nยอดรวมทั้งหมด: %,.2f บาท", order.getTotal()));
            detail.setText(sb.toString());
            detail.setCaretPosition(0);
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("คำสั่งซื้อของฉัน"));
        JScrollPane detailScroll = new JScrollPane(detail);
        detailScroll.setBorder(BorderFactory.createTitledBorder("รายละเอียดออเดอร์"));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, detailScroll);
        split.setResizeWeight(0.55);
        split.setBorder(new EmptyBorder(12, 12, 12, 12));

        dialog.setContentPane(split);
        dialog.setSize(700, 520);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }
}
