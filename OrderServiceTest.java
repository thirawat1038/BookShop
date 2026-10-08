package service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import model.Order;
import model.Product;

public class OrderServiceTest {

    public static void main(String[] args) {
        OrderServiceTest test = new OrderServiceTest();

        test.testConstructorNullProductServiceThrows();

        test.testCreateOrderValid();
        test.testCreateOrderReducesStock();
        test.testCreateOrderMultipleProducts();
        test.testCreateOrderExactStockBoundary();
        test.testCreateOrderUnknownProductThrows();
        test.testCreateOrderInsufficientStockThrows();
        test.testCreateOrderIsAllOrNothing();
        test.testCreateOrderZeroQuantityThrows();
        test.testCreateOrderNegativeQuantityThrows();
        test.testCreateOrderEmptyItemsThrows();
        test.testCreateOrderNullItemsThrows();
        test.testCreateOrderDuplicateOrderIdThrows();
        test.testCreateOrderNullMemberIdDoesNotReduceStock();

        test.testGetOrderById();
        test.testGetOrderByIdWhenNotFound();
        test.testGetAllOrdersIsUnmodifiable();

        test.testGetTotalSalesEmptyIsZero();
        test.testGetTotalSalesMultipleOrders();

        System.out.println("ทดสอบผ่านทั้งหมด");
    }

    // ---------- ตัวช่วยสร้างข้อมูลทดสอบ ----------

    private ProductService newProductService() {
        ProductService ps = new ProductService();
        ps.addProduct(new Product("b01", "book01", 150.0, 50));
        ps.addProduct(new Product("b02", "book02", 165.0, 60));
        ps.addProduct(new Product("b03", "book03", 200.0, 2));
        return ps;
    }

    private Map<String, Integer> qty(String id, int n) {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put(id, n);
        return m;
    }

    // ---------- Testing strategy: constructor ----------
    // Partition:
    //  - productService เป็น null -> throw

    void testConstructorNullProductServiceThrows() {
        try {
            new OrderService(null);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            // ผ่าน
        }
    }

    // ---------- Testing strategy: createOrder (กรณีสำเร็จ) ----------
    // Partition:
    //  - สั่งสินค้า 1 รายการ -> ได้ Order ที่ถูกต้อง และบันทึกไว้
    //  - สั่งแล้ว stock ต้องลดลงตามจำนวน
    //  - สั่งหลายรายการ -> total เป็นผลรวม และตัดสต๊อกทุกชิ้น
    //  - สั่งเท่ากับ stock ที่มีพอดี (boundary) -> สำเร็จ stock เหลือ 0

    void testCreateOrderValid() {
        OrderService os = new OrderService(newProductService());
        Order order = os.createOrder("O01", "M01", "2026-10-04", qty("b01", 2));

        assertEquals("O01", order.getOrderId());
        assertEquals("M01", order.getMemberId());
        assertEquals("2026-10-04", order.getDate());
        assertEquals(1, order.getItems().size());
        assertEquals(300.0, order.getTotal());
        assertEquals(1, os.getAllOrders().size());
    }

    void testCreateOrderReducesStock() {
        ProductService ps = newProductService();
        OrderService os = new OrderService(ps);
        os.createOrder("O01", "M01", "2026-10-04", qty("b01", 5));

        assertEquals(45, ps.getProductById("b01").get().getStock());
    }

    void testCreateOrderMultipleProducts() {
        ProductService ps = newProductService();
        OrderService os = new OrderService(ps);

        Map<String, Integer> items = new LinkedHashMap<>();
        items.put("b01", 2);   // 300.0
        items.put("b02", 1);   // 165.0
        Order order = os.createOrder("O01", "M01", "2026-10-04", items);

        assertEquals(2, order.getItems().size());
        assertEquals(465.0, order.getTotal());
        assertEquals(48, ps.getProductById("b01").get().getStock());
        assertEquals(59, ps.getProductById("b02").get().getStock());
    }

    void testCreateOrderExactStockBoundary() {
        ProductService ps = newProductService();
        OrderService os = new OrderService(ps);
        os.createOrder("O01", "M01", "2026-10-04", qty("b03", 2));   // b03 มีอยู่ 2 พอดี

        assertEquals(0, ps.getProductById("b03").get().getStock());
    }

    // ---------- Testing strategy: createOrder (กรณีผิดพลาด) ----------
    // Partition:
    //  - รหัสไม่มีอยู่จริง -> throw IllegalArgumentException
    //  - สต๊อกไม่พอ -> throw IllegalStateException และไม่บันทึกออเดอร์/ไม่ตัดสต๊อก
    //  - หลายรายการ แต่รายการหลังมีปัญหา -> ต้องไม่ตัดสต๊อกรายการแรก (all-or-nothing)
    //  - จำนวน 0 / ติดลบ -> throw
    //  - Map ว่าง / null -> throw
    //  - orderId ซ้ำ -> throw
    //  - memberId เป็น null -> throw และไม่ตัดสต๊อก

    void testCreateOrderUnknownProductThrows() {
        OrderService os = new OrderService(newProductService());
        try {
            os.createOrder("O01", "M01", "2026-10-04", qty("b99", 1));
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            assertEquals(0, os.getAllOrders().size());
        }
    }

