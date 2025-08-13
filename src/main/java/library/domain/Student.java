package library.domain;

public class Student extends  User{
    private static final int BORROW_LIMIT = 3;

    public Student(String name) {
        super(name);
    }

    @Override
    public int getBorrowLimit() {
        return BORROW_LIMIT;
    }

}
