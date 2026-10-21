package com.example.protpetverso_1.vaccine;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Espelha VacinaResponseDTO da API.
 */
public class Vacina {

    private long idVacina;
    private String nome;
    private LocalDate dataAplicacao;
    private LocalDate dataProximaDose; // pode ser null
    private String observacao;

    public Vacina() {}

    public static Vacina fromJson(JSONObject o) {
        Vacina v = new Vacina();
        if (o == null) return v;

        v.idVacina = o.optLong("idVacina", o.optLong("id", -1));
        v.nome = o.optString("nome", "");
        v.observacao = o.optString("observacao", "");

        String da = o.optString("dataAplicacao", "");
        if (!da.isEmpty()) {
            try {
                v.dataAplicacao = LocalDate.parse(da);
            } catch (Exception ignored) {}
        }

        if (!o.isNull("dataProximaDose")) {
            String dp = o.optString("dataProximaDose", "");
            if (!dp.isEmpty()) {
                try {
                    v.dataProximaDose = LocalDate.parse(dp);
                } catch (Exception ignored) {}
            }
        }
        return v;
    }

    /** Status para o card: Ok se próxima dose no futuro; Pendente se vencida/hoje ou sem próxima. */
    public String getStatusLabel() {
        if (dataProximaDose == null) return "Pendente";
        if (dataProximaDose.isAfter(LocalDate.now())) return "Ok";
        return "Pendente";
    }

    public boolean isProximaPendenteOuVencida() {
        if (dataProximaDose == null) return true;
        return !dataProximaDose.isAfter(LocalDate.now());
    }

    public boolean temProximaDoseFutura() {
        return dataProximaDose != null && dataProximaDose.isAfter(LocalDate.now());
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
    public LocalDate getDataProximaDose() { return dataProximaDose; }
    public String getObservacao() { return observacao != null ? observacao : ""; }

    public void setIdVacina(long idVacina) { this.idVacina = idVacina; }
    public void setNome(String nome) { this.nome = nome; }
    public void setDataAplicacao(LocalDate dataAplicacao) { this.dataAplicacao = dataAplicacao; }
    public void setDataProximaDose(LocalDate dataProximaDose) { this.dataProximaDose = dataProximaDose; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}