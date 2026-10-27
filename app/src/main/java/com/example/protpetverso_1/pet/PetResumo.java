package com.example.protpetverso_1.pet;

public class PetResumo {

    private final long idPet;
    private final String nome;
    private final String papel;
    private final String fotoPetBase64;
    private final String fotoPath;

    public PetResumo(long idPet, String nome, String papel, String fotoPetBase64) {
        this(idPet, nome, papel, fotoPetBase64, null);
    }

    public PetResumo(long idPet, String nome, String papel, String fotoPetBase64, String fotoPath) {
        this.idPet = idPet;
        this.nome = nome;
        this.papel = papel;
        this.fotoPetBase64 = fotoPetBase64;
        this.fotoPath = fotoPath;
    }

    public long getIdPet() { return idPet; }

    public String getNome() {
        return nome != null ? nome : "Pet";
    }

    public String getPapel() {
        return papel != null ? papel : "";
    }

    public String getFotoPetBase64() {
        return fotoPetBase64;
    }

    public String getFotoPath() {
        return fotoPath;
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
        return (fotoPath != null && !fotoPath.trim().isEmpty())
                || (fotoPetBase64 != null && !fotoPetBase64.trim().isEmpty());
    }
}