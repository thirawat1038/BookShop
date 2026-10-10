package app;

// เปิดร้านแบบเมนูข้อความใน Terminal

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import model.Book;
import model.Order;
import model.OrderLine;
import service.BookManager;
import service.OrderManager;
import service.PurchaseManager;

public final class TextMenu {
    public static void main(String[] args) {
        ShopSystem application = new ShopSystem(new DataFiles());
        BookManager productService = application.products;
        OrderManager orderService = application.orders;
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("=== ยินดีต้อนรับสู่ BookShop ===");

        while (running && scanner.hasNextLine()) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    showProducts(productService);
                    break;
                case "2":
                    placeOrder(scanner, productService, application.checkout);
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

    private static void showProducts(BookManager productService) {
        System.out.println();
        System.out.println("---- รายการหนังสือ ----");
        if (productService.getAllProducts().isEmpty()) {
            System.out.println("ยังไม่มีสินค้าในระบบ");
            return;
        }
        for (Book product : productService.getAllProducts()) {
            System.out.printf("[%s] %-20s ราคา %.2f บาท  คงเหลือ %d เล่ม%n",
                    product.getId(), product.getName(), product.getPrice(), product.getStock());
        }
    }

    private static void placeOrder(Scanner scanner, BookManager productService, PurchaseManager checkout) {
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
                System.out.println("ไม่พบหนังสือ " + productId);
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
            if (qty <= 0 || qty > Integer.MAX_VALUE - productQuantities.getOrDefault(productId, 0)) {
                System.out.println("จำนวนต้องมากกว่า 0 และไม่เกินขอบเขตที่รองรับ");
                continue;
            }
            productQuantities.merge(productId, qty, Integer::sum);
            System.out.println("เพิ่มลงตะกร้าแล้ว");
        }

        if (productQuantities.isEmpty()) {
            System.out.println("ยกเลิก: ไม่มีสินค้าในตะกร้า");
            return;
        }

        try {
            Order order = checkout.checkout(memberId, productQuantities);

            System.out.println();
            System.out.println("สั่งซื้อสำเร็จ! เลขที่ออเดอร์: " + order.getOrderId());
            for (OrderLine item : order.getItems()) {
                System.out.printf("  - %s x%d = %.2f บาท%n",
                        item.getProductName(), item.getQuantity(), item.getSubtotal());
            }
            System.out.printf("ยอดรวมทั้งหมด: %.2f บาท%n", order.getTotal());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("สั่งซื้อไม่สำเร็จ: " + e.getMessage());
        }
    }

    private static void showOrders(OrderManager orderService) {
        System.out.println();
        System.out.println("---- ประวัติคำสั่งซื้อ ----");
        if (orderService.getAllOrders().isEmpty()) {
            System.out.println("ยังไม่มีคำสั่งซื้อ");
            return;
        }
        for (Order order : orderService.getAllOrders()) {
            System.out.printf("เลขที่ %s | สมาชิก %s | วันที่ %s | ยอดรวม %.2f บาท%n",
                    order.getOrderId(), order.getMemberId(), order.getDate(), order.getTotal());
            for (OrderLine item : order.getItems()) {
                System.out.printf("   - %s x%d%n", item.getProductName(), item.getQuantity());
            }
        }
        System.out.printf("ยอดขายรวมทั้งหมด: %.2f บาท%n", orderService.getTotalSales());
    }
}
