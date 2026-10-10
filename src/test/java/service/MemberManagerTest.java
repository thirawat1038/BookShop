package service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import model.Member;
import repository.MemberFile;

public class MemberManagerTest {
    public static void main(String[] args) throws IOException {
        MemberManagerTest test = new MemberManagerTest();

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

    private static MemberManager serviceWithAlice() {
        MemberManager service = new MemberManager();
        service.register("m01", "alice01", "pw1234", "pw1234", "Bangkok", "0811111111");
        return service;
    }

    void testRegisterValid() {
        MemberManager service = new MemberManager();
        Member m = service.register("m01", "alice01", "pw1234", "pw1234", "Bangkok", "0811111111");
        assertEquals("m01", m.getId());
        assertEquals(1, service.getAllMembers().size());
    }

    void testRegisterPasswordMismatchThrows() {
        MemberManager service = new MemberManager();
        assertThrows(() -> service.register("m01", "alice01", "pw1234", "other", "Bangkok", "0811111111"));
        assertEquals(0, service.getAllMembers().size());
    }

    void testRegisterNullConfirmThrows() {
        MemberManager service = new MemberManager();
        assertThrows(() -> service.register("m01", "alice01", "pw1234", null, "Bangkok", "0811111111"));
    }

    void testRegisterInvalidMemberThrows() {
        MemberManager service = new MemberManager();

        assertThrows(() -> service.register("m01", "abc", "pw1234", "pw1234", "Bangkok", "0811111111"));

        assertThrows(() -> service.register("m01", "alice01", "pw1234", "pw1234", "Bangkok", "081"));
        assertEquals(0, service.getAllMembers().size());
    }

    void testRegisterDuplicateIdThrows() {
        MemberManager service = serviceWithAlice();
        assertThrows(() -> service.register("m01", "bobby02", "pw5678", "pw5678", "Nonthaburi", "0822222222"));
        assertEquals(1, service.getAllMembers().size());
    }

    void testRegisterDuplicateUsernameThrows() {
        MemberManager service = serviceWithAlice();
        assertThrows(() -> service.register("m02", "alice01", "pw5678", "pw5678", "Nonthaburi", "0822222222"));
        assertEquals(1, service.getAllMembers().size());
    }

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
        MemberManager service = serviceWithAlice();
        assertTrue(service.login(null, "pw1234").isEmpty(), "username null");
        assertTrue(service.login("alice01", null).isEmpty(), "password null");
    }

    void testNextMemberIdEmpty() {
        assertEquals("m01", new MemberManager().nextMemberId());
    }

    void testNextMemberIdContinuesFromMax() {
        MemberManager service = serviceWithAlice();
        service.register("m05", "bobby02", "pw5678", "pw5678", "Nonthaburi", "0822222222");
        assertEquals("m06", service.nextMemberId());

        service.register(service.nextMemberId(), "carol03", "pw0000", "pw0000", "Phuket", "0833333333");
        assertEquals(3, service.getAllMembers().size());
    }

    void testNextMemberIdIgnoresOtherFormats() {
        MemberManager service = new MemberManager();
        service.register("admin", "adminuser", "pw1234", "pw1234", "Bangkok", "0811111111");
        assertEquals("m01", service.nextMemberId());
    }

    void testGetMemberNotFound() {
        MemberManager service = serviceWithAlice();
        assertTrue(service.getMemberById("m99").isEmpty(), "ไม่มี id นี้");
        assertTrue(service.getMemberByUsername("nobody").isEmpty(), "ไม่มี username นี้");
    }

    void testRemoveMember() {
        MemberManager service = serviceWithAlice();
        service.removeMember("m01");
        assertEquals(0, service.getAllMembers().size());
        service.removeMember("m01");
    }

    void testGetAllMembersIsReadOnly() {
        MemberManager service = serviceWithAlice();
        assertThrows(() -> service.getAllMembers().clear());
    }

    void testSaveWithoutRepositoryDoesNothing() {
        serviceWithAlice().save();
    }

    void testNullRepositoryThrows() {
        assertThrows(() -> new MemberManager(null));
    }

    void testSaveAndReloadFromFile() throws IOException {
        Path dir = Files.createTempDirectory("memberservice-test");
        Path file = dir.resolve("members.csv");
        try {
            MemberManager first = new MemberManager(new MemberFile(file.toString()));
            first.register("m01", "alice01", "pw1234", "pw1234", "Bangkok", "0811111111");
            first.save();

            MemberManager second = new MemberManager(new MemberFile(file.toString()));
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
        Path file = dir.resolve("members.csv");
        try {
            MemberManager first = new MemberManager(new MemberFile(file.toString()));
            first.register("m01", "alice01", "pw1234", "pw1234", "Bangkok", "0811111111");

            MemberManager second = new MemberManager(new MemberFile(file.toString()));
            assertEquals(0, second.getAllMembers().size());
        } finally {
            Files.deleteIfExists(file);
            Files.deleteIfExists(dir);
        }
    }

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
