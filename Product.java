package model;

import java.util.Objects;

/**
 * AF(id,name,price,stock,imagePath)
 * Product แทนสินค้า 1 ชิ้นในร้าน
 * โดย id = รหัสสินค้า
 *     name = ชื่อสินค้า
 *     price = ราคาสินค้า
 *     stock = จำนวนสินค้าคงเหลือ
 *     imagePath = path ของไฟล์รูปภาพสินค้า (ไม่มีก็ได้)
 * RI:
 *  - id ห้ามเป็น null (และแก้ไขไม่ได้หลังสร้าง object แล้ว)
 *  - name ห้ามเป็น null
 *  - price ต้องไม่ติดลบ (>= 0)
 *  - stock ต้องไม่ติดลบ (>= 0)
 *  - imagePath เป็น null ได้ (สินค้าบางชิ้นอาจยังไม่มีรูป)
 */
public class Product {
    private final String id;
    private String name;
    private double price;
    private int stock;
    private String imagePath;
    private String color; // สีปกหนังสือแบบ hex เช่น #E57373 (null = ให้โปรแกรมสุ่มสีจาก id)

    /**
     * สร้าง Product ใหม่ โดยไม่ระบุรูปภาพ (imagePath จะเป็น null)
     * เรียกใช้ constructor เต็มด้านล่างแทน เพื่อไม่ต้องเขียน validate ซ้ำ
     *
     * @param id    รหัสสินค้า ห้ามเป็น null
     * @param name  ชื่อสินค้า ห้ามเป็น null
     * @param price ราคา ต้องไม่ติดลบ
     * @param stock จำนวนคงเหลือ ต้องไม่ติดลบ
     * @throws IllegalArgumentException ถ้า id/name เป็น null หรือ price/stock ติดลบ
     */
    public Product(String id, String name, double price, int stock) {
        this(id, name, price, stock, null);
    }

    /**
     * สร้าง Product ใหม่ พร้อมระบุ path รูปภาพ
     *
     * @param id        รหัสสินค้า ห้ามเป็น null
     * @param name      ชื่อสินค้า ห้ามเป็น null
     * @param price     ราคา ต้องไม่ติดลบ
     * @param stock     จำนวนคงเหลือ ต้องไม่ติดลบ
     * @param imagePath path ของไฟล์รูปภาพ (เป็น null ได้ ถ้ายังไม่มีรูป)
     * @throws IllegalArgumentException ถ้า id/name เป็น null หรือ price/stock ติดลบ
     */
    public Product(String id, String name, double price, int stock, String imagePath) {
        this(id, name, price, stock, imagePath, null);
    }

    /**
     * สร้าง Product ใหม่ พร้อมระบุรูปภาพและสีปก
     *
     * @param color รหัสสีแบบ hex เช่น "#E57373" หรือ "E57373" (เป็น null ได้ = ใช้สีที่สุ่มจาก id)
     */
    public Product(String id, String name, double price, int stock, String imagePath, String color) {
        // ตรวจสอบให้ครบทุกค่าก่อน แล้วค่อย assign พร้อมกันทีเดียวท้ายสุด
        // ทำแบบนี้เพื่อไม่ให้ checkRep() (เรียกท้ายสุด) ไปเจอ field ที่ยังตั้งค่าไม่ครบ
        // ระหว่างกลาง (ต่างจากตอนแรกที่เรียกผ่าน setter ทีละตัว ทำให้ field ยังไม่ครบตอนเช็ค)
        if (id == null) {
            throw new IllegalArgumentException("Id is null!");
        }
        validateName(name);
        validatePrice(price);
        validateStock(stock);

        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.imagePath = imagePath;
        this.color = normalizeColor(color);
        checkRep();
    }

    // ---------- ตัวช่วยตรวจสอบเงื่อนไขอย่างเดียว (ไม่ assign ค่า) ----------
    // แยกออกมาเพื่อให้ทั้ง constructor และ setter เรียกใช้ตัวเดียวกันได้
    // ไม่ต้องเขียนเงื่อนไขซ้ำ 2 ที่

