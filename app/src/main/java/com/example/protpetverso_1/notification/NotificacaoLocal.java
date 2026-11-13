package com.example.protpetverso_1.notification;

import org.json.JSONException;
import org.json.JSONObject;

/** Uma notificação registrada na lista do sino (guardada no aparelho). */
public class NotificacaoLocal {

    public static final String TIPO_ALIMENTACAO = "ALIMENTACAO";
    public static final String TIPO_PASSEIO = "PASSEIO";
    public static final String TIPO_VACINAS = "VACINAS";
    public static final String TIPO_TESTE = "TESTE";

    private final long id;
    private final String tipo;
    private final String titulo;
    private final String descricao;
    private final long idPet;
    private final long criadaEm;
    private boolean lida;

    public NotificacaoLocal(long id, String tipo, String titulo, String descricao,
                            long idPet, long criadaEm, boolean lida) {
        this.id = id;
        this.tipo = tipo != null ? tipo : "";
        this.titulo = titulo != null ? titulo : "";
        this.descricao = descricao != null ? descricao : "";
        this.idPet = idPet;
        this.criadaEm = criadaEm;
        this.lida = lida;
    }

    public static NotificacaoLocal fromJson(JSONObject o) {
        return new NotificacaoLocal(
                o.optLong("id"), o.optString("tipo", ""), o.optString("titulo", ""),
                o.optString("descricao", ""), o.optLong("idPet", -1),
                o.optLong("criadaEm"), o.optBoolean("lida", false));
    }

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", id);
            o.put("tipo", tipo);
            o.put("titulo", titulo);
            o.put("descricao", descricao);
            o.put("idPet", idPet);
            o.put("criadaEm", criadaEm);
            o.put("lida", lida);
        } catch (JSONException ignored) { }
        return o;
    }

    public long getId() { return id; }
    public String getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public long getIdPet() { return idPet; }
    public long getCriadaEm() { return criadaEm; }
    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }
}
