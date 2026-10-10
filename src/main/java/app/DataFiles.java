package app;

// ค้นหาตำแหน่งไฟล์ข้อมูลของร้าน

import java.nio.file.Files;
import java.nio.file.Path;

public final class DataFiles {
    private final Path projectDirectory;
    private final Path dataDirectory;

    public DataFiles() {
        projectDirectory = findProjectDirectory();
        String configured = System.getProperty("bookshop.dataDir");
        dataDirectory = configured == null ? projectDirectory.resolve("data") : Path.of(configured).toAbsolutePath();
    }

    public DataFiles(Path dataDirectory) {
        this.dataDirectory = dataDirectory.toAbsolutePath();
        this.projectDirectory = this.dataDirectory.getParent();
    }

    public Path products() { return dataDirectory.resolve("books.csv"); }
    public Path orders() { return dataDirectory.resolve("orders.csv"); }
    public Path members() { return dataDirectory.resolve("members.csv"); }
    public Path adminAccount() { return dataDirectory.resolve("admin.csv"); }
    public Path asset(String relativePath) { return projectDirectory.resolve(relativePath); }

    private static Path findProjectDirectory() {
        Path current = Path.of("").toAbsolutePath();
        if (Files.isDirectory(current.resolve("data"))) return current;
        Path nested = current.resolve("BookShop-combineVersion");
        if (Files.isDirectory(nested.resolve("data"))) return nested;
        try {
            Path location = Path.of(DataFiles.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            for (Path parent = location; parent != null; parent = parent.getParent()) {
                if (Files.isDirectory(parent.resolve("data"))) return parent;
            }
        } catch (java.net.URISyntaxException error) {
            throw new IllegalStateException("ไม่พบโฟลเดอร์โปรแกรม", error);
        }
        throw new IllegalStateException("ไม่พบโฟลเดอร์ data กรุณาเปิดโปรเจกต์ BookShop-combineVersion");
    }
}
