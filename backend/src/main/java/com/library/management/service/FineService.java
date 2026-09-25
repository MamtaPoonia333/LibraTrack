package com.library.management.service;

import com.library.management.model.Fine;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FineService {
    private final LibraryService libraryService;

    public FineService(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    public List<Fine> getFines() {
        return libraryService.getFines();
    }

    public List<Fine> getUserFines(String userId) {
        return libraryService.getUserFines(userId);
    }

    public Fine payFine(String fineId) {
        return libraryService.payFine(fineId);
    }
}
