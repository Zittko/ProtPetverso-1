package com.example.protpetverso_1.schedule;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Modelo de tarefa alinhado ao TarefaResponseDTO da API.
 */
public class Tarefa {

    private long idTarefa;
    private String titulo;
    private String desc;
    private LocalDate data;
    private LocalTime horario;
    private boolean concluida;
    private long idPet;
    private String nomePet;
    private String executadoPorNome;
    private LocalDateTime dataConclusao;
    private int imagemResId; // ícone local (placeholder)

    public Tarefa(String titulo, String desc, int imagemResId, LocalDate data, LocalTime horario) {
        this.titulo = titulo;
        this.desc = desc;
        this.imagemResId = imagemResId;
        this.data = data;
        this.horario = horario;
    }

    public Tarefa(String titulo, String desc, int imagemResId) {
        this(titulo, desc, imagemResId, null, null);
    }

    /** Monta a partir do JSON da API (TarefaResponseDTO). */
    public static Tarefa fromJson(JSONObject o) {
        Tarefa t = new Tarefa(
                o.optString("titulo", ""),
                o.optString("descricao", ""),
                android.R.drawable.ic_menu_my_calendar
        );
        t.idTarefa = o.optLong("idTarefa", -1);
        t.idPet = o.optLong("idPet", -1);
        t.nomePet = o.optString("nomePet", "");
        t.concluida = o.optBoolean("concluida", false);
        t.executadoPorNome = o.optString("executadoPorNome", null);

        String dataStr = o.optString("dataTarefa", "");
        if (!dataStr.isEmpty()) {
            try {
                t.data = LocalDate.parse(dataStr); // yyyy-MM-dd
            } catch (Exception ignored) {
            }
        }

        String horaStr = o.optString("hora", "");
        if (!horaStr.isEmpty() && !horaStr.equals("null")) {
            try {
                // API pode mandar "15:00:00" ou "15:00"
                if (horaStr.length() > 5) {
                    t.horario = LocalTime.parse(horaStr);
                } else {
                    t.horario = LocalTime.parse(horaStr + (horaStr.length() == 5 ? ":00" : ""));
                }
            } catch (Exception e) {
                try {
                    t.horario = LocalTime.parse(horaStr.substring(0, Math.min(8, horaStr.length())));
                } catch (Exception ignored) {
                }
            }
        }

        String dc = o.optString("dataConclusao", "");
        if (!dc.isEmpty() && !dc.equals("null")) {
            try {
                t.dataConclusao = LocalDateTime.parse(dc);
            } catch (Exception ignored) {
            }
        }
        return t;
    }

    public long getIdTarefa() { return idTarefa; }
    public String getTitulo() { return titulo != null ? titulo : ""; }
    public String getDesc() { return desc != null ? desc : ""; }
    public LocalDate getData() { return data; }
    public LocalTime getHorario() { return horario; }
    public boolean isConcluida() { return concluida; }
    public long getIdPet() { return idPet; }
    public String getNomePet() { return nomePet != null ? nomePet : ""; }
    public String getExecutadoPorNome() { return executadoPorNome; }
    public int getImagemResId() { return imagemResId; }

    public void setImagemResId(int imagemResId) { this.imagemResId = imagemResId; }
}