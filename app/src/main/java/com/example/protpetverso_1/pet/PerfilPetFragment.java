package com.example.protpetverso_1.pet;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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
import com.yalantis.ucrop.UCrop;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Perfil do Pet — GET /api/pets/{id}/perfil
 *
 * Foto:
 * 1) Toque na imagem → galeria
 * 2) uCrop → usuário enquadra (1:1)
 * 3) Resultado aparece na tela (Base64 guardado para futuro PUT de foto)
 */
public class PerfilPetFragment extends Fragment {

    private static final String TAG = "PET_PERFIL";

    private SessionManager sessionManager;
    private long petId = -1;
    private String codigoVinculoAtual = "";
    /** Foto recém-escolhida (para enviar à API quando houver endpoint). */
    private String fotoBase64Atual;

    private ImageView imgFotoPet;
    private TextView txtNomePet, txtRacaPeso, txtEspecie, txtPorte, txtSexo;
    private TextView txtCodigoPet, txtPersonalidade, txtSensibilidades, txtDataNascimento;
    private LinearLayout containerTutores;
    private ImageButton btnEditarTutores;
    private MaterialButton btnCopiarCodigo, btnGerarQrCode;

    // ==================== FOTO: GALERIA → UCROP ====================

    /** 1) Abre a galeria. */
    private final ActivityResultLauncher<String> selecionarFoto =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null || getContext() == null) return;
                iniciarRecorte(uri);
            });

    /** 2) Recebe o resultado do uCrop. */
    private final ActivityResultLauncher<Intent> recortarFoto =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (getContext() == null) return;

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

    public PerfilPetFragment() {
    }

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

        // Toque na foto → galeria → uCrop
        if (imgFotoPet != null) {
            imgFotoPet.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgFotoPet.setClickable(true);
            imgFotoPet.setFocusable(true);
            imgFotoPet.setOnClickListener(v -> selecionarFoto.launch("image/*"));
        }

        if (getArguments() != null) {
            petId = getArguments().getLong("PET_ID", -1);
        }
        if (petId <= 0 && sessionManager != null) {
            petId = sessionManager.obterPetId();
        }

        Log.d(TAG, "petId final: " + petId);

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
        txtEspecie = view.findViewById(R.id.txtEspecie);
        txtPorte = view.findViewById(R.id.txtPorte);
        txtSexo = view.findViewById(R.id.txtSexo);
        txtDataNascimento = view.findViewById(R.id.txtDataNascimento);
        txtCodigoPet = view.findViewById(R.id.txtCodigoPet);
        txtPersonalidade = view.findViewById(R.id.txtPersonalidade);
        txtSensibilidades = view.findViewById(R.id.txtSensibilidades);
        containerTutores = view.findViewById(R.id.containerTutores);
        btnEditarTutores = view.findViewById(R.id.btnEditarTutores);
        btnCopiarCodigo = view.findViewById(R.id.btnCopiarCodigo);
        btnGerarQrCode = view.findViewById(R.id.btnGerarQrCode);
    }

    private void configurarBotoes() {
        if (btnEditarTutores != null) {
            btnEditarTutores.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Editar tutores em desenvolvimento", Toast.LENGTH_SHORT).show());
        }

        if (btnCopiarCodigo != null) {
            btnCopiarCodigo.setOnClickListener(v -> {
                if (codigoVinculoAtual == null || codigoVinculoAtual.isEmpty()) {
                    Toast.makeText(requireContext(), "Código indisponível", Toast.LENGTH_SHORT).show();
                    return;
                }
                ClipboardManager clipboard =
                        (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(ClipData.newPlainText("codigoVinculo", codigoVinculoAtual));
                    Toast.makeText(requireContext(), "Código copiado!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnGerarQrCode != null) {
            btnGerarQrCode.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Gerar QR Code em desenvolvimento", Toast.LENGTH_SHORT).show());
        }
    }

    // ==================== UCROP ====================

    /** Abre a tela de recorte 1:1. */
    private void iniciarRecorte(Uri origem) {
        if (getContext() == null) return;

        Uri destino = Uri.fromFile(new File(
                requireContext().getCacheDir(),
                "crop_perfil_pet_" + System.currentTimeMillis() + ".jpg"
        ));

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(80);
        options.setToolbarTitle("Ajustar foto do pet");
        options.setFreeStyleCropEnabled(false);

        Intent intent = UCrop.of(origem, destino)
                .withAspectRatio(1, 1)
                .withMaxResultSize(800, 800)
                .withOptions(options)
                .getIntent(requireContext());

        recortarFoto.launch(intent);
    }

    /** Lê a foto recortada, mostra na ImageView e guarda Base64. */
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

            if (imgFotoPet != null) {
                imgFotoPet.setImageBitmap(bitmap);
                imgFotoPet.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }

            Toast.makeText(requireContext(),
                    "Foto atualizada na tela.",
                    Toast.LENGTH_SHORT).show();

            // TODO: quando a API tiver endpoint de update de foto do pet, chamar aqui:
            // enviarFotoParaApi(fotoBase64Atual);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar foto", e);
            Toast.makeText(requireContext(), "Erro ao processar foto", Toast.LENGTH_SHORT).show();
        }
    }

    private String bitmapParaBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
    }

    // ==================== GET PERFIL ====================

    private void buscarPetNaApi(long idPet) {
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Sessão expirada. Faça login.", Toast.LENGTH_SHORT).show();
            carregarDadosLocais();
            return;
        }

        String url = ApiConfig.URL_PET_PERFIL + idPet + "/perfil";
        Log.d(TAG, "GET URL: " + url);
        Log.d(TAG, "PetId: " + idPet);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d(TAG, "GET resposta: " + response.toString());

                    String nome = response.optString("nome", "—");
                    String raca = response.optString("raca", "—");
                    double peso = response.optDouble("peso", 0);
                    String racaPeso = raca + "  |  " + peso + " Kg";

                    String especie = response.optString("especie", "—");
                    if (especie.isEmpty()) especie = "—";

                    String sexo = formatarSexo(response.optString("sexo", "—"));
                    String porte = formatarPorte(response.optString("porte", "—"));

                    String dataNasc = response.optString("dataDeNascimento", "");
                    if (dataNasc.isEmpty()) dataNasc = response.optString("dataNascimento", "");
                    if (dataNasc.isEmpty()) dataNasc = "—";
                    else dataNasc = formatarDataTela(dataNasc);

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
                        } catch (Exception e) {
                            Log.e(TAG, "Erro ao ler personalidades", e);
                        }
                    }

                    codigoVinculoAtual = response.optString("codigoVinculo", "");
                    String codigo = codigoVinculoAtual.isEmpty()
                            ? "Código do Pet: —"
                            : "Código do Pet: " + codigoVinculoAtual;

                    // Textos (placeholder de imagem; a foto da API sobrescreve depois)
                    preencherCampos(nome, racaPeso, especie, sexo, porte, dataNasc,
                            codigo, personalidade, sensibilidades, R.drawable.thor);

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
                        int status = error.networkResponse.statusCode;
                        msg += " Código: " + status;
                        try {
                            String errBody = new String(
                                    error.networkResponse.data,
                                    java.nio.charset.StandardCharsets.UTF_8);
                            Log.e(TAG, "GET erro " + status + ": " + errBody);
                        } catch (Exception ignored) {
                        }
                    } else {
                        Log.e(TAG, "GET sem resposta do servidor", error);
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
            Log.e(TAG, "Erro ao decodificar foto", e);
        }
    }

    private String formatarSexo(String sexoApi) {
        if (sexoApi == null || sexoApi.isEmpty() || sexoApi.equals("—")) return "—";
        String s = sexoApi.toUpperCase();
        if (s.startsWith("F") || s.contains("FEMEA")) return "Fêmea";
        if (s.startsWith("M") || s.contains("MACHO")) return "Macho";
        return sexoApi;
    }

    private String formatarPorte(String porteApi) {
        if (porteApi == null || porteApi.isEmpty() || porteApi.equals("—")) return "—";
        switch (porteApi.toUpperCase()) {
            case "PEQUENO": return "Pequeno";
            case "MEDIO": return "Médio";
            case "GRANDE": return "Grande";
            default: return porteApi;
        }
    }

    private String formatarDataTela(String dataApi) {
        if (dataApi == null || dataApi.isEmpty() || dataApi.equals("—")) return "—";
        if (dataApi.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] p = dataApi.split("-");
            return p[2] + "/" + p[1] + "/" + p[0];
        }
        return dataApi;
    }

    private void carregarDadosLocais() {
        if (sessionManager != null && sessionManager.temPetSalvo()) {
            String racaPeso = sessionManager.obterPetRaca() + "  |  " + sessionManager.obterPetPeso() + " Kg";
            preencherCampos(
                    sessionManager.obterPetNome(),
                    racaPeso,
                    "—", "—", "—", "—",
                    "Código do Pet: —", "—", "—",
                    R.drawable.thor
            );
        } else {
            preencherCampos("Pet", "—", "—", "—", "—", "—",
                    "Código do Pet: —", "—", "—", R.drawable.thor);
        }
    }

    private void preencherCampos(String nome, String racaPeso,
                                 String especie, String sexo, String porte, String dataNasc,
                                 String codigo, String personalidade, String sensibilidades,
                                 int fotoResId) {
        if (txtNomePet != null) txtNomePet.setText(nome);
        if (txtRacaPeso != null) txtRacaPeso.setText(racaPeso);
        if (txtEspecie != null) txtEspecie.setText(especie);
        if (txtSexo != null) txtSexo.setText(sexo);
        if (txtPorte != null) txtPorte.setText(porte);
        if (txtDataNascimento != null) txtDataNascimento.setText(dataNasc);
        if (txtCodigoPet != null) txtCodigoPet.setText(codigo);
        if (txtPersonalidade != null) txtPersonalidade.setText(personalidade);
        if (txtSensibilidades != null) txtSensibilidades.setText(sensibilidades);
        if (imgFotoPet != null) imgFotoPet.setImageResource(fotoResId);
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