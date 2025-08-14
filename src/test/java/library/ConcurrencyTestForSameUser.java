package library;

import library.domain.Book;
import library.domain.Student;
import library.exceptions.BookNotAvailableException;
import library.repository.BookRepository;
import library.repository.UserRepository;
import library.service.LibraryService;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;
// one problem with this concurrencyTest is  that it will pass only for the first time,
// since we are saving the user and the books borrowed, when we run this program again, the
// user would have already borrowed the book earlier , so it would fail for that user.

// so as  of now when running it again , change the id of book.

public class ConcurrencyTest {

    public static void main(String[] args) throws InterruptedException {
        // Setup empty repos
        BookRepository bookRepo = new BookRepository();
        UserRepository userRepo = new UserRepository();

        // Temporary file paths to satisfy the needs of constructor of libraryService
        LibraryService libraryService = new LibraryService(
                bookRepo,
                userRepo,
                Paths.get("src/test/resources/test-data/books-test.csv"),
                Paths.get("src/test/resources/test-data/users-test.csv"),
                Paths.get("src/test/resources/test-data/borrowed-test.csv")
        );

        // Create a book with only 1 available copy
        Book singleCopyBook = new Book("B009", "Concurrent Programming", "Some Author", 1);
        bookRepo.save(singleCopyBook);

        // Create a student
        Student student = new Student("S003", "Alison");
        userRepo.save(student);

        // Prepare concurrent borrow attempts
        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        List<String> results = new CopyOnWriteArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // every thread amoung the ten threads should wait
                    // till the countdown of startLatch is zero.
                    libraryService.borrowBook(student.getId(), singleCopyBook.getId());
                    results.add("SUCCESS");
                } catch (BookNotAvailableException e) {
                    results.add("FAIL: Not available");
                } catch (Exception e) {
                    results.add("FAIL: " + e.getClass().getSimpleName());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Release all threads at the same time
        startLatch.countDown();

        // Wait for all of the ten tasks to finish
        doneLatch.await();
        executor.shutdown(); // graceful shutdown.

        long successCount = results.stream().filter(r -> r.equals("SUCCESS")).count();
        long failCount = results.size() - successCount;

        System.out.println("✅ Success count: " + successCount);
        System.out.println("❌ Fail count   : " + failCount);
        System.out.println("Results: " + results);

        if (successCount == 1 && failCount == 9 && singleCopyBook.getAvailableCopies() >= 0) {
            System.out.println("🎯 TEST PASSED: Concurrency handled correctly.");
        } else {
            System.out.println("🚨 TEST FAILED: Inventory or counts incorrect.");
        }
    }
}
