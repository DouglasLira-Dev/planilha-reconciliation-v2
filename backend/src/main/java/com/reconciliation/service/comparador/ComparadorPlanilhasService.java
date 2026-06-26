package com.reconciliation.service.comparador;

import com.reconciliation.service.model.RegistroPlanilha;
import com.reconciliation.util.NormalizacaoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ComparadorPlanilhasService {

    public ResultadoComparacao comparar(List<RegistroPlanilha> financeiro, List<RegistroPlanilha> cadastro) {
        return comparar(financeiro, cadastro, ConfiguracaoComparacao.criarPadrao());
    }

    public ResultadoComparacao comparar(List<RegistroPlanilha> financeiro,
                                        List<RegistroPlanilha> cadastro,
                                        ConfiguracaoComparacao config) {
        ResultadoComparacao resultado = new ResultadoComparacao();
        resultado.setConfiguracao(config);

        Map<String, RegistroPlanilha> mapFinanceiro = new HashMap<>();
        Map<String, RegistroPlanilha> mapCadastro = new HashMap<>();
        Map<String, RegistroPlanilha> mapPorCPF = new HashMap<>();

        for (RegistroPlanilha reg : financeiro) {
            String chave = chaveComposta(reg);
            if (chave != null && !chave.isEmpty() && !chave.equals("|")) {
                mapFinanceiro.put(chave, reg);
                mapPorCPF.put(reg.getCpfNorm(), reg);
            }
        }

        for (RegistroPlanilha reg : cadastro) {
            if (reg.isCancelado()) {
                resultado.addCancelado(reg);
                continue;
            }
            String chave = chaveComposta(reg);
            if (chave != null && !chave.isEmpty() && !chave.equals("|")) {
                mapCadastro.put(chave, reg);
            }
        }

        // Faltantes
        for (Map.Entry<String, RegistroPlanilha> entry : mapFinanceiro.entrySet()) {
            if (!mapCadastro.containsKey(entry.getKey())) {
                resultado.addFaltante(entry.getValue());
            }
        }

        // Excedentes
        for (Map.Entry<String, RegistroPlanilha> entry : mapCadastro.entrySet()) {
            if (!mapFinanceiro.containsKey(entry.getKey())) {
                resultado.addExcedente(entry.getValue());
            }
        }

        // Divergências e conformes
        for (Map.Entry<String, RegistroPlanilha> entry : mapFinanceiro.entrySet()) {
            String chave = entry.getKey();
            if (mapCadastro.containsKey(chave)) {
                RegistroPlanilha regFin = entry.getValue();
                RegistroPlanilha regCad = mapCadastro.get(chave);

                List<Divergencia> divergencias = compararRegistros(chave, regFin, regCad, config);
                List<Divergencia> divergenciasReais = new ArrayList<>();

                for (Divergencia div : divergencias) {
                    if (div.getCampo().contains("abreviação")) {
                        resultado.addPossivelAbreviatura(regFin, regCad, div.getSimilaridade() != null ? div.getSimilaridade() : 0.0);
                        resultado.addConforme(regFin);
                    } else {
                        divergenciasReais.add(div);
                    }
                }

                for (Divergencia div : divergenciasReais) {
                    resultado.addDivergencia(chave, div);
                }

                if (divergenciasReais.isEmpty() && divergencias.isEmpty()) {
                    resultado.addConforme(regFin);
                }
            }
        }

        // Conflitos CPF
        for (RegistroPlanilha regCad : cadastro) {
            String cpf = regCad.getCpfNorm();
            if (mapPorCPF.containsKey(cpf)) {
                RegistroPlanilha regFin = mapPorCPF.get(cpf);
                if (!regFin.getMatriculaNorm().equals(regCad.getMatriculaNorm())) {
                    String chaveConflito = cpf + "|" + regCad.getMatriculaNorm();
                    if (!mapFinanceiro.containsKey(chaveConflito) && !mapCadastro.containsKey(chaveComposta(regFin))) {
                        resultado.addConflito(regCad);
                    }
                }
            }
        }

        return resultado;
    }

    private String chaveComposta(RegistroPlanilha reg) {
        return reg.getCpfNorm() + "|" + reg.getMatriculaNorm();
    }

    private List<Divergencia> compararRegistros(String chave,
                                                RegistroPlanilha regFin,
                                                RegistroPlanilha regCad,
                                                ConfiguracaoComparacao config) {
        List<Divergencia> divergencias = new ArrayList<>();

        if (!regFin.getMatriculaNorm().equals(regCad.getMatriculaNorm())) {
            divergencias.add(new Divergencia(chave, "Matrícula", regFin.getMatriculaNorm(), regCad.getMatriculaNorm(), "ERRO", null));
        }

        if (!regFin.getCpfNorm().equals(regCad.getCpfNorm())) {
            divergencias.add(new Divergencia(chave, "CPF", regFin.getCpfNorm(), regCad.getCpfNorm(), "ERRO", null));
        }

        if (config.isCompararNome()) {
            if (!regFin.getNomeNorm().equals(regCad.getNomeNorm())) {
                double similaridade = NormalizacaoUtil.similaridade(regFin.getNomeNorm(), regCad.getNomeNorm());

                if (NormalizacaoUtil.isErroDigitacao(regFin.getNomeNorm(), regCad.getNomeNorm())) {
                    divergencias.add(new Divergencia(chave, "Nome", regFin.getNomeNorm(), regCad.getNomeNorm(), "ERRO", null));
                } else if (NormalizacaoUtil.isAbreviacao(regFin.getNomeNorm(), regCad.getNomeNorm())) {
                    divergencias.add(new Divergencia(chave, "Nome (abreviação)", regFin.getNomeNorm(), regCad.getNomeNorm(), "AVISO", similaridade));
                } else if (similaridade >= config.getLimiarSimilaridadeNomes()) {
                    divergencias.add(new Divergencia(chave, "Nome (similar)", regFin.getNomeNorm(), regCad.getNomeNorm(), "AVISO", similaridade));
                } else {
                    divergencias.add(new Divergencia(chave, "Nome", regFin.getNomeNorm(), regCad.getNomeNorm(), "ERRO", null));
                }
            }
        }

        if (config.isCompararNivelEstagio()) {
            if (!regFin.getNivelEstagioNorm().equals(regCad.getNivelEstagioNorm())) {
                divergencias.add(new Divergencia(chave, "Nível Estágio", regFin.getNivelEstagioNorm(), regCad.getNivelEstagioNorm(), "ERRO", null));
            }
        }

        if (config.isCompararDataInicio()) {
            if (!Objects.equals(regFin.getDataInicio(), regCad.getDataInicio())) {
                divergencias.add(new Divergencia(chave, "Data Início",
                        regFin.getDataInicio() != null ? regFin.getDataInicio().toString() : "null",
                        regCad.getDataInicio() != null ? regCad.getDataInicio().toString() : "null", "ERRO", null));
            }
        }

        if (config.isCompararDataFim()) {
            if (!Objects.equals(regFin.getDataFim(), regCad.getDataFim())) {
                divergencias.add(new Divergencia(chave, "Data Fim",
                        regFin.getDataFim() != null ? regFin.getDataFim().toString() : "null",
                        regCad.getDataFim() != null ? regCad.getDataFim().toString() : "null", "ERRO", null));
            }
        }

        if (config.isCompararBanco()) {
            if (!regFin.getBancoNorm().equals(regCad.getBancoNorm())) {
                divergencias.add(new Divergencia(chave, "Banco", regFin.getBancoNorm(), regCad.getBancoNorm(), "ERRO", null));
            }
        }

        if (config.isCompararAgencia()) {
            if (!regFin.getAgenciaNorm().equals(regCad.getAgenciaNorm())) {
                divergencias.add(new Divergencia(chave, "Agência", regFin.getAgenciaNorm(), regCad.getAgenciaNorm(), "ERRO", null));
            }
        }

        if (config.isCompararConta()) {
            if (!regFin.getContaNorm().equals(regCad.getContaNorm())) {
                divergencias.add(new Divergencia(chave, "Conta", regFin.getContaNorm(), regCad.getContaNorm(), "ERRO", null));
            }
        }

        return divergencias;
    }
}