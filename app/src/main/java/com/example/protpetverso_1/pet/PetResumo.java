package com.example.protpetverso_1.pet;

/**
 * Item da lista GET /api/pets/meusPets
 * (PetResumoDTO: idPet, nome, papel, fotoPetBase64)
 */
public class PetResumo {

    public long idPet;
    public String nome;
    public String papel; // DONO | SUPORTE
    public String codigoVinculo;
    public String fotoPetBase64; // novo campo da API

    public PetResumo(long idPet, String nome, String papel, String codigoVinculo, String fotoPetBase64) {
        this.idPet = idPet;
        this.nome = nome;
        this.papel = papel;
        this.codigoVinculo = codigoVinculo;
        this.fotoPetBase64 = fotoPetBase64;
    }

    // Construtor de compatibilidade (caso ainda exista código antigo)
    public PetResumo(long idPet, String nome, String papel, String codigoVinculo) {
        this(idPet, nome, papel, codigoVinculo, null);
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

    public String getFotoPetBase64() {
        return fotoPetBase64;
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

    /** Retorna true se existe foto em Base64 */
    public boolean hasFoto() {
        return fotoPetBase64 != null && !fotoPetBase64.trim().isEmpty();
    }
}