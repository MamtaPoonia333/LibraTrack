package com.library.management.controller;

import com.library.management.model.User;
import com.library.management.model.Book;
import com.library.management.dto.RegisterRequest;
import com.library.management.service.AuthService;
import com.library.management.service.UserService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    private final AuthService authService;
    public UserController(UserService service, AuthService authService) { this.service = service; this.authService = authService; }
    @GetMapping public List<User> getUsers() { return service.getUsers(); }
    @GetMapping("/{userId}/borrowed-books")
    public List<Book> getBorrowedBooks(@PathVariable String userId) { return service.getBorrowedBooks(userId); }
    @GetMapping("/{userId}/history")
    public List<com.library.management.model.BorrowRecord> getBorrowingHistory(@PathVariable String userId) {
        return service.getUserHistory(userId);
    }
    @PostMapping public User addUser(@RequestBody RegisterRequest request) {
        User user = authService.register(request.name(), request.email(), request.phoneNumber(), request.password());
        authService.issueOtp(user.getEmail(), "EMAIL_VERIFICATION");
        return user;
    }

}
