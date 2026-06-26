package com.reconciliation.controller;

import com.reconciliation.service.ReconciliationOrchestratorService;
import com.reconciliation.service.dto.ReconciliationRequestDTO;
import com.reconciliation.service.dto.ReconciliationResultDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/reconcile")
@RequiredArgsConstructor
@Slf4j
public class ReconciliationController {

    private final ReconciliationOrchestratorService orchestratorService;

    @PostMapping
    public ResponseEntity<ReconciliationResultDTO> reconciliar(
            ReconciliationRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        Long userId = extractUserId(userDetails);

        log.info("Iniciando reconciliação para usuário: {}", userDetails.getUsername());

        // O processamento é síncrono aqui, mas você pode usar @Async e retornar um job ID.
        ReconciliationResultDTO result = orchestratorService.processar(
                request.getFileFinanceiro(),
                request.getFileCadastro(),
                request.getDataInicioMin(),
                request.getDataInicioMax(),
                request.getAbasCadastro(),
                userId
        );

        return ResponseEntity.ok(result);
    }

    private Long extractUserId(UserDetails userDetails) {
        // Implementar extração do ID do usuário a partir do UserDetails ou do SecurityContext.
        // Você pode adicionar um campo 'id' no UserDetails ou usar um serviço para buscar.
        return 1L; // placeholder
    }
}