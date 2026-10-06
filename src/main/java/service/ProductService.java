package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import model.Product;

/**
 * AF(products)
 * ProductService จัดการรายการสินค้าทั้งหมดในร้าน (เพิ่ม/ลบ/ค้นหา/คำนวณมูลค่า)
 * โดย products = รายการสินค้าที่มีอยู่ในร้านตอนนี้
 * RI:
 *  - products ห้ามเป็น null
 *  - ไม่มีสมาชิกใน products ที่เป็น null
 *  - ไม่มี Product 2 ชิ้นที่ id ซ้ำกัน
 */
public class ProductService {
    private final List<Product> products = new ArrayList<>();

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

    /**
    มูลค่ารวมของสต๊อกทั้งหมด (ราคา x จำนวนคงเหลือ ของทุกสินค้า) ถ้าไม่มีสินค้าเลยคืน 0.0
     */
    public double calculateTotalStockValue() {
        double total = 0.0;
        for (Product product : products) {
            total += product.getPrice() * product.getStock();
        }
        return total;
    }

    /**
    true ถ้ามีสินค้าอย่างน้อย 1 ชิ้นที่ stock เป็น 0, false ถ้าไม่มี (รวมถึงกรณีไม่มีสินค้าเลย)
     */
    public boolean hasProductOutOfStock() {
        for (Product product : products) {
            if (product.getStock() == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     id รหัสสินค้าที่ต้องการค้นหา
     Optional ที่มีสินค้านั้น หรือ Optional.empty() ถ้าไม่เจอ
     */
    public Optional<Product> getProductById(String id) {
        for (Product product : products) {
            if (product.getId().equals(id)) {
                return Optional.of(product);
            }
        }
        return Optional.empty();
    }

    /**
     เพิ่มสินค้าใหม่เข้าร้าน
     product สินค้าที่จะเพิ่ม ห้ามเป็น null และ id ต้องไม่ซ้ำกับที่มีอยู่แล้ว
     *IllegalArgumentException ถ้า product เป็น null หรือ id ซ้ำ
     */
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

    /**
     * ลบสินค้าตามรหัส ถ้าไม่มีสินค้ารหัสนั้นจะไม่ทำอะไร
     id รหัสสินค้าที่จะลบ
     */
    public void removeProduct(String id) {
        products.removeIf(product -> product.getId().equals(id));
        checkRep();
    }

    /**
     รายการสินค้าทั้งหมด (แก้ไขจากภายนอกไม่ได้)
     */
    public List<Product> getAllProducts() {
        return Collections.unmodifiableList(products);
    }
}