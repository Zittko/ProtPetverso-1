package com.example.protpetverso_1;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.protpetverso_1.data.AppDataCache;
import com.example.protpetverso_1.pet.PetResumo;
import com.example.protpetverso_1.schedule.Tarefa;
import com.example.protpetverso_1.schedule.TarefaAdapter;
import com.example.protpetverso_1.schedule.TarefaIconHelper;
import com.example.protpetverso_1.vaccine.AlertaSaude;
import com.example.protpetverso_1.vaccine.AlertaSaudeAdapter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Home integrada à API.
 * Importante: callbacks do Volley só atualizam a UI se o Fragment ainda estiver attached
 * (isAdded() + getContext() != null), senão ocorre:
 * IllegalStateException: Fragment not attached to a context.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HOME";
    private static final int MAX_TAREFAS_HOME = 2;

    private ViewPager2 viewPagerPets;
    private ImageButton btnAnterior, btnProximo;
    private RecyclerView recyclerViewTarefas, recyclerViewTarefasAlertasSaude;

    private SessionManager sessionManager;
    private final List<PetResumo> listaPets = new ArrayList<>();
    private HomePetPagerAdapter petPagerAdapter;

    public HomeFragment() {
    }

    /** true só se ainda podemos usar requireContext() / views com segurança */
    private boolean podeAtualizarUi() {
        return isAdded() && getContext() != null && !isDetached() && getView() != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context ctx = getContext();
        if (ctx == null) return;

        sessionManager = new SessionManager(ctx);

        viewPagerPets = view.findViewById(R.id.viewPagerPets);
        btnAnterior = view.findViewById(R.id.btnAnterior);
        btnProximo = view.findViewById(R.id.btnProximo);
        recyclerViewTarefas = view.findViewById(R.id.recyclerViewTarefas);
        recyclerViewTarefasAlertasSaude = view.findViewById(R.id.recyclerViewAlertasSaude);

        if (recyclerViewTarefas != null) {
            recyclerViewTarefas.setLayoutManager(new LinearLayoutManager(ctx));
        }
        if (recyclerViewTarefasAlertasSaude != null) {
            recyclerViewTarefasAlertasSaude.setLayoutManager(new LinearLayoutManager(ctx));
        }

        petPagerAdapter = new HomePetPagerAdapter(listaPets);
        if (viewPagerPets != null) {
            viewPagerPets.setAdapter(petPagerAdapter);
        }

        if (btnProximo != null) {
            btnProximo.setOnClickListener(v -> {
                if (listaPets.isEmpty() || viewPagerPets == null) return;
                int p = (viewPagerPets.getCurrentItem() + 1) % listaPets.size();
                viewPagerPets.setCurrentItem(p, true);
            });
        }

        if (btnAnterior != null) {
            btnAnterior.setOnClickListener(v -> {
                if (listaPets.isEmpty() || viewPagerPets == null) return;
                int p = (viewPagerPets.getCurrentItem() - 1 + listaPets.size()) % listaPets.size();
                viewPagerPets.setCurrentItem(p, true);
            });
        }

        if (viewPagerPets != null) {
            viewPagerPets.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    if (podeAtualizarUi()) {
                        onPetSelecionado(position);
                    }
                }
            });
        }

        if (AppDataCache.get().temListaPets()) {
            listaPets.clear();
            listaPets.addAll(AppDataCache.get().getListaPets());
            if (petPagerAdapter != null) {
                petPagerAdapter.notifyDataSetChanged();
            }
            selecionarPetInicial();
        }
        carregarPetsDaApi();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!podeAtualizarUi() || sessionManager == null || listaPets.isEmpty()) return;

        long idSalvo = sessionManager.obterPetId();
        for (int i = 0; i < listaPets.size(); i++) {
            if (listaPets.get(i).getIdPet() == idSalvo) {
                if (viewPagerPets != null && viewPagerPets.getCurrentItem() != i) {
                    viewPagerPets.setCurrentItem(i, false);
                }
                break;
            }
        }
    }

    // ==================== PETS ====================

    private void carregarPetsDaApi() {
        if (sessionManager == null) return;
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "Sem token — não carrega meusPets");
            return;
        }

        Context ctx = getContext();
        if (ctx == null) return;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                ApiConfig.URL_MEUS_PETS,
                null,
                response -> {
                    // Fragment pode ter sido trocado (Pets/Agenda) enquanto a API respondia
                    if (!podeAtualizarUi()) {
                        // Ainda grava no cache para as outras telas
                        List<PetResumo> temp = parsePets(response);
                        AppDataCache.get().setListaPets(temp);
                        AppDataCache.get().setMeusPets(temp);
                        return;
                    }

                    listaPets.clear();
                    listaPets.addAll(parsePets(response));
                    AppDataCache.get().setListaPets(listaPets);
                    AppDataCache.get().setMeusPets(listaPets);

                    if (petPagerAdapter != null) {
                        petPagerAdapter.notifyDataSetChanged();
                    }
                    selecionarPetInicial();
                },
                error -> {
                    Log.e(TAG, "meusPets erro", error);
                    if (podeAtualizarUi() && listaPets.isEmpty()) {
                        atualizarAgenda(new ArrayList<>());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Authorization", "Bearer " + token);
                return h;
            }
        };

        VolleySingleton.getInstance(ctx.getApplicationContext()).addToRequestQueue(request);
    }

    private List<PetResumo> parsePets(org.json.JSONArray response) {
        List<PetResumo> lista = new ArrayList<>();
        if (response == null) return lista;
        for (int i = 0; i < response.length(); i++) {
            try {
                org.json.JSONObject o = response.getJSONObject(i);
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

    private void selecionarPetInicial() {
        if (!podeAtualizarUi()) return;

        if (listaPets.isEmpty()) {
            atualizarAgenda(new ArrayList<>());
            atualizarSaude(new ArrayList<>());
            return;
        }

        int index = 0;
        long idSalvo = sessionManager != null ? sessionManager.obterPetId() : -1;
        for (int i = 0; i < listaPets.size(); i++) {
            if (listaPets.get(i).getIdPet() == idSalvo) {
                index = i;
                break;
            }
        }
        if (viewPagerPets != null) {
            viewPagerPets.setCurrentItem(index, false);
        }
        onPetSelecionado(index);
    }

    private void onPetSelecionado(int position) {
        if (!podeAtualizarUi()) return;
        if (position < 0 || position >= listaPets.size()) return;
        if (sessionManager == null) return;

        PetResumo pet = listaPets.get(position);
        sessionManager.salvarPet(
                pet.getIdPet(),
                pet.getNome(),
                "", "", "", "", "", ""
        );

        if (AppDataCache.get().temTarefas(pet.getIdPet())) {
            atualizarAgenda(AppDataCache.get().getTarefas(pet.getIdPet()));
        }
        carregarTarefasDoPet(pet.getIdPet());
        atualizarSaude(new ArrayList<>());
    }

    // ==================== TAREFAS ====================

    private void carregarTarefasDoPet(long petId) {
        if (sessionManager == null || petId <= 0) return;
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) return;

        Context ctx = getContext();
        if (ctx == null) return;

        String url = ApiConfig.URL_TAREFAS_DO_PET + petId + "/tarefas";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    List<Tarefa> todas = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            todas.add(Tarefa.fromJson(response.getJSONObject(i)));
                        } catch (Exception e) {
                            Log.e(TAG, "parse tarefa " + i, e);
                        }
                    }
                    AppDataCache.get().setTarefas(petId, todas);

                    // Só atualiza UI se ainda estiver na Home e for o pet atual
                    if (!podeAtualizarUi() || sessionManager == null) return;
                    if (sessionManager.obterPetId() == petId) {
                        atualizarAgenda(todas);
                    }
                },
                error -> Log.e(TAG, "tarefas erro petId=" + petId, error)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Authorization", "Bearer " + token);
                return h;
            }
        };

        VolleySingleton.getInstance(ctx.getApplicationContext()).addToRequestQueue(request);
    }

    private void atualizarAgenda(List<Tarefa> tarefasDoPet) {
        if (!podeAtualizarUi() || recyclerViewTarefas == null) return;

        Context ctx = getContext();
        if (ctx == null) return;

        List<Tarefa> proximas = filtrarProximasTarefas(tarefasDoPet, MAX_TAREFAS_HOME);
        for (Tarefa t : proximas) {
            if (t.getImagemResId() == 0) {
                t.setImagemResId(TarefaIconHelper.resolverIcone(t.getTitulo(), t.getDesc()));
            }
        }

        TarefaAdapter adapter = new TarefaAdapter(ctx, proximas);
        adapter.setOnTarefaClickListener((tarefa, position) -> {
            // opcional
        });
        recyclerViewTarefas.setAdapter(adapter);
    }

    private List<Tarefa> filtrarProximasTarefas(List<Tarefa> origem, int limite) {
        if (origem == null || origem.isEmpty()) return new ArrayList<>();

        LocalDate hoje = LocalDate.now();
        LocalTime agora = LocalTime.now();

        return origem.stream()
                .filter(t -> t != null && !t.isConcluida())
                .filter(t -> {
                    if (t.getData() == null) return false;
                    if (t.getData().isAfter(hoje)) return true;
                    if (t.getData().isBefore(hoje)) return false;
                    return t.getHorario() == null || !t.getHorario().isBefore(agora);
                })
                .sorted(Comparator
                        .comparing(Tarefa::getData, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Tarefa::getHorario, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(limite)
                .collect(Collectors.toList());
    }

    private void atualizarSaude(List<AlertaSaude> lista) {
        if (!podeAtualizarUi() || recyclerViewTarefasAlertasSaude == null) return;
        Context ctx = getContext();
        if (ctx == null) return;

        List<AlertaSaude> dados = lista != null ? lista : new ArrayList<>();
        recyclerViewTarefasAlertasSaude.setAdapter(new AlertaSaudeAdapter(ctx, dados));
    }
}