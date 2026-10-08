package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import model.Member;
import model.Order;
import model.OrderItem;
import model.Product;
import repository.MemberRepository;
import repository.OrderRepository;
import repository.ProductRepository;
import service.MemberService;
import service.OrderService;
import service.ProductService;

/**
 * หน้าต่าง GUI หลักของ BookShop "Online Book Ordering System" (Swing)
 *
 * หน้าตาตามแบบ: หัวเรื่องกลาง + ปุ่มเข้าสู่ระบบ / แท็บหมวด / ช่องค้นหา / แถวหนังสือแนะนำ
 * ข้อมูลและ logic ธุรกิจทั้งหมดยังเรียกผ่าน ProductService / OrderService เดิม (ไม่แก้ชั้น model/service)
 *
 * รันจาก root ของโปรเจกต์ (โฟลเดอร์ที่มี data/ อยู่) ไม่งั้นจะหาไฟล์ CSV ไม่เจอ
 */
public class BookShopApp extends JFrame {

    private static final String PRODUCT_FILE = "data/Products.csv";
    private static final String ORDER_FILE = "data/Orders.csv";
    private static final String MEMBER_FILE = "data/member.csv";
    private static final int ROW_SIZE = 6;
    // ระบบสมาชิก / ชำระเงิน ยังทำไม่เสร็จ -> ปิดไว้ก่อน (เปลี่ยนเป็น true เมื่อพร้อมใช้งาน)
    private static final boolean LOGIN_ENABLED = true;
    private static final boolean CHECKOUT_ENABLED = true;
    private static final String[] TABS = {"หนังสือ", "อีบุ๊ก", "นิยายสาร", "อีแมกกาซีน"};

    private final ProductService productService;
    private final OrderService orderService;
    private final MemberService memberService;

    // ตะกร้าเก็บเป็น OrderItem ตัวเดียวกับที่ใช้สร้าง Order จริง ยังไม่กระทบสต๊อกจนกว่าจะชำระเงิน
    private final List<OrderItem> cart = new ArrayList<>();
    private Member loggedInMember = null;

    private PillButton loginButton;
    private LinkButton cartNavButton;
    private JTextField searchField;
    private JPanel sectionsPanel;
    private JPanel centerCards;
    private CardLayout centerLayout;
    private int selectedTab = 0;
    private TabBar tabBar;
    private JPanel detailHolder;

