package service;

// อ่านบัญชีแอดมินและตรวจสิทธิ์

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import repository.CsvFiles;
import util.CsvText;

public final class AdminAccounts {
    private final Path accountFile;
    private final Set<Session> sessions = Collections.newSetFromMap(new IdentityHashMap<>());

    private record Account(String username, String password) { }

    public static final class Session {
        private final String username;
        private final String password;
        private Session(Account account) {
            username = account.username();
            password = account.password();
        }
        public String username() { return username; }
    }

    public AdminAccounts(Path accountFile) { this.accountFile = accountFile; }

    public boolean isConfigured() { return !readAccounts().isEmpty(); }

    public boolean isAdminUsername(String username) {
        return readAccounts().stream().anyMatch(account -> account.username().equals(username));
    }

    // อ่านไฟล์ใหม่ทุกครั้ง ให้การแก้รหัสผ่านและยกเลิกสิทธิ์มีผลทันที
    private List<Account> readAccounts() {
        if (!Files.exists(accountFile)) return List.of();
        try {
            String content = Files.readString(accountFile, StandardCharsets.UTF_8);
            if (content.startsWith("\uFEFF")) content = content.substring(1);
            List<List<String>> rows = CsvText.decode(content);
            if (rows.isEmpty() || !rows.get(0).equals(List.of("username", "password"))) {
                throw new IllegalStateException("หัวตารางใน admin.csv ต้องเป็น username,password");
            }
            List<Account> accounts = new ArrayList<>();
            Set<String> usernames = new HashSet<>();
            for (int index = 1; index < rows.size(); index++) {
                List<String> row = rows.get(index);
                if (row.size() != 2 || !validUsername(row.get(0)) || row.get(1).isBlank()) {
                    throw new IllegalStateException("ข้อมูลแอดมินบรรทัดที่ " + (index + 1) + " ไม่ถูกต้อง");
                }
                if (!usernames.add(row.get(0))) throw new IllegalStateException("ชื่อผู้ใช้แอดมินซ้ำใน admin.csv");
                accounts.add(new Account(row.get(0), row.get(1)));
            }
            return accounts;
        } catch (IOException | IllegalArgumentException error) {
            throw new IllegalStateException("อ่าน admin.csv ไม่สำเร็จ กรุณาตรวจรูปแบบไฟล์", error);
        }
    }

    public Session setup(String username, char[] password, char[] confirmation) {
        if (isConfigured()) throw new IllegalStateException("ตั้งค่าบัญชีแอดมินแล้ว กรุณาเข้าสู่ระบบ");
        if (!validUsername(username)) {
            throw new IllegalArgumentException("ชื่อแอดมินต้องเป็นอักษรอังกฤษ ตัวเลข หรือ _ . - จำนวน 3-32 ตัวอักษร");
        }
        if (password == null || password.length < 8) throw new IllegalArgumentException("รหัสผ่านแอดมินต้องมีอย่างน้อย 8 ตัวอักษร");
        if (new String(password).isBlank()) throw new IllegalArgumentException("รหัสผ่านแอดมินห้ามว่าง");
        if (!java.util.Arrays.equals(password, confirmation)) throw new IllegalArgumentException("รหัสผ่านไม่ตรงกัน");
        Account account = new Account(username, new String(password));
        CsvFiles.writeRows(accountFile, List.of("username", "password"), List.of(List.of(account.username(), account.password())));
        return openSession(account);
    }

    public Session login(String username, char[] password) {
        if (username != null && password != null) {
            String secret = new String(password);
            for (Account account : readAccounts()) {
                if (account.username().equals(username) && account.password().equals(secret)) return openSession(account);
            }
        }
        throw new SecurityException("ชื่อผู้ใช้หรือรหัสผ่านแอดมินไม่ถูกต้อง");
    }

    public void requireSession(Session session) {
        if (session != null && sessions.contains(session)) {
            boolean unchanged = readAccounts().stream().anyMatch(account -> account.username().equals(session.username)
                    && account.password().equals(session.password));
            if (unchanged) return;
            sessions.remove(session);
        }
        throw new SecurityException("กรุณาเข้าสู่ระบบแอดมินก่อน หรือบัญชีถูกแก้ไขใน admin.csv");
    }

    public void logout(Session session) { sessions.remove(session); }

    private Session openSession(Account account) {
        Session session = new Session(account);
        sessions.add(session);
        return session;
    }

    private static boolean validUsername(String username) {
        return username != null && username.matches("[A-Za-z0-9_.-]{3,32}");
    }
}
