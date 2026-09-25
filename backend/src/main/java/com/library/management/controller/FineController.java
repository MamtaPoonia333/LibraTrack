package com.library.management.controller;

import com.library.management.model.Fine;
import com.library.management.service.FineService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/fines")
public class FineController {
    private final FineService service;

    public FineController(FineService service) { this.service = service; }

    @GetMapping
    public List<Fine> getFines() { return service.getFines(); }

    @GetMapping("/user/{userId}")
    public List<Fine> getUserFines(@PathVariable String userId) { return service.getUserFines(userId); }

    @PostMapping("/{fineId}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Fine payFine(@PathVariable String fineId) { return service.payFine(fineId); }
}