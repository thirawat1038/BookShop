package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import model.Member;
import repository.MemberRepository;

/**
 * AF(members, repository)
 * MemberService จัดการ logic เรื่องสมาชิก: สมัคร, เข้าสู่ระบบ, ค้นหา, ลบ
 * โดย members    = รายการสมาชิกทั้งหมดที่โหลดไว้ในหน่วยความจำ
 *     repository = ตัวอ่าน/เขียนไฟล์ member.csv (เป็น null ได้ = ทำงานในหน่วยความจำอย่างเดียว)
 *
 * การเปลี่ยนแปลง (register/removeMember) จะยังไม่ลงไฟล์จนกว่าจะเรียก save()
 * เหมือนกับ ProductService
 *
 * RI:
 *  - members ห้ามเป็น null และห้ามมีสมาชิกเป็น null
 *  - id ของสมาชิกแต่ละคนห้ามซ้ำกัน
 *  - username ของสมาชิกแต่ละคนห้ามซ้ำกัน
 */
public class MemberService {

    private final List<Member> members = new ArrayList<>();
    private final MemberRepository repository;

    /** สร้าง MemberService แบบไม่ผูกไฟล์ (เริ่มว่าง ใช้ในเทสต์) */
    public MemberService() {
        this.repository = null;
        checkRep();
    }

    /**
     * สร้าง MemberService ที่โหลดสมาชิกจากไฟล์ผ่าน repository
     *
     * @param repository ห้ามเป็น null
     */
    public MemberService(MemberRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("repository ห้ามเป็น null");
        }
        this.repository = repository;
        members.addAll(repository.findAll());
        checkRep();
    }

    private void checkRep() {
        if (members == null) {
            throw new RuntimeException("RI violated: members เป็น null");
        }
        for (int i = 0; i < members.size(); i++) {
            Member m = members.get(i);
            if (m == null) {
                throw new RuntimeException("RI violated: สมาชิกเป็น null ที่ตำแหน่ง " + i);
            }
            for (int j = i + 1; j < members.size(); j++) {
                if (m.getId().equals(members.get(j).getId())) {
                    throw new RuntimeException("RI violated: id ซ้ำ (" + m.getId() + ")");
                }
                if (m.getUsername().equals(members.get(j).getUsername())) {
                    throw new RuntimeException("RI violated: username ซ้ำ (" + m.getUsername() + ")");
                }
            }
        }
    }

    /** บันทึกสมาชิกทั้งหมดลงไฟล์ (ไม่ทำอะไรถ้าสร้างแบบไม่ผูกไฟล์) */
    public void save() {
        if (repository != null) {
            repository.saveAll(members);
        }
    }

    /**
     * สมัครสมาชิกใหม่
     *
     * @throws IllegalArgumentException ถ้ารหัสผ่านไม่ตรงกัน, ข้อมูลผิดเงื่อนไขของ Member,
     *                                  หรือ id / username ซ้ำกับที่มีอยู่
     */
    public Member register(String id, String username, String password,
                           String confirmPassword, String address, String phone) {
        if (confirmPassword == null || !confirmPassword.equals(password)) {
            throw new IllegalArgumentException("รหัสผ่านไม่ตรง");
        }
        Member member = new Member(id, username, password, address, phone);
        if (getMemberById(id).isPresent()) {
            throw new IllegalArgumentException("มีสมาชิกรหัส " + id + " อยู่แล้ว");
        }
        if (getMemberByUsername(username).isPresent()) {
            throw new IllegalArgumentException("มีผู้ใช้ชื่อ " + username + " อยู่แล้ว");
        }
        members.add(member);
        checkRep();
        return member;
    }

    /**
     * เข้าสู่ระบบ
     *
     * @return สมาชิกที่ username และรหัสผ่านถูกต้อง หรือ Optional.empty() ถ้าไม่ถูก (หรือส่ง null มา)
     */
    public Optional<Member> login(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        Optional<Member> found = getMemberByUsername(username);
        if (found.isPresent() && found.get().checkPassword(password)) {
            return found;
        }
        return Optional.empty();
    }

    /**
     * สร้างรหัสสมาชิกใหม่ที่ยังไม่ซ้ำ ต่อจากเลขสูงสุดของรหัสรูปแบบ m + ตัวเลข เช่น m01, m02 -> m03
     * (รหัสที่ไม่ตรงรูปแบบนี้จะถูกข้าม ถ้ายังไม่มีสมาชิกเลยจะได้ m01)
     *
     * @return รหัสสมาชิกใหม่ที่ไม่ซ้ำกับสมาชิกคนใดในระบบ
     */
    public String nextMemberId() {
        int max = 0;
        for (Member member : members) {
            String id = member.getId();
            if (id.matches("[mM]\\d{1,9}")) {
                max = Math.max(max, Integer.parseInt(id.substring(1)));
            }
        }
        int next = max + 1;
        String candidate = String.format("m%02d", next);
        while (getMemberById(candidate).isPresent()) {
            next++;
            candidate = String.format("m%02d", next);
        }
        return candidate;
    }

    public Optional<Member> getMemberById(String id) {
        for (Member member : members) {
            if (member.getId().equals(id)) {
                return Optional.of(member);
            }
        }
        return Optional.empty();
    }

    public Optional<Member> getMemberByUsername(String username) {
        for (Member member : members) {
            if (member.getUsername().equals(username)) {
                return Optional.of(member);
            }
        }
        return Optional.empty();
    }

    public void removeMember(String id) {
        members.removeIf(member -> member.getId().equals(id));
        checkRep();
    }

    public List<Member> getAllMembers() {
        return Collections.unmodifiableList(members);
    }
}