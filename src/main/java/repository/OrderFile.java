package repository;

// อ่านและบันทึกไฟล์คำสั่งซื้อ

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Book;
import model.Order;
import model.OrderLine;

public class OrderFile extends DataStore.Csv<Order> {
    private static final List<String> HEADER = List.of("orderId", "memberId", "date", "productId", "productName", "qty", "subtotal");
    private final BookFile productRepository;

    public OrderFile(String filePath, BookFile productRepository) {
        super(filePath);
        if (productRepository == null) throw new IllegalArgumentException("productRepository ห้ามเป็น null");
        this.productRepository = productRepository;
    }

    @Override protected String idOf(Order order) { return order.getOrderId(); }

    @Override public List<Order> findAll() {
        Map<String, Book> currentProducts = new LinkedHashMap<>();
        productRepository.findAll().forEach(product -> currentProducts.put(product.getId(), product));
        Map<String, Order> orders = new LinkedHashMap<>();
        for (List<String> row : CsvFiles.readRows(filePath)) {
            if (row.size() < 6 || row.size() > 7) throw new IllegalStateException("ข้อมูลออเดอร์ไม่ถูกต้องใน " + filePath);
            Order order = orders.computeIfAbsent(row.get(0), id -> new Order(id, row.get(1), row.get(2)));
            if (!order.getMemberId().equals(row.get(1)) || !order.getDate().equals(row.get(2))) {
                throw new IllegalStateException("ข้อมูลออเดอร์ " + order.getOrderId() + " ไม่ตรงกัน");
            }
            int quantity = Integer.parseInt(row.get(5));
            Book currentProduct = currentProducts.get(row.get(3));
            double subtotal;
            if (row.size() == 7 && !row.get(6).isBlank()) subtotal = Double.parseDouble(row.get(6));
            else if (currentProduct != null) subtotal = currentProduct.getPrice() * quantity;
            else throw new IllegalStateException("ออเดอร์เก่าไม่มีราคาสำหรับสินค้า " + row.get(3));
            Book snapshot = new Book(row.get(3), row.get(4), subtotal / quantity, 0);
            order.addItem(new OrderLine(snapshot, quantity, subtotal));
        }
        return new ArrayList<>(orders.values());
    }

    @Override public void saveAll(List<Order> orders) {
        if (orders == null) throw new IllegalArgumentException("orders ห้ามเป็น null");
        List<List<String>> rows = new ArrayList<>();
        for (Order order : orders) {
            for (OrderLine item : order.getItems()) {
                rows.add(List.of(order.getOrderId(), order.getMemberId(), order.getDate(),
                        item.getProduct().getId(), item.getProductName(), Integer.toString(item.getQuantity()), Double.toString(item.getSubtotal())));
            }
        }
        CsvFiles.writeRows(filePath, HEADER, rows);
    }
}
