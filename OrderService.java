package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import model.Order;
import model.OrderItem;
import model.Product;
import repository.OrderRepository;

/**
 * AF(productService, orders, repository)
 * OrderService จัดการการสั่งซื้อ: สร้างออเดอร์ ตัดสต๊อกสินค้า และเก็บรายการออเดอร์
 * โดย productService = ตัวจัดการสินค้าที่ใช้ค้นหาสินค้าและตัดสต๊อก
 *     orders = ออเดอร์ทั้งหมดที่สร้างสำเร็จแล้ว (อยู่ใน memory)
 *     repository = ตัวที่ใช้โหลด/บันทึกข้อมูลจริงลงไฟล์ (เป็น null ได้ ถ้าต้องการใช้แบบ in-memory
 *                  ล้วน ๆ เช่นตอนเทส ซึ่งจะเรียก save() ไม่ได้)
 * RI:
 *  - productService ห้ามเป็น null
 *  - orders ห้ามเป็น null และไม่มีสมาชิกที่เป็น null
 *  - ไม่มี Order 2 ใบที่ orderId ซ้ำกัน
 *  - ทุก Order ใน orders ต้องมีสินค้าอย่างน้อย 1 รายการ
 */
public class OrderService {
    private final ProductService productService;
    private final List<Order> orders = new ArrayList<>();
    private final OrderRepository repository;

