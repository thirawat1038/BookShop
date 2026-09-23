package repository;

import java.util.List;

/**
 * Repository<T, ID> คือ "แบบฟอร์มกลาง" ที่บอกว่า
 * repository ทุกตัวในโปรแกรมต้องทำอะไรได้บ้าง
 * โดยไม่สนใจว่าข้างในจะไปอ่าน/เขียนจากไฟล์ CSV, ฐานข้อมูล, หรืออะไรก็ตาม
 *
 * T  = ชนิดของข้อมูลที่เก็บ เช่น Product, Order
 * ID = ชนิดของรหัสที่ใช้ค้นหา เช่น String (productId, orderId)
 *
 * ทำไมต้องมี interface นี้ (เรื่อง Interfaces & Polymorphism ที่เรียนมา):
 * - ตอนนี้เราเก็บข้อมูลเป็น CSV แต่ถ้าอนาคตอยากเปลี่ยนไปใช้ database จริง
 *   แค่เขียนคลาสใหม่ เช่น ProductDatabaseRepository implements Repository<Product, String>
 *   แล้วเอาไปสลับใช้แทนตัวเดิมได้เลย โดยที่ ProductService ไม่ต้องแก้โค้ดเลยสักบรรทัด
 *   เพราะ ProductService รู้จักแค่ "Repository" ไม่ได้รู้จักว่าข้างในเป็น CSV หรือ DB
 * - ทำให้ ProductRepository และ OrderRepository มีหน้าตา method ตรงกัน
 *   ใครมาอ่านโค้ดทีหลังก็เดาได้ทันทีว่าแต่ละตัวต้องมี findAll(), save(), findById()
 */
public interface Repository<T, ID> {

    /**
     * คืนข้อมูลทั้งหมดที่มีอยู่
     */
    List<T> findAll();

    /**
     * บันทึกข้อมูลทั้งหมดกลับลงไฟล์ (เขียนทับของเดิมทั้งหมด)
     * เหมาะกับ Product ที่แก้ไขบ่อย (เพิ่ม/ลบ/แก้ราคา)
     */
    void saveAll(List<T> items);

    /**
     * ค้นหา 1 รายการด้วยรหัส คืนค่า null ถ้าไม่เจอ
     * (ฝั่ง Service จะเป็นคนตัดสินใจว่าจะ throw exception ต่อไหม)
     */
    T findById(ID id);
}