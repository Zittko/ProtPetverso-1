package com.example.protpetverso_1.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.VolleySingleton;
import com.example.protpetverso_1.pet.PetResumo;
import com.example.protpetverso_1.schedule.Tarefa;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dispara os GETs principais logo após o login / ao abrir MenuActivity.
 */
public final class DataPreloader {

    private static final String TAG = "DATA_PRELOAD";

    private DataPreloader() {}

    /** Chame uma vez na MenuActivity.onCreate (e após ações que mudam dados). */
    public static void preloadTudo(Context context) {
        Context app = context.getApplicationContext();
        SessionManager sm = new SessionManager(app);
        String token = sm.obterToken();
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "Sem token — preload cancelado");
            return;
        }

        carregarUsuario(app, token);
        carregarMeusPetsEDepoisPetEAgenda(app, token, sm);
        // Depois: carregarSolicitacoes(app, token);
    }

    // ---------- Usuário ----------
    private static void carregarUsuario(Context ctx, String token) {
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.URL_USUARIO_PERFIL,
                null,
                response -> {
                    AppDataCache.get().setUsuarioPerfil(response);
                    // Espelha no SessionManager o que for útil offline
                    SessionManager sm = new SessionManager(ctx);
                    String apelido = response.optString("apelido", "");
                    String tel = response.optString("telefone", "");
                    if (!apelido.isEmpty()) sm.salvarApelido(apelido);
                    if (!tel.isEmpty()) sm.salvarTelefone(tel);
                    Log.d(TAG, "Usuário em cache");
                },
                error -> Log.e(TAG, "Erro usuário: " + status(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return auth(token);
            }
        };
        VolleySingleton.getInstance(ctx).addToRequestQueue(req);
    }

    // ---------- Lista de pets → perfil + tarefas do selecionado ----------
    private static void carregarMeusPetsEDepoisPetEAgenda(Context ctx, String token, SessionManager sm) {
        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                ApiConfig.URL_MEUS_PETS,
                null,
                response -> {
                    List<PetResumo> lista = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject o = response.getJSONObject(i);
                            long id = o.optLong("idPet", o.optLong("id", -1));
                            String nome = o.optString("nome", "Pet");
                            String papel = o.optString("papel", "");
                            String foto = o.optString("fotoPetBase64", "");
                            if (foto.isEmpty()) foto = o.optString("fotoBase64", "");
                            if (id > 0) lista.add(new PetResumo(id, nome, papel, foto));
                        } catch (Exception ignored) {}
                    }
                    AppDataCache.get().setMeusPets(lista);
                    Log.d(TAG, "Meus pets em cache: " + lista.size());

                    long petId = sm.obterPetId();
                    if (petId <= 0 && !lista.isEmpty()) {
                        PetResumo p = lista.get(0);
                        petId = p.getIdPet();
                        sm.salvarPet(petId, p.getNome(), "", "", "", "", "", "");
                    }

                    if (petId > 0) {
                        carregarPerfilPet(ctx, token, petId);
                        carregarTarefasPet(ctx, token, petId);
                    }
                },
                error -> Log.e(TAG, "Erro meusPets: " + status(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return auth(token);
            }
        };
        VolleySingleton.getInstance(ctx).addToRequestQueue(req);
    }

    private static void carregarPerfilPet(Context ctx, String token, long petId) {
        String url = ApiConfig.URL_PET_PERFIL + petId + "/perfil";
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    AppDataCache.get().setPerfilPet(petId, response);
                    Log.d(TAG, "Perfil pet " + petId + " em cache");
                },
                error -> Log.e(TAG, "Erro perfil pet: " + status(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return auth(token);
            }
        };
        VolleySingleton.getInstance(ctx).addToRequestQueue(req);
    }

    private static void carregarTarefasPet(Context ctx, String token, long petId) {
        String url = ApiConfig.URL_TAREFAS_DO_PET + petId + "/tarefas";
        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<Tarefa> tarefas = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            tarefas.add(Tarefa.fromJson(response.getJSONObject(i)));
                        } catch (Exception ignored) {}
                    }
                    AppDataCache.get().setTarefas(petId, tarefas);
                    Log.d(TAG, "Tarefas pet " + petId + ": " + tarefas.size());
                },
                error -> Log.e(TAG, "Erro tarefas: " + status(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return auth(token);
            }
        };
        VolleySingleton.getInstance(ctx).addToRequestQueue(req);
    }

    /** Chame quando o usuário trocar o pet selecionado. */
    public static void preloadPetSelecionado(Context context, long petId) {
        if (petId <= 0) return;
        SessionManager sm = new SessionManager(context);
        String token = sm.obterToken();
        if (token == null || token.isEmpty()) return;
        Context app = context.getApplicationContext();
        carregarPerfilPet(app, token, petId);
        carregarTarefasPet(app, token, petId);
    }

    private static Map<String, String> auth(String token) {
        Map<String, String> h = new HashMap<>();
        h.put("Authorization", "Bearer " + token);
        return h;
    }

    private static String status(com.android.volley.VolleyError error) {
        return error.networkResponse != null
                ? String.valueOf(error.networkResponse.statusCode)
                : "sem rede";
    }
}