package ui;

import app.DataFiles;
import app.ShopSystem;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import service.AdminAccounts;

public class ScreenTest {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("bookshop-ui-test-");
        try {
            ShopSystem app = new ShopSystem(new DataFiles(directory));
            char[] password = "PreviewAccount123!".toCharArray();
            AdminAccounts.Session session = app.adminAuth.setup("preview", password, password);
            java.util.Arrays.fill(password, '\0');
            app.inventory.addBook(session, "Civil War", "Mark Millar", "Comics", "Marvel", 599, 15, "");
            app.inventory.addBook(session, "Dune", "Frank Herbert", "Fiction", "Publisher", 595, 12, "");
            app.inventory.addBook(session, "Atomic Habits", "James Clear", "Self Help", "Publisher", 285, 8, "");
            app.inventory.addBook(session, "The Infinity Gauntlet", "Jim Starlin", "Comics", "Marvel", 679, 20, "");
            SwingUtilities.invokeAndWait(() -> {
                try { exercisePages(app, session); }
                catch (Exception error) { throw new RuntimeException(error); }
            });
            app.adminAuth.logout(session);
            System.out.println("Admin UI: search, select, sell, add, stock, delete and layout checks passed");
        } finally {
            try (var files = Files.walk(directory)) {
                for (Path file : files.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(file);
            }
        }
    }

    private static void exercisePages(ShopSystem app, AdminAccounts.Session session) throws Exception {
        ScreenStyle.install();
        int[] updates = {0};
        AdminMenu panel = new AdminMenu(app, session, () -> updates[0]++);
        JFrame preview = new JFrame();
        preview.setContentPane(panel);
        preview.pack();
        InsetsSize.setContentSize(preview, 660, 462);
        try {
        panel.showPage(2);
        render(panel, "target/admin-sell-preview.png");
        panel.showPage(3);
        render(panel, "target/admin-add-book-preview.png");
        panel.showPage(0);
        render(panel, "target/admin-dashboard-preview.png");
        panel.showPage(1);
        render(panel, "target/admin-search-preview.png");

        panel.showPage(2);
        JTextField query = first(panel, JTextField.class);
        query.setText("dune");
        JTable table = first(panel, JTable.class);
        require(table.getRowCount() == 1, "Live search filters the table");
        require(!table.isCellEditable(0, 0) && table.isCellEditable(0, 4), "Only the action column is editable");
        require(table.editCellAt(0, 4), "Selection button starts editing");
        JButton choose = first((Container) table.getEditorComponent(), JButton.class);
        answerDialogs(choose::doClick);
        require("เลือกแล้ว".equals(table.getValueAt(0, 4)), "Selection updates the row");
        answerDialogs(() -> button(panel, "วางขาย").doClick());
        require(app.orders.getAllOrders().size() == 1 && app.products.search("Dune").get(0).getStock() == 11, "Sell button saves and deducts stock");

        panel.showPage(3);
        input(panel, "ชื่อหนังสือ").setText("หนังสือทดสอบ UI");
        input(panel, "ผู้แต่ง").setText("ผู้แต่งทดสอบ");
        JComboBox<?> category = first(panel, JComboBox.class);
        require(!category.isEditable() && category.getSelectedIndex() == -1, "Category requires selection from the existing shop categories");
        require(java.util.stream.IntStream.range(0, category.getItemCount()).mapToObj(category::getItemAt).toList()
                .equals(model.Book.CATEGORIES), "Add form uses the shop categories");
        category.setSelectedItem("หนังสือ");
        input(panel, "สำนักพิมพ์").setText("สำนักพิมพ์ทดสอบ");
        input(panel, "ราคา (บาท)").setText("120.50");
        input(panel, "จำนวน (เล่ม)").setText("4");
        JTextArea description = first(panel, JTextArea.class);
        description.setText("x".repeat(501));
        require(description.getText().length() == 500, "Description enforces 500-character limit");
        description.setText("รายละเอียด, พร้อมภาษาไทย\nบรรทัดใหม่");
        answerDialogs(() -> button(panel, "บันทึก").doClick());
        require(app.products.getAllProducts().size() == 5, "Add button saves a new book");
        require(app.products.search("หนังสือทดสอบ UI").get(0).getCategory().equals("หนังสือ"), "Selected category is saved");
        require(input(panel, "ชื่อหนังสือ").getText().isEmpty(), "Successful save resets the form");

        panel.showPage(1);
        first(panel, JTextField.class).setText("หนังสือทดสอบ UI");
        JTable inventory = first(panel, JTable.class);
        inventory.setRowSelectionInterval(0, 0);
        answerDialogs(() -> button(panel, "เพิ่ม / ลดสต๊อก").doClick());
        require(app.products.search("หนังสือทดสอบ UI").get(0).getStock() == 5, "Stock button saves the adjustment");
        inventory.setRowSelectionInterval(0, 0);
        answerDialogs(() -> button(panel, "ลบหนังสือ").doClick());
        require(app.products.search("หนังสือทดสอบ UI").isEmpty(), "Delete button removes the book");
        require(updates[0] == 4, "Successful mutations refresh the shop");
        checkSharedLogin(app);
        } finally { preview.dispose(); }
    }