    private void validateName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name is null!");
        }
    }

    private void validatePrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price must be 0 or higher");
        }
    }

    private void validateStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot lessthan 0");
        }
    }

    /**
     * ตรวจสอบว่า Rep Invariant (RI) ยังเป็นจริงอยู่หรือไม่
     * ใช้ throw RuntimeException เพราะทำงานเสมอไม่ว่าจะรันแบบไหน
     * ไม่ต้องเปิด flag -ea เพิ่ม (ต่างจาก assert ที่ถูกปิดไว้เป็นค่าเริ่มต้นของ Java)
     */
    private void checkRep() {
        if (id == null) {
            throw new RuntimeException("RI violated: id คือ null");
        }
        if (name == null) {
            throw new RuntimeException("RI violated: name คือ null");
        }
        if (price < 0) {
            throw new RuntimeException("RI violated: price ติดลบ (" + price + ")");
        }
        if (stock < 0) {
            throw new RuntimeException("RI violated: stock ติดลบ (" + stock + ")");
        }
    }

    /**
     * @return รหัสสินค้า
     */
    public String getId() {
        return id;
    }

    /**
     * @return ชื่อสินค้า
     */
    public String getName() {
        return name;
    }

    /**
     * @return ราคาปัจจุบัน
     */
    public double getPrice() {
        return price;
    }

    /**
     * @return จำนวนคงเหลือปัจจุบัน
     */
    public int getStock() {
        return stock;
    }

    /**
     * @return path ของไฟล์รูปภาพสินค้า (null ถ้ายังไม่มีรูป)
     */
    /** @return รหัสสีปกแบบ #RRGGBB หรือ null ถ้าไม่ได้กำหนด */
    public String getColor() {
        return color;
    }

    /** @param color รหัสสี hex เช่น "#E57373" (null/ว่าง = ใช้สีอัตโนมัติ, รูปแบบผิด = ข้าม) */
    public void setColor(String color) {
        this.color = normalizeColor(color);
    }

    private static String normalizeColor(String c) {
        if (c == null) {
            return null;
        }
        c = c.trim();
        if (c.startsWith("#")) {
            c = c.substring(1);
        }
        return c.matches("[0-9a-fA-F]{6}") ? "#" + c.toUpperCase() : null;
    }

    public String getImagePath() {
        return imagePath;
    }

    /**
     * ตั้ง path รูปภาพใหม่ (เป็น null ได้ ถ้าต้องการลบรูปออก)
     *
     * @param imagePath path ของไฟล์รูปภาพ
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
        checkRep();
    }

    /**
     * ตั้งชื่อสินค้าใหม่
     *
     * @param name ชื่อสินค้า ห้ามเป็น null
     * @throws IllegalArgumentException ถ้า name เป็น null
     */
    public void setName(String name) {
        validateName(name);
        this.name = name;
        checkRep();
    }

    /**
     * ตั้งราคาใหม่
     *
     * @param price ราคาใหม่ ต้องไม่ติดลบ
     * @throws IllegalArgumentException ถ้า price ติดลบ
     */
    public void setPrice(double price) {
        validatePrice(price);
        this.price = price;
        checkRep();
    }

    /**
     * ตั้งจำนวนคงเหลือใหม่ทั้งหมด (เขียนทับค่าเดิม)
     *
     * @param stock จำนวนใหม่ ต้องไม่ติดลบ
     * @throws IllegalArgumentException ถ้า stock ติดลบ
     */
    public void setStock(int stock) {
        validateStock(stock);
        this.stock = stock;
        checkRep();
    }

    /**
     * ลดสต๊อกสินค้า ใช้ตอนขายของ
     *
     * @param qty จำนวนที่จะลด ต้องมากกว่า 0
     * @throws IllegalArgumentException ถ้า qty เป็น 0 หรือติดลบ
     * @throws IllegalStateException    ถ้าสต๊อกไม่พอ (qty มากกว่าที่มีอยู่)
     */
    public void reduceStock(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("จำนวนต้องมากกว่า 0: " + qty);
        }
        if (qty > stock) {
            throw new IllegalStateException("สินค้ารหัส " + id + " มีไม่พอ: ต้องการ "
                    + qty + " แต่มีอยู่ " + stock);
        }
        setStock(stock - qty);
    }

    /**
     * เพิ่มสต๊อกสินค้า ใช้ตอนรับสินค้าเข้าคลัง
     *
     * @param qty จำนวนที่จะเพิ่ม ต้องมากกว่า 0
     * @throws IllegalArgumentException ถ้า qty เป็น 0 หรือติดลบ
     */
    public void increaseStock(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("จำนวนต้องมากกว่า 0: " + qty);
        }
        setStock(stock + qty);
    }

    /**
     * แปลง Product เป็น 1 บรรทัดของไฟล์ CSV รูปแบบ: id,name,price,stock,imagePath,color
     * ถ้า imagePath เป็น null จะเขียนเป็นช่องว่างแทน (ไม่ใช่คำว่า "null")
     *
     * @return บรรทัด CSV ที่แทนค่า Product นี้
     */
    public String toCsvLine() {
        String imagePart = (imagePath == null) ? "" : imagePath;
        String colorPart = (color == null) ? "" : color;
        return id + "," + name + "," + price + "," + stock + "," + imagePart + "," + colorPart;
    }

    /**
     * แปลง 1 บรรทัดจากไฟล์ CSV กลับมาเป็น Product object
     * รองรับทั้งบรรทัดเก่า (4 คอลัมน์ ไม่มี imagePath) และบรรทัดใหม่ (5 คอลัมน์)
     *
     * @param line บรรทัด CSV รูปแบบ id,name,price,stock[,imagePath[,color]]
     * @return Product ที่แปลงมาจากบรรทัดนั้น
     */
    public static Product fromCsvLine(String line) {
        String[] p = line.split(",", -1);
        String imagePath = (p.length >= 5 && !p[4].isEmpty()) ? p[4] : null;
        String color = (p.length >= 6 && !p[5].isEmpty()) ? p[5] : null;
        return new Product(p[0], p[1], Double.parseDouble(p[2]), Integer.parseInt(p[3]), imagePath, color);
    }

    /**
     * @param other object ที่จะเทียบด้วย
     * @return true ถ้า id เดียวกัน
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Product)) return false;
        Product that = (Product) other;
        return Objects.equals(this.id, that.id);
    }

    /**
     * @return hash code ที่คำนวณจาก id (ต้องเท่ากับของ Product อื่นที่ equals() เป็น true)
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}