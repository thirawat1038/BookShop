package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import model.Product;
import model.ProductType;
import repository.ProductRepository;

/**
 * AF(products, repository)
 * ProductService จัดการรายการสินค้าทั้งหมดในร้าน (เพิ่ม/ลบ/ค้นหา/คำนวณมูลค่า)
 * โดย products   = รายการสินค้าที่มีอยู่ในร้านตอนนี้ (อยู่ใน memory)
 *     repository = ตัวที่ใช้โหลด/บันทึกข้อมูลจริงลงไฟล์ (เป็น null ได้ ถ้าต้องการใช้แบบ in-memory
 *                  ล้วน ๆ เช่นตอนเทส ซึ่งจะเรียก save() ไม่ได้)
 * RI:
 *  - products ห้ามเป็น null
 *  - ไม่มีสมาชิกใน products ที่เป็น null
 *  - ไม่มี Product 2 ชิ้นที่ id ซ้ำกัน
 */
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

    /**
     * สร้าง ProductService แบบ in-memory ล้วน ๆ ไม่ผูกกับไฟล์ใด ๆ
     * เหมาะสำหรับการเทส หรือกรณีที่ไม่ต้องการบันทึกข้อมูลถาวร
     * เรียก save() บน instance นี้ไม่ได้ (จะ throw IllegalStateException)
     */
    public ProductService() {
        this.repository = null;
    }

    /**
     * สร้าง ProductService ที่ผูกกับ ProductRepository และโหลดสินค้าทั้งหมดจากไฟล์ทันที
     *
     * @param repository repository ที่จะใช้โหลด/บันทึกข้อมูล ห้ามเป็น null
     * @throws IllegalArgumentException ถ้า repository เป็น null
     */
    public ProductService(ProductRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("repository ห้ามเป็น null");
        }
        this.repository = repository;
        products.addAll(repository.findAll());
        checkRep();
    }

    /**
     * บันทึกสินค้าทั้งหมดตอนนี้กลับลงไฟล์ ผ่าน repository ที่ผูกไว้
     *
     * @throws IllegalStateException ถ้า ProductService นี้สร้างแบบ in-memory (ไม่มี repository)
     */
    public void save() {
        if (repository == null) {
            throw new IllegalStateException("ProductService นี้ไม่ได้ผูกกับ repository จึงบันทึกไม่ได้");
        }
        repository.saveAll(products);
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

    /**
     * สินค้าเฉพาะประเภทที่ระบุ เรียงตามลำดับเดียวกับรายการทั้งหมด (แก้ไขจากภายนอกไม่ได้)
     *
     * @param type ประเภทที่ต้องการ ห้ามเป็น null
     * @return list ของสินค้าประเภทนั้น (ว่างถ้ายังไม่มี)
     * @throws IllegalArgumentException ถ้า type เป็น null
     */
    /**
     * ปรับสต๊อกของสินค้า 1 รายการ แล้วบันทึกลงไฟล์ทันที (ถ้า ProductService นี้ผูกกับ repository)
     * ถ้าบันทึกลงไฟล์ไม่สำเร็จ จะย้อนสต๊อกกลับเป็นค่าเดิมก่อน แล้วค่อย throw ต่อ
     * เพื่อไม่ให้ในหน่วยความจำกับในไฟล์ไม่ตรงกัน
     *
     * @param id    รหัสสินค้าที่ต้องการปรับสต๊อก
     * @param delta จำนวนที่ปรับ: เป็นบวก = เพิ่มสต๊อก, เป็นลบ = ลดสต๊อก (ห้ามเป็น 0)
     * @return สินค้านั้นหลังปรับสต๊อกแล้ว
     * @throws IllegalArgumentException ถ้า delta เป็น 0, ไม่พบสินค้า หรือเพิ่มจนเกินขีดจำกัดของ int
     * @throws IllegalStateException    ถ้าลดมากกว่าจำนวนคงเหลือ
     * @throws RuntimeException         ถ้าบันทึกลงไฟล์ไม่สำเร็จ (สต๊อกจะถูกย้อนกลับแล้ว)
     */
    public Product adjustStock(String id, int delta) {
        if (delta == 0) {
            throw new IllegalArgumentException("จำนวนที่ปรับต้องไม่เป็น 0");
        }
        Product product = getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบสินค้ารหัส " + id));

        if (delta > 0) {
            if (product.getStock() > Integer.MAX_VALUE - delta) {
                throw new IllegalArgumentException("สต๊อกรวมมากเกินไป (ไม่เกิน " + Integer.MAX_VALUE + ")");
            }
            product.increaseStock(delta);
        } else {
            product.reduceStock(-delta); // สต๊อกไม่พอ -> IllegalStateException และยังไม่เปลี่ยนค่า
        }

        if (repository != null) {
            try {
                repository.saveAll(products);
            } catch (RuntimeException e) {
                if (delta > 0) {
                    product.reduceStock(delta);
                } else {
                    product.increaseStock(-delta);
                }
                throw e;
            }
        }
        checkRep();
        return product;
    }

    public List<Product> getProductsByType(ProductType type) {
        if (type == null) {
            throw new IllegalArgumentException("type ห้ามเป็น null");
        }
        List<Product> result = new ArrayList<>();
        for (Product product : products) {
            if (product.getType() == type) {
                result.add(product);
            }
        }
        return Collections.unmodifiableList(result);
    }
}