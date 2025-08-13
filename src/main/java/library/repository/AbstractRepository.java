package library.repository;

import java.util.*;
import java.util.function.Predicate;

public abstract class AbstractRepository<T,ID> implements Repository<T,ID>{
  final  Map<ID,T> store = new HashMap<>();
  // keeping public methods as synchronized so that only one thread can access it at a time.
    @Override
    public synchronized void save(T item) {
        store.put(getId(item),item);

    }

    @Override
    public synchronized Optional<T> findById(ID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public synchronized List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public synchronized void deleteById(ID id) {
            store.remove(id);
    }

    @Override
    public synchronized  List<T> search(Predicate<T> filter) {
        List<T> result = new ArrayList<>();
        for (T item : store.values()) {
            if (filter.test(item)) {
                result.add(item);
            }
        }
        return result;
    }
 abstract protected ID getId(T item);
}
