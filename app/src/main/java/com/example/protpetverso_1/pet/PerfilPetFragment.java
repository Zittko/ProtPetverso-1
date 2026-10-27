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
import com.example.protpetverso_1.UCropHelper;
import com.example.protpetverso_1.VolleySingleton;
import com.example.protpetverso_1.data.AppDataCache;
import com.example.protpetverso_1.data.PhotoDiskCache;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Perfil do Pet — GET /api/pets/{id}/perfil
 *
 * 1) Cache (AppDataCache / disco) → UI
 * 2) GET em background → atualiza cache + UI
 *
 * Foto: galeria → uCrop 1:1 → PUT /api/pets/{id}/foto
 */
public class PerfilPetFragment extends Fragment {

    private static final String TAG = "PET_PERFIL";

    private SessionManager sessionManager;
    private long petId = -1;
    private String codigoVinculoAtual = "";
    private String fotoBase64Atual;

    private ImageView imgFotoPet;
    private TextView txtNomePet, txtRacaPeso, txtEspecie, txtPorte, txtSexo;
    private TextView txtCodigoPet, txtPersonalidade, txtSensibilidades, txtDataNascimento;
    private LinearLayout containerTutores;
    private ImageButton btnEditarTutores;
    private MaterialButton btnCopiarCodigo, btnGerarQrCode;

