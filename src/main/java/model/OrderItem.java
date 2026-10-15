package model;

public class OrderItem {
    private final Product product;
    private final int quantity;
    private final double subtotal;

    private void checkRep() {
    if (product == null) {
        throw new RuntimeException("RI violated: product คือ null");
    }
    if (quantity <= 0) {
        throw new RuntimeException("RI violated: quantity ต้องมากกว่า 0 (" + quantity + ")");
    }
}
    public OrderItem(Product product,int quantity){
        if(product == null) throw new IllegalArgumentException("Product is null");
        if(quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.product = product;
        this.quantity = quantity;
        this.subtotal = product.getPrice() * quantity;
        checkRep();
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubtotal() {
        return subtotal;
    }
}
