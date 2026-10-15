package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
private final String orderId;
private final String memberId;   // ใหม่ เชื่อมกับ Member ที่สั่งซื้อ
private final String date;
private final List<OrderItem> items = new ArrayList<>();


private void checkRep() {
    if (orderId == null) {
        throw new RuntimeException("RI violated: orderId คือ null");
    }
    if (memberId == null) {
        throw new RuntimeException("RI violated: memberId คือ null");
    }
    if (date == null) {
        throw new RuntimeException("RI violated: date คือ null");
    }
    if (items == null) {
        throw new RuntimeException("RI violated: items คือ null");
    }
}
public Order(String orderId, String memberId, String date){
    if(orderId == null) throw new IllegalArgumentException("orderId no data");
    if(memberId == null) throw new IllegalArgumentException("memberId no data");
    if(date == null) throw new IllegalArgumentException("date no data");
    this.orderId = orderId;
    this.memberId = memberId;
    this.date = date;
    checkRep();

}
public String getOrderId() {
    return orderId;
}
public String getMemberId() {
    return memberId;
}
public String getDate() {
    return date;
}
public List<OrderItem> getItems() {
    return Collections.unmodifiableList(items);
}
public void addItem(OrderItem item) {
    if (item == null) {
        throw new IllegalArgumentException("item ห้ามเป็น null");
    }
    items.add(item);
}
public double getTotal() {
    double total = 0;
    for (OrderItem item : items) {
        total += item.getSubtotal();
    }
    return total;
}
}   
