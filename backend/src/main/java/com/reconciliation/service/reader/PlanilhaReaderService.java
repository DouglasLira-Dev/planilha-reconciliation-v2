package com.reconciliation.service.reader;

import com.reconciliation.service.model.RegistroPlanilha;
import com.reconciliation.util.NormalizacaoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class PlanilhaReaderService {

    /**
     * Lê a primeira aba da planilha.
     */
    public List<RegistroPlanilha> ler(MultipartFile file) throws IOException {
        return ler(file.getInputStream());
    }

    /**
     * Lê a primeira aba do InputStream.
     */
    public List<RegistroPlanilha> ler(InputStream inputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            return lerAba(sheet);
        }
    }

    /**
     * Lê múltiplas abas da planilha.
     */
    public List<RegistroPlanilha> ler(MultipartFile file, List<String> nomesAbas) throws IOException {
        return ler(file.getInputStream(), nomesAbas);
    }

    public List<RegistroPlanilha> ler(InputStream inputStream, List<String> nomesAbas) throws IOException {
        List<RegistroPlanilha> listaConsolidada = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            if (nomesAbas == null || nomesAbas.isEmpty()) {
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    String nomeAba = workbook.getSheetName(i);
                    if (isAbaDados(nomeAba)) {
                        Sheet sheet = workbook.getSheetAt(i);
                        listaConsolidada.addAll(lerAba(sheet));
                    }
                }
            } else {
                for (String nomeAba : nomesAbas) {
                    Sheet sheet = workbook.getSheet(nomeAba);
                    if (sheet == null) {
                        log.warn("Aba não encontrada: {}", nomeAba);
                        continue;
                    }
                    listaConsolidada.addAll(lerAba(sheet));
                }
            }
        }
        return listaConsolidada;
    }

    /**
     * Retorna os nomes de todas as abas.
     */
    public List<String> listarNomesAbas(MultipartFile file) throws IOException {
        List<String> nomes = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                nomes.add(workbook.getSheetName(i));
            }
        }
        return nomes;
    }

    /**
     * Retorna apenas nomes de abas de dados.
     */
    public List<String> listarNomesAbasDados(MultipartFile file) throws IOException {
        List<String> nomes = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                String nome = workbook.getSheetName(i);
                if (isAbaDados(nome)) {
                    nomes.add(nome);
                }
            }
        }
        return nomes;
    }

    private boolean isAbaDados(String nomeAba) {
        String lower = nomeAba.toLowerCase();
        return !(lower.contains("legenda") || lower.contains("instrução") 
                || lower.contains("resumo") || lower.contains("capa")
                || lower.contains("índice") || lower.startsWith("readme"));
    }

    private List<RegistroPlanilha> lerAba(Sheet sheet) {
        List<RegistroPlanilha> lista = new ArrayList<>();
        Iterator<Row> rowIterator = sheet.iterator();
        if (!rowIterator.hasNext()) return lista;

        // Cabeçalho
        Row headerRow = rowIterator.next();
        Map<String, Integer> colunaIndex = new HashMap<>();
        for (Cell cell : headerRow) {
            String nomeColuna = cell.getStringCellValue().trim().toLowerCase()
                    .replaceAll("[^a-z0-9]", "");
            colunaIndex.put(nomeColuna, cell.getColumnIndex());
        }

        int idxMatricula = obterIndice(colunaIndex, "matricula", "matrícula", "numero", "prontuario", "contrato", "registro");
        int idxCpf = obterIndice(colunaIndex, "cpf", "cpfcnpj", "documento");
        int idxNome = obterIndice(colunaIndex, "nome", "nomestagiario", "estagiario", "nomeestagiario", "nome abreviado", "nomeabreviado");
        int idxNivel = obterIndice(colunaIndex, "nivel", "nivelestagio", "grau", "escolaridade", "estagio",
                "niveldeestagio", "nivel_estagio", "nivel-estagio", "niveldoestagio", "estagionivel", "nvel");
        int idxDataInicio = obterIndice(colunaIndex, "datainicio", "iniciocontrato", "data_inicio", "inicio", "dtinicio", "dt admissao", "dtadmissao", "teie");
        int idxDataFim = obterIndice(colunaIndex, "datafim", "fimcontrato", "data_fim", "fim", "dtfim","dt fim cont.", "dtfimcont.", "dt_prevtermino", "dtprevtermino");
        int idxBanco = obterIndice(colunaIndex, "banco", "codigobanco", "bancocodigo", "bco", "cod_banco", "codbanco");
        int idxAgencia = obterIndice(colunaIndex, "agencia", "agenciabanco", "nr_agencia");
        int idxConta = obterIndice(colunaIndex, "conta", "contacorrente", "numeroconta", "nr_conta", "c.corrente", "ccorrente");

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            // Ignora linha vazia
            boolean linhaVazia = true;
            for (int i = 0; i < 9; i++) {
                Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String valor = obterValorCelula(cell);
                if (valor != null && !valor.isEmpty()) {
                    linhaVazia = false;
                    break;
                }
            }
            if (linhaVazia) continue;

            RegistroPlanilha registro = lerLinha(row, idxMatricula, idxCpf, idxNome, idxNivel,
                    idxDataInicio, idxDataFim, idxBanco, idxAgencia, idxConta);
            if (registro != null) {
                lista.add(registro);
            }
        }
        return lista;
    }

    private RegistroPlanilha lerLinha(Row row,
                                      int idxMatricula, int idxCpf, int idxNome, int idxNivel,
                                      int idxDataInicio, int idxDataFim, int idxBanco, int idxAgencia, int idxConta) {
        RegistroPlanilha registro = new RegistroPlanilha();
        registro.setMatricula(obterValorCelulaComIndice(row, idxMatricula));
        registro.setCpf(obterValorCelulaComIndice(row, idxCpf));
        registro.setNome(obterValorCelulaComIndice(row, idxNome));
        registro.setNivelEstagio(obterValorCelulaComIndice(row, idxNivel));
        registro.setDataInicioStr(obterValorCelulaComIndice(row, idxDataInicio));

        // Detecta cancelado
        String dataInicio = registro.getDataInicioStr();
        if (dataInicio != null && dataInicio.toLowerCase().contains("cancelado")) {
            registro.setCancelado(true);
            registro.setDataInicioStr("");
        } else {
            registro.setCancelado(false);
        }

        registro.setDataFimStr(obterValorCelulaComIndice(row, idxDataFim));
        registro.setBanco(obterValorCelulaComIndice(row, idxBanco));
        registro.setAgencia(obterValorCelulaComIndice(row, idxAgencia));
        registro.setConta(obterValorCelulaComIndice(row, idxConta));

        // Normaliza
        registro.setMatriculaNorm(NormalizacaoUtil.normalizarMatricula(registro.getMatricula()));
        registro.setCpfNorm(NormalizacaoUtil.normalizarCpf(registro.getCpf()));
        registro.setNomeNorm(NormalizacaoUtil.normalizarNome(registro.getNome()));
        registro.setNivelEstagioNorm(NormalizacaoUtil.normalizarNivelEstagio(registro.getNivelEstagio()));
        registro.setDataInicio(NormalizacaoUtil.parseData(registro.getDataInicioStr()));
        registro.setDataFim(NormalizacaoUtil.parseData(registro.getDataFimStr()));
        registro.setBancoNorm(NormalizacaoUtil.normalizarBanco(registro.getBanco()));
        registro.setAgenciaNorm(NormalizacaoUtil.normalizarAgencia(registro.getAgencia()));
        registro.setContaNorm(NormalizacaoUtil.normalizarConta(registro.getConta()));

        // Ignora linha sem matrícula ou CPF
        if (registro.getMatriculaNorm().isEmpty() || registro.getCpfNorm().isEmpty()) {
            return null;
        }
        return registro;
    }

    private int obterIndice(Map<String, Integer> mapa, String... possiveisNomes) {
        for (String nome : possiveisNomes) {
            String chave = nome.toLowerCase().replaceAll("[^a-z0-9]", "");
            if (mapa.containsKey(chave)) {
                return mapa.get(chave);
            }
        }
        return -1;
    }

    private String obterValorCelulaComIndice(Row row, int idx) {
        if (idx < 0) return "";
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        return obterValorCelula(cell);
    }

    private String obterValorCelula(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    try {
                        return cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception e) {
                        return cell.getDateCellValue().toString();
                    }
                }
                double valor = cell.getNumericCellValue();
                if (valor == Math.floor(valor)) {
                    return String.valueOf((long) valor);
                } else {
                    return String.valueOf(valor);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }
}