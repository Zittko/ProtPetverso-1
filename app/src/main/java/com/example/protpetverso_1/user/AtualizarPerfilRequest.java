package com.example.protpetverso_1.user;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * DTO de envio para atualizar o perfil do usuário
 * (apelido + foto em Base64).
 *
 * Endpoint: PUT /api/usuarios/atualizarPerfil
 * Header: Authorization: Bearer <token>
 */
public class AtualizarPerfilRequest {

    private String apelido;
    private String fotoBase64; // pode ser null

    public AtualizarPerfilRequest(String apelido, String fotoBase64) {
        this.apelido = apelido;
        this.fotoBase64 = fotoBase64;
    }

    /**
     * Monta o JSON esperado pela API:
     * {
     *   "apelido": "Juninho",
     *   "fotoBase64": "string_da_foto_ou_null"
     * }
     */
    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("apelido", apelido);
        if (fotoBase64 != null && !fotoBase64.isEmpty()) {
            json.put("fotoBase64", fotoBase64);
        } else {
            json.put("fotoBase64", JSONObject.NULL);
        }
        return json;
    }
}