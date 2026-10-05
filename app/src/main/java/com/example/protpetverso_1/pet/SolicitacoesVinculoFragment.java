package com.example.protpetverso_1.pet;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.VolleySingleton;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Solicitações de vínculo (Conta A — tutor principal).
 * GET  /api/pets/listarSolicitacoes
 * POST /api/pets/processarSolicitacao  (ACEITO / RECUSADO)
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
        atualizarTituloToolbar("Solicitações de Vínculo");

        txtVazio = view.findViewById(R.id.txtVazio);
        recycler = view.findViewById(R.id.recyclerSolicitacoes);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Se o XML usa NestedScrollView, deixe nestedScrollingEnabled=false no RecyclerView
        recycler.setNestedScrollingEnabled(false);

        adapter = new SolicitacaoVinculoAdapter(this);
        recycler.setAdapter(adapter);

        carregarSolicitacoes();
    }

    @Override
    public void onResume() {
        super.onResume();
        atualizarTituloToolbar("Solicitações de Vínculo");
        if (sessionManager != null) {
            carregarSolicitacoes();
        }
    }

    /** GET lista de pendentes do usuário logado (dono dos pets). */
    private void carregarSolicitacoes() {
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Sessão expirada.", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                ApiConfig.URL_LISTAR_SOLICITACOES,
                null,
                response -> {
                    List<SolicitacaoVinculo> lista = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject o = response.getJSONObject(i);
                            long id = o.optLong("idSolicitacao", -1);
                            String solicitante = o.optString("nomeSolicitante", "—");
                            String pet = o.optString("nomePet", "—");
                            String data = o.optString("dataDeCriacao", "");
                            if (id > 0) {
                                lista.add(new SolicitacaoVinculo(id, solicitante, pet, data));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Erro item " + i, e);
                        }
                    }
                    adapter.setItens(lista);
                    boolean vazio = lista.isEmpty();
                    if (txtVazio != null) {
                        txtVazio.setVisibility(vazio ? View.VISIBLE : View.GONE);
                    }
                    if (recycler != null) {
                        recycler.setVisibility(vazio ? View.GONE : View.VISIBLE);
                    }
                },
                error -> {
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