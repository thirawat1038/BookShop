# BookShop

นาย ถิรวัฒน์ แท่นทรัพย์ 6821601038

นาย อนันดา นามวงศ์ษา 6821601585

นาย อนุชิต ฟักสุมณฑา 6821601593

## ถิรวัฒน์ แท่นทรัพย์ — Model / Data

รับผิดชอบคลาสข้อมูลหลัก

- Product
- คลาส Model อื่น ๆ
- กำหนด field / constructor / getter
- toString(), equals(), hashCode() ถ้ามี
- ตรวจสอบข้อมูล เช่น stock, price

## อนันดา นามวงศ์ษา — Repository / Data Access

รับผิดชอบการจัดการข้อมูล

- Repository<T, ID> interface
- ProductRepository
- findAll()
- findById()
- save() / saveAll()
- delete() ถ้ามี
- จัดการ List<Product> หรือแหล่งเก็บข้อมูล

## อนุชิต ฟักสุมณฑา — Service / Application Logic

รับผิดชอบ logic ของโปรแกรม

- ProductService หรือคลาส Service
- คำนวณยอดรวม stock
- เช็กสินค้าหมด
- ค้นหา/กรองสินค้า
- logic ที่เรียกใช้ Repository
- เขียน Main / ทดลองการทำงาน

## วิธีรันหน้า GUI (Swing)

รันจาก root ของโปรเจกต์ (โฟลเดอร์ที่มี `data/`) และต้องคอมไพล์ด้วย UTF-8 เสมอ ไม่งั้นภาษาไทยในโค้ดจะเพี้ยน

```
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")
java -Dfile.encoding=UTF-8 -cp out ui.BookShopApp
```

- ปกหนังสือ: ใส่ path รูปในคอลัมน์ที่ 5 ของ `data/Products.csv` (เช่น `data/covers/b03.jpg`) ถ้าไม่ใส่จะสร้างปกสีให้อัตโนมัติ
- ฟอนต์ไทยเลือกอัตโนมัติใน `ui/Theme.java` (Leelawadee UI → Tahoma → Noto Sans Thai → ...)
