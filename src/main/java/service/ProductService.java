package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import model.Product;
import repository.ProductRepository;

public class ProductService {
    private final List<Product> products = new ArrayList<>();
    private final ProductRepository repository;

    private void checkRep() {
        if (products == null) {
            throw new RuntimeException("RI violated: products คือ null");
        }
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            if (p == null) {
                throw new RuntimeException("RI violated: มีสินค้าเป็น null ที่ตำแหน่ง " + i);
            }
            for (int j = i + 1; j < products.size(); j++) {
                if (p.getId().equals(products.get(j).getId())) {
                    throw new RuntimeException("RI violated: id ซ้ำกัน (" + p.getId() + ")");
                }
            }
        }
    }

    public ProductService() {
        this.repository = null;
    }

    public ProductService(ProductRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("repository ห้ามเป็น null");
        }
        this.repository = repository;
        products.addAll(repository.findAll());
        checkRep();
    }

    public void save() {
        if (repository == null) {
            throw new IllegalStateException("ProductService นี้ไม่ได้ผูกกับ repository จึงบันทึกไม่ได้");
        }
        repository.saveAll(products);
    }

    public double calculateTotalStockValue() {
        double total = 0.0;
        for (Product product : products) {
            total += product.getPrice() * product.getStock();
        }
        return total;
    }

    public boolean hasProductOutOfStock() {
        for (Product product : products) {
            if (product.getStock() == 0) {
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
        if (product == null) {
            throw new IllegalArgumentException("product ห้ามเป็น null");
        }
        if (getProductById(product.getId()).isPresent()) {
            throw new IllegalArgumentException("มีสินค้ารหัส " + product.getId() + " อยู่แล้ว");
        }
        products.add(product);
        checkRep();
    }

    public void removeProduct(String id) {
        products.removeIf(product -> product.getId().equals(id));
        checkRep();
    }

    public List<Product> getAllProducts() {
        return Collections.unmodifiableList(products);
    }
}