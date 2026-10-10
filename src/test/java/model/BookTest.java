package model;

public class BookTest {
    public static void main(String[] args) {
        BookTest test = new BookTest();

        test.testConstructorValid();
        test.testConstructorBoundaryZero();
        test.testConstructorNegativePriceThrows();
        test.testConstructorNegativeStockThrows();

        test.testSetPriceValid();
        test.testSetPriceNegativeThrows();

        test.testReduceStockNormal();
        test.testReduceStockExactBoundary();
        test.testReduceStockZeroQtyThrows();

        test.testIncreaseStockNormal();
        test.testIncreaseStockZeroQtyThrows();

        test.testEqualsSameId();
        test.testEqualsDifferentId();
        test.testHashCodeConsistentWithEquals();

        test.testCsvRoundTrip();

        System.out.println("ทดสอบผ่านทั้งหมด");
    }

    void testConstructorValid() {
        Book p = new Book("b01", "book01", 599.5, 10);
        assertEquals("b01", p.getId());
        assertEquals("book01", p.getName());
        assertEquals(599.5, p.getPrice());
        assertEquals(10, p.getStock());
    }

    void testConstructorBoundaryZero() {
        Book p = new Book("P002", "ของแถม", 0.0, 0);
        assertEquals(0.0, p.getPrice());
        assertEquals(0, p.getStock());
    }

    void testConstructorNegativePriceThrows() {
        try {
            new Book("P003", "ของผิดพลาด", -5.0, 10);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testConstructorNegativeStockThrows() {
        try {
            new Book("P004", "ของผิดพลาด", 10.0, -1);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testSetPriceValid() {
        Book p = new Book("P005", "น้ำดื่ม", 10.0, 50);
        p.setPrice(15.0);
        assertEquals(15.0, p.getPrice());
    }

    void testSetPriceNegativeThrows() {
        Book p = new Book("P006", "น้ำดื่ม", 10.0, 50);
        try {
            p.setPrice(-1.0);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testReduceStockNormal() {
        Book p = new Book("P007", "ขนมปัง", 25.0, 20);
        p.reduceStock(5);
        assertEquals(15, p.getStock());
    }

    void testReduceStockExactBoundary() {
        Book p = new Book("P008", "นม", 15.0, 10);
        p.reduceStock(10);
        assertEquals(0, p.getStock());
    }

    void testReduceStockZeroQtyThrows() {
        Book p = new Book("P010", "เกลือ", 8.0, 5);
        try {
            p.reduceStock(0);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testIncreaseStockNormal() {
        Book p = new Book("P011", "น้ำตาล", 12.0, 5);
        p.increaseStock(10);
        assertEquals(15, p.getStock());
    }

    void testIncreaseStockZeroQtyThrows() {
        Book p = new Book("P012", "น้ำตาล", 12.0, 5);
        try {
            p.increaseStock(0);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testEqualsSameId() {
        Book a = new Book("P013", "ชื่อ A", 10.0, 5);
        Book b = new Book("P013", "ชื่อ B", 99.0, 1);
        assertEquals(true, a.equals(b));
    }

    void testEqualsDifferentId() {
        Book a = new Book("P014", "ของเดียวกัน", 10.0, 5);
        Book b = new Book("P015", "ของเดียวกัน", 10.0, 5);
        assertEquals(false, a.equals(b));
    }

    void testHashCodeConsistentWithEquals() {
        Book a = new Book("P016", "ชื่อ A", 10.0, 5);
        Book b = new Book("P016", "ชื่อ B", 99.0, 1);

        assertEquals(a.hashCode(), b.hashCode());
    }

    void testCsvRoundTrip() {
        Book original = new Book("P017", "กาแฟ", 45.5, 30);
        String csvLine = original.toCsvLine();
        Book restored = Book.fromCsvLine(csvLine);

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getPrice(), restored.getPrice());
        assertEquals(original.getStock(), restored.getStock());
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
}
