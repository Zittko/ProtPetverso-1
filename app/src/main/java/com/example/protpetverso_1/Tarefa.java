package com.example.protpetverso_1;

import java.time.LocalDate;
import java.time.LocalTime;

public class Tarefa {
    private String titulo, desc;
    private int imagemResId;

    private LocalDate data;

    private LocalTime horario;

    public Tarefa(String titulo, String desc, int imagemResId, LocalDate data, LocalTime horario) {
        this.titulo = titulo;
        this.desc = desc;
        this.imagemResId = imagemResId;
        this.data = data;
        this.horario = horario;
    }

    // Construtor antigo (para não quebrar o que já existe)
    public Tarefa(String titulo, String desc, int imagemResId) {
        this.titulo = titulo;
        this.desc = desc;
        this.imagemResId = imagemResId;
        this.data = null;
        this.horario = null;
    }

    public LocalTime getHorario(){return horario;}
    public LocalDate getData(){return data;}
    public String getTitulo() {
        return titulo;
    }

    public String getDesc() {
        return desc;
    }

    public int getImagemResId() {
        return imagemResId;
    }
}
