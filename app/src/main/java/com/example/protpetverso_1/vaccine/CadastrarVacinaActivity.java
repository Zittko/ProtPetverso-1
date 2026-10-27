package com.example.protpetverso_1.vaccine;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * POST /api/pets/{idPet}/vacinas
 * PUT  /api/vacinas/{idVacina}
 */
public class CadastrarVacinaActivity extends AppCompatActivity {

    private static final String TAG = "CADASTRAR_VACINA";

    public static final String EXTRA_PET_ID = "PET_ID";
    public static final String EXTRA_ID_VACINA = "ID_VACINA";
    public static final String EXTRA_MODO_EDICAO = "MODO_EDICAO";
    public static final String EXTRA_NOME = "NOME";
    public static final String EXTRA_DATA = "DATA";
    public static final String EXTRA_OBS = "OBS";
    public static final String EXTRA_DOSE_UNICA = "DOSE_UNICA";
    public static final String EXTRA_INTERVALO_QTD = "INTERVALO_QTD";
    public static final String EXTRA_INTERVALO_UN = "INTERVALO_UN";
    public static final String EXTRA_STATUS = "STATUS";
    public static final String EXTRA_DATA_PROXIMA = "DATA_PROXIMA";
    public static final String EXTRA_PRECISA_CONFIRMAR = "PRECISA_CONFIRMAR";

    private static final String[] DOSE_UNICA_OPCOES = {"Não", "Sim"};
    private static final String[] UNIDADES = {"Dias", "Semanas", "Meses", "Anos"};
    private static final String[] UNIDADES_API = {"DIAS", "SEMANAS", "MESES", "ANOS"};
    private static final DateTimeFormatter FMT_BR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR"));

    private SessionManager sessionManager;
    private long petId = -1;
    private long idVacina = -1;
    private boolean modoEdicao = false;
    private boolean precisaConfirmarAplicacao = false;
    private boolean jaAplicadaChecked = false;

    private MaterialToolbar toolbar;
    private TextInputEditText edtNome, edtData, edtIntervaloQtd, edtObservacao;
    private Spinner spDosagemUnica, spIntervaloUnidade;
    private View boxJaAplicada, boxIntervalo;
    private FrameLayout btnJaAplicada;
    private View viewJaAplicadaBg;
    private ImageView imgJaAplicadaCheck;
    private MaterialButton btnConfirmar;

