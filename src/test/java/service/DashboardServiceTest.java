package service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.Product;
import service.DashboardService.MemberSales;
import service.DashboardService.ProductSales;

public class DashboardServiceTest {

    public static void main(String[] args) {
        DashboardServiceTest test = new DashboardServiceTest();

        test.testConstructorNullThrows();

        test.testEmptyShopHasZeroEverything();

        test.testTotals();
        test.testProductsByStockSortedAscending();
        test.testProductsByStockDoesNotAffectRealData();
        test.testLowStockIncludesBoundaryAndOutOfStock();
        test.testLowStockZeroThresholdOnlyOutOfStock();
        test.testLowStockNegativeThresholdThrows();
        test.testBestSellersSortedAndAggregated();
        test.testBestSellersExcludeUnsoldProducts();
        test.testMemberSalesIncludesMembersWithNoOrders();

        System.out.println("ทดสอบผ่านทั้งหมด");
    }

    // ---------- ข้อมูลทดสอบ (คำนวณมือไว้ให้ตรวจง่าย) ----------
    // สินค้า: b01 150 x50, b02 165 x60, b03 200 x2, b04 100 x0
    // สมาชิก: m01 alice01, m02 bobby02, m03 carol03 (ไม่เคยสั่ง)
    // O01 m01: b01 x2 (300) + b03 x1 (200) = 500
    // O02 m02: b01 x1 (150) + b02 x3 (495) = 645
    // O03 m01: b02 x1 (165)               = 165
    // รวม: ยอดขาย 1310, ออเดอร์ 3, ขายไป 8 เล่ม
    //  b01 ขาย 3 เล่ม 450 | b02 ขาย 4 เล่ม 660 | b03 ขาย 1 เล่ม 200
    // สต๊อกหลังขาย: b01 47, b02 56, b03 1, b04 0

    private ProductService products;
    private OrderService orders;
    private MemberService members;

    private DashboardService setUp() {
        products = new ProductService();
        products.addProduct(new Product("b01", "book01", 150.0, 50));
        products.addProduct(new Product("b02", "book02", 165.0, 60));
        products.addProduct(new Product("b03", "book03", 200.0, 2));
        products.addProduct(new Product("b04", "book04", 100.0, 0));

        members = new MemberService();
        members.register("m01", "alice01", "pw1234", "pw1234", "Bangkok", "0811111111");
        members.register("m02", "bobby02", "pw5678", "pw5678", "Nonthaburi", "0822222222");
        members.register("m03", "carol03", "pw0000", "pw0000", "Phuket", "0833333333");

        orders = new OrderService(products);
        Map<String, Integer> o1 = new LinkedHashMap<>();
        o1.put("b01", 2);
        o1.put("b03", 1);
        orders.createOrder("O01", "m01", "2026-10-01", o1);

        Map<String, Integer> o2 = new LinkedHashMap<>();
        o2.put("b01", 1);
        o2.put("b02", 3);
        orders.createOrder("O02", "m02", "2026-10-02", o2);

        Map<String, Integer> o3 = new LinkedHashMap<>();
        o3.put("b02", 1);
        orders.createOrder("O03", "m01", "2026-10-03", o3);

        return new DashboardService(products, orders, members);
    }

    // ---------- constructor ----------

    void testConstructorNullThrows() {
        ProductService p = new ProductService();
        OrderService o = new OrderService(p);
        MemberService m = new MemberService();
        assertThrows(() -> new DashboardService(null, o, m));
        assertThrows(() -> new DashboardService(p, null, m));
        assertThrows(() -> new DashboardService(p, o, null));
    }

    // ---------- ร้านว่าง ----------

    void testEmptyShopHasZeroEverything() {
        ProductService p = new ProductService();
        DashboardService d = new DashboardService(p, new OrderService(p), new MemberService());
        assertEquals(0.0, d.getTotalSales());
        assertEquals(0, d.getOrderCount());
        assertEquals(0, d.getMemberCount());
        assertEquals(0, d.getProductCount());
        assertEquals(0, d.getTotalItemsSold());
        assertEquals(0, d.getBestSellers().size());
        assertEquals(0, d.getMemberSales().size());
        assertEquals(0, d.getLowStockProducts(10).size());
    }

