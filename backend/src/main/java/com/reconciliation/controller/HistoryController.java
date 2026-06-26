package com.reconciliation.controller;

import com.reconciliation.service.HistoryService;
import com.reconciliation.service.dto.HistoryDetailDTO;
import com.reconciliation.service.dto.HistoryListItemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Slf4j
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<HistoryListItemDTO>> listHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "executedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy));

        Page<HistoryListItemDTO> history = historyService.listHistory(
                userDetails.getUsername(),
                startDate,
                endDate,
                username,
                pageable
        );

        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HistoryDetailDTO> getHistoryDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        HistoryDetailDTO detail = historyService.getHistoryDetail(id, userDetails.getUsername());
        return ResponseEntity.ok(detail);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHistory(@PathVariable Long id) {
        historyService.deleteHistory(id);
        return ResponseEntity.noContent().build();
    }
}