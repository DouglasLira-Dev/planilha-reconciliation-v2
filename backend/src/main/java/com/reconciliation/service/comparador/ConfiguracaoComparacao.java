package com.reconciliation.service.comparador;

import lombok.Data;

@Data
public class ConfiguracaoComparacao {
    private double limiarSimilaridadeNomes = 85.0;
    private boolean compararMatricula = true;
    private boolean compararCPF = true;
    private boolean compararNome = true;
    private boolean compararNivelEstagio = true;
    private boolean compararDataInicio = true;
    private boolean compararDataFim = true;
    private boolean compararBanco = true;
    private boolean compararAgencia = true;
    private boolean compararConta = true;

    public static ConfiguracaoComparacao criarPadrao() {
        return new ConfiguracaoComparacao();
    }

    public static ConfiguracaoComparacao criarEssencial() {
        ConfiguracaoComparacao config = new ConfiguracaoComparacao();
        config.setCompararBanco(false);
        config.setCompararAgencia(false);
        config.setCompararConta(false);
        return config;
    }
}