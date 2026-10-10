package service;

import app.DataFiles;
import app.ShopSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import model.Book;
import model.Order;
import repository.BookFile;
import repository.OrderFile;

public class AdminSystemTest {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("bookshop-admin-test-");
        try {
            checkAdminWorkflow(directory.resolve("normal"));
            checkFailedSaleRollsBack(directory.resolve("failed-sale"));
            checkInventoryRollback(directory.resolve("failed-inventory"));
            checkEditableAdminCsv(directory.resolve("editable-admins"));
            checkOrderNumbers(directory.resolve("order-numbers"));
            System.out.println("Admin, inventory, CSV and transaction checks passed");
        } finally {
            try (var files = Files.walk(directory)) {
                for (Path file : files.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(file);
            }
        }
    }

    private static void checkAdminWorkflow(Path directory) throws Exception {
        ShopSystem app = new ShopSystem(new DataFiles(directory));
        char[] password = "TestSecret123!".toCharArray();
        expectFailure(() -> app.adminAuth.requireSession(null));
        expectFailure(() -> app.adminAuth.setup("owner", "short".toCharArray(), "short".toCharArray()));
        require(!app.adminAuth.isConfigured(), "Failed setup must not write an account");
        AdminAccounts.Session session = app.adminAuth.setup("owner", password, password);
        LoginManager.Result administrator = app.login.login(" owner ", password);
        require(administrator.member() == null && administrator.administrator() != null, "Shared login identifies the administrator");
        app.adminAuth.requireSession(administrator.administrator());
        app.adminAuth.logout(administrator.administrator());
        expectFailure(() -> app.login.login("owner", "incorrect".toCharArray()));
        app.members.register("m01", "member01", "memberpass", "memberpass", "Bangkok", "0812345678");
        LoginManager.Result memberLogin = app.login.login("member01", "memberpass".toCharArray());
        require(memberLogin.member().getId().equals("m01") && memberLogin.administrator() == null, "Members do not receive an admin session");
        expectFailure(() -> app.login.login("member01", "incorrect".toCharArray()));
        expectFailure(() -> app.login.login("unknown", password));
        require(Files.readString(directory.resolve("admin.csv")).startsWith("username,password"), "Administrator CSV has editable columns");
        expectFailure(() -> app.adminAuth.setup("other", password, password));
        expectFailure(() -> app.adminAuth.login("owner", "incorrect".toCharArray()));
        expectFailure(() -> app.adminAuth.login("someone", password));
        AdminAccounts otherAuth = new AdminAccounts(directory.resolve("admin.csv"));
        expectFailure(() -> otherAuth.requireSession(session));
        otherAuth.logout(otherAuth.login("owner", password));

        String title = "Book, \"quoted\" ภาษาไทย";
        String details = "บรรทัดแรก, ข้อมูล\nบรรทัดที่สอง \"คำพูด\"";
        Book book = app.inventory.addBook(session, title, "Writer", "Fiction", "Publisher", 150.25, 10, details);
        require(book.getId().equals("b001"), "Book numbering starts at b001");
        require(app.inventory.search(session, "writer").size() == 1, "Search by author");
        Book stored = new BookFile(directory.resolve("books.csv").toString()).findById(book.getId());
        require(stored.getName().equals(title) && stored.getDescription().equals(details), "Quoted and multiline CSV round trip");
        require(stored.getPublisher().equals("Publisher") && stored.getCategory().equals("Fiction"), "Save all fields");
        expectFailure(() -> app.inventory.addBook(session, "Title", "", "Fiction", "Publisher", 10, 1, ""));
        expectFailure(() -> app.inventory.addBook(session, "Title", "Writer", "Fiction", "Publisher", Double.NaN, 1, ""));
        expectFailure(() -> app.inventory.addBook(session, "Title", "Writer", "Fiction", "Publisher", Double.POSITIVE_INFINITY, 1, ""));
        expectFailure(() -> app.inventory.addBook(session, "Title", "Writer", "Fiction", "Publisher", 10, -1, ""));
        expectFailure(() -> app.inventory.addBook(session, "Title", "Writer", "Fiction", "Publisher", 10, 1, "x".repeat(501)));
        app.inventory.adjustStock(session, book.getId(), 5);
        app.inventory.adjustStock(session, book.getId(), -3);
        require(book.getStock() == 12, "Add and reduce stock");
        expectFailure(() -> app.inventory.adjustStock(session, book.getId(), -13));
        expectFailure(() -> app.inventory.adjustStock(session, book.getId(), Integer.MAX_VALUE));
        expectFailure(() -> app.inventory.sell(session, Map.of(book.getId(), 13)));
        require(book.getStock() == 12 && app.orders.getAllOrders().isEmpty(), "Failed sale leaves stock and orders unchanged");
        Order sale = app.inventory.sell(session, Map.of(book.getId(), 2));
        require(book.getStock() == 10 && sale.getTotal() == 300.5, "Sale saves stock and total");
        book.setPrice(999);
        book.setName("Changed title");
        app.products.save();
        ShopSystem reloaded = new ShopSystem(new DataFiles(directory));
        require(reloaded.orders.getAllOrders().get(0).getTotal() == 300.5, "Historical prices do not change");
        require(reloaded.orders.getAllOrders().get(0).getItems().get(0).getProductName().equals(title), "Historical title does not change");
        app.inventory.deleteBook(session, book.getId());
        reloaded = new ShopSystem(new DataFiles(directory));
        require(reloaded.products.getAllProducts().isEmpty() && reloaded.orders.getAllOrders().get(0).getTotal() == 300.5,
                "Deleting inventory preserves order history");
        AdminAccounts.Session reloadedSession = reloaded.adminAuth.login("owner", password);
        Book next = reloaded.inventory.addBook(reloadedSession, "Next book", "Writer", "หนังสือ", "Publisher", 100, 1, "");
        require(next.getId().equals("b002"), "Restart after deleting a sold book does not reuse its ID");
        reloaded.adminAuth.logout(reloadedSession);
        app.adminAuth.logout(session);
        expectFailure(() -> app.inventory.addBook(session, "Title", "Writer", "Fiction", "Publisher", 10, 1, ""));
        expectFailure(() -> app.inventory.search(session, ""));
        Arrays.fill(password, '\0');
    }

