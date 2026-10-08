package model;

import java.util.Objects;

public class Product {
    private final String id;
    private String name;
    private double price;
    private int stock;
    private String imagePath;
    private String color; // สีปกหนังสือแบบ hex เช่น #E57373 (null = ให้โปรแกรมสุ่มสีจาก id)

    public Product(String id, String name, double price, int stock) {
        this(id, name, price, stock, null);
    }

    public Product(String id, String name, double price, int stock, String imagePath) {
        this(id, name, price, stock, imagePath, null);
    }

    public Product(String id, String name, double price, int stock, String imagePath, String color) {
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

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String getColor() {
        return color;
    }

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

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
        checkRep();
    }

    public void setName(String name) {
        validateName(name);
        this.name = name;
        checkRep();
    }

    public void setPrice(double price) {
        validatePrice(price);
        this.price = price;
        checkRep();
    }

    public void setStock(int stock) {
        validateStock(stock);
        this.stock = stock;
        checkRep();
    }

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

    public void increaseStock(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("จำนวนต้องมากกว่า 0: " + qty);
        }
        setStock(stock + qty);
    }

    public String toCsvLine() {
        String imagePart = (imagePath == null) ? "" : imagePath;
        String colorPart = (color == null) ? "" : color;
        return id + "," + name + "," + price + "," + stock + "," + imagePart + "," + colorPart;
    }

    public static Product fromCsvLine(String line) {
        String[] p = line.split(",", -1);
        String imagePath = (p.length >= 5 && !p[4].isEmpty()) ? p[4] : null;
        String color = (p.length >= 6 && !p[5].isEmpty()) ? p[5] : null;
        return new Product(p[0], p[1], Double.parseDouble(p[2]), Integer.parseInt(p[3]), imagePath, color);
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Product)) return false;
        Product that = (Product) other;
        return Objects.equals(this.id, that.id);
    }

    public int hashCode() {
        return Objects.hash(id);
    }
}