    private static void answerDialogs(Runnable action) {
        Timer timer = new Timer(60, event -> {
            for (Window window : Window.getWindows()) {
                if (!window.isVisible()) continue;
                for (JOptionPane option : find(window, JOptionPane.class)) option.setValue(JOptionPane.OK_OPTION);
            }
        });
        timer.start();
        try { action.run(); } finally { timer.stop(); }
    }

    private static void checkSharedLogin(ShopSystem app) {
        ShopWindow shop = new ShopWindow(app);
        try {
            checkAddToCart(shop);
            checkCategories(shop, app);
            require(find(shop, JButton.class).stream().noneMatch(button -> "แอดมิน".equals(button.getText())),
                    "Shop has no separate administrator button");
            checkRegistration(shop, app);
            LoginWindow loginPreview = new LoginWindow(shop, app);
            try {
                require(find(loginPreview, JTextField.class).size() == 2, "Login contains only username and password inputs");
                require(first(loginPreview, javax.swing.JPasswordField.class).getEchoChar() == '*', "Login password is masked with asterisks");
                render(loginPreview.getContentPane(), "target/login-preview.png");
            } catch (Exception error) { throw new RuntimeException(error); }
            finally { loginPreview.dispose(); }
            require(signIn(shop, "preview", "PreviewAccount123!"), "Shared login opens the administrator window");
            app.members.register(app.members.nextMemberId(), "member01", "memberpass", "memberpass", "Bangkok", "0812345678");
            require(!signIn(shop, "member01", "memberpass"), "Member login stays in the shop");
            require(button(shop, "สมาชิก: member01") != null, "Member login updates the account button");
        } finally { shop.dispose(); }
    }

