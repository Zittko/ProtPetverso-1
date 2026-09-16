package com.example.protpetverso_1;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * DTO de resposta do login.
 * Lê id, nome, email e token devolvidos pela API.
 */
public class LoginResponse {

    private long idUsuario;
    private String nome;
    private String email;
    private String token;

    public static LoginResponse fromJsonObject(JSONObject json) throws JSONException {
        LoginResponse response = new LoginResponse();

        if (json.has("id_usuario")) {
            response.idUsuario = json.getLong("id_usuario");
        } else if (json.has("id")) {
            response.idUsuario = json.getLong("id");
        }

        response.nome = json.optString("nome", "");
        response.email = json.optString("email", "");
        response.token = json.optString("token", "");

        return response;
    }

    public long getIdUsuario() {
        return idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }
}