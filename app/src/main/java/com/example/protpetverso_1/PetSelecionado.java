package com.example.protpetverso_1;

public class PetSelecionado {

    // Guarda o pet selecionado na tela inicial
    public static Pet petAtual = null;

    public static void setPet(Pet pet){
        petAtual = pet;
    }

    public static Pet getPet(){
        return petAtual;
    }
}
