package com.example.protpetverso_1.pet;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.protpetverso_1.data.AppDataCache;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.VolleySingleton;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Solicitações (tutor principal): pedidos de vínculo + pedidos de alteração do suporte.
 * GET   /api/solicitacoes/pendentes  → { vinculos: [...], alteracoes: [...] }
 * POST  /api/pets/processarSolicitacao  (vínculo: ACEITO / RECUSADO)
 * PATCH /api/solicitacoes/{id}/aprovar | /recusar  (alterações de tarefa/vacina)
 *
 * Aberta pela MenuActivity (drawer) — usa toolbar e bottom nav do Menu.
 */
public class SolicitacoesVinculoFragment extends Fragment
        implements SolicitacaoVinculoAdapter.Listener {

    private static final String TAG = "VINCULO_SOLICIT";

    private SessionManager sessionManager;
    private SolicitacaoVinculoAdapter adapter;
    private TextView txtVazio;
    private RecyclerView recycler;
    private RecyclerView recyclerAlteracoes;
    private SolicitacaoAlteracaoAdapter adapterAlteracoes;
    private TextView txtTituloAlteracoes;
    private TextView txtTituloVinculos;

    public SolicitacoesVinculoFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_solicitacoes_vinculo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        atualizarTituloToolbar("Solicitações");

        txtVazio = view.findViewById(R.id.txtVazio);
        recycler = view.findViewById(R.id.recyclerSolicitacoes);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Se o XML usa NestedScrollView, deixe nestedScrollingEnabled=false no RecyclerView
        recycler.setNestedScrollingEnabled(false);

        adapter = new SolicitacaoVinculoAdapter(this);
        recycler.setAdapter(adapter);

        txtTituloAlteracoes = view.findViewById(R.id.txtTituloAlteracoes);
        txtTituloVinculos = view.findViewById(R.id.txtTituloVinculos);
        recyclerAlteracoes = view.findViewById(R.id.recyclerAlteracoes);
        recyclerAlteracoes.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAlteracoes.setNestedScrollingEnabled(false);
        adapterAlteracoes = new SolicitacaoAlteracaoAdapter(new SolicitacaoAlteracaoAdapter.Listener() {
            @Override
            public void onAprovar(SolicitacaoAlteracao item) {
                confirmarAprovacao(item);
            }

            @Override
            public void onRecusar(SolicitacaoAlteracao item) {
                pedirMotivoERecusar(item);
            }
        });
        recyclerAlteracoes.setAdapter(adapterAlteracoes);

        carregarSolicitacoes();
    }

    @Override
    public void onResume() {
        super.onResume();
        atualizarTituloToolbar("Solicitações");
        if (sessionManager != null) {
            carregarSolicitacoes();
        }
    }

    /** GET pendentes do usuário logado (dono): vínculos + alterações, numa única chamada. */
    private void carregarSolicitacoes() {
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Sessão expirada.", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.URL_SOLICITACOES_PENDENTES,
                null,
                response -> {
                    if (!isAdded()) return;
                    List<SolicitacaoVinculo> vinculos = new ArrayList<>();
                    JSONArray arrV = response.optJSONArray("vinculos");
                    if (arrV != null) {
                        for (int i = 0; i < arrV.length(); i++) {
                            try {
                                JSONObject o = arrV.getJSONObject(i);
                                long id = o.optLong("idSolicitacao", -1);
                                if (id > 0) {
                                    vinculos.add(new SolicitacaoVinculo(id,
                                            o.optString("nomeSolicitante", "—"),
                                            o.optString("nomePet", "—"),
                                            o.optString("dataDeCriacao", "")));
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Erro vínculo " + i, e);
                            }
                        }
                    }

                    List<SolicitacaoAlteracao> alteracoes = new ArrayList<>();
                    JSONArray arrA = response.optJSONArray("alteracoes");
                    if (arrA != null) {
                        for (int i = 0; i < arrA.length(); i++) {
                            try {
                                SolicitacaoAlteracao a = SolicitacaoAlteracao.fromJson(arrA.getJSONObject(i));
                                if (a != null) alteracoes.add(a);
                            } catch (Exception e) {
                                Log.e(TAG, "Erro alteração " + i, e);
                            }
                        }
                    }
                    mostrar(vinculos, alteracoes);
                },
                error -> {
                    if (!isAdded()) return;
                    String msg = "Erro ao carregar solicitações.";
                    if (error.networkResponse != null) {
                        msg += " (" + error.networkResponse.statusCode + ")";
                    }
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
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

    private void mostrar(List<SolicitacaoVinculo> vinculos, List<SolicitacaoAlteracao> alteracoes) {
        adapter.setItens(vinculos);
        adapterAlteracoes.setItens(alteracoes);

        boolean temV = !vinculos.isEmpty();
        boolean temA = !alteracoes.isEmpty();
        recycler.setVisibility(temV ? View.VISIBLE : View.GONE);
        txtTituloVinculos.setVisibility(temV && temA ? View.VISIBLE : View.GONE);
        recyclerAlteracoes.setVisibility(temA ? View.VISIBLE : View.GONE);
        txtTituloAlteracoes.setVisibility(temA ? View.VISIBLE : View.GONE);
        txtVazio.setVisibility(!temV && !temA ? View.VISIBLE : View.GONE);
    }

    // ---------------------------------------------------------------- alterações

    private void confirmarAprovacao(SolicitacaoAlteracao item) {
        new AlertDialog.Builder(requireContext(), R.style.DialogPetVerso)
                .setTitle("Aprovar alteração?")
                .setMessage("Os dados serão atualizados com o que " + item.nomeSolicitante
                        + " propôs.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aprovar", (d, w) -> responderAlteracao(item, true, null))
                .show();
    }

    private void pedirMotivoERecusar(SolicitacaoAlteracao item) {
        EditText campo = new EditText(requireContext());
        campo.setHint("Motivo (opcional)");
        campo.setMaxLines(3);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        android.widget.FrameLayout box = new android.widget.FrameLayout(requireContext());
        box.setPadding(pad, pad / 2, pad, 0);
        box.addView(campo);

        new AlertDialog.Builder(requireContext(), R.style.DialogPetVerso)
                .setTitle("Recusar alteração?")
                .setView(box)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Recusar", (d, w) -> {
                    String motivo = campo.getText().toString().trim();
                    if (motivo.length() > 255) motivo = motivo.substring(0, 255);
                    responderAlteracao(item, false, motivo);
                })
                .show();
    }

    /** PATCH /api/solicitacoes/{id}/aprovar | /recusar */
    private void responderAlteracao(SolicitacaoAlteracao item, boolean aprovar, String motivo) {
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) return;

        JSONObject body = new JSONObject();
        try {
            if (!aprovar && motivo != null && !motivo.isEmpty()) body.put("motivo", motivo);
        } catch (Exception ignored) {
        }

        String url = ApiConfig.URL_SOLICITACOES + item.idSolicitacao
                + (aprovar ? "/aprovar" : "/recusar");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PATCH, url, body,
                response -> {
                    if (!isAdded()) return;
                    if (aprovar) {
                        // Os dados mudaram no servidor: descarta o que está guardado no app.
                        AppDataCache cache = AppDataCache.get();
                        if (item.ehTarefa()) {
                            cache.invalidarTarefas(item.idPet);
                            cache.invalidarTarefasDoDia();
                        } else {
                            cache.invalidarVacinas(item.idPet);
                        }
                    }
                    Toast.makeText(requireContext(),
                            aprovar ? "Alteração aprovada e aplicada." : "Alteração recusada.",
                            Toast.LENGTH_SHORT).show();
                    carregarSolicitacoes();
                },
                error -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), mensagemErro(error), Toast.LENGTH_LONG).show();
                    // 404/409: a lista está desatualizada; recarrega.
                    carregarSolicitacoes();
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
        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private String mensagemErro(VolleyError error) {
        int code = error.networkResponse != null ? error.networkResponse.statusCode : -1;
        switch (code) {
            case 404: return "Esta solicitação não existe mais (talvez o suporte tenha sido removido).";
            case 409: return "Esta solicitação já foi respondida.";
            case 403: return "Só o tutor principal pode responder.";
            case 400: return "Os dados do pedido não são mais válidos. Recuse e peça um novo.";
            default: return "Não foi possível responder agora. Tente novamente.";
        }
    }

    // ------------------------------------------------------------------ vínculos

    @Override
    public void onAceitar(SolicitacaoVinculo item) {
        processar(item.idSolicitacao, "ACEITO");
    }

    @Override
    public void onRecusar(SolicitacaoVinculo item) {
        processar(item.idSolicitacao, "RECUSADO");
    }

    /** POST /api/pets/processarSolicitacao */
    private void processar(long idSolicitacao, String novoStatus) {
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) return;

        try {
            JSONObject body = new JSONObject();
            body.put("idSolicitacao", idSolicitacao);
            body.put("novoStatus", novoStatus);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.URL_PROCESSAR_SOLICITACAO,
                    body,
                    response -> {
                        String msg = response.optString("mensagem", "Solicitação processada.");
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        carregarSolicitacoes();
                    },
                    error -> Toast.makeText(requireContext(),
                            "Erro ao processar solicitação.", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> h = new HashMap<>();
                    h.put("Authorization", "Bearer " + token);
                    h.put("Content-Type", "application/json");
                    return h;
                }
            };

            VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao montar processar", e);
        }
    }

    private void atualizarTituloToolbar(String titulo) {
        if (getActivity() == null) return;
        MaterialToolbar toolbar = getActivity().findViewById(R.id.toolbarMenu);
        if (toolbar != null) {
            toolbar.setTitle(titulo);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Volta o título padrão ao sair (opcional)
        atualizarTituloToolbar("Tela Inicial");
    }
}