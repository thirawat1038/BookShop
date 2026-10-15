package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import model.Member;
import model.Order;
import model.OrderItem;
import model.Product;
import service.DashboardService;
import service.DashboardService.MemberSales;
import service.DashboardService.ProductSales;
import service.MemberService;
import service.OrderService;
import service.ProductService;

/**
 * หน้า dashboard สำหรับ admin: การ์ดตัวเลขสรุปด้านบน + 4 แท็บ
 * (สินค้าขายดี / สต๊อกสินค้า / คำสั่งซื้อ / สมาชิก)
 *
 * ตัวเลขทั้งหมดอ่าน ณ ตอนสร้างหน้านี้ ข้อมูลที่แก้ไขได้จากหน้านี้มีอย่างเดียวคือสต๊อกสินค้า
 * (แท็บ "สต๊อกสินค้า": เพิ่ม/ลดตามจำนวนที่กรอก) ซึ่งหลังแก้จะรีเฟรชทั้งหน้าให้เอง
 * ตารางสมาชิกไม่แสดงรหัสผ่าน (Member ไม่เปิดให้อ่านรหัสผ่านอยู่แล้ว)
 */
final class AdminDashboardPanel extends JPanel {

    /** สินค้าที่เหลือน้อยกว่าหรือเท่ากับจำนวนนี้ถือว่า "ใกล้หมด" */
    static final int LOW_STOCK_THRESHOLD = 10;

    private static final Color DANGER = new Color(0xD64545);
    private static final Color WARN = new Color(0xE08A00);
    private static final Color OK = new Color(0x2E9D5B);

    private final DashboardService dashboard;
    private final OrderService orderService;
    private final MemberService memberService;
    private final ProductService productService;

    private JTabbedPane tabs;
    /** รหัสสินค้าที่ต้องเลือกแถวไว้ให้หลังรีเฟรชตารางสต๊อก (null = ไม่ต้องเลือก) */
    private String reselectProductId;

