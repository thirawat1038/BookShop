package service;

// ยืนยันการซื้อ หักสต๊อก และบันทึกข้อมูล

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import model.Order;
import repository.CsvFiles;

public final class PurchaseManager {
    private final BookManager products;
    private final OrderManager orders;
    private final List<Path> files;

    public PurchaseManager(BookManager products, OrderManager orders, Path productFile, Path orderFile) {
        this.products = products;
        this.orders = orders;
        files = List.of(productFile, orderFile);
    }

    public Order checkout(String memberId, Map<String, Integer> quantities) {
        String orderId = orders.nextOrderId();
        Order order = orders.createOrder(orderId, memberId, LocalDate.now().toString(), quantities);
        try {
            return CsvFiles.transaction(files, () -> {
                products.save();
                orders.save();
                return order;
            });
        } catch (RuntimeException error) {
            orders.rollbackNewOrder(orderId);
            throw error;
        }
    }
}
