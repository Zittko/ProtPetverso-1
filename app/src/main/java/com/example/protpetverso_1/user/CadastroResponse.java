package com.example.protpetverso_1.user;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * DTO de resposta do cadastro.
 * Lê o JSON devolvido pela API (id, nome, email, token).
 */
public class CadastroResponse {

    private long idUsuario;
    private String nome;
    private String email;
    private String token;

    public static CadastroResponse fromJsonObject(JSONObject json) throws JSONException {
        CadastroResponse response = new CadastroResponse();

        // A API devolve "idUsuario" (pode vir como String ou número)
        if (json.has("idUsuario")) {
            Object id = json.get("idUsuario");
            response.idUsuario = (id instanceof String)
                    ? Long.parseLong((String) id)
                    : json.getLong("idUsuario");
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