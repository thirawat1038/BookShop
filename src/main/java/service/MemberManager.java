package service;

// สมัครและจัดการสมาชิก

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import model.Member;
import repository.MemberFile;

public class MemberManager {
    private final List<Member> members = new ArrayList<>();
    private final MemberFile repository;

    public MemberManager() { repository = null; }

    public MemberManager(MemberFile repository) {
        if (repository == null) throw new IllegalArgumentException("repository ห้ามเป็น null");
        this.repository = repository;
        members.addAll(repository.findAll());
        Set<String> ids = new HashSet<>();
        Set<String> usernames = new HashSet<>();
        for (Member member : members) {
            if (!ids.add(member.getId()) || !usernames.add(member.getUsername())) {
                throw new IllegalStateException("สมาชิกซ้ำ: " + member.getUsername());
            }
        }
    }

    public void save() { if (repository != null) repository.saveAll(members); }

    public Member register(String id, String username, String password, String confirmation, String address, String phone) {
        validateConfirmation(password, confirmation);
        return addMember(new Member(id, username, password, address, phone));
    }

    public Member register(String id, String username, String password, String confirmation,
                           String phone, String email, String firstName, String lastName) {
        validateConfirmation(password, confirmation);
        if (email == null || email.isBlank()) throw new IllegalArgumentException("กรุณากรอกอีเมล");
        return addMember(new Member(id, username, password, "", phone, email, firstName, lastName));
    }

    private static void validateConfirmation(String password, String confirmation) {
        if (confirmation == null || !confirmation.equals(password)) throw new IllegalArgumentException("รหัสผ่านไม่ตรงกัน");
    }

    private Member addMember(Member member) {
        if (getMemberById(member.getId()).isPresent()) throw new IllegalArgumentException("รหัสสมาชิกซ้ำ: " + member.getId());
        if (getMemberByUsername(member.getUsername()).isPresent()) throw new IllegalArgumentException("ชื่อที่แสดงซ้ำ: " + member.getUsername());
        members.add(member);
        return member;
    }

    public Optional<Member> login(String username, String password) {
        if (username == null || password == null) return Optional.empty();
        return getMemberByUsername(username).filter(member -> member.checkPassword(password));
    }

    public String nextMemberId() {
        int highest = members.stream().map(Member::getId).filter(id -> id.matches("[mM]\\d{1,9}"))
                .mapToInt(id -> Integer.parseInt(id.substring(1))).max().orElse(0);
        return String.format(java.util.Locale.ROOT, "m%02d", highest + 1);
    }

    public Optional<Member> getMemberById(String id) {
        return members.stream().filter(member -> member.getId().equals(id)).findFirst();
    }

    public Optional<Member> getMemberByUsername(String username) {
        return members.stream().filter(member -> member.getUsername().equals(username)).findFirst();
    }

    public void removeMember(String id) { members.removeIf(member -> member.getId().equals(id)); }
    public List<Member> getAllMembers() { return Collections.unmodifiableList(members); }
}
