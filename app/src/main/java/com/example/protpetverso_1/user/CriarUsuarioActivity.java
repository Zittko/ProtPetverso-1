package com.example.protpetverso_1.user;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.pet.EscolhaPetActivity;
import com.example.protpetverso_1.account.LoginActivity;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.VolleySingleton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.yalantis.ucrop.UCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class CriarUsuarioActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextInputEditText edtNomeUsuario;
    private TextInputLayout ipNomeUsuario;
    private MaterialButton btnAvancar, btnVoltar;
    private ImageView imgAvatar;

    private String fotoBase64;

    /** 1) Galeria */
    private final ActivityResultLauncher<String> selecionarFoto =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                iniciarRecorte(uri);
            });

    /** 2) Retorno do uCrop */
    private final ActivityResultLauncher<Intent> recortarFoto =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri resultUri = UCrop.getOutput(result.getData());
                    if (resultUri != null) {
                        processarFotoRecortada(resultUri);
                    }
                } else if (result.getResultCode() == UCrop.RESULT_ERROR && result.getData() != null) {
                    Throwable cropError = UCrop.getError(result.getData());
                    Toast.makeText(this,
                            "Erro no recorte: " + (cropError != null ? cropError.getMessage() : ""),
                            Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.criar_usuario_layout);

        sessionManager = new SessionManager(this);

        edtNomeUsuario = findViewById(R.id.edtNomeUsuario);
        ipNomeUsuario = findViewById(R.id.ipNomeUsuario);
        btnAvancar = findViewById(R.id.btnAvancar);
        btnVoltar = findViewById(R.id.btnVoltar);
        imgAvatar = findViewById(R.id.imgAvatar);

        btnVoltar.setOnClickListener(v -> finish());

        if (imgAvatar != null) {
            imgAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgAvatar.setOnClickListener(v -> selecionarFoto.launch("image/*"));
        }

        btnAvancar.setOnClickListener(v -> {
            ipNomeUsuario.setError(null);

            String apelido = String.valueOf(edtNomeUsuario.getText()).trim();
            if (apelido.isEmpty()) {
                ipNomeUsuario.setError("Digite um nome de usuário");
                return;
            }

            // Envia apelido + foto (se escolhida)
            atualizarPerfil(apelido, fotoBase64);
        });
    }

    private void iniciarRecorte(Uri origem) {
        Uri destino = Uri.fromFile(new File(getCacheDir(), "crop_criar_usuario.jpg"));

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(80);
        options.setToolbarTitle("Ajustar foto");
        options.setFreeStyleCropEnabled(false);

        Intent intent = UCrop.of(origem, destino)
                .withAspectRatio(1, 1)
                .withMaxResultSize(800, 800)
                .withOptions(options)
                .getIntent(this);

        recortarFoto.launch(intent);
    }

    private void processarFotoRecortada(Uri uri) {
        try {
            InputStream input = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            if (input != null) input.close();

            if (bitmap == null) {
                Toast.makeText(this, "Não foi possível ler a foto", Toast.LENGTH_SHORT).show();
                return;
            }

            fotoBase64 = bitmapParaBase64(bitmap);
            imgAvatar.setImageBitmap(bitmap);
            imgAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao processar foto", Toast.LENGTH_SHORT).show();
        }
    }

    private String bitmapParaBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
    }

    /**
     * PUT /api/usuarios/atualizarPerfil
     * Header: Authorization Bearer token
     */
    private void atualizarPerfil(String apelido, String fotoBase64) {
        btnAvancar.setEnabled(false);

        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
            btnAvancar.setEnabled(true);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        try {
            AtualizarPerfilRequest dto = new AtualizarPerfilRequest(apelido, fotoBase64);
            JSONObject body = dto.toJsonObject();

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    ApiConfig.URL_ATUALIZAR_PERFIL,
                    body,
                    response -> {
                        sessionManager.salvarApelido(apelido);
                        Toast.makeText(this, "Perfil atualizado!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CriarUsuarioActivity.this, EscolhaPetActivity.class));
                        finish();
                    },
                    error -> {
                        btnAvancar.setEnabled(true);
                        tratarErroHttp(error);
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