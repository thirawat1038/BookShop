package ui;

// หน้าสรุปสต๊อกและยอดขาย

import app.ShopSystem;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import model.Book;
import model.Order;
import service.AdminAccounts;

final class AdminOverview extends AdminMenu.Page {
    AdminOverview(ShopSystem application, AdminAccounts.Session session,
            Runnable onInventoryChanged) {
        super(application, session, onInventoryChanged);
        add(buildDashboard());
    }

    private JPanel buildDashboard() {
        JPanel card = card("ภาพรวมร้านหนังสือ");
        JPanel center = new JPanel(new BorderLayout(0, 18));
        center.setOpaque(false);
        JPanel statistics = new JPanel(new GridLayout(1, 4, 12, 0));
        statistics.setOpaque(false);
        long stock = application.products.getAllProducts().stream().mapToLong(Book::getStock).sum();
        statistics.add(statistic("รายการหนังสือ", String.valueOf(application.products.getAllProducts().size())));
        statistics.add(statistic("คงเหลือ (เล่ม)", String.valueOf(stock)));
        statistics.add(statistic("มูลค่าสต๊อก (บาท)", money(application.products.calculateTotalStockValue())));
        statistics.add(statistic("ยอดขาย (บาท)", money(application.orders.getTotalSales())));
        center.add(statistics, BorderLayout.NORTH);
        DefaultTableModel model = ScreenParts.tableModel("เลขที่ออเดอร์", "ผู้ซื้อ / ช่องทาง", "วันที่", "ยอดรวม (บาท)");
        List<Order> orders = application.orders.getAllOrders();
        for (int index = orders.size() - 1; index >= Math.max(0, orders.size() - 10); index--) {
            Order order = orders.get(index);
            model.addRow(new Object[]{order.getOrderId(), order.getMemberId(), order.getDate(), money(order.getTotal())});
        }
        center.add(tableScroll(ScreenParts.table(model)), BorderLayout.CENTER);
        card.add(center, BorderLayout.CENTER);
        return page("Dashboard", "ภาพรวมสินค้าและยอดขายล่าสุด", card);
    }

    private JPanel statistic(String title, String value) {
        JPanel statistic = ScreenParts.roundedPanel(new Color(0xF0FCF8));
        statistic.setLayout(new BorderLayout(0, 5));
        JLabel label = new JLabel(title);
        label.setFont(ScreenStyle.font(Font.PLAIN, 11f));
        JLabel number = new JLabel(value);
        number.setFont(ScreenStyle.font(Font.BOLD, 18f));
        statistic.add(label, BorderLayout.NORTH);
        statistic.add(number, BorderLayout.CENTER);
        return statistic;
    }
}
