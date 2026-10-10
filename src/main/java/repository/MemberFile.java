package repository;

// อ่านและบันทึกไฟล์สมาชิก

import java.util.ArrayList;
import java.util.List;
import model.Member;

public class MemberFile extends DataStore.Csv<Member> {
    private static final List<String> HEADER = List.of("id", "username", "password", "address", "phone",
            "email", "firstName", "lastName");

    public MemberFile(String filePath) { super(filePath); }

    @Override protected String idOf(Member member) { return member.getId(); }

    @Override public List<Member> findAll() {
        List<Member> members = new ArrayList<>();
        int rowNumber = 1;
        for (List<String> row : CsvFiles.readRows(filePath)) {
            rowNumber++;
            try {
                members.add(Member.fromCsvFields(row));
            } catch (IllegalArgumentException error) {
                throw new IllegalStateException("ข้อมูลสมาชิกในไฟล์ " + filePath + " บรรทัดที่ " + rowNumber + " ไม่ถูกต้อง: " + error.getMessage(), error);
            }
        }
        return members;
    }

    @Override public void saveAll(List<Member> members) {
        if (members == null) throw new IllegalArgumentException("members ห้ามเป็น null");
        CsvFiles.writeRows(filePath, HEADER, members.stream().map(Member::toCsvFields).toList());
    }

    public Member findByUsername(String username) {
        return findAll().stream().filter(member -> member.getUsername().equals(username)).findFirst().orElse(null);
    }
}
