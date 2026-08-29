package com.example.protpetverso_1;

public class Pet {
    private String nome;
    private int imagemResId; // ou String url se for carregar da internet

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
}