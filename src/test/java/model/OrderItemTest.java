package model;

public class OrderItemTest {

    public static void main(String[] args) {
        OrderItemTest test = new OrderItemTest();

        test.testConstructorValid();
        test.testSubtotalCalculationWithDecimal();
        test.testConstructorNullProductThrows();
        test.testConstructorZeroQuantityThrows();
        test.testConstructorNegativeQuantityThrows();

        System.out.println("ทดสอบผ่านทั้งหมด");
    }

    // ---------- Testing strategy ----------
    // Partition:
    //  - product ปกติ, quantity ปกติ (คำนวณ subtotal ถูกไหม)
    //  - price เป็นทศนิยม (เช็คการคูณไม่คลาดเคลื่อน)
    //  - product เป็น null -> throw
    //  - quantity เป็น 0 -> throw
    //  - quantity ติดลบ -> throw

    void testConstructorValid() {
        Product p = new Product("P001", "น้ำดื่ม", 10.0, 50);
        OrderItem item = new OrderItem(p, 3);
        assertEquals(p, item.getProduct());
        assertEquals(3, item.getQuantity());
        assertEquals(30.0, item.getSubtotal());
    }

    void testSubtotalCalculationWithDecimal() {
        Product p = new Product("P002", "ขนมปัง", 25.5, 20);
        OrderItem item = new OrderItem(p, 4);
        assertEquals(102.0, item.getSubtotal());
    }

    void testConstructorNullProductThrows() {
        try {
            new OrderItem(null, 2);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            // ผ่าน
        }
    }

    void testConstructorZeroQuantityThrows() {
        Product p = new Product("P003", "นม", 15.0, 10);
        try {
            new OrderItem(p, 0);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            // ผ่าน
        }
    }

    void testConstructorNegativeQuantityThrows() {
        Product p = new Product("P004", "ไข่", 5.0, 30);
        try {
            new OrderItem(p, -1);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            // ผ่าน
        }
    }

    // ---------- ตัวช่วยเทส (assertEquals overload) ----------

   

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

    private void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected: " + expected + " actual is: " + actual);
        }
    }
}