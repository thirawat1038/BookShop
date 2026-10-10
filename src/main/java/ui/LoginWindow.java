package ui;

// หน้าต่างเข้าสู่ระบบ

import app.ShopSystem;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import model.Member;
import service.LoginManager;
import ui.ScreenControls.HintTextField;
import ui.ScreenControls.LinkButton;

final class LoginWindow extends JDialog {
    private final ShopSystem application;
    private final JTextField username = new HintTextField("Username");
    private final JPasswordField password = new JPasswordField();
    private LoginManager.Result result;

    LoginWindow(JFrame owner, ShopSystem application) {
        super(owner, "เข้าสู่ระบบ", true);
        this.application = application;
        JPanel root = ScreenParts.accountLayout();
        root.add(buildBody(), BorderLayout.CENTER);
        setContentPane(root);
        ScreenParts.configureDialog(this, owner, false);
    }

    static LoginManager.Result show(JFrame owner, ShopSystem application) {
        LoginWindow dialog = new LoginWindow(owner, application);
        SwingUtilities.invokeLater(() -> dialog.username.requestFocusInWindow());
        try {
            dialog.setVisible(true);
            return dialog.result;
        } finally {
            dialog.password.setText("");
            dialog.dispose();
        }
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints layout = new GridBagConstraints();
        layout.gridx = 0;
        layout.gridy = 0;
        layout.insets = new Insets(22, 0, 24, 0);
        JLabel title = new JLabel("เข้าสู่ระบบ", SwingConstants.CENTER);
        title.setFont(ScreenStyle.font(Font.BOLD, 18f));
        body.add(title, layout);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(418, 210));
        GridBagConstraints row = new GridBagConstraints();
        row.gridx = 0;
        row.gridy = 0;
        row.weightx = 1;
        row.fill = GridBagConstraints.HORIZONTAL;
        row.insets = new Insets(0, 0, 18, 0);
        form.add(ScreenParts.accountField("Username", username, false, Color.WHITE), row);
        row.gridy++;
        form.add(ScreenParts.accountField("Password", password, false, Color.WHITE), row);
        JButton submit = ScreenParts.button("เข้าสู่ระบบ", ScreenParts.ACCOUNT_GREEN, Color.WHITE);
        submit.setFont(ScreenStyle.font(Font.PLAIN, 12f));
        submit.setPreferredSize(new Dimension(390, 28));
        submit.addActionListener(event -> login());
        getRootPane().setDefaultButton(submit);
        row.gridy++;
        row.insets = new Insets(8, 14, 0, 14);
        form.add(submit, row);
        LinkButton register = new LinkButton("สมัครสมาชิก");
        register.setFont(ScreenStyle.font(Font.PLAIN, 12f));
        register.addActionListener(event -> register());
        row.gridy++;
        row.fill = GridBagConstraints.NONE;
        row.insets = new Insets(8, 0, 0, 0);
        form.add(register, row);
        layout.gridy++;
        layout.insets = new Insets(0, 0, 0, 0);
        body.add(form, layout);
        layout.gridy++;
        layout.weighty = 1;
        body.add(new JLabel(), layout);
        return body;
    }

    private void login() {
        char[] secret = password.getPassword();
        try {
            result = application.login.login(username.getText(), secret);
            dispose();
        } catch (RuntimeException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "เข้าสู่ระบบไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
            password.setText("");
            password.requestFocusInWindow();
        } finally { Arrays.fill(secret, '\0'); }
    }

    private void register() {
        Member member = new RegisterWindow(this, application.members).showRegistration();
        if (member == null) return;
        result = new LoginManager.Result(member, null);
        JOptionPane.showMessageDialog(this, "สมัครสมาชิกสำเร็จ รหัสสมาชิกของคุณคือ " + member.getId(), "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
