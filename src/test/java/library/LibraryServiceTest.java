package library;

import library.domain.Book;
import library.domain.Librarian;
import library.domain.Student;
import library.exceptions.*;
        import library.repository.BookRepository;
import library.repository.UserRepository;
import library.service.LibraryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class LibraryServiceTest {

    private LibraryService library;
    private BookRepository bookRepo;
    private UserRepository userRepo;
    private Librarian librarian;
    private Student student;

//    @BeforeEach
//    void setup() {
//        bookRepo = new BookRepository();
//        userRepo = new UserRepository();
//        library = new LibraryService(
//                bookRepo,
//                userRepo,
//                Paths.get("src/test/resources/test-data/books-test.csv"),
//                Paths.get("src/test/resources/test-data/users-test.csv"),
//                Paths.get("src/test/resources/test-data/borrowed-test.csv")


    // persistence causes test results to fail. As the previous results are saved , for
    //testing we don't need persistence.

//        );
//
//        librarian = new Librarian("L1", "Libby");
//        student = new Student("S1", "Studious Sam");
//
//        userRepo.save(librarian);
//        userRepo.save(student);
//    }
    @BeforeEach
    void setup() {
        bookRepo = new BookRepository();
        userRepo = new UserRepository();
        library = new LibraryService(
                bookRepo,
                userRepo,
                null,  // disable persistence
                null,
                null
        );
        librarian = new Librarian("L1", "Libby");
        student = new Student("S1", "Studious Sam");
        userRepo.save(librarian);
        userRepo.save(student);
    }


    @Test
    void borrowLimitEnforced() {
        Book b1 = new Book("B1", "Book One", "Author", 1);
        Book b2 = new Book("B2", "Book Two", "Author", 1);
        Book b3 = new Book("B3", "Book Three", "Author", 1);
        Book b4 = new Book("B4", "Book Four", "Author", 1);

        bookRepo.save(b1);
        bookRepo.save(b2);
        bookRepo.save(b3);
        bookRepo.save(b4);

        library.borrowBook("S1", "B1");
        library.borrowBook("S1", "B2");
        library.borrowBook("S1", "B3");

        assertThrows(BookLimitExceeded.class, () -> library.borrowBook("S1", "B4"));
    }

    @Test
    void cannotBorrowSameBookTwice() {
        Book b1 = new Book("B1", "Book One", "Author", 1);
        bookRepo.save(b1);

        library.borrowBook("S1", "B1");
        assertThrows(AlreadyBorrowed.class, () -> library.borrowBook("S1", "B1"));
    }

    @Test
    void returnIncreasesAvailableCopies() {
        Book b1 = new Book("B1", "Book One", "Author", 2);
        bookRepo.save(b1);

        library.borrowBook("S1", "B1");
        assertEquals(1, bookRepo.findById("B1").get().getAvailableCopies());

        library.returnBook("S1", "B1");
        assertEquals(2, bookRepo.findById("B1").get().getAvailableCopies());
    }

    @Test
    void librarianCanAddStudentCannot() {
        Book book = new Book("B1", "Book One", "Author", 1);

        // Librarian can add
        assertDoesNotThrow(() -> library.addBook(book, librarian));

        // Student cannot add
        Book another = new Book("B2", "Book Two", "Author", 1);
        assertThrows(PermissionDeniedException.class,
                () -> library.addBook(another, student));
    }
}
