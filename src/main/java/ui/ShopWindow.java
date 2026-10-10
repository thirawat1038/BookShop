package ui;

// หน้าต่างร้านหนังสือสำหรับลูกค้า

import app.DataFiles;
import app.ShopSystem;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import model.Book;
import model.Member;
import model.Order;
import model.OrderLine;
import service.BookManager;
import service.LoginManager;
import service.OrderManager;
import ui.ScreenControls.HintTextField;
import ui.ScreenControls.LinkButton;
import ui.ScreenControls.PillButton;
import ui.ScreenControls.ScrollablePanel;
import ui.ScreenControls.SearchBox;
import ui.ScreenControls.ShopTabBar;

public class ShopWindow extends JFrame {
    private static final int ROW_SIZE = 6;
    private static final String[] TABS = Book.CATEGORIES.toArray(String[]::new);

    private final BookManager productService;
    private final OrderManager orderService;
    private final ShopSystem application;

    // ตะกร้ายังไม่หักสต๊อกจนกว่าจะยืนยันคำสั่งซื้อ
    private final List<OrderLine> cart = new ArrayList<>();
    private Member loggedInMember = null;

    private PillButton loginButton;
    private LinkButton cartNavButton;
    private JTextField searchField;
    private JPanel sectionsPanel;
    private JPanel centerCards;
    private CardLayout centerLayout;
    private int selectedTab = 0;
    private ShopTabBar tabBar;
    private JPanel detailHolder;

    public ShopWindow() {
        this(new ShopSystem(new DataFiles()));
    }