    // ---------- ตัวเลขรวม ----------

    void testTotals() {
        DashboardService d = setUp();
        assertEquals(1310.0, d.getTotalSales());
        assertEquals(3, d.getOrderCount());
        assertEquals(3, d.getMemberCount());
        assertEquals(4, d.getProductCount());
        assertEquals(8, d.getTotalItemsSold());
    }

    // ---------- สต๊อก ----------

    void testProductsByStockSortedAscending() {
        List<Product> sorted = setUp().getProductsByStock();
        assertEquals("b04", sorted.get(0).getId()); // 0
        assertEquals("b03", sorted.get(1).getId()); // 1
        assertEquals("b01", sorted.get(2).getId()); // 47
        assertEquals("b02", sorted.get(3).getId()); // 56
    }

    void testProductsByStockDoesNotAffectRealData() {
        DashboardService d = setUp();
        d.getProductsByStock().clear();
        assertEquals(4, products.getAllProducts().size());
    }

    void testLowStockIncludesBoundaryAndOutOfStock() {
        List<Product> low = setUp().getLowStockProducts(1); // b03 มี 1 พอดีเกณฑ์, b04 หมด
        assertEquals(2, low.size());
        assertEquals("b04", low.get(0).getId());
        assertEquals("b03", low.get(1).getId());
    }

    void testLowStockZeroThresholdOnlyOutOfStock() {
        List<Product> low = setUp().getLowStockProducts(0);
        assertEquals(1, low.size());
        assertEquals("b04", low.get(0).getId());
    }

    void testLowStockNegativeThresholdThrows() {
        DashboardService d = setUp();
        assertThrows(() -> d.getLowStockProducts(-1));
    }

    // ---------- สินค้าขายดี ----------

    void testBestSellersSortedAndAggregated() {
        List<ProductSales> best = setUp().getBestSellers();
        assertEquals(3, best.size());

        assertEquals("b02", best.get(0).productId());
        assertEquals(4, best.get(0).quantity());
        assertEquals(660.0, best.get(0).revenue());

        assertEquals("b01", best.get(1).productId()); // รวมจาก O01 และ O02
        assertEquals(3, best.get(1).quantity());
        assertEquals(450.0, best.get(1).revenue());

        assertEquals("b03", best.get(2).productId());
        assertEquals(1, best.get(2).quantity());
        assertEquals("book03", best.get(2).productName());
    }

    void testBestSellersExcludeUnsoldProducts() {
        for (ProductSales ps : setUp().getBestSellers()) {
            if (ps.productId().equals("b04")) {
                throw new AssertionError("b04 ยังไม่เคยขาย ไม่ควรอยู่ในรายการ");
            }
        }
    }

    // ---------- สมาชิก ----------

    void testMemberSalesIncludesMembersWithNoOrders() {
        List<MemberSales> ms = setUp().getMemberSales();
        assertEquals(3, ms.size());

        assertEquals("m01", ms.get(0).member().getId()); // 500 + 165
        assertEquals(2, ms.get(0).orderCount());
        assertEquals(665.0, ms.get(0).totalSpent());

        assertEquals("m02", ms.get(1).member().getId());
        assertEquals(1, ms.get(1).orderCount());
        assertEquals(645.0, ms.get(1).totalSpent());

        assertEquals("m03", ms.get(2).member().getId());
        assertEquals(0, ms.get(2).orderCount());
        assertEquals(0.0, ms.get(2).totalSpent());
    }

    // ---------- ตัวช่วยเทส ----------

    private static void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected: " + expected + " actual is: " + actual);
        }
    }

    private static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Expected: " + expected + " actual is: " + actual);
        }
    }

    private static void assertEquals(double expected, double actual) {
        if (Math.abs(expected - actual) > 0.0001) {
            throw new AssertionError("Expected: " + expected + " actual is: " + actual);
        }
    }

    private static void assertThrows(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("ควร throw IllegalArgumentException");
    }
}