    private final ActivityResultLauncher<String> selecionarFoto =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null || getContext() == null) return;
                iniciarRecorte(uri);
            });

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

    private boolean podeAtualizarUi() {
        return isAdded() && getContext() != null && getView() != null;
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

        Log.d(TAG, "petId=" + petId + " temCache=" + AppDataCache.get().temPerfilPet(petId));

        if (petId > 0) {
            carregarPerfil(petId);
        } else {
            Toast.makeText(requireContext(), "Nenhum pet selecionado.", Toast.LENGTH_SHORT).show();
            carregarDadosLocais();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        atualizarTituloToolbar("Perfil do Pet");
    }

    private void carregarPerfil(long idPet) {
        if (AppDataCache.get().temPerfilPet(idPet)) {
            Log.d(TAG, "Aplicando cache do pet " + idPet);
            aplicarJsonNoPerfil(AppDataCache.get().getPerfilPet(idPet));
        }
        buscarPetNaApi(idPet);
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

    private void iniciarRecorte(Uri origem) {
        // Se no seu projeto o método for buildCropIntent, troque aqui.
        Intent intent = UCropHelper.criarIntentRecorte(
                requireContext(),
                origem,
                "Ajustar foto do pet"
        );
        recortarFoto.launch(intent);
    }

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

            enviarFotoParaApi(fotoBase64Atual);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar foto", e);
            Toast.makeText(requireContext(), "Erro ao processar foto", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarFotoParaApi(String fotoBase64) {
        if (petId <= 0) {
            Toast.makeText(requireContext(), "Pet inválido.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fotoBase64 == null || fotoBase64.isEmpty()) return;

        if (fotoBase64.contains(",")) {
            fotoBase64 = fotoBase64.substring(fotoBase64.indexOf(",") + 1);
        }

        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Sessão expirada.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String url = ApiConfig.URL_FOTO_PET + petId + "/foto";
            JSONObject body = new JSONObject();
            body.put("fotoBase64", fotoBase64);

            Log.d(TAG, "PUT foto: " + url);

            final String fotoEnviada = fotoBase64;

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    body,
                    response -> {
                        Log.d(TAG, "Foto salva na API");
                        if (!podeAtualizarUi()) return;

                        Toast.makeText(requireContext(), "Foto salva!", Toast.LENGTH_SHORT).show();

                        JSONObject cached = AppDataCache.get().getPerfilPet(petId);
                        try {
                            if (cached == null) cached = new JSONObject();
                            else cached = new JSONObject(cached.toString());

                            String fotoVolta = response.optString("fotoBase64", "");
                            if (fotoVolta.isEmpty()) {
                                fotoVolta = response.optString("fotoPetBase64", "");
                            }
                            if (fotoVolta.isEmpty()) {
                                fotoVolta = fotoEnviada;
                            }
                            cached.put("fotoPetBase64", fotoVolta);
                            AppDataCache.get().setPerfilPet(petId, cached);
                            PhotoDiskCache.salvarFotoPet(requireContext(), petId, fotoVolta);

                            if (imgFotoPet != null) {
                                mostrarFotoBase64(fotoVolta, imgFotoPet);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Erro ao atualizar cache da foto", e);
                        }
                    },
                    error -> {
                        if (!podeAtualizarUi()) return;
                        String msg = "Erro ao salvar foto.";
                        if (error.networkResponse != null) {
                            int s = error.networkResponse.statusCode;
                            if (s == 403) {
                                msg = "Apenas o tutor principal pode alterar a foto.";
                            } else {
                                msg += " (" + s + ")";
                            }
                            try {
                                String err = new String(error.networkResponse.data,
                                        java.nio.charset.StandardCharsets.UTF_8);
                                Log.e(TAG, "PUT foto erro: " + err);
                            } catch (Exception ignored) {
                            }
                        }
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> h = new HashMap<>();
                    h.put("Authorization", "Bearer " + token);
                    h.put("Content-Type", "application/json");
                    return h;
                }
            };

            VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao montar PUT foto", e);
            Toast.makeText(requireContext(), "Erro ao enviar foto.", Toast.LENGTH_SHORT).show();
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
            if (podeAtualizarUi()) {
                Toast.makeText(requireContext(), "Sessão expirada. Faça login.", Toast.LENGTH_SHORT).show();
                if (!AppDataCache.get().temPerfilPet(idPet)) {
                    carregarDadosLocais();
                }
            }
            return;
        }

        String url = ApiConfig.URL_PET_PERFIL + idPet + "/perfil";
        Log.d(TAG, "GET URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    AppDataCache.get().setPerfilPet(idPet, response);
                    Log.d(TAG, "GET ok — cache atualizado pet " + idPet);

                    if (!podeAtualizarUi()) return;
                    aplicarJsonNoPerfil(response);
                },
                error -> {
                    Log.e(TAG, "GET erro pet " + idPet, error);
                    if (!podeAtualizarUi()) return;

                    if (AppDataCache.get().temPerfilPet(idPet)) {
                        return;
                    }

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

        Context ctx = getContext();
        if (ctx != null) {
            VolleySingleton.getInstance(ctx.getApplicationContext()).addToRequestQueue(request);
        }
    }

    private void aplicarJsonNoPerfil(JSONObject response) {
        if (response == null || !podeAtualizarUi()) return;

        String nome = response.optString("nome", "—");
        String raca = response.optString("raca", "—");
        double peso = response.optDouble("peso", 0);
        String racaPeso = raca + "  |  " + peso + " Kg";

        String especie = response.optString("especie", "—");
        if (especie.isEmpty()) especie = "—";

        String sexo = formatarSexo(response.optString("sexo", "—"));
        String porte = formatarPorte(response.optString("porte", "—"));

        String dataNasc = response.optString("dataDeNascimento", "");
        if (dataNasc.isEmpty()) {
            dataNasc = response.optString("idade", "");
        }
        if (dataNasc.isEmpty()) {
            dataNasc = "—";
        } else {
            dataNasc = formatarDataTela(dataNasc);
        }

        String sensibilidades = response.optString("perfilDeSensibilidade", "—");
        if (sensibilidades.isEmpty()) sensibilidades = "—";

        String personalidade = "—";
        if (response.has("personalidades") && !response.isNull("personalidades")) {
            try {
                JSONArray arr = response.getJSONArray("personalidades");
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

        if (txtNomePet != null) txtNomePet.setText(nome);
        if (txtRacaPeso != null) txtRacaPeso.setText(racaPeso);
        if (txtEspecie != null) txtEspecie.setText(especie);
        if (txtSexo != null) txtSexo.setText(sexo);
        if (txtPorte != null) txtPorte.setText(porte);
        if (txtDataNascimento != null) txtDataNascimento.setText(dataNasc);
        if (txtCodigoPet != null) txtCodigoPet.setText(codigo);
        if (txtPersonalidade != null) txtPersonalidade.setText(personalidade);
        if (txtSensibilidades != null) txtSensibilidades.setText(sensibilidades);

        // Disco primeiro, depois base64
        String fotoPath = response.optString("fotoPath", "");
        if ((fotoPath == null || fotoPath.isEmpty()) && petId > 0) {
            java.io.File f = PhotoDiskCache.arquivoPet(requireContext(), petId);
            if (f.isFile() && f.length() > 0) {
                fotoPath = f.getAbsolutePath();
            }
        }
        Bitmap bmpDisk = PhotoDiskCache.carregarBitmap(fotoPath);
        if (bmpDisk != null && imgFotoPet != null) {
            imgFotoPet.setImageBitmap(bmpDisk);
            imgFotoPet.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            String fotoBase64 = response.optString("fotoPetBase64", "");
            if (fotoBase64.isEmpty()) {
                fotoBase64 = response.optString("fotoBase64", "");
            }
            if (!fotoBase64.isEmpty() && imgFotoPet != null) {
                mostrarFotoBase64(fotoBase64, imgFotoPet);
            }
        }
    }

    private void mostrarFotoBase64(String base64, ImageView imageView) {
        if (base64 == null || base64.isEmpty() || imageView == null) return;
        try {
            if (base64.contains(",")) {
                base64 = base64.substring(base64.indexOf(",") + 1);
            }
            base64 = base64.replace("\n", "").replace("\r", "").replace(" ", "");
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
        if (imgFotoPet != null && fotoResId != 0) {
            imgFotoPet.setImageResource(fotoResId);
        }
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