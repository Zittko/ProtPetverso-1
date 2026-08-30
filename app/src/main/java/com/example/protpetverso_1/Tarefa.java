package com.example.protpetverso_1;

public class Tarefa {
    private String titulo, desc;
    private int imagemResId;

    public Tarefa(String titulo, String desc, int imagemResId) {
        this.titulo = titulo;
        this.desc = desc;
        this.imagemResId = imagemResId;
    }

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
