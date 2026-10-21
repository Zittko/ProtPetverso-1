package com.example.protpetverso_1.vaccine;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

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

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Cadastro / edição de vacina ou imunização.
 * POST /api/pets/{idPet}/vacinas
 * PUT  /api/vacinas/{idVacina}
 *
 * Intervalos do spinner (uso real):
 * - 1 mês: vermífugo/prevenção mensal, risco alto
 * - 3 meses: vermífugo adulto clássico
 * - 6 meses: reforços intermediários
 * - 1 ano: vacinas anuais (não essenciais, raiva 1 ano)
 * - 3 anos: vacinas essenciais (adulto)
 * Dose única: não envia dataProximaDose.
 */
public class CadastrarVacinaActivity extends AppCompatActivity {

    private static final String TAG = "CADASTRAR_VACINA";

    /** Rótulos do spinner — ordem deve bater com {@link #mesesDoIntervalo(int)}. */
    private static final String[] INTERVALOS = {
            "1 Mês",
            "3 Meses",
            "6 Meses",
            "1 Ano",
            "3 Anos"
    };

    private TextInputEditText edtNome, edtDataAplicacao, edtObservacao;
    private AppCompatSpinner spinnerDosagemUnica, spinnerIntervalo;
    private MaterialButton btnConfirmar;
    private View lblAplicarNovamente;
    private ImageView btnCalendarioVacina;

    private SessionManager sessionManager;
    private long petId = -1;
    private long vacinaId = -1;
    private LocalDate dataAplicacao = LocalDate.now();

