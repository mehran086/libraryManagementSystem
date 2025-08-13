package library.domain;

public class Librarian extends User{

    public Librarian(String name) {
        super(name);
    }

    public Librarian(String id, String name) {
        super(id, name);
    }

    @Override
    public int getBorrowLimit() {
        return Integer.MAX_VALUE;    // librarian has no limit
    }
}
