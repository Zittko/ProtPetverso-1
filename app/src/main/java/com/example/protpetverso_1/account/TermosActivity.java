package com.example.protpetverso_1.account;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.example.protpetverso_1.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Exibe os Termos de Uso ou a Política de Privacidade / Consentimento.
 * O botão "Li e aceito" só é liberado quando o usuário rola até o final.
 * Devolve RESULT_OK ao ser aceito; RESULT_CANCELED caso contrário.
 */
public class TermosActivity extends AppCompatActivity {

    public static final String EXTRA_TIPO = "tipo";
    public static final int TIPO_TERMOS = 0;
    public static final int TIPO_PRIVACIDADE = 1;

    private static final String STATE_LIBERADO = "liberado";

    private NestedScrollView scroll;
    private Button btnAceitar;
    private TextView tvAviso;
    private boolean liberado = false;

    public static Intent criarIntent(Context context, int tipo) {
        return new Intent(context, TermosActivity.class).putExtra(EXTRA_TIPO, tipo);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_termos);

        int tipo = getIntent().getIntExtra(EXTRA_TIPO, TIPO_TERMOS);

        View raiz = findViewById(R.id.toolbarTermos).getParent() instanceof View
                ? (View) findViewById(R.id.toolbarTermos).getParent() : null;
        if (raiz != null) {
            ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            });
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbarTermos);
        scroll = findViewById(R.id.scrollTermos);
        btnAceitar = findViewById(R.id.btnAceitar);
        tvAviso = findViewById(R.id.tvAvisoRolagem);
        TextView conteudo = findViewById(R.id.tvConteudoTermos);

        toolbar.setTitle(tipo == TIPO_PRIVACIDADE ? "Política de Privacidade" : "Termos de Uso");
        toolbar.setNavigationOnClickListener(v -> recusar());
        findViewById(R.id.btnRecusar).setOnClickListener(v -> recusar());

        conteudo.setText(lerTexto(tipo == TIPO_PRIVACIDADE
                ? R.raw.politica_privacidade : R.raw.termos_uso));

        btnAceitar.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { recusar(); }
        });

        if (savedInstanceState != null && savedInstanceState.getBoolean(STATE_LIBERADO)) {
            liberar();
        }

        scroll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                (v, x, y, oldX, oldY) -> verificarFim());
        // Se o texto couber na tela sem rolagem, libera já
        scroll.post(this::verificarFim);
    }

    private void verificarFim() {
        if (liberado) return;
        View filho = scroll.getChildAt(0);
        if (filho == null || filho.getHeight() == 0) return;
        int fim = filho.getHeight() - scroll.getHeight();
        if (fim <= 0 || scroll.getScrollY() >= fim - 8) {
            liberar();
        }
    }

    private void liberar() {
        liberado = true;
        btnAceitar.setEnabled(true);
        tvAviso.setText("Você chegou ao final. Toque em \"Li e aceito\" para concordar.");
    }

    private void recusar() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private String lerTexto(int resId) {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = getResources().openRawResource(resId);
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = br.readLine()) != null) sb.append(linha).append('\n');
        } catch (Exception e) {
            sb.append("Não foi possível carregar o texto.");
        }
        return sb.toString();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_LIBERADO, liberado);
    }
}