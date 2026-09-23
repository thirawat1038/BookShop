package service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductService {

    private final List<Product> products = new ArrayList<>();

    public double calculateTotalStockValue() {
        double total = 0.0;
        for (Product product : products) {
            total += product.getPrice() * product.getQuantity();
        }
        return total;
    }

    public boolean hasProductOutOfStock() {
        for (Product product : products) {
            if (product.getQuantity() == 0) {
                return true;
            }
        }
        return false;
    }

    public Optional<Product> getProductById(String id) {
        for (Product product : products) {
            if (product.getId().equals(id)) {
                return Optional.of(product);
            }
        }
        return Optional.empty();
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public void removeProduct(String id) {
        products.removeIf(product -> product.getId().equals(id));
    }

    public List<Product> getAllProducts() {
        return products;
    }
}