package com.example.protpetverso_1;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * DTO de envio para cadastrar um pet.
 * Campos no padrão da API:
 * nome, raca, especie, dataDeNascimento, porte, peso, sexo
 */
public class CadastrarPetRequest {

    private String nome;
    private String raca;
    private String especie;
    private String dataDeNascimento; // yyyy-MM-dd
    private String porte;            // PEQUENO, MEDIO, GRANDE
    private double peso;
    private String sexo;             // MACHO, FEMEA

    public CadastrarPetRequest(String nome, String raca, String especie,
                               String dataDeNascimento, String porte,
                               double peso, String sexo) {
        this.nome = nome;
        this.raca = raca;
        this.especie = especie;
        this.dataDeNascimento = dataDeNascimento;
        this.porte = porte;
        this.peso = peso;
        this.sexo = sexo;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("nome", nome);
        json.put("raca", raca);
        json.put("especie", especie);
        json.put("dataDeNascimento", dataDeNascimento);
        json.put("porte", porte);
        json.put("peso", peso);
        json.put("sexo", sexo);
        return json;
    }
}