package com.example.protpetverso_1.pet;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

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
 * 1) MODO_RASCUNHO = true  → aberta no cadastro, SEM chamar API.
 *    Voltar / Salvar devolve os dados para a PetSignUpActivity.
 * 2) MODO_RASCUNHO = false → pet já existe (PET_ID); uso futuro para PUT.
 *
 * Chips:
 * - Selecionados: fundo #DF945D, texto branco, ícone X (remover)
 * - Sugestões: fundo #F7D4A0, texto preto, ícone + (adicionar)
 * - Nada vem pré-selecionado, só o que o usuário escolheu (ou o que voltou no Intent)
 *
 * Toolbar: edge-to-edge — laranja sobe até o topo (atrás da status bar).
 */
public class MaisInfoPetActivity extends AppCompatActivity {

    private ChipGroup chipGroupPersonalidades;
    private TextInputEditText edtNovaPersonalidade, edtSensibilidades;
    private MaterialButton btnAdicionarCaracteristica, btnFinalizarCadastro;

    private boolean modoRascunho = true;

    /** Só o que o usuário escolheu (vai para a API / devolve no Intent) */
    private final List<String> personalidadesSelecionadas = new ArrayList<>();

    private static final String[] SUGESTOES = {
            "Alegre", "Curioso", "Medroso", "Gosta de carinho",
            "Odeia banho", "Bravo", "Ansioso", "Brincalhão"
    };

    private static final int COR_SELECIONADO = Color.parseColor("#DF945D");
    private static final int COR_SUGESTAO = Color.parseColor("#F7D4A0");
    private static final int COR_BRANCO = Color.WHITE;
    private static final int COR_PRETO = Color.BLACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Conteúdo atrás da status bar (toolbar laranja até o topo)
        EdgeToEdge.enable(this);

        setContentView(R.layout.mais_info_pet_layout);

        // Ícones da status bar claros sobre o laranja
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(false);

        MaterialToolbar toolbar = findViewById(R.id.toolbarMaisInfo);
        View scroll = findViewById(R.id.scrollMaisInfo);

        // Padding TOP só na toolbar → laranja preenche até o topo
        if (toolbar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                v.setPadding(
                        v.getPaddingLeft(),
                        bars.top,
                        v.getPaddingRight(),
                        v.getPaddingBottom()
                );
                return insets;
            });
            toolbar.setNavigationOnClickListener(v -> devolverResultadoEFechar());
        }

        // Padding BOTTOM no scroll (barra de navegação + teclado)
        if (scroll != null) {
            ViewCompat.setOnApplyWindowInsetsListener(scroll, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
                int bottom = Math.max(bars.bottom, ime.bottom);
                v.setPadding(
                        v.getPaddingLeft(),
                        v.getPaddingTop(),
                        v.getPaddingRight(),
                        bottom
                );
                return insets;
            });
        }

        modoRascunho = getIntent().getBooleanExtra("MODO_RASCUNHO", true);

        chipGroupPersonalidades = findViewById(R.id.chipGroupPersonalidades);
        edtNovaPersonalidade = findViewById(R.id.edtNovaPersonalidade);
        edtSensibilidades = findViewById(R.id.edtSensibilidades);
        btnAdicionarCaracteristica = findViewById(R.id.btnAdicionarCaracteristica);
        btnFinalizarCadastro = findViewById(R.id.btnFinalizarCadastro);

        // Restaura só o que o usuário já tinha escolhido
        ArrayList<String> anteriores = getIntent().getStringArrayListExtra("PERSONALIDADES");
        if (anteriores != null) {
            personalidadesSelecionadas.addAll(anteriores);
        }

        String sens = getIntent().getStringExtra("SENSIBILIDADE");
        if (sens != null && edtSensibilidades != null) {
            edtSensibilidades.setText(sens);
        }

        montarChips();

        if (btnAdicionarCaracteristica != null) {
            btnAdicionarCaracteristica.setOnClickListener(v -> adicionarPersonalidadeDigitada());
        }

        if (btnFinalizarCadastro != null) {
            btnFinalizarCadastro.setText(modoRascunho ? "Salvar informações" : "Finalizar cadastro");
            btnFinalizarCadastro.setOnClickListener(v -> devolverResultadoEFechar());
        }
    }

    /**
     * Monta a lista de chips:
     * 1) personalidadesSelecionadas → chips escuros com X
     * 2) SUGESTOES que ainda não estão na lista → chips claros com +
     */
    private void montarChips() {
        if (chipGroupPersonalidades == null) return;
        chipGroupPersonalidades.removeAllViews();

        for (String texto : personalidadesSelecionadas) {
            chipGroupPersonalidades.addView(criarChipSelecionado(texto));
        }

        for (String texto : SUGESTOES) {
            if (!personalidadesSelecionadas.contains(texto)) {
                chipGroupPersonalidades.addView(criarChipSugestao(texto));
            }
        }
    }

    /** Chip selecionado: fundo #DF945D, texto branco, X para remover */
    private Chip criarChipSelecionado(String texto) {
        Chip chip = new Chip(this);
        chip.setText(texto);
        chip.setCheckable(false);
        chip.setClickable(true);
        chip.setCheckedIconVisible(false);
        chip.setCloseIconVisible(true);

        chip.setChipBackgroundColor(ColorStateList.valueOf(COR_SELECIONADO));
        chip.setTextColor(COR_BRANCO);
        chip.setCloseIconTint(ColorStateList.valueOf(COR_BRANCO));

        chip.setOnCloseIconClickListener(v -> {
            personalidadesSelecionadas.remove(texto);
            montarChips();
        });

        return chip;
    }

    /** Chip sugestão: fundo #F7D4A0, texto preto, + para adicionar */
    private Chip criarChipSugestao(String texto) {
        Chip chip = new Chip(this);
        chip.setText(texto);
        chip.setCheckable(false);
        chip.setClickable(true);
        chip.setCheckedIconVisible(false);
        chip.setCloseIconVisible(false);

        chip.setChipIconResource(android.R.drawable.ic_input_add);
        chip.setChipIconVisible(true);
        chip.setChipIconTint(ColorStateList.valueOf(COR_PRETO));

        chip.setChipBackgroundColor(ColorStateList.valueOf(COR_SUGESTAO));
        chip.setTextColor(COR_PRETO);

        chip.setOnClickListener(v -> {
            if (!personalidadesSelecionadas.contains(texto)) {
                personalidadesSelecionadas.add(texto);
                montarChips();
            }
        });

        return chip;
    }

    /** Digita uma característica e adiciona como selecionada */
    private void adicionarPersonalidadeDigitada() {
        String texto = edtNovaPersonalidade != null
                ? String.valueOf(edtNovaPersonalidade.getText()).trim()
                : "";

        if (texto.isEmpty()) {
            Toast.makeText(this, "Digite uma característica", Toast.LENGTH_SHORT).show();
            return;
        }
        if (personalidadesSelecionadas.contains(texto)) {
            Toast.makeText(this, "Já adicionada", Toast.LENGTH_SHORT).show();
            return;
        }

        personalidadesSelecionadas.add(texto);
        if (edtNovaPersonalidade != null) {
            edtNovaPersonalidade.setText("");
        }
        montarChips();
    }

    /**
     * Devolve personalidades + sensibilidade para a PetSignUpActivity e fecha.
     * Não cadastra pet e não navega para a Home.
     */
    private void devolverResultadoEFechar() {
        Intent data = new Intent();
        data.putStringArrayListExtra(
                "PERSONALIDADES",
                new ArrayList<>(personalidadesSelecionadas)
        );
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