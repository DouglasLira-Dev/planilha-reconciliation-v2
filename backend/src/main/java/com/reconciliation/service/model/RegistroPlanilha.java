package com.reconciliation.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroPlanilha {

    // Campos originais (como lidos da planilha)
    private String matricula;
    private String cpf;
    private String nome;
    private String nivelEstagio;
    private String dataInicioStr;
    private String dataFimStr;
    private String banco;
    private String agencia;
    private String conta;
    private boolean cancelado;

    // Campos normalizados (para comparação)
    private String matriculaNorm;
    private String cpfNorm;
    private String nomeNorm;
    private String nivelEstagioNorm;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String bancoNorm;
    private String agenciaNorm;
    private String contaNorm;
}