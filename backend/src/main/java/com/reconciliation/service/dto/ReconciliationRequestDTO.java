package com.reconciliation.service.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class ReconciliationRequestDTO {
    private MultipartFile fileFinanceiro;
    private MultipartFile fileCadastro;
    private LocalDate dataInicioMin;
    private LocalDate dataInicioMax;
    private List<String> abasCadastro;
}