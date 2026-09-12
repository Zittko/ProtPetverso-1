package com.example.protpetverso_1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Tela de Perfil do Usuário.
 * Mostra a foto, o nome de usuário e as informações pessoais
 * (nome, data de nascimento, e-mail e telefone).
 *
 * Esta classe foi organizada pensando na futura integração com a API.
 * Quando a API estiver pronta, bastará chamar os métodos de carregar
 * e atualizar dados.
 */
public class PerfilUsuarioFragment extends Fragment {

    // ==================== COMPONENTES DA TELA ====================
    // Aqui guardamos as referências dos elementos visuais do XML

    private ImageView imgFotoPerfil;          // Foto circular do usuário
    private TextView txtUsername;             // Nome de usuário (@carlosbuenow)
    private TextView txtNome;                 // Nome completo
    private TextView txtDataNascimento;       // Data de nascimento
    private TextView txtEmail;                // E-mail
    private TextView txtTelefone;             // Telefone
    private ImageButton btnEditarPerfil;      // Botão de editar informações

    // Construtor vazio obrigatório para Fragments
    public PerfilUsuarioFragment() {
    }

    /**
     * Este método é chamado quando o Android precisa criar a parte
     * visual (layout) deste Fragment.
     * Ele apenas "infla" o XML e devolve a View para ser exibida.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Carrega o arquivo fragment_perfil_usuario.xml
        return inflater.inflate(R.layout.fragment_perfil_usuario, container, false);
    }

    /**
     * Este método é chamado logo depois que o layout já foi criado.
     * É o melhor lugar para:
     * - Ligar os componentes do XML com as variáveis Java
     * - Configurar cliques de botões
     * - Carregar os dados do usuário
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Muda o título da Toolbar para "Perfil do Usuário"
        if (getActivity() != null) {
            MaterialToolbar toolbar = getActivity().findViewById(R.id.toolbarMenu);
            if (toolbar != null) {
                toolbar.setTitle("Perfil do Usuário");
            }
        }

        // Liga os componentes do XML com as variáveis desta classe
        ligarComponentes(view);

        // Configura o que acontece quando o usuário clica no botão de editar
        configurarBotaoEditar();

        // Carrega as informações do usuário na tela
        // (por enquanto usa dados fixos, depois virá da API)
        carregarDadosDoUsuario();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Volta o título para Tela Inicial quando sair do perfil
        if (getActivity() != null) {
            MaterialToolbar toolbar = getActivity().findViewById(R.id.toolbarMenu);
            if (toolbar != null) {
                toolbar.setTitle("Tela Inicial");
            }
        }
    }

    /**
     * Liga cada elemento do XML (com android:id) às variáveis Java.
     * Assim conseguimos alterar textos, imagens e reagir a cliques.
     */
    private void ligarComponentes(View view) {
        imgFotoPerfil     = view.findViewById(R.id.imgFotoPerfil);
        txtUsername       = view.findViewById(R.id.txtUsername);
        txtNome           = view.findViewById(R.id.txtNome);
        txtDataNascimento = view.findViewById(R.id.txtDataNascimento);
        txtEmail          = view.findViewById(R.id.txtEmail);
        txtTelefone       = view.findViewById(R.id.txtTelefone);
        btnEditarPerfil   = view.findViewById(R.id.btnEditarPerfil);
    }

    /**
     * Define o comportamento do botão de editar.
     * Por enquanto apenas mostra uma mensagem.
     * No futuro este botão poderá abrir uma tela de edição
     * ou um diálogo para alterar nome, e-mail, telefone, etc.
     */
    private void configurarBotaoEditar() {
        btnEditarPerfil.setOnClickListener(v -> {
            // TODO: No futuro, abrir tela ou diálogo de edição
            Toast.makeText(requireContext(),
                    "Função de editar perfil em desenvolvimento",
                    Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Preenche a tela com os dados do usuário.
     *
     * Atualmente os dados estão fixos (hardcoded) apenas para teste.
     * Quando a API estiver pronta, este método deverá:
     * 1. Fazer uma chamada Volley para buscar os dados do usuário logado
     * 2. Receber a resposta JSON
     * 3. Chamar o método preencherCampos() com os dados recebidos
     */
    private void carregarDadosDoUsuario() {
        // ===== DADOS TEMPORÁRIOS (apenas para visualização) =====
        // Depois estes valores virão da API
        String username        = "carlosbuenow";
        String nome            = "Carlos Henrique Bueno";
        String dataNascimento  = "02/08/1998";
        String email           = "carlinhos290826@hotmail.com";
        String telefone        = "(11) 98765-4321";

        // Chama o método que realmente coloca os dados na tela
        preencherCampos(username, nome, dataNascimento, email, telefone);

        // Exemplo futuro com API (quando for implementar):
        // buscarUsuarioNaApi();
    }

    /**
     * Coloca os textos recebidos nos TextViews da tela.
     * Este método é separado de propósito para facilitar a integração
     * com a API: quando a API responder, basta chamar este método
     * passando os valores recebidos.
     */
    private void preencherCampos(String username, String nome,
                                 String dataNascimento, String email,
                                 String telefone) {
        txtUsername.setText(username);
        txtNome.setText(nome);
        txtDataNascimento.setText(dataNascimento);
        txtEmail.setText(email);
        txtTelefone.setText(telefone);

        // No futuro também será possível carregar a foto aqui:
        // imgFotoPerfil.setImageBitmap(...);
        // ou usar biblioteca como Glide/Picasso para carregar da URL
    }

    /**
     * Método reservado para a futura integração com a API.
     * Aqui será feita a chamada Volley para buscar os dados
     * do usuário logado no servidor.
     *
     * Exemplo do que será feito depois:
     * - Montar a URL da API
     * - Enviar o token de autenticação
     * - Receber o JSON com os dados
     * - Chamar preencherCampos() com os valores recebidos
     */
    private void buscarUsuarioNaApi() {
        // TODO: Implementar chamada Volley aqui no futuro
        // Exemplo de estrutura:
        //
        // String url = "https://sua-api.com/usuario/perfil";
        // JsonObjectRequest request = new JsonObjectRequest(...);
        // Volley.newRequestQueue(requireContext()).add(request);
    }

    /**
     * Chamado toda vez que o Fragment volta a ficar visível.
     * Útil para atualizar os dados caso o usuário tenha
     * editado alguma informação e voltado para esta tela.
     */
    @Override
    public void onResume() {
        super.onResume();
        // Se quiser, pode chamar novamente carregarDadosDoUsuario()
        // para garantir que os dados estejam sempre atualizados.
    }
}