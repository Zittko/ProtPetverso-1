package com.example.protpetverso_1;

import org.json.JSONException;
import org.json.JSONObject;

public class SolicitarVinculoRequest {
    private final String codigoVinculo;

    public SolicitarVinculoRequest(String codigoVinculo) {
        this.codigoVinculo = codigoVinculo;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("codigoVinculo", codigoVinculo);
        return json;
    }
}