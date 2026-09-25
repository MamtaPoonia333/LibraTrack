package com.library.management.controller;

import com.library.management.service.BorrowService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/issues")
public class IssueController {
    private final BorrowService service;

    public IssueController(BorrowService service) {
        this.service = service;
    }

    @PostMapping("/users/{userId}/books/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'MEMBER')")
    public void issueBook(@PathVariable String userId, @PathVariable String bookId) {
        service.issueBook(userId, bookId);
    }

    @PostMapping("/users/{userId}/books/{bookId}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'MEMBER')")
    public void returnBook(@PathVariable String userId, @PathVariable String bookId) {
        service.returnBook(userId, bookId);
    }
}
