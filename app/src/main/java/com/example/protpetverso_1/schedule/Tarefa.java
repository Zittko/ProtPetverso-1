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
    private String criadoPorNome;
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
        t.nomePet = texto(o.optString("nomePet", ""));
        t.concluida = o.optBoolean("concluida", false);

        String dataStr = o.optString("dataTarefa", o.optString("data", ""));
        if (!dataStr.isEmpty() && !"null".equalsIgnoreCase(dataStr)) {
            try {
                t.data = LocalDate.parse(dataStr.length() >= 10 ? dataStr.substring(0, 10) : dataStr);
            } catch (Exception ignored) {}
        }

        String horaStr = o.optString("hora", "");
        if (!horaStr.isEmpty() && !"null".equalsIgnoreCase(horaStr)) {
            try {
                if (horaStr.length() >= 8) t.horario = LocalTime.parse(horaStr.substring(0, 8));
                else if (horaStr.length() == 5) t.horario = LocalTime.parse(horaStr + ":00");
                else t.horario = LocalTime.parse(horaStr);
            } catch (Exception ignored) {}
        }

        t.criadoPorNome = nomeDe(o, "criadaPor", "criadoPor", "criadoPorNome");
        t.executadoPorNome = nomeDe(o, "executadoPorNome", "executadoPor", "executadoPorNome");
        t.dataConclusao = parseDateTime(o.optString("dataConclusao", null));
        t.imagemResId = TarefaIconHelper.resolverIcone(t.titulo, t.desc);
        return t;
    }

    /** A API manda criadaPor como texto (nome). Aceita também objeto {nome, apelido}. */
    private static String nomeDe(JSONObject o, String chaveTexto, String chaveAlt, String chaveObj) {
        if (o.has(chaveTexto) && !o.isNull(chaveTexto)) {
            Object bruto = o.opt(chaveTexto);
            if (bruto instanceof String) {
                String s = texto((String) bruto);
                if (s != null) return s;
            } else if (bruto instanceof JSONObject) {
                return nomeDeUsuario((JSONObject) bruto);
            }
        }
        if (!chaveAlt.equals(chaveTexto) && o.has(chaveAlt) && !o.isNull(chaveAlt)) {
            Object bruto = o.opt(chaveAlt);
            if (bruto instanceof String) {
                String s = texto((String) bruto);
                if (s != null) return s;
            } else if (bruto instanceof JSONObject) {
                return nomeDeUsuario((JSONObject) bruto);
            }
        }
        JSONObject obj = o.optJSONObject(chaveObj);
        return nomeDeUsuario(obj);
    }

    private static String nomeDeUsuario(JSONObject u) {
        if (u == null) return null;
        String apelido = texto(u.optString("apelido", ""));
        if (apelido != null) return apelido;
        return texto(u.optString("nome", ""));
    }

    private static String texto(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty() || "null".equalsIgnoreCase(t)) return null;
        return t;
    }

    private static LocalDateTime parseDateTime(String s) {
        if (s == null || s.isEmpty() || "null".equalsIgnoreCase(s)) return null;
        try {
            return LocalDateTime.parse(s.replace(" ", "T"));
        } catch (Exception e) {
            try {
                if (s.length() >= 10) return LocalDate.parse(s.substring(0, 10)).atStartOfDay();
            } catch (Exception ignored) {}
        }
        return null;
    }

    public long getIdTarefa() { return idTarefa; }
    public long getId() { return idTarefa; }
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

    public String getRodapeTexto() {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        if (nomePet != null && !nomePet.isBlank()) {
            sb.append("Pet: ").append(nomePet.trim());
        }
        if (criadoPorNome != null && !criadoPorNome.isBlank()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("Tarefa criada por: ").append(criadoPorNome.trim());
        }
        if (concluida) {
            if (executadoPorNome != null && !executadoPorNome.isBlank()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("Executada por: ").append(executadoPorNome.trim());
            }
            if (dataConclusao != null) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("Data conclusão: ").append(dataConclusao.format(dtf));
            }
        }
        return sb.toString();
    }
}