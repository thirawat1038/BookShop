package service;

// ค้นหาและจัดการหนังสือ

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import model.Book;
import repository.BookFile;

public class BookManager {
    private final List<Book> products = new ArrayList<>();
    private final BookFile repository;

    public BookManager() { repository = null; }

    public BookManager(BookFile repository) {
        if (repository == null) throw new IllegalArgumentException("repository ห้ามเป็น null");
        this.repository = repository;
        products.addAll(repository.findAll());
        Set<String> ids = new HashSet<>();
        for (Book product : products) {
            if (!ids.add(product.getId())) throw new IllegalStateException("รหัสสินค้าซ้ำ: " + product.getId());
        }
    }

    public void save() {
        if (repository == null) throw new IllegalStateException("ยังไม่ได้กำหนดไฟล์สินค้า");
        repository.saveAll(products);
    }

    public double calculateTotalStockValue() {
        return products.stream().mapToDouble(product -> product.getPrice() * product.getStock()).sum();
    }

    public boolean hasProductOutOfStock() {
        return products.stream().anyMatch(product -> product.getStock() == 0);
    }

    public Optional<Book> getProductById(String id) {
        return products.stream().filter(product -> product.getId().equals(id)).findFirst();
    }

    public void addProduct(Book product) {
        if (product == null) throw new IllegalArgumentException("product ห้ามเป็น null");
        if (getProductById(product.getId()).isPresent()) throw new IllegalArgumentException("รหัสสินค้าซ้ำ: " + product.getId());
        products.add(product);
    }

    public void removeProduct(String id) { products.removeIf(product -> product.getId().equals(id)); }

    void restoreProduct(int index, Book product) { products.add(index, product); }

    public List<Book> getAllProducts() { return Collections.unmodifiableList(products); }

    public List<Book> search(String query) {
        String keyword = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return products.stream().filter(product -> keyword.isEmpty()
                || product.getId().toLowerCase(Locale.ROOT).contains(keyword)
                || product.getName().toLowerCase(Locale.ROOT).contains(keyword)
                || product.getAuthor().toLowerCase(Locale.ROOT).contains(keyword)
                || product.getCategory().toLowerCase(Locale.ROOT).contains(keyword)).toList();
    }
}
