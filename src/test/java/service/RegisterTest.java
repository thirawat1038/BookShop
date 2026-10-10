package service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import model.Member;
import repository.MemberFile;

public class RegisterTest {
    public static void main(String[] args) throws Exception {
        Path file = Files.createTempFile("registration-profile-", ".csv");
        try {
            Files.writeString(file, "id,username,password,address,phone\n"
                    + "m01,legacy01,password,Bangkok,0812345678\n", StandardCharsets.UTF_8);
            MemberManager service = new MemberManager(new MemberFile(file.toString()));
            service.register("m02", "reader01", "secret", "secret", "",
                    "reader@example.com", "สมชาย", "ใจดี");
            service.save();
            MemberManager reloaded = new MemberManager(new MemberFile(file.toString()));
            Member member = reloaded.login("reader01", "secret").orElseThrow();
            require(member.getEmail().equals("reader@example.com"));
            require(member.getFirstName().equals("สมชาย"));
            require(member.getLastName().equals("ใจดี"));
            require(member.getPhone().isEmpty());
            require(reloaded.login("legacy01", "password").isPresent());
            require(reloaded.getMemberById("m01").orElseThrow().getAddress().equals("Bangkok"));
            require(reloaded.nextMemberId().equals("m03"));
            expectInvalid(() -> reloaded.register("m03", "reader02", "secret", "wrong", "",
                    "reader2@example.com", "ชื่อ", "นามสกุล"));
            expectInvalid(() -> reloaded.register("m03", "reader02", "secret", "secret", "",
                    "invalid-email", "ชื่อ", "นามสกุล"));
            expectInvalid(() -> reloaded.register("m03", "reader02", "secret", "secret", "",
                    "", "ชื่อ", "นามสกุล"));
            expectInvalid(() -> reloaded.register("m03", "reader02", "secret", "secret", "123",
                    "reader2@example.com", "ชื่อ", "นามสกุล"));
            expectInvalid(() -> reloaded.register("m03", "reader02", "secret", "secret", "",
                    "reader2@example.com", "", "นามสกุล"));
            expectInvalid(() -> reloaded.register("m03", "reader02", "secret", "secret", "",
                    "reader2@example.com", "ชื่อ,อื่น", "นามสกุล"));
            expectInvalid(() -> reloaded.register("m03", "reader02", "secret", "secret", "",
                    "reader2@example.com", "ชื่อ\nอื่น", "นามสกุล"));
            expectInvalid(() -> reloaded.register("m03", "reader01", "secret", "secret", "",
                    "reader2@example.com", "ชื่อ", "นามสกุล"));
            require(reloaded.getAllMembers().size() == 2);
            reloaded.register("m03", "reader02", "secret", "secret", "0898765432",
                    "reader2@example.com", "ชื่อ", "นามสกุล");
            reloaded.save();
            require(new MemberManager(new MemberFile(file.toString()))
                    .getMemberById("m03").orElseThrow().getPhone().equals("0898765432"));
            System.out.println("Registration profile checks passed");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void require(boolean condition) {
        if (!condition) throw new AssertionError();
    }

    private static void expectInvalid(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected invalid registration to be rejected");
    }
}
