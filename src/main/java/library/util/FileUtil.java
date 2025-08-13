package library.util;

import library.domain.Book;
import library.domain.Student;
import library.domain.Librarian;
import library.domain.User;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileUtil {

    // ---------- BOOKS ----------
    public static List<Book> loadBooks(Path path) {
        List<Book> books = new ArrayList<>();
        if (!Files.exists(path)) return books;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length != 5) continue;

                String id = parts[0];
                String title = parts[1];
                String author = parts[2];
                int totalCopies = Integer.parseInt(parts[3]);
                int availableCopies = Integer.parseInt(parts[4]);

                Book book = new Book(id, title, author, totalCopies);
                book.setAvailableCopies(availableCopies);
                books.add(book);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return books;
    }

    public static void saveBooks(Collection<Book> books, Path path) {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (Book b : books) {
                writer.write(String.join(",",
                        b.getId(),
                        b.getTitle(),
                        b.getAuthor(),
                        String.valueOf(b.getTotalCopies()),
                        String.valueOf(b.getAvailableCopies())
                ));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------- USERS ----------
    public static List<User> loadUsers(Path path) {
        List<User> users = new ArrayList<>();
        if (!Files.exists(path)) return users;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length != 3) continue;

                String type = parts[0];
                String id = parts[1];
                String name = parts[2];

                if ("STUDENT".equalsIgnoreCase(type)) {
                    users.add(new Student(id, name));
                } else if ("LIBRARIAN".equalsIgnoreCase(type)) {
                    users.add(new Librarian(id, name));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static void saveUsers(Collection<User> users, Path path) {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (User u : users) {
                String type = (u instanceof Student) ? "STUDENT" : "LIBRARIAN";
                writer.write(String.join(",",
                        type,
                        u.getId(),
                        u.getName()
                ));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------- BORROWED BOOKS ----------
    public static Map<String, Set<String>> loadBorrowedBooks(Path path) {
        Map<String, Set<String>> borrowed = new HashMap<>();
        if (!Files.exists(path)) return borrowed;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 2) continue;
                borrowed.putIfAbsent(parts[0], new HashSet<>());
                borrowed.get(parts[0]).add(parts[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return borrowed;
    }

    public static void saveBorrowedBooks(Map<String, Set<String>> borrowed, Path path) {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (Map.Entry<String, Set<String>> entry : borrowed.entrySet()) {
                for (String bookId : entry.getValue()) {
                    writer.write(entry.getKey() + "," + bookId);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
