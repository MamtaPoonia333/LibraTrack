package com.library.management.service;

import org.springframework.stereotype.Service;

@Service
public class BorrowService {
    private final LibraryService libraryService;

    public BorrowService(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    public void issueBook(String userId, String bookId) {
        libraryService.issueBook(userId, bookId);
    }

    public void returnBook(String userId, String bookId) {
        libraryService.returnBook(userId, bookId);
    }
}
