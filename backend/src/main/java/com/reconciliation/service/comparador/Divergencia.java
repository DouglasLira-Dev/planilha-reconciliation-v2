package com.reconciliation.service.comparador;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Divergencia {
    private String chave;
    private String campo;
    private String valorFinanceiro;
    private String valorCadastro;
    private String tipo; // "ERRO" ou "AVISO"
    private Double similaridade;
}