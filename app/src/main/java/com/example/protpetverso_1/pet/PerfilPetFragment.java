package com.example.protpetverso_1.pet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.protpetverso_1.MenuActivity;
import com.example.protpetverso_1.pet.EditarPetActivity;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.UCropHelper;
import com.example.protpetverso_1.VolleySingleton;
import com.example.protpetverso_1.data.AppDataCache;
import com.example.protpetverso_1.data.DataPreloader;
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
 * Lápis de tutores: abre EditarPetActivity
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
    private MaterialButton btnExcluirPet;
    private boolean excluindoPet = false;
    private LinearLayout blocoCompartilhar;
    private boolean tutorEmAcao = false;

    /** Atualiza a lista de tutores enquanto o perfil está aberto (reflete desvinculações). */
    private static final long INTERVALO_TUTORES_MS = 10_000L;
    private final Handler handlerTutores = new Handler(Looper.getMainLooper());
    private final Runnable pollTutores = new Runnable() {
        @Override public void run() {
            if (!isAdded()) return;
            if (petId > 0) carregarTutores(petId);
            handlerTutores.postDelayed(this, INTERVALO_TUTORES_MS);
        }
    };
    private MaterialButton btnCopiarCodigo;

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
        if (petId > 0 && sessionManager != null) {
            carregarPerfil(petId);
            atualizarPapelNaTela();
            renderTutores();
            // O login não devolve o id: sem ele não dá para escrever "Você" nem achar a própria foto
            DataPreloader.garantirIdUsuario(requireContext(), () -> {
                if (!isAdded()) return;
                atualizarPapelNaTela();
                renderTutores();
            });
            handlerTutores.removeCallbacks(pollTutores);
            handlerTutores.post(pollTutores);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handlerTutores.removeCallbacks(pollTutores);
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
        btnExcluirPet = view.findViewById(R.id.btnExcluirPet);
        blocoCompartilhar = view.findViewById(R.id.blocoCompartilhar);
        btnCopiarCodigo = view.findViewById(R.id.btnCopiarCodigo);
    }

    private void configurarBotoes() {
        if (btnExcluirPet != null) {
            btnExcluirPet.setOnClickListener(v -> confirmarExclusaoPet());
        }
        if (btnEditarTutores != null) {
            btnEditarTutores.setOnClickListener(v -> {
                Intent i = new Intent(requireContext(), EditarPetActivity.class);
                i.putExtra("PET_ID", petId);
                i.putExtra("NOME", textoDe(txtNomePet));
                i.putExtra("RACA_PESO", textoDe(txtRacaPeso));
                i.putExtra("ESPECIE", textoDe(txtEspecie));
                i.putExtra("PORTE", textoDe(txtPorte));
                i.putExtra("SEXO", textoDe(txtSexo));
                i.putExtra("NASCIMENTO", textoDe(txtDataNascimento));
                i.putExtra("SENSIBILIDADE", textoDe(txtSensibilidades));
                i.putExtra("PERSONALIDADE", textoDe(txtPersonalidade));
                startActivity(i);
            });
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

    }

    private String textoDe(TextView tv) {
        if (tv == null || tv.getText() == null) return "";
        String s = tv.getText().toString().trim();
        return "—".equals(s) ? "" : s;
    }

    private void iniciarRecorte(Uri origem) {
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

        atualizarPapelNaTela();

        if (txtNomePet != null) txtNomePet.setText(nome);
        if (txtRacaPeso != null) txtRacaPeso.setText(racaPeso);
        if (txtEspecie != null) txtEspecie.setText(especie);
        if (txtSexo != null) txtSexo.setText(sexo);
        if (txtPorte != null) txtPorte.setText(porte);
        if (txtDataNascimento != null) txtDataNascimento.setText(dataNasc);
        if (txtCodigoPet != null) txtCodigoPet.setText(codigo);
        if (txtPersonalidade != null) txtPersonalidade.setText(personalidade);
        if (txtSensibilidades != null) txtSensibilidades.setText(sensibilidades);

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

    private void confirmarExclusaoPet() {
        if (petId <= 0 || excluindoPet || !podeAtualizarUi()) return;
        if (!souDono()) {
            confirmarSaidaDoPet();
            return;
        }
        String nome = textoDe(txtNomePet);
        String alvo = (nome.isEmpty() || nome.equals("—")) ? "este pet" : nome;
        new AlertDialog.Builder(requireContext(), R.style.DialogPetVerso)
                .setTitle("Excluir pet")
                .setMessage("Deseja excluir " + alvo + "? Todas as tarefas, vacinas e vínculos "
                        + "com outros tutores também serão apagados. Essa ação não pode ser desfeita.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (d, w) -> excluirPet())
                .show();
    }

    private void excluirPet() {
        final long idExcluir = petId;
        final String token = sessionManager.obterToken();
        if (idExcluir <= 0 || token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Sessão expirada.", Toast.LENGTH_SHORT).show();
            return;
        }
        final Context app = requireContext().getApplicationContext();
        excluindoPet = true;
        if (btnExcluirPet != null) btnExcluirPet.setEnabled(false);

        StringRequest req = new StringRequest(
                Request.Method.DELETE,
                ApiConfig.URL_EXCLUIR_PET + idExcluir,
                response -> {
                    excluindoPet = false;
                    aposPetExcluido(app, idExcluir, "Pet excluído.");
                },
                error -> {
                    excluindoPet = false;
                    if (!podeAtualizarUi()) return;
                    if (btnExcluirPet != null) btnExcluirPet.setEnabled(true);
                    int code = error.networkResponse != null ? error.networkResponse.statusCode : 0;
                    String msg;
                    if (code == 403) msg = "Apenas o tutor principal pode excluir o pet.";
                    else if (code == 404) msg = "Pet não encontrado ou sem permissão.";
                    else if (code == 0) msg = "Sem conexão com o servidor.";
                    else msg = "Erro ao excluir o pet (" + code + ").";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Authorization", "Bearer " + token);
                return h;
            }
        };
        VolleySingleton.getInstance(app).addToRequestQueue(req);
    }

    /** Limpa o cache do pet, escolhe outro pet (ou a tela de escolha) e recomeça pelo menu. */
    private void aposPetExcluido(Context app, long idExcluido, String mensagem) {
        AppDataCache cache = AppDataCache.get();
        cache.removerPet(idExcluido);

        SessionManager sm = new SessionManager(app);
        java.util.List<com.example.protpetverso_1.pet.PetResumo> restantes = cache.getMeusPets();
        if (sm.obterPetId() == idExcluido || sm.obterPetId() <= 0) {
            if (restantes.isEmpty()) {
                sm.limparPet();
            } else {
                com.example.protpetverso_1.pet.PetResumo p = restantes.get(0);
                sm.salvarPet(p.getIdPet(), p.getNome(), p.getPapel());
            }
        }

        Toast.makeText(app, mensagem, Toast.LENGTH_SHORT).show();

        Intent intent = restantes.isEmpty()
                ? EscolhaPetActivity.criarIntent(app, true)
                : new Intent(app, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        app.startActivity(intent);
        Activity act = getActivity();
        if (act != null) act.finish();
    }

    // ==================== PAPEL, TUTORES E DESVINCULAÇÃO ====================

    /**
     * Papel de quem está vendo. Ordem de confiança:
     * 1) a lista de tutores da API; 2) o papel do pet em /meusPets; 3) ter recebido o código de vínculo
     * (só o dono recebe). Se nada confirmar, trata como SUPORTE: é o lado seguro (esconde ações do dono).
     */
    private boolean souDono() {
        long eu = sessionManager.obterUserId();
        if (eu > 0) {
            for (Tutor t : AppDataCache.get().getTutores(petId)) {
                if (t.getIdUsuario() == eu) return t.isDono();
            }
        }
        for (PetResumo p : AppDataCache.get().getMeusPets()) {
            if (p.getIdPet() == petId) return "DONO".equalsIgnoreCase(p.getPapel());
        }
        return codigoVinculoAtual != null && !codigoVinculoAtual.isEmpty();
    }

    /** Dono: vê código e "Excluir pet". Suporte: sem código e com "Sair deste pet". */
    private void atualizarPapelNaTela() {
        if (!podeAtualizarUi()) return;
        boolean dono = souDono();
        if (blocoCompartilhar != null) blocoCompartilhar.setVisibility(dono ? View.VISIBLE : View.GONE);
        if (btnExcluirPet != null) {
            btnExcluirPet.setText(dono ? "Excluir pet" : "Desvincular desse pet");
            btnExcluirPet.setVisibility(View.VISIBLE);
        }
    }

    private void carregarTutores(long idPet) {
        final String token = sessionManager != null ? sessionManager.obterToken() : null;
        if (idPet <= 0 || token == null || token.isEmpty()) return;
        Context ctx = getContext();
        if (ctx == null) return;
        final Context app = ctx.getApplicationContext();

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                ApiConfig.URL_PETS + idPet + ApiConfig.SUFIXO_TUTORES,
                null,
                arr -> {
                    java.util.List<Tutor> lista = Tutor.listaFromJson(arr);
                    AppDataCache.get().setTutores(idPet, lista);
                    salvarFotosTutores(app, lista);
                    if (idPet != petId || !podeAtualizarUi()) return;

                    // Se eu não estou mais na lista, fui desvinculado pelo tutor principal.
                    // Só conclui isso se todos os ids vieram válidos (evita falso positivo).
                    long eu = sessionManager.obterUserId();
                    boolean idsValidos = !lista.isEmpty();
                    boolean estouNaLista = false;
                    for (Tutor t : lista) {
                        if (t.getIdUsuario() <= 0) idsValidos = false;
                        if (t.getIdUsuario() == eu) estouNaLista = true;
                    }
                    if (idsValidos && eu > 0 && !estouNaLista) {
                        handlerTutores.removeCallbacks(pollTutores);
                        aposPetExcluido(app, idPet, "Você foi desvinculado deste pet.");
                        return;
                    }

                    atualizarPapelNaTela();
                    renderTutores();
                },
                error -> {
                    Log.w(TAG, "GET tutores falhou: " + error);
                    int code = error.networkResponse != null ? error.networkResponse.statusCode : 0;
                    // 404 = sem vínculo aceito com este pet (fui desvinculado) ou pet excluído
                    if (code == 404 && idPet == petId && isAdded()) {
                        handlerTutores.removeCallbacks(pollTutores);
                        aposPetExcluido(app, idPet, "Este pet não está mais disponível para você.");
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Authorization", "Bearer " + token);
                return h;
            }
        };
        VolleySingleton.getInstance(app).addToRequestQueue(req);
    }

    /** Monta a lista: "Você (Tutor Principal)", "Fulano (Suporte)"... com Desvincular para o dono. */
    private void renderTutores() {
        if (containerTutores == null || !podeAtualizarUi()) return;
        containerTutores.removeAllViews();

        java.util.List<Tutor> lista = AppDataCache.get().getTutores(petId);
        final long eu = sessionManager.obterUserId();
        final boolean euDono = souDono();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        if (lista.isEmpty()) {
            // Sem lista ainda (primeira abertura offline): mostra pelo menos o próprio usuário
            View row = inflater.inflate(R.layout.item_tutor, containerTutores, false);
            aplicarFotoTutor((ImageView) row.findViewById(R.id.imgTutorFoto), eu);
            ((TextView) row.findViewById(R.id.txtTutorNome))
                    .setText("Você (" + Tutor.rotuloPapel(euDono) + ")");
            containerTutores.addView(row);
            return;
        }

        java.util.List<Tutor> ordenada = new java.util.ArrayList<>();
        for (Tutor t : lista) if (t.isDono()) ordenada.add(t);
        for (Tutor t : lista) if (!t.isDono()) ordenada.add(t);

        for (Tutor t : ordenada) {
            View row = inflater.inflate(R.layout.item_tutor, containerTutores, false);
            boolean ehEu = eu > 0 && t.getIdUsuario() == eu;
            String rotulo = Tutor.rotuloPapel(t.isDono());
            ((TextView) row.findViewById(R.id.txtTutorNome))
                    .setText((ehEu ? "Você" : t.getNomeExibicao()) + " (" + rotulo + ")");

            aplicarFotoTutor((ImageView) row.findViewById(R.id.imgTutorFoto), t.getIdUsuario());

            MaterialButton btn = row.findViewById(R.id.btnDesvincularTutor);
            if (euDono && t.isSuporte() && !ehEu) {
                btn.setVisibility(View.VISIBLE);
                btn.setOnClickListener(v -> confirmarDesvincularTutor(t));
            }
            containerTutores.addView(row);
        }
    }

    /** Foto do tutor (cache em disco); sem foto, mantém o ícone padrão. */
    private void aplicarFotoTutor(ImageView img, long idUsuario) {
        if (img == null || idUsuario <= 0 || getContext() == null) return;
        java.io.File f = PhotoDiskCache.arquivoUsuario(requireContext(), idUsuario);
        if (!f.isFile() || f.length() == 0) return;
        Bitmap bmp = PhotoDiskCache.carregarBitmap(f.getAbsolutePath(), 160);
        if (bmp == null) return;
        androidx.core.widget.ImageViewCompat.setImageTintList(img, null); // senão o AppCompat tinge a foto
        // Mesmo tamanho do item sem foto (36dp). O padding de 2dp evita cortar a borda (o traço é
        // desenhado metade para dentro e metade para fora do círculo) e o fundo bege some.
        int pad = Math.round(2 * getResources().getDisplayMetrics().density);
        img.setBackground(null);
        img.setPadding(pad, pad, pad, pad);
        img.setImageBitmap(bmp);
    }

    /** Grava as fotos que a API enviar junto da lista (fora da thread principal) e redesenha. */
    private void salvarFotosTutores(Context app, java.util.List<Tutor> lista) {
        final long eu = sessionManager.obterUserId();
        final java.util.List<Tutor> comFoto = new java.util.ArrayList<>();
        for (Tutor t : lista) {
            if (t.getIdUsuario() > 0 && t.getIdUsuario() != eu && !t.getFotoBase64().isEmpty()) comFoto.add(t);
        }
        if (comFoto.isEmpty()) return;
        new Thread(() -> {
            for (Tutor t : comFoto) {
                PhotoDiskCache.salvarFotoUsuario(app, t.getIdUsuario(), t.getFotoBase64());
            }
            handlerTutores.post(() -> { if (isAdded()) renderTutores(); });
        }).start();
    }

    private void confirmarDesvincularTutor(Tutor t) {
        if (tutorEmAcao || !podeAtualizarUi()) return;
        new AlertDialog.Builder(requireContext(), R.style.DialogPetVerso)
                .setTitle("Desvincular tutor")
                .setMessage("Desvincular " + t.getNomeExibicao() + "? Essa pessoa deixará de ver este pet, "
                        + "a agenda e as vacinas. As tarefas que ela fez continuam no histórico.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Desvincular", (d, w) -> desvincularTutor(t))
                .show();
    }

    /** Dono: DELETE /api/pets/{id}/tutores/{idUsuario}. */
    private void desvincularTutor(Tutor t) {
        final long idPet = petId;
        final String token = sessionManager.obterToken();
        if (idPet <= 0 || token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Sessão expirada.", Toast.LENGTH_SHORT).show();
            return;
        }
        final Context app = requireContext().getApplicationContext();
        tutorEmAcao = true;

        StringRequest req = new StringRequest(
                Request.Method.DELETE,
                ApiConfig.URL_PETS + idPet + ApiConfig.SUFIXO_TUTORES + "/" + t.getIdUsuario(),
                response -> {
                    tutorEmAcao = false;
                    // Tira da lista na hora (atualização automática) e confirma com a API em seguida
                    java.util.List<Tutor> atual = AppDataCache.get().getTutores(idPet);
                    java.util.List<Tutor> nova = new java.util.ArrayList<>();
                    for (Tutor x : atual) if (x.getIdUsuario() != t.getIdUsuario()) nova.add(x);
                    AppDataCache.get().setTutores(idPet, nova);
                    Toast.makeText(app, t.getNomeExibicao() + " foi desvinculado.", Toast.LENGTH_SHORT).show();
                    if (idPet == petId && podeAtualizarUi()) {
                        renderTutores();
                        carregarTutores(idPet);
                    }
                },
                error -> {
                    tutorEmAcao = false;
                    if (!podeAtualizarUi()) return;
                    int code = error.networkResponse != null ? error.networkResponse.statusCode : 0;
                    String msg;
                    if (code == 403) msg = "Apenas o tutor principal pode desvincular.";
                    else if (code == 404) msg = "Essa pessoa já não está vinculada.";
                    else if (code == 0) msg = "Sem conexão com o servidor.";
                    else msg = "Erro ao desvincular (" + code + ").";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                    if (code == 404) carregarTutores(idPet);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Authorization", "Bearer " + token);
                return h;
            }
        };
        VolleySingleton.getInstance(app).addToRequestQueue(req);
    }

    private void confirmarSaidaDoPet() {
        String nome = textoDe(txtNomePet);
        String alvo = (nome.isEmpty() || nome.equals("—")) ? "este pet" : nome;
        new AlertDialog.Builder(requireContext(), R.style.DialogPetVerso)
                .setTitle("Desvincular desse pet")
                .setMessage("Desvincular-se de " + alvo + "? Você deixará de ver este pet, a agenda e as vacinas. "
                        + "Os dados continuam com os outros tutores.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Desvincular", (d, w) -> sairDoPet())
                .show();
    }

    /** Suporte: DELETE /api/pets/{id}/vinculo (desvincula o próprio usuário). */
    private void sairDoPet() {
        final long idPet = petId;
        final String token = sessionManager.obterToken();
        if (idPet <= 0 || token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Sessão expirada.", Toast.LENGTH_SHORT).show();
            return;
        }
        final Context app = requireContext().getApplicationContext();
        excluindoPet = true;
        if (btnExcluirPet != null) btnExcluirPet.setEnabled(false);
        handlerTutores.removeCallbacks(pollTutores);

        StringRequest req = new StringRequest(
                Request.Method.DELETE,
                ApiConfig.URL_PETS + idPet + ApiConfig.SUFIXO_VINCULO,
                response -> {
                    excluindoPet = false;
                    aposPetExcluido(app, idPet, "Você foi desvinculado do pet.");
                },
                error -> {
                    excluindoPet = false;
                    if (!podeAtualizarUi()) return;
                    if (btnExcluirPet != null) btnExcluirPet.setEnabled(true);
                    handlerTutores.postDelayed(pollTutores, INTERVALO_TUTORES_MS);
                    int code = error.networkResponse != null ? error.networkResponse.statusCode : 0;
                    String msg;
                    if (code == 403 || code == 409 || code == 400) msg = "O tutor principal não pode sair do pet; ele pode excluí-lo.";
                    else if (code == 404) msg = "Você já não está vinculado a este pet.";
                    else if (code == 0) msg = "Sem conexão com o servidor.";
                    else msg = "Erro ao desvincular (" + code + ").";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Authorization", "Bearer " + token);
                return h;
            }
        };
        VolleySingleton.getInstance(app).addToRequestQueue(req);
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
        handlerTutores.removeCallbacks(pollTutores);
        atualizarTituloToolbar("Tela Inicial");
    }
}