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
 * Busca os dados em:
 * GET /api/usuarios/perfil
 * Header: Authorization: Bearer <token>
 *
 * Campos da API (UsuarioPerfilDTO):
 * idUsuario, nome, apelido, email, telefone, fotoBase64
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

        // Gerenciador da sessão (token, nome, e-mail, telefone...)
        sessionManager = new SessionManager(requireContext());

        // Título da toolbar
        if (getActivity() != null) {
            MaterialToolbar toolbar = getActivity().findViewById(R.id.toolbarMenu);
            if (toolbar != null) {
                toolbar.setTitle("Perfil do Usuário");
            }
        }

        ligarComponentes(view);
        configurarBotaoEditar();

        // Usuário não tem data de nascimento no cadastro/API
        if (txtDataNascimento != null) {
            txtDataNascimento.setVisibility(View.GONE);
        }

        // Busca os dados reais na API
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

    /** Liga os IDs do XML às variáveis Java. */
    private void ligarComponentes(View view) {
        imgFotoPerfil = view.findViewById(R.id.imgFotoPerfil);
        txtUsername = view.findViewById(R.id.txtUsername);
        txtNome = view.findViewById(R.id.txtNome);
        txtDataNascimento = view.findViewById(R.id.txtDataNascimento);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtTelefone = view.findViewById(R.id.txtTelefone);
        btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);
    }

    /** Clique do botão de editar (ainda em desenvolvimento). */
    private void configurarBotaoEditar() {
        btnEditarPerfil.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Função de editar perfil em desenvolvimento",
                        Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Chama a API para buscar o perfil do usuário logado.
     * Se falhar, usa os dados salvos no SessionManager.
     */
    private void buscarUsuarioNaApi() {
        String token = sessionManager.obterToken();

        // Sem token não dá para chamar endpoint protegido
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
                    // Leitura segura: se o campo não existir, usa valor padrão
                    String nome = response.optString("nome", "");
                    String apelido = response.optString("apelido", "");
                    String email = response.optString("email", "");
                    String telefone = response.optString("telefone", "");
                    // String fotoBase64 = response.optString("fotoBase64", "");

                    // Atualiza cache local
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
                // Token no header, como a API exige
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    /** Fallback: mostra o que já está salvo no aparelho. */
    private void preencherComSessaoLocal() {
        preencherCampos(
                valorOuPadrao(sessionManager.obterApelido(), "usuário"),
                valorOuPadrao(sessionManager.obterNome(), ""),
                valorOuPadrao(sessionManager.obterEmail(), ""),
                valorOuPadrao(sessionManager.obterTelefone(), "—")
        );
    }

    /** Coloca os textos nos TextViews da tela. */
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

    @Override
    public void onResume() {
        super.onResume();
        // Ao voltar para a tela, atualiza os dados
        if (sessionManager != null) {
            buscarUsuarioNaApi();
        }
    }
}