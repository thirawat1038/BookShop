package model;


public class ProductTest {

    public static void main(String[] args) {
        ProductTest test = new ProductTest();

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

    // ---------- Constructor ----------

    void testConstructorValid() {
        Product p = new Product("b01", "book01", 599.5, 10);
        assertEquals("b01", p.getId());
        assertEquals("book01", p.getName());
        assertEquals(599.5, p.getPrice());
        assertEquals(10, p.getStock());
    }

    void testConstructorBoundaryZero() {
        Product p = new Product("P002", "ของแถม", 0.0, 0);
        assertEquals(0.0, p.getPrice());
        assertEquals(0, p.getStock());
    }

    void testConstructorNegativePriceThrows() {
        try {
            new Product("P003", "ของผิดพลาด", -5.0, 10);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            // ผ่าน: throw ถูกต้องตามที่คาดหวัง
        }
    }

    void testConstructorNegativeStockThrows() {
        try {
            new Product("P004", "ของผิดพลาด", 10.0, -1);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            // ผ่าน
        }
    }

    // ---------- setPrice ----------

    void testSetPriceValid() {
        Product p = new Product("P005", "น้ำดื่ม", 10.0, 50);
        p.setPrice(15.0);
        assertEquals(15.0, p.getPrice());
    }

    void testSetPriceNegativeThrows() {
        Product p = new Product("P006", "น้ำดื่ม", 10.0, 50);
        try {
            p.setPrice(-1.0);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            // ผ่าน
        }
    }

    // ---------- reduceStock ----------

    void testReduceStockNormal() {
        Product p = new Product("P007", "ขนมปัง", 25.0, 20);
        p.reduceStock(5);
        assertEquals(15, p.getStock());
    }

    void testReduceStockExactBoundary() {
        Product p = new Product("P008", "นม", 15.0, 10);
        p.reduceStock(10);
        assertEquals(0, p.getStock());
    }


    void testReduceStockZeroQtyThrows() {
        Product p = new Product("P010", "เกลือ", 8.0, 5);
        try {
            p.reduceStock(0);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            // ผ่าน
        }
    }

    // ---------- increaseStock ----------

    void testIncreaseStockNormal() {
        Product p = new Product("P011", "น้ำตาล", 12.0, 5);
        p.increaseStock(10);
        assertEquals(15, p.getStock());
    }

    void testIncreaseStockZeroQtyThrows() {
        Product p = new Product("P012", "น้ำตาล", 12.0, 5);
        try {
            p.increaseStock(0);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            // ผ่าน
        }
    }

    // ---------- equals / hashCode ----------

    void testEqualsSameId() {
        Product a = new Product("P013", "ชื่อ A", 10.0, 5);
        Product b = new Product("P013", "ชื่อ B", 99.0, 1);
        assertEquals(true, a.equals(b));
    }

    void testEqualsDifferentId() {
        Product a = new Product("P014", "ของเดียวกัน", 10.0, 5);
        Product b = new Product("P015", "ของเดียวกัน", 10.0, 5);
        assertEquals(false, a.equals(b));
    }

    void testHashCodeConsistentWithEquals() {
        Product a = new Product("P016", "ชื่อ A", 10.0, 5);
        Product b = new Product("P016", "ชื่อ B", 99.0, 1);
        // a กับ b ต้อง equals() เป็น true (id เดียวกัน) จึง hashCode ต้องเท่ากันด้วย
        assertEquals(a.hashCode(), b.hashCode());
    }

    // ---------- toCsvLine / fromCsvLine ----------

    void testCsvRoundTrip() {
        Product original = new Product("P017", "กาแฟ", 45.5, 30);
        String csvLine = original.toCsvLine();
        Product restored = Product.fromCsvLine(csvLine);

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getPrice(), restored.getPrice());
        assertEquals(original.getStock(), restored.getStock());
    }

    // ---------- ตัวช่วยเทส (assertEquals overload) ----------

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