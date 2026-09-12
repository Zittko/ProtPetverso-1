package com.example.protpetverso_1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Tela de Perfil do Pet.
 * Mostra a foto, nome, raça, peso, tutores vinculados
 * e as informações clínicas (personalidade e sensibilidades).
 *
 * Esta classe foi organizada pensando na futura integração com a API.
 * Quando a API estiver pronta, bastará buscar os dados do pet
 * e preencher os campos com as informações recebidas.
 */
public class PerfilPetFragment extends Fragment {

    // ==================== COMPONENTES DA TELA ====================
    private ImageView imgFotoPet;
    private TextView txtNomePet;
    private TextView txtRacaPeso;
    private TextView txtCodigoPet;
    private TextView txtPersonalidade;
    private TextView txtSensibilidades;
    private LinearLayout containerTutores;
    private ImageButton btnEditarTutores;
    private MaterialButton btnCopiarCodigo;
    private MaterialButton btnGerarQrCode;

    // Construtor vazio obrigatório
    public PerfilPetFragment() {
    }

    /**
     * Cria a parte visual do Fragment (carrega o XML).
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil_pet, container, false);
    }

    /**
     * Chamado depois que o layout foi criado.
     * Aqui ligamos os componentes, configuramos os cliques
     * e carregamos os dados do pet.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Altera o título da Toolbar
        atualizarTituloToolbar("Perfil do Pet");

        // Liga os componentes do XML com as variáveis Java
        ligarComponentes(view);

        // Configura os cliques dos botões
        configurarBotoes();

        // Carrega as informações do pet na tela
        // (por enquanto dados fixos, depois virá da API)
        carregarDadosDoPet();
    }

    /**
     * Liga cada elemento do XML às variáveis desta classe.
     */
    private void ligarComponentes(View view) {
        imgFotoPet        = view.findViewById(R.id.imgFotoPet);
        txtNomePet        = view.findViewById(R.id.txtNomePet);
        txtRacaPeso       = view.findViewById(R.id.txtRacaPeso);
        txtCodigoPet      = view.findViewById(R.id.txtCodigoPet);
        txtPersonalidade  = view.findViewById(R.id.txtPersonalidade);
        txtSensibilidades = view.findViewById(R.id.txtSensibilidades);
        containerTutores  = view.findViewById(R.id.containerTutores);
        btnEditarTutores  = view.findViewById(R.id.btnEditarTutores);
        btnCopiarCodigo   = view.findViewById(R.id.btnCopiarCodigo);
        btnGerarQrCode    = view.findViewById(R.id.btnGerarQrCode);
    }

    /**
     * Define o que acontece quando o usuário clica nos botões.
     * Por enquanto apenas mostra mensagens de teste.
     */
    private void configurarBotoes() {
        // Botão de editar tutores
        btnEditarTutores.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "Editar tutores em desenvolvimento",
                    Toast.LENGTH_SHORT).show();
        });

        // Botão copiar código
        btnCopiarCodigo.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "Código copiado!",
                    Toast.LENGTH_SHORT).show();
            // No futuro: copiar o código real para a área de transferência
        });

        // Botão gerar QR Code
        btnGerarQrCode.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "Gerar QR Code em desenvolvimento",
                    Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Preenche a tela com os dados do pet.
     *
     * Atualmente usa dados fixos apenas para visualização.
     * Quando a API estiver pronta, este método deverá:
     * 1. Buscar os dados do pet no servidor (Volley)
     * 2. Receber o JSON de resposta
     * 3. Chamar preencherCampos() com os valores recebidos
     */
    private void carregarDadosDoPet() {
        // ===== DADOS TEMPORÁRIOS (apenas para teste) =====
        String nome           = "Thor";
        String racaPeso       = "Vira-Lata  |  12 Kg";
        String codigo         = "Código do Pet: XYZ-987";
        String personalidade  = "Brincalhão, Curioso";
        String sensibilidades = "Medo de fogos de artíficio";
        int fotoResId         = R.drawable.thor;

        // Coloca os dados na tela
        preencherCampos(nome, racaPeso, codigo, personalidade, sensibilidades, fotoResId);

        // Exemplo futuro:
        // buscarPetNaApi(idDoPet);
    }

    /**
     * Coloca os valores recebidos nos componentes da tela.
     * Este método é separado de propósito para facilitar
     * a integração com a API no futuro.
     */
    private void preencherCampos(String nome, String racaPeso, String codigo,
                                 String personalidade, String sensibilidades,
                                 int fotoResId) {
        txtNomePet.setText(nome);
        txtRacaPeso.setText(racaPeso);
        txtCodigoPet.setText(codigo);
        txtPersonalidade.setText(personalidade);
        txtSensibilidades.setText(sensibilidades);
        imgFotoPet.setImageResource(fotoResId);
    }

    /**
     * Método reservado para a futura integração com a API.
     * Aqui será feita a chamada Volley para buscar
     * os dados completos do pet no servidor.
     */
    private void buscarPetNaApi(int idPet) {
        // TODO: Implementar chamada Volley no futuro
        //
        // Exemplo da estrutura:
        // String url = "https://sua-api.com/pets/" + idPet;
        // JsonObjectRequest request = new JsonObjectRequest(...);
        // Volley.newRequestQueue(requireContext()).add(request);
    }

    /**
     * Altera o título da Toolbar.
     */
    private void atualizarTituloToolbar(String titulo) {
        if (getActivity() != null) {
            MaterialToolbar toolbar = getActivity().findViewById(R.id.toolbarMenu);
            if (toolbar != null) {
                toolbar.setTitle(titulo);
            }
        }
    }

    /**
     * Quando o usuário sai desta tela, volta o título
     * da Toolbar para o valor padrão.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        atualizarTituloToolbar("Tela Inicial");
    }
}