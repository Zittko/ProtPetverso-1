package com.example.protpetverso_1.schedule;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.VolleySingleton;
import com.example.protpetverso_1.data.AppDataCache;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Agenda do usuário.
 *
 * Lista tarefas de TODOS os pets com vínculo ACEITO no dia selecionado:
 *   GET /api/tarefas?data=yyyy-MM-dd
 *
 * Conclusão:
 *   PATCH /api/tarefas/{idTarefa}/concluir
 *
 * Edição (clique no card, só se não concluída — API exige DONO):
 *   abre CriarTarefaActivity em modo edição → PUT /api/tarefas/{idTarefa}
 *
 * Criação (FAB):
 *   exige pet selecionado no SessionManager
 *   POST /api/pets/{id}/tarefas
 */
public class AgendaFragment extends Fragment {

    private static final String TAG = "AGENDA";
    private static final int DIAS_COLAPSADO = 7;

    private SessionManager sessionManager;
    private LocalDate diaSelecionado = LocalDate.now();
    private boolean calendarioExpandido = false;

    private MaterialCardView cardCalendario;
    private View conteudoCalendario;
    private RecyclerView recyclerDias, recyclerMes, recyclerTarefas;
    private TextView txtMesAno, txtTituloLista, txtSemTarefas;
    private ImageView btnExpandirCalendario, btnMesAnterior, btnMesProximo;
    private FloatingActionButton btnNovaTarefa;

    private DiaAdapter diaAdapter;
    private DiaAdapter mesAdapter;
    private TarefaAdapter tarefaAdapter;

    /** Tarefas do dia (todos os pets). */
    private final List<Tarefa> tarefasDoDia = new ArrayList<>();
    private final List<Tarefa> listaTarefasExibidas = new ArrayList<>();

    private boolean podeAtualizarUi() {
        return isAdded() && getContext() != null && getView() != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agenda, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context ctx = getContext();
        if (ctx == null) return;

        sessionManager = new SessionManager(ctx);

        cardCalendario = view.findViewById(R.id.cardCalendario);
        conteudoCalendario = view.findViewById(R.id.conteudoCalendario);
        recyclerDias = view.findViewById(R.id.recyclerDias);
        recyclerMes = view.findViewById(R.id.recyclerMes);
        recyclerTarefas = view.findViewById(R.id.recyclerTarefas);
        txtMesAno = view.findViewById(R.id.txtMesAno);
        txtTituloLista = view.findViewById(R.id.txtTituloLista);
        txtSemTarefas = view.findViewById(R.id.txtSemTarefas);
        btnExpandirCalendario = view.findViewById(R.id.btnExpandirCalendario);
        btnMesAnterior = view.findViewById(R.id.btnMesAnterior);
        btnMesProximo = view.findViewById(R.id.btnMesProximo);
        btnNovaTarefa = view.findViewById(R.id.btnNovaTarefa);

        // ----- Semana (horizontal, ~5 chips visíveis) -----
        diaAdapter = new DiaAdapter();
        diaAdapter.setModoGrade(false);
        diaAdapter.setOnDiaClickListener(this::onDiaEscolhido);
        if (recyclerDias != null) {
            recyclerDias.setLayoutManager(
                    new LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false));
            recyclerDias.setAdapter(diaAdapter);
            recyclerDias.setClipToPadding(false);
            recyclerDias.post(this::aplicarLarguraCincoDias);
        }

