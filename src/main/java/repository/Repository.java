package repository;

import java.util.List;

public interface Repository<T, ID> {

    List<T> findAll();

    void saveAll(List<T> items);

    T findById(ID id);

    void save(T item);

    boolean delete(ID id);
}
