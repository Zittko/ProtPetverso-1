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
import com.example.protpetverso_1.vaccine.Vacina;

import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DataPreloader {

    private static final String TAG = "DATA_PRELOADER";

    private DataPreloader() {}

    public static void preloadTudo(Context context) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        AppDataCache.get().init(app);

        SessionManager sm = new SessionManager(app);
        String token = sm.obterToken();
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "Sem token");
            return;
        }

        preloadUsuario(app, token);
        preloadMeusPets(app, token, sm);
        preloadTarefasDoDia(app, token);
    }

    public static void preloadPetSelecionado(Context context, long petId) {
        if (context == null || petId <= 0) return;
        Context app = context.getApplicationContext();
        String token = new SessionManager(app).obterToken();
        if (token == null || token.isEmpty()) return;

        preloadPerfilPet(app, token, petId);
        preloadTarefasPet(app, token, petId);
        preloadVacinasPet(app, token, petId);
    }

    private static void preloadUsuario(Context app, String token) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, ApiConfig.URL_USUARIO_PERFIL, null,
                response -> {
                    AppDataCache.get().setUsuarioPerfil(response);
                    Log.d(TAG, "usuário ok");
                },
                error -> Log.e(TAG, "usuário", error)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return auth(token);
            }
        };
        VolleySingleton.getInstance(app).addToRequestQueue(request);
    }

    private static void preloadMeusPets(Context app, String token, SessionManager sm) {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, ApiConfig.URL_MEUS_PETS, null,
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
                        } catch (Exception e) {
                            Log.e(TAG, "parse pet", e);
                        }
                    }
                    AppDataCache.get().setMeusPets(lista);

                    long sel = sm.obterPetId();
                    boolean ok = false;
                    for (PetResumo p : lista) {
                        preloadPerfilPet(app, token, p.getIdPet());
                        preloadTarefasPet(app, token, p.getIdPet());
                        preloadVacinasPet(app, token, p.getIdPet());
                        if (p.getIdPet() == sel) ok = true;
                    }
                    if (!ok && !lista.isEmpty()) {
                        PetResumo p = lista.get(0);
                        sm.salvarPet(p.getIdPet(), p.getNome(), p.getPapel());
                    }
                    Log.d(TAG, "meusPets=" + lista.size());
                },
                error -> Log.e(TAG, "meusPets", error)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return auth(token);
            }
        };
        VolleySingleton.getInstance(app).addToRequestQueue(request);
    }

    private static void preloadPerfilPet(Context app, String token, long petId) {
        if (petId <= 0) return;
        String url = ApiConfig.URL_PET_PERFIL + petId + "/perfil";
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    AppDataCache.get().setPerfilPet(petId, response);
                    String foto = response.optString("fotoPetBase64", "");
                    if (foto.isEmpty()) foto = response.optString("fotoBase64", "");
                    if (!foto.isEmpty()) {
                        PhotoDiskCache.salvarFotoPet(app, petId, foto);
                    }
                },
                error -> Log.e(TAG, "perfil pet " + petId, error)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return auth(token);
            }
        };
        VolleySingleton.getInstance(app).addToRequestQueue(request);
    }

    private static void preloadTarefasPet(Context app, String token, long petId) {
        if (petId <= 0) return;
        String url = ApiConfig.URL_TAREFAS_DO_PET + petId + "/tarefas";
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<Tarefa> lista = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            lista.add(Tarefa.fromJson(response.getJSONObject(i)));
                        } catch (Exception e) {
                            Log.e(TAG, "tarefa", e);
                        }
                    }
                    AppDataCache.get().setTarefas(petId, lista);
                },
                error -> Log.e(TAG, "tarefas " + petId, error)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return auth(token);
            }
        };
        VolleySingleton.getInstance(app).addToRequestQueue(request);
    }

    private static void preloadVacinasPet(Context app, String token, long petId) {
        if (petId <= 0) return;
        String url = ApiConfig.URL_VACINAS_DO_PET + petId + "/vacinas";
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<Vacina> lista = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            lista.add(Vacina.fromJson(response.getJSONObject(i)));
                        } catch (Exception e) {
                            Log.e(TAG, "vacina", e);
                        }
                    }
                    AppDataCache.get().setVacinas(petId, lista);
                },
                error -> Log.e(TAG, "vacinas " + petId, error)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return auth(token);
            }
        };
        VolleySingleton.getInstance(app).addToRequestQueue(request);
    }

    private static void preloadTarefasDoDia(Context app, String token) {
        LocalDate hoje = LocalDate.now();
        String url = ApiConfig.URL_TAREFAS_POR_DIA + "?data=" + hoje;
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<Tarefa> lista = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            lista.add(Tarefa.fromJson(response.getJSONObject(i)));
                        } catch (Exception e) {
                            Log.e(TAG, "tarefa dia", e);
                        }
                    }
                    AppDataCache.get().setTarefasDoDia(hoje, lista);
                    Log.d(TAG, "agenda dia=" + lista.size());
                },
                error -> Log.e(TAG, "agenda dia", error)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return auth(token);
            }
        };
        VolleySingleton.getInstance(app).addToRequestQueue(request);
    }

    private static Map<String, String> auth(String token) {
        Map<String, String> h = new HashMap<>();
        h.put("Authorization", "Bearer " + token);
        return h;
    }
}