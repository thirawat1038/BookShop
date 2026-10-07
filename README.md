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

## อนันดา นามวงศ์ษา — Repository / Data Access / GUI

รับผิดชอบการจัดการข้อมูล

- Repository<T, ID> interface
- ProductRepository
- findAll()
- findById()
- save() / saveAll()
- delete() ถ้ามี
- จัดการ List<Product> หรือแหล่งเก็บข้อมูล
- GUI

## อนุชิต ฟักสุมณฑา — Service / Application Logic

รับผิดชอบ logic ของโปรแกรม

- ProductService หรือคลาส Service
- คำนวณยอดรวม stock
- เช็กสินค้าหมด
- ค้นหา/กรองสินค้า
- logic ที่เรียกใช้ Repository
- เขียน Main / ทดลองการทำงาน
