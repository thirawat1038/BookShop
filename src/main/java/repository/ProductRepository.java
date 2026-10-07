package repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import model.Product;

/**
 * AF(filePath)
 * ProductRepository จัดการอ่าน/เขียนข้อมูล Product ลงไฟล์ CSV จริง
 * โดย filePath = ตำแหน่งไฟล์ CSV ที่เก็บข้อมูลสินค้า (เช่น data/Products.csv)
 *
 * รูปแบบไฟล์ CSV: บรรทัดแรกเป็น header "id,name,price,stock,imagePath,color"
 * บรรทัดถัดไปแต่ละบรรทัดคือ 1 Product ใช้ Product.toCsvLine()/fromCsvLine()
 * ที่เตรียมไว้ในคลาส Product อยู่แล้ว เพื่อไม่ให้ format การแปลงข้อมูลกระจัดกระจาย
 *
 * RI:
 *  - filePath ห้ามเป็น null หรือว่าง
 */
public class ProductRepository implements Repository<Product, String> {

    private static final String HEADER = "id,name,price,stock,imagePath,color";

    private final Path filePath;

    /**
     * สร้าง ProductRepository ที่ผูกกับไฟล์ CSV ตามที่ระบุ
     * ถ้าไฟล์ยังไม่มีอยู่ จะถูกสร้างขึ้นใหม่ (พร้อม header) ตอนเรียก findAll()/saveAll() ครั้งแรก
     *
     * @param filePath ตำแหน่งไฟล์ CSV ห้ามเป็น null หรือว่าง
     */
    public ProductRepository(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath ห้ามเป็น null หรือว่าง");
        }
        this.filePath = Paths.get(filePath);
    }

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return products;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    // ข้าม header บรรทัดแรกเสมอ
                    firstLine = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }
                products.add(Product.fromCsvLine(line));
            }
        } catch (IOException e) {
            throw new RuntimeException("อ่านไฟล์ " + filePath + " ไม่สำเร็จ: " + e.getMessage(), e);
        }

        return products;
    }

    @Override
    public void saveAll(List<Product> items) {
        if (items == null) {
            throw new IllegalArgumentException("items ห้ามเป็น null");
        }

        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                writer.write(HEADER);
                writer.newLine();
                for (Product product : items) {
                    writer.write(product.toCsvLine());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("เขียนไฟล์ " + filePath + " ไม่สำเร็จ: " + e.getMessage(), e);
        }
    }

    @Override
    public Product findById(String id) {
        if (id == null) {
            return null;
        }
        for (Product product : findAll()) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        return null;
    }

    @Override
    public void save(Product item) {
        if (item == null) {
            throw new IllegalArgumentException("item ห้ามเป็น null");
        }
        List<Product> items = findAll();
        boolean replaced = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            items.add(item);
        }
        saveAll(items);
    }

    @Override
    public boolean delete(String id) {
        if (id == null) {
            return false;
        }
        List<Product> items = findAll();
        boolean removed = items.removeIf(p -> p.getId().equals(id));
        if (removed) {
            saveAll(items);
        }
        return removed;
    }
}
