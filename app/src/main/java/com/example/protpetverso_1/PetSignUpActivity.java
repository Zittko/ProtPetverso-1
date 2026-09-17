package com.example.protpetverso_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PetSignUpActivity extends AppCompatActivity {

    private TextInputEditText edtPetNome, edtPetEspecie, edtPetRaca, edtPetDtn, edtPetPeso;
    private TextInputLayout ipPetEdtNome, ipPetEspecie, ipPetRaca, ipPetDtn, ipPetPeso;

    private androidx.appcompat.widget.AppCompatSpinner PetSexoSpinner, PetPorteSpinner;

    private com.google.android.material.button.MaterialButton btnCadastrar;
    private android.widget.ImageButton btnVoltar;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pet_sign_up_layout);

        sessionManager = new SessionManager(this);

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

        configurarSpinners();

        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> finish());
        }

        btnCadastrar.setOnClickListener(v -> {
            if (!validarCampos()) return;
            cadastrarPetNaApi();
        });
    }

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

    private String converterDataParaApi(String dataTela) {
        String[] partes = dataTela.split("/");
        if (partes.length != 3) return dataTela;
        return partes[2] + "-" + partes[1] + "-" + partes[0];
    }

    private String normalizarPorte(String porteTela) {
        if (porteTela == null) return "MEDIO";
        String p = porteTela.trim().toUpperCase();
        if (p.startsWith("PEQ")) return "PEQUENO";
        if (p.startsWith("MED")) return "MEDIO";
        if (p.startsWith("GRA")) return "GRANDE";
        return p;
    }

    private String normalizarSexo(String sexoTela) {
        if (sexoTela == null) return "MACHO";
        String s = sexoTela.trim().toUpperCase();
        if (s.startsWith("F")) return "FEMEA";
        return "MACHO";
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
            String dataTela = String.valueOf(edtPetDtn.getText()).trim();
            String dataApi = converterDataParaApi(dataTela);
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

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.URL_CADASTRAR_PET,
                    body,
                    response -> {
                        long petId = -1;
                        try {
                            if (response.has("id")) {
                                Object id = response.get("id");
                                petId = (id instanceof String)
                                        ? Long.parseLong((String) id)
                                        : response.getLong("id");
                            } else if (response.has("idPet")) {
                                Object id = response.get("idPet");
                                petId = (id instanceof String)
                                        ? Long.parseLong((String) id)
                                        : response.getLong("idPet");
                            }
                            android.util.Log.d("PET_CADASTRO", "Resposta: " + response.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        sessionManager.salvarPet(
                                petId > 0 ? petId : 1,
                                nomeFinal,
                                racaFinal,
                                especieFinal,
                                pesoFinal,
                                sexoFinal,
                                porteFinal,
                                dataFinal
                        );

                        Toast.makeText(PetSignUpActivity.this, "Pet cadastrado com sucesso!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(PetSignUpActivity.this, MenuActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
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

    private void tratarErroHttp(com.android.volley.VolleyError error) {
        if (error.networkResponse != null) {
            int status = error.networkResponse.statusCode;
            String mensagem = "Erro na requisição.";
            try {
                String body = new String(error.networkResponse.data, java.nio.charset.StandardCharsets.UTF_8);
                JSONObject json = new JSONObject(body);
                mensagem = json.optString("mensagem", mensagem);
                android.util.Log.e("PET_CADASTRO", body);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Erro (" + status + "): " + mensagem, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Sem conexão com o servidor.", Toast.LENGTH_LONG).show();
        }
    }
}