    public BookShopApp() {
        super("BookShop - Online Book Ordering System");

        ProductRepository productRepository = new ProductRepository(PRODUCT_FILE);
        this.productService = new ProductService(productRepository);
        OrderRepository orderRepository = new OrderRepository(ORDER_FILE, productRepository);
        this.orderService = new OrderService(productService, orderRepository);
        this.memberService = loadMemberService();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.WHITE);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);
        setContentPane(root);

        setSize(1000, 720);
        setMinimumSize(new Dimension(840, 520));
        setLocationRelativeTo(null);

        rebuildSections();
        updateCartButton();
    }

    // ==================================================================
    // ส่วนหัว + แท็บ + ค้นหา
    // ==================================================================

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.WHITE);
        header.setBorder(new EmptyBorder(10, 20, 8, 20));

        JLabel title = new JLabel("Online Book Ordering System", SwingConstants.CENTER);
        title.setFont(Theme.font(Font.BOLD, 20f));
        title.setForeground(Theme.TEXT);

        // ขวา: ตะกร้า / ประวัติสั่งซื้อ / เข้าสู่ระบบ อยู่ติดกัน
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);
        right.setAlignmentY(Component.CENTER_ALIGNMENT);
        cartNavButton = new LinkButton("ตะกร้า (0)");
        cartNavButton.addActionListener(e -> showCartDialog());
        LinkButton historyButton = new LinkButton("ประวัติสั่งซื้อ");
        historyButton.addActionListener(e -> showHistoryDialog());
        loginButton = new PillButton("เข้าสู่ระบบ / สมัครสมาชิก", Theme.LOGIN_BG, Theme.LOGIN_BG_HOVER, Theme.TEXT);
        loginButton.setPreferredSize(new Dimension(230, 38));
        loginButton.addActionListener(e -> onLoginClicked());
        right.add(cartNavButton);
        right.add(historyButton);
        right.add(loginButton);

        // แถวบน: เมนูชิดขวา / แถวล่าง: ชื่อระบบอยู่กึ่งกลางหน้าต่างพอดี (ไม่ถูกเมนูเบียด)
        title.setBorder(new EmptyBorder(4, 0, 6, 0));
        header.add(right, BorderLayout.NORTH);
        header.add(title, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(header, BorderLayout.CENTER);
        tabBar = new TabBar();
        wrapper.add(tabBar, BorderLayout.SOUTH);
        return wrapper;
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Theme.PAGE_BG);

        // ช่องค้นหา
        searchField = new HintTextField("ค้นหาจากชื่อหนังสือ หรือรหัสสินค้า");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                showShopList();
                rebuildSections();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                showShopList();
                rebuildSections();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                showShopList();
                rebuildSections();
            }
        });
        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setBackground(Theme.PAGE_BG);
        searchWrap.setBorder(new EmptyBorder(14, 24, 6, 24));
        searchWrap.add(new SearchBox(searchField), BorderLayout.CENTER);
        body.add(searchWrap, BorderLayout.NORTH);

        // รายการหนังสือ (เลื่อนขึ้นลงได้)
        sectionsPanel = new ScrollablePanel();
        sectionsPanel.setLayout(new BoxLayout(sectionsPanel, BoxLayout.Y_AXIS));
        sectionsPanel.setBackground(Theme.PAGE_BG);
        sectionsPanel.setBorder(new EmptyBorder(8, 24, 24, 24));
        sectionsPanel.addComponentListener(new ComponentAdapter() {
            private int lastWidth = -1;

            @Override
            public void componentResized(ComponentEvent e) {
                if (sectionsPanel.getWidth() != lastWidth) {
                    lastWidth = sectionsPanel.getWidth();
                    sectionsPanel.revalidate(); // ให้แถวการ์ดคำนวณการตัดบรรทัดใหม่ตามความกว้างจริง
                }
            }
        });

        JScrollPane scroll = new JScrollPane(sectionsPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.PAGE_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        // แท็บอื่นที่ยังไม่เปิดให้บริการ
        JLabel soon = new JLabel("หมวดนี้ยังไม่เปิดให้บริการ", SwingConstants.CENTER);
        soon.setFont(Theme.font(Font.PLAIN, 16f));
        soon.setForeground(Theme.MUTED);

        centerLayout = new CardLayout();
        centerCards = new JPanel(centerLayout);
        centerCards.setOpaque(false);
        centerCards.add(scroll, "shop");
        centerCards.add(soon, "soon");
        detailHolder = new JPanel(new BorderLayout());
        detailHolder.setBackground(Theme.PAGE_BG);
        centerCards.add(detailHolder, "detail");
        body.add(centerCards, BorderLayout.CENTER);
        return body;
    }

    // ==================================================================
    // สร้างรายการหนังสือ
    // ==================================================================

    private void rebuildSections() {
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        List<Product> matches = new ArrayList<>();
        for (Product p : productService.getAllProducts()) {
            if (query.isEmpty()
                    || p.getName().toLowerCase(Locale.ROOT).contains(query)
                    || p.getId().toLowerCase(Locale.ROOT).contains(query)) {
                matches.add(p);
            }
        }

        sectionsPanel.removeAll();
        if (matches.isEmpty()) {
            JLabel empty = new JLabel(query.isEmpty() ? "ยังไม่มีสินค้าในระบบ" : "ไม่พบหนังสือที่ค้นหา");
            empty.setFont(Theme.font(Font.PLAIN, 15f));
            empty.setForeground(Theme.MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            empty.setBorder(new EmptyBorder(20, 0, 0, 0));
            sectionsPanel.add(empty);
        } else {
            int split = Math.min(ROW_SIZE, matches.size());
            addSection("หนังสือแนะนำสำหรับคุณ", matches.subList(0, split));
            if (split < matches.size()) {
                addSection("หนังสือใหม่ติดเทรน", matches.subList(split, matches.size()));
            }
        }
        sectionsPanel.add(Box.createVerticalGlue());
        sectionsPanel.revalidate();
        sectionsPanel.repaint();
        SwingUtilities.invokeLater(sectionsPanel::revalidate);
    }

    private void addSection(String title, List<Product> products) {
        JLabel heading = new JLabel(title);
        heading.setFont(Theme.font(Font.BOLD, 19f));
        heading.setForeground(Theme.TEXT);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(14, 0, 12, 0));
        sectionsPanel.add(heading);

        JPanel row = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 18));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (Product p : products) {
            row.add(new BookCard(p, this::addToCart, this::showDetail));
        }
        sectionsPanel.add(row);
    }

    // ==================================================================
    // ตะกร้า / สั่งซื้อ
    // ==================================================================

    private void addToCart(Product product) {
        int alreadyInCart = 0;
        int existingIndex = -1;
        for (int i = 0; i < cart.size(); i++) {
            if (cart.get(i).getProduct().getId().equals(product.getId())) {
                alreadyInCart = cart.get(i).getQuantity();
                existingIndex = i;
                break;
            }
        }

        if (alreadyInCart + 1 > product.getStock()) {
            JOptionPane.showMessageDialog(this,
                    "สินค้าไม่พอ: มีอยู่ " + product.getStock() + " เล่ม (อยู่ในตะกร้าแล้ว " + alreadyInCart + ")",
                    "สินค้าไม่พอ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (existingIndex >= 0) {
            cart.set(existingIndex, new OrderItem(product, alreadyInCart + 1));
        } else {
            cart.add(new OrderItem(product, 1));
        }
        updateCartButton();
    }

    private void updateCartButton() {
        int count = 0;
        for (OrderItem item : cart) {
            count += item.getQuantity();
        }
        cartNavButton.setText("ตะกร้า (" + count + ")");
    }

    private void showCartDialog() {
        JDialog dialog = new JDialog(this, "ตะกร้าสินค้า", true);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"ชื่อหนังสือ", "จำนวน", "ยอดรวม (บาท)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = styledTable(model);
        JLabel totalLabel = new JLabel();
        totalLabel.setFont(Theme.font(Font.BOLD, 16f));

        Runnable refresh = () -> {
            model.setRowCount(0);
            double total = 0;
            for (OrderItem item : cart) {
                model.addRow(new Object[]{item.getProduct().getName(), item.getQuantity(),
                        String.format(Locale.US, "%,.2f", item.getSubtotal())});
                total += item.getSubtotal();
            }
            totalLabel.setText(String.format(Locale.US, "ยอดรวม: %,.2f บาท", total));
            updateCartButton();
        };
        refresh.run();

        PillButton removeButton = new PillButton("ลบรายการที่เลือก", new Color(0xD9D9D9), new Color(0xC8C8C8), Theme.TEXT);
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

        PillButton checkoutButton = new PillButton("ชำระเงิน", Theme.BLUE, Theme.BLUE_DARK, Color.WHITE);
        checkoutButton.setPreferredSize(new Dimension(150, 40));
        checkoutButton.addActionListener(e -> {
            // ยังไม่เสร็จ: ถ้า CHECKOUT_ENABLED = false กดแล้วไม่ต้องทำอะไร
            if (CHECKOUT_ENABLED && checkout(dialog)) {
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
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /** @return true ถ้าสั่งซื้อสำเร็จ */
    private boolean checkout(Component parent) {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "ตะกร้าว่าง กรุณาเพิ่มสินค้าก่อน",
                    "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (loggedInMember == null && !promptLogin()) {
            return false;
        }

        Map<String, Integer> productQuantities = new LinkedHashMap<>();
        for (OrderItem item : cart) {
            productQuantities.put(item.getProduct().getId(), item.getQuantity());
        }
        String orderId = "O" + System.currentTimeMillis();
        String date = LocalDate.now().toString();

        try {
            Order order = orderService.createOrder(orderId, loggedInMember.getId(), date, productQuantities);
            productService.save();
            orderService.save();

            cart.clear();
            updateCartButton();
            rebuildSections();

            JOptionPane.showMessageDialog(parent,
                    "สั่งซื้อสำเร็จ!\nเลขที่ออเดอร์: " + order.getOrderId()
                            + String.format(Locale.US, "%nยอดรวม: %,.2f บาท", order.getTotal()),
                    "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IllegalArgumentException | IllegalStateException e) {
            JOptionPane.showMessageDialog(parent, "สั่งซื้อไม่สำเร็จ: " + e.getMessage(),
                    "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void showHistoryDialog() {
        // ดูได้เฉพาะประวัติของสมาชิกที่เข้าสู่ระบบ ถ้ายังไม่ได้เข้าสู่ระบบให้ล็อกอินก่อน
        if (loggedInMember == null && !promptLogin()) {
            return;
        }
        String memberId = loggedInMember.getId();
        JDialog dialog = new JDialog(this, "ประวัติคำสั่งซื้อของ " + loggedInMember.getUsername(), true);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"เลขที่ออเดอร์", "รหัสสมาชิก", "วันที่", "ยอดรวม (บาท)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Order order : orderService.getOrdersByMemberId(memberId)) {
            model.addRow(new Object[]{order.getOrderId(), order.getMemberId(), order.getDate(),
                    String.format(Locale.US, "%,.2f", order.getTotal())});
        }
        JTable table = styledTable(model);

        JTextArea detail = new JTextArea(8, 20);
        detail.setEditable(false);
        detail.setBorder(new EmptyBorder(8, 8, 8, 8));
        detail.setFont(Theme.font(Font.PLAIN, 14f));

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || table.getSelectedRow() == -1) {
                return;
            }
            String orderId = (String) model.getValueAt(table.getSelectedRow(), 0);
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isEmpty()) {
                return;
            }
            Order order = orderOpt.get();
            StringBuilder sb = new StringBuilder();
            sb.append("เลขที่ออเดอร์: ").append(order.getOrderId()).append("\n");
            sb.append("รหัสสมาชิก: ").append(order.getMemberId()).append("\n");
            sb.append("วันที่: ").append(order.getDate()).append("\n\n");
            for (OrderItem item : order.getItems()) {
                sb.append(String.format(Locale.US, "- %s x%d = %,.2f บาท%n",
                        item.getProduct().getName(), item.getQuantity(), item.getSubtotal()));
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
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(Theme.font(Font.PLAIN, 14f));
        table.getTableHeader().setFont(Theme.font(Font.BOLD, 14f));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(0xE6E6E6));
        table.setSelectionBackground(new Color(0xD8EEFC));
        table.setSelectionForeground(Theme.TEXT);
        table.setFillsViewportHeight(true);
        return table;
    }

    // ==================================================================
    // เข้าสู่ระบบ
    // ==================================================================

    private void onLoginClicked() {
        if (LOGIN_ENABLED) {
            doLogin();
        }
        // ยังไม่เสร็จ: กดแล้วไม่ต้องทำอะไร
    }

    private void doLogin() {
        if (loggedInMember == null) {
            promptLogin();
            return;
        }
        Object[] options = {"ออกจากระบบ", "ยกเลิก"};
        int choice = JOptionPane.showOptionDialog(this, "ต้องการออกจากระบบสมาชิก " + loggedInMember.getUsername() + " ใช่หรือไม่",
                "ออกจากระบบ", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        if (choice == 0) {
            loggedInMember = null;
            updateLoginButton();
        }
    }

    /** โหลดสมาชิกจาก member.csv ถ้าไฟล์ผิดรูปแบบจะแจ้งเตือนแล้วปิดโปรแกรม (ไม่เขียนทับไฟล์เดิม) */
    private static MemberService loadMemberService() {
        try {
            return new MemberService(new MemberRepository(MEMBER_FILE));
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(null,
                    "อ่านไฟล์สมาชิก " + MEMBER_FILE + " ไม่สำเร็จ\n" + e.getMessage(),
                    "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return null;
        }
    }

    /** @return true ถ้าเข้าสู่ระบบ (หรือสมัครแล้วเข้าสู่ระบบ) สำเร็จ */
    private boolean promptLogin() {
        JTextField usernameField = new JTextField(18);
        JPasswordField passwordField = new JPasswordField(18);
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
        form.add(new JLabel("ชื่อผู้ใช้ (username)"));
        form.add(usernameField);
        form.add(new JLabel("รหัสผ่าน"));
        form.add(passwordField);

        Object[] options = {"เข้าสู่ระบบ", "สมัครสมาชิก", "ยกเลิก"};
        while (true) {
            int choice = JOptionPane.showOptionDialog(this, form, "เข้าสู่ระบบ / สมัครสมาชิก",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (choice == 1) {
                if (promptRegister()) {
                    return true;
                }
                continue;
            }
            if (choice != 0) {
                return false; // ยกเลิก หรือปิดหน้าต่าง
            }
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            Optional<Member> member = memberService.login(username, password);
            if (member.isPresent()) {
                loggedInMember = member.get();
                updateLoginButton();
                return true;
            }
            JOptionPane.showMessageDialog(this, "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง",
                    "เข้าสู่ระบบไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }

    /** แสดงฟอร์มสมัครสมาชิก @return true ถ้าสมัครสำเร็จ (และเข้าสู่ระบบให้อัตโนมัติ) */
    private boolean promptRegister() {
        JTextField usernameField = new JTextField(18);
        JPasswordField passwordField = new JPasswordField(18);
        JPasswordField confirmField = new JPasswordField(18);
        JTextField addressField = new JTextField(18);
        JTextField phoneField = new JTextField(18);

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
        form.add(new JLabel("ชื่อผู้ใช้ (6-32 ตัวอักษร)"));
        form.add(usernameField);
        form.add(new JLabel("รหัสผ่าน"));
        form.add(passwordField);
        form.add(new JLabel("ยืนยันรหัสผ่าน"));
        form.add(confirmField);
        form.add(new JLabel("ที่อยู่ (ห้ามมีเครื่องหมาย ,)"));
        form.add(addressField);
        form.add(new JLabel("เบอร์โทร (ตัวเลข 10 หลัก)"));
        form.add(phoneField);

        while (true) {
            int choice = JOptionPane.showConfirmDialog(this, form, "สมัครสมาชิก",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) {
                return false;
            }
            String newId = memberService.nextMemberId();
            Member member;
            try {
                member = memberService.register(
                        newId,
                        usernameField.getText().trim(),
                        new String(passwordField.getPassword()),
                        new String(confirmField.getPassword()),
                        addressField.getText().trim(),
                        phoneField.getText().trim());
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, "สมัครสมาชิกไม่สำเร็จ: " + e.getMessage(),
                        "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
                continue; // ให้แก้ข้อมูลในฟอร์มเดิมแล้วลองใหม่
            }
            try {
                memberService.save();
            } catch (RuntimeException e) {
                // เขียนไฟล์ไม่สำเร็จ: เอาสมาชิกที่เพิ่งเพิ่มออกจากหน่วยความจำ จะได้ไม่ค้างโดยไม่ลงไฟล์
                memberService.removeMember(newId);
                JOptionPane.showMessageDialog(this, "บันทึกสมาชิกไม่สำเร็จ: " + e.getMessage(),
                        "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            loggedInMember = member;
            updateLoginButton();
            JOptionPane.showMessageDialog(this,
                    "สมัครสมาชิกสำเร็จ รหัสสมาชิกของคุณคือ " + member.getId(),
                    "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
    }

    private void updateLoginButton() {
        loginButton.setText(loggedInMember == null ? "เข้าสู่ระบบ / สมัครสมาชิก" : "สมาชิก: " + loggedInMember.getUsername());
    }

    private void selectTab(int index) {
        selectedTab = index;
        tabBar.repaint();
        centerLayout.show(centerCards, index == 0 ? "shop" : "soon");
    }

    private void showShopList() {
        if (centerLayout != null && selectedTab == 0) {
            centerLayout.show(centerCards, "shop");
        }
    }

    // ==================================================================
    // หน้ารายละเอียดหนังสือ
    // ==================================================================

    private void showDetail(Product product) {
        // ใช้ข้อมูลล่าสุด (สต๊อกอาจเปลี่ยนหลังชำระเงิน)
        Product p = productService.getAllProducts().stream()
                .filter(x -> x.getId().equals(product.getId())).findFirst().orElse(product);

        detailHolder.removeAll();

        LinkButton back = new LinkButton("< ย้อนกลับ");
        back.setForeground(Theme.MUTED);
        back.addActionListener(e -> showShopList());
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 8));
        backRow.setOpaque(false);
        backRow.add(back);

        final int infoW = 460;
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(new TextBlock(p.getName(), Theme.font(Font.BOLD, 16f), Theme.TEXT, infoW, 2));
        info.add(new TextBlock("รหัสสินค้า : " + p.getId(), Theme.font(Font.PLAIN, 12f), Theme.MUTED, infoW, 1));
        info.add(new TextBlock("ประเภท : Books", Theme.font(Font.PLAIN, 12f), Theme.MUTED, infoW, 1));
        info.add(Box.createVerticalStrut(8));

        JLabel price = new JLabel(String.format(Locale.US, "%,.2f บาท", p.getPrice()));
        price.setFont(Theme.font(Font.BOLD, 30f));
        price.setForeground(Theme.TEXT);
        price.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(price);
        info.add(Box.createVerticalStrut(10));

        boolean available = p.getStock() > 0;
        PillButton pay = new PillButton(available ? "ชำระเงิน" : "สินค้าหมด",
                available ? Color.BLACK : new Color(0xC9C9C9), new Color(0x333333), Color.WHITE);
        pay.setFont(Theme.font(Font.BOLD, 20f));
        pay.setPreferredSize(new Dimension(infoW - 160, 56));
        pay.setMaximumSize(new Dimension(infoW - 160, 56));
        pay.setAlignmentX(Component.LEFT_ALIGNMENT);
        pay.setEnabled(available);
        pay.addActionListener(e -> {
            if (CHECKOUT_ENABLED) {
                addToCart(p);
                showCartDialog(); // ไปหน้าตะกร้าเพื่อชำระเงินต่อ
            }
            // ยังไม่เสร็จ: กดแล้วไม่ต้องทำอะไร
        });
        info.add(pay);
        info.add(Box.createVerticalStrut(14));

        info.add(new TextBlock("รายละเอียด : " + p.getName(), Theme.font(Font.BOLD, 12f), Theme.TEXT, infoW, 2));
        info.add(Box.createVerticalStrut(4));
        info.add(new TextBlock(available ? "คงเหลือ " + p.getStock() + " เล่ม" : "สินค้าหมด",
                Theme.font(Font.PLAIN, 12f), available ? Theme.TEXT : new Color(0xD64545), infoW, 1));

        BookCard.Cover cover = new BookCard.Cover(p, 260, 370);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.insets = new Insets(0, 0, 0, 40);
        content.add(cover, c);
        c.gridx = 1;
        c.insets = new Insets(0, 0, 0, 0);
        content.add(info, c);

        JPanel centerWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        centerWrap.setOpaque(false);
        centerWrap.add(content);

        JScrollPane scroll = new JScrollPane(centerWrap,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        detailHolder.add(backRow, BorderLayout.NORTH);
        detailHolder.add(scroll, BorderLayout.CENTER);
        detailHolder.revalidate();
        detailHolder.repaint();
        centerLayout.show(centerCards, "detail");
    }

    // ==================================================================
    // Component ตกแต่งเอง
    // ==================================================================

    /** แถบแท็บ: หนังสือ • อีบุ๊ก • นิยายสาร • อีแมกกาซีน (แท็บที่เลือกมีเส้นใต้หนา) */
    private final class TabBar extends JComponent {
        TabBar() {
            setPreferredSize(new Dimension(100, 46));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && contains(e.getPoint())) {
                        int index = Math.min(TABS.length - 1, e.getX() * TABS.length / Math.max(1, getWidth()));
                        selectTab(index);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.antialias(g2);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(Theme.WHITE);
            g2.fillRect(0, 0, w, h);

            g2.setColor(Theme.LINE);
            g2.draw(new Line2D.Float(0, 0.5f, w, 0.5f));
            g2.draw(new Line2D.Float(0, h - 0.5f, w, h - 0.5f));

            int tabW = w / TABS.length;
            for (int i = 0; i < TABS.length; i++) {
                int x = i * tabW;
                int width = (i == TABS.length - 1) ? w - x : tabW;
                boolean selected = i == selectedTab;

                if (i > 0) {
                    g2.setColor(Theme.LINE);
                    g2.draw(new Line2D.Float(x + 0.5f, 0, x + 0.5f, h));
                }
                Font font = Theme.font(selected ? Font.BOLD : Font.PLAIN, 15f);
                int lineH = TextBlock.lineHeight(g2.getFontMetrics(font));
                g2.setColor(Theme.TEXT);
                TextBlock.drawLines(g2, TABS[i] + "    •", font, x, (h - lineH) / 2, width, 1, true);

                if (selected) {
                    g2.setColor(new Color(0x222222));
                    g2.fillRect(x, h - 4, width, 4);
                }
            }
            g2.dispose();
        }
    }

    /** กล่องค้นหาขอบมน มีไอคอนแว่นขยาย */
    private static final class SearchBox extends JPanel {
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
                    Theme.antialias(g2);
                    g2.setColor(Theme.TEXT);
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
            Theme.antialias(g2);
            RoundRectangle2D shape = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.setColor(Theme.WHITE);
            g2.fill(shape);
            g2.setColor(field.hasFocus() ? Theme.BLUE : Theme.LINE);
            g2.setStroke(new BasicStroke(field.hasFocus() ? 1.8f : 1f));
            g2.draw(shape);
            g2.dispose();
        }
    }

    /** ช่องพิมพ์ไร้ขอบ พร้อมข้อความจางเมื่อยังว่าง */
    private static final class HintTextField extends JTextField {
        private final String hint;

        HintTextField(String hint) {
            this.hint = hint;
            setBorder(new EmptyBorder(6, 4, 6, 4));
            setOpaque(false);
            setFont(Theme.font(Font.PLAIN, 14f));
            setForeground(Theme.TEXT);
            setCaretColor(Theme.TEXT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.antialias(g2);
                g2.setColor(Theme.MUTED);
                Insets in = getInsets();
                Font font = getFont();
                int lineH = TextBlock.lineHeight(g2.getFontMetrics(font));
                TextBlock.drawLines(g2, hint, font, in.left, (getHeight() - lineH) / 2,
                        getWidth() - in.left - in.right, 1, false);
                g2.dispose();
            }
        }
    }

    /** ปุ่มทรงแคปซูล (วาดข้อความด้วย TextLayout เพื่อให้ภาษาไทยไม่เพี้ยน) */
    private static final class PillButton extends JButton {
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
            setFont(Theme.font(Font.BOLD, 14f));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.antialias(g2);
            g2.setColor(getModel().isRollover() || getModel().isPressed() ? hover : base);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
            g2.setColor(textColor);
            Font font = getFont();
            int lineH = TextBlock.lineHeight(g2.getFontMetrics(font));
            TextBlock.drawLines(g2, getText(), font, 8, (getHeight() - lineH) / 2, getWidth() - 16, 1, true);
            g2.dispose();
        }
    }

    /** ปุ่มข้อความล้วน (ตะกร้า / ประวัติสั่งซื้อ) */
    private static final class LinkButton extends JButton {
        LinkButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setMargin(new Insets(2, 2, 2, 2));
            setRolloverEnabled(true);
            setFont(Theme.font(Font.PLAIN, 14f));
            setForeground(Theme.TEXT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.antialias(g2);
            g2.setColor(getModel().isRollover() ? Theme.BLUE_DARK : Theme.TEXT);
            Font font = getFont();
            int lineH = TextBlock.lineHeight(g2.getFontMetrics(font));
            TextBlock.drawLines(g2, getText(), font, 0, (getHeight() - lineH) / 2, getWidth(), 1, true);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 20, 36);
        }
    }

    /** panel ที่ยืดตามความกว้างของ viewport เพื่อให้การ์ดขึ้นบรรทัดใหม่ตามขนาดหน้าต่าง */
    private static final class ScrollablePanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(40, (int) (visibleRect.height * 0.9));
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    // ==================================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install(); // ต้องเรียกก่อนสร้างหน้าต่าง: ตั้งฟอนต์ไทย + anti-alias
            new BookShopApp().setVisible(true);
        });
    }
}