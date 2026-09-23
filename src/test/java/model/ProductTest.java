package model;

public class ProductTest {
void testConstructor() {
    Product p = new Product("P001", "Keyboard", 599.5, 10);

    assertEquals("b01", p.getId());
    assertEquals("book01", p.getName());
    assertEquals(150.0, p.getPrice());
    assertEquals(50, p.getStock());
}

private void assertEquals(String datatest, String dataActual) {
    if(!datatest.equals(dataActual)){
        throw new AssertionError("Expected: "+ datatest +" actual is: "+ dataActual);
    }
}
private  void assertEquals(double priceTest, double priceActual  ){
    if()
}
}
