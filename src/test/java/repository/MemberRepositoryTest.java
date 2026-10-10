package repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import model.Member;

public class MemberRepositoryTest {

    private Path tempDir;
    private Path file;

    public static void main(String[] args) throws IOException {
        MemberRepositoryTest test = new MemberRepositoryTest();

        test.run("testConstructorBlankPathThrows", test::testConstructorBlankPathThrows);
        test.run("testFindAllMissingFileReturnsEmpty", test::testFindAllMissingFileReturnsEmpty);
        test.run("testSaveAllThenFindAll", test::testSaveAllThenFindAll);
        test.run("testFindById", test::testFindById);
        test.run("testFindByUsername", test::testFindByUsername);
        test.run("testSaveNewMemberAppends", test::testSaveNewMemberAppends);
        test.run("testSaveExistingIdReplaces", test::testSaveExistingIdReplaces);
        test.run("testDelete", test::testDelete);
        test.run("testHeaderOnlyFileReturnsEmpty", test::testHeaderOnlyFileReturnsEmpty);
        test.run("testInvalidLineThrowsWithLineNumber", test::testInvalidLineThrowsWithLineNumber);
        test.run("testSixColumnLineIsRejected", test::testSixColumnLineIsRejected);

        System.out.println("ทดสอบผ่านทั้งหมด");
    }

    // ---------- helper ----------

    private interface TestCase {
        void run() throws Exception;
    }

    private void run(String name, TestCase testCase) throws IOException {
        tempDir = Files.createTempDirectory("memberrepo-test");
        file = tempDir.resolve("member.csv");
        try {
            testCase.run();
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionError(name + " โยน exception ที่ไม่คาดคิด: " + e, e);
        } finally {
            Files.deleteIfExists(file);
            Files.deleteIfExists(tempDir);
        }
    }

    private static Member member(String id, String username, String password, String address, String phone) {
        return new Member(id, username, password, address, phone);
    }

    private static Member alice() {
        return member("m01", "alice01", "pw1234", "Bangkok", "0811111111");
    }

    private static Member bobby() {
        return member("m02", "bobby02", "pw5678", "Nonthaburi", "0822222222");
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

    private static void assertNull(Object actual) {
        if (actual != null) {
            throw new AssertionError("expected null but was <" + actual + ">");
        }
    }

    // ---------- tests ----------

    void testConstructorBlankPathThrows() {
        try {
            new MemberRepository(" ");
        } catch (IllegalArgumentException e) {
            return;
        }
        throw new AssertionError("ควร throw IllegalArgumentException");
    }

    void testFindAllMissingFileReturnsEmpty() {
        MemberRepository repo = new MemberRepository(file.toString());
        assertTrue(repo.findAll().isEmpty(), "ไฟล์ไม่มีอยู่ ควรได้ list ว่าง");
    }

    void testSaveAllThenFindAll() {
        MemberRepository repo = new MemberRepository(file.toString());
        repo.saveAll(List.of(alice(), bobby()));

        List<Member> loaded = new MemberRepository(file.toString()).findAll();
        assertEquals(2, loaded.size());
        assertEquals("m01", loaded.get(0).getId());
        assertEquals("bobby02", loaded.get(1).getUsername());
        assertTrue(loaded.get(0).checkPassword("pw1234"), "รหัสผ่านต้องอ่านกลับมาตรงเดิม");
    }

    void testFindById() {
        MemberRepository repo = new MemberRepository(file.toString());
        repo.saveAll(List.of(alice(), bobby()));

        assertEquals("alice01", repo.findById("m01").getUsername());
        assertNull(repo.findById("m99"));
        assertNull(repo.findById(null));
    }

    void testFindByUsername() {
        MemberRepository repo = new MemberRepository(file.toString());
        repo.saveAll(List.of(alice(), bobby()));

        assertEquals("m02", repo.findByUsername("bobby02").getId());
        assertNull(repo.findByUsername("ALICE01"));
        assertNull(repo.findByUsername("nobody"));
        assertNull(repo.findByUsername(null));
    }

    void testSaveNewMemberAppends() {
        MemberRepository repo = new MemberRepository(file.toString());
        repo.save(alice());
        repo.save(bobby());

        assertEquals(2, repo.findAll().size());
    }

    void testSaveExistingIdReplaces() {
        MemberRepository repo = new MemberRepository(file.toString());
        repo.saveAll(List.of(alice(), bobby()));

        repo.save(member("m01", "alice01", "newpass", "Chiang Mai", "0899999999"));

        List<Member> loaded = repo.findAll();
        assertEquals(2, loaded.size());
        assertEquals("Chiang Mai", repo.findById("m01").getAddress());
        assertTrue(repo.findById("m01").checkPassword("newpass"), "ต้องได้รหัสผ่านใหม่");
    }

    void testDelete() {
        MemberRepository repo = new MemberRepository(file.toString());
        repo.saveAll(List.of(alice(), bobby()));

        assertTrue(repo.delete("m01"), "ลบ m01 ควรสำเร็จ");
        assertTrue(!repo.delete("m01"), "ลบซ้ำควรได้ false");
        assertTrue(!repo.delete(null), "ลบ null ควรได้ false");
        assertEquals(1, repo.findAll().size());
        assertNull(repo.findById("m01"));
    }

    void testHeaderOnlyFileReturnsEmpty() throws IOException {
        Files.writeString(file, "id,username,password,address,phone\n", StandardCharsets.UTF_8);
        assertTrue(new MemberRepository(file.toString()).findAll().isEmpty(), "มีแต่ header ควรได้ list ว่าง");
    }

    void testInvalidLineThrowsWithLineNumber() throws IOException {
        Files.writeString(file,
                "id,username,password,address,phone\n"
                        + "m01,alice01,pw1234,Bangkok,0811111111\n"
                        + "m02,short,secret99,Bangkok,0822222222\n",
                StandardCharsets.UTF_8);
        try {
            new MemberRepository(file.toString()).findAll();
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("บรรทัดที่ 3"), "ควรบอกเลขบรรทัดที่ผิด: " + e.getMessage());
            assertTrue(!e.getMessage().contains("secret99"), "ห้ามเอารหัสผ่านไปโผล่ในข้อความ error");
            return;
        }
        throw new AssertionError("ควร throw RuntimeException");
    }

    void testSixColumnLineIsRejected() throws IOException {
        Files.writeString(file,
                "id,username,password,address,phone\n"
                        + "m01,alice01,pw1234,Alice,Bangkok,0811111111\n",
                StandardCharsets.UTF_8);
        try {
            new MemberRepository(file.toString()).findAll();
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("บรรทัดที่ 2"), "ควรบอกเลขบรรทัดที่ผิด");
            return;
        }
        throw new AssertionError("แถว 6 คอลัมน์ควรถูกปฏิเสธ");
    }
}