package service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import model.Book;
import model.Order;

public class OrderManagerTest {
    public static void main(String[] args) {
        OrderManagerTest test = new OrderManagerTest();

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

    private BookManager newProductService() {
        BookManager ps = new BookManager();
        ps.addProduct(new Book("b01", "book01", 150.0, 50));
        ps.addProduct(new Book("b02", "book02", 165.0, 60));
        ps.addProduct(new Book("b03", "book03", 200.0, 2));
        return ps;
    }

    private Map<String, Integer> qty(String id, int n) {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put(id, n);
        return m;
    }

    void testConstructorNullProductServiceThrows() {
        try {
            new OrderManager(null);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testCreateOrderValid() {
        OrderManager os = new OrderManager(newProductService());
        Order order = os.createOrder("O01", "M01", "2026-10-04", qty("b01", 2));

        assertEquals("O01", order.getOrderId());
        assertEquals("M01", order.getMemberId());
        assertEquals("2026-10-04", order.getDate());
        assertEquals(1, order.getItems().size());
        assertEquals(300.0, order.getTotal());
        assertEquals(1, os.getAllOrders().size());
    }

    void testCreateOrderReducesStock() {
        BookManager ps = newProductService();
        OrderManager os = new OrderManager(ps);
        os.createOrder("O01", "M01", "2026-10-04", qty("b01", 5));

        assertEquals(45, ps.getProductById("b01").get().getStock());
    }

    void testCreateOrderMultipleProducts() {
        BookManager ps = newProductService();
        OrderManager os = new OrderManager(ps);

        Map<String, Integer> items = new LinkedHashMap<>();
        items.put("b01", 2);
        items.put("b02", 1);
        Order order = os.createOrder("O01", "M01", "2026-10-04", items);

        assertEquals(2, order.getItems().size());
        assertEquals(465.0, order.getTotal());
        assertEquals(48, ps.getProductById("b01").get().getStock());
        assertEquals(59, ps.getProductById("b02").get().getStock());
    }

    void testCreateOrderExactStockBoundary() {
        BookManager ps = newProductService();
        OrderManager os = new OrderManager(ps);
        os.createOrder("O01", "M01", "2026-10-04", qty("b03", 2));

        assertEquals(0, ps.getProductById("b03").get().getStock());
    }

    void testCreateOrderUnknownProductThrows() {
        OrderManager os = new OrderManager(newProductService());
        try {
            os.createOrder("O01", "M01", "2026-10-04", qty("b99", 1));
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            assertEquals(0, os.getAllOrders().size());
        }
    }

    void testCreateOrderInsufficientStockThrows() {
        BookManager ps = newProductService();
        OrderManager os = new OrderManager(ps);
        try {
            os.createOrder("O01", "M01", "2026-10-04", qty("b03", 3));
            throw new AssertionError("ควร throw IllegalStateException แต่ไม่ throw");
        } catch (IllegalStateException e) {
            assertEquals(0, os.getAllOrders().size());
            assertEquals(2, ps.getProductById("b03").get().getStock());
        }
    }

    void testCreateOrderIsAllOrNothing() {
        BookManager ps = newProductService();
        OrderManager os = new OrderManager(ps);

        Map<String, Integer> items = new LinkedHashMap<>();
        items.put("b01", 5);
        items.put("b03", 99);
        try {
            os.createOrder("O01", "M01", "2026-10-04", items);
            throw new AssertionError("ควร throw IllegalStateException แต่ไม่ throw");
        } catch (IllegalStateException e) {
            assertEquals(50, ps.getProductById("b01").get().getStock());
            assertEquals(0, os.getAllOrders().size());
        }
    }

    void testCreateOrderZeroQuantityThrows() {
        BookManager ps = newProductService();
        OrderManager os = new OrderManager(ps);
        try {
            os.createOrder("O01", "M01", "2026-10-04", qty("b01", 0));
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            assertEquals(50, ps.getProductById("b01").get().getStock());
        }
    }

    void testCreateOrderNegativeQuantityThrows() {
        BookManager ps = newProductService();
        OrderManager os = new OrderManager(ps);
        try {
            os.createOrder("O01", "M01", "2026-10-04", qty("b01", -1));
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            assertEquals(50, ps.getProductById("b01").get().getStock());
        }
    }

    void testCreateOrderEmptyItemsThrows() {
        OrderManager os = new OrderManager(newProductService());
        try {
            os.createOrder("O01", "M01", "2026-10-04", new LinkedHashMap<>());
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            assertEquals(0, os.getAllOrders().size());
        }
    }

    void testCreateOrderNullItemsThrows() {
        OrderManager os = new OrderManager(newProductService());
        try {
            os.createOrder("O01", "M01", "2026-10-04", null);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            assertEquals(0, os.getAllOrders().size());
        }
    }

    void testCreateOrderDuplicateOrderIdThrows() {
        BookManager ps = newProductService();
        OrderManager os = new OrderManager(ps);
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
        BookManager ps = newProductService();
        OrderManager os = new OrderManager(ps);
        try {
            os.createOrder("O01", null, "2026-10-04", qty("b01", 1));
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            assertEquals(50, ps.getProductById("b01").get().getStock());
            assertEquals(0, os.getAllOrders().size());
        }
    }

    void testGetOrderById() {
        OrderManager os = new OrderManager(newProductService());
        Order created = os.createOrder("O01", "M01", "2026-10-04", qty("b01", 1));

        Optional<Order> result = os.getOrderById("O01");
        assertEquals(true, result.isPresent());
        assertEquals(created.getOrderId(), result.get().getOrderId());
    }

    void testGetOrderByIdWhenNotFound() {
        OrderManager os = new OrderManager(newProductService());
        os.createOrder("O01", "M01", "2026-10-04", qty("b01", 1));

        assertEquals(true, os.getOrderById("O99").isEmpty());
    }

    void testGetAllOrdersIsUnmodifiable() {
        OrderManager os = new OrderManager(newProductService());
        try {
            os.getAllOrders().add(new Order("O01", "M01", "2026-10-04"));
            throw new AssertionError("ควร throw UnsupportedOperationException แต่ไม่ throw");
        } catch (UnsupportedOperationException e) {
        }
    }

    void testGetTotalSalesEmptyIsZero() {
        OrderManager os = new OrderManager(newProductService());
        assertEquals(0.0, os.getTotalSales());
    }

    void testGetTotalSalesMultipleOrders() {
        OrderManager os = new OrderManager(newProductService());
        os.createOrder("O01", "M01", "2026-10-04", qty("b01", 2));
        os.createOrder("O02", "M02", "2026-10-05", qty("b02", 1));
        assertEquals(465.0, os.getTotalSales());
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
