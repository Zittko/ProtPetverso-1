package com.example.protpetverso_1.vaccine;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Vacina {

    public static final String STATUS_PENDENTE = "PENDENTE";
    public static final String STATUS_APLICADA = "APLICADA";
    public static final String STATUS_ATRASADA = "ATRASADA";
    /** A API não envia OK. Mantido só para código antigo compilar. */
    public static final String STATUS_OK = "OK";

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private long idVacina;
    private String nome;
    private LocalDate dataAplicacao;
    private boolean dosagemUnica;
    private Integer intervaloQuantidade;
    private String intervaloUnidade;
    private LocalDate dataProximaDose;
    private String observacao;
    private String statusVacina;

    public Vacina() {}

    public static Vacina fromJson(JSONObject o) {
        Vacina v = new Vacina();
        if (o == null) return v;

        v.idVacina = o.optLong("idVacina", o.optLong("id", -1));
        v.nome = o.optString("nome", "");
        String obs = o.optString("observacao", "");
        v.observacao = "null".equalsIgnoreCase(obs) ? "" : obs;
        v.dosagemUnica = o.optBoolean("dosagemUnica", false);

        String st = o.optString("statusVacina", "");
        if (st.isEmpty() || "null".equalsIgnoreCase(st)) {
            st = o.optString("status", "");
        }
        if (st.isEmpty() || "null".equalsIgnoreCase(st)) {
            v.statusVacina = STATUS_PENDENTE;
        } else {
            v.statusVacina = st.trim().toUpperCase();
        }
        if (STATUS_OK.equals(v.statusVacina) || "APLICADO".equals(v.statusVacina)) {
            v.statusVacina = STATUS_APLICADA;
        }

        if (!o.isNull("intervaloQuantidade")) {
            v.intervaloQuantidade = o.optInt("intervaloQuantidade");
        }
        String un = o.optString("intervaloUnidade", "");
        v.intervaloUnidade = un.isEmpty() || "null".equalsIgnoreCase(un) ? null : un;

        v.dataAplicacao = parseData(o.optString("dataAplicacao", ""));
        v.dataProximaDose = parseData(o.isNull("dataProximaDose") ? "" : o.optString("dataProximaDose", ""));
        return v;
    }

    private static LocalDate parseData(String s) {
        if (s == null || s.isEmpty() || "null".equalsIgnoreCase(s)) return null;
        try {
            return LocalDate.parse(s.substring(0, Math.min(10, s.length())));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * A API guarda APLICADA até alguém confirmar de novo.
     * No app: no dia da próxima dose vira PENDENTE; depois desse dia, ATRASADA.
     * Dose única aplicada permanece APLICADA.
     */
    public String getStatusEfetivo() {
        LocalDate hoje = LocalDate.now(FUSO);
        String base = statusVacina == null ? STATUS_PENDENTE : statusVacina.trim().toUpperCase();

        if (STATUS_ATRASADA.equals(base)) return STATUS_ATRASADA;
        if (STATUS_PENDENTE.equals(base)) {
            if (dataAplicacao != null && dataAplicacao.isBefore(hoje)) return STATUS_ATRASADA;
            if (dataProximaDose != null && dataProximaDose.isBefore(hoje)) return STATUS_ATRASADA;
            return STATUS_PENDENTE;
        }

        if (dosagemUnica || dataProximaDose == null) return STATUS_APLICADA;
        if (dataProximaDose.isAfter(hoje)) return STATUS_APLICADA;
        if (dataProximaDose.isEqual(hoje)) return STATUS_PENDENTE;
        return STATUS_ATRASADA;
    }

    public boolean isAplicada() {
        return STATUS_APLICADA.equals(getStatusEfetivo());
    }

    public boolean isPendenteOuAtrasada() {
        String s = getStatusEfetivo();
        return STATUS_PENDENTE.equals(s) || STATUS_ATRASADA.equals(s);
    }

    public String getStatusLabel() {
        switch (getStatusEfetivo()) {
            case STATUS_APLICADA:
                return "Aplicada";
            case STATUS_ATRASADA:
                return "Atrasada";
            case STATUS_PENDENTE:
            default:
                return "Pendente";
        }
    }

    public String getStatusVacina() {
        return getStatusEfetivo();
    }

    public String getDataAplicacaoFormatada() {
        if (dataAplicacao == null) return "—";
        return dataAplicacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getDataProximaDoseFormatada() {
        if (dataProximaDose == null) return null;
        return dataProximaDose.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getIntervaloResumo() {
        if (dosagemUnica) return "Dose única";
        if (intervaloQuantidade == null || intervaloUnidade == null) return null;
        String u;
        switch (intervaloUnidade.toUpperCase()) {
            case "DIAS":
                u = intervaloQuantidade == 1 ? "dia" : "dias";
                break;
            case "SEMANAS":
                u = intervaloQuantidade == 1 ? "semana" : "semanas";
                break;
            case "MESES":
                u = intervaloQuantidade == 1 ? "mês" : "meses";
                break;
            case "ANOS":
                u = intervaloQuantidade == 1 ? "ano" : "anos";
                break;
            default:
                u = intervaloUnidade.toLowerCase();
                break;
        }
        return "A cada " + intervaloQuantidade + " " + u;
    }

    public long getIdVacina() { return idVacina; }
    public String getNome() { return nome != null ? nome : ""; }
    public LocalDate getDataAplicacao() { return dataAplicacao; }
    public boolean isDosagemUnica() { return dosagemUnica; }
    public Integer getIntervaloQuantidade() { return intervaloQuantidade; }
    public String getIntervaloUnidade() { return intervaloUnidade; }
    public LocalDate getDataProximaDose() { return dataProximaDose; }
    public String getObservacao() {
        if (observacao == null || "null".equalsIgnoreCase(observacao)) return "";
        return observacao;
    }
}