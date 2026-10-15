package ui;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;
import java.util.Locale;

/**
 * ค่าสี/ฟอนต์กลางของ GUI และตัวแก้ปัญหาตัวอักษรไทยเพี้ยน
 *
 * สาเหตุที่ภาษาไทยเพี้ยนใน Swing: Look and Feel ของระบบมักเลือกฟอนต์ที่ไม่มีกลุ่มอักษรไทย
 * (เช่น Segoe UI / Dialog) ทำให้ Java ไปดึงฟอนต์สำรองมาผสมเป็นบางตัว
 * ผลคือวรรณยุกต์/สระบนล่างซ้อนกันผิดตำแหน่ง หรือสูงต่ำไม่เท่ากัน
 * วิธีแก้: เลือก "ฟอนต์ที่แสดงภาษาไทยได้จริง" แล้วบังคับใช้กับทุก component + เปิด anti-alias
 */
final class Theme {

    private Theme() {
    }

    // ---------- สี ----------
    static final Color PAGE_BG = new Color(0xF8F8F8);
    static final Color WHITE = Color.WHITE;
    static final Color LINE = new Color(0xD0D0D0);
    static final Color TEXT = new Color(0x111111);
    static final Color MUTED = new Color(0x8A8A8A);
    static final Color BLUE = new Color(0x3AABF0);
    static final Color BLUE_DARK = new Color(0x2A92D6);
    static final Color LOGIN_BG = new Color(0xD9BCC1);
    static final Color LOGIN_BG_HOVER = new Color(0xCDA8AE);

    /** ฟอนต์ที่เรียงตามลำดับความชอบ (Windows / macOS / Linux) */
    private static final String[] PREFERRED = {
            "Leelawadee UI", "Tahoma", "Noto Sans Thai", "Noto Sans Thai UI",
            "Sarabun", "TH Sarabun New", "Thonburi", "Ayuthaya",
            "Segoe UI", "Loma", "Garuda", "Norasi", "FreeSans", "Dialog"
    };

    private static final String THAI_SAMPLE = "กขคงจฉชซญฎ ก็ก่ก้ก๊ก๋ กิกีกึกื กุกู ภาษาไทย ำ";

    private static String family = "Dialog";

    /** เรียกก่อนสร้างหน้าต่างใด ๆ */
    static void install() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // ใช้ look and feel เริ่มต้นแทน
        }

        family = pickThaiFamily();

        // บังคับฟอนต์ไทยให้ทุก component (ปุ่ม ตาราง dialog ฯลฯ)
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                Font old = (Font) value;
                int style = old.getStyle() & ~Font.ITALIC;
                UIManager.put(key, new FontUIResource(new Font(family, style, 14)));
            }
        }
    }

    static String family() {
        return family;
    }

    static Font font(int style, float size) {
        return new Font(family, style, 1).deriveFont(style, size);
    }

    private static String pickThaiFamily() {
        String[] installed = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(Locale.ENGLISH);
        java.util.Set<String> set = new java.util.HashSet<>();
        for (String name : installed) {
            set.add(name.toLowerCase(Locale.ROOT));
        }
        for (String candidate : PREFERRED) {
            if (candidate.equals("Dialog") || set.contains(candidate.toLowerCase(Locale.ROOT))) {
                if (supportsThai(candidate)) {
                    return candidate;
                }
            }
        }
        // ไม่เจอในรายการ -> ไล่หาฟอนต์ใดก็ได้ในเครื่องที่แสดงภาษาไทยได้ครบ
        for (String name : installed) {
            if (supportsThai(name)) {
                return name;
            }
        }
        return "Dialog";
    }

    private static boolean supportsThai(String name) {
        return new Font(name, Font.PLAIN, 14).canDisplayUpTo(THAI_SAMPLE) == -1;
    }

    static void antialias(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }
}
