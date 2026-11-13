package com.example.protpetverso_1.notification;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Histórico local da lista de notificações (sino). Fica no aparelho, funciona offline
 * e é apagado no logout. Guarda no máximo MAX itens, os mais recentes primeiro.
 */
public final class NotificacaoStore {

    private static final String PREF = "petverso_notificacoes";
    private static final String KEY_LISTA = "lista";
    private static final int MAX = 100;
    /** Cada notificação some da lista sozinha 15 dias depois de criada. */
    public static final long VALIDADE_MS = 15L * 24 * 60 * 60 * 1000;

    private NotificacaoStore() {}

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static synchronized List<NotificacaoLocal> listar(Context c) {
        List<NotificacaoLocal> out = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs(c).getString(KEY_LISTA, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                if (arr.optJSONObject(i) != null) out.add(NotificacaoLocal.fromJson(arr.getJSONObject(i)));
            }
        } catch (Exception ignored) { }

        // Limpeza automática: remove as com mais de 15 dias e grava a lista já enxuta
        long limite = System.currentTimeMillis() - VALIDADE_MS;
        boolean removeu = false;
        for (java.util.Iterator<NotificacaoLocal> it = out.iterator(); it.hasNext(); ) {
            if (it.next().getCriadaEm() < limite) {
                it.remove();
                removeu = true;
            }
        }
        if (removeu) gravar(c, out);
        return out;
    }

    private static void gravar(Context c, List<NotificacaoLocal> lista) {
        JSONArray arr = new JSONArray();
        for (NotificacaoLocal n : lista) arr.put(n.toJson());
        prefs(c).edit().putString(KEY_LISTA, arr.toString()).apply();
    }

    public static synchronized NotificacaoLocal adicionar(Context c, String tipo, String titulo,
                                                          String descricao, long idPet) {
        List<NotificacaoLocal> lista = listar(c);
        long agora = System.currentTimeMillis();
        NotificacaoLocal n = new NotificacaoLocal(agora, tipo, titulo, descricao, idPet, agora, false);
        lista.add(0, n);
        while (lista.size() > MAX) lista.remove(lista.size() - 1);
        gravar(c, lista);
        return n;
    }

    public static synchronized void marcarLida(Context c, long id) {
        List<NotificacaoLocal> lista = listar(c);
        for (NotificacaoLocal n : lista) if (n.getId() == id) n.setLida(true);
        gravar(c, lista);
    }

    public static synchronized void marcarTodasLidas(Context c) {
        List<NotificacaoLocal> lista = listar(c);
        for (NotificacaoLocal n : lista) n.setLida(true);
        gravar(c, lista);
    }

    public static synchronized int naoLidas(Context c) {
        int total = 0;
        for (NotificacaoLocal n : listar(c)) if (!n.isLida()) total++;
        return total;
    }

    /** Avisa quando a lista muda (nova notificação, lida, limpa) — usado pela bolinha do sino. */
    public static void registrarOuvinte(Context c, SharedPreferences.OnSharedPreferenceChangeListener l) {
        prefs(c).registerOnSharedPreferenceChangeListener(l);
    }

    public static void removerOuvinte(Context c, SharedPreferences.OnSharedPreferenceChangeListener l) {
        prefs(c).unregisterOnSharedPreferenceChangeListener(l);
    }

    public static synchronized void limpar(Context c) {
        prefs(c).edit().remove(KEY_LISTA).apply();
    }
}