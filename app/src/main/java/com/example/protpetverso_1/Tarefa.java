package com.example.protpetverso_1;

import java.time.LocalDate;

public class Tarefa {
    private String titulo, desc;
    private int imagemResId;

    private LocalDate data;

    public Tarefa(String titulo, String desc, int imagemResId) {
        this.titulo = titulo;
        this.desc = desc;
        this.imagemResId = imagemResId;
        this.data = data;
    }

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
