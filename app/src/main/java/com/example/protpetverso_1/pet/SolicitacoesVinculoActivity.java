package com.example.protpetverso_1.pet;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Conta A (tutor principal): lista pedidos de vínculo e permite Aceitar/Recusar.
 * GET  /api/pets/listarSolicitacoes
 * POST /api/pets/processarSolicitacao
 */
public class SolicitacoesVinculoActivity extends AppCompatActivity
        implements SolicitacaoVinculoAdapter.Listener {

    private SessionManager sessionManager;
    private SolicitacaoVinculoAdapter adapter;
    private TextView txtVazio;
    private RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitacoes_vinculo);

        sessionManager = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarSolicitacoes);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
            // Se o XML não tiver navigationIcon, pode usar só o botão voltar do sistema
        }

        txtVazio = findViewById(R.id.txtVazio);
        recycler = findViewById(R.id.recyclerSolicitacoes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SolicitacaoVinculoAdapter(this);
        recycler.setAdapter(adapter);

        carregarSolicitacoes();
    }

    /** GET lista de pendentes do usuário logado (dono dos pets). */
    private void carregarSolicitacoes() {
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Sessão expirada.", Toast.LENGTH_SHORT).show();
            finish();
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
                            Log.e("VINCULO", "Erro item " + i, e);
                        }
                    }
                    adapter.setItens(lista);
                    txtVazio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                    recycler.setVisibility(lista.isEmpty() ? View.GONE : View.VISIBLE);
                },
                error -> {
                    String msg = "Erro ao carregar solicitações.";
                    if (error.networkResponse != null) {
                        msg += " (" + error.networkResponse.statusCode + ")";
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Authorization", "Bearer " + token);
                return h;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    @Override
    public void onAceitar(SolicitacaoVinculo item) {
        // Confirme com a API o valor exato do enum (APROVADO, ACEITO, etc.)
        processar(item.idSolicitacao, "ACEITO");
    }

    @Override
    public void onRecusar(SolicitacaoVinculo item) {
        processar(item.idSolicitacao, "RECUSADO");
    }

    /** POST processarSolicitacao */
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
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        carregarSolicitacoes(); // atualiza a lista
                    },
                    error -> Toast.makeText(this, "Erro ao processar solicitação.", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> h = new HashMap<>();
                    h.put("Authorization", "Bearer " + token);
                    h.put("Content-Type", "application/json");
                    return h;
                }
            };

            VolleySingleton.getInstance(this).addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}