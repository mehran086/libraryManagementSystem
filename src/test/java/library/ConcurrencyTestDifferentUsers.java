
package library;

import library.domain.Book;
import library.domain.Student;
import library.repository.BookRepository;
import library.repository.UserRepository;
import library.service.LibraryService;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrencyTestDifferentUsers {

    @Test
    void multipleUsersRacingForSingleCopy() throws Exception {
        // Use in-memory repos for isolation
        BookRepository bookRepo = new BookRepository();
        UserRepository userRepo = new UserRepository();

        LibraryService libraryService = new LibraryService(
                bookRepo,
                userRepo,
                Paths.get("src/test/resources/test-data/books-test.csv"),
                Paths.get("src/test/resources/test-data/users-test.csv"),
                Paths.get("src/test/resources/test-data/borrowed-test.csv")
        );

        // Setup: One book with only 1 copy
        Book testBook = new Book("B1", "Effective Java", "Joshua Bloch", 1);
        bookRepo.save(testBook);

        // Create 10 different users
        for (int i = 1; i <= 10; i++) {
            userRepo.save(new Student("U" + i, "Student" + i));
        }

        int numThreads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 1; i <= numThreads; i++) {
            String userId = "U" + i;
            results.add(executor.submit(() -> {
                startLatch.await(); // wait for the "go" signal
                try {
                    libraryService.borrowBook(userId, "B1");
                    return true; // success
                } catch (Exception e) {
                    return false; // failed
                } finally {
                    doneLatch.countDown();
                }
            }));
        }

        // Start all threads at the same time
        startLatch.countDown();

        // Wait for all to finish
        doneLatch.await();

        executor.shutdown();

        // Count successes
        long successCount = 0;
        for (Future<Boolean> f : results) {
            if (f.get()) successCount++;
        }

        System.out.println("Successes: " + successCount);
        System.out.println("Failures: " + (numThreads - successCount));

        assertEquals(1, successCount, "Only one user should be able to borrow the book");
        assertEquals(0, bookRepo.findById("B1").get().getAvailableCopies(), "No copies should remain");
    }
}


