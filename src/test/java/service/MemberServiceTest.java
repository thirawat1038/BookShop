package service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import model.Member;
import repository.MemberRepository;

public class MemberServiceTest {

    public static void main(String[] args) throws IOException {
        MemberServiceTest test = new MemberServiceTest();

        test.testRegisterValid();
        test.testRegisterPasswordMismatchThrows();
        test.testRegisterNullConfirmThrows();
        test.testRegisterInvalidMemberThrows();
        test.testRegisterDuplicateIdThrows();
        test.testRegisterDuplicateUsernameThrows();

        test.testLoginSuccess();
        test.testLoginWrongPassword();
        test.testLoginUnknownUsername();
        test.testLoginNullInputs();

        test.testNextMemberIdEmpty();
        test.testNextMemberIdContinuesFromMax();
        test.testNextMemberIdIgnoresOtherFormats();

        test.testGetMemberNotFound();
        test.testRemoveMember();
        test.testGetAllMembersIsReadOnly();

        test.testSaveWithoutRepositoryDoesNothing();
        test.testNullRepositoryThrows();
        test.testSaveAndReloadFromFile();
        test.testUnsavedRegisterIsNotPersisted();

        System.out.println("ทดสอบผ่านทั้งหมด");
    }

    private static MemberService serviceWithAlice() {
        MemberService service = new MemberService();
        service.register("m01", "alice01", "pw1234", "pw1234", "Bangkok", "0811111111");
        return service;
    }

    // ---------- register ----------

    void testRegisterValid() {
        MemberService service = new MemberService();
        Member m = service.register("m01", "alice01", "pw1234", "pw1234", "Bangkok", "0811111111");
        assertEquals("m01", m.getId());
        assertEquals(1, service.getAllMembers().size());
    }

    void testRegisterPasswordMismatchThrows() {
        MemberService service = new MemberService();
        assertThrows(() -> service.register("m01", "alice01", "pw1234", "other", "Bangkok", "0811111111"));
        assertEquals(0, service.getAllMembers().size());
    }

    void testRegisterNullConfirmThrows() {
        MemberService service = new MemberService();
        assertThrows(() -> service.register("m01", "alice01", "pw1234", null, "Bangkok", "0811111111"));
    }

    void testRegisterInvalidMemberThrows() {
        MemberService service = new MemberService();
        // username สั้นเกินไป
        assertThrows(() -> service.register("m01", "abc", "pw1234", "pw1234", "Bangkok", "0811111111"));
        // เบอร์ไม่ครบ 10 หลัก
        assertThrows(() -> service.register("m01", "alice01", "pw1234", "pw1234", "Bangkok", "081"));
        assertEquals(0, service.getAllMembers().size());
    }

    void testRegisterDuplicateIdThrows() {
        MemberService service = serviceWithAlice();
        assertThrows(() -> service.register("m01", "bobby02", "pw5678", "pw5678", "Nonthaburi", "0822222222"));
        assertEquals(1, service.getAllMembers().size());
    }

    void testRegisterDuplicateUsernameThrows() {
        MemberService service = serviceWithAlice();
        assertThrows(() -> service.register("m02", "alice01", "pw5678", "pw5678", "Nonthaburi", "0822222222"));
        assertEquals(1, service.getAllMembers().size());
    }

    // ---------- login ----------

    void testLoginSuccess() {
        Optional<Member> result = serviceWithAlice().login("alice01", "pw1234");
        assertTrue(result.isPresent(), "ควร login สำเร็จ");
        assertEquals("m01", result.get().getId());
    }

    void testLoginWrongPassword() {
        assertTrue(serviceWithAlice().login("alice01", "wrong").isEmpty(), "รหัสผ่านผิดต้อง login ไม่ได้");
    }

    void testLoginUnknownUsername() {
        assertTrue(serviceWithAlice().login("nobody", "pw1234").isEmpty(), "ไม่มี username นี้");
    }

    void testLoginNullInputs() {
        MemberService service = serviceWithAlice();
        assertTrue(service.login(null, "pw1234").isEmpty(), "username null");
        assertTrue(service.login("alice01", null).isEmpty(), "password null");
    }

    // ---------- nextMemberId ----------

    void testNextMemberIdEmpty() {
        assertEquals("m01", new MemberService().nextMemberId());
    }

    void testNextMemberIdContinuesFromMax() {
        MemberService service = serviceWithAlice(); // m01
        service.register("m05", "bobby02", "pw5678", "pw5678", "Nonthaburi", "0822222222");
        assertEquals("m06", service.nextMemberId());
        // รหัสที่ได้ต้องสมัครได้จริง
        service.register(service.nextMemberId(), "carol03", "pw0000", "pw0000", "Phuket", "0833333333");
        assertEquals(3, service.getAllMembers().size());
    }

    void testNextMemberIdIgnoresOtherFormats() {
        MemberService service = new MemberService();
        service.register("admin", "adminuser", "pw1234", "pw1234", "Bangkok", "0811111111");
        assertEquals("m01", service.nextMemberId());
    }

    // ---------- get / remove ----------

    void testGetMemberNotFound() {
        MemberService service = serviceWithAlice();
        assertTrue(service.getMemberById("m99").isEmpty(), "ไม่มี id นี้");
        assertTrue(service.getMemberByUsername("nobody").isEmpty(), "ไม่มี username นี้");
    }

    void testRemoveMember() {
        MemberService service = serviceWithAlice();
        service.removeMember("m01");
        assertEquals(0, service.getAllMembers().size());
        service.removeMember("m01"); // ลบซ้ำต้องไม่พัง
    }

    void testGetAllMembersIsReadOnly() {
        MemberService service = serviceWithAlice();
        assertThrows(() -> service.getAllMembers().clear());
    }

    // ---------- repository ----------

    void testSaveWithoutRepositoryDoesNothing() {
        serviceWithAlice().save();
    }

    void testNullRepositoryThrows() {
        assertThrows(() -> new MemberService(null));
    }

    void testSaveAndReloadFromFile() throws IOException {
        Path dir = Files.createTempDirectory("memberservice-test");
        Path file = dir.resolve("member.csv");
        try {
            MemberService first = new MemberService(new MemberRepository(file.toString()));
            first.register("m01", "alice01", "pw1234", "pw1234", "Bangkok", "0811111111");
            first.save();

            MemberService second = new MemberService(new MemberRepository(file.toString()));
            assertEquals(1, second.getAllMembers().size());
            assertTrue(second.login("alice01", "pw1234").isPresent(), "สมาชิกที่บันทึกไว้ต้อง login ได้หลังโหลดใหม่");
            assertThrows(() -> second.register("m02", "alice01", "x", "x", "Bangkok", "0822222222"));
        } finally {
            Files.deleteIfExists(file);
            Files.deleteIfExists(dir);
        }
    }

    void testUnsavedRegisterIsNotPersisted() throws IOException {
        Path dir = Files.createTempDirectory("memberservice-test");
        Path file = dir.resolve("member.csv");
        try {
            MemberService first = new MemberService(new MemberRepository(file.toString()));
            first.register("m01", "alice01", "pw1234", "pw1234", "Bangkok", "0811111111");
            // ไม่เรียก save()

            MemberService second = new MemberService(new MemberRepository(file.toString()));
            assertEquals(0, second.getAllMembers().size());
        } finally {
            Files.deleteIfExists(file);
            Files.deleteIfExists(dir);
        }
    }

    // ---------- helper ----------

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertThrows(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException expected) {
            return;
        }
        throw new AssertionError("ควร throw exception");
    }
}