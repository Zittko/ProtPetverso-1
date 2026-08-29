package com.example.protpetverso_1;

public class ComponentesEletronicos {
    private String nome;
    private String desc;
    private String preco;
    private int imagem;

    public ComponentesEletronicos(String nome, String desc, String preco, int imagem) {
        this.nome = nome;
        this.desc = desc;
        this.preco = preco;
        this.imagem = imagem;
    }

    public String getNome() {
        return nome;
    }

    public String getDesc() {
        return desc;
    }

    public String getPreco() {
        return preco;
    }

    public int getImagem() {
        return imagem;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setPreco(String preco) {
        this.preco = preco;
    }

    public void setImagem(int imagem) {
        this.imagem = imagem;
    }
}
