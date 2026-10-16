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

public class AgendaFragment extends Fragment {

    private static final String TAG = "AGENDA";
    private static final int DIAS_COLAPSADO = 7;

    private SessionManager sessionManager;
    private long petId = -1;
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

    private final List<Tarefa> todasTarefasDoPet = new ArrayList<>();
    private final List<Tarefa> listaTarefasExibidas = new ArrayList<>();

    private boolean podeAtualizarUi() {
        return isAdded() && getContext() != null && getView() != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agenda, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context ctx = getContext();
        if (ctx == null) return;

        sessionManager = new SessionManager(ctx);
        petId = sessionManager.obterPetId();

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
                public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View v,
                                           @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
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

        tarefaAdapter = new TarefaAdapter(ctx, listaTarefasExibidas);
        tarefaAdapter.setOnTarefaClickListener(this::concluirTarefaNaApi);
        if (recyclerTarefas != null) {
            recyclerTarefas.setLayoutManager(new LinearLayoutManager(ctx));
            recyclerTarefas.setAdapter(tarefaAdapter);
        }

        if (btnNovaTarefa != null) {
            btnNovaTarefa.setOnClickListener(v -> {
                if (petId <= 0) {
                    Toast.makeText(ctx, "Selecione um pet na Home ou em Pets.", Toast.LENGTH_SHORT).show();
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

        carregarTarefas();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sessionManager == null) return;
        long novo = sessionManager.obterPetId();
        if (novo != petId) petId = novo;
        carregarTarefas();
    }

    /** ~5 chips visíveis; lista tem 7 dias e rola na horizontal. */
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
        filtrarTarefasPorDia();
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
        filtrarTarefasPorDia();
    }

    private void carregarTarefas() {
        if (!podeAtualizarUi()) return;

        if (petId <= 0) {
            todasTarefasDoPet.clear();
            filtrarTarefasPorDia();
            if (txtTituloLista != null) txtTituloLista.setText("Nenhum pet selecionado");
            return;
        }

        if (AppDataCache.get().temTarefas(petId)) {
            todasTarefasDoPet.clear();
            todasTarefasDoPet.addAll(AppDataCache.get().getTarefas(petId));
            filtrarTarefasPorDia();
        }

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
                            Log.e(TAG, "parse", e);
                        }
                    }
                    AppDataCache.get().setTarefas(petId, todas);
                    if (!podeAtualizarUi()) return;
                    if (sessionManager.obterPetId() != petId) return;
                    todasTarefasDoPet.clear();
                    todasTarefasDoPet.addAll(todas);
                    filtrarTarefasPorDia();
                },
                error -> Log.e(TAG, "GET tarefas", error)
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

    private void filtrarTarefasPorDia() {
        listaTarefasExibidas.clear();
        for (Tarefa t : todasTarefasDoPet) {
            if (t.getData() != null && t.getData().equals(diaSelecionado)) {
                listaTarefasExibidas.add(t);
            }
        }
        if (tarefaAdapter != null) tarefaAdapter.setItens(listaTarefasExibidas);

        boolean vazio = listaTarefasExibidas.isEmpty();
        if (recyclerTarefas != null) {
            recyclerTarefas.setVisibility(vazio ? View.GONE : View.VISIBLE);
        }
        if (txtSemTarefas != null) {
            txtSemTarefas.setVisibility(vazio ? View.VISIBLE : View.GONE);
        }
        if (txtTituloLista != null && petId > 0) {
            txtTituloLista.setText("Tarefas do Dia");
        }
    }

    private void concluirTarefaNaApi(Tarefa tarefa, int position) {
        if (tarefa == null || tarefa.getId() <= 0 || tarefa.isConcluida()) return;
        if (sessionManager == null) return;

        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) return;

        Context ctx = getContext();
        if (ctx == null) return;

        String url = ApiConfig.URL_CONCLUIR_TAREFA + tarefa.getId() + "/concluir";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PATCH,
                url,
                null,
                response -> {
                    try {
                        Tarefa atualizada = Tarefa.fromJson(response);
                        for (int i = 0; i < todasTarefasDoPet.size(); i++) {
                            if (todasTarefasDoPet.get(i).getId() == atualizada.getId()) {
                                todasTarefasDoPet.set(i, atualizada);
                                break;
                            }
                        }
                        AppDataCache.get().setTarefas(petId, todasTarefasDoPet);
                        if (podeAtualizarUi()) filtrarTarefasPorDia();
                    } catch (Exception e) {
                        Log.e(TAG, "parse concluir", e);
                    }
                },
                error -> {
                    if (podeAtualizarUi()) {
                        Toast.makeText(ctx, "Erro ao concluir tarefa.", Toast.LENGTH_SHORT).show();
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
}