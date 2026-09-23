package model;

public class Product {
    private String id;
    private String name;
    private double price;
    private int stock;
    public Product(String id,String name,double price,int stock){
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
        public String getId(){
        return id;
        }
        public String getName(){
            return name;
        }
        public double getPrice(){
            return price;
        }
        public int getStock(){
            return stock;
        }
        public void setId(String id) {
            this.id = id;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void setPrice(double price) {
            this.price = price;
        }
        public void setStock(int stock) {
            this.stock = stock;
        }

        public String toCsvLine(){
            return id + "," + name + "," + price + "," +stock;
        }
        public static Product fromCsvLine(String line){
            String[] p = line.split(",");
            return new Product(p[0], p[1], Double.parseDouble(p[2]), Integer.parseInt(p[3]));
        }
}


