package library;

import library.cli.LibraryCli;
import library.domain.Book;
import library.domain.Librarian;
import library.domain.Student;
import library.domain.User;
import library.repository.BookRepository;
import library.repository.UserRepository;
import library.service.BackupService;
import library.service.LibraryService;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        Path booksFile = Path.of("src/main/resources/data/books.csv");
        Path usersFile = Path.of("src/main/resources/data/users.csv");
        Path borrowedFile = Path.of("src/main/resources/data/BorrowedBooks.csv");

        BookRepository bookRepo = new BookRepository();
        UserRepository userRepo = new UserRepository();
        LibraryService library = new LibraryService(bookRepo, userRepo, booksFile, usersFile, borrowedFile);
        BackupService backupService = new BackupService(library);
//        backupService.start();

        // Bootstrap default librarian if no users exist
        if (userRepo.findAll().isEmpty()) {
            User defaultLib = new Librarian("admin", "Default Librarian");
            library.addUser(defaultLib, defaultLib);
            System.out.println("📢 Created default librarian: ID=admin, Name=Default Librarian");
            System.out.println("   Login with: login admin");
        }

        new LibraryCli(library, userRepo,backupService).start();
    }
}

