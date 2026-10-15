package model;

/**
 * ประเภทของสินค้าในร้าน ใช้แยกสินค้าตามแท็บในหน้าร้าน
 * โดย label = ชื่อภาษาไทยที่แสดงบนหน้าจอ (ตรงกับชื่อแท็บ)
 *
 * ค่าที่เขียนในไฟล์ Products.csv คือชื่อ enum เช่น BOOK, EBOOK, MAGAZINE, EMAGAZINE
 */
public enum ProductType {
    BOOK("หนังสือ"),
    EBOOK("อีบุ๊ก"),
    MAGAZINE("นิตยสาร"),
    EMAGAZINE("อีแมกกาซีน");

    private final String label;

    ProductType(String label) {
        this.label = label;
    }

    /** @return ชื่อภาษาไทยของประเภทนี้ */
    public String getLabel() {
        return label;
    }

    /**
     * แปลงข้อความจากไฟล์ CSV เป็นประเภทสินค้า (ไม่สนตัวพิมพ์เล็ก/ใหญ่ และตัดช่องว่างหัวท้าย)
     *
     * @param text ข้อความ เช่น "EBOOK" หรือ "ebook"
     * @return ประเภทที่ตรงกัน
     * @throws IllegalArgumentException ถ้า text เป็น null หรือไม่ตรงกับประเภทใดเลย
     */
    public static ProductType parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Type is null!");
        }
        try {
            return valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown type: " + text);
        }
    }
}
