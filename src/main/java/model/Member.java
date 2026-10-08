package model;

import java.util.Objects;

public class Member {
    private final String id;
    private final String username;
    private final String password;
    private final String address;
    private final String phone;

    public Member(String id, String username, String password, String address, String phone) {
        // ตรวจสอบให้ครบทุกค่าก่อน 
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

        this.id = id;
        this.username = username;
        this.password = password;
        this.address = address;
        this.phone = phone;
        checkRep();
    }

    //ตรวจสอบว่ายังเป็นจริงอยู่หรือไม่
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
    }

    //รหัสสมาชิก
    public String getId() {
        return id;
    }

    // ชื่อที่แสดง 
    public String getUsername() {
        return username;
    }

    //ที่อยู่ 
    public String getAddress() {
        return address;
    }

    //เบอร์ติดต่อ 
    public String getPhone() {
        return phone;
    }

    //เช็คว่ารหัสผ่านที่ส่งมาตรงกับของสมาชิกคนนี้ไหม
    public boolean checkPassword(String input) {
        return password.equals(input);
    }

    public String toCsvLine() {
        return id + "," + username + "," + password + "," + address + "," + phone;
    }

    public static Member fromCsvLine(String line) {
        String[] p = line.split(",", -1);
        if (p.length != 5) {
            throw new IllegalArgumentException("CSV line must have 5 columns but has " + p.length);
        }
        return new Member(p[0], p[1], p[2], p[3], p[4]);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Member)) return false;
        Member that = (Member) other;
        return Objects.equals(this.id, that.id);
    }
    public int hashCode() {
        return Objects.hash(id);
    }
}
