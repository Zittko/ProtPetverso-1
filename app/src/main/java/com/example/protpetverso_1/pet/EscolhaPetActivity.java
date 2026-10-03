package com.example.protpetverso_1.pet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Tela pós-login / drawer: cadastrar pet novo OU solicitar vínculo por código.
 * POST /api/pets/solicitarVinculo
 */
public class EscolhaPetActivity extends AppCompatActivity {

    private static final String TAG = "VINCULO";

    private SessionManager sessionManager;
    private TextInputEditText edtCodigoVinculo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.escolha_pet_layout);

        sessionManager = new SessionManager(this);

        // --- Cadastrar novo pet ---
        MaterialCardView cardCadastrarPet = findViewById(R.id.cardCadastrarPet);
        if (cardCadastrarPet != null) {
            cardCadastrarPet.setOnClickListener(v ->
                    startActivity(new Intent(this, PetSignUpActivity.class)));
        } else {
            Log.w(TAG, "ID cardCadastrarPet não encontrado no XML");
        }

        // --- Campo do código (confira o id no escolha_pet_layout.xml) ---
        edtCodigoVinculo = findViewById(R.id.edtCodigoVinculo);
        if (edtCodigoVinculo == null) {
            Log.w(TAG, "ID edtCodigoVinculo não encontrado no XML");
        }

        // --- Botão e/ou card de vincular ---
        MaterialButton btnVincular = findViewById(R.id.btnVincular);
        MaterialCardView cardVincular = findViewById(R.id.cardVincularPet);
        MaterialButton btnVoltar = findViewById(R.id.btnVoltar);

        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> finish());
        }

        Runnable acaoVincular = () -> {
            String codigo = "";
            if (edtCodigoVinculo != null && edtCodigoVinculo.getText() != null) {
                codigo = edtCodigoVinculo.getText().toString().trim();
            }
            solicitarVinculo(codigo);
        };

        boolean temAcao = false;

        if (btnVincular != null) {
            btnVincular.setOnClickListener(v -> acaoVincular.run());
            temAcao = true;
        } else {
            Log.w(TAG, "ID btnVincular não encontrado no XML");
        }

        if (cardVincular != null) {
            cardVincular.setOnClickListener(v -> acaoVincular.run());
            temAcao = true;
        } else {
            Log.w(TAG, "ID cardVincularPet não encontrado no XML");
        }

        if (!temAcao) {
            Toast.makeText(this,
                    "Layout sem botão/card de vincular. Verifique os IDs no XML.",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Envia o código para a API.
     * Em geral a resposta vem com status PENDENTE até a Conta A aceitar.
     */
    private void solicitarVinculo(String codigo) {
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Sessão expirada. Faça login.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (codigo == null || codigo.isEmpty()) {
            Toast.makeText(this, "Digite o código do pet", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject body = new SolicitarVinculoRequest(codigo).toJsonObject();
            Log.d(TAG, "POST body: " + body);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.URL_SOLICITAR_VINCULO,
                    body,
                    response -> {
                        Log.d(TAG, "Resposta: " + response.toString());

                        boolean sucesso = response.optBoolean("sucesso", true);
                        String mensagem = response.optString("mensagem", "Solicitação enviada.");
                        String status = response.optString("statusDeVinculo", "");
                        if (status.isEmpty() && response.has("statusDeVinculo")) {
                            status = String.valueOf(response.opt("statusDeVinculo"));
                        }

                        if (!sucesso) {
                            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (status.equalsIgnoreCase("ACEITO")) {
                            Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
                            irParaHome();
                        } else if (status.equalsIgnoreCase("PENDENTE")) {
                            Toast.makeText(this,
                                    "Aguardando o tutor principal aceitar.",
                                    Toast.LENGTH_LONG).show();
                        } else if (status.equalsIgnoreCase("RECUSADO")) {
                            Toast.makeText(this, "Solicitação recusada.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Status vazio: ainda mostra a mensagem da API
                            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> {
                        String msg = "Erro ao vincular.";
                        if (error.networkResponse != null) {
                            msg += " Código: " + error.networkResponse.statusCode;
                            try {
                                String err = new String(
                                        error.networkResponse.data,
                                        java.nio.charset.StandardCharsets.UTF_8);
                                Log.e(TAG, "Erro body: " + err);
                            } catch (Exception ignored) {
                            }
                        } else {
                            Log.e(TAG, "Sem resposta do servidor", error);
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
            Log.e(TAG, "Erro ao montar solicitação", e);
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