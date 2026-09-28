package com.example.protpetverso_1.pet;

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
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.VolleySingleton;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

/**
 * Tela de Perfil do Pet.
 * Busca os dados em:
 * GET /api/pets/{id}/perfil
 * Header: Authorization: Bearer <token>
 *
 * Campos da API (PetPerfilDTO):
 * id, nome, raca, especie, porte, peso, sexo,
 * fotoPetBase64, perfilDeSensibilidade, personalidades
 *
 * Se personalidade/sensibilidade não vierem, a tela mostra "—" e não quebra.
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

        // 1) tenta receber o id por argumento
        if (getArguments() != null) {
            petId = getArguments().getLong("PET_ID", -1);
        }

        // 2) se não veio, usa o último pet salvo no cadastro
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
     * Lê os campos com optString/optDouble para não quebrar
     * quando personalidade ou sensibilidade estiverem vazias.
     */
    private void buscarPetNaApi(long idPet) {
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Sessão expirada. Faça login.", Toast.LENGTH_SHORT).show();
            carregarDadosLocais();
            return;
        }

        String url = ApiConfig.URL_PET_PERFIL + idPet + "/perfil";

        android.util.Log.d("PET_PERFIL", "GET URL: " + url);
        android.util.Log.d("PET_PERFIL", "PetId: " + idPet);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    android.util.Log.d("PET_PERFIL", "GET resposta: " + response.toString());

                    String nome = response.optString("nome", "—");
                    String raca = response.optString("raca", "—");
                    double peso = response.optDouble("peso", 0);
                    String racaPeso = raca + "  |  " + peso + " Kg";

                    String sensibilidades = response.optString("perfilDeSensibilidade", "—");
                    if (sensibilidades.isEmpty()) {
                        sensibilidades = "—";
                    }

                    String personalidade = "—";
                    if (response.has("personalidades") && !response.isNull("personalidades")) {
                        try {
                            org.json.JSONArray arr = response.getJSONArray("personalidades");
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < arr.length(); i++) {
                                if (i > 0) sb.append(", ");
                                sb.append(arr.getString(i));
                            }
                            if (sb.length() > 0) {
                                personalidade = sb.toString();
                            }
                        } catch (Exception ignored) {
                        }
                    }

                    // código de vínculo, se a API mandar em algum campo
                    String codigoVinculo = response.optString("codigoVinculo", "");
                    String codigo = codigoVinculo.isEmpty()
                            ? "Código do Pet: —"
                            : "Código do Pet: " + codigoVinculo;

                    int fotoResId = R.drawable.thor;
                    preencherCampos(nome, racaPeso, codigo, personalidade, sensibilidades, fotoResId);

                    // se vier foto em Base64
                    String fotoBase64 = response.optString("fotoPetBase64", "");
                    if (!fotoBase64.isEmpty() && imgFotoPet != null) {
                        mostrarFotoBase64(fotoBase64, imgFotoPet);
                    }
                },
                error -> {
                    String msg = "Erro ao carregar pet.";
                    if (error.networkResponse != null) {
                        int status = error.networkResponse.statusCode;
                        msg += " Código: " + status;
                        try {
                            String errBody = new String(
                                    error.networkResponse.data,
                                    java.nio.charset.StandardCharsets.UTF_8
                            );
                            android.util.Log.e("PET_PERFIL", "GET erro " + status + ": " + errBody);
                        } catch (Exception ignored) {
                        }
                    } else {
                        android.util.Log.e("PET_PERFIL", "GET sem resposta do servidor");
                    }
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    carregarDadosLocais();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    /** Opcional: mostrar foto Base64 no ImageView */
    private void mostrarFotoBase64(String base64, ImageView imageView) {
        try {
            if (base64.contains(",")) {
                base64 = base64.substring(base64.indexOf(",") + 1);
            }
            byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
            android.graphics.Bitmap bmp =
                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bmp != null) {
                imageView.setImageBitmap(bmp);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fallback com dados salvos no SessionManager após o cadastro do pet.
     */
    private void carregarDadosLocais() {
        if (sessionManager != null && sessionManager.temPetSalvo()) {
            String nome = sessionManager.obterPetNome();
            String raca = sessionManager.obterPetRaca();
            String peso = sessionManager.obterPetPeso();
            String racaPeso = raca + "  |  " + peso + " Kg";

            preencherCampos(nome, racaPeso, "Código do Pet: —", "—", "—", R.drawable.thor);
        } else {
            preencherCampos("Pet", "—", "Código do Pet: —", "—", "—", R.drawable.thor);
        }
    }

    /** Coloca os valores nos componentes da tela. */
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