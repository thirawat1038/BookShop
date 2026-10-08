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

    public OrderService(ProductService productService) {
        if (productService == null) {
            throw new IllegalArgumentException("productService ห้ามเป็น null");
        }
        this.productService = productService;
        this.repository = null;
        checkRep();
    }

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

    public void save() {
        if (repository == null) {
            throw new IllegalStateException("OrderService นี้ไม่ได้ผูกกับ repository จึงบันทึกไม่ได้");
        }
        repository.saveAll(orders);
    }

    public Order createOrder(String orderId, String memberId, String date,
                             Map<String, Integer> productQuantities) {
        Order order = new Order(orderId, memberId, date);

        if (getOrderById(orderId).isPresent()) {
            throw new IllegalArgumentException("มีออเดอร์รหัส " + orderId + " อยู่แล้ว");
        }
        if (productQuantities == null || productQuantities.isEmpty()) {
            throw new IllegalArgumentException("ออเดอร์ต้องมีสินค้าอย่างน้อย 1 รายการ");
        }

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

        for (OrderItem item : items) {
            order.addItem(item);
            item.getProduct().reduceStock(item.getQuantity());
        }
        orders.add(order);
        checkRep();
        return order;
    }

    public Optional<Order> getOrderById(String orderId) {
        for (Order order : orders) {
            if (order.getOrderId().equals(orderId)) {
                return Optional.of(order);
            }
        }
        return Optional.empty();
    }

    public List<Order> getAllOrders() {
        return Collections.unmodifiableList(orders);
    }

    public List<Order> getOrdersByMemberId(String memberId) {
        List<Order> result = new ArrayList<>();
        if (memberId == null) {
            return Collections.unmodifiableList(result);
        }
        for (Order order : orders) {
            if (memberId.equals(order.getMemberId())) {
                result.add(order);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public double getTotalSales() {
        double total = 0.0;
        for (Order order : orders) {
            total += order.getTotal();
        }
        return total;
    }
}