package com.reconciliation.service;

import com.reconciliation.model.ReconciliationHistory;
import com.reconciliation.model.User;
import com.reconciliation.repository.ReconciliationHistoryRepository;
import com.reconciliation.repository.UserRepository;
import com.reconciliation.service.comparador.ComparadorPlanilhasService;
import com.reconciliation.service.comparador.Divergencia;
import com.reconciliation.service.comparador.ResultadoComparacao;
import com.reconciliation.service.dto.DivergenciaDTO;
import com.reconciliation.service.dto.ReconciliationResultDTO;
import com.reconciliation.service.dto.RegistroSimplesDTO;
import com.reconciliation.service.dto.PossivelAbreviaturaDTO;
import com.reconciliation.service.model.RegistroPlanilha;
import com.reconciliation.service.reader.PlanilhaReaderService;
import com.reconciliation.service.relatorio.RelatorioExcelService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReconciliationOrchestratorService {

    private final PlanilhaReaderService readerService;
    private final ComparadorPlanilhasService comparadorService;
    private final RelatorioExcelService relatorioService;
    private final ReconciliationHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private ProgressService progressService;

    @Async("reconciliationExecutor")
    @Transactional
    public ReconciliationResultDTO processar(
            MultipartFile fileFinanceiro,
            MultipartFile fileCadastro,
            LocalDate dataInicioMin,
            LocalDate dataInicioMax,
            List<String> abasCadastro,
            Long userId) throws IOException {

        // 1. Ler planilhas
        List<RegistroPlanilha> financeiro = readerService.ler(fileFinanceiro);
        List<RegistroPlanilha> cadastro = readerService.ler(fileCadastro, abasCadastro);

        // 2. Filtrar financeiro por período
        if (dataInicioMin != null) {
            financeiro = financeiro.stream()
                    .filter(reg -> {
                        LocalDate inicio = reg.getDataInicio();
                        if (inicio == null) return false;
                        if (inicio.isBefore(dataInicioMin)) return false;
                        if (dataInicioMax != null && inicio.isAfter(dataInicioMax)) return false;
                        return true;
                    })
                    .collect(Collectors.toList());
        }

        // 3. Comparar
        ResultadoComparacao resultado = comparadorService.comparar(financeiro, cadastro);

        // 4. Gerar relatório Excel (byte[])
        byte[] relatorioBytes = relatorioService.gerar(
                resultado,
                fileFinanceiro.getOriginalFilename(),
                fileCadastro.getOriginalFilename(),
                financeiro.size(),
                cadastro.size()
        );

        // 5. Salvar histórico
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        ReconciliationHistory history = new ReconciliationHistory();
        history.setUser(user);
        history.setFilenameFinanceiro(fileFinanceiro.getOriginalFilename());
        history.setFilenameCadastro(fileCadastro.getOriginalFilename());
        history.setTotalConformes(resultado.getTotalConformes());
        history.setTotalFaltantesCadastro(resultado.getTotalFaltantes());
        history.setTotalExcedentesCadastro(resultado.getTotalExcedentes());
        history.setTotalDivergencias(resultado.getTotalDivergencias());
        history.setTotalConflitosCpf(resultado.getTotalConflitos());
        history.setTotalAbreviacoes(resultado.getTotalPossiveisAbreviacoes());
        history.setTotalCancelados(resultado.getTotalCancelados());
        // Aqui você pode salvar o relatório em disco e armazenar o caminho em history.setReportPath()
        // Ou salvar o byte[] em uma tabela separada. Por simplicidade, vou armazenar apenas o caminho.

        // Supondo que você salve o arquivo em disco e retorne o caminho:
        String reportPath = salvarRelatorio(relatorioBytes, history.getId());
        history.setReportPath(reportPath);

        history = historyRepository.save(history);

        // 6. Converter para DTO
        return toDTO(resultado, financeiro.size(), cadastro.size(), history.getId(), reportPath);
    }

    private String salvarRelatorio(byte[] bytes, Long historyId) {
        String dir = System.getProperty("user.home") + "/planilha-reconciliation-reports/";
        File directory = new File(dir);

                if (!directory.exists()) directory.mkdirs();

                String filename = "relatorio_" + historyId + ".xlsx";
                Path path = Paths.get(dir, filename);
                try {
                        Files.write(path, bytes);
                        return path.toString();

                } catch (IOException e) {
                        log.error("Erro ao salvar relatório", e);

                        return null;
                }
        }

    private ReconciliationResultDTO toDTO(ResultadoComparacao resultado,
                                          int totalFin,
                                          int totalCad,
                                          Long historyId,
                                          String reportPath) {
        ReconciliationResultDTO dto = new ReconciliationResultDTO();
        dto.setTotalFinanceiro(totalFin);
        dto.setTotalCadastro(totalCad);
        dto.setTotalConformes(resultado.getTotalConformes());
        dto.setTotalFaltantes(resultado.getTotalFaltantes());
        dto.setTotalExcedentes(resultado.getTotalExcedentes());
        dto.setTotalDivergencias(resultado.getTotalDivergencias());
        dto.setTotalErros(resultado.getTotalErros());
        dto.setTotalAvisos(resultado.getTotalAvisos());
        dto.setTotalConflitos(resultado.getTotalConflitos());
        dto.setTotalCancelados(resultado.getTotalCancelados());
        dto.setTotalPossiveisAbreviacoes(resultado.getTotalPossiveisAbreviacoes());
        dto.setHistoryId(historyId);
        dto.setReportDownloadUrl("/api/report/" + historyId);

        // Divergências
        Map<String, List<DivergenciaDTO>> divMap = new HashMap<>();
        for (Map.Entry<String, List<Divergencia>> entry : resultado.getDivergenciasPorChave().entrySet()) {
            List<DivergenciaDTO> list = entry.getValue().stream()
                    .map(d -> new DivergenciaDTO(d.getCampo(), d.getValorFinanceiro(), d.getValorCadastro(), d.getTipo(), d.getSimilaridade()))
                    .collect(Collectors.toList());
            divMap.put(entry.getKey(), list);
        }
        dto.setDivergencias(divMap);

        // Faltantes
        dto.setFaltantes(resultado.getFaltantesNoCadastro().stream()
                .map(r -> new RegistroSimplesDTO(r.getMatricula(), r.getCpf(), r.getNome()))
                .collect(Collectors.toList()));

        // Excedentes
        dto.setExcedentes(resultado.getExcedentesNoCadastro().stream()
                .map(r -> new RegistroSimplesDTO(r.getMatricula(), r.getCpf(), r.getNome()))
                .collect(Collectors.toList()));

        // Conflitos
        dto.setConflitos(resultado.getConflitosCPFMatricula().stream()
                .map(r -> new RegistroSimplesDTO(r.getMatricula(), r.getCpf(), r.getNome()))
                .collect(Collectors.toList()));

        // Possíveis abreviações
        dto.setPossiveisAbreviacoes(resultado.getPossiveisAbreviacoes().stream()
                .map(abv -> new PossivelAbreviaturaDTO(
                        abv.getFinanceiro().getMatricula(),
                        abv.getFinanceiro().getCpf(),
                        abv.getFinanceiro().getNome(),
                        abv.getCadastro().getNome(),
                        abv.getSimilaridade()
                ))
                .collect(Collectors.toList()));

        return dto;
    }

        @Async("reconciliationExecutor")
        public void processarAsync(MultipartFile fileFinanceiro,
                                MultipartFile fileCadastro,
                                LocalDate dataInicioMin,
                                LocalDate dataInicioMax,
                                List<String> abasCadastro,
                                Long userId,
                                String sessionId) {

        ProgressService.ProgressEvent progress = new ProgressService.

        ProgressEvent(0, "Iniciando...");
        progressService.sendProgress(sessionId, 5, "Lendo planilha do financeiro...");

                try {
                        // 1. Ler financeiro
                        List<RegistroPlanilha> financeiro = readerService.ler(fileFinanceiro);
                        progressService.sendProgress(sessionId, 20, "Lendo planilha de cadastro...");

                        // 2. Ler cadastro
                        List<RegistroPlanilha> cadastro = readerService.ler(fileCadastro, abasCadastro);
                        progressService.sendProgress(sessionId, 40, "Aplicando filtros...");

                        // 3. Filtrar financeiro
                        if (dataInicioMin != null) {
                        financeiro = financeiro.stream()
                                .filter(reg -> {
                                        LocalDate inicio = reg.getDataInicio();
                                        if (inicio == null) return false;
                                        if (inicio.isBefore(dataInicioMin)) return false;
                                        if (dataInicioMax != null && inicio.isAfter(dataInicioMax)) return false;
                                        return true;
                                })
                                .collect(Collectors.toList());
                        }
                progressService.sendProgress(sessionId, 60, "Comparando dados...");

                // 4. Comparar
                ResultadoComparacao resultado = comparadorService.comparar(financeiro, cadastro);
                progressService.sendProgress(sessionId, 80, "Gerando relatório Excel...");

                // 5. Gerar relatório
                byte[] relatorioBytes = relatorioService.gerar(
                        resultado,
                        fileFinanceiro.getOriginalFilename(),
                        fileCadastro.getOriginalFilename(),
                        financeiro.size(),
                        cadastro.size()
                );
                progressService.sendProgress(sessionId, 90, "Salvando histórico...");

                // 6. Salvar histórico e reportPath
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

                ReconciliationHistory history = new ReconciliationHistory();
                history.setUser(user);
                history.setFilenameFinanceiro(fileFinanceiro.getOriginalFilename());
                history.setFilenameCadastro(fileCadastro.getOriginalFilename());
                history.setTotalConformes(resultado.getTotalConformes());
                history.setTotalFaltantesCadastro(resultado.getTotalFaltantes());
                history.setTotalExcedentesCadastro(resultado.getTotalExcedentes());
                history.setTotalDivergencias(resultado.getTotalDivergencias());
                history.setTotalConflitosCpf(resultado.getTotalConflitos());
                history.setTotalCancelados(resultado.getTotalCancelados());
                history.setTotalAbreviacoes(resultado.getTotalPossiveisAbreviacoes());
                history.setTotalErros(resultado.getTotalErros());
                history.setTotalAvisos(resultado.getTotalAvisos());
                

                String reportPath = salvarRelatorio(relatorioBytes, history.getId());
                history.setReportPath(reportPath);
                history = historyRepository.save(history);

                progressService.sendProgress(sessionId, 100, "Concluído!");

                // 7. Enviar resultado final
                ReconciliationResultDTO dto = toDTO(resultado, financeiro.size(), cadastro.size(), history.getId(), reportPath);
                progressService.sendComplete(sessionId, dto);

                } catch (Exception e) {
                        log.error("Erro na reconciliação assíncrona", e);
                        progressService.sendError(sessionId, "Erro no processamento: " + e.getMessage());
                }
        }
}