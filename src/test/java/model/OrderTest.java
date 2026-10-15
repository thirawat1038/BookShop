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

    // ---------- Testing strategy: constructor ----------
    // Partition:
    //  - ค่าปกติครบทั้ง 3 ตัว -> สร้างได้ items ว่างเปล่า
    //  - orderId เป็น null -> throw
    //  - memberId เป็น null -> throw
    //  - date เป็น null -> throw

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
            // ผ่าน
        }
    }

    void testConstructorNullMemberIdThrows() {
        try {
            new Order("O002", null, "2026-10-04");
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            // ผ่าน
        }
    }

    void testConstructorNullDateThrows() {
        try {
            new Order("O003", "M001", null);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            // ผ่าน
        }
    }

    // ---------- Testing strategy: addItem / getTotal ----------
    // Partition:
    //  - ยังไม่เพิ่มอะไรเลย -> getTotal ต้องเป็น 0
    //  - เพิ่ม 1 รายการ -> getTotal ตรงกับ subtotal ของรายการนั้น
    //  - เพิ่มหลายรายการ -> getTotal เป็นผลรวมทุกรายการ

    void testAddItemIncreasesItems() {
        Order order = new Order("O004", "M001", "2026-10-04");
        Product p = new Product("P001", "น้ำดื่ม", 10.0, 50);
        order.addItem(new OrderItem(p, 2));
        assertEquals(1, order.getItems().size());
    }

    void testGetTotalEmptyIsZero() {
        Order order = new Order("O005", "M001", "2026-10-04");
        assertEquals(0.0, order.getTotal());
    }

    void testGetTotalSingleItem() {
        Order order = new Order("O006", "M001", "2026-10-04");
        Product p = new Product("P001", "น้ำดื่ม", 10.0, 50);
        order.addItem(new OrderItem(p, 3));
        assertEquals(30.0, order.getTotal());
    }

    void testGetTotalMultipleItems() {
        Order order = new Order("O007", "M001", "2026-10-04");
        Product water = new Product("P001", "น้ำดื่ม", 10.0, 50);
        Product bread = new Product("P002", "ขนมปัง", 25.0, 20);
        order.addItem(new OrderItem(water, 2));   // 20.0
        order.addItem(new OrderItem(bread, 1));   // 25.0
        assertEquals(45.0, order.getTotal());
    }

    // ---------- Testing strategy: getItems ต้องแก้ไขไม่ได้จากภายนอก ----------

    void testGetItemsIsUnmodifiable() {
        Order order = new Order("O008", "M001", "2026-10-04");
        Product p = new Product("P001", "น้ำดื่ม", 10.0, 50);
        try {
            order.getItems().add(new OrderItem(p, 1));
            throw new AssertionError("ควร throw UnsupportedOperationException แต่ไม่ throw");
        } catch (UnsupportedOperationException e) {
            // ผ่าน: getItems() ป้องกันการแก้ไขจากภายนอกถูกต้อง
        }
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
}