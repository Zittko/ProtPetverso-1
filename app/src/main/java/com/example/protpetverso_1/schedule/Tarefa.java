package com.example.protpetverso_1.schedule;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
    private int imagemResId = android.R.drawable.ic_menu_my_calendar;

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

        if (!o.isNull("executadoPorNome")) {
            t.executadoPorNome = o.optString("executadoPorNome", null);
        }

        String dataStr = o.optString("dataTarefa", "");
        if (!dataStr.isEmpty()) {
            try { t.data = LocalDate.parse(dataStr); } catch (Exception ignored) {}
        }

        String horaStr = o.optString("hora", "");
        if (!horaStr.isEmpty() && !"null".equals(horaStr)) {
            try {
                if (horaStr.length() >= 8) t.horario = LocalTime.parse(horaStr.substring(0, 8));
                else if (horaStr.length() == 5) t.horario = LocalTime.parse(horaStr + ":00");
                else t.horario = LocalTime.parse(horaStr);
            } catch (Exception ignored) {}
        }
        return t;
    }

    public long getIdTarefa() { return idTarefa; }
    public String getTitulo() { return titulo != null ? titulo : ""; }
    public String getDesc() { return desc != null ? desc : ""; }
    public LocalDate getData() { return data; }
    public LocalTime getHorario() { return horario; }
    public boolean isConcluida() { return concluida; }
    public void setConcluida(boolean c) { this.concluida = c; }
    public long getIdPet() { return idPet; }
    public String getNomePet() { return nomePet != null ? nomePet : ""; }
    public String getExecutadoPorNome() { return executadoPorNome; }
    public int getImagemResId() { return imagemResId; }
}