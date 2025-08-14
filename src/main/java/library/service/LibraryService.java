package library.service;

import library.domain.Book;
import library.domain.Librarian;
import library.domain.User;
import library.exceptions.*;
import library.repository.BookRepository;
import library.repository.UserRepository;
import library.util.FileUtil;

import java.nio.file.Path;
import java.util.*;

public class LibraryService {
    private final BookRepository bookrepo;
    public final UserRepository userRepo;
    private final Path booksFile;
    private final Path usersFile;
    private final Path borrowedFile;

    // Tracks borrowed books: key = userId, value = set of bookIds
    private final Map<String, Set<String>> trackBooks = new HashMap<>();

    public LibraryService(BookRepository bookrepo, UserRepository userRepo,
                          Path booksFile, Path usersFile, Path borrowedFile) {
        this.bookrepo = bookrepo;
        this.userRepo = userRepo;
        this.booksFile = booksFile;
        this.usersFile = usersFile;
        this.borrowedFile = borrowedFile;

        // Load existing data
        FileUtil.loadBooks(booksFile).forEach(bookrepo::save);
        FileUtil.loadUsers(usersFile).forEach(userRepo::save);
        trackBooks.putAll(FileUtil.loadBorrowedBooks(borrowedFile));
    }

    // ---------------- Librarian Functions ----------------

    public void addBook(Book b1, User l1) {
        if (!(l1 instanceof Librarian)) {
            throw new PermissionDeniedException("Only a librarian can add books");
        }
        bookrepo.save(b1);
        FileUtil.saveBooks(bookrepo.findAll(), booksFile);
    }

    public void addUser(User user, User addedBy) {
        if (!(addedBy instanceof Librarian)) {
            throw new PermissionDeniedException("Only a librarian can add users");
        }
        userRepo.save(user);
        FileUtil.saveUsers(userRepo.findAll(), usersFile);
    }

    // ---------------- Student Functions ----------------

    public List<Book> listAllBooks() {
        return bookrepo.findAll();
    }
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public List<Book> searchByName(String bookname) {
        String searchTerm = bookname.toLowerCase();
        return bookrepo.search(book -> book.getTitle().toLowerCase().contains(searchTerm));
    }

    public synchronized void borrowBook(String userId, String bookId) {
        // user should exist
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFound("User not found"));
        if (user instanceof Librarian) {
            throw new PermissionDeniedException("Only a student can borrow books");
        }

        // book should exist
        Book book = bookrepo.findById(bookId)
                .orElseThrow(() -> new NotFound("Book not found"));

        // initialize tracking for user
        trackBooks.putIfAbsent(userId, new HashSet<>());

        // already borrowed?
        if (trackBooks.get(userId).contains(bookId)) {
            throw new AlreadyBorrowed("Book is already borrowed by the user");
        }

        // limit check
        if (trackBooks.get(userId).size() >= user.getBorrowLimit()) {
            throw new BookLimitExceeded("The user has already reached borrow limit");
        }

        // availability check
        if (book.getAvailableCopies() <= 0) {
            throw new BookNotAvailableException("The book is currently unavailable");
        }

        // borrow
        trackBooks.get(userId).add(bookId);
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookrepo.save(book);

        // save to files
        FileUtil.saveBooks(bookrepo.findAll(), booksFile);
        FileUtil.saveBorrowedBooks(trackBooks, borrowedFile);
        userRepo.save(user);
        FileUtil.saveUsers(userRepo.findAll(), usersFile);
    }

    public synchronized void returnBook(String userId, String bookId) {
        if (!trackBooks.containsKey(userId) || !trackBooks.get(userId).contains(bookId)) {
            throw new NotFound("User cannot return this book as they didn't borrow it");
        }

        Book book = bookrepo.findById(bookId)
                .orElseThrow(() -> new NotFound("Book not found"));

        book.setAvailableCopies(book.getAvailableCopies() + 1);
        trackBooks.get(userId).remove(bookId);
        bookrepo.save(book);

        // save changes
        FileUtil.saveBooks(bookrepo.findAll(), booksFile);
        FileUtil.saveBorrowedBooks(trackBooks, borrowedFile);
    }

    public void printBooksOfStudent(String userId) {
        Set<String> bookIds = trackBooks.get(userId);
        if (bookIds == null || bookIds.isEmpty()) {
            System.out.println("No books borrowed by this user.");
            return;
        }

        bookIds.stream()
                .map(bookrepo::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(System.out::println);
    }


}
