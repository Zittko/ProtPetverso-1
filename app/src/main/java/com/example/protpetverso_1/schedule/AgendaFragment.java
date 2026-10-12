package com.example.protpetverso_1.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.VolleySingleton;
import com.example.protpetverso_1.data.AppDataCache;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Agenda do pet selecionado.
 * Cache (AppDataCache) → lista na hora → GET /pets/{id}/tarefas em background.
 */
public class AgendaFragment extends Fragment {

    private static final String TAG = "AGENDA";

    private RecyclerView recyclerDias, recyclerTarefas, recyclerMes;
    private TextView txtTituloLista, txtSemTarefas, txtMesAno, txtNomeMesExpandido;
    private View containerCalendarioMes;
    private ImageView btnExpandirCalendario;
    private ImageButton btnMesAnterior, btnMesProximo;
    private FloatingActionButton btnNovaTarefa;

    private DiaAdapter diaAdapter;
    private MesAdapter mesAdapter;
    private TarefaAdapter tarefaAdapter;

    private final List<LocalDate> listaDias = new ArrayList<>();
    private final List<Tarefa> listaTarefasExibidas = new ArrayList<>();
    private final List<Tarefa> todasTarefasDoPet = new ArrayList<>();

    private LocalDate diaSelecionado;
    private LocalDate mesExibido;
    private boolean calendarioExpandido = false;

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
        recyclerMes = view.findViewById(R.id.recyclerMes);
        txtTituloLista = view.findViewById(R.id.txtTituloLista);
        txtSemTarefas = view.findViewById(R.id.txtSemTarefas);
        txtMesAno = view.findViewById(R.id.txtMesAno);
        txtNomeMesExpandido = view.findViewById(R.id.txtNomeMesExpandido);
        containerCalendarioMes = view.findViewById(R.id.containerCalendarioMes);
        btnExpandirCalendario = view.findViewById(R.id.btnExpandirCalendario);
        btnMesAnterior = view.findViewById(R.id.btnMesAnterior);
        btnMesProximo = view.findViewById(R.id.btnMesProximo);
        btnNovaTarefa = view.findViewById(R.id.btnNovaTarefa);

        diaSelecionado = LocalDate.now();
        mesExibido = diaSelecionado.withDayOfMonth(1);

        recyclerTarefas.setLayoutManager(new LinearLayoutManager(requireContext()));
        tarefaAdapter = new TarefaAdapter(requireContext(), listaTarefasExibidas);
        tarefaAdapter.setOnTarefaClickListener((tarefa, position) ->
                Toast.makeText(requireContext(),
                        "Conclusão de tarefa: aguardando endpoint na API.",
                        Toast.LENGTH_SHORT).show());
        recyclerTarefas.setAdapter(tarefaAdapter);

        configurarCalendario();

        if (btnNovaTarefa != null) {
            btnNovaTarefa.setOnClickListener(v -> {
                if (petId <= 0) {
                    Toast.makeText(requireContext(), "Selecione um pet antes.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent i = new Intent(requireContext(), CriarTarefaActivity.class);
                i.putExtra("PET_ID", petId);
                i.putExtra("DATA_PADRAO", diaSelecionado.toString());
                startActivity(i);
            });
        }

        if (btnExpandirCalendario != null) {
            btnExpandirCalendario.setOnClickListener(v -> alternarCalendario());
        }
        if (btnMesAnterior != null) {
            btnMesAnterior.setOnClickListener(v -> mudarMes(-1));
        }
        if (btnMesProximo != null) {
            btnMesProximo.setOnClickListener(v -> mudarMes(1));
        }

        if (containerCalendarioMes != null) {
            containerCalendarioMes.setVisibility(View.GONE);
        }
        if (recyclerDias != null) {
            recyclerDias.setVisibility(View.VISIBLE);
        }

        atualizarIconeSeta();
        atualizarTextoMesAno();
        atualizarBotoesMes();

        // Cache + API
        aplicarCacheTarefas();
        carregarTarefasDoPet();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sessionManager != null) {
            long novoId = sessionManager.obterPetId();
            if (novoId != petId) {
                petId = novoId;
                aplicarCacheTarefas();
            }
            carregarTarefasDoPet();
        }
    }

    /** Preenche a lista a partir do AppDataCache (se existir). */
    private void aplicarCacheTarefas() {
        petId = sessionManager.obterPetId();
        if (petId > 0 && AppDataCache.get().temTarefas(petId)) {
            todasTarefasDoPet.clear();
            todasTarefasDoPet.addAll(AppDataCache.get().getTarefas(petId));
            filtrarTarefasPorDia();
        }
    }

    // ==================== CALENDÁRIO ====================

