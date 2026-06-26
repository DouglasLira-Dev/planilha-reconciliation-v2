package com.reconciliation.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationResultDTO {
    private int totalFinanceiro;
    private int totalCadastro;
    private int totalConformes;
    private int totalFaltantes;
    private int totalExcedentes;
    private int totalDivergencias;
    private int totalErros;
    private int totalAvisos;
    private int totalConflitos;
    private int totalCancelados;
    private int totalPossiveisAbreviacoes;
    private Map<String, List<DivergenciaDTO>> divergencias;
    private List<RegistroSimplesDTO> faltantes;
    private List<RegistroSimplesDTO> excedentes;
    private List<RegistroSimplesDTO> conflitos;
    private List<PossivelAbreviaturaDTO> possiveisAbreviacoes;
    private Long historyId;
    private String reportDownloadUrl;
}