        // ----- Mês (grade 7 colunas) -----
        mesAdapter = new DiaAdapter();
        mesAdapter.setModoGrade(true);
        mesAdapter.setOnDiaClickListener(this::onDiaEscolhido);
        if (recyclerMes != null) {
            recyclerMes.setLayoutManager(new GridLayoutManager(ctx, 7));
            recyclerMes.setAdapter(mesAdapter);
            recyclerMes.setVisibility(View.GONE);
            final int s = (int) (3 * getResources().getDisplayMetrics().density);
            recyclerMes.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull android.graphics.Rect outRect,
                                           @NonNull View v,
                                           @NonNull RecyclerView parent,
                                           @NonNull RecyclerView.State state) {
                    outRect.set(s, s, s, s);
                }
            });
        }

        aplicarEstiloCalendarioColapsado();
        montarSemana();
        atualizarTituloMes();

        if (btnExpandirCalendario != null) {
            btnExpandirCalendario.setOnClickListener(v -> alternarCalendario());
        }
        if (btnMesAnterior != null) {
            btnMesAnterior.setOnClickListener(v -> mudarMes(-1));
        }
        if (btnMesProximo != null) {
            btnMesProximo.setOnClickListener(v -> mudarMes(1));
        }
        if (txtMesAno != null) {
            txtMesAno.setOnClickListener(v -> {
                if (!calendarioExpandido) alternarCalendario();
            });
        }

        // ----- Lista de tarefas -----
        tarefaAdapter = new TarefaAdapter(ctx, listaTarefasExibidas);
        tarefaAdapter.setOnTarefaClickListener(this::concluirTarefaNaApi);
        tarefaAdapter.setOnEditarTarefaListener(this::abrirEdicaoTarefa);
        if (recyclerTarefas != null) {
            recyclerTarefas.setLayoutManager(new LinearLayoutManager(ctx));
            recyclerTarefas.setAdapter(tarefaAdapter);
        }

        // ----- FAB: criar tarefa no pet selecionado -----
        if (btnNovaTarefa != null) {
            btnNovaTarefa.setOnClickListener(v -> {
                long petId = sessionManager.obterPetId();
                if (petId <= 0) {
                    Toast.makeText(ctx,
                            "Selecione um pet na Home ou em Pets.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (diaSelecionado.isBefore(LocalDate.now())) {
                    Toast.makeText(ctx,
                            "Não é possível criar tarefa em datas passadas.\nEscolha hoje ou um dia futuro.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Intent i = new Intent(ctx, CriarTarefaActivity.class);
                i.putExtra("PET_ID", petId);
                i.putExtra("DATA", diaSelecionado.toString());
                startActivity(i);
            });
        }

        carregarTarefasDoDia();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recarrega ao voltar da criação/edição
        carregarTarefasDoDia();
    }

    // ==================== CALENDÁRIO ====================

    /** ~5 chips visíveis; a lista tem 7 dias e rola na horizontal. */
    private void aplicarLarguraCincoDias() {
        if (recyclerDias == null || diaAdapter == null) return;

        int larguraLista = recyclerDias.getWidth()
                - recyclerDias.getPaddingLeft()
                - recyclerDias.getPaddingRight();
        if (larguraLista <= 0) return;

        float density = getResources().getDisplayMetrics().density;
        int margemPorItem = (int) (3 * density * 2);
        int larguraItem = (larguraLista / 5) - margemPorItem;
        int min = (int) (48 * density);
        if (larguraItem < min) larguraItem = min;

        diaAdapter.setLarguraItemSemana(larguraItem);
    }

    private void mudarMes(int delta) {
        YearMonth atual = YearMonth.from(diaSelecionado);
        YearMonth novo = atual.plusMonths(delta);
        int dia = Math.min(diaSelecionado.getDayOfMonth(), novo.lengthOfMonth());
        diaSelecionado = novo.atDay(dia);

        if (calendarioExpandido) {
            montarMes();
        } else {
            montarSemana();
            if (recyclerDias != null) {
                recyclerDias.post(this::aplicarLarguraCincoDias);
                recyclerDias.scrollToPosition(0);
            }
        }
        if (diaAdapter != null) diaAdapter.setDiaSelecionado(diaSelecionado);
        if (mesAdapter != null) mesAdapter.setDiaSelecionado(diaSelecionado);
        atualizarTituloMes();
        carregarTarefasDoDia();
    }

    private void alternarCalendario() {
        calendarioExpandido = !calendarioExpandido;

        if (recyclerDias != null) {
            recyclerDias.setVisibility(calendarioExpandido ? View.GONE : View.VISIBLE);
        }
        if (recyclerMes != null) {
            recyclerMes.setVisibility(calendarioExpandido ? View.VISIBLE : View.GONE);
        }
        if (btnExpandirCalendario != null) {
            btnExpandirCalendario.setRotation(calendarioExpandido ? 180f : 0f);
        }

        if (calendarioExpandido) {
            montarMes();
            aplicarEstiloCalendarioExpandido();
        } else {
            montarSemana();
            aplicarEstiloCalendarioColapsado();
            if (recyclerDias != null) {
                recyclerDias.post(this::aplicarLarguraCincoDias);
            }
        }
        atualizarTituloMes();
    }

    private void aplicarEstiloCalendarioColapsado() {
        if (cardCalendario != null) {
            cardCalendario.setCardBackgroundColor(Color.TRANSPARENT);
            cardCalendario.setStrokeWidth(0);
        }
        if (conteudoCalendario != null) {
            int p = (int) (4 * getResources().getDisplayMetrics().density);
            conteudoCalendario.setPadding(p, p, p, p);
        }
    }

    private void aplicarEstiloCalendarioExpandido() {
        if (cardCalendario != null) {
            cardCalendario.setCardBackgroundColor(Color.WHITE);
            int stroke = (int) (1.5f * getResources().getDisplayMetrics().density);
            cardCalendario.setStrokeWidth(stroke);
            cardCalendario.setStrokeColor(Color.BLACK);
        }
        if (conteudoCalendario != null) {
            int p = (int) (12 * getResources().getDisplayMetrics().density);
            conteudoCalendario.setPadding(p, p, p, p);
        }
    }

    private void montarSemana() {
        List<LocalDate> dias = new ArrayList<>();
        LocalDate inicio = diaSelecionado.with(DayOfWeek.MONDAY);
        for (int i = 0; i < DIAS_COLAPSADO; i++) {
            dias.add(inicio.plusDays(i));
        }
        if (diaAdapter != null) {
            diaAdapter.setModoGrade(false);
            diaAdapter.setDias(dias);
            diaAdapter.setDiaSelecionado(diaSelecionado);
        }
    }

    private void montarMes() {
        YearMonth ym = YearMonth.from(diaSelecionado);
        LocalDate primeiro = ym.atDay(1);
        LocalDate inicioGrade = primeiro.with(DayOfWeek.MONDAY);

        List<LocalDate> dias = new ArrayList<>();
        LocalDate d = inicioGrade;
        for (int i = 0; i < 42; i++) {
            dias.add(d);
            d = d.plusDays(1);
        }
        if (mesAdapter != null) {
            mesAdapter.setModoGrade(true);
            mesAdapter.setMesReferencia(ym);
            mesAdapter.setDias(dias);
            mesAdapter.setDiaSelecionado(diaSelecionado);
        }
    }

    private void atualizarTituloMes() {
        if (txtMesAno == null) return;
        Locale pt = new Locale("pt", "BR");
        String diaSemana = diaSelecionado.getDayOfWeek().getDisplayName(TextStyle.FULL, pt);
        String mes = diaSelecionado.getMonth().getDisplayName(TextStyle.FULL, pt);
        txtMesAno.setText(diaSemana + ", " + diaSelecionado.getDayOfMonth()
                + " de " + mes + " " + diaSelecionado.getYear());
    }

    private void onDiaEscolhido(LocalDate data) {
        if (data == null) return;
        diaSelecionado = data;
        if (diaAdapter != null) diaAdapter.setDiaSelecionado(data);
        if (mesAdapter != null) mesAdapter.setDiaSelecionado(data);
        atualizarTituloMes();
        carregarTarefasDoDia();
    }

    // ==================== TAREFAS (TODOS OS PETS DO DIA) ====================

    /**
     * GET /api/tarefas?data=yyyy-MM-dd
     * Retorna tarefas de todos os pets com vínculo ACEITO no dia escolhido.
     */

    /**
     * GET /api/tarefas?data=yyyy-MM-dd
     * Retorna tarefas de todos os pets com vínculo ACEITO no dia escolhido.
     */
    private void carregarTarefasDoDia() {
        if (!podeAtualizarUi()) return;

        // Cache-first (disco/memória)
        if (AppDataCache.get().temTarefasDoDia(diaSelecionado)) {
            List<Tarefa> cached = AppDataCache.get().getTarefasDoDia(diaSelecionado);
            aplicarListaTarefasDoDia(cached);
        }

        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            if (!AppDataCache.get().temTarefasDoDia(diaSelecionado)) {
                atualizarListaVazia("Faça login para ver as tarefas.");
            }
            return;
        }

        Context ctx = getContext();
        if (ctx == null) return;

        String url = ApiConfig.URL_TAREFAS_POR_DIA + "?data=" + diaSelecionado.toString();
        Log.d(TAG, "GET " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    List<Tarefa> lista = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            lista.add(Tarefa.fromJson(response.getJSONObject(i)));
                        } catch (Exception e) {
                            Log.e(TAG, "parse tarefa", e);
                        }
                    }
                    if (!podeAtualizarUi()) return;

                    AppDataCache.get().setTarefasDoDia(diaSelecionado, lista);
                    aplicarListaTarefasDoDia(lista);
                },
                error -> {
                    Log.e(TAG, "GET tarefas por dia", error);
                    if (podeAtualizarUi()) {
                        // Mantém cache se a rede falhar
                        if (!AppDataCache.get().temTarefasDoDia(diaSelecionado)) {
                            String msg = "Erro ao carregar tarefas.";
                            if (error.networkResponse != null) {
                                msg += " (" + error.networkResponse.statusCode + ")";
                            }
                            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
                        }
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

    private void aplicarListaTarefasDoDia(List<Tarefa> lista) {
        if (lista == null) lista = new ArrayList<>();
        tarefasDoDia.clear();
        tarefasDoDia.addAll(lista);
        listaTarefasExibidas.clear();
        listaTarefasExibidas.addAll(lista);
        if (tarefaAdapter != null) {
            tarefaAdapter.setItens(listaTarefasExibidas);
        }
        boolean vazio = lista.isEmpty();
        if (recyclerTarefas != null) {
            recyclerTarefas.setVisibility(vazio ? View.GONE : View.VISIBLE);
        }
        if (txtSemTarefas != null) {
            txtSemTarefas.setVisibility(vazio ? View.VISIBLE : View.GONE);
            if (vazio) {
                txtSemTarefas.setText(
                        "Nenhuma tarefa neste dia.\nToque no + para criar uma nova.");
            }
        }
        if (txtTituloLista != null) {
            txtTituloLista.setText("Tarefas do Dia");
        }
    }

    private void atualizarListaVazia(String mensagem) {
        tarefasDoDia.clear();
        listaTarefasExibidas.clear();
        if (tarefaAdapter != null) tarefaAdapter.setItens(listaTarefasExibidas);
        if (recyclerTarefas != null) recyclerTarefas.setVisibility(View.GONE);
        if (txtSemTarefas != null) {
            txtSemTarefas.setVisibility(View.VISIBLE);
            txtSemTarefas.setText(mensagem);
        }
    }

    // ==================== CONCLUIR ====================

    /**
     * PATCH /api/tarefas/{idTarefa}/concluir
     * Qualquer usuário com vínculo ACEITO pode concluir.
     */
    private void concluirTarefaNaApi(Tarefa tarefa, int position) {
        if (tarefa == null || tarefa.isConcluida()) return;

        long id = tarefa.getIdTarefa() > 0 ? tarefa.getIdTarefa() : tarefa.getId();
        if (id <= 0) return;
        if (sessionManager == null) return;

        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) return;

        Context ctx = getContext();
        if (ctx == null) return;

        String url = ApiConfig.URL_CONCLUIR_TAREFA + id + "/concluir";
        Log.d(TAG, "PATCH " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PATCH,
                url,
                null,
                response -> {
                    // Recarrega o dia inteiro para refletir executor e data de conclusão
                    if (podeAtualizarUi()) {
                        carregarTarefasDoDia();
                    }
                },
                error -> {
                    if (podeAtualizarUi()) {
                        String msg = "Erro ao concluir tarefa.";
                        if (error.networkResponse != null) {
                            int s = error.networkResponse.statusCode;
                            if (s == 409 || s == 400) {
                                msg = "Esta tarefa já foi concluída.";
                            } else {
                                msg += " (" + s + ")";
                            }
                        }
                        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Authorization", "Bearer " + token);
                h.put("Content-Type", "application/json");
                return h;
            }
        };

        VolleySingleton.getInstance(ctx.getApplicationContext()).addToRequestQueue(request);
    }

    // ==================== EDITAR ====================

    /**
     * Abre CriarTarefaActivity em modo edição.
     * A API só permite PUT se o usuário for DONO e a tarefa não estiver concluída.
     */
    private void abrirEdicaoTarefa(Tarefa tarefa) {
        if (tarefa == null || tarefa.isConcluida()) return;

        long id = tarefa.getIdTarefa() > 0 ? tarefa.getIdTarefa() : tarefa.getId();
        if (id <= 0) return;

        Context ctx = getContext();
        if (ctx == null) return;

        Intent i = new Intent(ctx, CriarTarefaActivity.class);
        i.putExtra("MODO_EDICAO", true);
        i.putExtra("ID_TAREFA", id);
        i.putExtra("PET_ID", tarefa.getIdPet() > 0 ? tarefa.getIdPet() : sessionManager.obterPetId());
        i.putExtra("TITULO", tarefa.getTitulo());
        i.putExtra("DESCRICAO", tarefa.getDesc());
        if (tarefa.getData() != null) {
            i.putExtra("DATA", tarefa.getData().toString());
        } else {
            i.putExtra("DATA", diaSelecionado.toString());
        }
        if (tarefa.getHorario() != null) {
            i.putExtra("HORA", tarefa.getHorario().toString());
        }
        startActivity(i);
    }
}