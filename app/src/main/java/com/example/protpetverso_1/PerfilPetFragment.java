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

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

/**
 * Tela de Perfil do Pet.
 * Busca os dados em GET /api/pets/{id}/perfil com o token do usuário.
 */
public class PerfilPetFragment extends Fragment {

    private SessionManager sessionManager;
    private long petId = -1;

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

    public PerfilPetFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil_pet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        atualizarTituloToolbar("Perfil do Pet");
        ligarComponentes(view);
        configurarBotoes();

        // 1) id por argumento
        if (getArguments() != null) {
            petId = getArguments().getLong("PET_ID", -1);
        }

        // 2) se não veio, usa o pet salvo localmente
        if (petId <= 0) {
            petId = sessionManager.obterPetId();
        }

        if (petId > 0) {
            buscarPetNaApi(petId);
        } else {
            Toast.makeText(requireContext(), "Nenhum pet selecionado.", Toast.LENGTH_SHORT).show();
            carregarDadosLocais();
        }
    }

    private void ligarComponentes(View view) {
        imgFotoPet = view.findViewById(R.id.imgFotoPet);
        txtNomePet = view.findViewById(R.id.txtNomePet);
        txtRacaPeso = view.findViewById(R.id.txtRacaPeso);
        txtCodigoPet = view.findViewById(R.id.txtCodigoPet);
        txtPersonalidade = view.findViewById(R.id.txtPersonalidade);
        txtSensibilidades = view.findViewById(R.id.txtSensibilidades);
        containerTutores = view.findViewById(R.id.containerTutores);
        btnEditarTutores = view.findViewById(R.id.btnEditarTutores);
        btnCopiarCodigo = view.findViewById(R.id.btnCopiarCodigo);
        btnGerarQrCode = view.findViewById(R.id.btnGerarQrCode);
    }

    private void configurarBotoes() {
        btnEditarTutores.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Editar tutores em desenvolvimento",
                        Toast.LENGTH_SHORT).show()
        );

        btnCopiarCodigo.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Código copiado!",
                        Toast.LENGTH_SHORT).show()
        );

        btnGerarQrCode.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Gerar QR Code em desenvolvimento",
                        Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * GET /api/pets/{id}/perfil
     */
    private void buscarPetNaApi(long idPet) {
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Sessão expirada. Faça login.", Toast.LENGTH_SHORT).show();
            carregarDadosLocais();
            return;
        }

        String url = ApiConfig.URL_PET_PERFIL + idPet + "/perfil";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    String nome = response.optString("nome", "");
                    String raca = response.optString("raca", "");
                    double peso = response.optDouble("peso", 0);
                    String porte = response.optString("porte", "");

                    String racaPeso = raca + "  |  " + peso + " Kg";
                    if (!porte.isEmpty()) {
                        racaPeso = raca + "  |  " + porte + "  |  " + peso + " Kg";
                    }

                    String sensibilidades = response.optString("perfilDeSensibilidade", "—");
                    if (sensibilidades.isEmpty()) {
                        sensibilidades = "—";
                    }

                    String personalidade = "—";
                    if (response.has("personalidades") && !response.isNull("personalidades")) {
                        try {
                            JSONArray arr = response.getJSONArray("personalidades");
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < arr.length(); i++) {
                                if (i > 0) sb.append(", ");
                                sb.append(arr.getString(i));
                            }
                            if (sb.length() > 0) {
                                personalidade = sb.toString();
                            }
                        } catch (Exception e) {
                            personalidade = response.optString("personalidades", "—");
                        }
                    }

                    String codigo = "Código do Pet: —";
                    int fotoResId = R.drawable.thor;

                    preencherCampos(nome, racaPeso, codigo, personalidade, sensibilidades, fotoResId);
                },
                error -> {
                    String msg = "Erro ao carregar pet.";
                    if (error.networkResponse != null) {
                        msg += " Código: " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    carregarDadosLocais();
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
     * Fallback com dados salvos no SessionManager.
     */
    private void carregarDadosLocais() {
        if (sessionManager != null && sessionManager.temPetSalvo()) {
            String nome = sessionManager.obterPetNome();
            String raca = sessionManager.obterPetRaca();
            String peso = sessionManager.obterPetPeso();
            String racaPeso = raca + "  |  " + peso + " Kg";

            preencherCampos(
                    nome,
                    racaPeso,
                    "Código do Pet: —",
                    "—",
                    "—",
                    R.drawable.thor
            );
        } else {
            // último recurso (dados fixos)
            preencherCampos(
                    "Pet",
                    "—",
                    "Código do Pet: —",
                    "—",
                    "—",
                    R.drawable.thor
            );
        }
    }

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

    private void atualizarTituloToolbar(String titulo) {
        if (getActivity() != null) {
            MaterialToolbar toolbar = getActivity().findViewById(R.id.toolbarMenu);
            if (toolbar != null) {
                toolbar.setTitle(titulo);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        atualizarTituloToolbar("Tela Inicial");
    }
}