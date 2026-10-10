package ui;

// สร้างปุ่ม ช่องกรอก ตาราง และรูปแบบหน้าต่างร่วมกัน

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

final class ScreenParts {
    private ScreenParts() { }

    static JPanel roundedPanel(Color background) {
        return roundedPanel(background, () -> ScreenStyle.LINE);
    }

    private static JPanel roundedPanel(Color background, Supplier<Color> borderColor) {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                ScreenStyle.antialias(g);
                g.setColor(background);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g.setColor(borderColor.get());
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        return panel;
    }

    static JButton button(String text, Color background, Color foreground) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                ScreenStyle.antialias(g);
                g.setColor(getModel().isRollover() ? background.darker() : background);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setForeground(foreground);
        button.setFont(ScreenStyle.font(Font.PLAIN, 13f));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 32));
        return button;
    }

    static JTextField input(String placeholder) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D background = (Graphics2D) graphics.create();
                ScreenStyle.antialias(background);
                background.setColor(Color.WHITE);
                background.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                background.dispose();
                super.paintComponent(graphics);
                if (getText().isEmpty()) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    ScreenStyle.antialias(g);
                    g.setFont(getFont());
                    g.setColor(new Color(0xAAAAAA));
                    TextDrawing.drawLines(g, placeholder, getFont(), 10,
                            (getHeight() - TextDrawing.lineHeight(g.getFontMetrics())) / 2, getWidth() - 20, 1, false);
                    g.dispose();
                }
            }
        };
        field.setOpaque(false);
        field.setFont(ScreenStyle.font(Font.PLAIN, 12f));
        field.setBorder(new javax.swing.border.AbstractBorder() {
            @Override public Insets getBorderInsets(Component component) { return new Insets(3, 10, 3, 10); }
            @Override public Insets getBorderInsets(Component component, Insets insets) {
                insets.set(3, 10, 3, 10);
                return insets;
            }
            @Override public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
                Graphics2D g = (Graphics2D) graphics.create();
                ScreenStyle.antialias(g);
                g.setColor(component.hasFocus() ? ScreenStyle.BLUE : ScreenStyle.LINE);
                g.drawRoundRect(x, y, width - 1, height - 1, 14, 14);
                g.dispose();
            }
        });
        field.setPreferredSize(new Dimension(160, 34));
        return field;
    }

    static JPanel labelled(String text, Component input, boolean required) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        JLabel label = new JLabel(required ? "<html>" + text + " <font color='#EE4444'>*</font></html>" : text);
        label.setFont(ScreenStyle.font(Font.BOLD, 12f));
        label.setPreferredSize(new Dimension(100, 16));
        if (input instanceof javax.swing.JComponent component) component.getAccessibleContext().setAccessibleName(text);
        panel.add(label, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    static void onChange(JTextField field, Runnable action) { onChange(field.getDocument(), action); }

    static void onChange(javax.swing.text.Document document, Runnable action) {
        document.addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent event) { action.run(); }
            @Override public void removeUpdate(DocumentEvent event) { action.run(); }
            @Override public void changedUpdate(DocumentEvent event) { action.run(); }
        });
    }

    static DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    static JTable table(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(43);
        table.setFont(ScreenStyle.font(Font.PLAIN, 12f));
        table.getTableHeader().setFont(ScreenStyle.font(Font.PLAIN, 12f));
        table.getTableHeader().setBackground(new Color(0xF0FCF8));
        table.getTableHeader().setPreferredSize(new Dimension(100, 36));
        table.getTableHeader().setDefaultRenderer((owner, value, selected, focused, row, column) -> {
            JLabel label = new JLabel(String.valueOf(value));
            label.setOpaque(true);
            label.setBackground(new Color(0xF0FCF8));
            label.setForeground(ScreenStyle.MUTED);
            label.setFont(ScreenStyle.font(Font.PLAIN, 12f));
            label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return label;
        });
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(0xEEEEEE));
        table.setSelectionBackground(new Color(0xE3F2FD));
        table.setSelectionForeground(ScreenStyle.TEXT);
        table.setFillsViewportHeight(true);
        return table;
    }

    static void addButtonColumn(JTable table, int column, IntConsumer onClick) {
        class ButtonCell extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
            private final JButton renderer = button("เลือก", ScreenStyle.BLUE, Color.WHITE);
            private final JButton editor = button("เลือก", ScreenStyle.BLUE, Color.WHITE);
            private int editingRow;
            ButtonCell() {
                editor.addActionListener(event -> {
                    int row = editingRow;
                    fireEditingStopped();
                    onClick.accept(table.convertRowIndexToModel(row));
                });
            }
            private Component padded(JButton button, Object value) {
                button.setText(String.valueOf(value));
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBackground(Color.WHITE);
                panel.setBorder(BorderFactory.createEmptyBorder(7, 5, 7, 5));
                panel.add(button);
                return panel;
            }
            @Override public Component getTableCellRendererComponent(JTable owner, Object value, boolean selected, boolean focus, int row, int col) {
                return padded(renderer, value);
            }
            @Override public Component getTableCellEditorComponent(JTable owner, Object value, boolean selected, int row, int col) {
                editingRow = row;
                return padded(editor, value);
            }
            @Override public Object getCellEditorValue() { return editor.getText(); }
        }

        table.setDefaultEditor(Object.class, null);
        ButtonCell cell = new ButtonCell();
        table.getColumnModel().getColumn(column).setCellRenderer(cell);
        table.getColumnModel().getColumn(column).setCellEditor(cell);
        table.getColumnModel().getColumn(column).setPreferredWidth(90);
        table.getColumnModel().getColumn(column).setMinWidth(70);
        table.getColumnModel().getColumn(column).setMaxWidth(75);
    }

    static final Color ACCOUNT_GREEN = new Color(0x9ABA9D);

    static JLabel accountHeader() {
        JLabel header = new JLabel("Online Book Ordering System", SwingConstants.CENTER);
        header.setOpaque(true);
        header.setBackground(Color.WHITE);
        header.setFont(ScreenStyle.font(Font.BOLD, 14f));
        header.setPreferredSize(new Dimension(660, 44));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ScreenStyle.LINE));
        return header;
    }

    static JPanel accountFooter() {
        JPanel footer = new JPanel();
        footer.setBackground(Color.WHITE);
        footer.setPreferredSize(new Dimension(660, 28));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ScreenStyle.LINE));
        return footer;
    }

    static JPanel accountField(String text, JTextField input, boolean required, Color background) {
        if (input instanceof javax.swing.JPasswordField password) password.setEchoChar('*');
        input.getAccessibleContext().setAccessibleName(text);
        input.setFont(ScreenStyle.font(Font.PLAIN, 12f));
        input.setOpaque(false);
        input.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        JPanel box = roundedPanel(background, () -> input.hasFocus() ? ACCOUNT_GREEN : ScreenStyle.LINE);
        box.setBorder(null);
        box.setLayout(new BorderLayout());
        box.setPreferredSize(new Dimension(194, 27));
        box.add(input);
        input.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent event) { box.repaint(); }
            @Override public void focusLost(FocusEvent event) { box.repaint(); }
        });
        return labelled(text, box, required);
    }

    static JPanel accountLayout() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ScreenStyle.PAGE_BG);
        panel.setPreferredSize(new Dimension(660, 460));
        panel.add(accountHeader(), BorderLayout.NORTH);
        panel.add(accountFooter(), BorderLayout.SOUTH);
        return panel;
    }

    static void configureDialog(JDialog dialog, Component owner, boolean resizable) {
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "close");
        dialog.getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) { dialog.dispose(); }
        });
        dialog.pack();
        dialog.setResizable(resizable);
        dialog.setLocationRelativeTo(owner);
    }
}
