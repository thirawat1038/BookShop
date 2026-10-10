package service;

// เพิ่ม ลด และลบหนังสือโดยแอดมิน

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import model.Book;
import model.Order;

public final class AdminBookManager {
    private final AdminAccounts authentication;
    private final BookManager products;
    private final PurchaseManager checkout;
    private final OrderManager orders;

    public AdminBookManager(AdminAccounts authentication, BookManager products, PurchaseManager checkout, OrderManager orders) {
        this.authentication = authentication;
        this.products = products;
        this.checkout = checkout;
        this.orders = orders;
    }

    private String nextBookId() {
        Stream<String> currentIds = products.getAllProducts().stream().map(Book::getId);
        Stream<String> historicalIds = orders.getAllOrders().stream().flatMap(order -> order.getItems().stream())
                .map(item -> item.getProduct().getId());
        BigInteger highest = Stream.concat(currentIds, historicalIds).filter(id -> id.matches("b\\d+"))
                .map(id -> new BigInteger(id.substring(1))).max(BigInteger::compareTo).orElse(BigInteger.ZERO);
        return String.format(java.util.Locale.ROOT, "b%03d", highest.add(BigInteger.ONE));
    }

    public List<Book> search(AdminAccounts.Session session, String query) {
        authentication.requireSession(session);
        return products.search(query);
    }

    public Book addBook(AdminAccounts.Session session, String name, String author, String category,
                           String publisher, double price, int stock, String description) {
        authentication.requireSession(session);
        requireText(name, "ชื่อหนังสือ");
        requireText(author, "ผู้แต่ง");
        requireText(category, "หมวดหมู่");
        requireText(publisher, "สำนักพิมพ์");
        Book book = new Book(nextBookId(), name.trim(), price, stock, null, null,
                author.trim(), category.trim(), publisher.trim(), description == null ? "" : description);
        products.addProduct(book);
        try {
            products.save();
            return book;
        } catch (RuntimeException error) {
            products.removeProduct(book.getId());
            throw error;
        }
    }

    public void adjustStock(AdminAccounts.Session session, String id, int change) {
        authentication.requireSession(session);
        Book book = products.getProductById(id).orElseThrow(() -> new IllegalArgumentException("ไม่พบหนังสือ"));
        int previous = book.getStock();
        if (change == 0) throw new IllegalArgumentException("จำนวนที่ปรับต้องไม่เป็น 0");
        long next = (long) previous + change;
        if (next < 0 || next > Integer.MAX_VALUE) throw new IllegalArgumentException("จำนวนคงเหลือไม่ถูกต้อง");
        book.setStock((int) next);
        try { products.save(); }
        catch (RuntimeException error) { book.setStock(previous); throw error; }
    }

    public void deleteBook(AdminAccounts.Session session, String id) {
        authentication.requireSession(session);
        Book book = products.getProductById(id).orElseThrow(() -> new IllegalArgumentException("ไม่พบหนังสือ"));
        int index = products.getAllProducts().indexOf(book);
        products.removeProduct(id);
        try { products.save(); }
        catch (RuntimeException error) { products.restoreProduct(index, book); throw error; }
    }

    public Order sell(AdminAccounts.Session session, Map<String, Integer> quantities) {
        authentication.requireSession(session);
        return checkout.checkout("ADMIN_COUNTER:" + session.username(), quantities);
    }

    private static void requireText(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("กรุณากรอก" + label);
    }
}
