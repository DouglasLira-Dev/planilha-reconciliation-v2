package com.reconciliation.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DivergenciaDTO {
    private String campo;
    private String valorFinanceiro;
    private String valorCadastro;
    private String tipo;
    private Double similaridade;
}