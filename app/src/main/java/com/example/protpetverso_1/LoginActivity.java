package com.example.protpetverso_1;
// Define o pacote do projeto.

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
// Imports para Intent, Views e TextWatcher.

//Imports para integração com a API
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
// Imports para Edge-to-Edge e tratamento de teclado/barras do sistema.

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
// Componentes Material dos campos de texto.

public class LoginActivity extends AppCompatActivity {

    private SessionManager sessionManager;
// Tela de Login. É a Activity que aparece após a Splash.

    TextInputEditText edtUser, edtSenha;
    // Campos onde o usuário digita e-mail e senha.

    Button btnEntrar;
    // Botão de login.

    TextInputLayout ipEdtUser, ipEdtSenha;
    // Containers dos campos (usados para mostrar mensagens de erro).

    TextView txtForgotPassword, txtCadastrar;
    // Textos clicáveis: "Esqueceu a senha?" e "Cadastrar".

    ScrollView loginScroll;
    // ScrollView principal do layout. Usado para subir a tela quando o teclado abre.

    private void scrollToView(View target) {
        // Faz a tela rolar suavemente até o campo focado (principalmente a senha).
        loginScroll.postDelayed(() -> {
            int[] location = new int[2];
            target.getLocationInWindow(location);
            int offset = 400;
            loginScroll.smoothScrollTo(0, location[1] - offset);
        }, 350);
        // Conectado com: loginScroll + ipEdtSenha
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Ativa o modo edge-to-edge (conteúdo atrás das barras do sistema).

        sessionManager = new SessionManager(this);

        // Se já estiver logado, pula o login
        if (sessionManager.isLogado()) {
            startActivity(new Intent(this, MenuActivity.class)); // ou EscolhaPetActivity, conforme a regra
            finish();
            return;
        }

        setContentView(R.layout.login_layout);
        // Carrega o layout XML da tela de login.
        // Conectado com: login_layout.xml



        // Liga os componentes do XML com as variáveis Java
        loginScroll = findViewById(R.id.loginScroll);
        ipEdtUser = findViewById(R.id.ipEdtUser);
        ipEdtSenha = findViewById(R.id.ipEdtSenha);
        edtUser = findViewById(R.id.edtUser);
        edtSenha = findViewById(R.id.edtSenha);
        txtCadastrar = findViewById(R.id.txtCadastrar);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        btnEntrar = findViewById(R.id.btnEntrar);

        // Trata as barras do sistema e o teclado (IME)
        ViewCompat.setOnApplyWindowInsetsListener(loginScroll, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = Math.max(systemBars.bottom, ime.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottom);
            return insets;
        });
        // Faz o conteúdo subir quando o teclado aparece.
        // Conectado com: loginScroll

        // Ação do botão Entrar
        btnEntrar.setOnClickListener(v -> {
            ipEdtUser.setError(null);
            ipEdtSenha.setError(null);

            String email = String.valueOf(edtUser.getText()).trim();
            String senha = String.valueOf(edtSenha.getText()).trim();

            boolean valido = true;

            if (email.isEmpty()) {
                ipEdtUser.setError("Digite o e-mail");
                valido = false;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                ipEdtUser.setError("E-mail inválido");
                valido = false;
            }

            if (senha.isEmpty()) {
                ipEdtSenha.setError("Digite a senha");
                valido = false;
            }

            if (valido) {
                executarLogin(email, senha);
            }
        });

        txtForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RecuperarSenhaActivity.class));
        });

        // Quando o campo senha ganha foco, sobe a tela
        edtSenha.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) scrollToView(ipEdtSenha);
        });

        // Clique no texto "Cadastrar"
        txtCadastrar.setOnClickListener(v -> {
            edtUser.setText("");
            edtSenha.setText("");
            // Limpa os campos antes de ir para o cadastro
            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
            // Conectado com: SignUpActivity
        });

        // Limpa o erro do e-mail enquanto o usuário digita
        edtUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ipEdtUser.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Limpa o erro da senha enquanto o usuário digita
        edtSenha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ipEdtSenha.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


    }
    /**
     * Envia e-mail e senha para a API e salva a sessão se der certo.
     */
    private void executarLogin(String email, String senha) {
        btnEntrar.setEnabled(false);

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

                            // Por enquanto vai para a Home.
                            // Depois: verificar se tem pet e decidir entre EscolhaPet e Menu.
                            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            btnEntrar.setEnabled(true);
                            Toast.makeText(this, "Erro ao processar resposta da API", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        btnEntrar.setEnabled(true);
                        tratarErroHttp(error);
                    }
            );

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (JSONException e) {
            btnEntrar.setEnabled(true);
            e.printStackTrace();
            Toast.makeText(this, "Erro ao montar os dados de login", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Trata erros HTTP do login.
     */
    private void tratarErroHttp(VolleyError error) {
        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            String mensagem = "Erro na requisição.";

            try {
                String body = new String(error.networkResponse.data, java.nio.charset.StandardCharsets.UTF_8);
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