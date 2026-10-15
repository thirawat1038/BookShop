package model;

import java.util.Objects;

/**
 * AF(id,username,password,address,phone,role)
 * Member แทนสมาชิก 1 คนของร้านหนังสือ
 * โดย id = รหัสสมาชิก
 *     username = ชื่อที่แสดง (ใช้ล็อกอิน)
 *     password = รหัสผ่าน
 *     address = ที่อยู่สำหรับจัดส่ง
 *     phone = เบอร์ติดต่อ
 *     role = บทบาท (MEMBER = สมาชิกทั่วไป, ADMIN = ผู้ดูแลระบบ)
 * RI:
 *  - id ห้ามเป็น null หรือว่าง
 *  - username ยาว 6-32 ตัวอักษร
 *  - password ห้ามเป็น null หรือว่าง
 *  - address ห้ามเป็น null หรือว่าง
 *  - phone เป็นตัวเลขล้วน 10 หลัก
 *  - role ห้ามเป็น null
 *  - id, username, password, address ห้ามมีเครื่องหมายจุลภาค (,)
 *    เพราะจะทำให้ไฟล์ CSV อ่านกลับมาผิดคอลัมน์
 */
public class Member {
    private final String id;
    private final String username;
    private final String password;
    private final String address;
    private final String phone;
    private final Role role;

    /** บทบาทของสมาชิก */
    public enum Role {
        MEMBER,
        ADMIN
    }

    /**
     * สร้าง Member ใหม่
     *
     * @param id       รหัสสมาชิก ห้ามเป็น null หรือว่าง
     * @param username ชื่อผู้ใช้ ยาว 6-32 ตัวอักษร
     * @param password รหัสผ่าน ห้ามเป็น null หรือว่าง
     * @param address  ที่อยู่ ห้ามเป็น null หรือว่าง และห้ามมีเครื่องหมาย ,
     * @param phone    เบอร์ติดต่อ เป็นตัวเลข 10 หลัก
     * @param role     บทบาท ห้ามเป็น null
     * @throws IllegalArgumentException ถ้าค่าใดผิดเงื่อนไข
     */
    public Member(String id, String username, String password, String address, String phone, Role role) {
        // ตรวจสอบให้ครบทุกค่าก่อน แล้วค่อย assign พร้อมกันท้ายสุด
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id is null or blank!");
        }
        if (username == null || username.length() < 6 || username.length() > 32) {
            throw new IllegalArgumentException("Username must be 6-32 characters");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is null or blank!");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Address is null or blank!");
        }
        if (phone == null || !phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone must be 10 digits");
        }
        if (id.contains(",") || username.contains(",")
                || password.contains(",") || address.contains(",")) {
            throw new IllegalArgumentException("Comma (,) is not allowed");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role is null!");
        }

        this.id = id;
        this.username = username;
        this.password = password;
        this.address = address;
        this.phone = phone;
        this.role = role;
        checkRep();
    }

    /** สร้างสมาชิกทั่วไป (role = MEMBER) ใช้ตอนสมัครสมาชิก จึงสมัครเป็น ADMIN ผ่านช่องทางนี้ไม่ได้ */
    public Member(String id, String username, String password, String address, String phone) {
        this(id, username, password, address, phone, Role.MEMBER);
    }

    /**
     * ตรวจสอบว่า Rep Invariant (RI) ยังเป็นจริงอยู่หรือไม่
     */
    private void checkRep() {
        if (id == null || id.isBlank()) {
            throw new RuntimeException("RI violated: id ว่างหรือ null");
        }
        if (username == null || username.length() < 6 || username.length() > 32) {
            throw new RuntimeException("RI violated: username ต้องยาว 6-32 ตัวอักษร");
        }
        if (password == null || password.isBlank()) {
            throw new RuntimeException("RI violated: password ว่างหรือ null");
        }
        if (address == null || address.isBlank()) {
            throw new RuntimeException("RI violated: address ว่างหรือ null");
        }
        if (phone == null || !phone.matches("\\d{10}")) {
            throw new RuntimeException("RI violated: phone ต้องเป็นตัวเลข 10 หลัก");
        }
        if (id.contains(",") || username.contains(",")
                || password.contains(",") || address.contains(",")) {
            throw new RuntimeException("RI violated: พบเครื่องหมาย , ในข้อมูล");
        }
        if (role == null) {
            throw new RuntimeException("RI violated: role เป็น null");
        }
    }

    /** @return รหัสสมาชิก */
    public String getId() {
        return id;
    }

    /** @return ชื่อที่แสดง */
    public String getUsername() {
        return username;
    }

    /** @return ที่อยู่ */
    public String getAddress() {
        return address;
    }

    /** @return เบอร์ติดต่อ */
    public String getPhone() {
        return phone;
    }

    /** @return บทบาทของสมาชิก */
    public Role getRole() {
        return role;
    }

    /** @return true ถ้าเป็นผู้ดูแลระบบ */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * เช็คว่ารหัสผ่านที่ส่งมาตรงกับของสมาชิกคนนี้ไหม
     * ใช้ตอน login แทน getPassword() จะได้ไม่ต้องให้คลาสอื่นดึงรหัสผ่านออกไปเทียบเอง
     *
     * @param input รหัสผ่านที่ต้องการตรวจ
     * @return true ถ้าตรงกัน (input เป็น null จะได้ false)
     */
    public boolean checkPassword(String input) {
        return password.equals(input);
    }

    /**
     * แปลง Member เป็น 1 บรรทัดของไฟล์ CSV รูปแบบ: id,username,password,address,phone,role
     *
     * @return บรรทัด CSV ที่แทนค่า Member นี้
     */
    public String toCsvLine() {
        return id + "," + username + "," + password + "," + address + "," + phone + "," + role.name();
    }

    /**
     * แปลง 1 บรรทัดจากไฟล์ CSV กลับมาเป็น Member object
     *
     * @param line บรรทัด CSV รูปแบบ id,username,password,address,phone,role
     *             (ไฟล์เก่าที่มี 5 คอลัมน์ ไม่มี role จะถือเป็น MEMBER)
     * @return Member ที่แปลงมาจากบรรทัดนั้น
     * @throws IllegalArgumentException ถ้าจำนวนคอลัมน์ไม่ใช่ 5 หรือ 6 ค่า role ไม่รู้จัก หรือค่าผิดเงื่อนไข
     */
    public static Member fromCsvLine(String line) {
        String[] p = line.split(",", -1);
        if (p.length != 5 && p.length != 6) {
            throw new IllegalArgumentException("CSV line must have 5 or 6 columns but has " + p.length);
        }
        Role role = Role.MEMBER;
        if (p.length == 6) {
            try {
                role = Role.valueOf(p[5].trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown role: " + p[5]);
            }
        }
        return new Member(p[0], p[1], p[2], p[3], p[4], role);
    }

    /**
     * @return true ถ้า id เดียวกัน
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Member)) return false;
        Member that = (Member) other;
        return Objects.equals(this.id, that.id);
    }

    /**
     * @return hash code ที่คำนวณจาก id
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}