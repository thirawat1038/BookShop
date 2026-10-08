package repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import model.Member;

public class MemberRepository implements Repository<Member, String> {

    private static final String HEADER = "id,username,password,address,phone";

    private final Path filePath;

    public MemberRepository(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath ห้ามเป็น null หรือว่าง");
        }
        this.filePath = Paths.get(filePath);
    }

    @Override
    public List<Member> findAll() {
        List<Member> members = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return members;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1) {
                    // ข้าม header บรรทัดแรกเสมอ
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }
                try {
                    members.add(Member.fromCsvLine(line));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("ข้อมูลสมาชิกในไฟล์ " + filePath
                            + " บรรทัดที่ " + lineNumber + " ไม่ถูกต้อง: " + e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("อ่านไฟล์ " + filePath + " ไม่สำเร็จ: " + e.getMessage(), e);
        }

        return members;
    }

    @Override
    public void saveAll(List<Member> items) {
        if (items == null) {
            throw new IllegalArgumentException("items ห้ามเป็น null");
        }

        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                writer.write(HEADER);
                writer.newLine();
                for (Member member : items) {
                    writer.write(member.toCsvLine());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("เขียนไฟล์ " + filePath + " ไม่สำเร็จ: " + e.getMessage(), e);
        }
    }

    @Override
    public Member findById(String id) {
        if (id == null) {
            return null;
        }
        for (Member member : findAll()) {
            if (member.getId().equals(id)) {
                return member;
            }
        }
        return null;
    }

    public Member findByUsername(String username) {
        if (username == null) {
            return null;
        }
        for (Member member : findAll()) {
            if (member.getUsername().equals(username)) {
                return member;
            }
        }
        return null;
    }

    @Override
    public void save(Member item) {
        if (item == null) {
            throw new IllegalArgumentException("item ห้ามเป็น null");
        }
        List<Member> items = findAll();
        boolean replaced = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            items.add(item);
        }
        saveAll(items);
    }

    @Override
    public boolean delete(String id) {
        if (id == null) {
            return false;
        }
        List<Member> items = findAll();
        boolean removed = items.removeIf(m -> m.getId().equals(id));
        if (removed) {
            saveAll(items);
        }
        return removed;
    }
}
