package library.exceptions;

public class BookLimitExceeded extends RuntimeException{
    public BookLimitExceeded(String message) {
        super(message);
    }
}
