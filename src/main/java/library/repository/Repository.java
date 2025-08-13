package library.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface Repository<T,ID> {
    void save(T item);
    Optional<T> findById(ID id);
    List<T> findAll();
    void deleteById(ID id);
    List<T> search(Predicate<T> filter);
}
