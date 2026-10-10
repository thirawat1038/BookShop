package app;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ui.ScreenStyle;
import ui.ShopWindow;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ScreenStyle.install();
            try {
                new ShopWindow(new ShopSystem(new DataFiles())).setVisible(true);
            } catch (RuntimeException error) {
                JOptionPane.showMessageDialog(null, "เปิดโปรแกรมไม่สำเร็จ: " + error.getMessage(),
                        "BookShop", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
