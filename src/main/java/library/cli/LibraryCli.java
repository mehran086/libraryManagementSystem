package library.cli;

import library.domain.Book;
import library.domain.Librarian;
import library.domain.Student;
import library.domain.User;
import library.repository.BookRepository;
import library.repository.UserRepository;
import library.service.BackupService;
import library.service.LibraryService;
import library.exceptions.*;
import library.util.ReportUtil;

import java.nio.file.Path;
import java.util.Scanner;

public class LibraryCli {
    private final LibraryService library;
    private final UserRepository userRepo;
    private final BackupService backupService;
    private User currentUser;

    public LibraryCli(LibraryService library, UserRepository userRepo , BackupService backupService) {
        this.library = library;
        this.userRepo = userRepo;
        this.backupService= backupService;
        backupService.start();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Library CLI! Type 'help' for commands.");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            if (line.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                backupService.stop();
                System.exit(0);
                break;
            }

            try {
                handleCommand(line);
            } catch (Exception e) {
                System.out.println("❌ " + e.getMessage());
            }
        }
    }

    private void handleCommand(String input) {
        String[] parts = input.split(" ", 2);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "help":
                printHelp();
                break;

            case "login":
                if (parts.length < 2) {
                    System.out.println("Usage: login <userId>");
                    return;
                }
                login(parts[1]);
                break;

            case "add-user":
                requireLogin();
                if (!(currentUser instanceof Librarian)) {
                    throw new PermissionDeniedException("Only librarians can add users.");
                }
                if (parts.length < 2) {
                    System.out.println("Usage: add-user <type>|<id>|<name>");
                    return;
                }
                String[] userParts = parts[1].split("\\|");
                if (userParts.length != 3) {
                    System.out.println("Format: <type>|<id>|<name>");
                    return;
                }
                String type = userParts[0].toLowerCase();
                String id = userParts[1];
                String name = userParts[2];

                User newUser;
                if (type.equals("student")) {
                    newUser = new Student(id, name);
                } else if (type.equals("librarian")) {
                    newUser = new Librarian(id, name);
                } else {
                    System.out.println("Type must be 'student' or 'librarian'");
                    return;
                }
                library.addUser(newUser, currentUser);
                System.out.println("✅ User added: " + id + " (" + type + ")");
                break;

            case "add-book":
                requireLogin();
                if (!(currentUser instanceof Librarian)) {
                    throw new PermissionDeniedException("Only librarians can add books.");
                }
                if (parts.length < 2) {
                    System.out.println("Usage: add-book <title>|<author>|<copies>");
                    return;
                }
                String[] bookParts = parts[1].split("\\|");
                if (bookParts.length != 3) {
                    System.out.println("Format: <title>|<author>|<copies>");
                    return;
                }
                String title = bookParts[0];
                String author = bookParts[1];
                int copies = Integer.parseInt(bookParts[2]);
                String bookId = "BK-" + System.currentTimeMillis();
                library.addBook(new Book(bookId, title, author, copies), currentUser);
//                Book book =new Book(title, author, copies);
//                library.addBook(book, currentUser);
                System.out.println("✅ Book added with ID: " + bookId);
                break;

            case "list":
                library.listAllBooks().forEach(System.out::println);
                break;
            case "list-users":
                requireLogin();
                if (!(currentUser instanceof Librarian)) {
                    throw new PermissionDeniedException("Only librarians can see users.");
                }
                library.getAllUsers().forEach(System.out::println);
                break;
            case "search":
                if (parts.length < 2) {
                    System.out.println("Usage: search <title-fragment>");
                    return;
                }
                library.searchByName(parts[1]).forEach(System.out::println);
                break;

            case "borrow":
                requireLogin();
                if (parts.length < 2) {
                    System.out.println("Usage: borrow <bookId>");
                    return;
                }
                library.borrowBook(currentUser.getId(), parts[1]);
                System.out.println("✅ Borrowed book: " + parts[1]);
                break;

            case "return":
                requireLogin();
                if (parts.length < 2) {
                    System.out.println("Usage: return <bookId>");
                    return;
                }
                library.returnBook(currentUser.getId(), parts[1]);
                System.out.println("✅ Returned book: " + parts[1]);
                break;

            case "report":
                requireLogin();
                library.printBooksOfStudent(currentUser.getId());
                break;
            case "admin-report":
                requireLogin();
                if (!(currentUser instanceof Librarian)) {
                    throw new PermissionDeniedException("Only librarians can generate reports.");
                }
                System.out.println(ReportUtil.generateReport(library));
                break;


            default:
                System.out.println("Unknown command. Type 'help' for a list of commands.");
        }
    }

    private void login(String userId) {
        currentUser = userRepo.findById(userId)
                .orElseThrow(() -> new NotFound("User not found: " + userId));
        System.out.println("✅ Logged in as: " + currentUser.getName());
    }

    private void requireLogin() {
        if (currentUser == null) {
            throw new PermissionDeniedException("You must login first.");
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  login <userId>                     - Log in as an existing user");
        System.out.println("  add-user <type>|<id>|<name>        - Add a new user (librarians only)");
        System.out.println("  add-book <title>|<author>|<copies> - Add a new book (librarians only)");
        System.out.println("  list                               - List all books");
        System.out.println("  list-users                         - List all users(librarians only)");
        System.out.println("  search <title-fragment>            - Search books by title");
        System.out.println("  borrow <bookId>                    - Borrow a book");
        System.out.println("  return <bookId>                    - Return a borrowed book");
        System.out.println("  report                             - Show books you have borrowed");
        System.out.println("  admin-report                       - Generate admin reflection report (librarians only)");
        System.out.println("  help                               - Show this help message");
        System.out.println("  exit                               - Exit the program");
    }

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
