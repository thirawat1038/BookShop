package service;

import java.util.Optional;

import model.Product;

public class ProductServiceTest {

    public static void main(String[] args) {
        ProductServiceTest test = new ProductServiceTest();

        test.testCalculateTotalStockValue();
        test.testCalculateTotalStockValueWhenEmpty();

        test.testHasProductOutOfStock();
        test.testHasProductOutOfStockWhenAllInStock();
        test.testHasProductOutOfStockWhenEmpty();

        test.testGetProductById();
        test.testGetProductByIdWhenNotFound();

        test.testAddProduct();
        test.testAddProductNullThrows();
        test.testAddProductDuplicateIdThrows();

        test.testRemoveProduct();
        test.testRemoveProductNotFoundDoesNothing();

        test.testGetAllProductsIsUnmodifiable();

        System.out.println("ทดสอบผ่านทั้งหมด");
    }

    // ---------- Testing strategy: calculateTotalStockValue ----------
    // Partition:
    //  - มีสินค้าหลายชิ้น -> ผลรวมของ (ราคา x stock) ทุกชิ้น
    //  - ไม่มีสินค้าเลย -> 0.0

    void testCalculateTotalStockValue() {
        ProductService service = new ProductService();
        service.addProduct(new Product("b01", "book01", 150.0, 50));   // 7500.0
        service.addProduct(new Product("b02", "book02", 165.0, 60));   // 9900.0
        service.addProduct(new Product("b03", "book03", 100.0, 0));    // 0.0
        assertEquals(17400.0, service.calculateTotalStockValue());
    }

    void testCalculateTotalStockValueWhenEmpty() {
        ProductService service = new ProductService();
        assertEquals(0.0, service.calculateTotalStockValue());
    }

    // ---------- Testing strategy: hasProductOutOfStock ----------
    // Partition:
    //  - มีสินค้าที่ stock = 0 อย่างน้อย 1 ชิ้น -> true
    //  - ทุกชิ้น stock > 0 -> false
    //  - ไม่มีสินค้าเลย -> false

    void testHasProductOutOfStock() {
        ProductService service = new ProductService();
        service.addProduct(new Product("b01", "book01", 150.0, 50));
        service.addProduct(new Product("b02", "book02", 165.0, 0));
        assertEquals(true, service.hasProductOutOfStock());
    }

    void testHasProductOutOfStockWhenAllInStock() {
        ProductService service = new ProductService();
        service.addProduct(new Product("b01", "book01", 150.0, 50));
        service.addProduct(new Product("b02", "book02", 165.0, 1));
        assertEquals(false, service.hasProductOutOfStock());
    }

    void testHasProductOutOfStockWhenEmpty() {
        ProductService service = new ProductService();
        assertEquals(false, service.hasProductOutOfStock());
    }

    // ---------- Testing strategy: getProductById ----------
    // Partition:
    //  - id ที่มีอยู่ -> เจอสินค้านั้น
    //  - id ที่ไม่มีอยู่ -> Optional.empty()

    void testGetProductById() {
        ProductService service = new ProductService();
        Product book = new Product("b01", "book01", 150.0, 50);
        service.addProduct(book);

        Optional<Product> result = service.getProductById("b01");
        assertEquals(true, result.isPresent());
        assertEquals(book, result.get());
    }

    void testGetProductByIdWhenNotFound() {
        ProductService service = new ProductService();
        service.addProduct(new Product("b01", "book01", 150.0, 50));

        Optional<Product> result = service.getProductById("b99");
        assertEquals(true, result.isEmpty());
    }

    // ---------- Testing strategy: addProduct ----------
    // Partition:
    //  - สินค้าปกติ -> เพิ่มได้ และค้นเจอ
    //  - null -> throw
    //  - id ซ้ำกับที่มีอยู่ -> throw และของเดิมไม่เปลี่ยน

    void testAddProduct() {
        ProductService service = new ProductService();
        Product book = new Product("b01", "book01", 150.0, 50);
        service.addProduct(book);

        assertEquals(1, service.getAllProducts().size());
        assertEquals(true, service.getAllProducts().contains(book));
    }

    void testAddProductNullThrows() {
        ProductService service = new ProductService();
        try {
            service.addProduct(null);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            // ผ่าน
        }
    }

    void testAddProductDuplicateIdThrows() {
        ProductService service = new ProductService();
        service.addProduct(new Product("b01", "book01", 150.0, 50));
        try {
            service.addProduct(new Product("b01", "ชื่ออื่น", 99.0, 1));
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
            // ผ่าน: ของเดิมต้องไม่ถูกเปลี่ยน
            assertEquals(1, service.getAllProducts().size());
            assertEquals("book01", service.getProductById("b01").get().getName());
        }
    }

    // ---------- Testing strategy: removeProduct ----------
    // Partition:
    //  - id ที่มีอยู่ -> ถูกลบออก
    //  - id ที่ไม่มีอยู่ -> ไม่มีอะไรเปลี่ยน

    void testRemoveProduct() {
        ProductService service = new ProductService();
        service.addProduct(new Product("b01", "book01", 150.0, 50));
        service.addProduct(new Product("b02", "book02", 165.0, 60));

        service.removeProduct("b01");

        assertEquals(1, service.getAllProducts().size());
        assertEquals(true, service.getProductById("b01").isEmpty());
        assertEquals(true, service.getProductById("b02").isPresent());
    }

    void testRemoveProductNotFoundDoesNothing() {
        ProductService service = new ProductService();
        service.addProduct(new Product("b01", "book01", 150.0, 50));

        service.removeProduct("b99");

        assertEquals(1, service.getAllProducts().size());
    }

    // ---------- Testing strategy: getAllProducts ต้องแก้ไขไม่ได้จากภายนอก ----------

    void testGetAllProductsIsUnmodifiable() {
        ProductService service = new ProductService();
        try {
            service.getAllProducts().add(new Product("b01", "book01", 150.0, 50));
            throw new AssertionError("ควร throw UnsupportedOperationException แต่ไม่ throw");
        } catch (UnsupportedOperationException e) {
            // ผ่าน: getAllProducts() ป้องกันการแก้ไขจากภายนอกถูกต้อง
        }
    }

    // ---------- ตัวช่วยเทส (assertEquals overload) ----------

    private void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected: " + expected + " actual is: " + actual);
        }
    }

    private void assertEquals(double expected, double actual) {
        if (expected != actual) {
            throw new AssertionError("Expected: " + expected + " actual is: " + actual);
        }
    }

    private void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Expected: " + expected + " actual is: " + actual);
        }
    }

    private void assertEquals(boolean expected, boolean actual) {
        if (expected != actual) {
            throw new AssertionError("Expected: " + expected + " actual is: " + actual);
        }
    }

    private void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected: " + expected + " actual is: " + actual);
        }
    }
}