    private LocalDate dataSelecionada = LocalDate.now();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastrar_vacina_layout);

        sessionManager = new SessionManager(this);

        toolbar = findViewById(R.id.toolbarCadastrarVacina);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> finish());

        edtNome = findViewById(R.id.edtNomeVacina);
        edtData = findViewById(R.id.edtDataAplicacao);
        edtIntervaloQtd = findViewById(R.id.edtIntervaloQtd);
        edtObservacao = findViewById(R.id.edtObservacao);
        spDosagemUnica = findViewById(R.id.spDosagemUnica);
        spIntervaloUnidade = findViewById(R.id.spIntervaloUnidade);
        boxJaAplicada = findViewById(R.id.boxJaAplicada);
        boxIntervalo = findViewById(R.id.boxIntervalo);
        btnJaAplicada = findViewById(R.id.btnJaAplicada);
        viewJaAplicadaBg = findViewById(R.id.viewJaAplicadaBg);
        imgJaAplicadaCheck = findViewById(R.id.imgJaAplicadaCheck);
        btnConfirmar = findViewById(R.id.btnConfirmarVacina);

        configurarSpinners();
        lerExtras();
        if (toolbar != null) {
            toolbar.setTitle(modoEdicao ? "Atualizar Imunidade" : "Cadastrar Imunidade");
        }
        atualizarUiJaAplicada();
        atualizarVisibilidade();

        if (edtData != null) {
            edtData.setText(dataSelecionada.format(FMT_BR));
            edtData.setOnClickListener(v -> abrirDatePicker());
        }

        if (btnJaAplicada != null) {
            btnJaAplicada.setOnClickListener(v -> {
                jaAplicadaChecked = !jaAplicadaChecked;
                atualizarUiJaAplicada();
                atualizarVisibilidade();
            });
        }

        if (spDosagemUnica != null) {
            spDosagemUnica.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    atualizarVisibilidade();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        if (btnConfirmar != null) {
            btnConfirmar.setOnClickListener(v -> salvar());
        }
    }

    private void configurarSpinners() {
        ArrayAdapter<String> adDose = new ArrayAdapter<>(this, R.layout.spinner_item_vacina, DOSE_UNICA_OPCOES);
        adDose.setDropDownViewResource(R.layout.spinner_dropdown_vacina);
        if (spDosagemUnica != null) {
            spDosagemUnica.setAdapter(adDose);
            spDosagemUnica.setSelection(0);
        }

        ArrayAdapter<String> adUn = new ArrayAdapter<>(this, R.layout.spinner_item_vacina, UNIDADES);
        adUn.setDropDownViewResource(R.layout.spinner_dropdown_vacina);
        if (spIntervaloUnidade != null) {
            spIntervaloUnidade.setAdapter(adUn);
            spIntervaloUnidade.setSelection(2); // Meses
        }
    }

    private void lerExtras() {
        petId = getIntent().getLongExtra(EXTRA_PET_ID, sessionManager.obterPetId());
        if (petId <= 0) {
            petId = getIntent().getLongExtra("PET_ID", sessionManager.obterPetId());
        }

        // Aceita ID_VACINA (oficial) e VACINA_ID (Cadu antiga)
        idVacina = getIntent().getLongExtra(EXTRA_ID_VACINA, -1);
        if (idVacina <= 0) {
            idVacina = getIntent().getLongExtra("VACINA_ID", -1);
        }

        modoEdicao = getIntent().getBooleanExtra(EXTRA_MODO_EDICAO, false);
        if (idVacina > 0) {
            modoEdicao = true;
        }

        precisaConfirmarAplicacao = getIntent().getBooleanExtra(EXTRA_PRECISA_CONFIRMAR, false);
        String status = getIntent().getStringExtra(EXTRA_STATUS);
        String dataProx = getIntent().getStringExtra(EXTRA_DATA_PROXIMA);
        if (!precisaConfirmarAplicacao && modoEdicao) {
            precisaConfirmarAplicacao = calcularPrecisaConfirmar(status, dataProx);
        }

        String nome = getIntent().getStringExtra(EXTRA_NOME);
        if (nome == null) nome = getIntent().getStringExtra("NOME");
        if (nome != null && edtNome != null) edtNome.setText(nome);

        dataSelecionada = LocalDate.now();
        String data = getIntent().getStringExtra(EXTRA_DATA);
        if (data == null || data.isEmpty()) {
            data = getIntent().getStringExtra("DATA_APLICACAO");
        }
        if (data != null && !data.isEmpty()) {
            try {
                dataSelecionada = LocalDate.parse(data.substring(0, Math.min(10, data.length())));
            } catch (Exception e) {
                Log.w(TAG, "data inválida: " + data);
            }
        }

        String obs = getIntent().getStringExtra(EXTRA_OBS);
        if (obs == null) obs = getIntent().getStringExtra("OBSERVACAO");
        if (obs != null && edtObservacao != null) edtObservacao.setText(obs);

        boolean doseUnica = getIntent().getBooleanExtra(EXTRA_DOSE_UNICA, false);
        if (spDosagemUnica != null) {
            spDosagemUnica.setSelection(doseUnica ? 1 : 0);
        }

        int qtd = getIntent().getIntExtra(EXTRA_INTERVALO_QTD, 0);
        if (qtd > 0 && edtIntervaloQtd != null) {
            edtIntervaloQtd.setText(String.valueOf(qtd));
        }

        String un = getIntent().getStringExtra(EXTRA_INTERVALO_UN);
        if (un != null && spIntervaloUnidade != null) {
            for (int i = 0; i < UNIDADES_API.length; i++) {
                if (UNIDADES_API[i].equalsIgnoreCase(un)) {
                    spIntervaloUnidade.setSelection(i);
                    break;
                }
            }
        }

        jaAplicadaChecked = false;
        if (modoEdicao && !precisaConfirmarAplicacao) {
            jaAplicadaChecked = true;
        }

        Log.d(TAG, "lerExtras idVacina=" + idVacina + " modoEdicao=" + modoEdicao + " petId=" + petId);
    }

    private boolean calcularPrecisaConfirmar(String status, String dataProximaIso) {
        if (status != null) {
            String s = status.trim().toUpperCase(Locale.ROOT);
            if (Vacina.STATUS_PENDENTE.equals(s) || Vacina.STATUS_ATRASADA.equals(s)) return true;
            if (Vacina.STATUS_APLICADA.equals(s) || Vacina.STATUS_OK.equals(s)) return false;
        }
        if (dataProximaIso == null || dataProximaIso.isEmpty()) return false;
        try {
            LocalDate dp = LocalDate.parse(dataProximaIso.substring(0, Math.min(10, dataProximaIso.length())));
            return !dp.isAfter(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isDoseUnica() {
        return spDosagemUnica != null && spDosagemUnica.getSelectedItemPosition() == 1;
    }

    private void atualizarUiJaAplicada() {
        if (viewJaAplicadaBg == null || imgJaAplicadaCheck == null) return;
        if (jaAplicadaChecked) {
            viewJaAplicadaBg.setBackgroundResource(R.drawable.bg_status_concluida);
            imgJaAplicadaCheck.setVisibility(View.VISIBLE);
        } else {
            viewJaAplicadaBg.setBackgroundResource(R.drawable.bg_status_pendente);
            imgJaAplicadaCheck.setVisibility(View.GONE);
        }
    }

    private void atualizarVisibilidade() {
        boolean mostrarJa = !modoEdicao || precisaConfirmarAplicacao;
        if (boxJaAplicada != null) {
            boxJaAplicada.setVisibility(mostrarJa ? View.VISIBLE : View.GONE);
        }
        if (boxIntervalo != null) {
            boxIntervalo.setVisibility(isDoseUnica() ? View.GONE : View.VISIBLE);
        }
    }

    private void abrirDatePicker() {
        Calendar c = Calendar.getInstance();
        c.set(dataSelecionada.getYear(),
                dataSelecionada.getMonthValue() - 1,
                dataSelecionada.getDayOfMonth());

        DatePickerDialog dlg = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    dataSelecionada = LocalDate.of(year, month + 1, dayOfMonth);
                    if (edtData != null) {
                        edtData.setText(dataSelecionada.format(FMT_BR));
                    }
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        if (jaAplicadaChecked) {
            dlg.getDatePicker().setMaxDate(System.currentTimeMillis());
        }
        dlg.show();
    }

    private void salvar() {
        String nome = edtNome != null && edtNome.getText() != null
                ? edtNome.getText().toString().trim() : "";
        if (nome.isEmpty()) {
            Toast.makeText(this, "Informe o nome da vacina.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (petId <= 0) {
            Toast.makeText(this, "Pet inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean doseUnica = isDoseUnica();
        boolean jaAplicada;
        if (!modoEdicao || precisaConfirmarAplicacao) {
            jaAplicada = jaAplicadaChecked;
        } else {
            jaAplicada = true;
        }

        if (jaAplicada && dataSelecionada.isAfter(LocalDate.now())) {
            Toast.makeText(this, "Vacina já aplicada não pode ter data futura.", Toast.LENGTH_LONG).show();
            return;
        }

        Integer intervaloQtd = null;
        String intervaloUn = null;
        if (!doseUnica) {
            String qtdStr = edtIntervaloQtd != null && edtIntervaloQtd.getText() != null
                    ? edtIntervaloQtd.getText().toString().trim() : "";
            if (qtdStr.isEmpty()) {
                Toast.makeText(this, "Informe o intervalo (quantidade).", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                intervaloQtd = Integer.parseInt(qtdStr);
                if (intervaloQtd <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Quantidade inválida.", Toast.LENGTH_SHORT).show();
                return;
            }
            int idx = spIntervaloUnidade != null ? spIntervaloUnidade.getSelectedItemPosition() : 2;
            if (idx < 0 || idx >= UNIDADES_API.length) idx = 2;
            intervaloUn = UNIDADES_API[idx];
        }

        String obs = edtObservacao != null && edtObservacao.getText() != null
                ? edtObservacao.getText().toString().trim() : "";

        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Sessão expirada.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnConfirmar != null) btnConfirmar.setEnabled(false);

        try {
            JSONObject body = new JSONObject();
            body.put("nome", nome);
            body.put("dataAplicacao", dataSelecionada.toString());
            body.put("dosagemUnica", doseUnica);
            body.put("jaAplicada", jaAplicada);
            if (doseUnica) {
                body.put("intervaloQuantidade", JSONObject.NULL);
                body.put("intervaloUnidade", JSONObject.NULL);
            } else {
                body.put("intervaloQuantidade", intervaloQtd);
                body.put("intervaloUnidade", intervaloUn);
            }
            body.put("observacao", obs.isEmpty() ? JSONObject.NULL : obs);

            // *** CRÍTICO: id > 0 → PUT (atualiza); senão POST (cria) ***
            final boolean editar = idVacina > 0;
            String url = editar
                    ? ApiConfig.URL_VACINA + idVacina
                    : ApiConfig.URL_VACINAS_DO_PET + petId + "/vacinas";
            int method = editar ? Request.Method.PUT : Request.Method.POST;

            Log.d(TAG, (editar ? "PUT " : "POST ") + url
                    + " idVacina=" + idVacina + " body=" + body);

            JsonObjectRequest req = new JsonObjectRequest(
                    method, url, body,
                    response -> {
                        AppDataCache.get().invalidarVacinas(petId);
                        Toast.makeText(this,
                                editar ? "Imunidade atualizada!" : "Vacina cadastrada!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    error -> {
                        if (btnConfirmar != null) btnConfirmar.setEnabled(true);
                        String msg = "Erro ao salvar.";
                        if (error.networkResponse != null) {
                            int code = error.networkResponse.statusCode;
                            String errBody = "";
                            try {
                                errBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                Log.e(TAG, "HTTP " + code + " " + errBody);
                            } catch (Exception ignored) {}
                            if (code == 403) {
                                msg = "Apenas o tutor principal pode cadastrar/editar.";
                            } else if (code == 400) {
                                msg = errBody.isEmpty()
                                        ? "Dados inválidos."
                                        : "Dados inválidos: " + errBody;
                                if (msg.length() > 200) msg = msg.substring(0, 200) + "…";
                            } else {
                                msg = "Erro (" + code + ").";
                            }
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
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
        } catch (Exception e) {
            if (btnConfirmar != null) btnConfirmar.setEnabled(true);
            Log.e(TAG, "body", e);
            Toast.makeText(this, "Falha ao montar dados.", Toast.LENGTH_SHORT).show();
        }
    }
}