package ui;

// หน้าต่างสมัครสมาชิก

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import model.Member;
import service.MemberManager;
import ui.ScreenControls.HintTextField;

final class RegisterWindow extends JDialog {
    private final MemberManager memberService;
    private final JTextField phone = new JTextField();
    private final JTextField username = new HintTextField("ชื่อที่แสดง");
    private final JTextField email = new HintTextField("example@gmail.com");
    private final JPasswordField password = new JPasswordField();
    private final JPasswordField confirmation = new JPasswordField();
    private final JTextField firstName = new JTextField();
    private final JTextField lastName = new JTextField();
    private Member registeredMember;

    RegisterWindow(Window owner, MemberManager memberService) {
        super(owner, "สมัครสมาชิก", ModalityType.APPLICATION_MODAL);
        this.memberService = memberService;
        JPanel root = ScreenParts.accountLayout();

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints position = new GridBagConstraints();
        position.gridx = 0;
        position.gridy = 0;
        position.insets = new Insets(22, 0, 18, 0);
        JLabel title = new JLabel("สมัครสมาชิก", SwingConstants.CENTER);
        title.setFont(ScreenStyle.font(Font.BOLD, 18f));
        body.add(title, position);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints row = new GridBagConstraints();
        row.gridx = 0;
        row.gridy = 0;
        row.fill = GridBagConstraints.HORIZONTAL;
        row.weightx = 1;
        row.insets = new Insets(0, 0, 8, 0);
        form.add(ScreenParts.accountField("เบอร์ติดต่อ", phone, false, new Color(0xEEEEEE)), row);

        row.gridy++;
        JPanel displayName = ScreenParts.accountField("ชื่อที่แสดง", username, true, Color.WHITE);
        JLabel hint = new JLabel("ชื่อที่แสดง (6-32 ตัวอักษร) สามารถเปลี่ยนแปลงได้ภายหลัง");
        hint.setFont(ScreenStyle.font(Font.PLAIN, 9f));
        hint.setForeground(ScreenStyle.MUTED);
        hint.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        hint.setPreferredSize(new Dimension(194, 18));
        displayName.add(hint, BorderLayout.SOUTH);
        JPanel emailField = ScreenParts.accountField("อีเมล", email, true, Color.WHITE);

        emailField.setBorder(BorderFactory.createEmptyBorder(0, 0, 22, 0));
        form.add(pair(displayName, emailField), row);

        row.gridy++;
        form.add(pair(ScreenParts.accountField("รหัสผ่าน", password, true, Color.WHITE),
                ScreenParts.accountField("ยืนยันรหัสผ่าน", confirmation, true, Color.WHITE)), row);
        row.gridy++;
        form.add(pair(ScreenParts.accountField("ชื่อ", firstName, true, Color.WHITE),
                ScreenParts.accountField("นามสกุล", lastName, true, Color.WHITE)), row);

        JButton submit = new JButton("สมัครสมาชิก") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ScreenStyle.antialias(g2);
                g2.setColor(getModel().isRollover() || getModel().isPressed()
                        ? new Color(0x86AC89) : ScreenParts.ACCOUNT_GREEN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        submit.setContentAreaFilled(false);
        submit.setBorderPainted(false);
        submit.setForeground(Color.WHITE);
        submit.setFont(ScreenStyle.font(Font.PLAIN, 12f));
        submit.setPreferredSize(new Dimension(390, 28));
        submit.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        submit.addActionListener(event -> register());
        row.gridy++;
        row.insets = new Insets(16, 14, 0, 14);
        form.add(submit, row);
        form.setPreferredSize(new Dimension(418, 286));
        position.gridy++;
        position.insets = new Insets(0, 0, 0, 0);
        body.add(form, position);
        position.gridy++;
        position.weighty = 1;
        body.add(new JLabel(), position);
        root.add(body, BorderLayout.CENTER);

        setContentPane(root);
        getRootPane().setDefaultButton(submit);
        ScreenParts.configureDialog(this, owner, false);
    }

    Member showRegistration() {
        SwingUtilities.invokeLater(() -> phone.requestFocusInWindow());
        setVisible(true);
        return registeredMember;
    }

    private JPanel pair(JPanel left, JPanel right) {
        JPanel pair = new JPanel(new GridLayout(1, 2, 28, 0));
        pair.setOpaque(false);
        pair.add(left);
        pair.add(right);
        return pair;
    }

    private void register() {
        String id = memberService.nextMemberId();
        Member member;
        try {
            member = memberService.register(id, username.getText().trim(),
                    new String(password.getPassword()), new String(confirmation.getPassword()),
                    phone.getText().trim(), email.getText().trim(),
                    firstName.getText().trim(), lastName.getText().trim());
        } catch (IllegalArgumentException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "สมัครสมาชิกไม่สำเร็จ",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            memberService.save();
        } catch (RuntimeException error) {
            memberService.removeMember(id);
            JOptionPane.showMessageDialog(this, "บันทึกสมาชิกไม่สำเร็จ: " + error.getMessage(),
                    "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
            return;
        }
        registeredMember = member;
        dispose();
    }
}
