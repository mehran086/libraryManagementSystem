package library.service;

import library.domain.Book;
import library.domain.Librarian;
import library.domain.User;
import library.exceptions.*;
import library.repository.BookRepository;
import library.repository.UserRepository;

import javax.naming.LimitExceededException;
import java.util.*;

public class LibraryService {
    BookRepository bookrepo;
    UserRepository userRepo ;

    public LibraryService(BookRepository bookrepo, UserRepository userRepo) {
        this.bookrepo = bookrepo;
        this.userRepo = userRepo;
    }

    // Librarian functionaliy
    // 1) Add books
    // Adding books (only a librarian can do that);
   public void addBook(Book b1, User l1){
        if(!(l1 instanceof Librarian)){
            throw  new PermissionDeniedException("Only a librarian can add books");
        }
        bookrepo.save(b1);
    }

    // Student functionality
    // 1) list down all the books present.
    // 2) see whether a book is available or not(search)
    // 3) borrow a book( user should exist, book should exist, book should be available, limit should not cross, cannot borrow the same book again)
    // 4) Return the book(user should only return the book that he borrowed)

    public List<Book> listAllBooks(){

        // 1) list down all the books present.

        return bookrepo.findAll();

    }
    // 2) see whether a book is available or not(search)
    List<Book>  searchByName(String bookname){
        bookname = bookname.toLowerCase();
        String finalBookname = bookname;
//      return  bookrepo.search(book -> book.getTitle().contains(finalBookname));
      return  bookrepo.search(book -> book.getTitle().toLowerCase().contains(finalBookname));
    }
    // 3) borrow a book( book should be available, limit should not cross, cannot borrow the same book again)
    private final Map<String, Set<String>> trackBooks = new HashMap<>();   // key for userId and values for bookId

    public synchronized  void borrowBook(String userId, String bookId){
        // user should exist
       User user =  userRepo.findById(userId).orElseThrow(() -> new NotFound("User not found"));
       if(user instanceof Librarian){
           throw  new PermissionDeniedException("Only a student can borrow books");
       }
       // Book should exist
        Book book = bookrepo.findById(bookId).orElseThrow(()->new NotFound("Book not found"));

       // adding user to trackBooks if it is not present  already
        trackBooks.putIfAbsent(userId,new HashSet<>());

        // check whether the book is already present or not
        if(trackBooks.get(userId).contains(bookId)){
            throw new AlreadyBorrowed("Book is already borrowed by the user");
        }
        // check that the limit of user is under the specified limit
        if(trackBooks.get(userId).size() >= user.getBorrowLimit()){
            throw new BookLimitExceeded("The user has already reached borrow Limit");
        }

        // book should be available as well
        if(book.getAvailableCopies()<=0){
            throw new BookNotAvailableException("The book is currently unavailable");
        }
        //now borrow the book
//        trackBooks.put(userId, Collections.singleton(bookId));
        trackBooks.get(userId).add(bookId);
        book.setAvailableCopies(book.getAvailableCopies()-1);
        bookrepo.save(book);
    }
    public synchronized void returnBook(String userId, String bookId){
                // if a user(should be a student) contains a particular book,
                 // only then he/she can return it
            if(!(trackBooks.containsKey(userId)) || !(trackBooks.get(userId).contains(bookId))){
                throw  new NotFound("User cannot return this book as he didn't borrow it");
            }
        Book book = bookrepo.findById(bookId).orElseThrow(()->new NotFound("Book not found"));
            book.setAvailableCopies(book.getAvailableCopies()+1);
            trackBooks.get(userId).remove(bookId);
            bookrepo.save(book);

    }
   public void printBooksOfStudent(String userId){
//        trackBooks.get(userId).forEach(System.out::println);
        Set<String> bookIds = trackBooks.get(userId);
//        bookIds.stream().map((curr)-> bookrepo.findById(curr)).forEach(System.out::println);
////       System.out.println(trackBooks.get(userId));

       if (bookIds == null || bookIds.isEmpty()) {
           System.out.println("No books borrowed by this user.");
           return;
       }

       bookIds.stream()
               .map(bookrepo::findById) // returns Optional<Book>
               .filter(Optional::isPresent) // only keep found books
               .map(Optional::get) // unwrap
               .forEach(System.out::println);
    }




}
