package com.example.protpetverso_1.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.MenuActivity;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.VolleySingleton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Tela de Login (após a Splash).
 * Fluxo de sucesso:
 * 1) POST login → salva sessão
 * 2) GET meusPets → grava 1º pet no SessionManager (se houver)
 * 3) irParaMenu() uma única vez
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN";

    private SessionManager sessionManager;

    private TextInputEditText edtUser, edtSenha;
    private Button btnEntrar;
    private TextInputLayout ipEdtUser, ipEdtSenha;
    private TextView txtForgotPassword, txtCadastrar;
    private ScrollView loginScroll;

    private void scrollToView(View target) {
        if (loginScroll == null || target == null) return;
        loginScroll.postDelayed(() -> {
            int[] location = new int[2];
            target.getLocationInWindow(location);
            int offset = 400;
            loginScroll.smoothScrollTo(0, location[1] - offset);
        }, 350);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        sessionManager = new SessionManager(this);

        // Já logado → Menu uma vez
        if (sessionManager.isLogado()) {
            irParaMenu();
            return;
        }

        setContentView(R.layout.login_layout);

        loginScroll = findViewById(R.id.loginScroll);
        ipEdtUser = findViewById(R.id.ipEdtUser);
        ipEdtSenha = findViewById(R.id.ipEdtSenha);
        edtUser = findViewById(R.id.edtUser);
        edtSenha = findViewById(R.id.edtSenha);
        txtCadastrar = findViewById(R.id.txtCadastrar);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        btnEntrar = findViewById(R.id.btnEntrar);

        if (loginScroll != null) {
            ViewCompat.setOnApplyWindowInsetsListener(loginScroll, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
                int bottom = Math.max(systemBars.bottom, ime.bottom);
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottom);
                return insets;
            });
        }

        if (btnEntrar != null) {
            btnEntrar.setOnClickListener(v -> {
                if (ipEdtUser != null) ipEdtUser.setError(null);
                if (ipEdtSenha != null) ipEdtSenha.setError(null);

                String email = edtUser != null ? String.valueOf(edtUser.getText()).trim() : "";
                String senha = edtSenha != null ? String.valueOf(edtSenha.getText()).trim() : "";

                boolean valido = true;

                if (email.isEmpty()) {
                    if (ipEdtUser != null) ipEdtUser.setError("Digite o e-mail");
                    valido = false;
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (ipEdtUser != null) ipEdtUser.setError("E-mail inválido");
                    valido = false;
                }

                if (senha.isEmpty()) {
                    if (ipEdtSenha != null) ipEdtSenha.setError("Digite a senha");
                    valido = false;
                }

                // Só dispara a API — NÃO abre o Menu aqui
                if (valido) {
                    executarLogin(email, senha);
                }
            });
        }

        if (txtForgotPassword != null) {
            txtForgotPassword.setOnClickListener(v ->
                    startActivity(new Intent(LoginActivity.this, RecuperarSenhaActivity.class)));
        }

        if (edtSenha != null) {
            edtSenha.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && ipEdtSenha != null) scrollToView(ipEdtSenha);
            });
        }

        if (txtCadastrar != null) {
            txtCadastrar.setOnClickListener(v -> {
                if (edtUser != null) edtUser.setText("");
                if (edtSenha != null) edtSenha.setText("");
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            });
        }

        if (edtUser != null) {
            edtUser.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (ipEdtUser != null) ipEdtUser.setError(null);
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (edtSenha != null) {
            edtSenha.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (ipEdtSenha != null) ipEdtSenha.setError(null);
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    /**
     * POST login → salva sessão → busca pets → irParaMenu() uma vez.
     */
    private void executarLogin(String email, String senha) {
        if (btnEntrar != null) btnEntrar.setEnabled(false);

        try {
            LoginRequest requestDto = new LoginRequest(email, senha);
            JSONObject body = requestDto.toJsonObject();

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.URL_LOGIN,
                    body,
                    responseJson -> {
                        try {
                            LoginResponse responseDto = LoginResponse.fromJsonObject(responseJson);

                            sessionManager.salvarSessao(
                                    responseDto.getToken(),
                                    responseDto.getIdUsuario(),
                                    responseDto.getNome(),
                                    responseDto.getEmail()
                            );

                            Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

                            // NÃO abre Menu aqui — só depois dos pets
                            carregarMeusPetsESeguir(responseDto.getToken());

                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (btnEntrar != null) btnEntrar.setEnabled(true);
                            Toast.makeText(this, "Erro ao processar resposta da API", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        if (btnEntrar != null) btnEntrar.setEnabled(true);
                        tratarErroHttp(error);
                    }
            );

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (JSONException e) {
            if (btnEntrar != null) btnEntrar.setEnabled(true);
            e.printStackTrace();
            Toast.makeText(this, "Erro ao montar os dados de login", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * GET meusPets → grava o primeiro pet → irParaMenu().
     * Em erro de rede também segue para o Menu (uma vez).
     */
    private void carregarMeusPetsESeguir(String token) {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                ApiConfig.URL_MEUS_PETS,
                null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            JSONObject p = response.getJSONObject(0);
                            long id = p.optLong("idPet", -1);
                            if (id <= 0) id = p.optLong("id", -1);

                            String nome = p.optString("nome", "");
                            String raca = p.optString("raca", "");
                            String especie = p.optString("especie", "");
                            String peso = String.valueOf(p.optDouble("peso", 0));
                            String sexo = p.optString("sexo", "");
                            String porte = p.optString("porte", "");
                            String data = p.optString("dataDeNascimento", "");

                            if (id > 0) {
                                sessionManager.salvarPet(
                                        id, nome, raca, especie, peso, sexo, porte, data
                                );
                                Log.d(TAG, "Pet restaurado: id=" + id);
                            }
                        } else {
                            Log.d(TAG, "Usuário sem pets");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    irParaMenu();
                },
                error -> {
                    Log.e(TAG, "Erro meusPets", error);
                    irParaMenu();
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

    /** Única entrada no app após login / sessão já existente. */
    private void irParaMenu() {
        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void tratarErroHttp(VolleyError error) {
        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            String mensagem = "Erro na requisição.";

            try {
                String body = new String(
                        error.networkResponse.data,
                        java.nio.charset.StandardCharsets.UTF_8
                );
                JSONObject jsonError = new JSONObject(body);
                mensagem = jsonError.optString("mensagem", mensagem);
            } catch (Exception e) {
                e.printStackTrace();
            }

            switch (statusCode) {
                case 400:
                    Toast.makeText(this, "Dados inválidos: " + mensagem, Toast.LENGTH_LONG).show();
                    break;
                case 401:
                    Toast.makeText(this, "E-mail ou senha incorretos.", Toast.LENGTH_LONG).show();
                    break;
                case 500:
                    Toast.makeText(this, "Erro interno no servidor.", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(this, "Erro (" + statusCode + "): " + mensagem, Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            Toast.makeText(this, "Sem conexão com o servidor.", Toast.LENGTH_LONG).show();
        }
    }
}