    private void configurarCalendario() {
        montarSemanaDoDia(diaSelecionado);
        if (recyclerDias != null) {
            recyclerDias.setLayoutManager(
                    new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
            diaAdapter = new DiaAdapter(requireContext(), listaDias, diaSelecionado, this::onDiaEscolhido);
            recyclerDias.setAdapter(diaAdapter);
        }

        if (recyclerMes != null) {
            recyclerMes.setLayoutManager(new GridLayoutManager(requireContext(), 7));
            mesAdapter = new MesAdapter(this::onDiaEscolhido);
            recyclerMes.setAdapter(mesAdapter);
            mesAdapter.setMes(mesExibido, diaSelecionado);
        }
    }

    private void onDiaEscolhido(LocalDate data) {
        if (data.isBefore(LocalDate.now())) {
            Toast.makeText(requireContext(), "Não é possível selecionar datas passadas.", Toast.LENGTH_SHORT).show();
            return;
        }
        diaSelecionado = data;
        mesExibido = data.withDayOfMonth(1);

        montarSemanaDoDia(data);
        if (diaAdapter != null) diaAdapter.setDiaSelecionado(diaSelecionado);
        if (mesAdapter != null) mesAdapter.setMes(mesExibido, diaSelecionado);

        atualizarTextoMesAno();
        atualizarBotoesMes();
        filtrarTarefasPorDia();
    }

    private void montarSemanaDoDia(LocalDate ref) {
        listaDias.clear();
        LocalDate segunda = ref.minusDays(ref.getDayOfWeek().getValue() - 1L);
        for (int i = 0; i < 7; i++) {
            listaDias.add(segunda.plusDays(i));
        }
        if (diaAdapter != null) {
            diaAdapter.setListaDias(listaDias);
            diaAdapter.setDiaSelecionado(diaSelecionado);
        }
    }

    private void alternarCalendario() {
        calendarioExpandido = !calendarioExpandido;

        if (containerCalendarioMes != null) {
            containerCalendarioMes.setVisibility(calendarioExpandido ? View.VISIBLE : View.GONE);
        }
        if (recyclerDias != null) {
            recyclerDias.setVisibility(calendarioExpandido ? View.GONE : View.VISIBLE);
        }

        if (calendarioExpandido && mesAdapter != null) {
            mesExibido = diaSelecionado.withDayOfMonth(1);
            mesAdapter.setMes(mesExibido, diaSelecionado);
            atualizarNomeMesExpandido();
            atualizarBotoesMes();
        }
        atualizarIconeSeta();
    }

    private void mudarMes(int delta) {
        LocalDate candidato = mesExibido.plusMonths(delta).withDayOfMonth(1);
        LocalDate inicioMesAtual = LocalDate.now().withDayOfMonth(1);

        if (candidato.isBefore(inicioMesAtual)) {
            Toast.makeText(requireContext(), "Não há datas disponíveis neste mês.", Toast.LENGTH_SHORT).show();
            return;
        }

        mesExibido = candidato;
        if (mesAdapter != null) {
            mesAdapter.setMes(mesExibido, diaSelecionado);
        }
        atualizarNomeMesExpandido();
        atualizarBotoesMes();
    }

    private void atualizarBotoesMes() {
        if (btnMesAnterior == null) return;
        LocalDate inicioMesAtual = LocalDate.now().withDayOfMonth(1);
        boolean podeVoltar = mesExibido.isAfter(inicioMesAtual);
        btnMesAnterior.setEnabled(podeVoltar);
        btnMesAnterior.setAlpha(podeVoltar ? 1f : 0.3f);
    }

    private void atualizarNomeMesExpandido() {
        if (txtNomeMesExpandido == null || mesExibido == null) return;
        String mes = capitalizar(mesExibido.getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("pt", "BR")));
        txtNomeMesExpandido.setText(mes + " " + mesExibido.getYear());
    }

    private void atualizarIconeSeta() {
        if (btnExpandirCalendario == null) return;
        btnExpandirCalendario.setRotation(calendarioExpandido ? 180f : 0f);
    }

    private void atualizarTextoMesAno() {
        if (txtMesAno == null || diaSelecionado == null) return;
        String nomeDia = capitalizar(diaSelecionado.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("pt", "BR")));
        String mes = capitalizar(diaSelecionado.getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("pt", "BR")));
        txtMesAno.setText(nomeDia + ", " + diaSelecionado.getDayOfMonth()
                + " de " + mes + " " + diaSelecionado.getYear());
        atualizarNomeMesExpandido();
    }

    private String capitalizar(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // ==================== TAREFAS (API + CACHE) ====================

    private void carregarTarefasDoPet() {
        petId = sessionManager.obterPetId();
        if (petId <= 0) {
            if (txtTituloLista != null) {
                txtTituloLista.setText("Nenhum pet selecionado");
            }
            todasTarefasDoPet.clear();
            filtrarTarefasPorDia();
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
                            todasTarefasDoPet.add(Tarefa.fromJson(response.getJSONObject(i)));
                        } catch (Exception e) {
                            Log.e(TAG, "item " + i, e);
                        }
                    }
                    // Grava no cache global
                    AppDataCache.get().setTarefas(petId, todasTarefasDoPet);
                    filtrarTarefasPorDia();
                },
                error -> {
                    // Se já mostrou cache, não precisa toast
                    if (!AppDataCache.get().temTarefas(petId)) {
                        String msg = "Erro ao carregar tarefas";
                        if (error.networkResponse != null) {
                            msg += " (" + error.networkResponse.statusCode + ")";
                        }
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        todasTarefasDoPet.clear();
                        filtrarTarefasPorDia();
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

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private void filtrarTarefasPorDia() {
        listaTarefasExibidas.clear();

        if (petId <= 0) {
            if (txtTituloLista != null) {
                txtTituloLista.setText("Nenhum pet selecionado");
            }
            tarefaAdapter.setItens(listaTarefasExibidas);
            atualizarVisibilidadeLista();
            return;
        }

        if (diaSelecionado != null) {
            for (Tarefa t : todasTarefasDoPet) {
                if (t.getData() != null && t.getData().equals(diaSelecionado)) {
                    listaTarefasExibidas.add(t);
                }
            }
        }

        if (txtTituloLista != null) {
            txtTituloLista.setText("Tarefas do Dia");
        }

        tarefaAdapter.setItens(listaTarefasExibidas);
        atualizarVisibilidadeLista();
    }

    private void atualizarVisibilidadeLista() {
        boolean vazio = listaTarefasExibidas.isEmpty();
        if (recyclerTarefas != null) {
            recyclerTarefas.setVisibility(vazio ? View.GONE : View.VISIBLE);
        }
        if (txtSemTarefas != null) {
            txtSemTarefas.setVisibility(vazio ? View.VISIBLE : View.GONE);
        }
    }
}