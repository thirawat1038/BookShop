package repository;

// อ่านและบันทึกไฟล์หนังสือ

import java.util.ArrayList;
import java.util.List;
import model.Book;

public class BookFile extends DataStore.Csv<Book> {
    private static final List<String> HEADER = List.of("id", "name", "price", "stock", "imagePath", "color",
            "author", "category", "publisher", "description");

    public BookFile(String filePath) { super(filePath); }

    @Override protected String idOf(Book product) { return product.getId(); }

    @Override public List<Book> findAll() {
        List<Book> products = new ArrayList<>();
        int rowNumber = 1;
        for (List<String> row : CsvFiles.readRows(filePath)) {
            rowNumber++;
            try {
                products.add(Book.fromCsvFields(row));
            } catch (IllegalArgumentException error) {
                throw new IllegalStateException("ข้อมูลสินค้าในไฟล์ " + filePath + " บรรทัดที่ " + rowNumber + " ไม่ถูกต้อง", error);
            }
        }
        return products;
    }

    @Override public void saveAll(List<Book> products) {
        if (products == null) throw new IllegalArgumentException("products ห้ามเป็น null");
        CsvFiles.writeRows(filePath, HEADER, products.stream().map(Book::toCsvFields).toList());
    }
}
