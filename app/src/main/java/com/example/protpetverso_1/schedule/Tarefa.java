package com.example.protpetverso_1.schedule;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Tarefa {

    private long id;
    private String titulo;
    private String desc;
    private int imagemResId;
    private LocalDate data;
    private LocalTime horario;
    private boolean concluida;
    private String executadoPor;
    private String criadoPor;
    private LocalDateTime dataConclusao;

    public Tarefa(String titulo, String desc, int imagemResId, LocalDate data, LocalTime horario) {
        this.titulo = titulo;
        this.desc = desc;
        this.imagemResId = imagemResId;
        this.data = data;
        this.horario = horario;
        this.concluida = false;
    }

    public static Tarefa fromJson(JSONObject json) {
        Tarefa t = new Tarefa(
                json.optString("titulo", ""),
                json.optString("descricao", json.optString("desc", "")),
                0, null, null
        );

        t.id = json.optLong("id", json.optLong("idTarefa", 0));

        String dataStr = json.optString("dataTarefa", json.optString("data", ""));
        if (!dataStr.isEmpty()) {
            try {
                t.data = LocalDate.parse(dataStr.substring(0, Math.min(10, dataStr.length())));
            } catch (Exception ignored) {}
        }

        String horaStr = json.optString("hora", json.optString("horario", ""));
        if (!horaStr.isEmpty()) {
            try {
                t.horario = LocalTime.parse(horaStr.length() >= 5 ? horaStr.substring(0, 5) : horaStr);
            } catch (Exception ignored) {}
        }

        t.concluida = json.optBoolean("concluida", false);

        t.executadoPor = json.optString("executadoPor", "");
        if (t.executadoPor.isEmpty()) {
            t.executadoPor = json.optString("nomeExecutor", "");
        }
        if (t.executadoPor.isEmpty()) {
            JSONObject u = json.optJSONObject("executadoPorUsuario");
            if (u != null) {
                t.executadoPor = u.optString("nome", u.optString("apelido", ""));
            }
        }

        t.criadoPor = json.optString("criadoPor", json.optString("criado_por", ""));

        String dc = json.optString("dataConclusao", "");
        if (!dc.isEmpty()) {
            try {
                t.dataConclusao = LocalDateTime.parse(dc.replace(" ", "T"));
            } catch (Exception e) {
                try {
                    t.dataConclusao = LocalDate.parse(dc.substring(0, 10)).atStartOfDay();
                } catch (Exception ignored) {}
            }
        }

        t.imagemResId = TarefaIconHelper.resolverIcone(t.titulo, t.desc);
        return t;
    }

    public long getId() { return id; }
    public String getTitulo() { return titulo != null ? titulo : ""; }
    public String getDesc() { return desc != null ? desc : ""; }
    public int getImagemResId() { return imagemResId; }
    public void setImagemResId(int id) { this.imagemResId = id; }
    public LocalDate getData() { return data; }
    public LocalTime getHorario() { return horario; }
    public boolean isConcluida() { return concluida; }
    public void setConcluida(boolean c) { this.concluida = c; }
    public String getExecutadoPor() { return executadoPor; }
    public void setExecutadoPor(String s) { this.executadoPor = s; }
    public String getCriadoPor() { return criadoPor; }
    public LocalDateTime getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDateTime d) { this.dataConclusao = d; }

    public String getTituloComHorario() {
        String h = horario != null ? horario.format(DateTimeFormatter.ofPattern("HH:mm")) : "";
        String tit = getTitulo();
        if (!h.isEmpty() && !tit.isEmpty()) return h + " - " + tit;
        if (!h.isEmpty()) return h;
        return tit;
    }

    public String getRodapeTexto() {
        if (!concluida) {
            if (criadoPor != null && !criadoPor.isEmpty()) {
                return "Criado por: " + criadoPor;
            }
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (executadoPor != null && !executadoPor.trim().isEmpty()) {
            sb.append("Feito por: ").append(executadoPor.trim());
        }
        if (dataConclusao != null) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("Concluído em: ")
                    .append(dataConclusao.toLocalDate()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        return sb.toString();
    }
}