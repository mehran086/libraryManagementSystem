package library;

import library.domain.Book;
import library.domain.Librarian;
import library.domain.Student;
import library.domain.User;
import library.repository.BookRepository;
import library.repository.UserRepository;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
//        Book b1 = new Book("Effective Java", "Joshua Bloch", 5);
//        Book b2 = new Book("Clean Code", "Robert C. Martin", 3);
//
//        Student s1 = new Student("Alice");
//        Librarian l1 = new Librarian("Bob");
//
//        System.out.println(b1);
//        System.out.println(b2);
//        System.out.println(s1.getId() + " - " + s1.getName() + " limit: " + s1.getBorrowLimit());
//        System.out.println(l1.getId() + " - " + l1.getName() + " limit: " + l1.getBorrowLimit());
        BookRepository repo = new BookRepository();
        Book b1 = new Book("Java 101", "John Doe", 5);
        Book b2 = new Book("Spring Boot", "Jane Smith", 3);

        repo.save(b1);
        repo.save(b2);

        System.out.println(repo.findAll()); // should list both
        System.out.println(repo.search(b -> b.getTitle().contains("10"))); // only Java 101
        UserRepository userRepo = new UserRepository();
        Student s1 = new Student("Alice");
        Student s2 = new Student("Bob");
        Librarian l1 = new Librarian("Charlie");

        userRepo.save(s1);
        userRepo.save(s2);
        userRepo.save(l1);

        System.out.println("\nAll users:");
        System.out.println(userRepo.findAll());
        System.out.println("Users with name starting with 'A':");
        System.out.println(userRepo.search(u -> u.getName().startsWith("A")));

        System.out.println("Find by ID (Charlie):");
        System.out.println(userRepo.findById(l1.getId()).orElse(null));

    }
}