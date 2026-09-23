package com.example.protpetverso_1.pet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.MenuActivity;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.VolleySingleton;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaisInfoPetActivity extends AppCompatActivity {

    private ChipGroup chipGroupPersonalidades;
    private TextInputEditText edtNovaPersonalidade, edtSensibilidades;
    private MaterialButton btnAdicionarCaracteristica, btnFinalizarCadastro;

    private SessionManager sessionManager;
    private long petId = -1;

    // personalidades escolhidas (chips marcados + digitadas)
    private final List<String> personalidadesSelecionadas = new ArrayList<>();

    private final String[] SUGESTOES = {
            "Alegre", "Curioso", "Medroso", "Gosta de carinho",
            "Odeia banho", "Bravo", "Ansioso", "Brincalhão"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mais_info_pet_layout);

        sessionManager = new SessionManager(this);
        petId = getIntent().getLongExtra("PET_ID", -1);
        if (petId <= 0) petId = sessionManager.obterPetId();

        MaterialToolbar toolbar = findViewById(R.id.toolbarMaisInfo);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(MaisInfoPetActivity.this, MenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        chipGroupPersonalidades = findViewById(R.id.chipGroupPersonalidades);
        edtNovaPersonalidade = findViewById(R.id.edtNovaPersonalidade);
        edtSensibilidades = findViewById(R.id.edtSensibilidades);
        btnAdicionarCaracteristica = findViewById(R.id.btnAdicionarCaracteristica);
        btnFinalizarCadastro = findViewById(R.id.btnFinalizarCadastro);

        montarChipsSugestao();

        btnAdicionarCaracteristica.setOnClickListener(v -> adicionarPersonalidadeDigitada());
        btnFinalizarCadastro.setOnClickListener(v -> enviarParaApi());
    }

    /** Cria chips de sugestão (clique marca/desmarca). */
    private void montarChipsSugestao() {
        chipGroupPersonalidades.removeAllViews();
        for (String texto : SUGESTOES) {
            Chip chip = new Chip(this);
            chip.setText(texto);
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!personalidadesSelecionadas.contains(texto)) {
                        personalidadesSelecionadas.add(texto);
                    }
                } else {
                    personalidadesSelecionadas.remove(texto);
                }
            });
            chipGroupPersonalidades.addView(chip);
        }
    }

    /**
     * Campo único: digita uma característica e adiciona na lista (como chip).
     */
    private void adicionarPersonalidadeDigitada() {
        String texto = String.valueOf(edtNovaPersonalidade.getText()).trim();
        if (texto.isEmpty()) {
            Toast.makeText(this, "Digite uma característica", Toast.LENGTH_SHORT).show();
            return;
        }
        if (personalidadesSelecionadas.contains(texto)) {
            Toast.makeText(this, "Já adicionada", Toast.LENGTH_SHORT).show();
            return;
        }

        personalidadesSelecionadas.add(texto);

        Chip chip = new Chip(this);
        chip.setText(texto);
        chip.setCheckable(true);
        chip.setChecked(true);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            personalidadesSelecionadas.remove(texto);
            chipGroupPersonalidades.removeView(chip);
        });
        chipGroupPersonalidades.addView(chip);

        edtNovaPersonalidade.setText("");
    }

    /** PUT personalidades + sensibilidades na API. */
    private void enviarParaApi() {
        if (petId <= 0) {
            Toast.makeText(this, "Pet não identificado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Sessão expirada.", Toast.LENGTH_SHORT).show();
            return;
        }

        String sensibilidade = String.valueOf(edtSensibilidades.getText()).trim();

        try {
            JSONObject body = new JSONObject();
            body.put("perfilDeSensibilidade", sensibilidade);

            JSONArray arr = new JSONArray();
            for (String p : personalidadesSelecionadas) {
                arr.put(p);
            }
            body.put("personalidades", arr);

            String url = ApiConfig.URL_ATUALIZAR_PET_PERFIL + petId + "/atualizarPetPerfil";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    body,
                    response -> {
                        Toast.makeText(this, "Cadastro finalizado!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MaisInfoPetActivity.this, MenuActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    },
                    error -> Toast.makeText(this, "Erro ao salvar informações.", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> h = new HashMap<>();
                    h.put("Authorization", "Bearer " + token);
                    h.put("Content-Type", "application/json");
                    return h;
                }
            };

            VolleySingleton.getInstance(this).addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao montar dados.", Toast.LENGTH_SHORT).show();
        }
    }
}