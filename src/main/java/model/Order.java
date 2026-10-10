package model;

// ข้อมูลคำสั่งซื้อและยอดรวม

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private final String orderId;
    private final String memberId;
    private final String date;
    private final List<OrderLine> items = new ArrayList<>();

    public Order(String orderId, String memberId, String date) {
        if (orderId == null || memberId == null || date == null) {
            throw new IllegalArgumentException("รหัสออเดอร์ รหัสสมาชิก และวันที่ห้ามเป็น null");
        }
        this.orderId = orderId;
        this.memberId = memberId;
        this.date = date;
    }

    public String getOrderId() { return orderId; }
    public String getMemberId() { return memberId; }
    public String getDate() { return date; }
    public List<OrderLine> getItems() { return Collections.unmodifiableList(items); }

    public void addItem(OrderLine item) {
        if (item == null) throw new IllegalArgumentException("item ห้ามเป็น null");
        items.add(item);
    }

    public double getTotal() { return items.stream().mapToDouble(OrderLine::getSubtotal).sum(); }
}
