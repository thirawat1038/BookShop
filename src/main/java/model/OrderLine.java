package model;

// หนังสือแต่ละรายการในคำสั่งซื้อ

// เก็บชื่อและยอดรวมตอนขาย เพื่อให้ประวัติไม่เปลี่ยนตามสินค้า
public class OrderLine {
    private final Book product;
    private final String productName;
    private final int quantity;
    private final double subtotal;

    public OrderLine(Book product, int quantity) {
        this(product, quantity, product == null ? 0 : product.getPrice() * quantity);
    }

    public OrderLine(Book product, int quantity, double subtotal) {
        if (product == null) throw new IllegalArgumentException("Book is null");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (!Double.isFinite(subtotal) || subtotal < 0) throw new IllegalArgumentException("ยอดรวมไม่ถูกต้อง");
        this.product = product;
        this.productName = product.getName();
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public Book getProduct() { return product; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getSubtotal() { return subtotal; }
}
