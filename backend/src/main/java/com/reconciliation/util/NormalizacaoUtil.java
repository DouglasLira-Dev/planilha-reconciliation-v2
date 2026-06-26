package com.reconciliation.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public final class NormalizacaoUtil {

    private static final Map<String, String> MAPA_NIVEIS = new HashMap<>();
    static {
        MAPA_NIVEIS.put("formprof", "medio");
        MAPA_NIVEIS.put("formacao profissional", "medio");
        MAPA_NIVEIS.put("ensinomedio", "medio");
        MAPA_NIVEIS.put("superior", "superior");
        MAPA_NIVEIS.put("medio", "medio");
        MAPA_NIVEIS.put("médio", "medio");
        MAPA_NIVEIS.put("tecnico", "tecnico");
        MAPA_NIVEIS.put("técnico", "tecnico");
        MAPA_NIVEIS.put("posgraduacao", "posgraduacao");
        MAPA_NIVEIS.put("pós graduação", "posgraduacao");
        MAPA_NIVEIS.put("especializacao", "especializacao");
        MAPA_NIVEIS.put("especialização", "especializacao");
        MAPA_NIVEIS.put("mestrado", "mestrado");
        MAPA_NIVEIS.put("doutorado", "doutorado");
    }

    private NormalizacaoUtil() {}

    // Remove tudo que não é número
    public static String apenasNumeros(String valor) {
        if (valor == null) return "";
        return valor.replaceAll("[^0-9]", "");
    }

    // Normaliza matrícula: remove espaços extras
    public static String normalizarMatricula(String matricula) {
        if (matricula == null) return "";
        return matricula.trim().replaceAll("\\s+", " ");
    }

    // Normaliza CPF (apenas números, 11 dígitos com zeros à esquerda)
    public static String normalizarCpf(String cpf) {
        String numeros = apenasNumeros(cpf);
        if (numeros.isEmpty()) return "";
        while (numeros.length() < 11) {
            numeros = "0" + numeros;
        }
        if (numeros.length() > 11) {
            numeros = numeros.substring(0, 11);
        }
        return numeros;
    }

    // Normaliza nome: remove acentos, minúsculo, pontuação
    public static String normalizarNome(String nome) {
        if (nome == null) return "";
        String normalized = Normalizer.normalize(nome, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        normalized = normalized.toLowerCase();
        normalized = normalized.replaceAll("[^a-z0-9\\s]", "");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }

    // Normaliza nível de estágio
    public static String normalizarNivelEstagio(String nivel) {
        if (nivel == null) return "";
        String norm = normalizarNome(nivel);
        return MAPA_NIVEIS.getOrDefault(norm, norm);
    }

    // Converte número do Excel para LocalDate
    private static LocalDate excelNumberToLocalDate(double excelDateNumber) {
        long days = (long) excelDateNumber;
        return LocalDate.of(1900, 1, 1).plusDays(days - 2);
    }

    // Converte string para LocalDate (suporta vários formatos e serial Excel)
    public static LocalDate parseData(String dataStr) {
        if (dataStr == null || dataStr.trim().isEmpty()) return null;

        // Tenta interpretar como número do Excel
        if (dataStr.matches("\\d+")) {
            try {
                long number = Long.parseLong(dataStr);
                if (number > 0 && number < 100000) {
                    return excelNumberToLocalDate(number);
                }
            } catch (NumberFormatException ignored) {}
        }

        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy")
        };
        for (DateTimeFormatter fmt : formatters) {
            try {
                return LocalDate.parse(dataStr.trim(), fmt);
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    // Normaliza banco (apenas números)
    public static String normalizarBanco(String banco) {
        return apenasNumeros(banco);
    }

    // Normaliza agência (5 dígitos com zeros à esquerda)
    public static String normalizarAgencia(String agencia) {
        String numeros = apenasNumeros(agencia);
        if (numeros.isEmpty()) return "";
        while (numeros.length() < 5) {
            numeros = "0" + numeros;
        }
        if (numeros.length() > 5) {
            numeros = numeros.substring(0, 5);
        }
        return numeros;
    }

    // Normaliza conta (apenas números)
    public static String normalizarConta(String conta) {
        return apenasNumeros(conta);
    }

    // ========== MÉTODOS DE SIMILARIDADE ==========

    public static int distanciaLevenshtein(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) dp[i][j] = j;
                else if (j == 0) dp[i][j] = i;
                else {
                    int custo = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(Math.min(dp[i-1][j] + 1, dp[i][j-1] + 1), dp[i-1][j-1] + custo);
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }

    public static double similaridade(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        if (s1.isEmpty() && s2.isEmpty()) return 100;
        int distancia = distanciaLevenshtein(s1, s2);
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 100;
        return (1.0 - (double) distancia / maxLen) * 100;
    }

    public static boolean isErroDigitacao(String nome1, String nome2) {
        if (nome1 == null || nome2 == null) return false;
        int distancia = distanciaLevenshtein(nome1, nome2);
        return distancia <= 2 && distancia > 0;
    }

    public static boolean isAbreviacao(String nome1, String nome2) {
        if (nome1 == null || nome2 == null) return false;
        String[] palavras1 = nome1.split(" ");
        String[] palavras2 = nome2.split(" ");
        int palavrasComuns = 0;
        int palavrasSignificativas = 0;
        for (String p1 : palavras1) {
            if (p1.length() > 2) {
                palavrasSignificativas++;
                for (String p2 : palavras2) {
                    if (p2.contains(p1) || p1.contains(p2)) {
                        palavrasComuns++;
                        break;
                    }
                }
            }
        }
        return palavrasSignificativas > 0 && (double) palavrasComuns / palavrasSignificativas >= 0.6;
    }
}