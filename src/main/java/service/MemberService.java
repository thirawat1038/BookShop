package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import model.Member;
import repository.MemberRepository;

public class MemberService {

    private final List<Member> members = new ArrayList<>();
    private final MemberRepository repository;

    // MemberServiceใช้เทสต์ 
    public MemberService() {
        this.repository = null;
        checkRep();
    }

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

    public void save() {
        if (repository != null) {
            repository.saveAll(members);
        }
    }

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