    private void checkRep() {
        if (productService == null) {
            throw new RuntimeException("RI violated: productService คือ null");
        }
        if (orders == null) {
            throw new RuntimeException("RI violated: orders คือ null");
        }
        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            if (o == null) {
                throw new RuntimeException("RI violated: มีออเดอร์เป็น null ที่ตำแหน่ง " + i);
            }
            if (o.getItems().isEmpty()) {
                throw new RuntimeException("RI violated: ออเดอร์ " + o.getOrderId() + " ไม่มีสินค้า");
            }
            for (int j = i + 1; j < orders.size(); j++) {
                if (o.getOrderId().equals(orders.get(j).getOrderId())) {
                    throw new RuntimeException("RI violated: orderId ซ้ำกัน (" + o.getOrderId() + ")");
                }
            }
        }
    }

    /**
     productService ตัวจัดการสินค้าที่จะใช้ ห้ามเป็น null
     IllegalArgumentException ถ้า productService เป็น null
     */
    public OrderService(ProductService productService) {
        if (productService == null) {
            throw new IllegalArgumentException("productService ห้ามเป็น null");
        }
        this.productService = productService;
        this.repository = null;
        checkRep();
    }

    /**
     * สร้าง OrderService ที่ผูกกับ OrderRepository และโหลดออเดอร์ทั้งหมดจากไฟล์ทันที
     * หมายเหตุ: การโหลดออเดอร์เก่ากลับมาจะ "ไม่" ตัดสต๊อกสินค้าซ้ำ เพราะถือว่าสต๊อกที่โหลดมา
     * จาก ProductRepository นั้นเป็นค่าล่าสุดที่ถูกตัดไปแล้วตั้งแต่ตอนบันทึกครั้งก่อนอยู่แล้ว
     *
     * @param productService ตัวจัดการสินค้าที่จะใช้ ห้ามเป็น null
     * @param repository     repository ที่จะใช้โหลด/บันทึกข้อมูลออเดอร์ ห้ามเป็น null
     * @throws IllegalArgumentException ถ้า productService หรือ repository เป็น null
     */
    public OrderService(ProductService productService, OrderRepository repository) {
        if (productService == null) {
            throw new IllegalArgumentException("productService ห้ามเป็น null");
        }
        if (repository == null) {
            throw new IllegalArgumentException("repository ห้ามเป็น null");
        }
        this.productService = productService;
        this.repository = repository;
        orders.addAll(repository.findAll());
        checkRep();
    }

    /**
     * บันทึกออเดอร์ทั้งหมดตอนนี้กลับลงไฟล์ ผ่าน repository ที่ผูกไว้
     *
     * @throws IllegalStateException ถ้า OrderService นี้สร้างแบบ in-memory (ไม่มี repository)
     */
    public void save() {
        if (repository == null) {
            throw new IllegalStateException("OrderService นี้ไม่ได้ผูกกับ repository จึงบันทึกไม่ได้");
        }
        repository.saveAll(orders);
    }

    /**
     * สร้างออเดอร์ใหม่ และตัดสต๊อกสินค้าตามจำนวนที่สั่ง
     * ทำงานแบบ "สำเร็จทั้งหมดหรือไม่ทำเลย": ถ้ามีรายการใดมีปัญหา จะไม่ตัดสต๊อกและไม่บันทึกออเดอร์
     *
      orderId     รหัสออเดอร์ ห้ามเป็น null และห้ามซ้ำกับที่มีอยู่
      memberId    รหัสสมาชิกที่สั่งซื้อ ห้ามเป็น null
      date        วันที่สั่งซื้อ ห้ามเป็น null
      productQuantities Map ของ รหัสสินค้า -> จำนวนที่สั่ง (ต้องไม่ว่าง และจำนวนต้องมากกว่า 0)
     ออเดอร์ที่สร้างสำเร็จ
      IllegalArgumentException ถ้าข้อมูลไม่ถูกต้อง, orderId ซ้ำ, ไม่พบสินค้า หรือจำนวน <= 0
      IllegalStateException    ถ้าสต๊อกสินค้าชิ้นใดไม่พอ
     */
    public Order createOrder(String orderId, String memberId, String date,
                             Map<String, Integer> productQuantities) {
        Order order = new Order(orderId, memberId, date);

        if (getOrderById(orderId).isPresent()) {
            throw new IllegalArgumentException("มีออเดอร์รหัส " + orderId + " อยู่แล้ว");
        }
        if (productQuantities == null || productQuantities.isEmpty()) {
            throw new IllegalArgumentException("ออเดอร์ต้องมีสินค้าอย่างน้อย 1 รายการ");
        }

        // ตรวจสอบทุกรายการก่อน ยังไม่แตะสต๊อก
        List<OrderItem> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            Integer qty = entry.getValue();
            if (qty == null || qty <= 0) {
                throw new IllegalArgumentException("จำนวนของสินค้า " + entry.getKey() + " ต้องมากกว่า 0");
            }
            Product product = productService.getProductById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("ไม่พบสินค้ารหัส " + entry.getKey()));
            if (product.getStock() < qty) {
                throw new IllegalStateException("สินค้ารหัส " + product.getId() + " มีไม่พอ: ต้องการ "
                        + qty + " แต่มีอยู่ " + product.getStock());
            }
            items.add(new OrderItem(product, qty));
        }

        // ผ่านทุกรายการแล้ว จึงค่อยตัดสต๊อกและบันทึกออเดอร์
        for (OrderItem item : items) {
            order.addItem(item);
            item.getProduct().reduceStock(item.getQuantity());
        }
        orders.add(order);
        checkRep();
        return order;
    }

    /**
      orderId รหัสออเดอร์ที่ต้องการค้นหา
      Optional ที่มีออเดอร์นั้น หรือ Optional.empty() ถ้าไม่เจอ
     */
    public Optional<Order> getOrderById(String orderId) {
        for (Order order : orders) {
            if (order.getOrderId().equals(orderId)) {
                return Optional.of(order);
            }
        }
        return Optional.empty();
    }

    /**
      ออเดอร์ทั้งหมด (แก้ไขจากภายนอกไม่ได้)
     */
    public List<Order> getAllOrders() {
        return Collections.unmodifiableList(orders);
    }

    /**
      ยอดขายรวมของทุกออเดอร์ ถ้ายังไม่มีออเดอร์คืน 0.0
     */
    public double getTotalSales() {
        double total = 0.0;
        for (Order order : orders) {
            total += order.getTotal();
        }
        return total;
    }
}