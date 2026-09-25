package com.library.management.service;

import com.library.management.exception.ApiException;
import com.library.management.model.Book;
import com.library.management.model.BorrowRecord;
import com.library.management.model.Fine;
import com.library.management.model.IssuedBook;
import com.library.management.model.User;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

@Service
public class LibraryService {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final String BOOK_HEADER = "id,title,author,genre,isbn,type,fileFormat,description,publisher,publicationYear,fileName,fileContentType,filePath,available";
    private static final String USER_HEADER = "id,name,email,phoneNumber,borrowedBooks,passwordHash,role,emailVerified";
    private static final String LOAN_HEADER = "userId,bookId,issuedDate,dueDate";
    private static final String HISTORY_HEADER = "userId,bookId,issuedDate,dueDate,returnedDate";
    private static final String FINE_HEADER = "id,userId,bookId,overdueDays,amount,status";

    private final Path dataDirectory;
    private final Map<String, Book> books = new LinkedHashMap<>();
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Loan> loans = new LinkedHashMap<>();
    private final List<BorrowRecord> borrowingHistory = new ArrayList<>();
    private final Map<String, Fine> fines = new LinkedHashMap<>();
    private final double finePerDay;
    private final JdbcTemplate jdbc;

    public LibraryService(
            @Value("${library.data-directory:data}") String dataDirectory,
            @Value("${library.fine-per-day:1.0}") double finePerDay,
            JdbcTemplate jdbc) {
        this.dataDirectory = Paths.get(dataDirectory);
        this.finePerDay = finePerDay;
        this.jdbc = jdbc;
    }

    @PostConstruct
    public synchronized void load() {
        try { Files.createDirectories(dataDirectory); } catch (IOException exception) {
            throw new IllegalStateException("Unable to initialize file storage", exception);
        }
        readBooks();
        readUsers();
        readLoans();
        readBorrowingHistory();
        readFines();
        syncBorrowedBooks();
    }

    public synchronized List<Book> getBooks() { return getBooks(null, null, null); }

    public synchronized List<Book> getBooks(String search, String genre, Boolean available) {
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase();
        String normalizedGenre = genre == null ? "" : genre.trim();
        return books.values().stream()
                .filter(book -> normalizedSearch.isBlank()
                        || contains(book.getTitle(), normalizedSearch)
                        || contains(book.getAuthor(), normalizedSearch)
                        || contains(book.getGenre(), normalizedSearch))
                .filter(book -> normalizedGenre.isBlank() || normalizedGenre.equalsIgnoreCase(book.getGenre()))
                .filter(book -> available == null || book.isAvailable() == available)
                .toList();
    }

    public synchronized ResponseEntity<Resource> downloadEBook(String bookId) {
        Book book = findBook(bookId);
        if (book.getFilePath() == null || book.getFilePath().isBlank()) throw notFound("No eBook file is attached");
        Path path = dataDirectory.resolve(book.getFilePath()).normalize();
        if (!path.startsWith(dataDirectory.normalize()) || !Files.exists(path)) throw notFound("eBook file not found");
        Resource resource = new FileSystemResource(path);
        MediaType mediaType = MediaType.parseMediaType(book.getFileContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : book.getFileContentType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + book.getFileName() + "\"")
                .body(resource);
    }

    public synchronized Map<String, Integer> getBookStats() {
        int total = books.size();
        int available = (int) books.values().stream().filter(Book::isAvailable).count();
        return Map.of("total", total, "available", available, "issued", total - available);
    }
    public synchronized List<User> getUsers() { return new ArrayList<>(users.values()); }

