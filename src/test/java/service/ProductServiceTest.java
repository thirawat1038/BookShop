package service;
import//
import//


public class ProductServiceTest{
void calculateTotalStockValue(){
    productService.addProduct(new Product(/*เพิ่มidชื่อจำนวนสินค้าราคา*/));
    productService.addProduct(new Product(/*เพิ่มidชื่อจำนวนสินค้าราคา*/));
    productService.addProduct(new Product(/*เพิ่มidชื่อจำนวนสินค้าราคา*/));
    double total = productService.calculateTotalStockValue();เรียกคำนวนมูลค้ารวมแล้วเก็บในtotal
    assertEquals(มูลค่ารวมสิ้นค้า,total,0.001);
}

void calculateTotalStockValueWhenEmpty() {
    double total = productService.calculateTotalStockValue();
    assertEquals(0.0, total, 0.001);//ถ้าไม่มีสิ้นค้าเลยคือเป็น0

}    

void hasProductOutOfStock() {
    productService.addProduct(new Product(/*เพิ่มสิ้นค่าที่จำนวนหมด */));
    productService.addProduct(new Product(/*เพิ่มสิ้นค่าที่จำนวนหมด */));
    assertTrue(productService.hasProductOutOfStock());//คือค่าเป็นtrunถ้ามีสินค้าหมด

}

void hasProductOutOfStockWhenAllInStock() {
    productService.addProduct(new Product(/*เพิ่มสิ้นค่าที่จำนวนหมด */));
    productService.addProduct(new Product(/*เพิ่มสิ้นค่าที่จำนวนหมด */));
    assertFalse(productService.hasProductOutOfStock());//คือค่าเป็นfalseถ้ามีสินค้ายังมีของอยู่

}

void hasProductOutOfStockWhenEmpty() {
    assertFalse(productService.hasProductOutOfStock());//ถ้าไม่มีสิ้นค้าเลยคือเป็นfalse

}

void getProductById() {
    Product เพิ่มชื่อสิ้นค้า = new Product(/*เพิ่มชื่อจำนวนสินค้าราคา*/);
    productService.addProduct(เพิ่มชื่อสิ้นค้า);
    รอเพิ่มในProductService result = productService.getProductById( idสิ้นค้า  );
    assertTrue(result.isPresent());//ใ้ช้เช็คว่ามีสิ้นค้า
    }

void getProductByIdWhenNotFound() {
    รอเพิ่มในProductService result = productService.getProductById(ไม่มีสิ้นค้าจริง);
    assertTrue(result.isEmpty());หาสิ้นค้าที่ไม่มีจริง
}   

void addProduct() {
    Product ชื่อสิ้นค้าทีเพิ่ม = new Product(เพิ่มidชื่อจำนวนสินค้าราคา);
    productService.addProduct(ชื่อสิ้นค้าทีเพิ่ม);
    assertEquals(1, productService.getAllProducts().size());//สิ้นค้าต้องมี่1ขั้นต่ำ
    assertTrue(productService.getAllProducts().contains(ชื่อสิ้นค้าทีเพิ่ม));//เช็คว่าสิ้นค้ามีอยู่
}

void removeProduct() {
    Product keyboard = new Product(เพิ่มidชื่อจำนวนสินค้าราคาที่จะลบ);
    productService.addProduct(ชื่อสิ้นค้าทีจะลบ);
    productService.removeProduct(idที่จะลบ);
    assertTrue(productService.getAllProducts().isEmpty());//เช็คว่าลบแล้วจริง
}
}