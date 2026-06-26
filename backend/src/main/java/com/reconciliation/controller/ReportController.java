package com.reconciliation.controller;

import com.reconciliation.model.ReconciliationHistory;
import com.reconciliation.repository.ReconciliationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReconciliationHistoryRepository historyRepository;

    @GetMapping("/{historyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadReport(@PathVariable Long historyId) {
        ReconciliationHistory history = historyRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("Histórico não encontrado: " + historyId));

        String reportPath = history.getReportPath();
        if (reportPath == null || reportPath.isEmpty()) {
            throw new RuntimeException("Relatório não encontrado para este histórico.");
        }

        File file = Paths.get(reportPath).toFile();
        if (!file.exists()) {
            throw new RuntimeException("Arquivo de relatório não encontrado: " + reportPath);
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }
}