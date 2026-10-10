package service;

import java.util.Optional;
import model.Book;

public class BookManagerTest {
    public static void main(String[] args) {
        BookManagerTest test = new BookManagerTest();

        test.testCalculateTotalStockValue();
        test.testCalculateTotalStockValueWhenEmpty();

        test.testHasProductOutOfStock();
        test.testHasProductOutOfStockWhenAllInStock();
        test.testHasProductOutOfStockWhenEmpty();

        test.testGetProductById();
        test.testGetProductByIdWhenNotFound();

        test.testAddProduct();
        test.testAddProductNullThrows();
        test.testAddProductDuplicateIdThrows();

        test.testRemoveProduct();
        test.testRemoveProductNotFoundDoesNothing();

        test.testGetAllProductsIsUnmodifiable();

        System.out.println("ทดสอบผ่าน");
    }

    void testCalculateTotalStockValue() {
        BookManager service = new BookManager();
        service.addProduct(new Book("b01", "book01", 150.0, 50));
        service.addProduct(new Book("b02", "book02", 165.0, 60));
        service.addProduct(new Book("b03", "book03", 100.0, 0));
        assertEquals(17400.0, service.calculateTotalStockValue());
    }

    void testCalculateTotalStockValueWhenEmpty() {
        BookManager service = new BookManager();
        assertEquals(0.0, service.calculateTotalStockValue());
    }

    void testHasProductOutOfStock() {
        BookManager service = new BookManager();
        service.addProduct(new Book("b01", "book01", 150.0, 50));
        service.addProduct(new Book("b02", "book02", 165.0, 0));
        assertEquals(true, service.hasProductOutOfStock());
    }

    void testHasProductOutOfStockWhenAllInStock() {
        BookManager service = new BookManager();
        service.addProduct(new Book("b01", "book01", 150.0, 50));
        service.addProduct(new Book("b02", "book02", 165.0, 1));
        assertEquals(false, service.hasProductOutOfStock());
    }

    void testHasProductOutOfStockWhenEmpty() {
        BookManager service = new BookManager();
        assertEquals(false, service.hasProductOutOfStock());
    }

    void testGetProductById() {
        BookManager service = new BookManager();
        Book book = new Book("b01", "book01", 150.0, 50);
        service.addProduct(book);

        Optional<Book> result = service.getProductById("b01");
        assertEquals(true, result.isPresent());
        assertEquals(book, result.get());
    }

    void testGetProductByIdWhenNotFound() {
        BookManager service = new BookManager();
        service.addProduct(new Book("b01", "book01", 150.0, 50));

        Optional<Book> result = service.getProductById("b99");
        assertEquals(true, result.isEmpty());
    }

    void testAddProduct() {
        BookManager service = new BookManager();
        Book book = new Book("b01", "book01", 150.0, 50);
        service.addProduct(book);

        assertEquals(1, service.getAllProducts().size());
        assertEquals(true, service.getAllProducts().contains(book));
    }

    void testAddProductNullThrows() {
        BookManager service = new BookManager();
        try {
            service.addProduct(null);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testAddProductDuplicateIdThrows() {
        BookManager service = new BookManager();
        service.addProduct(new Book("b01", "book01", 150.0, 50));
        try {
            service.addProduct(new Book("b01", "ชื่ออื่น", 99.0, 1));
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            assertEquals(1, service.getAllProducts().size());
            assertEquals("book01", service.getProductById("b01").get().getName());
        }
    }

    void testRemoveProduct() {
        BookManager service = new BookManager();
        service.addProduct(new Book("b01", "book01", 150.0, 50));
        service.addProduct(new Book("b02", "book02", 165.0, 60));

        service.removeProduct("b01");

        assertEquals(1, service.getAllProducts().size());
        assertEquals(true, service.getProductById("b01").isEmpty());
        assertEquals(true, service.getProductById("b02").isPresent());
    }

    void testRemoveProductNotFoundDoesNothing() {
        BookManager service = new BookManager();
        service.addProduct(new Book("b01", "book01", 150.0, 50));

        service.removeProduct("b99");

        assertEquals(1, service.getAllProducts().size());
    }

    void testGetAllProductsIsUnmodifiable() {
        BookManager service = new BookManager();
        try {
            service.getAllProducts().add(new Book("b01", "book01", 150.0, 50));
            throw new AssertionError("ควร throw UnsupportedOperationException แต่ไม่ throw");
        } catch (UnsupportedOperationException e) {
        }
    }

    private void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected: " + expected + " actual is: " + actual);
        }
    }

    private void assertEquals(double expected, double actual) {
        if (expected != actual) {
            throw new AssertionError("Expected: " + expected + " actual is: " + actual);
        }
    }

    private void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Expected: " + expected + " actual is: " + actual);
        }
    }

    private void assertEquals(boolean expected, boolean actual) {
        if (expected != actual) {
            throw new AssertionError("Expected: " + expected + " actual is: " + actual);
        }
    }

    private void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected: " + expected + " actual is: " + actual);
        }
    }
}
