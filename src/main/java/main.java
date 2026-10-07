import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import model.Order;
import model.OrderItem;
import model.Product;
import repository.OrderRepository;
import repository.ProductRepository;
import service.OrderService;
import service.ProductService;

/**
 * จุดเริ่มต้นของโปรแกรม BookShop แบบเมนูคอนโซล
 * ใช้สาธิตว่าทุกชั้น (Model - Repository - Service) ทำงานร่วมกันได้จริง
 * ข้อมูลสินค้าอ่านจาก data/Products.csv และข้อมูลออเดอร์อ่านจาก data/Orders.csv
 * (รันโปรแกรมจาก root ของโปรเจกต์ ไม่งั้นจะหาไฟล์ data/ ไม่เจอ)
 */
public class main {

    private static final String PRODUCT_FILE = "data/Products.csv";
    private static final String ORDER_FILE = "data/Orders.csv";

    public static void main(String[] args) {
        ProductRepository productRepository = new ProductRepository(PRODUCT_FILE);
        ProductService productService = new ProductService(productRepository);

        OrderRepository orderRepository = new OrderRepository(ORDER_FILE, productRepository);
        OrderService orderService = new OrderService(productService, orderRepository);

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("=== ยินดีต้อนรับสู่ BookShop ===");

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    showProducts(productService);
                    break;
                case "2":
                    placeOrder(scanner, productService, orderService);
                    break;
                case "3":
                    showOrders(orderService);
                    break;
                case "4":
                    running = false;
                    System.out.println("ออกจากโปรแกรม... ขอบคุณที่ใช้บริการ BookShop");
                    break;
                default:
                    System.out.println("กรุณาเลือกเมนู 1-4 เท่านั้น");
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("---- เมนู ----");
        System.out.println("1. แสดงรายการหนังสือทั้งหมด");
        System.out.println("2. สั่งซื้อหนังสือ");
        System.out.println("3. ดูประวัติคำสั่งซื้อ");
        System.out.println("4. ออกจากโปรแกรม");
        System.out.print("เลือกเมนู: ");
    }

    private static void showProducts(ProductService productService) {
        System.out.println();
        System.out.println("---- รายการหนังสือ ----");
        if (productService.getAllProducts().isEmpty()) {
            System.out.println("ยังไม่มีสินค้าในระบบ");
            return;
        }
        for (Product product : productService.getAllProducts()) {
            System.out.printf("[%s] %-20s ราคา %.2f บาท  คงเหลือ %d เล่ม%n",
                    product.getId(), product.getName(), product.getPrice(), product.getStock());
        }
    }

    private static void placeOrder(Scanner scanner, ProductService productService, OrderService orderService) {
        showProducts(productService);
        if (productService.getAllProducts().isEmpty()) {
            return;
        }

        System.out.print("กรอกรหัสสมาชิก (memberId): ");
        String memberId = scanner.nextLine().trim();
        if (memberId.isEmpty()) {
            System.out.println("ยกเลิก: ต้องกรอกรหัสสมาชิก");
            return;
        }

        Map<String, Integer> productQuantities = new LinkedHashMap<>();
        boolean addingItems = true;
        while (addingItems) {
            System.out.print("กรอกรหัสหนังสือที่ต้องการสั่งซื้อ (เว้นว่างเพื่อจบรายการ): ");
            String productId = scanner.nextLine().trim();
            if (productId.isEmpty()) {
                addingItems = false;
                continue;
            }
            if (productService.getProductById(productId).isEmpty()) {
                System.out.println("ไม่พบหนังสือรหัส " + productId);
                continue;
            }
            System.out.print("จำนวน: ");
            int qty;
            try {
                qty = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("จำนวนไม่ถูกต้อง");
                continue;
            }
            productQuantities.merge(productId, qty, Integer::sum);
            System.out.println("เพิ่มลงตะกร้าแล้ว");
        }

        if (productQuantities.isEmpty()) {
            System.out.println("ยกเลิก: ไม่มีสินค้าในตะกร้า");
            return;
        }

        String orderId = "O" + System.currentTimeMillis();
        String date = LocalDate.now().toString();

        try {
            Order order = orderService.createOrder(orderId, memberId, date, productQuantities);
            productService.save();
            orderService.save();

            System.out.println();
            System.out.println("สั่งซื้อสำเร็จ! เลขที่ออเดอร์: " + order.getOrderId());
            for (OrderItem item : order.getItems()) {
                System.out.printf("  - %s x%d = %.2f บาท%n",
                        item.getProduct().getName(), item.getQuantity(), item.getSubtotal());
            }
            System.out.printf("ยอดรวมทั้งหมด: %.2f บาท%n", order.getTotal());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("สั่งซื้อไม่สำเร็จ: " + e.getMessage());
        }
    }

    private static void showOrders(OrderService orderService) {
        System.out.println();
        System.out.println("---- ประวัติคำสั่งซื้อ ----");
        if (orderService.getAllOrders().isEmpty()) {
            System.out.println("ยังไม่มีคำสั่งซื้อ");
            return;
        }
        for (Order order : orderService.getAllOrders()) {
            System.out.printf("เลขที่ %s | สมาชิก %s | วันที่ %s | ยอดรวม %.2f บาท%n",
                    order.getOrderId(), order.getMemberId(), order.getDate(), order.getTotal());
            for (OrderItem item : order.getItems()) {
                System.out.printf("   - %s x%d%n", item.getProduct().getName(), item.getQuantity());
            }
        }
        System.out.printf("ยอดขายรวมทั้งหมด: %.2f บาท%n", orderService.getTotalSales());
    }
}
