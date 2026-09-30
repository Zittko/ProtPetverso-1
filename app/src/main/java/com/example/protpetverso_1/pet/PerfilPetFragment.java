package com.example.protpetverso_1.pet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.widget.LinearLayout;
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
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Perfil do Pet — GET /api/pets/{id}/perfil
 * Mostra dados clínicos e permite trocar a foto (corte central, sem uCrop).
 */
public class PerfilPetFragment extends Fragment {

    private SessionManager sessionManager;
    private long petId = -1;
    private String codigoVinculoAtual = "";

    private ImageView imgFotoPet;
    private TextView txtNomePet, txtRacaPeso, txtCodigoPet, txtPersonalidade, txtSensibilidades;
    private LinearLayout containerTutores;
    private ImageButton btnEditarTutores;
    private MaterialButton btnCopiarCodigo, btnGerarQrCode;

    /**
     * Galeria → corta centro → mostra na tela.
     * (Persistir na API só quando existir endpoint de update de foto do pet.)
     */
    private final ActivityResultLauncher<String> selecionarFoto =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null || getContext() == null) return;
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                            requireContext().getContentResolver(), uri);
                    bitmap = cortarCentroQuadrado(bitmap);
                    bitmap = redimensionar(bitmap, 800);

                    if (imgFotoPet != null) {
                        imgFotoPet.setImageBitmap(bitmap);
                        imgFotoPet.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                    Toast.makeText(requireContext(),
                            "Foto atualizada na tela. Envio à API quando o endpoint estiver disponível.",
                            Toast.LENGTH_SHORT).show();
                    // TODO: enviar Base64 para API quando houver PUT de foto do pet
                    // String b64 = bitmapParaBase64(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Erro ao carregar foto", Toast.LENGTH_SHORT).show();
                }
            });

    public PerfilPetFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil_pet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        atualizarTituloToolbar("Perfil do Pet");
        ligarComponentes(view);
        configurarBotoes();

        // Toque na foto → galeria
        if (imgFotoPet != null) {
            imgFotoPet.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgFotoPet.setOnClickListener(v -> selecionarFoto.launch("image/*"));
        }

        if (getArguments() != null) {
            petId = getArguments().getLong("PET_ID", -1);
        }
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

    /** Liga views do XML. */
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

    /** Ações dos botões da tela. */
    private void configurarBotoes() {
        btnEditarTutores.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Editar tutores em desenvolvimento", Toast.LENGTH_SHORT).show());

        // Copia o código de vínculo para a área de transferência
        btnCopiarCodigo.setOnClickListener(v -> {
            if (codigoVinculoAtual == null || codigoVinculoAtual.isEmpty()) {
                Toast.makeText(requireContext(), "Código indisponível", Toast.LENGTH_SHORT).show();
                return;
            }
            ClipboardManager clipboard =
                    (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("codigoVinculo", codigoVinculoAtual));
            Toast.makeText(requireContext(), "Código copiado!", Toast.LENGTH_SHORT).show();
        });

        btnGerarQrCode.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Gerar QR Code em desenvolvimento", Toast.LENGTH_SHORT).show());
    }

    /**
     * GET /api/pets/{id}/perfil
     * Preenche nome, raça, peso, personalidade, sensibilidade, código e foto.
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
                Request.Method.GET, url, null,
                response -> {
                    android.util.Log.d("PET_PERFIL", "GET resposta: " + response.toString());

                    String nome = response.optString("nome", "—");
                    String raca = response.optString("raca", "—");
                    double peso = response.optDouble("peso", 0);
                    String racaPeso = raca + "  |  " + peso + " Kg";

                    String sensibilidades = response.optString("perfilDeSensibilidade", "—");
                    if (sensibilidades.isEmpty()) sensibilidades = "—";

                    String personalidade = "—";
                    if (response.has("personalidades") && !response.isNull("personalidades")) {
                        try {
                            org.json.JSONArray arr = response.getJSONArray("personalidades");
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < arr.length(); i++) {
                                if (i > 0) sb.append(", ");
                                sb.append(arr.getString(i));
                            }
                            if (sb.length() > 0) personalidade = sb.toString();
                        } catch (Exception ignored) { }
                    }

                    codigoVinculoAtual = response.optString("codigoVinculo", "");
                    String codigo = codigoVinculoAtual.isEmpty()
                            ? "Código do Pet: —"
                            : "Código do Pet: " + codigoVinculoAtual;

                    preencherCampos(nome, racaPeso, codigo, personalidade, sensibilidades, R.drawable.thor);

                    String fotoBase64 = response.optString("fotoPetBase64", "");
                    if (fotoBase64.isEmpty()) {
                        fotoBase64 = response.optString("fotoBase64", "");
                    }
                    if (!fotoBase64.isEmpty() && imgFotoPet != null) {
                        mostrarFotoBase64(fotoBase64, imgFotoPet);
                    }
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

    /** Decodifica Base64 da API e coloca no ImageView. */
    private void mostrarFotoBase64(String base64, ImageView imageView) {
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

    /** Fallback com dados do SessionManager. */
    private void carregarDadosLocais() {
        if (sessionManager != null && sessionManager.temPetSalvo()) {
            String racaPeso = sessionManager.obterPetRaca() + "  |  " + sessionManager.obterPetPeso() + " Kg";
            preencherCampos(sessionManager.obterPetNome(), racaPeso,
                    "Código do Pet: —", "—", "—", R.drawable.thor);
        } else {
            preencherCampos("Pet", "—", "Código do Pet: —", "—", "—", R.drawable.thor);
        }
    }

    /** Atualiza os TextViews e a foto padrão da tela. */
    private void preencherCampos(String nome, String racaPeso, String codigo,
                                 String personalidade, String sensibilidades, int fotoResId) {
        txtNomePet.setText(nome);
        txtRacaPeso.setText(racaPeso);
        txtCodigoPet.setText(codigo);
        txtPersonalidade.setText(personalidade);
        txtSensibilidades.setText(sensibilidades);
        imgFotoPet.setImageResource(fotoResId);
    }

    private Bitmap cortarCentroQuadrado(Bitmap src) {
        int lado = Math.min(src.getWidth(), src.getHeight());
        int x = (src.getWidth() - lado) / 2;
        int y = (src.getHeight() - lado) / 2;
        return Bitmap.createBitmap(src, x, y, lado, lado);
    }

    private Bitmap redimensionar(Bitmap original, int maxLado) {
        float escala = Math.min(
                (float) maxLado / original.getWidth(),
                (float) maxLado / original.getHeight());
        if (escala >= 1f) return original;
        return Bitmap.createScaledBitmap(original,
                Math.round(original.getWidth() * escala),
                Math.round(original.getHeight() * escala), true);
    }

    private void atualizarTituloToolbar(String titulo) {
        if (getActivity() != null) {
            MaterialToolbar toolbar = getActivity().findViewById(R.id.toolbarMenu);
            if (toolbar != null) toolbar.setTitle(titulo);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        atualizarTituloToolbar("Tela Inicial");
    }
}