package com.reconciliation.service.relatorio;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.reconciliation.service.comparador.Divergencia;
import com.reconciliation.service.comparador.ResultadoComparacao;
import com.reconciliation.service.model.RegistroPlanilha;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RelatorioExcelService {

    // Estilos serão armazenados como variáveis de instância durante a criação do workbook
    private CellStyle estiloTitulo;
    private CellStyle estiloCabecalho;
    private CellStyle estiloErro;
    private CellStyle estiloAviso;
    private CellStyle estiloSucesso;
    private CellStyle estiloDestaque;

    /**
     * Gera o relatório Excel completo e retorna os bytes do arquivo.
     *
     * @param resultado         Resultado da comparação
     * @param nomeFinanceiro    Nome do arquivo do financeiro (para exibição)
     * @param nomeCadastro      Nome do arquivo do cadastro (para exibição)
     * @param totalFinanceiro   Total de registros do financeiro (após filtro)
     * @param totalCadastro     Total de registros do cadastro
     * @return byte[] do arquivo Excel
     * @throws IOException Em caso de erro na escrita
     */
    public byte[] gerar(ResultadoComparacao resultado,
                        String nomeFinanceiro,
                        String nomeCadastro,
                        int totalFinanceiro,
                        int totalCadastro) throws IOException {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            criarEstilos(workbook);

            criarAbaResumo(workbook, resultado, nomeFinanceiro, nomeCadastro, totalFinanceiro, totalCadastro);
            criarAbaConformes(workbook, resultado);
            criarAbaFaltantes(workbook, resultado);
            criarAbaExcedentes(workbook, resultado);
            criarAbaDivergencias(workbook, resultado);
            criarAbaConflitos(workbook, resultado);
            criarAbaPossiveisAbreviacoes(workbook, resultado);
            criarAbaCancelados(workbook, resultado);
            criarAbaDetalhado(workbook, resultado);

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erro ao gerar relatório Excel", e);
            throw new IOException("Falha na geração do relatório", e);
        }
    }

    // ======================== ESTILOS ============================

    private void criarEstilos(Workbook workbook) {
        estiloTitulo = workbook.createCellStyle();
        Font fontTitulo = workbook.createFont();
        fontTitulo.setBold(true);
        fontTitulo.setFontHeightInPoints((short) 14);
        estiloTitulo.setFont(fontTitulo);

        estiloCabecalho = workbook.createCellStyle();
        Font fontCabecalho = workbook.createFont();
        fontCabecalho.setBold(true);
        estiloCabecalho.setFont(fontCabecalho);
        estiloCabecalho.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estiloCabecalho.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloCabecalho.setBorderBottom(BorderStyle.THIN);
        estiloCabecalho.setBorderTop(BorderStyle.THIN);
        estiloCabecalho.setBorderLeft(BorderStyle.THIN);
        estiloCabecalho.setBorderRight(BorderStyle.THIN);

        estiloErro = workbook.createCellStyle();
        estiloErro.setFillForegroundColor(IndexedColors.RED.getIndex());
        estiloErro.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloErro.setBorderBottom(BorderStyle.THIN);
        estiloErro.setBorderTop(BorderStyle.THIN);
        estiloErro.setBorderLeft(BorderStyle.THIN);
        estiloErro.setBorderRight(BorderStyle.THIN);

        estiloAviso = workbook.createCellStyle();
        estiloAviso.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        estiloAviso.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloAviso.setBorderBottom(BorderStyle.THIN);
        estiloAviso.setBorderTop(BorderStyle.THIN);
        estiloAviso.setBorderLeft(BorderStyle.THIN);
        estiloAviso.setBorderRight(BorderStyle.THIN);

        estiloSucesso = workbook.createCellStyle();
        estiloSucesso.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        estiloSucesso.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        estiloDestaque = workbook.createCellStyle();
        estiloDestaque.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        estiloDestaque.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    // ======================== ABAS ============================

    // ---- 01 - Resumo ----
    private void criarAbaResumo(Workbook workbook, ResultadoComparacao resultado,
                                String nomeFinanceiro, String nomeCadastro,
                                int totalFinanceiro, int totalCadastro) {
        Sheet sheet = workbook.createSheet("01 - Resumo");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("RELATÓRIO DE COMPARAÇÃO DE PLANILHAS");
        titleCell.setCellStyle(estiloTitulo);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));

        rowNum++;

        addInfoRow(sheet, rowNum++, "Data/Hora da geração:",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        addInfoRow(sheet, rowNum++, "Planilha do Financeiro:", nomeFinanceiro);
        addInfoRow(sheet, rowNum++, "Planilha de Cadastro:", nomeCadastro);
        addInfoRow(sheet, rowNum++, "Chave de comparação:", "CPF + Matrícula");
        addInfoRow(sheet, rowNum++, "Limiar similaridade de nomes:",
                resultado.getConfiguracao().getLimiarSimilaridadeNomes() + "%");

        rowNum++;
        rowNum++;

        addInfoRow(sheet, rowNum++, "📊 ESTATÍSTICAS DA COMPARAÇÃO", "");
        rowNum++;
        addInfoRow(sheet, rowNum++, "Total de Registros do Financeiro:", String.valueOf(totalFinanceiro));
        addInfoRow(sheet, rowNum++, "Total de Registros do Cadastro:", String.valueOf(totalCadastro));
        rowNum++;
        addInfoRow(sheet, rowNum++, "✅ Registros conformes (idênticos):",
                String.valueOf(resultado.getTotalConformes()));
        addInfoRow(sheet, rowNum++, "❌ Registros que se encontram apenas no Financeiro:",
                String.valueOf(resultado.getTotalFaltantes()));
        addInfoRow(sheet, rowNum++, "⚠️ Registros que não estão no Financeiro:",
                String.valueOf(resultado.getTotalExcedentes()));
        addInfoRow(sheet, rowNum++, "🔄 Registros com divergências:",
                String.valueOf(resultado.getTotalDivergencias()));
        addInfoRow(sheet, rowNum++, "   ├── ❌ Erros:",
                String.valueOf(resultado.getTotalErros()));
        addInfoRow(sheet, rowNum++, "   └── ⚠️ Avisos:",
                String.valueOf(resultado.getTotalAvisos()));
        addInfoRow(sheet, rowNum++, "⚡ Incompatibilidades (CPF igual, matrícula diferente):",
                String.valueOf(resultado.getTotalConflitos()));
        addInfoRow(sheet, rowNum++, "⚠️ Cancelados no cadastro (ignorados):",
                String.valueOf(resultado.getTotalCancelados()));
        addInfoRow(sheet, rowNum++, "🔍 Possíveis abreviações (incluídas nos conformes):",
                String.valueOf(resultado.getTotalPossiveisAbreviacoes()));

        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 6000);
    }

    private void addInfoRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(estiloCabecalho);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(estiloDestaque);
    }

    // ---- 02 - Conformes ----
    private void criarAbaConformes(Workbook workbook, ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("02 - Registros Conformes");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REGISTROS CONFORMES (IDÊNTICOS NAS DUAS PLANILHAS)");
        titleCell.setCellStyle(estiloTitulo);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 8));

        rowNum++;

        if (resultado.getConformes().isEmpty()) {
            Row emptyRow = sheet.createRow(rowNum);
            emptyRow.createCell(0).setCellValue("Nenhum registro conforme encontrado.");
            return;
        }

        String[] headers = {"Matrícula", "CPF", "Nome", "Nível Estágio", "Data Início",
                "Data Fim", "Banco", "Agência", "Conta"};
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estiloCabecalho);
        }

        for (RegistroPlanilha reg : resultado.getConformes()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(reg.getMatricula());
            row.createCell(1).setCellValue(reg.getCpf());
            row.createCell(2).setCellValue(reg.getNome());
            row.createCell(3).setCellValue(reg.getNivelEstagio());
            row.createCell(4).setCellValue(reg.getDataInicioStr());
            row.createCell(5).setCellValue(reg.getDataFimStr());
            row.createCell(6).setCellValue(reg.getBanco());
            row.createCell(7).setCellValue(reg.getAgencia());
            row.createCell(8).setCellValue(reg.getConta());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 5000);
        }
    }

    // ---- 03 - Faltantes ----
    private void criarAbaFaltantes(Workbook workbook, ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("03 - Faltantes no Cadastro");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REGISTROS QUE SE ENCONTRAM APENAS NO FINANCEIRO (não estão no cadastro)");
        titleCell.setCellStyle(estiloTitulo);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 8));

        rowNum++;

        if (resultado.getFaltantesNoCadastro().isEmpty()) {
            Row emptyRow = sheet.createRow(rowNum);
            emptyRow.createCell(0).setCellValue("Nenhum registro faltante encontrado.");
            return;
        }

        String[] headers = {"Matrícula", "CPF", "Nome", "Nível Estágio", "Data Início",
                "Data Fim", "Banco", "Agência", "Conta"};
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estiloCabecalho);
        }

        for (RegistroPlanilha reg : resultado.getFaltantesNoCadastro()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(reg.getMatricula());
            row.createCell(1).setCellValue(reg.getCpf());
            row.createCell(2).setCellValue(reg.getNome());
            row.createCell(3).setCellValue(reg.getNivelEstagio());
            row.createCell(4).setCellValue(reg.getDataInicioStr());
            row.createCell(5).setCellValue(reg.getDataFimStr());
            row.createCell(6).setCellValue(reg.getBanco());
            row.createCell(7).setCellValue(reg.getAgencia());
            row.createCell(8).setCellValue(reg.getConta());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 5000);
        }
    }

    // ---- 04 - Excedentes ----
    private void criarAbaExcedentes(Workbook workbook, ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("04 - Excedentes no Cadastro");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REGISTROS QUE NÃO ESTÃO NO FINANCEIRO (excedentes no cadastro)");
        titleCell.setCellStyle(estiloTitulo);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 8));

        rowNum++;

        if (resultado.getExcedentesNoCadastro().isEmpty()) {
            Row emptyRow = sheet.createRow(rowNum);
            emptyRow.createCell(0).setCellValue("Nenhum registro excedente encontrado.");
            return;
        }

        String[] headers = {"Matrícula", "CPF", "Nome", "Nível Estágio", "Data Início",
                "Data Fim", "Banco", "Agência", "Conta"};
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estiloCabecalho);
        }

        for (RegistroPlanilha reg : resultado.getExcedentesNoCadastro()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(reg.getMatricula());
            row.createCell(1).setCellValue(reg.getCpf());
            row.createCell(2).setCellValue(reg.getNome());
            row.createCell(3).setCellValue(reg.getNivelEstagio());
            row.createCell(4).setCellValue(reg.getDataInicioStr());
            row.createCell(5).setCellValue(reg.getDataFimStr());
            row.createCell(6).setCellValue(reg.getBanco());
            row.createCell(7).setCellValue(reg.getAgencia());
            row.createCell(8).setCellValue(reg.getConta());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 5000);
        }
    }

    // ---- 05 - Divergências ----
    private void criarAbaDivergencias(Workbook workbook, ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("05 - Divergências");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("DIVERGÊNCIAS ENCONTRADAS");
        titleCell.setCellStyle(estiloTitulo);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5));

        rowNum++;

        if (resultado.getDivergenciasPorChave().isEmpty()) {
            Row emptyRow = sheet.createRow(rowNum);
            emptyRow.createCell(0).setCellValue("Nenhuma divergência encontrada.");
            return;
        }

        String[] headers = {"CPF", "Matrícula", "Campo", "Valor Financeiro", "Valor Cadastro", "Tipo"};
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estiloCabecalho);
        }

        for (Map.Entry<String, List<Divergencia>> entry : resultado.getDivergenciasPorChave().entrySet()) {
            String[] partes = entry.getKey().split("\\|");
            String cpf = partes.length > 0 ? partes[0] : "";
            String matricula = partes.length > 1 ? partes[1] : "";

            for (Divergencia div : entry.getValue()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(cpf);
                row.createCell(1).setCellValue(matricula);
                row.createCell(2).setCellValue(div.getCampo());
                row.createCell(3).setCellValue(div.getValorFinanceiro());
                row.createCell(4).setCellValue(div.getValorCadastro());

                Cell tipoCell = row.createCell(5);
                tipoCell.setCellValue(div.getTipo());
                if ("ERRO".equals(div.getTipo())) {
                    tipoCell.setCellStyle(estiloErro);
                } else if ("AVISO".equals(div.getTipo())) {
                    tipoCell.setCellStyle(estiloAviso);
                }
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 5000);
        }
    }

    // ---- 06 - Conflitos ----
    private void criarAbaConflitos(Workbook workbook, ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("06 - Conflitos CPF/Matrícula");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("CONFLITOS (MESMO CPF COM MATRÍCULA DIFERENTE)");
        titleCell.setCellStyle(estiloTitulo);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 9));

        rowNum++;

        if (resultado.getConflitosCPFMatricula().isEmpty()) {
            Row emptyRow = sheet.createRow(rowNum);
            emptyRow.createCell(0).setCellValue("Nenhum conflito encontrado.");
            return;
        }

        String[] headers = {"Matrícula", "CPF", "Nome", "Nível Estágio", "Data Início",
                "Data Fim", "Banco", "Agência", "Conta", "Observação"};
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estiloCabecalho);
        }

        for (RegistroPlanilha reg : resultado.getConflitosCPFMatricula()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(reg.getMatricula());
            row.createCell(1).setCellValue(reg.getCpf());
            row.createCell(2).setCellValue(reg.getNome());
            row.createCell(3).setCellValue(reg.getNivelEstagio());
            row.createCell(4).setCellValue(reg.getDataInicioStr());
            row.createCell(5).setCellValue(reg.getDataFimStr());
            row.createCell(6).setCellValue(reg.getBanco());
            row.createCell(7).setCellValue(reg.getAgencia());
            row.createCell(8).setCellValue(reg.getConta());
            row.createCell(9).setCellValue("Verificar manualmente - mesmo CPF com matrícula diferente");

            for (int i = 0; i < 10; i++) {
                if (row.getCell(i) == null) row.createCell(i);
                row.getCell(i).setCellStyle(estiloErro);
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 5000);
        }
        sheet.setColumnWidth(9, 10000);
    }

    // ---- 07 - Possíveis Abreviações ----
    private void criarAbaPossiveisAbreviacoes(Workbook workbook, ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("07 - Possíveis Abreviações");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REGISTROS COM POSSÍVEIS ABREVIAÇÕES (CONSIDERADOS CONFORMES)");
        titleCell.setCellStyle(estiloTitulo);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 10));

        rowNum++;

        if (resultado.getPossiveisAbreviacoes().isEmpty()) {
            Row emptyRow = sheet.createRow(rowNum);
            emptyRow.createCell(0).setCellValue("Nenhuma possível abreviação encontrada.");
            return;
        }

        String[] headers = {"Matrícula", "CPF", "Nome Financeiro", "Nome Cadastro",
                "Similaridade", "Nível Estágio", "Data Início", "Data Fim",
                "Banco", "Agência", "Conta"};
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estiloCabecalho);
        }

        for (ResultadoComparacao.PossivelAbreviatura abv : resultado.getPossiveisAbreviacoes()) {
            RegistroPlanilha fin = abv.getFinanceiro();
            RegistroPlanilha cad = abv.getCadastro();
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(fin.getMatricula());
            row.createCell(1).setCellValue(fin.getCpf());
            row.createCell(2).setCellValue(fin.getNome());
            row.createCell(3).setCellValue(cad.getNome());
            row.createCell(4).setCellValue(String.format("%.1f%%", abv.getSimilaridade()));
            row.createCell(5).setCellValue(cad.getNivelEstagio());
            row.createCell(6).setCellValue(cad.getDataInicioStr());
            row.createCell(7).setCellValue(cad.getDataFimStr());
            row.createCell(8).setCellValue(cad.getBanco());
            row.createCell(9).setCellValue(cad.getAgencia());
            row.createCell(10).setCellValue(cad.getConta());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, i == 4 ? 4000 : 5000);
        }
        sheet.setColumnWidth(2, 6000);
        sheet.setColumnWidth(3, 6000);
    }

    // ---- 08 - Cancelados ----
    private void criarAbaCancelados(Workbook workbook, ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("08 - Cancelados no Cadastro");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REGISTROS CANCELADOS (ignorados na comparação)");
        titleCell.setCellStyle(estiloTitulo);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 9));

        rowNum++;

        if (resultado.getCanceladosNoCadastro().isEmpty()) {
            Row emptyRow = sheet.createRow(rowNum);
            emptyRow.createCell(0).setCellValue("Nenhum registro cancelado encontrado.");
            return;
        }

        String[] headers = {"Matrícula", "CPF", "Nome", "Nível Estágio", "Data Início (original)",
                "Data Fim", "Banco", "Agência", "Conta", "Observação"};
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estiloCabecalho);
        }

        for (RegistroPlanilha reg : resultado.getCanceladosNoCadastro()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(reg.getMatricula());
            row.createCell(1).setCellValue(reg.getCpf());
            row.createCell(2).setCellValue(reg.getNome());
            row.createCell(3).setCellValue(reg.getNivelEstagio());
            row.createCell(4).setCellValue(reg.getDataInicioStr());
            row.createCell(5).setCellValue(reg.getDataFimStr());
            row.createCell(6).setCellValue(reg.getBanco());
            row.createCell(7).setCellValue(reg.getAgencia());
            row.createCell(8).setCellValue(reg.getConta());
            row.createCell(9).setCellValue("Registro cancelado (não comparado)");

            for (int i = 0; i < headers.length; i++) {
                if (row.getCell(i) == null) row.createCell(i);
                row.getCell(i).setCellStyle(estiloAviso);
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 5000);
        }
        sheet.setColumnWidth(9, 8000);
    }

    // ---- 09 - Detalhado (placeholder) ----
    private void criarAbaDetalhado(Workbook workbook, ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("09 - Comparação Detalhada");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("COMPARAÇÃO DETALHADA (em desenvolvimento)");
        titleCell.setCellStyle(estiloTitulo);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 9));

        rowNum++;

        Row infoRow = sheet.createRow(rowNum);
        infoRow.createCell(0).setCellValue("Esta aba mostrará todos os registros com seus respectivos status em futuras versões.");
        sheet.setColumnWidth(0, 8000);
    }
}