    private final DateTimeFormatter fmtTela =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter fmtApi =
            DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastrar_vacina_layout);

        sessionManager = new SessionManager(this);
        petId = getIntent().getLongExtra("PET_ID", sessionManager.obterPetId());
        vacinaId = getIntent().getLongExtra("VACINA_ID", -1);

        MaterialToolbar toolbar = findViewById(R.id.toolbarCadastrarVacina);
        if (toolbar != null) {
            if (vacinaId > 0) {
                toolbar.setTitle("Editar Imunidade");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        edtNome = findViewById(R.id.edtNomeVacina);
        edtDataAplicacao = findViewById(R.id.edtDataAplicacao);
        edtObservacao = findViewById(R.id.edtObservacao);
        spinnerDosagemUnica = findViewById(R.id.spinnerDosagemUnica);
        spinnerIntervalo = findViewById(R.id.spinnerIntervalo);
        lblAplicarNovamente = findViewById(R.id.lblAplicarNovamente);
        btnConfirmar = findViewById(R.id.btnConfirmarVacina);
        btnCalendarioVacina = findViewById(R.id.btnCalendarioVacina);

        ArrayAdapter<String> adSimNao = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Não", "Sim"}
        );
        if (spinnerDosagemUnica != null) {
            spinnerDosagemUnica.setAdapter(adSimNao);
            spinnerDosagemUnica.setOnItemSelectedListener(
                    new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent,
                                                   View view, int position, long id) {
                            boolean unica = position == 1; // Sim
                            int vis = unica ? View.GONE : View.VISIBLE;
                            if (spinnerIntervalo != null) {
                                spinnerIntervalo.setVisibility(vis);
                            }
                            if (lblAplicarNovamente != null) {
                                lblAplicarNovamente.setVisibility(vis);
                            }
                        }

                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent) {
                        }
                    });
        }

        ArrayAdapter<String> adIntervalo = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                INTERVALOS
        );
        if (spinnerIntervalo != null) {
            spinnerIntervalo.setAdapter(adIntervalo);
            spinnerIntervalo.setSelection(1); // 3 Meses — padrão mais comum em vermífugo adulto
        }

        if (edtDataAplicacao != null) {
            edtDataAplicacao.setText(dataAplicacao.format(fmtTela));
            edtDataAplicacao.setOnClickListener(v -> abrirDatePicker());
        }
        if (btnCalendarioVacina != null) {
            btnCalendarioVacina.setOnClickListener(v -> abrirDatePicker());
        }

        if (vacinaId > 0) {
            preencherModoEdicao();
        }

        if (btnConfirmar != null) {
            btnConfirmar.setOnClickListener(v -> salvar());
        }
    }

    private void preencherModoEdicao() {
        String nome = getIntent().getStringExtra("NOME");
        String da = getIntent().getStringExtra("DATA_APLICACAO");
        String dp = getIntent().getStringExtra("DATA_PROXIMA");
        String obs = getIntent().getStringExtra("OBSERVACAO");

        if (edtNome != null && nome != null) {
            edtNome.setText(nome);
        }
        if (da != null && !da.isEmpty()) {
            try {
                dataAplicacao = LocalDate.parse(da);
                if (edtDataAplicacao != null) {
                    edtDataAplicacao.setText(dataAplicacao.format(fmtTela));
                }
            } catch (Exception ignored) {
            }
        }
        if (edtObservacao != null && obs != null) {
            edtObservacao.setText(obs);
        }

        if (dp == null || dp.isEmpty()) {
            if (spinnerDosagemUnica != null) {
                spinnerDosagemUnica.setSelection(1); // dose única
            }
        } else {
            if (spinnerDosagemUnica != null) {
                spinnerDosagemUnica.setSelection(0);
            }
            // Tenta marcar o intervalo mais próximo pela diferença em meses
            try {
                LocalDate prox = LocalDate.parse(dp);
                long meses = java.time.temporal.ChronoUnit.MONTHS.between(dataAplicacao, prox);
                if (spinnerIntervalo != null) {
                    spinnerIntervalo.setSelection(indicePeloMeses((int) meses));
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Converte posição do spinner em meses a somar na data de aplicação.
     * 0→1, 1→3, 2→6, 3→12, 4→36
     */
    private int mesesDoIntervalo(int posicaoSpinner) {
        switch (posicaoSpinner) {
            case 0:
                return 1;
            case 1:
                return 3;
            case 2:
                return 6;
            case 3:
                return 12;
            case 4:
                return 36;
            default:
                return 3;
        }
    }

    /** Para edição: escolhe o índice do spinner mais próximo dos meses reais. */
    private int indicePeloMeses(int meses) {
        if (meses <= 1) return 0;
        if (meses <= 3) return 1;
        if (meses <= 6) return 2;
        if (meses <= 12) return 3;
        return 4; // 3 anos
    }

    private void abrirDatePicker() {
        Calendar c = Calendar.getInstance();
        c.set(
                dataAplicacao.getYear(),
                dataAplicacao.getMonthValue() - 1,
                dataAplicacao.getDayOfMonth()
        );

        DatePickerDialog dlg = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    dataAplicacao = LocalDate.of(year, month + 1, dayOfMonth);
                    if (dataAplicacao.isAfter(LocalDate.now())) {
                        Toast.makeText(
                                this,
                                "A data de aplicação não pode ser futura.",
                                Toast.LENGTH_SHORT
                        ).show();
                        dataAplicacao = LocalDate.now();
                    }
                    if (edtDataAplicacao != null) {
                        edtDataAplicacao.setText(dataAplicacao.format(fmtTela));
                    }
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        dlg.getDatePicker().setMaxDate(System.currentTimeMillis());
        dlg.show();
    }

    private void salvar() {
        String nome = edtNome != null ? String.valueOf(edtNome.getText()).trim() : "";
        if (nome.isEmpty()) {
            Toast.makeText(this, "Informe o nome.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (petId <= 0) {
            Toast.makeText(this, "Pet inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean doseUnica = spinnerDosagemUnica != null
                && spinnerDosagemUnica.getSelectedItemPosition() == 1;

        LocalDate dataProxima = null;
        if (!doseUnica && spinnerIntervalo != null) {
            int meses = mesesDoIntervalo(spinnerIntervalo.getSelectedItemPosition());
            dataProxima = dataAplicacao.plusMonths(meses);
        }

        String obs = edtObservacao != null
                ? String.valueOf(edtObservacao.getText()).trim()
                : "";

        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Sessão expirada.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnConfirmar != null) {
            btnConfirmar.setEnabled(false);
        }

        try {
            JSONObject body = new JSONObject();
            body.put("nome", nome);
            body.put("dataAplicacao", dataAplicacao.format(fmtApi));
            if (dataProxima != null) {
                body.put("dataProximaDose", dataProxima.format(fmtApi));
            } else {
                body.put("dataProximaDose", JSONObject.NULL);
            }
            body.put("observacao", obs.isEmpty() ? JSONObject.NULL : obs);

            boolean edicao = vacinaId > 0;
            String url = edicao
                    ? ApiConfig.URL_VACINA + vacinaId
                    : ApiConfig.URL_VACINAS_DO_PET + petId + "/vacinas";
            int method = edicao ? Request.Method.PUT : Request.Method.POST;

            Log.d(TAG, (edicao ? "PUT " : "POST ") + url
                    + " aplicacao=" + dataAplicacao
                    + " proxima=" + dataProxima);

            JsonObjectRequest request = new JsonObjectRequest(
                    method,
                    url,
                    body,
                    response -> {
                        Toast.makeText(
                                this,
                                edicao ? "Vacina atualizada!" : "Vacina cadastrada!",
                                Toast.LENGTH_SHORT
                        ).show();
                        AppDataCache.get().invalidarVacinas(petId);
                        finish();
                    },
                    error -> {
                        if (btnConfirmar != null) {
                            btnConfirmar.setEnabled(true);
                        }
                        String msg = "Erro ao salvar.";
                        if (error.networkResponse != null) {
                            int s = error.networkResponse.statusCode;
                            if (s == 403) {
                                msg = "Apenas o tutor principal pode cadastrar/editar vacinas.";
                            } else if (s == 400) {
                                msg = "Dados inválidos. Verifique as datas.";
                                try {
                                    String err = new String(
                                            error.networkResponse.data,
                                            java.nio.charset.StandardCharsets.UTF_8
                                    );
                                    Log.e(TAG, err);
                                    String lower = err.toLowerCase();
                                    if (lower.contains("aplicacao")) {
                                        msg = "A data de aplicação não pode ser futura.";
                                    } else if (lower.contains("proxima")) {
                                        msg = "A próxima dose não pode ser anterior à aplicação.";
                                    }
                                } catch (Exception ignored) {
                                }
                            } else {
                                msg += " (" + s + ")";
                            }
                        } else {
                            msg = "Sem conexão com o servidor.";
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
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

            VolleySingleton.getInstance(this).addToRequestQueue(request);
        } catch (Exception e) {
            if (btnConfirmar != null) {
                btnConfirmar.setEnabled(true);
            }
            e.printStackTrace();
            Toast.makeText(this, "Erro ao montar dados.", Toast.LENGTH_SHORT).show();
        }
    }
}