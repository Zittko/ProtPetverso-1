package com.example.protpetverso_1.schedule;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Tarefa {

    private long idTarefa;
    private String titulo;
    private String desc;
    private int imagemResId;
    private LocalDate data;
    private LocalTime horario;
    private boolean concluida;

    private long idPet;
    private String nomePet;

    private String criadoPorNome;       // futuro (API ainda não envia)
    private LocalDateTime dataCriacao;  // futuro (API ainda não envia)
    private String executadoPorNome;
    private LocalDateTime dataConclusao;

    public Tarefa(String titulo, String desc, int imagemResId, LocalDate data, LocalTime horario) {
        this.titulo = titulo;
        this.desc = desc;
        this.imagemResId = imagemResId;
        this.data = data;
        this.horario = horario;
        this.concluida = false;
    }

    public static Tarefa fromJson(JSONObject o) {
        Tarefa t = new Tarefa(
                o.optString("titulo", ""),
                o.optString("descricao", o.optString("desc", "")),
                0, null, null
        );

        t.idTarefa = o.optLong("idTarefa", o.optLong("id", -1));
        t.idPet = o.optLong("idPet", -1);
        t.nomePet = o.optString("nomePet", "");
        t.concluida = o.optBoolean("concluida", false);

        // Data da tarefa
        String dataStr = o.optString("dataTarefa", o.optString("data", ""));
        if (!dataStr.isEmpty()) {
            try {
                t.data = LocalDate.parse(dataStr.length() >= 10 ? dataStr.substring(0, 10) : dataStr);
            } catch (Exception ignored) {}
        }

        // Hora
        String horaStr = o.optString("hora", "");
        if (!horaStr.isEmpty() && !"null".equalsIgnoreCase(horaStr)) {
            try {
                if (horaStr.length() >= 8) {
                    t.horario = LocalTime.parse(horaStr.substring(0, 8));
                } else if (horaStr.length() == 5) {
                    t.horario = LocalTime.parse(horaStr + ":00");
                } else {
                    t.horario = LocalTime.parse(horaStr);
                }
            } catch (Exception ignored) {}
        }

        // Quem executou (API atual envia executadoPorNome)
        t.executadoPorNome = o.optString("executadoPorNome", null);
        if (t.executadoPorNome == null || t.executadoPorNome.isEmpty()) {
            t.executadoPorNome = o.optString("executadoPor", null);
        }
        if (t.executadoPorNome == null || t.executadoPorNome.isEmpty()) {
            JSONObject u = o.optJSONObject("executadoPor");
            if (u != null) {
                String apelido = u.optString("apelido", "");
                t.executadoPorNome = !apelido.isBlank() ? apelido : u.optString("nome", null);
            }
        }

        // Data de conclusão
        t.dataConclusao = parseDateTime(o.optString("dataConclusao", null));

        // Campos futuros (já preparados)
        t.criadoPorNome = o.optString("criadoPorNome", o.optString("criadoPor", null));
        if (t.criadoPorNome != null && t.criadoPorNome.isEmpty()) t.criadoPorNome = null;
        t.dataCriacao = parseDateTime(o.optString("dataCriacao", null));

        t.imagemResId = TarefaIconHelper.resolverIcone(t.titulo, t.desc);
        return t;
    }

    private static LocalDateTime parseDateTime(String s) {
        if (s == null || s.isEmpty() || "null".equalsIgnoreCase(s)) return null;
        try {
            return LocalDateTime.parse(s.replace(" ", "T"));
        } catch (Exception e) {
            try {
                if (s.length() >= 10) {
                    return LocalDate.parse(s.substring(0, 10)).atStartOfDay();
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    // --- Getters / Setters ---

    public long getIdTarefa() { return idTarefa; }
    public long getId() { return idTarefa; } // compatibilidade
    public String getTitulo() { return titulo != null ? titulo : ""; }
    public String getDesc() { return desc != null ? desc : ""; }
    public int getImagemResId() { return imagemResId; }
    public void setImagemResId(int id) { this.imagemResId = id; }
    public LocalDate getData() { return data; }
    public LocalTime getHorario() { return horario; }
    public boolean isConcluida() { return concluida; }
    public void setConcluida(boolean c) { this.concluida = c; }

    public long getIdPet() { return idPet; }
    public String getNomePet() { return nomePet != null ? nomePet : ""; }

    public String getCriadoPorNome() { return criadoPorNome; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public String getExecutadoPorNome() { return executadoPorNome; }
    public void setExecutadoPorNome(String s) { this.executadoPorNome = s; }
    public LocalDateTime getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDateTime d) { this.dataConclusao = d; }

    public String getTituloComHorario() {
        String h = horario != null ? horario.format(DateTimeFormatter.ofPattern("HH:mm")) : "";
        String tit = getTitulo();
        if (!h.isEmpty() && !tit.isEmpty()) return h + " - " + tit;
        if (!h.isEmpty()) return h;
        return tit;
    }

    /**
     * Sequência desejada:
     * 1. Pet: NomeDoPet
     * 2. Criado por: X   |  Data criação: dd/MM/yyyy HH:mm   (quando a API enviar)
     * 3. Executado por: Y | Data conclusão: dd/MM/yyyy HH:mm  (quando concluída)
     */
    public String getRodapeTexto() {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // 1. Pet (sempre que houver)
        if (nomePet != null && !nomePet.isBlank()) {
            sb.append("Pet: ").append(nomePet.trim());
        }

        // 2. Criado por + data criação (quando a API passar)
        if (criadoPorNome != null && !criadoPorNome.isBlank()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("Criado por: ").append(criadoPorNome.trim());
        }
        if (dataCriacao != null) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("Data criação: ").append(dataCriacao.format(dtf));
        }

        // 3. Executado por + data conclusão (somente se concluída)
        if (concluida) {
            if (executadoPorNome != null && !executadoPorNome.isBlank()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("Executado por: ").append(executadoPorNome.trim());
            }
            if (dataConclusao != null) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("Data conclusão: ").append(dataConclusao.format(dtf));
            }
        }

        return sb.toString();
    }
}