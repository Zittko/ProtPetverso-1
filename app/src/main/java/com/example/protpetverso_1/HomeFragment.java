package com.example.protpetverso_1;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.example.protpetverso_1.vaccine.Vacina;

import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Home: pet selecionado (foto + setas), tarefas do dia DESSE pet, alertas DESSE pet.
 * Agenda global continua em AgendaFragment.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HOME";
    private static final int MAX_TAREFAS_HOME = 3;

    private TextView txtSemTarefasHome;
    private ViewPager2 viewPagerPets;
    private ImageButton btnAnterior, btnProximo;
    private RecyclerView recyclerViewTarefas, recyclerViewAlertasSaude;

    private SessionManager sessionManager;
    private final List<PetResumo> listaPets = new ArrayList<>();
    private HomePetPagerAdapter petPagerAdapter;

    private final List<Tarefa> tarefasHome = new ArrayList<>();
    private TarefaAdapter tarefaAdapter;

    private final List<AlertaSaude> alertasHome = new ArrayList<>();
    private AlertaSaudeAdapter alertaAdapter;

    public HomeFragment() {}

    private boolean podeAtualizarUi() {
        return isAdded() && getContext() != null && getView() != null;
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

        txtSemTarefasHome = view.findViewById(R.id.txtSemTarefasHome);
        viewPagerPets = view.findViewById(R.id.viewPagerPets);
        btnAnterior = view.findViewById(R.id.btnAnterior);
        btnProximo = view.findViewById(R.id.btnProximo);
        recyclerViewTarefas = view.findViewById(R.id.recyclerViewTarefas);
        recyclerViewAlertasSaude = view.findViewById(R.id.recyclerViewAlertasSaude);

        petPagerAdapter = new HomePetPagerAdapter(listaPets);
        if (viewPagerPets != null) {
            viewPagerPets.setAdapter(petPagerAdapter);
            viewPagerPets.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    if (podeAtualizarUi()) onPetSelecionado(position);
                }
            });
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

        tarefaAdapter = new TarefaAdapter(ctx, tarefasHome);
        if (recyclerViewTarefas != null) {
            recyclerViewTarefas.setLayoutManager(new LinearLayoutManager(ctx));
            recyclerViewTarefas.setAdapter(tarefaAdapter);
            recyclerViewTarefas.setNestedScrollingEnabled(false);
        }

        alertaAdapter = new AlertaSaudeAdapter(ctx, alertasHome);
        if (recyclerViewAlertasSaude != null) {
            recyclerViewAlertasSaude.setLayoutManager(new LinearLayoutManager(ctx));
            recyclerViewAlertasSaude.setAdapter(alertaAdapter);
            recyclerViewAlertasSaude.setNestedScrollingEnabled(false);
        }

        if (AppDataCache.get().temMeusPets()) {
            aplicarListaPets(AppDataCache.get().getMeusPets());
            selecionarPetInicial();
        }
        carregarPetsDaApi();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!podeAtualizarUi() || sessionManager == null) return;
        if (!listaPets.isEmpty()) {
            long idSalvo = sessionManager.obterPetId();
            for (int i = 0; i < listaPets.size(); i++) {
                if (listaPets.get(i).getIdPet() == idSalvo) {
                    if (viewPagerPets != null && viewPagerPets.getCurrentItem() != i) {
                        viewPagerPets.setCurrentItem(i, false);
                    }
                    onPetSelecionado(i);
                    return;
                }
            }
            onPetSelecionado(viewPagerPets != null ? viewPagerPets.getCurrentItem() : 0);
        } else {
            carregarPetsDaApi();
        }
    }

    private void aplicarListaPets(List<PetResumo> origem) {
        listaPets.clear();
        if (origem != null) listaPets.addAll(origem);
        if (petPagerAdapter != null) {
            // Preferir setItens se existir; senão notify
            try {
                petPagerAdapter.setItens(new ArrayList<>(listaPets));
            } catch (Throwable t) {
                petPagerAdapter.notifyDataSetChanged();
            }
        }
    }

    private void selecionarPetInicial() {
        if (listaPets.isEmpty() || viewPagerPets == null) return;
        long idSalvo = sessionManager.obterPetId();
        int idx = 0;
        for (int i = 0; i < listaPets.size(); i++) {
            if (listaPets.get(i).getIdPet() == idSalvo) {
                idx = i;
                break;
            }
        }
        viewPagerPets.setCurrentItem(idx, false);
        onPetSelecionado(idx);
    }

    private void carregarPetsDaApi() {
        if (sessionManager == null) return;
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) return;
        Context ctx = getContext();
        if (ctx == null) return;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                ApiConfig.URL_MEUS_PETS,
                null,
                response -> {
                    List<PetResumo> parseados = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject o = response.getJSONObject(i);
                            long id = o.optLong("idPet", o.optLong("id", -1));
                            String nome = o.optString("nome", "Pet");
                            String papel = o.optString("papel", "");
                            String foto = o.optString("fotoPetBase64", "");
                            if (foto.isEmpty()) foto = o.optString("fotoBase64", "");
                            if (foto.isEmpty()) foto = o.optString("foto", "");
                            if (id > 0) parseados.add(new PetResumo(id, nome, papel, foto));
                        } catch (Exception e) {
                            Log.e(TAG, "parse pet", e);
                        }
                    }
                    AppDataCache.get().setMeusPets(parseados);
                    if (!podeAtualizarUi()) return;
                    aplicarListaPets(parseados);
                    selecionarPetInicial();
                },
                error -> Log.e(TAG, "meusPets", error)
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

    private void onPetSelecionado(int position) {
        if (position < 0 || position >= listaPets.size() || sessionManager == null) return;
        PetResumo pet = listaPets.get(position);
        // Mantém papel para DONO/SUPORTE
        sessionManager.salvarPet(pet.getIdPet(), pet.getNome(), pet.getPapel());

        carregarTarefasDoPet(pet.getIdPet());
        carregarVacinasDoPet(pet.getIdPet());
    }

    /** Tarefas só do pet selecionado (Home). */
    private void carregarTarefasDoPet(long petId) {
        if (sessionManager == null || petId <= 0) return;
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) return;
        Context ctx = getContext();
        if (ctx == null) return;

        if (AppDataCache.get().temTarefas(petId)) {
            atualizarAgenda(AppDataCache.get().getTarefas(petId));
        }

        String url = ApiConfig.URL_TAREFAS_DO_PET + petId + "/tarefas";
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<Tarefa> todas = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            todas.add(Tarefa.fromJson(response.getJSONObject(i)));
                        } catch (Exception e) {
                            Log.e(TAG, "parse tarefa", e);
                        }
                    }
                    AppDataCache.get().setTarefas(petId, todas);
                    if (podeAtualizarUi() && sessionManager.obterPetId() == petId) {
                        atualizarAgenda(todas);
                    }
                },
                error -> Log.e(TAG, "tarefas pet", error)
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
        if (!podeAtualizarUi()) return;
        LocalDate hoje = LocalDate.now();
        List<Tarefa> doDia = new ArrayList<>();
        if (tarefasDoPet != null) {
            doDia = tarefasDoPet.stream()
                    .filter(t -> t != null && !t.isConcluida())
                    .filter(t -> t.getData() == null || t.getData().equals(hoje))
                    .sorted(Comparator.comparing(
                            Tarefa::getHorario, Comparator.nullsLast(Comparator.naturalOrder())))
                    .limit(MAX_TAREFAS_HOME)
                    .collect(Collectors.toList());
        }
        for (Tarefa t : doDia) {
            if (t.getImagemResId() == 0) {
                t.setImagemResId(TarefaIconHelper.resolverIcone(t.getTitulo(), t.getDesc()));
            }
        }
        tarefasHome.clear();
        tarefasHome.addAll(doDia);
        if (tarefaAdapter != null) tarefaAdapter.setItens(tarefasHome);

        boolean vazio = tarefasHome.isEmpty();
        if (recyclerViewTarefas != null) {
            recyclerViewTarefas.setVisibility(vazio ? View.GONE : View.VISIBLE);
        }
        if (txtSemTarefasHome != null) {
            txtSemTarefasHome.setVisibility(vazio ? View.VISIBLE : View.GONE);
        }
    }

    private void carregarVacinasDoPet(long petId) {
        if (sessionManager == null || petId <= 0) return;
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) return;
        Context ctx = getContext();
        if (ctx == null) return;

        if (AppDataCache.get().temVacinas(petId)) {
            montarAlertas(AppDataCache.get().getVacinas(petId));
        }

        String url = ApiConfig.URL_VACINAS_DO_PET + petId + "/vacinas";
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<Vacina> lista = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            lista.add(Vacina.fromJson(response.getJSONObject(i)));
                        } catch (Exception e) {
                            Log.e(TAG, "parse vacina", e);
                        }
                    }
                    AppDataCache.get().setVacinas(petId, lista);
                    if (podeAtualizarUi() && sessionManager.obterPetId() == petId) {
                        montarAlertas(lista);
                    }
                },
                error -> Log.e(TAG, "vacinas home", error)
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

    private void montarAlertas(List<Vacina> vacinas) {
        alertasHome.clear();
        LocalDate hoje = LocalDate.now();
        int icon = R.drawable.ic_vacina;

        if (vacinas != null) {
            for (Vacina v : vacinas) {
                if (v == null) continue;
                if (v.isPendenteOuAtrasada()) {
                    alertasHome.add(new AlertaSaude(
                            v.getNome(),
                            "Vacina " + v.getStatusLabel().toLowerCase()
                                    + " — prevista: " + v.getDataAplicacaoFormatada(),
                            icon
                    ));
                } else if (v.getDataProximaDose() != null
                        && !v.getDataProximaDose().isBefore(hoje)
                        && !v.getDataProximaDose().isAfter(hoje.plusDays(30))) {
                    alertasHome.add(new AlertaSaude(
                            v.getNome(),
                            "A data para renovar está próxima (" + v.getDataProximaDoseFormatada() + ").",
                            icon
                    ));
                }
            }
        }
        if (alertaAdapter != null) {
            alertaAdapter.setItens(alertasHome);
        }
    }
}