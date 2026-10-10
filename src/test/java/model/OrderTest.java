package model;

public class OrderTest {
    public static void main(String[] args) {
        OrderTest test = new OrderTest();

        test.testConstructorValid();
        test.testConstructorNullOrderIdThrows();
        test.testConstructorNullMemberIdThrows();
        test.testConstructorNullDateThrows();

        test.testAddItemIncreasesItems();
        test.testGetTotalEmptyIsZero();
        test.testGetTotalSingleItem();
        test.testGetTotalMultipleItems();
        test.testGetItemsIsUnmodifiable();

        System.out.println("ทดสอบผ่านทั้งหมด");
    }

    void testConstructorValid() {
        Order order = new Order("O001", "M001", "2026-10-04");
        assertEquals("O001", order.getOrderId());
        assertEquals("M001", order.getMemberId());
        assertEquals("2026-10-04", order.getDate());
        assertEquals(0, order.getItems().size());
    }

    void testConstructorNullOrderIdThrows() {
        try {
            new Order(null, "M001", "2026-10-04");
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testConstructorNullMemberIdThrows() {
        try {
            new Order("O002", null, "2026-10-04");
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testConstructorNullDateThrows() {
        try {
            new Order("O003", "M001", null);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testAddItemIncreasesItems() {
        Order order = new Order("O004", "M001", "2026-10-04");
        Book p = new Book("P001", "น้ำดื่ม", 10.0, 50);
        order.addItem(new OrderLine(p, 2));
        assertEquals(1, order.getItems().size());
    }

    void testGetTotalEmptyIsZero() {
        Order order = new Order("O005", "M001", "2026-10-04");
        assertEquals(0.0, order.getTotal());
    }

    void testGetTotalSingleItem() {
        Order order = new Order("O006", "M001", "2026-10-04");
        Book p = new Book("P001", "น้ำดื่ม", 10.0, 50);
        order.addItem(new OrderLine(p, 3));
        assertEquals(30.0, order.getTotal());
    }

    void testGetTotalMultipleItems() {
        Order order = new Order("O007", "M001", "2026-10-04");
        Book water = new Book("P001", "น้ำดื่ม", 10.0, 50);
        Book bread = new Book("P002", "ขนมปัง", 25.0, 20);
        order.addItem(new OrderLine(water, 2));
        order.addItem(new OrderLine(bread, 1));
        assertEquals(45.0, order.getTotal());
    }

    void testGetItemsIsUnmodifiable() {
        Order order = new Order("O008", "M001", "2026-10-04");
        Book p = new Book("P001", "น้ำดื่ม", 10.0, 50);
        try {
            order.getItems().add(new OrderLine(p, 1));
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
}
