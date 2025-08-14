
package library.service;

import library.domain.Book;
import library.domain.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically writes snapshots of books and users into /backups/
 */
public class BackupService {

    private final LibraryService libraryService;
    private ScheduledExecutorService scheduler;

    public BackupService(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    public void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return; // already running
        }
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::backupNow, 30, 30, TimeUnit.SECONDS);
        System.out.println("📦 BackupService started (every 30s)");
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
            System.out.println("📦 BackupService stopped.");
        }
    }

    public void backupNow() {
        try {
            File backupDir = new File("src/main/resources/backups");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

            // Backup books
            List<Book> books = libraryService.listAllBooks();
            try (FileWriter writer = new FileWriter(new File(backupDir, "books-" + timestamp + ".csv"))) {
                writer.write("id,title,author,totalCopies,availableCopies\n");
                for (Book b : books) {
                    writer.write(String.format("%s,%s,%s,%d,%d\n",
                            b.getId(),
                            b.getTitle(),
                            b.getAuthor(),
                            b.getTotalCopies(),
                            b.getAvailableCopies()
                    ));
                }
            }

            // Backup users
            List<User> users = libraryService.getAllUsers();
            try (FileWriter writer = new FileWriter(new File(backupDir, "users-" + timestamp + ".csv"))) {
                writer.write("id,name,type\n");
                for (User u : users) {
                    writer.write(String.format("%s,%s,%s\n",
                            u.getId(),
                            u.getName(),
                            u.getClass().getSimpleName()
                    ));
                }
            }

            System.out.println("📦 Backup completed @ " + timestamp);

        } catch (IOException e) {
            System.err.println("❌ Backup failed: " + e.getMessage());
        }
    }
}
