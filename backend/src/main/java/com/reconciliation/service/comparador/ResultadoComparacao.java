package com.reconciliation.service.comparador;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.reconciliation.service.model.RegistroPlanilha;

@Data
@NoArgsConstructor
public class ResultadoComparacao {

    public static class PossivelAbreviatura {
        private RegistroPlanilha financeiro;
        private RegistroPlanilha cadastro;
        private double similaridade;

        public PossivelAbreviatura(RegistroPlanilha financeiro, RegistroPlanilha cadastro, double similaridade) {
            this.financeiro = financeiro;
            this.cadastro = cadastro;
            this.similaridade = similaridade;
        }
    }

    private List<RegistroPlanilha> faltantesNoCadastro = new ArrayList<>();
    private List<RegistroPlanilha> excedentesNoCadastro = new ArrayList<>();
    private List<RegistroPlanilha> conformes = new ArrayList<>();
    private Map<String, List<Divergencia>> divergenciasPorChave = new HashMap<>();
    private List<RegistroPlanilha> conflitosCPFMatricula = new ArrayList<>();
    private List<RegistroPlanilha> canceladosNoCadastro = new ArrayList<>();
    private List<PossivelAbreviatura> possiveisAbreviacoes = new ArrayList<>();
    private ConfiguracaoComparacao configuracao = ConfiguracaoComparacao.criarPadrao();

    public int getTotalFaltantes() { return faltantesNoCadastro.size(); }
    public int getTotalConformes() { return conformes.size(); }
    public int getTotalExcedentes() { return excedentesNoCadastro.size(); }
    public int getTotalCancelados() { return canceladosNoCadastro.size(); }
    public int getTotalDivergencias() { return divergenciasPorChave.size(); }
    public int getTotalConflitos() { return conflitosCPFMatricula.size(); }
    public int getTotalPossiveisAbreviacoes() { return possiveisAbreviacoes.size(); }

    public int getTotalErros() {
        int total = 0;
        for (List<Divergencia> lista : divergenciasPorChave.values()) {
            for (Divergencia d : lista) {
                if ("ERRO".equals(d.getTipo())) total++;
            }
        }
        return total;
    }

    public int getTotalAvisos() {
        int total = 0;
        for (List<Divergencia> lista : divergenciasPorChave.values()) {
            for (Divergencia d : lista) {
                if ("AVISO".equals(d.getTipo())) total++;
            }
        }
        return total;
    }

    public void addConforme(RegistroPlanilha reg) { this.conformes.add(reg); }
    public void addFaltante(RegistroPlanilha reg) { this.faltantesNoCadastro.add(reg); }
    public void addExcedente(RegistroPlanilha reg) { this.excedentesNoCadastro.add(reg); }
    public void addCancelado(RegistroPlanilha reg) { this.canceladosNoCadastro.add(reg); }
    public void addConflito(RegistroPlanilha reg) { this.conflitosCPFMatricula.add(reg); }
    public void addPossivelAbreviatura(RegistroPlanilha fin, RegistroPlanilha cad, double similaridade) {
        this.possiveisAbreviacoes.add(new PossivelAbreviatura(fin, cad, similaridade));
    }
    public void addDivergencia(String chave, Divergencia div) {
        divergenciasPorChave.computeIfAbsent(chave, k -> new ArrayList<>()).add(div);
    }
}