package repository;

// บันทึก CSV และคืนข้อมูลเมื่อบันทึกไม่สำเร็จ

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import util.CsvText;

public final class CsvFiles {
    private CsvFiles() { }

    public static List<List<String>> readRows(Path file) {
        if (!Files.exists(file)) return List.of();
        try {
            List<List<String>> rows = CsvText.decode(Files.readString(file, StandardCharsets.UTF_8));
            return rows.isEmpty() ? List.of() : rows.subList(1, rows.size());
        } catch (IOException error) {
            throw new IllegalStateException("อ่านไฟล์ " + file + " ไม่สำเร็จ", error);
        }
    }

    public static void writeRows(Path file, List<String> header, List<List<String>> rows) {
        StringBuilder content = new StringBuilder(CsvText.encodeRow(header)).append('\n');
        rows.forEach(row -> content.append(CsvText.encodeRow(row)).append('\n'));
        writeBytes(file, content.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void writeBytes(Path file, byte[] bytes) {
        Path target = file.toAbsolutePath();
        Path temporary = null;
        try {
            Files.createDirectories(target.getParent());
            temporary = Files.createTempFile(target.getParent(), ".bookshop-", ".tmp");
            Files.write(temporary, bytes);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException error) {
            throw new IllegalStateException("บันทึกไฟล์ " + file + " ไม่สำเร็จ", error);
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
            }
        }
    }

    // คืนค่าไฟล์เดิมเมื่อการบันทึกออเดอร์หรือสต๊อกล้มเหลว
    public static <T> T transaction(List<Path> files, Supplier<T> action) {
        Map<Path, byte[]> backups = new LinkedHashMap<>();
        try {
            for (Path file : files) backups.put(file, Files.exists(file) ? Files.readAllBytes(file) : null);
        } catch (IOException error) {
            throw new IllegalStateException("เตรียมการบันทึกไม่สำเร็จ", error);
        }
        try {
            return action.get();
        } catch (RuntimeException error) {
            backups.forEach((file, bytes) -> {
                try {
                    if (bytes == null) Files.deleteIfExists(file);
                    else writeBytes(file, bytes);
                } catch (IOException | RuntimeException restoreError) {
                    error.addSuppressed(restoreError);
                }
            });
            throw error;
        }
    }
}
