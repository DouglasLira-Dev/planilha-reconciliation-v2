package com.reconciliation.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryDetailDTO {
    private Long id;
    private String username;
    private LocalDateTime executedAt;
    private String filenameFinanceiro;
    private String filenameCadastro;
    private int totalConformes;
    private int totalFaltantes;
    private int totalExcedentes;
    private int totalDivergencias;
    private int totalErros;
    private int totalAvisos;
    private int totalConflitos;
    private int totalCancelados;
    private int totalPossiveisAbreviacoes;
    private String reportPath;
    private String reportDownloadUrl;
}