package com.example.protpetverso_1;

public class PetResumo {
    private long idPet;
    private String nome;
    private String codigoVinculo;
    private String papel; // PRINCIPAL, SUPORTE, etc.

    public PetResumo(long idPet, String nome, String codigoVinculo, String papel) {
        this.idPet = idPet;
        this.nome = nome;
        this.codigoVinculo = codigoVinculo;
        this.papel = papel;
    }

    public long getIdPet() { return idPet; }
    public String getNome() { return nome; }
    public String getCodigoVinculo() { return codigoVinculo; }
    public String getPapel() { return papel; }
}