    private static void checkAddToCart(ShopWindow shop) {
        BookCard card = first(shop, BookCard.class);
        BookCard.BookCover cover = first(card, BookCard.BookCover.class);
        cover.setSize(cover.getPreferredSize());
        java.awt.event.MouseEvent click = new java.awt.event.MouseEvent(cover,
                java.awt.event.MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 10, 10, 1, false,
                java.awt.event.MouseEvent.BUTTON1);
        for (var listener : cover.getMouseListeners()) listener.mouseReleased(click);
        boolean[] cartOpened = {false};
        Timer timer = new Timer(60, event -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof javax.swing.JDialog dialog && dialog.isVisible()) {
                    cartOpened[0] = true;
                    dialog.dispose();
                }
            }
        });
        timer.start();
        try {
            button(shop, "เพิ่มลงตะกร้า").doClick();
            button(shop, "เพิ่มลงตะกร้า").doClick();
        } finally { timer.stop(); }
        require(!cartOpened[0], "Adding from book details does not open a dialog");
        require(button(shop, "ตะกร้า (2)") != null, "Adding from details updates cart quantity");
    }

    private static void checkCategories(ShopWindow shop, ShopSystem app) {
        for (int index = 1; index < model.Book.CATEGORIES.size(); index++) {
            app.products.addProduct(new model.Book("category-" + index, "Category item " + index, 100, 5,
                    null, null, "Author", model.Book.CATEGORIES.get(index), "Publisher", ""));
        }
        ScreenControls.ShopTabBar tabs = first(shop, ScreenControls.ShopTabBar.class);
        tabs.setSize(400, 46);
        JTextField search = first(shop, JTextField.class);
        for (int index = 1; index < model.Book.CATEGORIES.size(); index++) {
            chooseCategory(tabs, index);
            require(find(shop, BookCard.class).size() == 1, "Each category displays only its own books");
            search.setText("Civil War");
            require(find(shop, BookCard.class).isEmpty(), "Search does not show books from other categories");
            search.setText("Category item " + index);
            require(find(shop, BookCard.class).size() == 1, "Search finds books in the selected category");
            search.setText("");
        }
        chooseCategory(tabs, 0);
        require(find(shop, BookCard.class).size() == 4, "Legacy books stay in the physical books category");
    }

    private static void chooseCategory(ScreenControls.ShopTabBar tabs, int index) {
        var click = new java.awt.event.MouseEvent(tabs, java.awt.event.MouseEvent.MOUSE_RELEASED,
                System.currentTimeMillis(), 0, index * 100 + 10, 10, 1, false, java.awt.event.MouseEvent.BUTTON1);
        for (var listener : tabs.getMouseListeners()) listener.mouseReleased(click);
    }

    private static void checkRegistration(ShopWindow shop, ShopSystem app) {
        LoginWindow login = new LoginWindow(shop, app);
        int[] stage = {0};
        Throwable[] failure = {null};
        long deadline = System.currentTimeMillis() + 10_000;
        Timer timer = new Timer(80, event -> {
            try {
                if (System.currentTimeMillis() > deadline) throw new AssertionError("Registration timed out");
                if (stage[0] == 0 && login.isVisible()) {
                    stage[0] = 1;
                    button(login, "สมัครสมาชิก").doClick();
                    return;
                }
                for (Window window : Window.getWindows()) {
                    if (!window.isVisible()) continue;
                    if (stage[0] == 1 && window instanceof RegisterWindow registration) {
                        require(registration.getOwner() == login, "Registration belongs to the modal login window");
                        require(find(registration, javax.swing.JPasswordField.class).stream().allMatch(field -> field.getEchoChar() == '*'),
                                "Registration passwords are masked with asterisks");
                        input(registration, "ชื่อที่แสดง").setText("newmember");
                        input(registration, "อีเมล").setText("newmember@example.com");
                        input(registration, "รหัสผ่าน").setText("memberpass");
                        input(registration, "ยืนยันรหัสผ่าน").setText("memberpass");
                        input(registration, "ชื่อ").setText("สมชาย");
                        input(registration, "นามสกุล").setText("ทดสอบ");
                        stage[0] = 2;
                        button(registration, "สมัครสมาชิก").doClick();
                        continue;
                    }
                    for (JOptionPane option : find(window, JOptionPane.class)) option.setValue(JOptionPane.OK_OPTION);
                }
            } catch (Throwable error) {
                failure[0] = error;
                for (Window window : Window.getWindows()) {
                    if (window instanceof javax.swing.JDialog && window.isVisible()) window.dispose();
                }
            }
        });
        timer.setCoalesce(false);
        timer.start();
        try { login.setVisible(true); }
        finally { timer.stop(); login.dispose(); }
        if (failure[0] != null) throw new AssertionError("Registration flow failed", failure[0]);
        require(stage[0] == 2 && app.members.login("newmember", "memberpass").isPresent(),
                "Registration button creates a member from the login window");
    }

    private static boolean signIn(ShopWindow shop, String username, String password) {
        boolean[] administratorOpened = {false};
        long deadline = System.currentTimeMillis() + 10_000;
        Timer timer = new Timer(60, event -> {
            for (Window window : Window.getWindows()) {
                if (!window.isVisible()) continue;
                if (System.currentTimeMillis() > deadline) { window.dispose(); continue; }
                if (window instanceof LoginWindow login) {
                    input(login, "Username").setText(username);
                    input(login, "Password").setText(password);
                    button(login, "เข้าสู่ระบบ").doClick();
                    continue;
                }
                if (window instanceof javax.swing.JDialog dialog && dialog.getTitle().startsWith("Book Management System")) {
                    administratorOpened[0] = true;
                    window.dispose();
                    continue;
                }
                for (JOptionPane option : find(window, JOptionPane.class)) {
                    List<JTextField> fields = find(option, JTextField.class);
                    fields.stream().filter(field -> !(field instanceof javax.swing.JPasswordField)).forEach(field -> field.setText(username));
                    fields.stream().filter(field -> field instanceof javax.swing.JPasswordField).forEach(field -> field.setText(password));
                    Object[] options = option.getOptions();
                    option.setValue(options == null ? JOptionPane.OK_OPTION : options[0]);
                }
            }
        });
        timer.start();
        try { button(shop, "เข้าสู่ระบบ / สมัครสมาชิก").doClick(); }
        finally { timer.stop(); }
        return administratorOpened[0];
    }

    private static void render(Container panel, String file) throws Exception {
        Window window = SwingUtilities.getWindowAncestor(panel);
        if (window != null) window.validate();
        layout(panel);
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        panel.printAll(graphics);
        graphics.dispose();
        ImageIO.write(image, "png", Path.of(file).toFile());
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) if (child instanceof Container nested) layout(nested);
    }

    private static JTextField input(Container panel, String name) {
        return find(panel, JTextField.class).stream().filter(field -> name.equals(field.getAccessibleContext().getAccessibleName())).findFirst().orElseThrow();
    }

    private static JButton button(Container panel, String text) {
        return find(panel, JButton.class).stream().filter(button -> text.equals(button.getText())).findFirst().orElseThrow();
    }

    private static <T extends Component> T first(Container panel, Class<T> type) { return find(panel, type).get(0); }

    private static <T extends Component> List<T> find(Container parent, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Component child : parent.getComponents()) {
            if (type.isInstance(child)) result.add(type.cast(child));
            if (child instanceof Container nested) result.addAll(find(nested, type));
        }
        return result;
    }

    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }

    private static final class InsetsSize {
        static void setContentSize(JFrame frame, int width, int height) {
            var insets = frame.getInsets();
            frame.setSize(width + insets.left + insets.right, height + insets.top + insets.bottom);
            frame.validate();
        }
    }
}
