package com.reconciliation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reconciliation_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ReconciliationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(name = "executed_at", updatable = false)
    private LocalDateTime executedAt;

    @Column(name = "filename_financeiro", length = 255)
    private String filenameFinanceiro;

    @Column(name = "filename_cadastro", length = 255)
    private String filenameCadastro;

    @Column(name = "total_conformes")
    private Integer totalConformes = 0;

    @Column(name = "total_faltantes_cadastro")
    private Integer totalFaltantesCadastro = 0;

    @Column(name = "total_excedentes_cadastro")
    private Integer totalExcedentesCadastro = 0;

    @Column(name = "total_divergencias")
    private Integer totalDivergencias = 0;

    @Column(name = "total_conflitos_cpf")
    private Integer totalConflitosCpf = 0;

    @Column(name = "total_abreviacoes")
    private Integer totalAbreviacoes = 0;

    @Column(name = "total_cancelados")
    private Integer totalCancelados = 0;

    @Column(name = "report_path", length = 500)
    private String reportPath;
}