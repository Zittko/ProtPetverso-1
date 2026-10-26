package com.example.protpetverso_1.vaccine;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Vacina {

    public static final String STATUS_PENDENTE = "PENDENTE";
    public static final String STATUS_APLICADA = "APLICADA";
    public static final String STATUS_ATRASADA = "ATRASADA";
    public static final String STATUS_OK = "OK";

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
        v.observacao = o.optString("observacao", "");
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
        if ("OK".equals(v.statusVacina) || "APLICADO".equals(v.statusVacina)) {
            v.statusVacina = STATUS_APLICADA;
        }

        if (!o.isNull("intervaloQuantidade")) {
            v.intervaloQuantidade = o.optInt("intervaloQuantidade");
        }
        String un = o.optString("intervaloUnidade", "");
        v.intervaloUnidade = un.isEmpty() || "null".equalsIgnoreCase(un) ? null : un;

        String da = o.optString("dataAplicacao", "");
        if (!da.isEmpty() && !"null".equalsIgnoreCase(da)) {
            try {
                v.dataAplicacao = LocalDate.parse(da.substring(0, Math.min(10, da.length())));
            } catch (Exception ignored) {}
        }

        if (!o.isNull("dataProximaDose")) {
            String dp = o.optString("dataProximaDose", "");
            if (!dp.isEmpty() && !"null".equalsIgnoreCase(dp)) {
                try {
                    v.dataProximaDose = LocalDate.parse(dp.substring(0, Math.min(10, dp.length())));
                } catch (Exception ignored) {}
            }
        }
        return v;
    }

    public boolean isAplicada() {
        if (statusVacina == null) return false;
        String s = statusVacina.trim().toUpperCase();
        return STATUS_APLICADA.equals(s) || STATUS_OK.equals(s);
    }

    public boolean isPendenteOuAtrasada() {
        if (statusVacina != null && !statusVacina.trim().isEmpty()) {
            String s = statusVacina.trim().toUpperCase();
            if (STATUS_PENDENTE.equals(s) || STATUS_ATRASADA.equals(s)) return true;
            if (isAplicada()) return false;
        }
        if (dataProximaDose == null) return !isAplicada();
        return !dataProximaDose.isAfter(LocalDate.now());
    }

    public String getStatusLabel() {
        if (statusVacina == null || statusVacina.trim().isEmpty()) return "Pendente";
        String s = statusVacina.trim().toUpperCase();
        switch (s) {
            case STATUS_APLICADA:
            case STATUS_OK:
                return "Ok";
            case STATUS_ATRASADA:
                return "Atrasada";
            case STATUS_PENDENTE:
            default:
                return "Pendente";
        }
    }

    public String getDataAplicacaoFormatada() {
        if (dataAplicacao == null) return "—";
        return dataAplicacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getDataProximaDoseFormatada() {
        if (dataProximaDose == null) return null;
        return dataProximaDose.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public long getIdVacina() { return idVacina; }
    public String getNome() { return nome != null ? nome : ""; }
    public LocalDate getDataAplicacao() { return dataAplicacao; }
    public boolean isDosagemUnica() { return dosagemUnica; }
    public Integer getIntervaloQuantidade() { return intervaloQuantidade; }
    public String getIntervaloUnidade() { return intervaloUnidade; }
    public LocalDate getDataProximaDose() { return dataProximaDose; }
    public String getObservacao() { return observacao != null ? observacao : ""; }
    public String getStatusVacina() { return statusVacina; }
}