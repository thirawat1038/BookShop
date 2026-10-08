package repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.Order;
import model.OrderItem;
import model.Product;

public class OrderRepository implements Repository<Order, String> {

    private static final String HEADER = "orderId,memberId,date,productId,productName,qty,subtotal";

    private final Path filePath;
    private final ProductRepository productRepository;

    public OrderRepository(String filePath, ProductRepository productRepository) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath ห้ามเป็น null หรือว่าง");
        }
        if (productRepository == null) {
            throw new IllegalArgumentException("productRepository ห้ามเป็น null");
        }
        this.filePath = Paths.get(filePath);
        this.productRepository = productRepository;
    }

    @Override
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return orders;
        }

        // เก็บบรรทัดดิบตามลำดับ orderId ที่เจอก่อน-หลัง เพื่อรวมหลายบรรทัดที่ orderId เดียวกัน
        Map<String, List<String[]>> rowsByOrderId = new LinkedHashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",", -1);
                if (fields.length < 6) {
                    throw new RuntimeException("บรรทัดข้อมูลออเดอร์ไม่ถูกต้อง: " + line);
                }
                rowsByOrderId.computeIfAbsent(fields[0], k -> new ArrayList<>()).add(fields);
            }
        } catch (IOException e) {
            throw new RuntimeException("อ่านไฟล์ " + filePath + " ไม่สำเร็จ: " + e.getMessage(), e);
        }

        for (Map.Entry<String, List<String[]>> entry : rowsByOrderId.entrySet()) {
            String orderId = entry.getKey();
            List<String[]> rows = entry.getValue();

            String memberId = rows.get(0)[1];
            String date = rows.get(0)[2];
            Order order = new Order(orderId, memberId, date);

            for (String[] row : rows) {
                String productId = row[3];
                int qty = Integer.parseInt(row[5]);
                Product product = productRepository.findById(productId);
                if (product == null) {
                    throw new RuntimeException("ไม่พบสินค้ารหัส " + productId
                            + " ที่อ้างอิงในออเดอร์ " + orderId);
                }
                order.addItem(new OrderItem(product, qty));
            }

            orders.add(order);
        }

        return orders;
    }

    @Override
    public void saveAll(List<Order> items) {
        if (items == null) {
            throw new IllegalArgumentException("items ห้ามเป็น null");
        }

        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                writer.write(HEADER);
                writer.newLine();
                for (Order order : items) {
                    for (OrderItem item : order.getItems()) {
                        writer.write(toCsvRow(order, item));
                        writer.newLine();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("เขียนไฟล์ " + filePath + " ไม่สำเร็จ: " + e.getMessage(), e);
        }
    }

    @Override
    public Order findById(String id) {
        if (id == null) {
            return null;
        }
        for (Order order : findAll()) {
            if (order.getOrderId().equals(id)) {
                return order;
            }
        }
        return null;
    }

    private String toCsvRow(Order order, OrderItem item) {
        Product product = item.getProduct();
        return order.getOrderId() + ","
                + order.getMemberId() + ","
                + order.getDate() + ","
                + product.getId() + ","
                + product.getName() + ","
                + item.getQuantity() + ","
                + item.getSubtotal();
    }

    @Override
    public void save(Order item) {
        if (item == null) {
            throw new IllegalArgumentException("item ห้ามเป็น null");
        }
        List<Order> items = findAll();
        boolean replaced = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getOrderId().equals(item.getOrderId())) {
                items.set(i, item);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            items.add(item);
        }
        saveAll(items);
    }

    @Override
    public boolean delete(String id) {
        if (id == null) {
            return false;
        }
        List<Order> items = findAll();
        boolean removed = items.removeIf(o -> o.getOrderId().equals(id));
        if (removed) {
            saveAll(items);
        }
        return removed;
    }
}
