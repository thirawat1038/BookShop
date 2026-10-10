package model;

// ข้อมูลหนังสือ ราคา และจำนวนคงเหลือ

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import util.CsvText;

public class Book {
    public static final List<String> CATEGORIES = List.of("หนังสือ", "อีบุ๊ก", "นิยายสาร", "อีแมกกาซีน");

    private final String id;
    private String name;
    private double price;
    private int stock;
    private String imagePath;
    private String color;
    private final String author;
    private final String category;
    private final String publisher;
    private final String description;

    public Book(String id, String name, double price, int stock) {
        this(id, name, price, stock, null);
    }

    public Book(String id, String name, double price, int stock, String imagePath) {
        this(id, name, price, stock, imagePath, null);
    }

    public Book(String id, String name, double price, int stock, String imagePath, String color) {
        this(id, name, price, stock, imagePath, color, "", "", "", "");
    }

    public Book(String id, String name, double price, int stock, String imagePath, String color,
                   String author, String category, String publisher, String description) {
        this.id = required(id, "รหัสสินค้า");
        this.name = required(name, "ชื่อหนังสือ");
        validatePrice(price);
        validateStock(stock);
        this.price = price;
        this.stock = stock;
        this.imagePath = imagePath;
        this.color = normalizeColor(color);
        this.author = Objects.requireNonNull(author, "author");
        this.category = Objects.requireNonNull(category, "category");
        this.publisher = Objects.requireNonNull(publisher, "publisher");
        this.description = Objects.requireNonNull(description, "description");
        if (description.length() > 500) throw new IllegalArgumentException("รายละเอียดต้องไม่เกิน 500 ตัวอักษร");
    }

    private static String required(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("กรุณากรอก" + label);
        return value;
    }

    private static void validatePrice(double price) {
        if (!Double.isFinite(price) || price < 0) throw new IllegalArgumentException("ราคาต้องเป็นจำนวนที่ไม่ติดลบ");
    }

    private static void validateStock(int stock) {
        if (stock < 0) throw new IllegalArgumentException("จำนวนคงเหลือต้องไม่ติดลบ");
    }

    private static String normalizeColor(String color) {
        if (color == null) return null;
        String hex = color.trim().replaceFirst("^#", "");
        return hex.matches("[0-9a-fA-F]{6}") ? "#" + hex.toUpperCase(Locale.ROOT) : null;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public String getImagePath() { return imagePath; }
    public String getColor() { return color; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getPublisher() { return publisher; }
    public String getDescription() { return description; }

    public void setName(String name) { this.name = required(name, "ชื่อหนังสือ"); }
    public void setPrice(double price) { validatePrice(price); this.price = price; }
    public void setStock(int stock) { validateStock(stock); this.stock = stock; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setColor(String color) { this.color = normalizeColor(color); }

    public void reduceStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("จำนวนต้องมากกว่า 0");
        if (quantity > stock) throw new IllegalStateException("สินค้า " + name + " มีไม่พอ (คงเหลือ " + stock + ")");
        stock -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("จำนวนต้องมากกว่า 0");
        if (quantity > Integer.MAX_VALUE - stock) throw new IllegalArgumentException("จำนวนสินค้าเกินขอบเขตที่รองรับ");
        stock += quantity;
    }

    public List<String> toCsvFields() {
        return List.of(id, name, Double.toString(price), Integer.toString(stock),
                imagePath == null ? "" : imagePath, color == null ? "" : color,
                author, category, publisher, description);
    }

    public String toCsvLine() { return CsvText.encodeRow(toCsvFields()); }

    public static Book fromCsvLine(String line) {
        return fromCsvFields(CsvText.parseRow(line));
    }

    public static Book fromCsvFields(List<String> fields) {
        if (fields.size() < 4 || fields.size() > 10) throw new IllegalArgumentException("จำนวนคอลัมน์สินค้าไม่ถูกต้อง");
        return new Book(fields.get(0), fields.get(1), Double.parseDouble(fields.get(2)),
                Integer.parseInt(fields.get(3)), optional(fields, 4).isEmpty() ? null : fields.get(4),
                optional(fields, 5), optional(fields, 6), optional(fields, 7), optional(fields, 8), optional(fields, 9));
    }

    private static String optional(List<String> fields, int index) {
        return index < fields.size() ? fields.get(index) : "";
    }

    @Override public boolean equals(Object other) {
        return this == other || other instanceof Book product && id.equals(product.id);
    }
    @Override public int hashCode() { return id.hashCode(); }
}
