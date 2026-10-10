package service;

// จัดการคำสั่งซื้อและประวัติการขาย

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import model.Book;
import model.Order;
import model.OrderLine;
import repository.OrderFile;

public class OrderManager {
    private final BookManager productService;
    private final List<Order> orders = new ArrayList<>();
    private final OrderFile repository;

    public OrderManager(BookManager productService) {
        if (productService == null) throw new IllegalArgumentException("productService ห้ามเป็น null");
        this.productService = productService;
        repository = null;
    }

    public OrderManager(BookManager productService, OrderFile repository) {
        if (productService == null || repository == null) throw new IllegalArgumentException("service และ repository ห้ามเป็น null");
        this.productService = productService;
        this.repository = repository;
        orders.addAll(repository.findAll());
        Set<String> ids = new HashSet<>();
        for (Order order : orders) {
            if (order.getItems().isEmpty() || !ids.add(order.getOrderId())) {
                throw new IllegalStateException("ข้อมูลออเดอร์ไม่ถูกต้อง: " + order.getOrderId());
            }
        }
    }

    public void save() {
        if (repository == null) throw new IllegalStateException("ยังไม่ได้กำหนดไฟล์ออเดอร์");
        repository.saveAll(orders);
    }

    public String nextOrderId() {
        BigInteger highest = orders.stream().map(Order::getOrderId)
                .filter(id -> id.matches("O\\d+"))
                .map(id -> new BigInteger(id.substring(1)))
                .max(BigInteger::compareTo).orElse(BigInteger.ZERO);
        return String.format(java.util.Locale.ROOT, "O%03d", highest.add(BigInteger.ONE));
    }

    public Order createOrder(String orderId, String memberId, String date, Map<String, Integer> quantities) {
        Order order = new Order(orderId, memberId, date);
        if (getOrderById(orderId).isPresent()) throw new IllegalArgumentException("รหัสออเดอร์ซ้ำ: " + orderId);
        List<OrderLine> items = validateItems(quantities);

        // ตรวจทุกสินค้าก่อนหักสต๊อก เพื่อไม่ให้เปลี่ยนข้อมูลบางส่วน
        for (OrderLine item : items) {
            item.getProduct().reduceStock(item.getQuantity());
            order.addItem(item);
        }
        orders.add(order);
        return order;
    }

    private List<OrderLine> validateItems(Map<String, Integer> quantities) {
        if (quantities == null || quantities.isEmpty()) throw new IllegalArgumentException("ออเดอร์ต้องมีสินค้าอย่างน้อย 1 รายการ");
        List<OrderLine> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            Integer quantity = entry.getValue();
            if (quantity == null || quantity <= 0) throw new IllegalArgumentException("จำนวนสินค้าต้องมากกว่า 0");
            Book product = productService.getProductById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("ไม่พบสินค้ารหัส " + entry.getKey()));
            if (quantity > product.getStock()) throw new IllegalStateException("สินค้า " + product.getName() + " มีไม่พอ (คงเหลือ " + product.getStock() + ")");
            items.add(new OrderLine(product, quantity));
        }
        return items;
    }

    void rollbackNewOrder(String orderId) {
        getOrderById(orderId).ifPresent(order -> {
            order.getItems().forEach(item -> item.getProduct().increaseStock(item.getQuantity()));
            orders.remove(order);
        });
    }

    public Optional<Order> getOrderById(String id) {
        return orders.stream().filter(order -> order.getOrderId().equals(id)).findFirst();
    }

    public List<Order> getAllOrders() { return Collections.unmodifiableList(orders); }

    public List<Order> getOrdersByMemberId(String memberId) {
        return orders.stream().filter(order -> order.getMemberId().equals(memberId)).toList();
    }

    public double getTotalSales() { return orders.stream().mapToDouble(Order::getTotal).sum(); }
}
