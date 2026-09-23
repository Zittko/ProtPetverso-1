package com.example.protpetverso_1.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.VolleySingleton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.protpetverso_1.user.CadastroRequest;
import com.example.protpetverso_1.user.CadastroResponse;
import com.example.protpetverso_1.user.CriarUsuarioActivity;

/**
 * Tela de Cadastro.
 * Envia POST /api/usuarios/cadastrar
 * Salva token + dados na SessionManager e segue para CriarUsuarioActivity.
 */
public class SignUpActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    TextInputEditText edtNome, edtCriarEmail, edtTelefone, edtCriarSenha, edtSenha;
    TextInputLayout ipEdtNome, ipEdtCriarEmail, ipEdtTelefone, ipEdtCriarSenha, ipEdtSenha;

    ScrollView signupScroll;
    ImageButton btnVoltar;
    Button btnCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup_layout);

        sessionManager = new SessionManager(this);

        edtNome = findViewById(R.id.edtNome);
        edtCriarEmail = findViewById(R.id.edtCriarEmail);
        edtTelefone = findViewById(R.id.edtTelefone);
        edtCriarSenha = findViewById(R.id.edtCriarSenha);
        edtSenha = findViewById(R.id.edtSenha);

        ipEdtNome = findViewById(R.id.ipEdtNome);
        ipEdtCriarEmail = findViewById(R.id.ipEdtCriarEmail);
        ipEdtTelefone = findViewById(R.id.ipEdtTelefone);
        ipEdtCriarSenha = findViewById(R.id.ipEdtCriarSenha);
        ipEdtSenha = findViewById(R.id.ipEdtSenha);

        signupScroll = findViewById(R.id.signupScroll);
        btnVoltar = findViewById(R.id.btnVoltar);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        ViewCompat.setOnApplyWindowInsetsListener(signupScroll, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = Math.max(systemBars.bottom, ime.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottom);
            return insets;
        });

        btnCadastrar.setOnClickListener(v -> {
            ipEdtNome.setError(null);
            ipEdtCriarEmail.setError(null);
            ipEdtTelefone.setError(null);
            ipEdtCriarSenha.setError(null);
            ipEdtSenha.setError(null);

            String nome = String.valueOf(edtNome.getText()).trim();
            String email = String.valueOf(edtCriarEmail.getText()).trim();
            String telefone = String.valueOf(edtTelefone.getText()).trim();
            String senha = String.valueOf(edtCriarSenha.getText()).trim();
            String confirma = String.valueOf(edtSenha.getText()).trim();

            boolean valido = true;

            if (nome.isEmpty()) {
                ipEdtNome.setError("Digite um nome");
                valido = false;
            }

            if (email.isEmpty()) {
                ipEdtCriarEmail.setError("Digite um email");
                valido = false;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                ipEdtCriarEmail.setError("E-mail inválido");
                valido = false;
            }

            if (telefone.isEmpty()) {
                ipEdtTelefone.setError("Digite um telefone");
                valido = false;
            }

            if (senha.isEmpty()) {
                ipEdtCriarSenha.setError("Digite uma senha");
                valido = false;
            } else if (senha.length() < 6) {
                ipEdtCriarSenha.setError("A senha deve ter pelo menos 6 caracteres");
                valido = false;
            }

            if (confirma.isEmpty()) {
                ipEdtSenha.setError("Confirme a senha");
                valido = false;
            } else if (!senha.equals(confirma)) {
                ipEdtSenha.setError("As senhas não coincidem");
                valido = false;
            }

            // Só envia se tudo estiver válido
            if (valido) {
                executarCadastro(nome, email, telefone, senha);
            }
        });

        btnVoltar.setOnClickListener(v -> finish());

        edtSenha.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) scrollToView(ipEdtSenha);
        });

        edtSenha.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String senhaCriada = String.valueOf(edtCriarSenha.getText()).trim();
                String confirma = String.valueOf(edtSenha.getText()).trim();

                if (!confirma.isEmpty() && !senhaCriada.equals(confirma)) {
                    ipEdtSenha.setError("A senha digitada deve ser igual à senha criada acima");
                } else {
                    ipEdtSenha.setError(null);
                }
            }
        });
    }

    private void scrollToView(View target) {
        signupScroll.postDelayed(() -> {
            int[] location = new int[2];
            target.getLocationInWindow(location);
            int offset = 400;
            signupScroll.smoothScrollTo(0, location[1] - offset);
        }, 350);
    }

    /**
     * POST /api/usuarios/cadastrar
     */
    private void executarCadastro(String nome, String email, String telefone, String senha) {
        btnCadastrar.setEnabled(false);

        try {
            CadastroRequest requestDto = new CadastroRequest(nome, email, telefone, senha);
            JSONObject body = requestDto.toJsonObject();

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.URL_CADASTRAR,
                    body,
                    responseJson -> {
                        try {
                            CadastroResponse responseDto = CadastroResponse.fromJsonObject(responseJson);

                            // Salva token + dados do usuário
                            sessionManager.salvarSessao(
                                    responseDto.getToken(),
                                    responseDto.getIdUsuario(),
                                    responseDto.getNome(),
                                    responseDto.getEmail()
                            );

                            // Salva telefone localmente (GET perfil ainda não devolve telefone)
                            sessionManager.salvarTelefone(telefone);

                            Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(SignUpActivity.this, CriarUsuarioActivity.class));
                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            btnCadastrar.setEnabled(true);
                            Toast.makeText(this, "Erro ao processar resposta da API", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        btnCadastrar.setEnabled(true);
                        tratarErroHttp(error);
                    }
            );

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (JSONException e) {
            btnCadastrar.setEnabled(true);
            e.printStackTrace();
            Toast.makeText(this, "Erro ao montar os dados de cadastro", Toast.LENGTH_SHORT).show();
        }
    }

    private void tratarErroHttp(com.android.volley.VolleyError error) {
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
                case 409:
                    Toast.makeText(this, "Conflito: este e-mail já pode estar cadastrado.", Toast.LENGTH_LONG).show();
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