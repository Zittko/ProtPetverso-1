package com.example.protpetverso_1.pet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.MenuActivity;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.UCropHelper;
import com.example.protpetverso_1.VolleySingleton;
import com.example.protpetverso_1.account.LoginActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Cadastro do pet.
 *
 * Foto (igual criar usuário):
 * 1) Toque na imagem → galeria
 * 2) uCrop → usuário enquadra (1:1)
 * 3) Resultado → Base64 → envio no POST
 *
 * Fluxo geral:
 * - Mais Informações → rascunho (sem API)
 * - Cadastrar → POST /api/pets/cadastrar
 *            → PUT personalidade/sensibilidade se houver
 *            → Home
 */
public class PetSignUpActivity extends AppCompatActivity {

    private TextInputEditText edtPetNome, edtPetEspecie, edtPetRaca, edtPetDtn, edtPetPeso;
    private TextInputLayout ipPetEdtNome, ipPetEspecie, ipPetRaca, ipPetDtn, ipPetPeso;
    private androidx.appcompat.widget.AppCompatSpinner PetSexoSpinner, PetPorteSpinner;
    private com.google.android.material.button.MaterialButton btnCadastrar;
    private android.widget.ImageButton btnVoltar;
    private ImageView imgFotoPetCadastro;
    private TextView txtMaisInformacoes;

    private SessionManager sessionManager;
    private String fotoBase64;

    private ArrayList<String> personalidadesTemp = new ArrayList<>();
    private String sensibilidadeTemp = "";

    // ---------- FOTO: galeria → uCrop ----------

