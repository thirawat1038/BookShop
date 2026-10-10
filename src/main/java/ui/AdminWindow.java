package ui;

// เปิดหน้าต่างแอดมินและออกจากระบบเมื่อปิดหน้าต่าง

import app.ShopSystem;
import java.awt.Dimension;
import javax.swing.JDialog;
import javax.swing.JFrame;
import service.AdminAccounts;

final class AdminWindow {
    private AdminWindow() { }

    static void open(JFrame owner, ShopSystem application, AdminAccounts.Session session,
                     Runnable onInventoryChanged) {
        application.adminAuth.requireSession(session);
        JDialog window = new JDialog(owner, "Book Management System — " + session.username(), true);
        try {
            window.setContentPane(new AdminMenu(application, session, onInventoryChanged));
            window.setMinimumSize(new Dimension(720, 520));
            ScreenParts.configureDialog(window, owner, true);
            window.setVisible(true);
        } finally {
            application.adminAuth.logout(session);
            window.dispose();
        }
    }

}