    private static void checkOrderNumbers(Path directory) {
        ShopSystem app = new ShopSystem(new DataFiles(directory));
        Book book = new Book("b01", "Test book", 100, 10);
        app.products.addProduct(book);
        app.products.save();
        require(app.checkout.checkout("m01", Map.of("b01", 1)).getOrderId().equals("O001"), "Order numbers start at O001");
        ShopSystem reloaded = new ShopSystem(new DataFiles(directory));
        require(reloaded.checkout.checkout("m01", Map.of("b01", 1)).getOrderId().equals("O002"), "Order numbering continues after restart");
        expectFailure(() -> reloaded.checkout.checkout("m01", Map.of("b01", 100)));
        require(reloaded.orders.nextOrderId().equals("O003"), "Failed checkout does not consume a number");
        reloaded.orders.createOrder("O999", "m01", "2026-10-10", Map.of("b01", 1));
        require(reloaded.checkout.checkout("m01", Map.of("b01", 1)).getOrderId().equals("O1000"), "Order numbers expand beyond three digits");
    }

    private static void checkFailedSaleRollsBack(Path directory) throws Exception {
        Files.createDirectories(directory);
        BookFile productRepository = new BookFile(directory.resolve("books.csv").toString());
        productRepository.saveAll(List.of(new Book("b01", "Book", 100, 10)));
        OrderFile orderRepository = new OrderFile(directory.resolve("orders.csv").toString(), productRepository) {
            @Override public void saveAll(List<Order> orders) {
                super.saveAll(orders);
                throw new IllegalStateException("Simulated order write failure");
            }
        };
        BookManager products = new BookManager(productRepository);
        OrderManager orders = new OrderManager(products, orderRepository);
        PurchaseManager checkout = new PurchaseManager(products, orders, productRepository.getFilePath(), orderRepository.getFilePath());
        byte[] originalProducts = Files.readAllBytes(productRepository.getFilePath());
        expectFailure(() -> checkout.checkout("member", Map.of("b01", 2)));
        require(products.getProductById("b01").orElseThrow().getStock() == 10 && orders.getAllOrders().isEmpty(), "Memory rollback");
        require(Arrays.equals(originalProducts, Files.readAllBytes(productRepository.getFilePath())), "Book file rollback");
        require(!Files.exists(orderRepository.getFilePath()), "New order file rollback");
        Files.writeString(orderRepository.getFilePath(), "orderId,memberId,date,productId,productName,qty,subtotal\n");
        byte[] originalOrders = Files.readAllBytes(orderRepository.getFilePath());
        expectFailure(() -> checkout.checkout("member", Map.of("b01", 2)));
        require(Arrays.equals(originalOrders, Files.readAllBytes(orderRepository.getFilePath())), "Existing order file rollback");
    }

