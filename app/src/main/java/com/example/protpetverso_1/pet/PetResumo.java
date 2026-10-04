package com.example.protpetverso_1.pet;

/**
 * Item da lista GET /api/pets/meusPets
 * (PetResumoDTO: idPet, nome, codigoVinculo, papel)
 */
public class PetResumo {

    public long idPet;
    public String nome;
    public String papel; // DONO | SUPORTE
    public String codigoVinculo;

    public PetResumo(long idPet, String nome, String papel, String codigoVinculo) {
        this.idPet = idPet;
        this.nome = nome;
        this.papel = papel;
        this.codigoVinculo = codigoVinculo;
    }

    public long getIdPet() {
        return idPet;
    }

    public String getNome() {
        return nome != null ? nome : "Pet";
    }

    public String getPapel() {
        return papel != null ? papel : "";
    }

    public String getCodigoVinculo() {
        return codigoVinculo != null ? codigoVinculo : "";
    }

    /** Texto do card: Tutor principal / Suporte */
    public String getPapelLabel() {
        if (papel == null || papel.isEmpty()) return "";
        if (papel.equalsIgnoreCase("DONO")) return "Tutor principal";
        if (papel.equalsIgnoreCase("SUPORTE")) return "Suporte";
        return papel;
    }

    public boolean isDono() {
        return papel != null && papel.equalsIgnoreCase("DONO");
    }
}