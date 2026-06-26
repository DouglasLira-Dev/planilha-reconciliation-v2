package com.reconciliation.service;

import com.reconciliation.model.ReconciliationHistory;
import com.reconciliation.model.User;
import com.reconciliation.repository.ReconciliationHistoryRepository;
import com.reconciliation.repository.UserRepository;
import com.reconciliation.service.dto.HistoryDetailDTO;
import com.reconciliation.service.dto.HistoryListItemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryService {

    private final ReconciliationHistoryRepository historyRepository;
    private final UserRepository userRepository;

    public Page<HistoryListItemDTO> listHistory(String currentUsername,
                                                LocalDate startDate,
                                                LocalDate endDate,
                                                String filterUsername,
                                                Pageable pageable) {

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Se não for ADMIN, só pode ver os próprios registros
        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;
        String targetUsername = isAdmin && filterUsername != null ? filterUsername : currentUsername;

        // Converte datas para LocalDateTime (início e fim do dia)
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        Page<ReconciliationHistory> historyPage;

        if (isAdmin && filterUsername != null) {
            // ADMIN filtrando por outro usuário
            historyPage = historyRepository.findByUserUsernameAndExecutedAtBetween(
                    filterUsername, startDateTime, endDateTime, pageable);
        } else if (isAdmin) {
            // ADMIN vendo todos (sem filtro de usuário)
            historyPage = historyRepository.findByExecutedAtBetween(startDateTime, endDateTime, pageable);
        } else {
            // OPERADOR vendo apenas os próprios registros
            historyPage = historyRepository.findByUserUsernameAndExecutedAtBetween(
                    currentUsername, startDateTime, endDateTime, pageable);
        }

        List<HistoryListItemDTO> list = historyPage.getContent().stream()
                .map(this::toListItemDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(list, pageable, historyPage.getTotalElements());
    }

    public HistoryDetailDTO getHistoryDetail(Long id, String currentUsername) {
        ReconciliationHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Histórico não encontrado"));

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Verifica permissão
        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;
        boolean isOwner = history.getUser().getUsername().equals(currentUsername);

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Você não tem permissão para acessar este histórico");
        }

        return toDetailDTO(history);
    }

    @Transactional
    public void deleteHistory(Long id) {
        // Verifica se existe
        ReconciliationHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Histórico não encontrado"));

        // Deletar arquivo do disco (opcional)
        // File file = new File(history.getReportPath());
        // if (file.exists()) file.delete();

        historyRepository.delete(history);
        log.info("Histórico deletado: {}", id);
    }

    private HistoryListItemDTO toListItemDTO(ReconciliationHistory history) {
        return new HistoryListItemDTO(
                history.getId(),
                history.getUser().getUsername(),
                history.getExecutedAt(),
                history.getFilenameFinanceiro(),
                history.getFilenameCadastro(),
                history.getTotalConformes(),
                history.getTotalFaltantesCadastro(),
                history.getTotalExcedentesCadastro(),
                history.getTotalDivergencias(),
                history.getTotalErros(),
                history.getTotalAvisos(),
                history.getTotalConflitosCpf(),
                history.getTotalCancelados(),
                history.getTotalAbreviacoes()
        );
    }

    private HistoryDetailDTO toDetailDTO(ReconciliationHistory history) {
        return new HistoryDetailDTO(
                history.getId(),
                history.getUser().getUsername(),
                history.getExecutedAt(),
                history.getFilenameFinanceiro(),
                history.getFilenameCadastro(),
                history.getTotalConformes(),
                history.getTotalFaltantesCadastro(),
                history.getTotalExcedentesCadastro(),
                history.getTotalDivergencias(),
                history.getTotalErros(),
                history.getTotalAvisos(),
                history.getTotalConflitosCpf(),
                history.getTotalCancelados(),
                history.getTotalAbreviacoes(),
                history.getReportPath(),
                "/api/report/" + history.getId()
        );
    }
}