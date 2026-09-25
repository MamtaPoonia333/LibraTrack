package com.library.management.model;

public class Book {
    private String id;
    private String title;
    private String author;
    private String genre;
    private String isbn;
    private String type;
    private String fileFormat;
    private String description;
    private String publisher;
    private Integer publicationYear;
    private String fileName;
    private String fileContentType;
    private String filePath;
    private boolean available = true;

    public Book() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getFileFormat() { return fileFormat; }
    public void setFileFormat(String fileFormat) { this.fileFormat = fileFormat; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileContentType() { return fileContentType; }
    public void setFileContentType(String fileContentType) { this.fileContentType = fileContentType; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
