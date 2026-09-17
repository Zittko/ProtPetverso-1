package com.example.protpetverso_1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.HashMap;
import java.util.Map;

/**
 * Tela de Perfil do Usuário.
 * Busca os dados na API (GET /api/usuarios/perfil) usando o token salvo.
 * Campos atuais da API: idUsuario, nome, apelido, email, fotoBase64.
 * Telefone será adicionado futuramente na API.
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

        // Oculta data de nascimento (não existe no cadastro/API do usuário)
        if (txtDataNascimento != null) {
            txtDataNascimento.setVisibility(View.GONE);
        }

        // Busca dados reais na API
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
     * Header: Authorization: Bearer <token>
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
                    String fotoBase64 = response.optString("fotoBase64", "");

                    // Quando a API incluir telefone, descomente:
                    // String telefone = response.optString("telefone", "");
                    String telefone = "";

                    if (!apelido.isEmpty()) {
                        sessionManager.salvarApelido(apelido);
                    }

                    preencherCampos(
                            apelido.isEmpty() ? "usuário" : apelido,
                            nome,
                            email,
                            telefone
                    );

                    // TODO: quando houver fotoBase64 válida, converter e exibir em imgFotoPerfil
                    // if (fotoBase64 != null && !fotoBase64.isEmpty()) { ... }
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
     * Fallback: usa dados salvos no SessionManager.
     */
    private void preencherComSessaoLocal() {
        preencherCampos(
                safe(sessionManager.obterApelido(), "usuário"),
                safe(sessionManager.obterNome(), ""),
                safe(sessionManager.obterEmail(), ""),
                ""
        );
    }

    private void preencherCampos(String username, String nome, String email, String telefone) {
        txtUsername.setText(username);
        txtNome.setText(nome);
        txtEmail.setText(email);

        if (txtTelefone != null) {
            txtTelefone.setText((telefone == null || telefone.isEmpty()) ? "—" : telefone);
        }
    }

    private String safe(String value, String fallback) {
        return (value == null || value.isEmpty()) ? fallback : value;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Atualiza ao voltar para a tela
        if (sessionManager != null) {
            buscarUsuarioNaApi();
        }
    }
}