    private static void checkInventoryRollback(Path directory) throws Exception {
        Files.createDirectories(directory);
        BookFile repository = new BookFile(directory.resolve("books.csv").toString()) {
            @Override public void saveAll(List<Book> books) { throw new IllegalStateException("Simulated inventory write failure"); }
        };
        BookManager products = new BookManager(repository);
        Book original = new Book("b01", "Book", 100, 10);
        products.addProduct(original);
        AdminAccounts authentication = new AdminAccounts(directory.resolve("admin.csv"));
        char[] password = "TestSecret123!".toCharArray();
        AdminAccounts.Session session = authentication.setup("owner", password, password);
        AdminBookManager inventory = new AdminBookManager(authentication, products, null, new OrderManager(products));
        expectFailure(() -> inventory.addBook(session, "New Book", "Writer", "Fiction", "Publisher", 10, 1, ""));
        require(products.getAllProducts().size() == 1, "Failed add rolls back");
        expectFailure(() -> inventory.adjustStock(session, "b01", -2));
        require(original.getStock() == 10, "Failed stock save rolls back");
        expectFailure(() -> inventory.deleteBook(session, "b01"));
        require(products.getAllProducts().get(0) == original, "Failed delete restores the item and its position");
        authentication.logout(session);
        Arrays.fill(password, '\0');
    }

    private static void checkEditableAdminCsv(Path directory) throws Exception {
        Files.createDirectories(directory);
        Path file = directory.resolve("admin.csv");
        Files.writeString(file, "username,password\nadmin,Admin1234!\n");
        ShopSystem app = new ShopSystem(new DataFiles(directory));
        app.members.register("m01", "editor01", "memberpass", "memberpass", "Bangkok", "0812345678");
        require(app.login.login("editor01", "memberpass".toCharArray()).member() != null, "Account starts as a member");
        AdminAccounts.Session previous = app.adminAuth.login("admin", "Admin1234!".toCharArray());

        Files.writeString(file, "\uFEFFusername,password\nadmin,Changed123!\neditor01,Editor123!\nmanager,\"Comma,Password\"\n");
        expectFailure(() -> app.adminAuth.requireSession(previous));
        expectFailure(() -> app.adminAuth.login("admin", "Admin1234!".toCharArray()));
        app.adminAuth.logout(app.adminAuth.login("admin", "Changed123!".toCharArray()));
        app.adminAuth.logout(app.adminAuth.login("manager", "Comma,Password".toCharArray()));
        LoginManager.Result editor = app.login.login("editor01", "Editor123!".toCharArray());
        require(editor.administrator() != null && editor.member() == null, "CSV addition promotes an account without restarting");
        expectFailure(() -> app.login.login("editor01", "memberpass".toCharArray()));

        Files.writeString(file, "username,password\nadmin,Changed123!\n");
        expectFailure(() -> app.adminAuth.requireSession(editor.administrator()));
        require(!app.adminAuth.isAdminUsername("editor01"), "Removing a CSV row removes the admin role");
        require(app.login.login("editor01", "memberpass".toCharArray()).member() != null, "The member account is preserved");

        Files.writeString(file, "username,password\nadmin,First123!\nadmin,Second123!\n");
        expectFailure(() -> app.adminAuth.login("admin", "First123!".toCharArray()));
        Files.writeString(file, "username,password\nadmin,\n");
        expectFailure(() -> app.adminAuth.isConfigured());
        Files.writeString(file, "wrong,password\nadmin,Changed123!\n");
        expectFailure(() -> app.adminAuth.isConfigured());
        Files.writeString(file, "username,password\n");
        require(!app.adminAuth.isConfigured(), "Header-only CSV allows first-time setup");
        AdminAccounts.Session configured = app.adminAuth.setup("newadmin", "Setup123!".toCharArray(), "Setup123!".toCharArray());
        app.adminAuth.requireSession(configured);
        app.adminAuth.logout(configured);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void expectFailure(Runnable action) {
        try { action.run(); }
        catch (IllegalArgumentException | IllegalStateException | SecurityException expected) { return; }
        throw new AssertionError("Expected operation to fail");
    }
}
