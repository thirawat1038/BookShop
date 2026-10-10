package model;

public class MemberTest {
    public static void main(String[] args) {
        MemberTest test = new MemberTest();

        test.testConstructorValid();

        test.testIdNullThrows();
        test.testIdBlankThrows();

        test.testUsernameTooShortThrows();
        test.testUsernameMinBoundaryOk();
        test.testUsernameMaxBoundaryOk();
        test.testUsernameTooLongThrows();
        test.testUsernameNullThrows();

        test.testPasswordNullThrows();
        test.testPasswordBlankThrows();

        test.testAddressNullThrows();
        test.testAddressBlankThrows();
        test.testAddressWithCommaThrows();

        test.testPhoneNineDigitsThrows();
        test.testPhoneElevenDigitsThrows();
        test.testPhoneWithLettersThrows();
        test.testPhoneWithDashThrows();
        test.testPhoneNullThrows();

        test.testCheckPasswordCorrect();
        test.testCheckPasswordWrong();
        test.testCheckPasswordNullIsFalse();

        test.testEqualsSameId();
        test.testEqualsDifferentId();
        test.testHashCodeConsistentWithEquals();

        test.testCsvRoundTrip();
        test.testFromCsvLineWrongColumnCountThrows();

        System.out.println("ทดสอบผ่านทั้งหมด");
    }
    private Member valid() {
        return new Member("M001", "somchai01", "1234", "123 Sukhumvit Bangkok", "0812345678");
    }

    void testConstructorValid() {
        Member m = valid();
        assertEquals("M001", m.getId());
        assertEquals("somchai01", m.getUsername());
        assertEquals("123 Sukhumvit Bangkok", m.getAddress());
        assertEquals("0812345678", m.getPhone());
    }

    void testIdNullThrows() {
        try {
            new Member(null, "somchai01", "1234", "addr", "0812345678");
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testIdBlankThrows() {
        try {
            new Member("  ", "somchai01", "1234", "addr", "0812345678");
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testUsernameTooShortThrows() {
        try {
            new Member("M001", "abcde", "1234", "addr", "0812345678");
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testUsernameMinBoundaryOk() {
        Member m = new Member("M001", "abcdef", "1234", "addr", "0812345678");
        assertEquals("abcdef", m.getUsername());
    }

    void testUsernameMaxBoundaryOk() {
        String name32 = "a".repeat(32);
        Member m = new Member("M001", name32, "1234", "addr", "0812345678");
        assertEquals(name32, m.getUsername());
    }

    void testUsernameTooLongThrows() {
        try {
            new Member("M001", "a".repeat(33), "1234", "addr", "0812345678");
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testUsernameNullThrows() {
        try {
            new Member("M001", null, "1234", "addr", "0812345678");
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testPasswordNullThrows() {
        try {
            new Member("M001", "somchai01", null, "addr", "0812345678");
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testPasswordBlankThrows() {
        try {
            new Member("M001", "somchai01", "   ", "addr", "0812345678");
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testAddressNullThrows() {
        try {
            new Member("M001", "somchai01", "1234", null, "0812345678");
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testAddressBlankThrows() {
        try {
            new Member("M001", "somchai01", "1234", "", "0812345678");
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testAddressWithCommaThrows() {
        try {
            new Member("M001", "somchai01", "1234", "123 Sukhumvit, Bangkok", "0812345678");
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testPhoneNineDigitsThrows() {
        try {
            new Member("M001", "somchai01", "1234", "addr", "081234567");
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testPhoneElevenDigitsThrows() {
        try {
            new Member("M001", "somchai01", "1234", "addr", "08123456789");
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testPhoneWithLettersThrows() {
        try {
            new Member("M001", "somchai01", "1234", "addr", "08123abcde");
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testPhoneWithDashThrows() {
        try {
            new Member("M001", "somchai01", "1234", "addr", "081-234-5678");
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testPhoneNullThrows() {
        try {
            new Member("M001", "somchai01", "1234", "addr", null);
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    void testCheckPasswordCorrect() {
        assertEquals(true, valid().checkPassword("1234"));
    }

    void testCheckPasswordWrong() {
        assertEquals(false, valid().checkPassword("9999"));
    }

    void testCheckPasswordNullIsFalse() {
        assertEquals(false, valid().checkPassword(null));
    }

    void testEqualsSameId() {
        Member a = new Member("M001", "somchai01", "1234", "addr A", "0812345678");
        Member b = new Member("M001", "somying99", "abcd", "addr B", "0898765432");
        assertEquals(true, a.equals(b));
    }

    void testEqualsDifferentId() {
        Member a = new Member("M001", "somchai01", "1234", "addr", "0812345678");
        Member b = new Member("M002", "somchai01", "1234", "addr", "0812345678");
        assertEquals(false, a.equals(b));
    }

    void testHashCodeConsistentWithEquals() {
        Member a = new Member("M001", "somchai01", "1234", "addr A", "0812345678");
        Member b = new Member("M001", "somying99", "abcd", "addr B", "0898765432");
        assertEquals(a.hashCode(), b.hashCode());
    }

    void testCsvRoundTrip() {
        Member original = valid();
        Member restored = Member.fromCsvLine(original.toCsvLine());
        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getUsername(), restored.getUsername());
        assertEquals(true, restored.checkPassword("1234"));
        assertEquals(original.getAddress(), restored.getAddress());
        assertEquals(original.getPhone(), restored.getPhone());
    }

    void testFromCsvLineWrongColumnCountThrows() {
        try {
            Member.fromCsvLine("M001,somchai01,1234,addr");
            throw new AssertionError("ควร throw IllegalArgumentException แต่ไม่ throw");
        } catch (IllegalArgumentException e) {
        }
    }

    private void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected: " + expected + " actual is: " + actual);
        }
    }

    private void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Expected: " + expected + " actual is: " + actual);
        }
    }

    private void assertEquals(boolean expected, boolean actual) {
        if (expected != actual) {
            throw new AssertionError("Expected: " + expected + " actual is: " + actual);
        }
    }
}