    public synchronized User findUserByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email == null ? "" : email.trim()))
                .findFirst()
                .orElseThrow(() -> notFound("User not found"));
    }

    public synchronized User updateUser(User user) {
        users.put(user.getId(), user);
        saveAll();
        return user;
    }

    public synchronized List<Book> getBorrowedBooks(String userId) {
        User user = findUser(userId);
        return user.getBorrowedBooks().stream()
                .map(books::get)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public synchronized List<BorrowRecord> getUserHistory(String userId) {
        findUser(userId);
        return borrowingHistory.stream().filter(record -> userId.equals(record.getUserId())).toList();
    }

    public synchronized List<Fine> getFines() {
        refreshFines();
        return new ArrayList<>(fines.values());
    }

    public synchronized List<Fine> getUserFines(String userId) {
        findUser(userId);
        refreshFines();
        return fines.values().stream().filter(fine -> userId.equals(fine.getUserId())).toList();
    }

    public synchronized Fine payFine(String fineId) {
        refreshFines();
        Fine fine = fines.get(fineId);
        if (fine == null) throw notFound("Fine not found");
        fine.setStatus("PAID");
        saveAll();
        return fine;
    }

    public synchronized Book addBook(Book book) {
        requireText(book.getId(), "Book ID is required");
        requireText(book.getTitle(), "Title is required");
        requireText(book.getAuthor(), "Author is required");
        requireText(book.getGenre(), "Genre is required");
        book.setId(book.getId().trim());
        book.setTitle(book.getTitle().trim());
        book.setAuthor(book.getAuthor().trim());
        book.setGenre(book.getGenre().trim());
        if (books.containsKey(book.getId())) throw conflict("Book ID already exists");
        book.setIsbn(normalizeIsbn(book.getIsbn()));
        validateBook(book);
        book.setAvailable(true);
        books.put(book.getId(), book);
        saveAll();
        return book;
    }

    public synchronized void deleteBook(String bookId) {
        Book book = findBook(bookId);
        if (!book.isAvailable()) throw conflict("Issued books cannot be deleted");
        books.remove(bookId);
        saveAll();
    }

    public synchronized Book uploadEBook(String bookId, MultipartFile file) {
        Book book = findBook(bookId);
        if (!"EBook".equals(book.getType())) throw badRequest("Files can only be uploaded for eBooks");
        if (file == null || file.isEmpty()) throw badRequest("eBook file is required");
        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = extensionOf(originalName);
        if (!extension.equalsIgnoreCase(book.getFileFormat())) {
            throw badRequest("Uploaded file must be in " + book.getFileFormat() + " format");
        }
        try {
            Path fileDirectory = dataDirectory.resolve("files");
            Files.createDirectories(fileDirectory);
            String storedName = UUID.randomUUID() + "." + extension.toLowerCase();
            Path storedPath = fileDirectory.resolve(storedName);
            Files.copy(file.getInputStream(), storedPath);
            book.setFileName(originalName);
            book.setFileContentType(file.getContentType());
            book.setFilePath(dataDirectory.relativize(storedPath).toString());
            saveAll();
            return book;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to store eBook file", exception);
        }
    }

    public synchronized List<BorrowRecord> getBookHistory(String bookId) {
        findBook(bookId);
        return borrowingHistory.stream().filter(record -> bookId.equals(record.getBookId())).toList();
    }

    public synchronized User addUser(User user) {
        if (user == null) throw badRequest("User details are required");
        requireText(user.getName(), "Name is required");
        requireText(user.getEmail(), "Email is required");
        user.setName(user.getName().trim());
        user.setEmail(user.getEmail().trim().toLowerCase());
        if (!EMAIL.matcher(user.getEmail()).matches()) throw badRequest("Please enter a valid email address");
        if (users.values().stream().anyMatch(existing -> existing.getEmail().equalsIgnoreCase(user.getEmail()))) {
            throw conflict("Email is already registered");
        }
        user.setId(UUID.randomUUID().toString());
        user.setPhoneNumber(normalizePhone(user.getPhoneNumber()));
        user.setBorrowedBooks(new ArrayList<>());
        users.put(user.getId(), user);
        saveAll();
        return user;
    }

    public synchronized void issueBook(String userId, String bookId) {
        User user = findUser(userId);
        Book book = findBook(bookId);
        if (!book.isAvailable()) throw conflict("Book is not available");
        if (user.getBorrowedBooks().size() >= 3) throw conflict("User has reached maximum book limit (3 books)");
        String key = loanKey(userId, bookId);
        loans.put(key, new Loan(userId, bookId, LocalDate.now(), LocalDate.now().plusDays(14)));
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setUserName(user.getName());
        record.setBookId(bookId);
        record.setBookTitle(book.getTitle());
        record.setIssuedDate(LocalDate.now().toString());
        record.setDueDate(LocalDate.now().plusDays(14).toString());
        borrowingHistory.add(record);
        book.setAvailable(false);
        syncBorrowedBooks();
        saveAll();
    }

    public synchronized void returnBook(String userId, String bookId) {
        findUser(userId);
        Book book = findBook(bookId);
        if (!loans.containsKey(loanKey(userId, bookId))) throw badRequest("This book is not issued to the user");
        loans.remove(loanKey(userId, bookId));
        borrowingHistory.stream()
            .filter(record -> userId.equals(record.getUserId())
                && bookId.equals(record.getBookId())
                && record.getReturnedDate() == null)
            .reduce((first, second) -> second)
            .ifPresent(record -> record.setReturnedDate(LocalDate.now().toString()));
        book.setAvailable(true);
        syncBorrowedBooks();
        saveAll();
    }

    public synchronized List<IssuedBook> getIssuedBooks() {
        List<IssuedBook> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Loan loan : loans.values()) {
            User user = users.get(loan.userId());
            Book book = books.get(loan.bookId());
            if (user == null || book == null) continue;
            long daysUntilDue = ChronoUnit.DAYS.between(today, loan.dueDate());
            long overdueDays = Math.max(0, -daysUntilDue);
            IssuedBook issued = new IssuedBook();
            issued.setUserId(user.getId());
            issued.setUserName(user.getName());
            issued.setBookId(book.getId());
            issued.setBookTitle(book.getTitle());
            issued.setIssuedDate(loan.issuedDate().toString());
            issued.setDueDate(loan.dueDate().toString());
            issued.setOverdue(overdueDays > 0);
            issued.setDaysOverdue(overdueDays);
            issued.setDaysRemaining(Math.max(0, daysUntilDue));
            upsertFine(user.getId(), book.getId(), overdueDays);
            result.add(issued);
        }
        saveAll();
        return result;
    }

    private void validateBook(Book book) {
        if (book.getIsbn() != null && !book.getIsbn().isBlank() && !book.getIsbn().matches("\\d{10}|\\d{13}")) {
            throw badRequest("ISBN must be 10 or 13 digits");
        }
        if (!"Book".equals(book.getType()) && !"EBook".equals(book.getType())) throw badRequest("Book type must be Book or EBook");
        if ("EBook".equals(book.getType()) && !List.of("PDF", "EPUB").contains(book.getFileFormat())) throw badRequest("eBooks must use PDF or EPUB format");
        if (book.getPublicationYear() != null && (book.getPublicationYear() < 1000 || book.getPublicationYear() > LocalDate.now().getYear())) {
            throw badRequest("Publication year is invalid");
        }
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) return "";
        String digits = phone.replaceAll("[\\s()+-]", "");
        if (!digits.matches("\\d{10,15}")) throw badRequest("Phone number must be between 10-15 digits");
        return digits;
    }

    private String normalizeIsbn(String isbn) { return isbn == null ? "" : isbn.replaceAll("[^0-9]", ""); }
    private String extensionOf(String filename) {
        int separator = filename.lastIndexOf('.');
        if (separator < 0 || separator == filename.length() - 1) throw badRequest("File extension is required");
        return filename.substring(separator + 1);
    }
    private void requireText(String value, String message) { if (value == null || value.isBlank()) throw badRequest(message); }
    private Book findBook(String id) { Book book = books.get(id); if (book == null) throw notFound("Book not found"); return book; }
    public synchronized User findUser(String id) { User user = users.get(id); if (user == null) throw notFound("User not found"); return user; }
    private String loanKey(String userId, String bookId) { return userId + "|" + bookId; }
    private ApiException badRequest(String message) { return new ApiException(HttpStatus.BAD_REQUEST, message); }
    private ApiException notFound(String message) { return new ApiException(HttpStatus.NOT_FOUND, message); }
    private ApiException conflict(String message) { return new ApiException(HttpStatus.CONFLICT, message); }

    private void syncBorrowedBooks() {
        users.values().forEach(user -> user.setBorrowedBooks(loans.values().stream().filter(loan -> loan.userId().equals(user.getId())).map(Loan::bookId).toList()));
    }

    private void saveAll() {
        jdbc.update("DELETE FROM fines");
        jdbc.update("DELETE FROM borrow_history");
        jdbc.update("DELETE FROM loans");
        jdbc.update("DELETE FROM books");
        jdbc.update("DELETE FROM users");
        users.values().forEach(user -> jdbc.update("INSERT INTO users (id,name,email,phone_number,password_hash,role,email_verified) VALUES (?,?,?,?,?,?,?)", user.getId(), user.getName(), user.getEmail(), user.getPhoneNumber(), user.getPasswordHash(), user.getRole(), user.isEmailVerified()));
        books.values().forEach(book -> jdbc.update("INSERT INTO books (id,title,author,genre,isbn,type,file_format,description,publisher,publication_year,file_name,file_content_type,file_path,available) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", book.getId(), book.getTitle(), book.getAuthor(), book.getGenre(), book.getIsbn(), book.getType(), book.getFileFormat(), book.getDescription(), book.getPublisher(), book.getPublicationYear(), book.getFileName(), book.getFileContentType(), book.getFilePath(), book.isAvailable()));
        loans.values().forEach(loan -> jdbc.update("INSERT INTO loans (user_id,book_id,issued_date,due_date) VALUES (?,?,?,?)", loan.userId(), loan.bookId(), loan.issuedDate(), loan.dueDate()));
        borrowingHistory.forEach(record -> jdbc.update("INSERT INTO borrow_history (user_id,book_id,issued_date,due_date,returned_date) VALUES (?,?,?,?,?)", record.getUserId(), record.getBookId(), LocalDate.parse(record.getIssuedDate()), LocalDate.parse(record.getDueDate()), record.getReturnedDate() == null ? null : LocalDate.parse(record.getReturnedDate())));
        fines.values().forEach(fine -> jdbc.update("INSERT INTO fines (id,user_id,book_id,overdue_days,amount,status) VALUES (?,?,?,?,?,?)", fine.getId(), fine.getUserId(), fine.getBookId(), fine.getOverdueDays(), fine.getAmount(), fine.getStatus()));
    }

    private void write(String file, String header, List<String> rows) throws IOException { Files.write(dataDirectory.resolve(file), concat(header, rows), StandardCharsets.UTF_8); }
    private List<String> concat(String header, List<String> rows) { List<String> lines = new ArrayList<>(); lines.add(header); lines.addAll(rows); return lines; }
    private String csv(String... values) { return java.util.Arrays.stream(values).map(this::escape).collect(java.util.stream.Collectors.joining(",")); }
    private String escape(String value) { String safe = value == null ? "" : value; return "\"" + safe.replace("\"", "\"\"") + "\""; }

    private void readBooks() { jdbc.query("SELECT * FROM books", (rs, row) -> { Book b = new Book(); b.setId(rs.getString("id")); b.setTitle(rs.getString("title")); b.setAuthor(rs.getString("author")); b.setGenre(rs.getString("genre")); b.setIsbn(rs.getString("isbn")); b.setType(rs.getString("type")); b.setFileFormat(rs.getString("file_format")); b.setDescription(rs.getString("description")); b.setPublisher(rs.getString("publisher")); b.setPublicationYear((Integer) rs.getObject("publication_year")); b.setFileName(rs.getString("file_name")); b.setFileContentType(rs.getString("file_content_type")); b.setFilePath(rs.getString("file_path")); b.setAvailable(rs.getBoolean("available")); books.put(b.getId(), b); return b; }); }
    private void readUsers() { jdbc.query("SELECT * FROM users", (rs, row) -> { User u = new User(); u.setId(rs.getString("id")); u.setName(rs.getString("name")); u.setEmail(rs.getString("email")); u.setPhoneNumber(rs.getString("phone_number")); u.setPasswordHash(rs.getString("password_hash")); u.setRole(rs.getString("role")); u.setEmailVerified(rs.getBoolean("email_verified")); users.put(u.getId(), u); return u; }); }
    private void readLoans() { jdbc.query("SELECT * FROM loans", (rs, row) -> { String userId = rs.getString("user_id"); String bookId = rs.getString("book_id"); loans.put(loanKey(userId, bookId), new Loan(userId, bookId, rs.getDate("issued_date").toLocalDate(), rs.getDate("due_date").toLocalDate())); return null; }); }
    private void readBorrowingHistory() { jdbc.query("SELECT h.*, u.name user_name, b.title book_title FROM borrow_history h JOIN users u ON u.id=h.user_id JOIN books b ON b.id=h.book_id ORDER BY h.id", (rs, row) -> { BorrowRecord r = new BorrowRecord(); r.setUserId(rs.getString("user_id")); r.setUserName(rs.getString("user_name")); r.setBookId(rs.getString("book_id")); r.setBookTitle(rs.getString("book_title")); r.setIssuedDate(rs.getDate("issued_date").toLocalDate().toString()); r.setDueDate(rs.getDate("due_date").toLocalDate().toString()); java.sql.Date returned = rs.getDate("returned_date"); r.setReturnedDate(returned == null ? null : returned.toLocalDate().toString()); borrowingHistory.add(r); return r; }); }
    private void readFines() { jdbc.query("SELECT * FROM fines", (rs, row) -> { Fine f = new Fine(); f.setId(rs.getString("id")); f.setUserId(rs.getString("user_id")); f.setBookId(rs.getString("book_id")); f.setOverdueDays(rs.getLong("overdue_days")); f.setAmount(rs.getDouble("amount")); f.setStatus(rs.getString("status")); fines.put(f.getId(), f); return f; }); }
    private void refreshFines() { getIssuedBooks(); }
    private void upsertFine(String userId, String bookId, long overdueDays) {
        String fineId = userId + "|" + bookId;
        if (overdueDays == 0) return;
        Fine fine = fines.get(fineId);
        if (fine == null) { fine = new Fine(); fine.setId(fineId); fine.setUserId(userId); fine.setBookId(bookId); fine.setStatus("UNPAID"); fines.put(fineId, fine); }
        if (!"PAID".equals(fine.getStatus())) { fine.setOverdueDays(overdueDays); fine.setAmount(overdueDays * finePerDay); fine.setStatus("UNPAID"); }
    }
    private Integer parseYear(String value) { return value == null || value.isBlank() || "null".equals(value) ? null : Integer.valueOf(value); }
    private List<String> dataLines(String file) throws IOException { Path path = dataDirectory.resolve(file); if (!Files.exists(path)) return List.of(); return Files.readAllLines(path, StandardCharsets.UTF_8).stream().skip(1).filter(line -> !line.isBlank()).toList(); }
    private List<String> parse(String line) { List<String> result = new ArrayList<>(); StringBuilder current = new StringBuilder(); boolean quoted = false; for (int i = 0; i < line.length(); i++) { char ch = line.charAt(i); if (ch == '"') { if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') { current.append('"'); i++; } else quoted = !quoted; } else if (ch == ',' && !quoted) { result.add(current.toString()); current.setLength(0); } else current.append(ch); } result.add(current.toString()); return result; }

    private record Loan(String userId, String bookId, LocalDate issuedDate, LocalDate dueDate) { }
}
