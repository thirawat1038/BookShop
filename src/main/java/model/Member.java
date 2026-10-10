package model;

// ข้อมูลสมาชิกและข้อมูลสำหรับเข้าสู่ระบบ

import java.util.List;
import java.util.Objects;
import util.CsvText;

public class Member {
    private final String id;
    private final String username;
    private final String password;
    private final String address;
    private final String phone;
    private final String email;
    private final String firstName;
    private final String lastName;

    public Member(String id, String username, String password, String address, String phone) {
        this(id, username, password, address, phone, "", "", "");
    }

    public Member(String id, String username, String password, String address, String phone,
                  String email, String firstName, String lastName) {
        validateCore(id, username, password);
        validateProfile(address, phone, email, firstName, lastName);
        validateCsvFields(id, username, password, address, phone, email, firstName, lastName);
        this.id = id;
        this.username = username;
        this.password = password;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    private static void validateCore(String id, String username, String password) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("รหัสสมาชิกห้ามว่าง");
        if (username == null || username.length() < 6 || username.length() > 32) {
            throw new IllegalArgumentException("ชื่อที่แสดงต้องมี 6-32 ตัวอักษร");
        }
        if (password == null || password.isBlank()) throw new IllegalArgumentException("กรุณากรอกรหัสผ่าน");
    }

    private static void validateProfile(String address, String phone, String email, String firstName, String lastName) {
        if (email == null || firstName == null || lastName == null || address == null || phone == null) {
            throw new IllegalArgumentException("ข้อมูลสมาชิกห้ามเป็น null");
        }
        boolean legacyAccount = email.isEmpty();
        if (legacyAccount && address.isBlank()) throw new IllegalArgumentException("กรุณากรอกที่อยู่");
        if (!phone.matches("\\d{10}") && (legacyAccount || !phone.isEmpty())) {
            throw new IllegalArgumentException("เบอร์ติดต่อต้องเป็นตัวเลข 10 หลัก");
        }
        if (!legacyAccount) {
            if (!email.matches("[^\\s,@]+@[^\\s,@]+\\.[^\\s,@]+")) throw new IllegalArgumentException("กรุณากรอกอีเมลให้ถูกต้อง");
            if (firstName.isBlank() || lastName.isBlank()) throw new IllegalArgumentException("กรุณากรอกชื่อและนามสกุล");
        }
    }

    private static void validateCsvFields(String... fields) {
        for (String field : fields) {
            if (field.contains(",") || field.contains("\n") || field.contains("\r")) {
                throw new IllegalArgumentException("ข้อมูลสมาชิกห้ามมีเครื่องหมาย , หรือขึ้นบรรทัดใหม่");
            }
        }
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public boolean checkPassword(String input) { return password.equals(input); }

    public List<String> toCsvFields() {
        return List.of(id, username, password, address, phone, email, firstName, lastName);
    }

    public String toCsvLine() { return CsvText.encodeRow(toCsvFields()); }

    public static Member fromCsvLine(String line) { return fromCsvFields(CsvText.parseRow(line)); }

    public static Member fromCsvFields(List<String> fields) {
        if (fields.size() == 5) return new Member(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4));
        if (fields.size() != 8) throw new IllegalArgumentException("CSV สมาชิกต้องมี 5 หรือ 8 คอลัมน์");
        return new Member(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7));
    }

    @Override public boolean equals(Object other) {
        return this == other || other instanceof Member member && id.equals(member.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}
