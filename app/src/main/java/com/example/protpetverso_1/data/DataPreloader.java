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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pré-carrega dados da API para o AppDataCache assim que o usuário entra no app (MenuActivity).
 *
 * Ordem:
 * 1) GET /api/usuarios/perfil
 * 2) GET /api/pets/meusPets
 * 3) Para CADA pet: GET /api/pets/{id}/perfil
 * 4) Para o pet selecionado (SessionManager): GET /api/pets/{id}/tarefas
 */
public final class DataPreloader {

    private static final String TAG = "DATA_PRELOADER";

    private DataPreloader() {
    }

    /** Chamar uma vez ao abrir a MenuActivity. */
    public static void preloadTudo(Context context) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        SessionManager sm = new SessionManager(app);
        String token = sm.obterToken();
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "Sem token — preload cancelado");
            return;
        }

        preloadUsuario(app, token);
        preloadMeusPets(app, token, sm);
    }

    /** Útil ao trocar o pet selecionado (drawer / lista). */
    public static void preloadPetSelecionado(Context context, long petId) {
        if (context == null || petId <= 0) return;
        Context app = context.getApplicationContext();
        SessionManager sm = new SessionManager(app);
        String token = sm.obterToken();
        if (token == null || token.isEmpty()) return;

        preloadPerfilPet(app, token, petId);
        preloadTarefasPet(app, token, petId);
    }

    // -------------------- USUÁRIO --------------------

    private static void preloadUsuario(Context app, String token) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.URL_USUARIO_PERFIL,
                null,
                response -> {
                    AppDataCache.get().setUsuarioPerfil(response);
                    Log.d(TAG, "Usuário em cache");
                },
                error -> Log.e(TAG, "Erro perfil usuário", error)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return headers(token);
            }
        };
        VolleySingleton.getInstance(app).addToRequestQueue(request);
    }

    // -------------------- LISTA DE PETS --------------------

    private static void preloadMeusPets(Context app, String token, SessionManager sm) {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                ApiConfig.URL_MEUS_PETS,
                null,
                response -> {
                    List<PetResumo> pets = parsePets(response);
                    AppDataCache.get().setMeusPets(pets);
                    AppDataCache.get().setListaPets(pets);
                    Log.d(TAG, "meusPets em cache: " + pets.size());

                    // Perfil de TODOS os pets
                    for (PetResumo pet : pets) {
                        if (pet.getIdPet() > 0) {
                            preloadPerfilPet(app, token, pet.getIdPet());
                        }
                    }

                    // Tarefas do pet selecionado (ou o primeiro da lista)
                    long petId = sm.obterPetId();
                    if (petId <= 0 && !pets.isEmpty()) {
                        petId = pets.get(0).getIdPet();
                        sm.salvarPet(petId, pets.get(0).getNome(), "", "", "", "", "", "");
                    }
                    if (petId > 0) {
                        preloadTarefasPet(app, token, petId);
                    }
                },
                error -> Log.e(TAG, "Erro meusPets", error)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return headers(token);
            }
        };
        VolleySingleton.getInstance(app).addToRequestQueue(request);
    }

    private static List<PetResumo> parsePets(JSONArray response) {
        List<PetResumo> lista = new ArrayList<>();
        if (response == null) return lista;

        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject o = response.getJSONObject(i);
                long id = o.optLong("idPet", o.optLong("id", -1));
                String nome = o.optString("nome", "Pet");
                String papel = o.optString("papel", "");
                String foto = o.optString("fotoPetBase64", "");
                if (foto.isEmpty()) {
                    foto = o.optString("fotoBase64", "");
                }
                if (id > 0) {
                    lista.add(new PetResumo(id, nome, papel, foto));
                }
            } catch (Exception e) {
                Log.e(TAG, "parse pet " + i, e);
            }
        }
        return lista;
    }

    // -------------------- PERFIL DO PET --------------------

    private static void preloadPerfilPet(Context app, String token, long petId) {
        String url = ApiConfig.URL_PET_PERFIL + petId + "/perfil";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    AppDataCache.get().setPerfilPet(petId, response);
                    Log.d(TAG, "Perfil pet " + petId + " em cache");
                },
                error -> Log.e(TAG, "Erro perfil pet " + petId, error)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return headers(token);
            }
        };
        VolleySingleton.getInstance(app).addToRequestQueue(request);
    }

    // -------------------- TAREFAS --------------------

    private static void preloadTarefasPet(Context app, String token, long petId) {
        String url = ApiConfig.URL_TAREFAS_DO_PET + petId + "/tarefas";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    List<Tarefa> tarefas = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            tarefas.add(Tarefa.fromJson(response.getJSONObject(i)));
                        } catch (Exception e) {
                            Log.e(TAG, "parse tarefa " + i, e);
                        }
                    }
                    AppDataCache.get().setTarefas(petId, tarefas);
                    Log.d(TAG, "Tarefas pet " + petId + ": " + tarefas.size());
                },
                error -> Log.e(TAG, "Erro tarefas pet " + petId, error)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return headers(token);
            }
        };
        VolleySingleton.getInstance(app).addToRequestQueue(request);
    }

    private static Map<String, String> headers(String token) {
        Map<String, String> h = new HashMap<>();
        h.put("Authorization", "Bearer " + token);
        h.put("Content-Type", "application/json");
        return h;
    }
}