package com.example.protpetverso_1.user;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.VolleySingleton;
import com.example.protpetverso_1.data.AppDataCache;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class EditarUsuarioActivity extends AppCompatActivity {

    private TextInputEditText edtNome, edtApelido, edtEmail, edtTelefone, edtSenhaAtual, edtNovaSenha;
    private MaterialButton btnSalvar;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_usuario);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);

        session = new SessionManager(this);
        MaterialToolbar toolbar = findViewById(R.id.toolbarEditarUsuario);
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            return insets;
        });
        toolbar.setNavigationOnClickListener(v -> finish());

        edtNome = findViewById(R.id.edtNome);
        edtApelido = findViewById(R.id.edtApelido);
        edtEmail = findViewById(R.id.edtEmail);
        edtTelefone = findViewById(R.id.edtTelefone);
        edtSenhaAtual = findViewById(R.id.edtSenhaAtual);
        edtNovaSenha = findViewById(R.id.edtNovaSenha);
        btnSalvar = findViewById(R.id.btnSalvarUsuario);

        edtNome.setText(getIntent().getStringExtra("NOME"));
        edtEmail.setText(getIntent().getStringExtra("EMAIL"));
        String tel = getIntent().getStringExtra("TELEFONE");
        edtTelefone.setText("—".equals(tel) ? "" : tel);
        String apelido = getIntent().getStringExtra("APELIDO");
        if ("usuário".equalsIgnoreCase(apelido)) apelido = "";
        edtApelido.setText(apelido);

        btnSalvar.setOnClickListener(v -> salvar());
    }

    private void salvar() {
        String nome = texto(edtNome);
        String email = texto(edtEmail);
        String telefone = texto(edtTelefone);
        String apelido = texto(edtApelido);
        String senhaAtual = texto(edtSenhaAtual);
        String novaSenha = texto(edtNovaSenha);

        if (nome.isEmpty() || email.isEmpty() || !email.contains("@") || telefone.length() < 6) {
            Toast.makeText(this, "Preencha nome, e-mail válido e telefone.", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean trocarSenha = !senhaAtual.isEmpty() || !novaSenha.isEmpty();
        if (trocarSenha && (senhaAtual.isEmpty() || novaSenha.length() < 6)) {
            Toast.makeText(this, "Informe a senha atual e uma nova com pelo menos 6 caracteres.", Toast.LENGTH_LONG).show();
            return;
        }
        if (session.obterToken() == null || session.obterToken().isEmpty()) {
            Toast.makeText(this, "Sessão expirada.", Toast.LENGTH_SHORT).show();
            return;
        }
        btnSalvar.setEnabled(false);
        enviarDados(nome, email, telefone, apelido, trocarSenha, senhaAtual, novaSenha);
    }

    private void enviarDados(String nome, String email, String telefone, String apelido,
                             boolean trocarSenha, String senhaAtual, String novaSenha) {
        try {
            JSONObject body = new JSONObject();
            body.put("nome", nome);
            body.put("email", email);
            body.put("telefone", telefone);
            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.PUT, ApiConfig.URL_USUARIO_DADOS, body,
                    response -> enviarApelido(nome, email, telefone, apelido, trocarSenha, senhaAtual, novaSenha),
                    error -> falha(error, "Não foi possível salvar os dados.")
            ) {
                @Override public Map<String, String> getHeaders() { return auth(); }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(req);
        } catch (Exception e) {
            btnSalvar.setEnabled(true);
        }
    }

    private void enviarApelido(String nome, String email, String telefone, String apelido,
                               boolean trocarSenha, String senhaAtual, String novaSenha) {
        try {
            JSONObject body = new JSONObject();
            body.put("apelido", apelido);
            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.PUT, ApiConfig.URL_ATUALIZAR_PERFIL, body,
                    response -> {
                        if (trocarSenha) enviarSenha(nome, email, telefone, apelido, senhaAtual, novaSenha, response);
                        else concluir(nome, email, telefone, apelido, response);
                    },
                    error -> falha(error, "Dados salvos, mas o apelido falhou.")
            ) {
                @Override public Map<String, String> getHeaders() { return auth(); }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(req);
        } catch (Exception e) {
            btnSalvar.setEnabled(true);
        }
    }

    private void enviarSenha(String nome, String email, String telefone, String apelido,
                             String senhaAtual, String novaSenha, JSONObject perfil) {
        try {
            JSONObject body = new JSONObject();
            body.put("senhaAtual", senhaAtual);
            body.put("novaSenha", novaSenha);
            StringRequest req = new StringRequest(
                    Request.Method.PUT, ApiConfig.URL_USUARIO_SENHA,
                    response -> concluir(nome, email, telefone, apelido, perfil),
                    error -> falha(error, "Dados salvos, mas a senha não foi alterada.")
            ) {
                @Override public byte[] getBody() { return body.toString().getBytes(StandardCharsets.UTF_8); }
                @Override public String getBodyContentType() { return "application/json; charset=utf-8"; }
                @Override public Map<String, String> getHeaders() { return auth(); }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(req);
        } catch (Exception e) {
            btnSalvar.setEnabled(true);
        }
    }

    private void concluir(String nome, String email, String telefone, String apelido, JSONObject perfil) {
        session.salvarPerfil(nome, email, apelido, telefone);
        try {
            JSONObject cache = perfil != null ? perfil : new JSONObject();
            cache.put("nome", nome);
            cache.put("email", email);
            cache.put("telefone", telefone);
            cache.put("apelido", apelido);
            if (!cache.has("fotoBase64")) {
                cache.put("fotoBase64", AppDataCache.get().getUsuarioFotoBase64());
            }
            AppDataCache.get().setUsuarioPerfil(cache);
        } catch (Exception ignored) {}
        Toast.makeText(this, "Perfil atualizado.", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void falha(com.android.volley.VolleyError error, String padrao) {
        btnSalvar.setEnabled(true);
        String msg = padrao;
        if (error.networkResponse != null && error.networkResponse.data != null) {
            String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
            if (body.toLowerCase().contains("telefone")) msg = "Telefone já cadastrado.";
            else if (body.toLowerCase().contains("email") || body.toLowerCase().contains("e-mail")) msg = "E-mail já cadastrado.";
            else if (body.toLowerCase().contains("senha atual")) msg = "Senha atual incorreta.";
            else if (body.toLowerCase().contains("diferente")) msg = "A nova senha deve ser diferente da atual.";
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private Map<String, String> auth() {
        Map<String, String> h = new HashMap<>();
        h.put("Authorization", "Bearer " + session.obterToken());
        h.put("Content-Type", "application/json");
        return h;
    }

    private String texto(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}