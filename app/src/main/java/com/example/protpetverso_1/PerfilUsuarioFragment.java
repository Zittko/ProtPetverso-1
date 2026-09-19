package com.example.protpetverso_1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Perfil do Usuário
 * GET  /api/usuarios/perfil
 * PUT  /api/usuarios/atualizarPerfil  (apelido + fotoBase64)
 */
public class PerfilUsuarioFragment extends Fragment {

    private SessionManager sessionManager;

    private ImageView imgFotoPerfil;
    private TextView txtUsername;
    private TextView txtNome;
    private TextView txtDataNascimento;
    private TextView txtEmail;
    private TextView txtTelefone;
    private ImageButton btnEditarPerfil;

    private String fotoBase64Atual;
    private String apelidoAtual = "";

    /**
     * Abre a galeria e, ao escolher a foto:
     * 1) mostra no ImageView
     * 2) converte para Base64
     * 3) envia no PUT da API
     */
    private final ActivityResultLauncher<String> selecionarFoto =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null || getContext() == null) return;
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                            requireContext().getContentResolver(), uri);
                    bitmap = redimensionar(bitmap, 800);
                    fotoBase64Atual = bitmapParaBase64(bitmap);
                    imgFotoPerfil.setImageBitmap(bitmap);
                    enviarFotoUsuario();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Erro ao carregar foto", Toast.LENGTH_SHORT).show();
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

        // Clique na foto → escolher nova imagem
        imgFotoPerfil.setOnClickListener(v -> selecionarFoto.launch("image/*"));

        if (txtDataNascimento != null) {
            txtDataNascimento.setVisibility(View.GONE);
        }

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
        txtDataNascimento = view.findViewById(R.id.txtDataNascimento);
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
     * GET /api/usuarios/perfil
     */
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

                    if (!apelido.isEmpty()) {
                        sessionManager.salvarApelido(apelido);
                    }
                    if (!telefone.isEmpty()) {
                        sessionManager.salvarTelefone(telefone);
                    }

                    preencherCampos(
                            apelido.isEmpty() ? "usuário" : apelido,
                            nome,
                            email,
                            telefone.isEmpty() ? "—" : telefone
                    );

                    // Mostra a foto salva na API
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

    /**
     * PUT /api/usuarios/atualizarPerfil
     * Envia apelido atual + nova foto em Base64
     */
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
        if (txtTelefone != null) {
            txtTelefone.setText(telefone);
        }
    }

    private String valorOuPadrao(String valor, String padrao) {
        return (valor == null || valor.isEmpty()) ? padrao : valor;
    }

    /** Converte Bitmap para Base64 (JPEG). */
    private String bitmapParaBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
    }

    /** Reduz a imagem para não gerar Base64 enorme. */
    private Bitmap redimensionar(Bitmap original, int maxLado) {
        float escala = Math.min(
                (float) maxLado / original.getWidth(),
                (float) maxLado / original.getHeight());
        if (escala >= 1f) return original;
        int novaL = Math.round(original.getWidth() * escala);
        int novaA = Math.round(original.getHeight() * escala);
        return Bitmap.createScaledBitmap(original, novaL, novaA, true);
    }

    /** Mostra foto que veio da API em Base64. */
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Não chama GET toda vez se quiser evitar sobrescrever foto recém-escolhida.
        // Se preferir sempre atualizar da API, descomente:
        // if (sessionManager != null) buscarUsuarioNaApi();
    }
}