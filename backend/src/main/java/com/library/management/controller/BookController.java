package com.library.management.controller;

import com.library.management.model.Book;
import com.library.management.model.IssuedBook;
import com.library.management.service.BookService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService service;
    public BookController(BookService service) { this.service = service; }
    @GetMapping
    public List<Book> getBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Boolean available) {
        return service.getBooks(search, genre, available);
    }

    @GetMapping("/stats")
    public Map<String, Integer> getBookStats() { return service.getBookStats(); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Book addBook(@RequestBody Book book) { return service.addBook(book); }
    @PostMapping("/{id}/file")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Book uploadEBook(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        return service.uploadEBook(id, file);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> downloadEBook(@PathVariable String id) {
        return service.downloadEBook(id);
    }

    @GetMapping("/{id}/history")
    public List<com.library.management.model.BorrowRecord> getBookHistory(@PathVariable String id) {
        return service.getBookHistory(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public void deleteBook(@PathVariable String id) { service.deleteBook(id); }
    @GetMapping("/issued") public List<IssuedBook> getIssuedBooks() { return service.getIssuedBooks(); }
}
