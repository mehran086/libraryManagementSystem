package library.repository;

import library.domain.Book;

public class BookRepository extends AbstractRepository<Book,String>{
    @Override
    protected String getId(Book item) {
        return item.getId();
    }
}
