package com.example.protpetverso_1.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.VolleySingleton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Agenda: barra de dias + lista de tarefas do pet selecionado (API).
 * GET /api/pets/{id}/tarefas  → filtra pelo dia na tela
 * FAB → CriarTarefaActivity
 */
public class AgendaFragment extends Fragment {

    private static final String TAG = "AGENDA";

    private RecyclerView recyclerDias, recyclerTarefas;
    private TextView txtTituloLista, txtSemTarefas, txtMesAno;
    private ImageView btnExpandirCalendario;
    private FloatingActionButton btnNovaTarefa;

    private DiaAdapter diaAdapter;
    private TarefaAdapter tarefaAdapter;

    private final List<LocalDate> listaDias = new ArrayList<>();
    private final List<Tarefa> listaTarefasExibidas = new ArrayList<>();
    /** Todas as tarefas do pet vindas da API (sem filtrar por dia). */
    private final List<Tarefa> todasTarefasDoPet = new ArrayList<>();

    private LocalDate diaSelecionado;
    private SessionManager sessionManager;
    private long petId = -1;

    public AgendaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agenda, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        petId = sessionManager.obterPetId();

        recyclerDias = view.findViewById(R.id.recyclerDias);
        recyclerTarefas = view.findViewById(R.id.recyclerTarefas);
        txtTituloLista = view.findViewById(R.id.txtTituloLista);
        txtSemTarefas = view.findViewById(R.id.txtSemTarefas);
        btnExpandirCalendario = view.findViewById(R.id.btnExpandirCalendario);
        btnNovaTarefa = view.findViewById(R.id.btnNovaTarefa);

        recyclerTarefas.setLayoutManager(new LinearLayoutManager(requireContext()));
        tarefaAdapter = new TarefaAdapter(requireContext(), listaTarefasExibidas);
        recyclerTarefas.setAdapter(tarefaAdapter);

        configurarBarraDeDias();

        btnNovaTarefa.setOnClickListener(v -> {
            if (petId <= 0) {
                Toast.makeText(requireContext(), "Selecione um pet antes de criar tarefa.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(requireContext(), CriarTarefaActivity.class);
            i.putExtra("PET_ID", petId);
            i.putExtra("DATA_PADRAO", diaSelecionado != null ? diaSelecionado.toString() : LocalDate.now().toString());
            startActivity(i);
        });

        if (btnExpandirCalendario != null) {
            btnExpandirCalendario.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Calendário completo em breve", Toast.LENGTH_SHORT).show());
        }

        carregarTarefasDoPet();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Atualiza pet e lista ao voltar da criação de tarefa / troca de pet
        if (sessionManager != null) {
            long novoId = sessionManager.obterPetId();
            if (novoId != petId) {
                petId = novoId;
            }
            carregarTarefasDoPet();
        }
    }

    private void configurarBarraDeDias() {
        listaDias.clear();
        LocalDate hoje = LocalDate.now();
        for (int i = 0; i < 14; i++) { // 2 semanas
            listaDias.add(hoje.plusDays(i));
        }
        diaSelecionado = hoje;
        atualizarTextoMesAno();

        recyclerDias.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        diaAdapter = new DiaAdapter(requireContext(), listaDias, diaSelecionado, dataClicada -> {
            diaSelecionado = dataClicada;
            diaAdapter.setDiaSelecionado(diaSelecionado);
            filtrarTarefasPorDia();
        });
        recyclerDias.setAdapter(diaAdapter);
    }

    /**
     * GET /api/pets/{id}/tarefas
     * Depois filtra pelo dia selecionado em filtrarTarefasPorDia().
     */
    private void carregarTarefasDoPet() {
        petId = sessionManager.obterPetId();
        if (petId <= 0) {
            txtTituloLista.setText("Nenhum pet selecionado");
            todasTarefasDoPet.clear();
            listaTarefasExibidas.clear();
            tarefaAdapter.setItens(listaTarefasExibidas);
            atualizarVisibilidadeLista();
            return;
        }

        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Sessão expirada.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConfig.URL_TAREFAS_DO_PET + petId + "/tarefas";
        Log.d(TAG, "GET " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    todasTarefasDoPet.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject o = response.getJSONObject(i);
                            todasTarefasDoPet.add(Tarefa.fromJson(o));
                        } catch (Exception e) {
                            Log.e(TAG, "Erro item " + i, e);
                        }
                    }
                    filtrarTarefasPorDia();
                },
                error -> {
                    String msg = "Erro ao carregar tarefas.";
                    if (error.networkResponse != null) {
                        msg += " (" + error.networkResponse.statusCode + ")";
                        Log.e(TAG, msg);
                    }
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    todasTarefasDoPet.clear();
                    filtrarTarefasPorDia();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Authorization", "Bearer " + token);
                return h;
            }
        };

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    /** Filtra todasTarefasDoPet pelo diaSelecionado. */
    private void filtrarTarefasPorDia() {
        listaTarefasExibidas.clear();

        if (petId <= 0) {
            txtTituloLista.setText("Nenhum pet selecionado");
            tarefaAdapter.setItens(listaTarefasExibidas);
            atualizarVisibilidadeLista();
            return;
        }

        for (Tarefa t : todasTarefasDoPet) {
            if (t.getData() != null && t.getData().equals(diaSelecionado)) {
                listaTarefasExibidas.add(t);
            }
        }

        String nomeDia = diaSelecionado.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        String nomePet = sessionManager.obterPetNome();
        if (nomePet == null || nomePet.isEmpty()) nomePet = "Pet";
        txtTituloLista.setText("Tarefas de " + nomePet + " — " + nomeDia);

        tarefaAdapter.setItens(listaTarefasExibidas);
        atualizarVisibilidadeLista();
    }

    private void atualizarVisibilidadeLista() {
        if (listaTarefasExibidas.isEmpty()) {
            recyclerTarefas.setVisibility(View.GONE);
            if (txtSemTarefas != null) txtSemTarefas.setVisibility(View.VISIBLE);
        } else {
            recyclerTarefas.setVisibility(View.VISIBLE);
            if (txtSemTarefas != null) txtSemTarefas.setVisibility(View.GONE);
        }
    }

    private void atualizarTextoMesAno() {
        if (txtMesAno != null && diaSelecionado != null) {
            String mes = diaSelecionado.getMonth()
                    .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
            mes = mes.substring(0, 1).toUpperCase() + mes.substring(1);
            txtMesAno.setText(mes + " " + diaSelecionado.getYear());
        }
    }
}