    public ShopWindow(ShopSystem application) {
        super("BookShop - Online Book Ordering System");
        this.application = application;
        this.productService = application.products;
        this.orderService = application.orders;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ScreenStyle.WHITE);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);
        setContentPane(root);

        setSize(1000, 720);
        setMinimumSize(new Dimension(840, 520));
        setLocationRelativeTo(null);

        rebuildSections();
        updateCartButton();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ScreenStyle.WHITE);
        header.setBorder(new EmptyBorder(10, 20, 8, 20));

        JLabel title = new JLabel("Online Book Ordering System", SwingConstants.CENTER);
        title.setFont(ScreenStyle.font(Font.BOLD, 20f));
        title.setForeground(ScreenStyle.TEXT);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);
        right.setAlignmentY(Component.CENTER_ALIGNMENT);
        cartNavButton = new LinkButton("ตะกร้า (0)");
        cartNavButton.addActionListener(e -> showCartDialog());
        LinkButton historyButton = new LinkButton("ประวัติสั่งซื้อ");
        historyButton.addActionListener(e -> showHistoryDialog());
        loginButton = new PillButton("เข้าสู่ระบบ / สมัครสมาชิก", ScreenStyle.LOGIN_BG, ScreenStyle.LOGIN_BG_HOVER, ScreenStyle.WHITE);
        loginButton.setPreferredSize(new Dimension(230, 38));
        loginButton.addActionListener(e -> doLogin());
        right.add(cartNavButton);
        right.add(historyButton);
        right.add(loginButton);

        title.setBorder(new EmptyBorder(4, 0, 6, 0));
        header.add(right, BorderLayout.NORTH);
        header.add(title, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(header, BorderLayout.CENTER);
        tabBar = new ShopTabBar(TABS, this::selectTab);
        wrapper.add(tabBar, BorderLayout.SOUTH);
        return wrapper;
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(ScreenStyle.PAGE_BG);

        searchField = new HintTextField("ค้นหาจากชื่อหนังสือ หรือรหัสสินค้า");
        ScreenParts.onChange(searchField, () -> {
            showShopList();
            rebuildSections();
        });
        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setBackground(ScreenStyle.PAGE_BG);
        searchWrap.setBorder(new EmptyBorder(14, 24, 6, 24));
        searchWrap.add(new SearchBox(searchField), BorderLayout.CENTER);
        body.add(searchWrap, BorderLayout.NORTH);

        sectionsPanel = new ScrollablePanel();
        sectionsPanel.setLayout(new BoxLayout(sectionsPanel, BoxLayout.Y_AXIS));
        sectionsPanel.setBackground(ScreenStyle.PAGE_BG);
        sectionsPanel.setBorder(new EmptyBorder(8, 24, 24, 24));
        sectionsPanel.addComponentListener(new ComponentAdapter() {
            private int lastWidth = -1;

            @Override
            public void componentResized(ComponentEvent e) {
                if (sectionsPanel.getWidth() != lastWidth) {
                    lastWidth = sectionsPanel.getWidth();
                    sectionsPanel.revalidate();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(sectionsPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(ScreenStyle.PAGE_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        centerLayout = new CardLayout();
        centerCards = new JPanel(centerLayout);
        centerCards.setOpaque(false);
        centerCards.add(scroll, "shop");
        detailHolder = new JPanel(new BorderLayout());
        detailHolder.setBackground(ScreenStyle.PAGE_BG);
        centerCards.add(detailHolder, "detail");
        body.add(centerCards, BorderLayout.CENTER);
        return body;
    }

    private void rebuildSections() {
        String query = searchField == null ? "" : searchField.getText().trim();
        List<Book> matches = productService.search(query).stream().filter(book -> {
            String category = Book.CATEGORIES.contains(book.getCategory()) ? book.getCategory() : TABS[0];
            return category.equals(TABS[selectedTab]);
        }).toList();

        sectionsPanel.removeAll();
        if (matches.isEmpty()) {
            JLabel empty = new JLabel(query.isEmpty() ? "ยังไม่มีสินค้าในหมวด " + TABS[selectedTab] : "ไม่พบหนังสือที่ค้นหาในหมวด " + TABS[selectedTab]);
            empty.setFont(ScreenStyle.font(Font.PLAIN, 15f));
            empty.setForeground(ScreenStyle.MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            empty.setBorder(new EmptyBorder(20, 0, 0, 0));
            sectionsPanel.add(empty);
        } else {
            int split = Math.min(ROW_SIZE, matches.size());
            addSection(TABS[selectedTab] + "แนะนำสำหรับคุณ", matches.subList(0, split));
            if (split < matches.size()) {
                addSection(TABS[selectedTab] + "ใหม่ติดเทรน", matches.subList(split, matches.size()));
            }
        }
        sectionsPanel.add(Box.createVerticalGlue());
        sectionsPanel.revalidate();
        sectionsPanel.repaint();
        SwingUtilities.invokeLater(sectionsPanel::revalidate);
    }

    private void addSection(String title, List<Book> products) {
        JLabel heading = new JLabel(title);
        heading.setFont(ScreenStyle.font(Font.BOLD, 19f));
        heading.setForeground(ScreenStyle.TEXT);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(14, 0, 12, 0));
        sectionsPanel.add(heading);

        JPanel row = new JPanel(new RowLayout(FlowLayout.LEFT, 20, 18));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (Book book : products) {
            row.add(new BookCard(book, this::addToCart, this::showDetail));
        }
        sectionsPanel.add(row);
    }

    private void addToCart(Book product) {
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
            cart.set(existingIndex, new OrderLine(product, alreadyInCart + 1));
        } else {
            cart.add(new OrderLine(product, 1));
        }
        updateCartButton();
    }

    private void updateCartButton() {
        int count = 0;
        for (OrderLine item : cart) {
            count += item.getQuantity();
        }
        cartNavButton.setText("ตะกร้า (" + count + ")");
    }

    private void showCartDialog() {
        CartWindow.show(this, cart, this::updateCartButton, this::checkout);
    }

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
        for (OrderLine item : cart) {
            productQuantities.put(item.getProduct().getId(), item.getQuantity());
        }
        try {
            Order order = application.checkout.checkout(loggedInMember.getId(), productQuantities);

            cart.clear();
            updateCartButton();
            rebuildSections();

            JOptionPane.showMessageDialog(parent,
                    "สั่งซื้อสำเร็จ!\nเลขที่ออเดอร์: " + order.getOrderId()
                            + String.format(Locale.US, "%nยอดรวม: %,.2f บาท", order.getTotal()),
                    "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(parent, "สั่งซื้อไม่สำเร็จ: " + e.getMessage(),
                    "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void showHistoryDialog() {
        if (loggedInMember == null && !promptLogin()) return;
        OrderHistoryWindow.show(this, loggedInMember, orderService);
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

    private boolean promptLogin() {
        LoginManager.Result result = LoginWindow.show(this, application);
        if (result == null) return false;
        if (result.administrator() != null) {
            AdminWindow.open(this, application, result.administrator(), this::refreshInventory);
            return false;
        }
        loggedInMember = result.member();
        updateLoginButton();
        return true;
    }

    private void updateLoginButton() {
        loginButton.setText(loggedInMember == null ? "เข้าสู่ระบบ / สมัครสมาชิก" : "สมาชิก: " + loggedInMember.getUsername());
    }

    private void selectTab(int index) {
        selectedTab = index;
        tabBar.setSelectedIndex(index);
        rebuildSections();
        showShopList();
    }

    private void showShopList() {
        if (centerLayout != null) {
            centerLayout.show(centerCards, "shop");
        }
    }

    private void showDetail(Book product) {
        Book current = productService.getProductById(product.getId()).orElse(null);
        if (current == null) { refreshInventory(); return; }
        detailHolder.removeAll();
        detailHolder.add(new BookDetails(current, this::showShopList, this::addToCart));
        detailHolder.revalidate();
        detailHolder.repaint();
        centerLayout.show(centerCards, "detail");
    }

    private void refreshInventory() {
        cart.removeIf(item -> productService.getProductById(item.getProduct().getId()).isEmpty());
        updateCartButton();
        showShopList();
        rebuildSections();
    }
}
