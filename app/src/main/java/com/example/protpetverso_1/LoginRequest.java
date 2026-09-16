package com.example.protpetverso_1;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * DTO de envio do formulário de login.
 * Monta o JSON com e-mail e senha.
 */
public class LoginRequest {

    private String email;
    private String senha;

    public LoginRequest(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("login", this.email); // a API espera "login"
        json.put("senha", this.senha);
        return json;
    }
}