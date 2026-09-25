package com.library.management.controller;

import com.library.management.dto.RoleRequest;
import com.library.management.exception.ApiException;
import com.library.management.model.User;
import com.library.management.service.UserService;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService service;

    public AdminController(UserService service) {
        this.service = service;
    }

    @PostMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public User updateRole(@PathVariable String userId, @RequestBody RoleRequest request) {
        User user = service.findUser(userId);
        if (!Set.of("ADMIN", "LIBRARIAN", "MEMBER").contains(request.role())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid role");
        }
        user.setRole(request.role());
        return service.updateUser(user);
    }
}
