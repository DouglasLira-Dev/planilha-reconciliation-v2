package com.reconciliation.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class NormalizacaoUtilTest {

    @Test
    void testNormalizarCpf() {
        assertEquals("12345678901", NormalizacaoUtil.normalizarCpf("123.456.789-01"));
        assertEquals("00012345678", NormalizacaoUtil.normalizarCpf("12345678"));
        assertEquals("", NormalizacaoUtil.normalizarCpf(null));
        assertEquals("", NormalizacaoUtil.normalizarCpf(""));
    }

    @Test
    void testNormalizarNome() {
        assertEquals("joao silva", NormalizacaoUtil.normalizarNome("João Silva"));
        assertEquals("maria eduarda", NormalizacaoUtil.normalizarNome("Maria Eduarda!"));
        assertEquals("", NormalizacaoUtil.normalizarNome(null));
    }

    @Test
    void testNormalizarAgencia() {
        assertEquals("00123", NormalizacaoUtil.normalizarAgencia("123"));
        assertEquals("12345", NormalizacaoUtil.normalizarAgencia("12345"));
        assertEquals("00001", NormalizacaoUtil.normalizarAgencia("1"));
        assertEquals("", NormalizacaoUtil.normalizarAgencia(null));
    }

    @Test
    void testSimilaridade() {
        double sim = NormalizacaoUtil.similaridade("joao", "joão");
        assertTrue(sim > 80.0, "Similaridade deve ser alta: " + sim);
        
        double sim2 = NormalizacaoUtil.similaridade("joao", "maria");
        assertTrue(sim2 < 50.0, "Similaridade deve ser baixa: " + sim2);
    }

    @Test
    void testDistanciaLevenshtein() {
        assertEquals(0, NormalizacaoUtil.distanciaLevenshtein("java", "java"));
        assertEquals(1, NormalizacaoUtil.distanciaLevenshtein("java", "jaba"));
        assertEquals(3, NormalizacaoUtil.distanciaLevenshtein("java", "python"));
    }

    @Test
    void testIsAbreviacao() {
        assertTrue(NormalizacaoUtil.isAbreviacao("joao", "joao silva"));
        assertTrue(NormalizacaoUtil.isAbreviacao("j silva", "joao silva"));
        assertFalse(NormalizacaoUtil.isAbreviacao("joao", "maria"));
    }

    @Test
    void testParseData() {
        LocalDate date = NormalizacaoUtil.parseData("25/12/2024");
        assertNotNull(date);
        assertEquals(2024, date.getYear());
        assertEquals(12, date.getMonthValue());
        assertEquals(25, date.getDayOfMonth());

        // Formato Excel serial
        LocalDate excelDate = NormalizacaoUtil.parseData("45000");
        assertNotNull(excelDate);
    }
}