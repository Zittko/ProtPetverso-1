package com.example.protpetverso_1.user;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.VolleySingleton;
import com.google.android.material.appbar.MaterialToolbar;
import com.yalantis.ucrop.UCrop;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Perfil do Usuário
 * GET  /api/usuarios/perfil
 * PUT  /api/usuarios/atualizarPerfil  (apelido + fotoBase64)
 *
 * Foto: galeria → recorte (uCrop 1:1) → Base64 → API
 */
public class PerfilUsuarioFragment extends Fragment {

    private SessionManager sessionManager;

    private ImageView imgFotoPerfil;
    private TextView txtUsername;
    private TextView txtNome;
    private TextView txtEmail;
    private TextView txtTelefone;
    private ImageButton btnEditarPerfil;

    private String fotoBase64Atual;
    private String apelidoAtual = "";

    /** 1) Escolhe a foto na galeria */
    private final ActivityResultLauncher<String> selecionarFoto =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null || getContext() == null) return;
                iniciarRecorte(uri);
            });

    /** 2) Volta do uCrop com a foto já enquadrada */
    private final ActivityResultLauncher<android.content.Intent> recortarFoto =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri resultUri = UCrop.getOutput(result.getData());
                    if (resultUri != null) {
                        processarFotoRecortada(resultUri);
                    }
                } else if (result.getResultCode() == UCrop.RESULT_ERROR && result.getData() != null) {
                    Throwable cropError = UCrop.getError(result.getData());
                    Toast.makeText(requireContext(),
                            "Erro no recorte: " + (cropError != null ? cropError.getMessage() : ""),
                            Toast.LENGTH_SHORT).show();
                }
            });

    public PerfilUsuarioFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil_usuario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        if (getActivity() != null) {
            MaterialToolbar toolbar = getActivity().findViewById(R.id.toolbarMenu);
            if (toolbar != null) {
                toolbar.setTitle("Perfil do Usuário");
            }
        }

        ligarComponentes(view);
        configurarBotaoEditar();

        // Um toque na foto → galeria → recorte
        imgFotoPerfil.setOnClickListener(v -> selecionarFoto.launch("image/*"));
        imgFotoPerfil.setScaleType(ImageView.ScaleType.CENTER_CROP);

        buscarUsuarioNaApi();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            MaterialToolbar toolbar = getActivity().findViewById(R.id.toolbarMenu);
            if (toolbar != null) {
                toolbar.setTitle("Tela Inicial");
            }
        }
    }

    private void ligarComponentes(View view) {
        imgFotoPerfil = view.findViewById(R.id.imgFotoPerfil);
        txtUsername = view.findViewById(R.id.txtUsername);
        txtNome = view.findViewById(R.id.txtNome);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtTelefone = view.findViewById(R.id.txtTelefone);
        btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);
    }

    private void configurarBotaoEditar() {
        btnEditarPerfil.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Função de editar perfil em desenvolvimento",
                        Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Abre o uCrop em formato quadrado (ideal para foto circular).
     * O usuário move/aplica zoom no enquadramento.
     */
    private void iniciarRecorte(Uri origem) {
        Uri destino = Uri.fromFile(new File(requireContext().getCacheDir(), "crop_perfil.jpg"));

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(80);
        options.setFreeStyleCropEnabled(false); // mantém proporção 1:1
        options.setHideBottomControls(false);
        options.setToolbarTitle("Ajustar foto");

        android.content.Intent intent = UCrop.of(origem, destino)
                .withAspectRatio(1, 1)
                .withMaxResultSize(800, 800)
                .withOptions(options)
                .getIntent(requireContext());

        recortarFoto.launch(intent);
    }

    /** Lê a foto recortada, mostra na tela e envia para a API. */
    private void processarFotoRecortada(Uri uri) {
        try {
            InputStream input = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            if (input != null) input.close();

            if (bitmap == null) {
                Toast.makeText(requireContext(), "Não foi possível ler a foto", Toast.LENGTH_SHORT).show();
                return;
            }

            fotoBase64Atual = bitmapParaBase64(bitmap);
            imgFotoPerfil.setImageBitmap(bitmap);
            imgFotoPerfil.setScaleType(ImageView.ScaleType.CENTER_CROP);
            enviarFotoUsuario();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Erro ao processar foto", Toast.LENGTH_SHORT).show();
        }
    }

    private void buscarUsuarioNaApi() {
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Sessão expirada. Faça login novamente.",
                    Toast.LENGTH_SHORT).show();
            preencherComSessaoLocal();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.URL_USUARIO_PERFIL,
                null,
                response -> {
                    String nome = response.optString("nome", "");
                    String apelido = response.optString("apelido", "");
                    String email = response.optString("email", "");
                    String telefone = response.optString("telefone", "");
                    String fotoBase64 = response.optString("fotoBase64", "");

                    apelidoAtual = apelido;
                    fotoBase64Atual = fotoBase64;

                    if (!apelido.isEmpty()) sessionManager.salvarApelido(apelido);
                    if (!telefone.isEmpty()) sessionManager.salvarTelefone(telefone);

                    preencherCampos(
                            apelido.isEmpty() ? "usuário" : apelido,
                            nome,
                            email,
                            telefone.isEmpty() ? "—" : telefone
                    );
                    mostrarFotoBase64(fotoBase64, imgFotoPerfil);
                },
                error -> {
                    String msg = "Erro ao carregar perfil.";
                    if (error.networkResponse != null) {
                        msg += " Código: " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    preencherComSessaoLocal();
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

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private void enviarFotoUsuario() {
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Sessão expirada.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fotoBase64Atual == null || fotoBase64Atual.isEmpty()) {
            Toast.makeText(requireContext(), "Nenhuma foto selecionada.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("apelido", apelidoAtual == null ? "" : apelidoAtual);
            body.put("fotoBase64", fotoBase64Atual);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    ApiConfig.URL_ATUALIZAR_PERFIL,
                    body,
                    response -> Toast.makeText(requireContext(),
                            "Foto atualizada!", Toast.LENGTH_SHORT).show(),
                    error -> {
                        String msg = "Erro ao enviar foto.";
                        if (error.networkResponse != null) {
                            msg += " Código: " + error.networkResponse.statusCode;
                        }
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
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

            VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Erro ao montar envio da foto.", Toast.LENGTH_SHORT).show();
        }
    }

    private void preencherComSessaoLocal() {
        apelidoAtual = valorOuPadrao(sessionManager.obterApelido(), "usuário");
        preencherCampos(
                apelidoAtual,
                valorOuPadrao(sessionManager.obterNome(), ""),
                valorOuPadrao(sessionManager.obterEmail(), ""),
                valorOuPadrao(sessionManager.obterTelefone(), "—")
        );
    }

    private void preencherCampos(String username, String nome, String email, String telefone) {
        txtUsername.setText(username);
        txtNome.setText(nome);
        txtEmail.setText(email);
        if (txtTelefone != null) txtTelefone.setText(telefone);
    }

    private String valorOuPadrao(String valor, String padrao) {
        return (valor == null || valor.isEmpty()) ? padrao : valor;
    }

    private String bitmapParaBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
    }

    private void mostrarFotoBase64(String base64, ImageView imageView) {
        if (base64 == null || base64.isEmpty() || imageView == null) return;
        try {
            if (base64.contains(",")) {
                base64 = base64.substring(base64.indexOf(",") + 1);
            }
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bmp != null) {
                imageView.setImageBitmap(bmp);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}