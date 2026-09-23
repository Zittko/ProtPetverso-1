package com.example.protpetverso_1.user;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * DTO de envio do formulário de cadastro.
 * Monta o JSON que será enviado para a API.
 */
public class CadastroRequest {

    private String nome;
    private String email;
    private String telefone;
    private String senha;

    public CadastroRequest(String nome, String email, String telefone, String senha) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.senha = senha;
    }

    /**
     * Converte os dados do formulário em JSONObject para o Volley.
     */
    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("nome", this.nome);
        json.put("email", this.email);
        json.put("telefone", this.telefone);
        json.put("senha", this.senha);
        return json;
    }
}