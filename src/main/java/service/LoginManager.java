package service;

// เข้าสู่ระบบสมาชิกหรือแอดมิน

import model.Member;

public final class LoginManager {
    private final MemberManager members;
    private final AdminAccounts administrators;

    public record Result(Member member, AdminAccounts.Session administrator) {
        public Result {
            if ((member == null) == (administrator == null)) {
                throw new IllegalArgumentException("ต้องระบุบัญชีสมาชิกหรือแอดมินเพียงหนึ่งบัญชี");
            }
        }
    }

    public LoginManager(MemberManager members, AdminAccounts administrators) {
        this.members = members;
        this.administrators = administrators;
    }

    public Result login(String username, char[] password) {
        if (username == null || password == null) throw invalidCredentials();
        String accountName = username.trim();
        if (administrators.isAdminUsername(accountName)) {
            return new Result(null, administrators.login(accountName, password));
        }
        Member member = members.login(accountName, new String(password)).orElseThrow(LoginManager::invalidCredentials);
        return new Result(member, null);
    }

    private static SecurityException invalidCredentials() {
        return new SecurityException("ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
    }
}
