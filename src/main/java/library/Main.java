
//
//import library.domain.Book;
//import library.domain.Librarian;
//import library.domain.Student;
//import library.domain.User;
//import library.repository.BookRepository;
//import library.repository.UserRepository;
//import library.service.LibraryService;
//
//import java.nio.file.Path;
//
////TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
//// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
//public class Main {
//    private static final Path BOOKS_PATH = Path.of("src/main/resources/data/books.csv");
//    private static final Path USERS_PATH = Path.of("src/main/resources/data/users.csv");
//
//    public static void main(String[] args) {
////        Book b1 = new Book("Effective Java", "Joshua Bloch", 5);
////        Book b2 = new Book("Clean Code", "Robert C. Martin", 3);
////
////        Student s1 = new Student("Alice");
////        Librarian l1 = new Librarian("Bob");
////
////        System.out.println(b1);
////        System.out.println(b2);
////        System.out.println(s1.getId() + " - " + s1.getName() + " limit: " + s1.getBorrowLimit());
////        System.out.println(l1.getId() + " - " + l1.getName() + " limit: " + l1.getBorrowLimit());
//
//        // test 2
//
////        BookRepository repo = new BookRepository();
////        Book b1 = new Book("Java 101", "John Doe", 5);
////        Book b2 = new Book("Spring Boot", "Jane Smith", 3);
////
////        repo.save(b1);
////        repo.save(b2);
////
////        System.out.println(repo.findAll()); // should list both
////        System.out.println(repo.search(b -> b.getTitle().contains("10"))); // only Java 101
////        UserRepository userRepo = new UserRepository();
////        Student s1 = new Student("Alice");
////        Student s2 = new Student("Bob");
////        Librarian l1 = new Librarian("Charlie");
////
////        userRepo.save(s1);
////        userRepo.save(s2);
////        userRepo.save(l1);
////
////        System.out.println("\nAll users:");
////        System.out.println(userRepo.findAll());
////        System.out.println("Users with name starting with 'A':");
////        System.out.println(userRepo.search(u -> u.getName().startsWith("A")));
////
////        System.out.println("Find by ID (Charlie):");
////        System.out.println(userRepo.findById(l1.getId()).orElse(null));
//
//        // test 3
//        BookRepository bookRepository = new BookRepository();
//        UserRepository userRepository = new UserRepository();
//        Book b1 = new Book("Java 101", "John Doe", 5);
//        Book b2 = new Book("Spring Boot", "Jane Smith", 3);
//        Book b3 = new Book("Spring Boot", "Jane Smith", 3);
//        Book b4 = new Book("Effective Java", "Joshua Bloch", 5);
//        bookRepository.save(b1);
//        bookRepository.save(b2);
//        bookRepository.save(b3);
//        bookRepository.save(b4);
////        System.out.println(bookRepository.findAll());
//        bookRepository.findAll().forEach(System.out::println);
//
//        Student s1 = new Student("Alice");
//        Student s2 = new Student("Bob");
//        userRepository.save(s1);
//        userRepository.save(s2);
//        userRepository.findAll().forEach(System.out::println);
//        LibraryService libraryService = new LibraryService(bookRepository,userRepository);
//        Librarian l1= new Librarian("wajahat");
//        userRepository.save(l1);
////        libraryService.addBook(b1,s1);
//        libraryService.addBook(b1,l1);
////        bookRepository.findAll().forEach(System.out::println);
//            libraryService.listAllBooks().forEach(System.out::println);
//            libraryService.borrowBook("USR-2001","BK-1000");
////        System.out.println( bookRepository.findById("BK-1000").get());
//        libraryService.printBooksOfStudent("USR-2001");
//    }
//}
package library;

import library.domain.Book;
import library.domain.Librarian;
import library.domain.Student;
import library.domain.User;
import library.repository.BookRepository;
import library.repository.UserRepository;
import library.service.LibraryService;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        Path booksFile = Path.of("src/main/resources/data/books.csv");
        Path usersFile = Path.of("src/main/resources/data/users.csv");
        Path borrowedFile = Path.of("src/main/resources/data/BorrowedBooks.csv");

        // Initialize repositories
        BookRepository bookRepo = new BookRepository();
        UserRepository userRepo = new UserRepository();

        // Create library service
        LibraryService library = new LibraryService(bookRepo, userRepo, booksFile, usersFile, borrowedFile);

        // Create librarian and student
        User librarian = new Librarian("LIB-1", "Jane Smith");
        User student = new Student("STU-1", "John Doe");

        // Add them to the system
        try {
            library.addUser(librarian, librarian);
            library.addUser(student, librarian);
        } catch (Exception e) {
            System.out.println("User already exists: " + e.getMessage());
        }

        // Add books
        try {
            library.addBook(new Book("BK-1", "The Java Handbook", "Patrick Naughton", 5), librarian);
            library.addBook(new Book("BK-2", "Effective Java", "Joshua Bloch", 3), librarian);
        } catch (Exception e) {
            System.out.println("Book already exists: " + e.getMessage());
        }

        // List books
        System.out.println("\n--- All Books ---");
        library.listAllBooks().forEach(System.out::println);

        // Search
        System.out.println("\n--- Search for 'Java' ---");
        library.searchByName("Java").forEach(System.out::println);

        // Borrow a book
        System.out.println("\nBorrowing BK-1 for student...");
        try {
            library.borrowBook("STU-1", "BK-1");
        } catch (Exception e) {
            System.out.println("Borrow failed: " + e.getMessage());
        }

        // Print books borrowed by student
        System.out.println("\n--- Books borrowed by STU-1 ---");
        library.printBooksOfStudent("STU-1");

        // Return a book
//        System.out.println("\nReturning BK-1 for student...");
//        try {
//            library.returnBook("STU-1", "BK-1");
//        } catch (Exception e) {
//            System.out.println("Return failed: " + e.getMessage());
//        }

        // Show final list after return
        System.out.println("\n--- Final Books List ---");
        library.listAllBooks().forEach(System.out::println);

        System.out.println("\nRestart the program to see data persistence.");
    }
}

