package com.example.protpetverso_1;

import java.util.List;

public class Pet {
    private String nome;
    private int imagemResId; // ou String url se for carregar da internet
    private List<Tarefa> tarefas;
    private List<AlertaSaude> alertasSaude;

    public Pet(String nome, int imagemResId, List<Tarefa> tarefas, List<AlertaSaude> alertasSaude) {
        this.nome = nome;
        this.imagemResId = imagemResId;
        this.tarefas = tarefas;
        this.alertasSaude = alertasSaude;
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