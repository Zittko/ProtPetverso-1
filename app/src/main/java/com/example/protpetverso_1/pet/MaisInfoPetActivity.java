package com.example.protpetverso_1.pet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.protpetverso_1.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Tela de personalidade e sensibilidades.
 *
 * Dois modos:
 * 1) MODO_RASCUNHO = true  → aberta a partir do cadastro, SEM chamar API.
 *    Voltar / Salvar devolve os dados para a PetSignUpActivity.
 * 2) MODO_RASCUNHO = false → pet já existe (PET_ID); pode ser usado no futuro para PUT direto.
 */
public class MaisInfoPetActivity extends AppCompatActivity {

    private ChipGroup chipGroupPersonalidades;
    private TextInputEditText edtNovaPersonalidade, edtSensibilidades;
    private MaterialButton btnAdicionarCaracteristica, btnFinalizarCadastro;

    private boolean modoRascunho = true;
    private final List<String> personalidadesSelecionadas = new ArrayList<>();

    private final String[] SUGESTOES = {
            "Alegre", "Curioso", "Medroso", "Gosta de carinho",
            "Odeia banho", "Bravo", "Ansioso", "Brincalhão"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mais_info_pet_layout);

        modoRascunho = getIntent().getBooleanExtra("MODO_RASCUNHO", true);

        MaterialToolbar toolbar = findViewById(R.id.toolbarMaisInfo);
        // Voltar → devolve o que está na tela e fecha (NÃO vai para Home)
        toolbar.setNavigationOnClickListener(v -> devolverResultadoEFechar());

        chipGroupPersonalidades = findViewById(R.id.chipGroupPersonalidades);
        edtNovaPersonalidade = findViewById(R.id.edtNovaPersonalidade);
        edtSensibilidades = findViewById(R.id.edtSensibilidades);
        btnAdicionarCaracteristica = findViewById(R.id.btnAdicionarCaracteristica);
        btnFinalizarCadastro = findViewById(R.id.btnFinalizarCadastro);

        // Restaura o que o usuário já tinha escolhido antes
        ArrayList<String> anteriores = getIntent().getStringArrayListExtra("PERSONALIDADES");
        if (anteriores != null) {
            personalidadesSelecionadas.addAll(anteriores);
        }
        String sens = getIntent().getStringExtra("SENSIBILIDADE");
        if (sens != null && edtSensibilidades != null) {
            edtSensibilidades.setText(sens);
        }

        montarChipsSugestao();

        btnAdicionarCaracteristica.setOnClickListener(v -> adicionarPersonalidadeDigitada());

        // No rascunho: só devolve dados. Não chama API.
        btnFinalizarCadastro.setText(modoRascunho ? "Salvar informações" : "Finalizar cadastro");
        btnFinalizarCadastro.setOnClickListener(v -> devolverResultadoEFechar());
    }

    /** Cria chips de sugestão e marca os que já estavam na lista. */
    private void montarChipsSugestao() {
        chipGroupPersonalidades.removeAllViews();
        for (String texto : SUGESTOES) {
            Chip chip = new Chip(this);
            chip.setText(texto);
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setChecked(personalidadesSelecionadas.contains(texto));
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

        // Chips personalizados (digitados antes) que não estão nas sugestões
        for (String texto : new ArrayList<>(personalidadesSelecionadas)) {
            boolean ehSugestao = false;
            for (String s : SUGESTOES) {
                if (s.equals(texto)) {
                    ehSugestao = true;
                    break;
                }
            }
            if (!ehSugestao) {
                adicionarChipCustom(texto);
            }
        }
    }

    /** Digita uma característica e adiciona como chip removível. */
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
        adicionarChipCustom(texto);
        edtNovaPersonalidade.setText("");
    }

    private void adicionarChipCustom(String texto) {
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
    }

    /**
     * Devolve personalidades + sensibilidade para a PetSignUpActivity e fecha.
     * Não cadastra pet e não navega para a Home.
     */
    private void devolverResultadoEFechar() {
        Intent data = new Intent();
        data.putStringArrayListExtra("PERSONALIDADES", new ArrayList<>(personalidadesSelecionadas));
        String sens = edtSensibilidades != null
                ? String.valueOf(edtSensibilidades.getText()).trim()
                : "";
        data.putExtra("SENSIBILIDADE", sens);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onBackPressed() {
        devolverResultadoEFechar();
    }
}