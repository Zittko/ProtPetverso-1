package com.example.protpetverso_1.pet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.MenuActivity;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.SolicitarVinculoRequest;
import com.example.protpetverso_1.VolleySingleton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EscolhaPetActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.escolha_pet_layout);
        // Liga o card "Cadastrar novo pet"
        MaterialCardView cardCadastrarPet = findViewById(R.id.cardCadastrarPet);

        sessionManager = new SessionManager(this);

        cardCadastrarPet.setOnClickListener(v -> {
            // Abre a tela de formulário do pet
            startActivity(new Intent(EscolhaPetActivity.this, PetSignUpActivity.class));
        });
    }

    private void solicitarVinculo(String codigo) {
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Sessão expirada. Faça login.", Toast.LENGTH_SHORT).show();
            return;
        }

        codigo = codigo.trim();
        if (codigo.isEmpty()) {
            Toast.makeText(this, "Digite o código do pet", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject body = new SolicitarVinculoRequest(codigo).toJsonObject();

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.URL_SOLICITAR_VINCULO,
                    body,
                    response -> {
                        boolean sucesso = response.optBoolean("sucesso", true);
                        String mensagem = response.optString("mensagem", "Solicitação enviada.");
                        String status = response.optString("statusDeVinculo", "");

                        // Se vierem dados da solicitação / pet
                        JSONObject dados = response.optJSONObject("dadosSolicitacaoDTO");
                        if (dados != null) {
                            // opcional: guardar idSolicitacao, nomePet, etc.
                        }

                        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();

                        // Se a API já aprovar na hora:
                        if (status.equalsIgnoreCase("APROVADO")
                                || status.equalsIgnoreCase("ACEITO")
                                || status.equalsIgnoreCase("ATIVO")) {
                            // Depois: buscar lista de pets e ir para Home
                            irParaHome();
                        }
                        // Se ficar pendente, só avisa e permanece na tela
                    },
                    error -> {
                        String msg = "Erro ao vincular.";
                        if (error.networkResponse != null) {
                            msg += " Código: " + error.networkResponse.statusCode;
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao montar solicitação.", Toast.LENGTH_SHORT).show();
        }
    }

    private void irParaHome() {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}