package repository;

// คำสั่งอ่านและบันทึกข้อมูลที่ใช้ร่วมกัน

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public interface DataStore<T, ID> {
    List<T> findAll();

    void saveAll(List<T> items);

    T findById(ID id);

    void save(T item);

    boolean delete(ID id);

    abstract class Csv<T> implements DataStore<T, String> {
        protected final Path filePath;

        Csv(String filePath) {
            if (filePath == null || filePath.isBlank()) {
                throw new IllegalArgumentException("filePath ห้ามเป็น null หรือว่าง");
            }
            this.filePath = Path.of(filePath);
        }

        public Path getFilePath() { return filePath; }

        protected abstract String idOf(T item);

        @Override
        public T findById(String id) {
            return findAll().stream().filter(item -> idOf(item).equals(id)).findFirst().orElse(null);
        }

        @Override
        public void save(T item) {
            if (item == null) throw new IllegalArgumentException("item ห้ามเป็น null");
            List<T> items = new ArrayList<>(findAll());
            int index = -1;
            for (int position = 0; position < items.size(); position++) {
                if (idOf(items.get(position)).equals(idOf(item))) { index = position; break; }
            }
            if (index < 0) items.add(item);
            else items.set(index, item);
            saveAll(items);
        }

        @Override
        public boolean delete(String id) {
            List<T> items = new ArrayList<>(findAll());
            boolean removed = items.removeIf(item -> idOf(item).equals(id));
            if (removed) saveAll(items);
            return removed;
        }
    }
}
