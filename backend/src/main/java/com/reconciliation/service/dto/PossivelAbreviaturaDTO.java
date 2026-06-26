package com.reconciliation.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PossivelAbreviaturaDTO {
    private String matricula;
    private String cpf;
    private String nomeFinanceiro;
    private String nomeCadastro;
    private double similaridade;
}