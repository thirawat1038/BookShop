package model;

public class OrderLineTest {
    public static void main(String[] args) {
        OrderLineTest test = new OrderLineTest();

        test.testConstructorValid();
        test.testSubtotalCalculationWithDecimal();
        test.testConstructorNullProductThrows();
        test.testConstructorZeroQuantityThrows();
        test.testConstructorNegativeQuantityThrows();

        System.out.println("ทดสอบผ่านทั้งหมด");
    }

    void testConstructorValid() {
        Book p = new Book("P001", "น้ำดื่ม", 10.0, 50);
        OrderLine item = new OrderLine(p, 3);
        assertEquals(p, item.getProduct());
        assertEquals(3, item.getQuantity());
        assertEquals(30.0, item.getSubtotal());
    }

    void testSubtotalCalculationWithDecimal() {
        Book p = new Book("P002", "ขนมปัง", 25.5, 20);
        OrderLine item = new OrderLine(p, 4);
        assertEquals(102.0, item.getSubtotal());
    }

    void testConstructorNullProductThrows() {
        try {
            new OrderLine(null, 2);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testConstructorZeroQuantityThrows() {
        Book p = new Book("P003", "นม", 15.0, 10);
        try {
            new OrderLine(p, 0);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testConstructorNegativeQuantityThrows() {
        Book p = new Book("P004", "ไข่", 5.0, 30);
        try {
            new OrderLine(p, -1);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
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

    private void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected: " + expected + " actual is: " + actual);
        }
    }
}
