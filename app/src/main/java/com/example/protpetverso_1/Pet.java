package com.example.protpetverso_1;

import java.util.List;

public class Pet {
    private String nome;
    private int imagemResId; // ou String url se for carregar da internet
    private List<Tarefa> tarefas;
    private List<AlertaSaude> alertasSaude;

    public Pet(String nome, int imagemResId) {
        this.nome = nome;
        this.imagemResId = imagemResId;
    }

    public String getNome() {
        return nome;
    }

    public int getImagemResId() {
        return imagemResId;
    }

    public List<Tarefa> getTarefas() {
        return tarefas;
    }

    public List<AlertaSaude> getAlertasSaude() {
        return alertasSaude;
    }
}