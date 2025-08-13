package library.domain;

public class Student extends  User{
    private static final int BORROW_LIMIT = 3;

    public Student(String name) {
        super(name);
    }

    public Student(String id, String name) {
        super(id, name);
    }

    @Override
    public int getBorrowLimit() {
        return BORROW_LIMIT;
    }

}
