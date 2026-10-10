package util;

// แปลงข้อความ CSV รองรับจุลภาคและหลายบรรทัด

import java.util.ArrayList;
import java.util.List;

public final class CsvText {
    private CsvText() { }

    public static List<String> parseRow(String content) {
        if (content == null) throw new IllegalArgumentException("CSV row ห้ามเป็น null");
        List<List<String>> rows = decode(content);
        if (rows.size() != 1) throw new IllegalArgumentException("ต้องเป็นข้อมูล CSV หนึ่งรายการ");
        return rows.get(0);
    }

    public static String encodeRow(List<String> fields) {
        return fields.stream().map(value -> {
            if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                    || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
                return '"' + value.replace("\"", "\"\"") + '"';
            }
            return value;
        }).collect(java.util.stream.Collectors.joining(","));
    }

    public static List<List<String>> decode(String content) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < content.length(); index++) {
            char character = content.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < content.length() && content.charAt(index + 1) == '"') {
                    field.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                row.add(field.toString());
                field.setLength(0);
            } else if ((character == '\n' || character == '\r') && !quoted) {
                if (character == '\r' && index + 1 < content.length() && content.charAt(index + 1) == '\n') index++;
                finishRow(rows, row, field);
                row = new ArrayList<>();
            } else {
                field.append(character);
            }
        }
        if (quoted) throw new IllegalArgumentException("CSV มีเครื่องหมายคำพูดไม่ครบคู่");
        if (!row.isEmpty() || !field.isEmpty()) finishRow(rows, row, field);
        return rows;
    }

    private static void finishRow(List<List<String>> rows, List<String> row, StringBuilder field) {
        row.add(field.toString());
        field.setLength(0);
        if (row.size() != 1 || !row.get(0).isBlank()) rows.add(row);
    }
}
