package ui;

// เมนูและโครงหน้าจอแอดมิน

import app.ShopSystem;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import model.Book;
import service.AdminAccounts;

final class AdminMenu extends JPanel {
    private static final String[] TABS = {"Dashboard", "Search", "Sell", "Add Book"};
    private final ShopSystem application;
    private final AdminAccounts.Session session;
    private final Runnable onInventoryChanged;
    private final JPanel content = new JPanel(new BorderLayout());
    private final List<JButton> tabs = new ArrayList<>();
    private final Map<String, Integer> saleItems = new LinkedHashMap<>();

    AdminMenu(ShopSystem application, AdminAccounts.Session session, Runnable onInventoryChanged) {
        super(new BorderLayout());
        application.adminAuth.requireSession(session);
        this.application = application;
        this.session = session;
        this.onInventoryChanged = onInventoryChanged;
        setPreferredSize(new Dimension(820, 570));
        setBackground(ScreenStyle.PAGE_BG);
        add(buildHeader(), BorderLayout.NORTH);
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(10, 20, 16, 20));
        add(content, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ScreenStyle.LINE), BorderFactory.createEmptyBorder(5, 16, 5, 16)));
        JLabel account = new JLabel("แอดมิน: " + session.username());
        account.setForeground(ScreenStyle.MUTED);
        account.setFont(ScreenStyle.font(Font.PLAIN, 11f));
        footer.add(account);
        add(footer, BorderLayout.SOUTH);
        showPage(0);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        JLabel title = new JLabel("Book Management System", SwingConstants.CENTER);
        title.setFont(ScreenStyle.font(Font.BOLD, 14f));
        title.setPreferredSize(new Dimension(100, 42));
        header.add(title, BorderLayout.NORTH);
        JPanel navigation = new JPanel(new GridLayout(1, TABS.length));
        navigation.setOpaque(false);
        for (int index = 0; index < TABS.length; index++) {
            final int page = index;
            JButton tab = new JButton(TABS[index] + "  •");
            tab.setFont(ScreenStyle.font(Font.PLAIN, 13f));
            tab.setContentAreaFilled(false);
            tab.setPreferredSize(new Dimension(100, 32));
            tab.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            tab.addActionListener(event -> showPage(page));
            navigation.add(tab);
            tabs.add(tab);
        }
        header.add(navigation, BorderLayout.CENTER);
        return header;
    }

    void showPage(int index) {
        application.adminAuth.requireSession(session);
        for (int position = 0; position < tabs.size(); position++) {
            tabs.get(position).setBorder(BorderFactory.createMatteBorder(position == index ? 3 : 1, 0, 1,
                    position == tabs.size() - 1 ? 0 : 1, position == index ? new Color(0x444444) : ScreenStyle.LINE));
        }
        content.removeAll();
        content.add(switch (index) {
            case 0 -> new AdminOverview(application, session, this::inventoryChanged);
            case 1 -> new AdminSearch(application, session, this::inventoryChanged);
            case 2 -> new AdminSell(application, session, this::inventoryChanged, saleItems);
            case 3 -> new AdminAddBook(application, session, this::inventoryChanged, this::showPage);
            default -> throw new IllegalArgumentException("Unknown page");
        });
        content.revalidate();
        content.repaint();
    }

    private void inventoryChanged() {
        saleItems.keySet().removeIf(id -> application.products.getProductById(id).isEmpty());
        onInventoryChanged.run();
    }

    static abstract class Page extends JPanel {
        protected final ShopSystem application;
        protected final AdminAccounts.Session session;
        private final Runnable onInventoryChanged;

        Page(ShopSystem application, AdminAccounts.Session session, Runnable onInventoryChanged) {
            super(new BorderLayout());
            this.application = application;
            this.session = session;
            this.onInventoryChanged = onInventoryChanged;
            setOpaque(false);
        }

        protected JPanel page(String title, String subtitle, JPanel card) {
            JPanel page = new JPanel(new BorderLayout(0, 6));
            page.setOpaque(false);
            JPanel heading = new JPanel(new GridLayout(2, 1));
            heading.setPreferredSize(new Dimension(100, 32));
            heading.setOpaque(false);
            JLabel headingText = new JLabel(title);
            headingText.setFont(ScreenStyle.font(Font.BOLD, 16f));
            JLabel description = new JLabel(subtitle);
            description.setFont(ScreenStyle.font(Font.PLAIN, 11f));
            description.setForeground(ScreenStyle.MUTED);
            heading.add(headingText);
            heading.add(description);
            page.add(heading, BorderLayout.NORTH);
            page.add(card, BorderLayout.CENTER);
            return page;
        }

        protected JPanel card(String title) {
            JPanel card = ScreenParts.roundedPanel(Color.WHITE);
            card.setLayout(new BorderLayout(0, 12));
            JLabel heading = new JLabel(title);
            heading.setFont(ScreenStyle.font(Font.BOLD, 18f));
            heading.setPreferredSize(new Dimension(100, 24));
            card.add(heading, BorderLayout.NORTH);
            return card;
        }

        protected JTextField searchHeader(JPanel card, String title) {
            JPanel header = new JPanel(new BorderLayout(15, 0));
            header.setOpaque(false);
            JLabel label = new JLabel(title);
            label.setFont(ScreenStyle.font(Font.BOLD, 18f));
            JTextField query = ScreenParts.input("ค้นหาจากชื่อเรื่อง ผู้แต่ง หรือ รหัส...");
            query.setPreferredSize(new Dimension(250, 34));
            query.getAccessibleContext().setAccessibleName("ค้นหาหนังสือ");
            header.add(label, BorderLayout.WEST);
            header.add(query, BorderLayout.EAST);
            card.add(header, BorderLayout.NORTH);
            return query;
        }

        protected JScrollPane tableScroll(JTable table) {
            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getViewport().setBackground(Color.WHITE);
            return scroll;
        }

        protected void inventoryChanged() { onInventoryChanged.run(); }

        @FunctionalInterface protected interface UiAction { String run() throws Exception; }

        protected void perform(UiAction action) {
            try { inform(action.run()); }
            catch (Exception error) {
                JOptionPane.showMessageDialog(this, error.getMessage(), "ดำเนินการไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
            }
        }

        protected void inform(String message) { JOptionPane.showMessageDialog(this, message, "แจ้งเตือน", JOptionPane.INFORMATION_MESSAGE); }

        protected static String money(double value) { return String.format(Locale.US, "%,.2f", value); }

        protected static String author(Book book) { return book.getAuthor().isBlank() ? "—" : book.getAuthor(); }
    }
}
