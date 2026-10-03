package com.example.protpetverso_1.pet;

public class SolicitacaoVinculo {
    public long idSolicitacao;
    public String nomeSolicitante;
    public String nomePet;
    public String dataDeCriacao;

    public SolicitacaoVinculo(long id, String solicitante, String pet, String data) {
        this.idSolicitacao = id;
        this.nomeSolicitante = solicitante;
        this.nomePet = pet;
        this.dataDeCriacao = data;
    }
}