    AdminDashboardPanel(DashboardService dashboard, OrderService orderService, MemberService memberService,
                        ProductService productService) {
        super(new BorderLayout(0, 12));
        this.dashboard = dashboard;
        this.orderService = orderService;
        this.memberService = memberService;
        this.productService = productService;

        setBackground(Theme.PAGE_BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildTop(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
    }

    // ==================================================================
    // ส่วนบน: ชื่อหน้า + การ์ดตัวเลขสรุป
    // ==================================================================

    private JComponent buildTop() {
        JLabel title = new JLabel("แดชบอร์ดแอดมิน");
        title.setFont(Theme.font(Font.BOLD, 22f));
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        int lowStock = dashboard.getLowStockProducts(LOW_STOCK_THRESHOLD).size();

        JPanel cards = new JPanel(new GridLayout(1, 5, 12, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.add(new StatCard("ยอดขายรวม (บาท)", money(dashboard.getTotalSales()), Theme.BLUE));
        cards.add(new StatCard("จำนวนออเดอร์", String.valueOf(dashboard.getOrderCount()), Theme.BLUE));
        cards.add(new StatCard("จำนวนสมาชิก", String.valueOf(dashboard.getMemberCount()), Theme.BLUE));
        cards.add(new StatCard("หนังสือที่ขายแล้ว (เล่ม)", String.valueOf(dashboard.getTotalItemsSold()), Theme.BLUE));
        cards.add(new StatCard("สินค้าใกล้หมด (รายการ)",
                String.valueOf(lowStock), lowStock > 0 ? DANGER : OK));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.add(Box.createVerticalStrut(12));
        top.add(cards);
        return top;
    }

    // ==================================================================
    // แท็บ
    // ==================================================================

    /** สร้างหน้าใหม่ด้วยข้อมูลล่าสุด โดยคงแท็บที่เปิดอยู่ไว้ */
    private void refresh() {
        int selected = tabs == null ? 0 : tabs.getSelectedIndex();
        removeAll();
        add(buildTop(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
        tabs.setSelectedIndex(Math.max(selected, 0));
        revalidate();
        repaint();
    }

    private JComponent buildTabs() {
        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(Theme.font(Font.PLAIN, 14f));
        tabs.addTab("สินค้าขายดี", buildBestSellersTab());
        tabs.addTab("สต๊อกสินค้า", buildStockTab());
        tabs.addTab("คำสั่งซื้อ", buildOrdersTab());
        tabs.addTab("สมาชิก", buildMembersTab());
        return tabs;
    }

    private JComponent buildBestSellersTab() {
        List<ProductSales> best = dashboard.getBestSellers();
        DefaultTableModel model = readOnlyModel("อันดับ", "รหัส", "ชื่อหนังสือ", "ขายได้ (เล่ม)", "รายได้ (บาท)");

        int maxQty = 1;
        int rank = 1;
        for (ProductSales ps : best) {
            maxQty = Math.max(maxQty, ps.quantity());
            model.addRow(new Object[]{rank++, ps.productId(), ps.productName(), ps.quantity(), money(ps.revenue())});
        }

        JTable table = styledTable(model);
        table.getColumnModel().getColumn(3).setCellRenderer(new BarCell(maxQty));
        widths(table, 60, 70, 380, 180, 130);
        alignCenter(table, 0);
        alignRight(table, 4);
        return scrollOrMessage(table, "ยังไม่มียอดขาย");
    }

    private JComponent buildStockTab() {
        DefaultTableModel model = readOnlyModel("รหัส", "ชื่อหนังสือ", "ราคา (บาท)", "คงเหลือ (เล่ม)", "สถานะ");
        for (Product p : dashboard.getProductsByStock()) {
            model.addRow(new Object[]{p.getId(), p.getName(), money(p.getPrice()), p.getStock(), stockStatus(p.getStock())});
        }

        JTable table = styledTable(model);
        widths(table, 70, 380, 120, 120, 100);
        alignRight(table, 2);
        alignCenter(table, 3);
        table.getColumnModel().getColumn(3).setCellRenderer(new StockCell(true));
        table.getColumnModel().getColumn(4).setCellRenderer(new StockCell(false));
        if (table.getRowCount() == 0) {
            return messagePanel("ยังไม่มีสินค้าในระบบ");
        }

        // หลังแก้สต๊อกแล้วรีเฟรช ให้เลือกแถวเดิมต่อ (แถวอาจย้ายตำแหน่งเพราะเรียงตามสต๊อก)
        if (reselectProductId != null) {
            for (int row = 0; row < model.getRowCount(); row++) {
                if (reselectProductId.equals(model.getValueAt(row, 0))) {
                    table.setRowSelectionInterval(row, row);
                    table.scrollRectToVisible(table.getCellRect(row, 0, true));
                    break;
                }
            }
            reselectProductId = null;
        }

        JButton addButton = new JButton("+ เพิ่มสต๊อก");
        JButton reduceButton = new JButton("- ลดสต๊อก");
        for (JButton b : new JButton[]{addButton, reduceButton}) {
            b.setFont(Theme.font(Font.BOLD, 14f));
            b.setFocusPainted(false);
        }
        addButton.addActionListener(e -> adjustSelectedStock(table, model, true));
        reduceButton.addActionListener(e -> adjustSelectedStock(table, model, false));

        JLabel hint = new JLabel("เลือกสินค้าในตาราง แล้วกดปุ่มเพื่อเพิ่ม/ลดตามจำนวนที่กรอก");
        hint.setFont(Theme.font(Font.PLAIN, 13f));
        hint.setForeground(Theme.MUTED);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttons.setOpaque(false);
        buttons.add(addButton);
        buttons.add(reduceButton);
        buttons.add(hint);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(withHeader(table), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * เพิ่มหรือลดสต๊อกของสินค้าที่เลือกในตาราง ตามจำนวนที่ admin กรอกในกล่องโต้ตอบ
     * (ตัวกรอกเป็น JSpinner จึงกรอกได้เฉพาะจำนวนเต็ม >= 1 และลดได้ไม่เกินที่มีอยู่)
     * การเปลี่ยนจริงและการบันทึกไฟล์ทำใน ProductService.adjustStock
     */
    private void adjustSelectedStock(JTable table, DefaultTableModel model, boolean increase) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "กรุณาเลือกสินค้าในตารางก่อน",
                    "ยังไม่ได้เลือกสินค้า", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = (String) model.getValueAt(row, 0);
        Optional<Product> found = productService.getProductById(id);
        if (found.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ไม่พบสินค้ารหัส " + id, "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Product product = found.get();
        int current = product.getStock();
        int max = increase ? Integer.MAX_VALUE - current : current;
        if (max < 1) {
            JOptionPane.showMessageDialog(this,
                    increase ? "สต๊อกของสินค้านี้เต็มขีดจำกัดแล้ว" : "สินค้านี้หมดสต๊อกอยู่แล้ว ลดต่อไม่ได้",
                    "ปรับสต๊อกไม่ได้", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, max, 1));
        spinner.setFont(Theme.font(Font.PLAIN, 14f));
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
        form.add(new JLabel(product.getName() + " (คงเหลือ " + current + ")"));
        form.add(new JLabel(increase ? "จำนวนที่ต้องการเพิ่ม" : "จำนวนที่ต้องการลด (ลดได้สูงสุด " + current + ")"));
        form.add(spinner);

        int choice = JOptionPane.showConfirmDialog(this, form, increase ? "เพิ่มสต๊อก" : "ลดสต๊อก",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            spinner.commitEdit(); // กรณีพิมพ์ค้างไว้ในช่องแล้วกดตกลงทันที
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "กรุณากรอกจำนวนเป็นตัวเลขที่ถูกต้อง (1 ถึง " + max + ")",
                    "จำนวนไม่ถูกต้อง", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int qty = ((Number) spinner.getValue()).intValue();

        try {
            productService.adjustStock(id, increase ? qty : -qty);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "ปรับสต๊อกไม่สำเร็จ: " + e.getMessage(),
                    "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
            return;
        }
        reselectProductId = id;
        refresh();
    }

    private JComponent buildOrdersTab() {
        List<Order> newestFirst = new ArrayList<>(orderService.getAllOrders());
        Collections.reverse(newestFirst);

        DefaultTableModel model = readOnlyModel("เลขที่ออเดอร์", "สมาชิก", "วันที่", "ยอดรวม (บาท)");
        for (Order order : newestFirst) {
            model.addRow(new Object[]{order.getOrderId(), memberLabel(order.getMemberId()), order.getDate(),
                    money(order.getTotal())});
        }
        if (newestFirst.isEmpty()) {
            return messagePanel("ยังไม่มีคำสั่งซื้อ");
        }

        JTable table = styledTable(model);
        widths(table, 200, 220, 140, 140);
        alignRight(table, 3);

        JTextArea detail = new JTextArea(7, 20);
        detail.setEditable(false);
        detail.setBorder(new EmptyBorder(8, 8, 8, 8));
        detail.setFont(Theme.font(Font.PLAIN, 14f));
        detail.setText("เลือกออเดอร์ด้านบนเพื่อดูรายละเอียด");

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || table.getSelectedRow() == -1) {
                return;
            }
            String orderId = (String) model.getValueAt(table.getSelectedRow(), 0);
            Optional<Order> found = orderService.getOrderById(orderId);
            if (found.isEmpty()) {
                return;
            }
            detail.setText(orderDetail(found.get()));
            detail.setCaretPosition(0);
        });

        JScrollPane tableScroll = withHeader(table);
        JScrollPane detailScroll = new JScrollPane(detail);
        detailScroll.setBorder(BorderFactory.createTitledBorder("รายละเอียดออเดอร์"));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, detailScroll);
        split.setResizeWeight(0.6);
        split.setBorder(null);
        return split;
    }

    private JComponent buildMembersTab() {
        DefaultTableModel model = readOnlyModel("รหัส", "ชื่อผู้ใช้", "ที่อยู่", "เบอร์โทร", "ออเดอร์", "ยอดซื้อรวม (บาท)");
        for (MemberSales ms : dashboard.getMemberSales()) {
            Member m = ms.member();
            model.addRow(new Object[]{m.getId(), m.getUsername(), m.getAddress(), m.getPhone(),
                    ms.orderCount(), money(ms.totalSpent())});
        }

        JTable table = styledTable(model);
        widths(table, 70, 160, 220, 130, 80, 150);
        alignCenter(table, 4);
        alignRight(table, 5);
        return scrollOrMessage(table, "ยังไม่มีสมาชิก");
    }

    // ==================================================================
    // ตัวช่วยจัดรูปแบบข้อความ
    // ==================================================================

    private static String money(double value) {
        return String.format(Locale.US, "%,.2f", value);
    }

    private static String stockStatus(int stock) {
        if (stock <= 0) {
            return "หมด";
        }
        return stock <= LOW_STOCK_THRESHOLD ? "ใกล้หมด" : "ปกติ";
    }

    /** ชื่อผู้ใช้ของสมาชิก ถ้าหาไม่เจอ (เช่น ออเดอร์เก่าของสมาชิกที่ถูกลบ) ใช้รหัสสมาชิกแทน */
    private String memberLabel(String memberId) {
        Optional<Member> member = memberService.getMemberById(memberId);
        return member.map(m -> m.getUsername() + " (" + m.getId() + ")").orElse(String.valueOf(memberId));
    }

    private String orderDetail(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("เลขที่ออเดอร์: ").append(order.getOrderId()).append("\n");
        sb.append("สมาชิก: ").append(memberLabel(order.getMemberId())).append("\n");
        sb.append("วันที่: ").append(order.getDate()).append("\n\n");
        for (OrderItem item : order.getItems()) {
            sb.append(String.format(Locale.US, "- %s x%d = %,.2f บาท%n",
                    item.getProduct().getName(), item.getQuantity(), item.getSubtotal()));
        }
        sb.append(String.format(Locale.US, "%nยอดรวมทั้งหมด: %,.2f บาท", order.getTotal()));
        return sb.toString();
    }

    // ==================================================================
    // ตัวช่วยสร้างตาราง
    // ==================================================================

    private static DefaultTableModel readOnlyModel(Object... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(Theme.font(Font.PLAIN, 14f));
        table.getTableHeader().setFont(Theme.font(Font.BOLD, 14f));
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(0xE6E6E6));
        table.setSelectionBackground(new Color(0xD8EEFC));
        table.setSelectionForeground(Theme.TEXT);
        table.setFillsViewportHeight(true);
        return table;
    }

    private static void widths(JTable table, int... widths) {
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private static void alignRight(JTable table, int column) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(column).setCellRenderer(r);
    }

    private static void alignCenter(JTable table, int column) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(column).setCellRenderer(r);
    }

    /** ใส่ตารางในกรอบเลื่อน พร้อมหัวตาราง (ตั้งเองให้ชัดเจน ไม่รอให้ Swing ตั้งให้ตอนแสดงผล) */
    private static JScrollPane withHeader(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setColumnHeaderView(table.getTableHeader());
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private static JComponent scrollOrMessage(JTable table, String emptyMessage) {
        if (table.getRowCount() == 0) {
            return messagePanel(emptyMessage);
        }
        return withHeader(table);
    }

    private static JComponent messagePanel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(Theme.font(Font.PLAIN, 16f));
        label.setForeground(Theme.MUTED);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    // ==================================================================
    // คอมโพเนนต์ย่อย
    // ==================================================================

    /** การ์ดตัวเลขสรุป: แถบสีด้านซ้าย + ชื่อหัวข้อ + ตัวเลขใหญ่ */
    private static final class StatCard extends JPanel {
        private final String title;
        private final String value;
        private final Color accent;

        StatCard(String title, String value, Color accent) {
            this.title = title;
            this.value = value;
            this.accent = accent;
            setOpaque(false);
            setPreferredSize(new Dimension(180, 88));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.antialias(g2);
            int w = getWidth() - 1;
            int h = getHeight() - 1;
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w, h, 16, 16);
            g2.setColor(Theme.LINE);
            g2.drawRoundRect(0, 0, w, h, 16, 16);
            g2.setColor(accent);
            g2.fillRoundRect(0, 14, 5, h - 28, 5, 5);

            g2.setFont(Theme.font(Font.PLAIN, 13f));
            g2.setColor(Theme.MUTED);
            g2.drawString(title, 18, 30);

            g2.setFont(Theme.font(Font.BOLD, 24f));
            g2.setColor(Theme.TEXT);
            g2.drawString(value, 18, 66);
            g2.dispose();
        }
    }

    /** เซลล์ตารางที่วาดแท่งสัดส่วนจำนวนเล่มที่ขายได้ เทียบกับเล่มที่ขายได้มากสุด */
    private static final class BarCell extends JComponent implements TableCellRenderer {
        private final int max;
        private int quantity;
        private boolean selected;

        BarCell(int max) {
            this.max = Math.max(1, max);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            this.quantity = value instanceof Number ? ((Number) value).intValue() : 0;
            this.selected = isSelected;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.antialias(g2);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(selected ? new Color(0xD8EEFC) : Color.WHITE);
            g2.fillRect(0, 0, w, h);

            int barW = (int) Math.round((w - 16) * (quantity / (double) max));
            g2.setColor(new Color(0xBFE3F9));
            g2.fillRoundRect(8, 6, Math.max(barW, 0), h - 12, 8, 8);

            g2.setFont(Theme.font(Font.BOLD, 14f));
            g2.setColor(Theme.TEXT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(String.valueOf(quantity), 14, (h + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** เซลล์สต๊อก/สถานะ: หมด = แดงหนา, ใกล้หมด = ส้ม, ปกติ = เขียว (ใช้สีเดียวกับสถานะของแถว) */
    private static final class StockCell extends DefaultTableCellRenderer {
        private final boolean numberColumn;

        StockCell(boolean numberColumn) {
            this.numberColumn = numberColumn;
            setHorizontalAlignment(numberColumn ? SwingConstants.CENTER : SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int stock = ((Number) table.getModel().getValueAt(row, 3)).intValue();
            Color color = stock <= 0 ? DANGER : stock <= LOW_STOCK_THRESHOLD ? WARN : OK;
            setForeground(color);
            setFont(Theme.font(stock <= LOW_STOCK_THRESHOLD ? Font.BOLD : Font.PLAIN, 14f));
            return this;
        }
    }
}