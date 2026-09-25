package com.library.management.service;

import com.library.management.model.Book;
import com.library.management.model.BorrowRecord;
import com.library.management.model.User;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final LibraryService libraryService;

    public UserService(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    public List<User> getUsers() {
        return libraryService.getUsers();
    }

    public User findUserByEmail(String email) {
        return libraryService.findUserByEmail(email);
    }

    public User updateUser(User user) {
        return libraryService.updateUser(user);
    }

    public List<Book> getBorrowedBooks(String userId) {
        return libraryService.getBorrowedBooks(userId);
    }

    public List<BorrowRecord> getUserHistory(String userId) {
        return libraryService.getUserHistory(userId);
    }

    public User addUser(User user) {
        return libraryService.addUser(user);
    }

    public User findUser(String id) {
        return libraryService.findUser(id);
    }
}
