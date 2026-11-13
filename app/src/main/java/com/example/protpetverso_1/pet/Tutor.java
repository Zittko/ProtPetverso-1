package com.example.protpetverso_1.pet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Tutor vinculado (aceito) a um pet, como devolvido por GET /api/pets/{id}/tutores.
 * Não traz e-mail nem telefone: só o necessário para mostrar a lista.
 */
public class Tutor {

    public static final String PAPEL_DONO = "DONO";
    public static final String PAPEL_SUPORTE = "SUPORTE";

    private final long idUsuario;
    private final String nome;
    private final String apelido;
    private final String papel;
    /** Só vem da API (se ela enviar); não é gravado no JSON do cache, vai para o PhotoDiskCache. */
    private String fotoBase64 = "";

    public Tutor(long idUsuario, String nome, String apelido, String papel) {
        this.idUsuario = idUsuario;
        this.nome = nome != null ? nome : "";
        this.apelido = apelido != null ? apelido : "";
        this.papel = papel != null ? papel.trim().toUpperCase() : "";
    }

    /** Lê com tolerância: aceita nomes de campo alternativos e ignora o que faltar. */
    public static Tutor fromJson(JSONObject o) {
        if (o == null) return null;
        long id = o.optLong("idUsuario", o.optLong("id", -1));
        String nome = o.optString("nome", o.optString("nomeUsuario", ""));
        String apelido = o.isNull("apelido") ? "" : o.optString("apelido", "");
        String papel = o.optString("papel", "");
        Tutor t = new Tutor(id, nome, apelido, papel);
        for (String k : new String[]{"fotoBase64", "fotoPerfilBase64", "foto"}) {
            if (!o.isNull(k) && !o.optString(k, "").isEmpty()) {
                t.fotoBase64 = o.optString(k, "");
                break;
            }
        }
        return t;
    }

    public static List<Tutor> listaFromJson(JSONArray arr) {
        List<Tutor> lista = new ArrayList<>();
        if (arr == null) return lista;
        for (int i = 0; i < arr.length(); i++) {
            Tutor t = fromJson(arr.optJSONObject(i));
            if (t != null) lista.add(t);
        }
        return lista;
    }

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("idUsuario", idUsuario);
            o.put("nome", nome);
            o.put("apelido", apelido);
            o.put("papel", papel);
        } catch (JSONException ignored) { }
        return o;
    }

    public long getIdUsuario() { return idUsuario; }
    public String getFotoBase64() { return fotoBase64; }
    public String getPapel() { return papel; }
    public boolean isDono() { return PAPEL_DONO.equals(papel); }
    public boolean isSuporte() { return !isDono(); }

    /** Apelido quando existir; senão o nome. */
    public String getNomeExibicao() {
        if (!apelido.trim().isEmpty()) return apelido.trim();
        if (!nome.trim().isEmpty()) return nome.trim();
        return "Tutor";
    }

    public static String rotuloPapel(boolean dono) {
        return dono ? "Tutor Principal" : "Suporte";
    }
}