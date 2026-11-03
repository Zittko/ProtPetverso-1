package com.example.protpetverso_1.pet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.MenuActivity;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.SolicitarVinculoRequest;
import com.example.protpetverso_1.VolleySingleton;
import com.example.protpetverso_1.account.LoginActivity;
import com.example.protpetverso_1.data.AppDataCache;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Tela pós-login / drawer: cadastrar pet novo OU solicitar vínculo por código.
 * POST /api/pets/solicitarVinculo
 *
 * Modo OBRIGATÓRIO (usuário sem nenhum pet): a tela inicial fica bloqueada. O usuário só
 * avança ao cadastrar um pet ou quando o tutor principal aceitar a solicitação; enquanto a
 * tela está aberta, consulta GET /api/pets/meusPets (só devolve vínculos ACEITO) e libera
 * o acesso assim que o pet aparecer.
 */
public class EscolhaPetActivity extends AppCompatActivity {

    private static final String TAG = "VINCULO";

    /** true = usuário sem pet; não pode ir para a tela inicial. */
    public static final String EXTRA_OBRIGATORIO = "ESCOLHA_PET_OBRIGATORIO";

    /** Intervalo entre consultas enquanto espera o tutor aceitar. */
    private static final long INTERVALO_VERIFICACAO_MS = 3000L;
    private static final String TAG_VERIFICACAO = "verificacao_vinculo";

    private SessionManager sessionManager;
    private TextInputEditText edtCodigoVinculo;

    private boolean obrigatorio;
    private boolean ativa;
    private boolean consultando;
    private boolean liberado;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable verificar = this::verificarVinculoAceito;

    public static Intent criarIntent(Context context, boolean obrigatorio) {
        Intent intent = new Intent(context, EscolhaPetActivity.class);
        intent.putExtra(EXTRA_OBRIGATORIO, obrigatorio);
        return intent;
    }

    /** Sem pet salvo no aparelho e sem pets no cache → precisa passar pela escolha. */
    public static boolean precisaEscolherPet(SessionManager session) {
        return !session.temPetSalvo() && AppDataCache.get().getMeusPets().isEmpty();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.escolha_pet_layout);

        sessionManager = new SessionManager(this);
        obrigatorio = getIntent().getBooleanExtra(EXTRA_OBRIGATORIO, false);

        // Voltar do sistema: no modo obrigatório não existe tela anterior permitida
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (obrigatorio) {
                    moveTaskToBack(true);
                } else {
                    finish();
                }
            }
        });

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
            if (obrigatorio) {
                // Sem pet não há para onde voltar; permite trocar de conta
                btnVoltar.setText("Sair da conta");
                btnVoltar.setOnClickListener(v -> sairDaConta());
            } else {
                btnVoltar.setOnClickListener(v -> finish());
            }
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

    @Override
    protected void onResume() {
        super.onResume();
        ativa = true;
        if (obrigatorio) {
            handler.removeCallbacks(verificar);
            handler.post(verificar); // confere já ao abrir/voltar para a tela
        }
    }

    @Override
    protected void onPause() {
        ativa = false;
        handler.removeCallbacks(verificar);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(verificar);
        VolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG_VERIFICACAO);
        super.onDestroy();
    }

    private void agendarVerificacao() {
        if (obrigatorio && ativa && !liberado && !isFinishing()) {
            handler.postDelayed(verificar, INTERVALO_VERIFICACAO_MS);
        }
    }

    /**
     * Consulta meusPets (só vínculos ACEITO). Lista vazia = ainda sem pet → tenta de novo.
     * Com pelo menos um pet → libera o acesso à tela inicial na hora.
     */
    private void verificarVinculoAceito() {
        if (!obrigatorio || liberado || isFinishing() || consultando) return;

        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) return;

        consultando = true;
        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                ApiConfig.URL_MEUS_PETS,
                null,
                response -> {
                    consultando = false;
                    if (response.length() > 0 && ativa && !liberado) {
                        liberarAcesso(response.optJSONObject(0));
                    } else {
                        agendarVerificacao();
                    }
                },
                error -> {
                    consultando = false;
                    Log.w(TAG, "Falha ao verificar vínculo (tentando de novo)", error);
                    agendarVerificacao();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Authorization", "Bearer " + token);
                return h;
            }
        };
        req.setTag(TAG_VERIFICACAO);
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void liberarAcesso(JSONObject pet) {
        liberado = true;
        handler.removeCallbacks(verificar);

        if (pet != null) {
            long id = pet.optLong("idPet", pet.optLong("id", -1));
            if (id > 0) {
                sessionManager.salvarPet(id,
                        pet.optString("nome", "Pet"),
                        pet.optString("papel", ""));
            }
        }
        Toast.makeText(this, "Vínculo aceito! Bem-vindo(a).", Toast.LENGTH_SHORT).show();
        irParaHome();
    }

    private void sairDaConta() {
        handler.removeCallbacks(verificar);
        AppDataCache.get().init(this);
        AppDataCache.get().limparTudo(true);
        sessionManager.limparSessao();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
                            // Já fica consultando: libera assim que o tutor aceitar
                            if (obrigatorio) {
                                handler.removeCallbacks(verificar);
                                handler.postDelayed(verificar, INTERVALO_VERIFICACAO_MS);
                            }
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