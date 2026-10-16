package com.example.protpetverso_1.pet;

/**
 * GET /api/pets/meusPets
 * Campos: idPet/id, nome, papel, fotoPetBase64
 */
public class PetResumo {

    private final long idPet;
    private final String nome;
    private final String papel;
    private final String fotoPetBase64;

    public PetResumo(long idPet, String nome, String papel, String fotoPetBase64) {
        this.idPet = idPet;
        this.nome = nome;
        this.papel = papel;
        this.fotoPetBase64 = fotoPetBase64;
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

    public String getFotoPetBase64() {
        return fotoPetBase64;
    }

    public String getPapelLabel() {
        if (papel == null || papel.isEmpty()) return "";
        if (papel.equalsIgnoreCase("DONO")) return "Tutor principal";
        if (papel.equalsIgnoreCase("SUPORTE")) return "Suporte";
        return papel;
    }

    public boolean isDono() {
        return papel != null && papel.equalsIgnoreCase("DONO");
    }

    public boolean hasFoto() {
        return fotoPetBase64 != null && !fotoPetBase64.trim().isEmpty();
    }
}