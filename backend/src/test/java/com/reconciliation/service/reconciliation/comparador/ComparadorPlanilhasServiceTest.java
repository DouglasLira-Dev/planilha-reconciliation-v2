package com.reconciliation.service.reconciliation.comparador;

import com.reconciliation.service.comparador.ComparadorPlanilhasService;
import com.reconciliation.service.comparador.ResultadoComparacao;
import com.reconciliation.service.model.RegistroPlanilha;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComparadorPlanilhasServiceTest {

    private ComparadorPlanilhasService comparador;

    @BeforeEach
    void setUp() {
        comparador = new ComparadorPlanilhasService();
    }

    @Test
    void testComparacaoConforme() {
        List<RegistroPlanilha> financeiro = new ArrayList<>();
        List<RegistroPlanilha> cadastro = new ArrayList<>();

        RegistroPlanilha reg = new RegistroPlanilha();
        reg.setMatricula("12345");
        reg.setCpf("12345678901");
        reg.setNome("João Silva");
        reg.setMatriculaNorm("12345");
        reg.setCpfNorm("12345678901");
        reg.setNomeNorm("joao silva");

        financeiro.add(reg);
        cadastro.add(reg);

        ResultadoComparacao resultado = comparador.comparar(financeiro, cadastro);

        assertEquals(1, resultado.getTotalConformes());
        assertEquals(0, resultado.getTotalFaltantes());
        assertEquals(0, resultado.getTotalExcedentes());
        assertEquals(0, resultado.getTotalDivergencias());
    }

    @Test
    void testFaltanteNoCadastro() {
        List<RegistroPlanilha> financeiro = new ArrayList<>();
        List<RegistroPlanilha> cadastro = new ArrayList<>();

        RegistroPlanilha reg = new RegistroPlanilha();
        reg.setMatricula("12345");
        reg.setCpf("12345678901");
        reg.setNome("João Silva");
        reg.setMatriculaNorm("12345");
        reg.setCpfNorm("12345678901");
        reg.setNomeNorm("joao silva");

        financeiro.add(reg);

        ResultadoComparacao resultado = comparador.comparar(financeiro, cadastro);

        assertEquals(0, resultado.getTotalConformes());
        assertEquals(1, resultado.getTotalFaltantes());
        assertEquals(0, resultado.getTotalExcedentes());
    }

    @Test
    void testDivergenciaNome() {
        List<RegistroPlanilha> financeiro = new ArrayList<>();
        List<RegistroPlanilha> cadastro = new ArrayList<>();

        RegistroPlanilha regFin = new RegistroPlanilha();
        regFin.setMatricula("12345");
        regFin.setCpf("12345678901");
        regFin.setNome("João Silva");
        regFin.setMatriculaNorm("12345");
        regFin.setCpfNorm("12345678901");
        regFin.setNomeNorm("joao silva");

        RegistroPlanilha regCad = new RegistroPlanilha();
        regCad.setMatricula("12345");
        regCad.setCpf("12345678901");
        regCad.setNome("José Silva");
        regCad.setMatriculaNorm("12345");
        regCad.setCpfNorm("12345678901");
        regCad.setNomeNorm("jose silva");

        financeiro.add(regFin);
        cadastro.add(regCad);

        ResultadoComparacao resultado = comparador.comparar(financeiro, cadastro);

        assertEquals(0, resultado.getTotalConformes());
        assertEquals(1, resultado.getTotalDivergencias());
        
        var divergencias = resultado.getDivergenciasPorChave();
        assertFalse(divergencias.isEmpty());
    }
}