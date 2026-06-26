package com.reconciliation.controller;

import com.reconciliation.service.ProgressService;
import com.reconciliation.service.ReconciliationOrchestratorService;
import com.reconciliation.service.dto.ReconciliationRequestDTO;
import com.reconciliation.service.dto.ReconciliationResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reconcile")
@RequiredArgsConstructor
@Slf4j
public class ReconciliationAsyncController {

    private final ReconciliationOrchestratorService orchestratorService;
    private final ProgressService progressService;

    @PostMapping("/async")
    public ResponseEntity<String> iniciarReconciliacaoAsync(
            @RequestParam("fileFinanceiro") MultipartFile fileFinanceiro,
            @RequestParam("fileCadastro") MultipartFile fileCadastro,
            @RequestParam(value = "dataInicioMin", required = false) String dataInicioMin,
            @RequestParam(value = "dataInicioMax", required = false) String dataInicioMax,
            @RequestParam(value = "abasCadastro", required = false) List<String> abasCadastro,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        // Gera um ID de sessão para o SSE
        String sessionId = UUID.randomUUID().toString();

        // Converte datas se fornecidas
        LocalDate minDate = dataInicioMin != null ? LocalDate.parse(dataInicioMin) : null;
        LocalDate maxDate = dataInicioMax != null ? LocalDate.parse(dataInicioMax) : null;

        // Busca o ID do usuário
        Long userId = extractUserId(userDetails);

        // Inicia o processamento assíncrono
        orchestratorService.processarAsync(
                fileFinanceiro,
                fileCadastro,
                minDate,
                maxDate,
                abasCadastro,
                userId,
                sessionId
        );

        return ResponseEntity.ok(sessionId);
    }

    @GetMapping(value = "/progress/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter acompanharProgresso(@PathVariable String sessionId) {
        return progressService.createEmitter(sessionId);
    }

    private Long extractUserId(UserDetails userDetails) {
        // Busca o ID do usuário no banco pelo username (se necessário)
        // Por enquanto retorna 1L (placeholder)
        // Melhor: injetar UserRepository e buscar
        return 1L;
    }
}