    void testCreateOrderInsufficientStockThrows() {
        ProductService ps = newProductService();
        OrderService os = new OrderService(ps);
        try {
            os.createOrder("O01", "M01", "2026-10-04", qty("b03", 3));   // b03 มีแค่ 2
            throw new AssertionError("ควร throw IllegalStateException แต่ไม่ throw");
        } catch (IllegalStateException e) {
            assertEquals(0, os.getAllOrders().size());
            assertEquals(2, ps.getProductById("b03").get().getStock());
        }
    }

    void testCreateOrderIsAllOrNothing() {
        ProductService ps = newProductService();
        OrderService os = new OrderService(ps);

        Map<String, Integer> items = new LinkedHashMap<>();
        items.put("b01", 5);    // ปกติ
        items.put("b03", 99);   // เกินสต๊อก
        try {
            os.createOrder("O01", "M01", "2026-10-04", items);
            throw new AssertionError("ควร throw IllegalStateException แต่ไม่ throw");
        } catch (IllegalStateException e) {
            // b01 ต้องไม่ถูกตัดสต๊อก ทั้งที่รายการของมันปกติ
            assertEquals(50, ps.getProductById("b01").get().getStock());
            assertEquals(0, os.getAllOrders().size());
        }
    }

    void testCreateOrderZeroQuantityThrows() {
        ProductService ps = newProductService();
        OrderService os = new OrderService(ps);
        try {
            os.createOrder("O01", "M01", "2026-10-04", qty("b01", 0));
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            assertEquals(50, ps.getProductById("b01").get().getStock());
        }
    }

    void testCreateOrderNegativeQuantityThrows() {
        ProductService ps = newProductService();
        OrderService os = new OrderService(ps);
        try {
            os.createOrder("O01", "M01", "2026-10-04", qty("b01", -1));
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            assertEquals(50, ps.getProductById("b01").get().getStock());
        }
    }

    void testCreateOrderEmptyItemsThrows() {
        OrderService os = new OrderService(newProductService());
        try {
            os.createOrder("O01", "M01", "2026-10-04", new LinkedHashMap<>());
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            assertEquals(0, os.getAllOrders().size());
        }
    }

    void testCreateOrderNullItemsThrows() {
        OrderService os = new OrderService(newProductService());
        try {
            os.createOrder("O01", "M01", "2026-10-04", null);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            assertEquals(0, os.getAllOrders().size());
        }
    }

    void testCreateOrderDuplicateOrderIdThrows() {
        ProductService ps = newProductService();
        OrderService os = new OrderService(ps);
        os.createOrder("O01", "M01", "2026-10-04", qty("b01", 1));
        try {
            os.createOrder("O01", "M02", "2026-10-05", qty("b01", 1));
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            assertEquals(1, os.getAllOrders().size());
            assertEquals(49, ps.getProductById("b01").get().getStock());
        }
    }

    void testCreateOrderNullMemberIdDoesNotReduceStock() {
        ProductService ps = newProductService();
        OrderService os = new OrderService(ps);
        try {
            os.createOrder("O01", null, "2026-10-04", qty("b01", 1));
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            assertEquals(50, ps.getProductById("b01").get().getStock());
            assertEquals(0, os.getAllOrders().size());
        }
    }

    // ---------- Testing strategy: getOrderById ----------
    // Partition:
    //  - id ที่มีอยู่ -> เจอออเดอร์นั้น
    //  - id ที่ไม่มีอยู่ -> Optional.empty()

    void testGetOrderById() {
        OrderService os = new OrderService(newProductService());
        Order created = os.createOrder("O01", "M01", "2026-10-04", qty("b01", 1));

        Optional<Order> result = os.getOrderById("O01");
        assertEquals(true, result.isPresent());
        assertEquals(created.getOrderId(), result.get().getOrderId());
    }

    void testGetOrderByIdWhenNotFound() {
        OrderService os = new OrderService(newProductService());
        os.createOrder("O01", "M01", "2026-10-04", qty("b01", 1));

        assertEquals(true, os.getOrderById("O99").isEmpty());
    }

    // ---------- Testing strategy: getAllOrders ต้องแก้ไขไม่ได้จากภายนอก ----------

    void testGetAllOrdersIsUnmodifiable() {
        OrderService os = new OrderService(newProductService());
        try {
            os.getAllOrders().add(new Order("O01", "M01", "2026-10-04"));
            throw new AssertionError("ควร throw UnsupportedOperationException แต่ไม่ throw");
        } catch (UnsupportedOperationException e) {
            // ผ่าน
        }
    }

    // ---------- Testing strategy: getTotalSales ----------
    // Partition:
    //  - ยังไม่มีออเดอร์ -> 0.0
    //  - มีหลายออเดอร์ -> ผลรวม total ของทุกออเดอร์

    void testGetTotalSalesEmptyIsZero() {
        OrderService os = new OrderService(newProductService());
        assertEquals(0.0, os.getTotalSales());
    }

    void testGetTotalSalesMultipleOrders() {
        OrderService os = new OrderService(newProductService());
        os.createOrder("O01", "M01", "2026-10-04", qty("b01", 2));   // 300.0
        os.createOrder("O02", "M02", "2026-10-05", qty("b02", 1));   // 165.0
        assertEquals(465.0, os.getTotalSales());
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