    /** Abre a galeria. */
    private final ActivityResultLauncher<String> selecionarFoto =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                iniciarRecorte(uri);
            });

    /** Recebe o resultado do uCrop. */
    private final ActivityResultLauncher<Intent> recortarFoto =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri resultUri = UCrop.getOutput(result.getData());
                    if (resultUri != null) {
                        processarFotoRecortada(resultUri);
                    }
                } else if (result.getResultCode() == UCrop.RESULT_ERROR && result.getData() != null) {
                    Throwable cropError = UCrop.getError(result.getData());
                    Toast.makeText(this,
                            "Erro no recorte: " + (cropError != null ? cropError.getMessage() : ""),
                            Toast.LENGTH_SHORT).show();
                }
            });

    /** Retorno da tela Mais Informações (só memória). */
    private final ActivityResultLauncher<Intent> maisInfoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    ArrayList<String> lista = data.getStringArrayListExtra("PERSONALIDADES");
                    personalidadesTemp = lista != null ? lista : new ArrayList<>();
                    sensibilidadeTemp = data.getStringExtra("SENSIBILIDADE");
                    if (sensibilidadeTemp == null) sensibilidadeTemp = "";

                    Toast.makeText(this,
                            "Informações extras guardadas. Toque em Cadastrar para finalizar.",
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pet_sign_up_layout);

        sessionManager = new SessionManager(this);
        ligarComponentes();
        configurarSpinners();
        configurarDatePicker();

        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> finish());
        }

        // Toque na foto → galeria → uCrop
        if (imgFotoPetCadastro != null) {
            imgFotoPetCadastro.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgFotoPetCadastro.setOnClickListener(v -> selecionarFoto.launch("image/*"));
        } else {
            Log.w("PET_CADASTRO", "imgPetFoto não encontrado no XML — foto não clicável");
        }

        btnCadastrar.setOnClickListener(v -> {
            if (!validarCampos()) return;
            cadastrarPetNaApi();
        });

        if (txtMaisInformacoes != null) {
            txtMaisInformacoes.setOnClickListener(v -> abrirMaisInformacoes());
        }
    }

    private void ligarComponentes() {
        edtPetNome = findViewById(R.id.edtPetNome);
        edtPetEspecie = findViewById(R.id.edtPetEspecie);
        edtPetRaca = findViewById(R.id.edtPetRaca);
        edtPetDtn = findViewById(R.id.edtPetDtn);
        edtPetPeso = findViewById(R.id.edtPetPeso);

        ipPetEdtNome = findViewById(R.id.ipPetEdtNome);
        ipPetEspecie = findViewById(R.id.ipPetEspecie);
        ipPetRaca = findViewById(R.id.ipPetRaca);
        ipPetDtn = findViewById(R.id.ipPetDtn);
        ipPetPeso = findViewById(R.id.ipPetPeso);

        PetSexoSpinner = findViewById(R.id.PetSexoSpinner);
        PetPorteSpinner = findViewById(R.id.PetPorteSpinner);

        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnVoltar = findViewById(R.id.btnVoltar);
        // ID do XML do cadastro do pet — confirme se é imgPetFoto
        imgFotoPetCadastro = findViewById(R.id.imgPetFoto);
        txtMaisInformacoes = findViewById(R.id.txtMaisInformacoes);
    }

    // ==================== UCROP (igual CriarUsuarioActivity) ====================

    /** Abre a tela de recorte 1:1 para o usuário enquadrar. */
    private void iniciarRecorte(Uri origem) {
        Intent intent = UCropHelper.criarIntentRecorte(
                this,
                origem,
                "Ajustar foto do pet"
        );
        recortarFoto.launch(intent);
    }

    /** Lê o arquivo recortado, mostra na ImageView e gera Base64. */
    private void processarFotoRecortada(Uri uri) {
        try {
            InputStream input = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            if (input != null) input.close();

            if (bitmap == null) {
                Toast.makeText(this, "Não foi possível ler a foto", Toast.LENGTH_SHORT).show();
                return;
            }

            fotoBase64 = bitmapParaBase64(bitmap);
            if (imgFotoPetCadastro != null) {
                imgFotoPetCadastro.setImageBitmap(bitmap);
                imgFotoPetCadastro.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao processar foto", Toast.LENGTH_SHORT).show();
        }
    }

    private String bitmapParaBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
    }

    // ==================== MAIS INFORMAÇÕES ====================

    private void abrirMaisInformacoes() {
        Intent intent = new Intent(this, MaisInfoPetActivity.class);
        intent.putExtra("MODO_RASCUNHO", true);
        intent.putStringArrayListExtra("PERSONALIDADES", personalidadesTemp);
        intent.putExtra("SENSIBILIDADE", sensibilidadeTemp);
        maisInfoLauncher.launch(intent);
    }

    // ==================== SPINNERS / DATA ====================

    private void configurarSpinners() {
        String[] opcoesSexo = {"Selecione", "Macho", "Fêmea"};
        String[] opcoesPorte = {"Selecione", "Pequeno", "Médio", "Grande"};

        ArrayAdapter<String> adapterSexo = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, opcoesSexo);
        adapterSexo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        PetSexoSpinner.setAdapter(adapterSexo);

        ArrayAdapter<String> adapterPorte = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, opcoesPorte);
        adapterPorte.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        PetPorteSpinner.setAdapter(adapterPorte);
    }

    private void configurarDatePicker() {
        if (edtPetDtn == null) return;
        edtPetDtn.setOnClickListener(v -> abrirDatePicker());
        if (ipPetDtn != null) {
            ipPetDtn.setEndIconOnClickListener(v -> abrirDatePicker());
        }
    }

    private void abrirDatePicker() {
        final java.util.Calendar calendario = java.util.Calendar.getInstance();
        String texto = String.valueOf(edtPetDtn.getText()).trim();
        try {
            if (texto.matches("\\d{2}/\\d{2}/\\d{4}")) {
                String[] p = texto.split("/");
                calendario.set(Integer.parseInt(p[2]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[0]));
            }
        } catch (Exception ignored) { }

        new android.app.DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> edtPetDtn.setText(String.format(
                        java.util.Locale.getDefault(),
                        "%02d/%02d/%04d", dayOfMonth, month + 1, year)),
                calendario.get(java.util.Calendar.YEAR),
                calendario.get(java.util.Calendar.MONTH),
                calendario.get(java.util.Calendar.DAY_OF_MONTH)
        ).show();
    }

    private String converterDataParaApi(String dataTela) {
        if (dataTela == null) return "";
        dataTela = dataTela.trim();
        if (dataTela.matches("\\d{4}-\\d{2}-\\d{2}")) return dataTela;
        if (dataTela.contains("/") || dataTela.contains("-")) {
            String[] partes = dataTela.split("[/-]");
            if (partes.length == 3) {
                String dia = partes[0].length() == 1 ? "0" + partes[0] : partes[0];
                String mes = partes[1].length() == 1 ? "0" + partes[1] : partes[1];
                return partes[2] + "-" + mes + "-" + dia;
            }
        }
        return dataTela;
    }

    private String normalizarPorte(String porteTela) {
        if (porteTela == null) return "MEDIO";
        String p = Normalizer.normalize(porteTela, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim().toUpperCase();
        if (p.startsWith("PEQ")) return "PEQUENO";
        if (p.startsWith("MED")) return "MEDIO";
        if (p.startsWith("GRA")) return "GRANDE";
        return "MEDIO";
    }

    private String normalizarSexo(String sexoTela) {
        if (sexoTela == null) return "MACHO";
        String s = Normalizer.normalize(sexoTela, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim().toUpperCase();
        return s.startsWith("F") ? "FEMEA" : "MACHO";
    }

    private boolean validarCampos() {
        boolean ok = true;
        String nome = String.valueOf(edtPetNome.getText()).trim();
        String data = String.valueOf(edtPetDtn.getText()).trim();
        String sexo = PetSexoSpinner.getSelectedItem() != null
                ? PetSexoSpinner.getSelectedItem().toString() : "";
        String porte = PetPorteSpinner.getSelectedItem() != null
                ? PetPorteSpinner.getSelectedItem().toString() : "";

        if (nome.isEmpty()) {
            ipPetEdtNome.setError("Informe o nome");
            ok = false;
        } else {
            ipPetEdtNome.setError(null);
        }

        if (data.isEmpty()) {
            ipPetDtn.setError("Informe a data");
            ok = false;
        } else {
            ipPetDtn.setError(null);
        }

        if (sexo.isEmpty() || sexo.equalsIgnoreCase("Selecione")) {
            Toast.makeText(this, "Selecione o sexo", Toast.LENGTH_SHORT).show();
            ok = false;
        }
        if (porte.isEmpty() || porte.equalsIgnoreCase("Selecione")) {
            Toast.makeText(this, "Selecione o porte", Toast.LENGTH_SHORT).show();
            ok = false;
        }
        return ok;
    }

    // ==================== API ====================

    private void cadastrarPetNaApi() {
        btnCadastrar.setEnabled(false);

        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
            btnCadastrar.setEnabled(true);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        try {
            String nome = String.valueOf(edtPetNome.getText()).trim();
            String especie = String.valueOf(edtPetEspecie.getText()).trim();
            String raca = String.valueOf(edtPetRaca.getText()).trim();
            String dataApi = converterDataParaApi(String.valueOf(edtPetDtn.getText()).trim());
            String sexo = normalizarSexo(PetSexoSpinner.getSelectedItem().toString());
            String porte = normalizarPorte(PetPorteSpinner.getSelectedItem().toString());

            double peso = 0;
            String pesoStr = String.valueOf(edtPetPeso.getText()).trim();
            if (!pesoStr.isEmpty()) {
                peso = Double.parseDouble(pesoStr.replace(",", "."));
            }
            if (especie.isEmpty()) especie = "Não informado";
            if (raca.isEmpty()) raca = "SRD";

            final String nomeFinal = nome;
            final String racaFinal = raca;
            final String especieFinal = especie;
            final String dataFinal = dataApi;
            final String sexoFinal = sexo;
            final String porteFinal = porte;
            final String pesoFinal = pesoStr.isEmpty() ? "0" : pesoStr.replace(",", ".");

            CadastrarPetRequest dto = new CadastrarPetRequest(
                    nome, raca, especie, dataApi, porte, peso, sexo
            );
            JSONObject body = dto.toJsonObject();

            if (fotoBase64 != null && !fotoBase64.isEmpty()) {
                body.put("fotoBase64", fotoBase64);
            } else {
                body.put("fotoBase64", JSONObject.NULL);
            }

            Log.d("PET_CADASTRO", "Enviando cadastro (foto? " + (fotoBase64 != null) + ")");

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.URL_CADASTRAR_PET,
                    body,
                    response -> {
                        long petId = lerPetId(response);
                        Log.d("PET_CADASTRO", "Resposta: " + response.toString());

                        if (petId > 0) {
                            sessionManager.salvarPet(
                                    petId, nomeFinal, racaFinal, especieFinal,
                                    pesoFinal, sexoFinal, porteFinal, dataFinal
                            );
                        }

                        boolean temExtras = !personalidadesTemp.isEmpty()
                                || (sensibilidadeTemp != null && !sensibilidadeTemp.trim().isEmpty());

                        if (temExtras && petId > 0) {
                            enviarPerfilClinico(petId, token);
                        } else {
                            Toast.makeText(this, "Pet cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                            irParaHome();
                        }
                    },
                    error -> {
                        btnCadastrar.setEnabled(true);
                        tratarErroHttp(error);
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

            VolleySingleton.getInstance(this).addToRequestQueue(request);
        } catch (Exception e) {
            btnCadastrar.setEnabled(true);
            e.printStackTrace();
            Toast.makeText(this, "Erro ao preparar cadastro do pet.", Toast.LENGTH_SHORT).show();
        }
    }

    private long lerPetId(JSONObject response) {
        try {
            if (response.has("id")) {
                Object id = response.get("id");
                return (id instanceof String) ? Long.parseLong((String) id) : response.getLong("id");
            }
            if (response.has("idPet")) {
                Object id = response.get("idPet");
                return (id instanceof String) ? Long.parseLong((String) id) : response.getLong("idPet");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void enviarPerfilClinico(long petId, String token) {
        try {
            String url = ApiConfig.URL_ATUALIZAR_PERFIL_PET + petId + "/atualizarPetPerfil";

            JSONObject body = new JSONObject();
            body.put("perfilDeSensibilidade", sensibilidadeTemp != null ? sensibilidadeTemp : "");
            JSONArray arr = new JSONArray();
            for (String p : personalidadesTemp) {
                arr.put(p);
            }
            body.put("personalidades", arr);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    body,
                    response -> {
                        Toast.makeText(this, "Pet cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                        irParaHome();
                    },
                    error -> {
                        Toast.makeText(this,
                                "Pet criado, mas houve erro ao salvar personalidade/sensibilidade.",
                                Toast.LENGTH_LONG).show();
                        irParaHome();
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

            VolleySingleton.getInstance(this).addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
            irParaHome();
        }
    }

    private void irParaHome() {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void tratarErroHttp(com.android.volley.VolleyError error) {
        if (error.networkResponse != null) {
            int status = error.networkResponse.statusCode;
            String mensagem = "Erro na requisição.";
            try {
                String body = new String(error.networkResponse.data, java.nio.charset.StandardCharsets.UTF_8);
                mensagem = new JSONObject(body).optString("mensagem", mensagem);
                Log.e("PET_CADASTRO", body);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Erro (" + status + "): " + mensagem, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Sem conexão com o servidor.", Toast.LENGTH_LONG).show();
        }
    }
}