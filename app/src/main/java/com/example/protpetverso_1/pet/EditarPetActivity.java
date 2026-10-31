package com.example.protpetverso_1.pet;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.VolleySingleton;
import com.example.protpetverso_1.data.AppDataCache;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditarPetActivity extends AppCompatActivity {

    private static final String[] PORTES_TELA = {"Pequeno", "Médio", "Grande"};
    private static final String[] PORTES_API = {"PEQUENO", "MEDIO", "GRANDE"};
    private static final String[] SEXOS_TELA = {"Macho", "Fêmea"};
    private static final String[] SEXOS_API = {"MACHO", "FEMEA"};
    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private long petId;
    private LocalDate nascimento = LocalDate.now();
    private SessionManager session;
    private ScrollView scrollEditarPet;
    private TextInputEditText edtNome, edtRaca, edtEspecie, edtPeso, edtNascimento, edtSensibilidade, edtPersonalidade;
    private Spinner spPorte, spSexo;
    private MaterialButton btnSalvar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_pet);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);

        session = new SessionManager(this);
        petId = getIntent().getLongExtra("PET_ID", session.obterPetId());

        MaterialToolbar toolbar = findViewById(R.id.toolbarEditarPet);
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            return insets;
        });
        toolbar.setNavigationOnClickListener(v -> finish());

        scrollEditarPet = findViewById(R.id.scrollEditarPet);
        edtNome = findViewById(R.id.edtNomePet);
        edtRaca = findViewById(R.id.edtRaca);
        edtEspecie = findViewById(R.id.edtEspecie);
        edtPeso = findViewById(R.id.edtPeso);
        edtNascimento = findViewById(R.id.edtNascimento);
        edtSensibilidade = findViewById(R.id.edtSensibilidade);
        edtPersonalidade = findViewById(R.id.edtPersonalidade);
        spPorte = findViewById(R.id.spPorte);
        spSexo = findViewById(R.id.spSexo);
        btnSalvar = findViewById(R.id.btnSalvarPet);

        ArrayAdapter<String> adPorte = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, PORTES_TELA);
        spPorte.setAdapter(adPorte);
        ArrayAdapter<String> adSexo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, SEXOS_TELA);
        spSexo.setAdapter(adSexo);

        preencher();
        configurarScrollComTeclado();
        edtNascimento.setOnClickListener(v -> abrirData());
        btnSalvar.setOnClickListener(v -> salvar());
    }

    private void configurarScrollComTeclado() {
        if (scrollEditarPet == null) return;
        ViewCompat.setOnApplyWindowInsetsListener(scrollEditarPet, (v, insets) -> {
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, Math.max(ime.bottom, bars.bottom));
            if (ime.bottom > 0) rolarAteFoco();
            return insets;
        });
        View.OnFocusChangeListener foco = (view, hasFocus) -> {
            if (hasFocus) rolarAteFoco();
        };
        TextInputEditText[] campos = {edtNome, edtRaca, edtEspecie, edtPeso, edtSensibilidade, edtPersonalidade};
        for (TextInputEditText campo : campos) {
            if (campo != null) campo.setOnFocusChangeListener(foco);
        }
    }

    private void rolarAteFoco() {
        if (scrollEditarPet == null) return;
        scrollEditarPet.post(() -> {
            View alvo = scrollEditarPet.findFocus();
            if (alvo == null) return;
            Rect r = new Rect();
            alvo.getDrawingRect(r);
            scrollEditarPet.offsetDescendantRectToMyCoords(alvo, r);
            int extra = (int) (24 * getResources().getDisplayMetrics().density);
            int y = r.bottom - scrollEditarPet.getHeight() + scrollEditarPet.getPaddingBottom() + extra;
            if (y < 0) y = 0;
            scrollEditarPet.smoothScrollTo(0, y);
        });
    }

    private void preencher() {
        edtNome.setText(getIntent().getStringExtra("NOME"));
        String racaPeso = getIntent().getStringExtra("RACA_PESO");
        if (racaPeso != null && racaPeso.contains("|")) {
            String[] p = racaPeso.split("\\|");
            edtRaca.setText(p[0].trim());
            String peso = p[1].replace("Kg", "").replace("kg", "").trim();
            edtPeso.setText(peso);
        }
        edtEspecie.setText(getIntent().getStringExtra("ESPECIE"));
        edtSensibilidade.setText(limparTraco(getIntent().getStringExtra("SENSIBILIDADE")));
        edtPersonalidade.setText(limparTraco(getIntent().getStringExtra("PERSONALIDADE")));

        String porte = getIntent().getStringExtra("PORTE");
        if (porte != null && porte.toLowerCase(Locale.ROOT).startsWith("m")) spPorte.setSelection(1);
        else if (porte != null && porte.toLowerCase(Locale.ROOT).startsWith("g")) spPorte.setSelection(2);
        else spPorte.setSelection(0);

        String sexo = getIntent().getStringExtra("SEXO");
        spSexo.setSelection(sexo != null && sexo.toLowerCase(Locale.ROOT).startsWith("f") ? 1 : 0);

        String nasc = getIntent().getStringExtra("NASCIMENTO");
        if (nasc != null && nasc.contains("/")) {
            try {
                nascimento = LocalDate.parse(nasc.substring(0, 10), BR);
            } catch (Exception ignored) {}
        }
        edtNascimento.setText(nascimento.format(BR));
    }

    private void abrirData() {
        new DatePickerDialog(this, (view, y, m, d) -> {
            nascimento = LocalDate.of(y, m + 1, d);
            edtNascimento.setText(nascimento.format(BR));
        }, nascimento.getYear(), nascimento.getMonthValue() - 1, nascimento.getDayOfMonth()).show();
    }

    private void salvar() {
        String nome = texto(edtNome);
        String raca = texto(edtRaca);
        String especie = texto(edtEspecie);
        String pesoTxt = texto(edtPeso).replace(",", ".");
        if (nome.isEmpty() || raca.isEmpty() || especie.isEmpty() || pesoTxt.isEmpty() || petId <= 0) {
            Toast.makeText(this, "Preencha nome, raça, espécie e peso.", Toast.LENGTH_SHORT).show();
            return;
        }
        double peso;
        try { peso = Double.parseDouble(pesoTxt); }
        catch (NumberFormatException e) {
            Toast.makeText(this, "Peso inválido.", Toast.LENGTH_SHORT).show();
            return;
        }
        btnSalvar.setEnabled(false);
        String porte = PORTES_API[spPorte.getSelectedItemPosition()];
        String sexo = SEXOS_API[spSexo.getSelectedItemPosition()];
        try {
            JSONObject body = new JSONObject();
            body.put("nome", nome);
            body.put("raca", raca);
            body.put("especie", especie);
            body.put("dataDeNascimento", nascimento.toString());
            body.put("porte", porte);
            body.put("peso", peso);
            body.put("sexo", sexo);
            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.PUT, ApiConfig.URL_PET_DADOS + petId + "/dados", body,
                    response -> enviarClinico(nome, raca, especie, pesoTxt, sexo, porte),
                    error -> falha(error, "Não foi possível salvar os dados do pet.")
            ) {
                @Override public Map<String, String> getHeaders() { return auth(); }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(req);
        } catch (Exception e) {
            btnSalvar.setEnabled(true);
        }
    }

    private void enviarClinico(String nome, String raca, String especie, String peso, String sexo, String porte) {
        try {
            JSONObject body = new JSONObject();
            body.put("perfilDeSensibilidade", texto(edtSensibilidade));
            JSONArray pers = new JSONArray();
            for (String item : texto(edtPersonalidade).split(",")) {
                String t = item.trim();
                if (!t.isEmpty()) pers.put(t);
            }
            body.put("personalidades", pers);
            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.PUT,
                    ApiConfig.URL_ATUALIZAR_PERFIL_PET + petId + "/atualizarPetPerfil",
                    body,
                    response -> concluir(nome, raca, especie, peso, sexo, porte),
                    error -> {
                        btnSalvar.setEnabled(true);
                        Toast.makeText(this, "Dados salvos. Sensibilidade só o tutor principal altera.", Toast.LENGTH_LONG).show();
                        concluir(nome, raca, especie, peso, sexo, porte);
                    }
            ) {
                @Override public Map<String, String> getHeaders() { return auth(); }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(req);
        } catch (Exception e) {
            btnSalvar.setEnabled(true);
        }
    }

    private void concluir(String nome, String raca, String especie, String peso, String sexo, String porte) {
        session.salvarPet(petId, nome, raca, especie, peso, sexo, porte, nascimento.format(BR));
        try {
            JSONObject cache = AppDataCache.get().getPerfilPet(petId);
            if (cache == null) cache = new JSONObject();
            cache.put("nome", nome);
            cache.put("raca", raca);
            cache.put("especie", especie);
            cache.put("peso", Double.parseDouble(peso));
            cache.put("sexo", sexo);
            cache.put("porte", porte);
            cache.put("idade", nascimento.toString());
            cache.put("dataDeNascimento", nascimento.toString());
            cache.put("perfilDeSensibilidade", texto(edtSensibilidade));
            AppDataCache.get().setPerfilPet(petId, cache);
        } catch (Exception ignored) {}
        Toast.makeText(this, "Pet atualizado.", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void falha(com.android.volley.VolleyError error, String padrao) {
        btnSalvar.setEnabled(true);
        String msg = padrao;
        if (error != null && error.networkResponse != null) {
            if (error.networkResponse.statusCode == 403) msg = "Sem permissão para alterar este pet.";
            else if (error.networkResponse.data != null) {
                String b = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                if (!b.isEmpty() && b.length() < 160) msg = b;
            }
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private Map<String, String> auth() {
        Map<String, String> h = new HashMap<>();
        h.put("Authorization", "Bearer " + session.obterToken());
        h.put("Content-Type", "application/json");
        return h;
    }

    private String texto(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private String limparTraco(String s) {
        if (s == null || "—".equals(s.trim())) return "";
        return s;
    }
}