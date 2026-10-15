package service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.Member;
import model.Order;
import model.OrderItem;
import model.Product;

/**
 * AF(productService, orderService, memberService)
 * DashboardService คำนวณตัวเลขสรุปสำหรับหน้า dashboard ของ admin
 * โดยอ่านข้อมูลจาก Service ทั้งสามตัว ณ เวลาที่เรียกแต่ละเมธอด (ไม่เก็บสำเนาไว้เอง
 * จึงได้ตัวเลขล่าสุดเสมอ) และไม่แก้ไขข้อมูลใด ๆ
 *
 * RI:
 *  - productService, orderService, memberService ห้ามเป็น null
 */
public class DashboardService {

    /** สรุปยอดขายของหนังสือ 1 เล่ม: ขายได้กี่เล่ม และรายได้เท่าไร */
    public record ProductSales(String productId, String productName, int quantity, double revenue) {
    }

    /** สรุปการซื้อของสมาชิก 1 คน: จำนวนออเดอร์ และยอดซื้อรวม */
    public record MemberSales(Member member, int orderCount, double totalSpent) {
    }

    private final ProductService productService;
    private final OrderService orderService;
    private final MemberService memberService;

    /**
     * สร้าง DashboardService จาก Service ที่ใช้งานอยู่ในโปรแกรม
     *
     * @throws IllegalArgumentException ถ้าตัวใดตัวหนึ่งเป็น null
     */
    public DashboardService(ProductService productService, OrderService orderService, MemberService memberService) {
        if (productService == null || orderService == null || memberService == null) {
            throw new IllegalArgumentException("service ห้ามเป็น null");
        }
        this.productService = productService;
        this.orderService = orderService;
        this.memberService = memberService;
    }

    /** @return ยอดขายรวมของทุกออเดอร์ (0.0 ถ้ายังไม่มี) */
    public double getTotalSales() {
        return orderService.getTotalSales();
    }

    /** @return จำนวนออเดอร์ทั้งหมด */
    public int getOrderCount() {
        return orderService.getAllOrders().size();
    }

    /** @return จำนวนสมาชิกทั้งหมด */
    public int getMemberCount() {
        return memberService.getAllMembers().size();
    }

    /** @return จำนวนรายการสินค้าทั้งหมด */
    public int getProductCount() {
        return productService.getAllProducts().size();
    }

    /** @return จำนวนหนังสือที่ขายไปทั้งหมด (รวมทุกออเดอร์ ทุกเล่ม) */
    public int getTotalItemsSold() {
        int total = 0;
        for (Order order : orderService.getAllOrders()) {
            for (OrderItem item : order.getItems()) {
                total += item.getQuantity();
            }
        }
        return total;
    }

    /**
     * สินค้าทั้งหมดเรียงตามสต๊อกจากน้อยไปมาก (สต๊อกเท่ากันเรียงตามรหัสสินค้า)
     *
     * @return list ใหม่ แก้ไขได้โดยไม่กระทบข้อมูลจริง
     */
    public List<Product> getProductsByStock() {
        List<Product> sorted = new ArrayList<>(productService.getAllProducts());
        sorted.sort(Comparator.comparingInt(Product::getStock).thenComparing(Product::getId));
        return sorted;
    }

    /**
     * สินค้าที่สต๊อกน้อยกว่าหรือเท่ากับเกณฑ์ที่กำหนด (รวมสินค้าหมด) เรียงจากน้อยไปมาก
     *
     * @param threshold เกณฑ์สต๊อก ต้องไม่ติดลบ
     * @throws IllegalArgumentException ถ้า threshold ติดลบ
     */
    public List<Product> getLowStockProducts(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("threshold ต้องไม่ติดลบ");
        }
        List<Product> result = new ArrayList<>();
        for (Product product : getProductsByStock()) {
            if (product.getStock() <= threshold) {
                result.add(product);
            }
        }
        return result;
    }

    /**
     * หนังสือที่เคยขายได้ เรียงจากขายได้จำนวนเล่มมากไปน้อย (เท่ากันดูรายได้ แล้วดูรหัส)
     * รวมยอดตามรหัสสินค้า ไม่นับสินค้าที่ยังไม่เคยขาย
     */
    public List<ProductSales> getBestSellers() {
        Map<String, int[]> quantities = new LinkedHashMap<>();
        Map<String, double[]> revenues = new LinkedHashMap<>();
        Map<String, String> names = new LinkedHashMap<>();

        for (Order order : orderService.getAllOrders()) {
            for (OrderItem item : order.getItems()) {
                String id = item.getProduct().getId();
                names.put(id, item.getProduct().getName());
                quantities.computeIfAbsent(id, k -> new int[1])[0] += item.getQuantity();
                revenues.computeIfAbsent(id, k -> new double[1])[0] += item.getSubtotal();
            }
        }

        List<ProductSales> result = new ArrayList<>();
        for (String id : quantities.keySet()) {
            result.add(new ProductSales(id, names.get(id), quantities.get(id)[0], revenues.get(id)[0]));
        }
        result.sort(Comparator.comparingInt(ProductSales::quantity).reversed()
                .thenComparing(Comparator.comparingDouble(ProductSales::revenue).reversed())
                .thenComparing(ProductSales::productId));
        return result;
    }

    /**
     * สรุปการซื้อของสมาชิกทุกคน (รวมคนที่ยังไม่เคยสั่ง) เรียงตามยอดซื้อมากไปน้อย
     * ออเดอร์ที่อ้างรหัสสมาชิกซึ่งไม่มีในระบบจะไม่ถูกนับในรายการนี้
     */
    public List<MemberSales> getMemberSales() {
        List<MemberSales> result = new ArrayList<>();
        for (Member member : memberService.getAllMembers()) {
            List<Order> orders = orderService.getOrdersByMemberId(member.getId());
            double total = 0.0;
            for (Order order : orders) {
                total += order.getTotal();
            }
            result.add(new MemberSales(member, orders.size(), total));
        }
        result.sort(Comparator.comparingDouble(MemberSales::totalSpent).reversed()
                .thenComparing(ms -> ms.member().getId()));
        return result;
    }
}