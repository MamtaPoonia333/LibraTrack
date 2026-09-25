package com.library.management.service;

import com.library.management.model.Book;
import com.library.management.model.BorrowRecord;
import com.library.management.model.IssuedBook;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BookService {
    private final LibraryService libraryService;

    public BookService(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    public List<Book> getBooks() {
        return libraryService.getBooks();
    }

    public List<Book> getBooks(String search, String genre, Boolean available) {
        return libraryService.getBooks(search, genre, available);
    }

    public ResponseEntity<Resource> downloadEBook(String bookId) {
        return libraryService.downloadEBook(bookId);
    }

    public Map<String, Integer> getBookStats() {
        return libraryService.getBookStats();
    }

    public Book addBook(Book book) {
        return libraryService.addBook(book);
    }

    public void deleteBook(String bookId) {
        libraryService.deleteBook(bookId);
    }

    public Book uploadEBook(String bookId, MultipartFile file) {
        return libraryService.uploadEBook(bookId, file);
    }

    public List<BorrowRecord> getBookHistory(String bookId) {
        return libraryService.getBookHistory(bookId);
    }

    public List<IssuedBook> getIssuedBooks() {
        return libraryService.getIssuedBooks();
    }
}
