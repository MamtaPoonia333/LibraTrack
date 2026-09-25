package com.library.management.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class User {
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    @JsonIgnore
    private String passwordHash;
    private String role = "MEMBER";
    private boolean emailVerified;
    private transient String password;
    private List<String> borrowedBooks = new ArrayList<>();

    public User() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<String> getBorrowedBooks() { return borrowedBooks; }
    public void setBorrowedBooks(List<String> borrowedBooks) { this.borrowedBooks = borrowedBooks; }
}
