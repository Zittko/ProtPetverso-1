package com.example.protpetverso_1;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CriarUsuarioActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextInputEditText edtNomeUsuario;
    private TextInputLayout ipNomeUsuario;
    private MaterialButton btnAvancar, btnVoltar;
    private ImageView imgAvatar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.criar_usuario_layout); // seu layout

        sessionManager = new SessionManager(this);

        edtNomeUsuario = findViewById(R.id.edtNomeUsuario);
        ipNomeUsuario = findViewById(R.id.ipNomeUsuario);
        btnAvancar = findViewById(R.id.btnAvancar);
        btnVoltar = findViewById(R.id.btnVoltar);
        imgAvatar = findViewById(R.id.imgAvatar);

        btnVoltar.setOnClickListener(v -> finish());

        btnAvancar.setOnClickListener(v -> {
            ipNomeUsuario.setError(null);

            String apelido = String.valueOf(edtNomeUsuario.getText()).trim();

            if (apelido.isEmpty()) {
                ipNomeUsuario.setError("Digite um nome de usuário");
                return;
            }

            // Por enquanto sem foto (null). Depois podemos enviar Base64.
            atualizarPerfil(apelido, null);
        });
    }

    /**
     * Envia apelido (e foto opcional) para a API.
     * Usa o token salvo no SessionManager no header Authorization.
     */
    private void atualizarPerfil(String apelido, String fotoBase64) {
        btnAvancar.setEnabled(false);

        // 1) Recupera o token salvo no cadastro/login
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
            btnAvancar.setEnabled(true);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        try {
            // 2) Monta o JSON
            AtualizarPerfilRequest dto = new AtualizarPerfilRequest(apelido, fotoBase64);
            JSONObject body = dto.toJsonObject();

            // 3) Cria a requisição PUT
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    ApiConfig.URL_ATUALIZAR_PERFIL,
                    body,
                    response -> {
                        // Sucesso
                        Toast.makeText(this, "Perfil atualizado!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CriarUsuarioActivity.this, EscolhaPetActivity.class));
                        finish();
                    },
                    error -> {
                        // Erro
                        btnAvancar.setEnabled(true);
                        tratarErroHttp(error);
                    }
            ) {
                // 4) Aqui vai o HEADER com o token
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            // 5) Envia a requisição
            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (JSONException e) {
            btnAvancar.setEnabled(true);
            e.printStackTrace();
            Toast.makeText(this, "Erro ao montar os dados.", Toast.LENGTH_SHORT).show();
        }
    }

    private void tratarErroHttp(VolleyError error) {
        if (error.networkResponse != null) {
            int status = error.networkResponse.statusCode;
            String mensagem = "Erro na requisição.";

            try {
                String body = new String(error.networkResponse.data, java.nio.charset.StandardCharsets.UTF_8);
                JSONObject json = new JSONObject(body);
                mensagem = json.optString("mensagem", mensagem);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (status == 401) {
                Toast.makeText(this, "Token inválido. Faça login novamente.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Erro (" + status + "): " + mensagem, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Sem conexão com o servidor.", Toast.LENGTH_LONG).show();
        }
    }
}