package com.example.protpetverso_1.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Formatação de telefone e datas para exibição.
 * Telefone: guardado/enviado à API só com dígitos (ex.: 11987432817) e exibido como (11) 98743-2817.
 * Datas: a API usa ISO (yyyy-MM-dd); a tela mostra dd/MM/yyyy.
 */
public final class Formatadores {

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Formatadores() {}

    // ==================== TELEFONE ====================

    public static String apenasDigitos(CharSequence s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') sb.append(c);
        }
        return sb.toString();
    }

    /** Só dígitos, sem o código do país (55) quando vier colado com ele. */
    public static String telefoneParaApi(CharSequence s) {
        String d = apenasDigitos(s);
        if (d.length() > 11 && d.startsWith("55")) d = d.substring(2);
        return d;
    }

    /** DDD + 8 dígitos (fixo) ou DDD + 9 dígitos (celular). */
    public static boolean telefoneValido(CharSequence s) {
        int n = telefoneParaApi(s).length();
        return n == 10 || n == 11;
    }

    /**
     * Formata o que já foi digitado: (1, (11, (11) 9, (11) 9874-3, (11) 98743-2817.
     * Até 10 dígitos usa NNNN-NNNN; com 11 usa NNNNN-NNNN.
     */
    public static String telefoneParcial(String digitos) {
        if (digitos == null || digitos.isEmpty()) return "";
        int n = digitos.length();
        StringBuilder sb = new StringBuilder("(");
        sb.append(digitos, 0, Math.min(2, n));
        if (n <= 2) return sb.toString();

        sb.append(") ");
        String resto = digitos.substring(2);
        int bloco = n <= 10 ? 4 : 5;
        if (resto.length() <= bloco) {
            sb.append(resto);
        } else {
            sb.append(resto, 0, bloco).append('-').append(resto.substring(bloco));
        }
        return sb.toString();
    }

    /** Para exibir um telefone vindo da API/sessão. Valores que não parecem telefone ficam como estão. */
    public static String telefone(String bruto) {
        if (bruto == null) return "";
        String d = telefoneParaApi(bruto);
        if (d.length() == 10 || d.length() == 11) return telefoneParcial(d);
        return bruto;
    }

    // ==================== DATAS ====================

    /** "2026-11-03" ou "2026-11-03T19:30:12..." → "03/11/2026". Outros valores voltam como estão. */
    public static String dataIsoParaBr(String iso) {
        if (iso == null || iso.length() < 10) return iso == null ? "" : iso;
        try {
            return LocalDate.parse(iso.substring(0, 10)).format(DATA_BR);
        } catch (Exception e) {
            return iso;
        }
    }

    /** "2026-11-03T19:30:12.123456" → "03/11/2026 19:30". Sem hora, mostra só a data. */
    public static String dataHoraIsoParaBr(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        try {
            if (iso.length() > 10) return LocalDateTime.parse(iso).format(DATA_HORA_BR);
        } catch (Exception ignored) {
            // cai para a data simples
        }
        return dataIsoParaBr(iso);
    }
}