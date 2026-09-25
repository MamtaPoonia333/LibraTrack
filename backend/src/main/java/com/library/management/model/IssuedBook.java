package com.library.management.model;

public class IssuedBook {
    private String userId;
    private String userName;
    private String bookId;
    private String bookTitle;
    private String issuedDate;
    private String dueDate;
    private boolean isOverdue;
    private long daysOverdue;
    private long daysRemaining;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getIssuedDate() { return issuedDate; }
    public void setIssuedDate(String issuedDate) { this.issuedDate = issuedDate; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public boolean isOverdue() { return isOverdue; }
    public void setOverdue(boolean overdue) { isOverdue = overdue; }
    public long getDaysOverdue() { return daysOverdue; }
    public void setDaysOverdue(long daysOverdue) { this.daysOverdue = daysOverdue; }
    public long getDaysRemaining() { return daysRemaining; }
    public void setDaysRemaining(long daysRemaining) { this.daysRemaining = daysRemaining; }
}
