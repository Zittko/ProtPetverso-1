package com.example.protpetverso_1.schedule;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.VolleySingleton;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Cria tarefa na API:
 * POST /api/pets/{id}/tarefas
 * Body: { titulo, descricao, dataTarefa, hora }
 *
 * Não permite data passada (DatePicker com minDate + validação no salvar).
 * Intent extras: PET_ID, DATA ou DATA_PADRAO (yyyy-MM-dd).
 */
public class CriarTarefaActivity extends AppCompatActivity {

    private static final String TAG = "CRIAR_TAREFA";

    private TextInputEditText edtTitulo, edtData, edtHora, edtDesc;
    private MaterialButton btnConfirmar;
    private SessionManager sessionManager;
    private long petId = -1;
    private LocalDate dataSelecionada = LocalDate.now();
    private LocalTime horaSelecionada = LocalTime.of(8, 0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_tarefa);

        sessionManager = new SessionManager(this);
        petId = getIntent().getLongExtra("PET_ID", sessionManager.obterPetId());

        // Aceita DATA (Agenda) ou DATA_PADRAO (legado)
        String dataPadrao = getIntent().getStringExtra("DATA");
        if (dataPadrao == null || dataPadrao.isEmpty()) {
            dataPadrao = getIntent().getStringExtra("DATA_PADRAO");
        }
        if (dataPadrao != null && !dataPadrao.isEmpty()) {
            try {
                dataSelecionada = LocalDate.parse(dataPadrao);
            } catch (Exception ignored) {
            }
        }

        // Se veio data passada do calendário, força hoje (criação não permite passado)
        if (dataSelecionada.isBefore(LocalDate.now())) {
            dataSelecionada = LocalDate.now();
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbarCriarTarefa);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        edtTitulo = findViewById(R.id.edtTituloTarefa);
        edtData = findViewById(R.id.edtDataTarefa);
        edtHora = findViewById(R.id.edtHoraTarefa);
        edtDesc = findViewById(R.id.edtDescTarefa);
        btnConfirmar = findViewById(R.id.btnConfirmarTarefa);

        atualizarTextoData();
        atualizarTextoHora();

        if (edtData != null) {
            edtData.setFocusable(false);
            edtData.setClickable(true);
            edtData.setOnClickListener(v -> abrirDatePicker());
        }
        if (edtHora != null) {
            edtHora.setFocusable(false);
            edtHora.setClickable(true);
            edtHora.setOnClickListener(v -> abrirTimePicker());
        }

        if (btnConfirmar != null) {
            btnConfirmar.setOnClickListener(v -> salvarTarefa());
        }
    }

    private void atualizarTextoData() {
        if (edtData == null) return;
        edtData.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d",
                dataSelecionada.getDayOfMonth(),
                dataSelecionada.getMonthValue(),
                dataSelecionada.getYear()));
    }

    private void atualizarTextoHora() {
        if (edtHora == null) return;
        edtHora.setText(String.format(Locale.getDefault(), "%02d:%02d",
                horaSelecionada.getHour(), horaSelecionada.getMinute()));
    }

    private void abrirDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    LocalDate escolhida = LocalDate.of(y, m + 1, d);
                    if (escolhida.isBefore(LocalDate.now())) {
                        Toast.makeText(this,
                                "Não é possível criar tarefa em data passada.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dataSelecionada = escolhida;
                    atualizarTextoData();
                },
                dataSelecionada.getYear(),
                dataSelecionada.getMonthValue() - 1,
                dataSelecionada.getDayOfMonth()
        );

        // Impede dias anteriores no seletor
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void abrirTimePicker() {
        new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    horaSelecionada = LocalTime.of(hourOfDay, minute);
                    atualizarTextoHora();
                },
                horaSelecionada.getHour(),
                horaSelecionada.getMinute(),
                true
        ).show();
    }

    private void salvarTarefa() {
        String titulo = edtTitulo != null && edtTitulo.getText() != null
                ? edtTitulo.getText().toString().trim() : "";
        String desc = edtDesc != null && edtDesc.getText() != null
                ? edtDesc.getText().toString().trim() : "";

        if (titulo.isEmpty()) {
            Toast.makeText(this, "Informe o título", Toast.LENGTH_SHORT).show();
            return;
        }
        if (petId <= 0) {
            Toast.makeText(this, "Pet inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validação final: não cria em data passada
        if (dataSelecionada.isBefore(LocalDate.now())) {
            Toast.makeText(this, "Não é possível criar tarefa em data passada.", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Sessão expirada", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnConfirmar != null) {
            btnConfirmar.setEnabled(false);
        }

        try {
            JSONObject body = new JSONObject();
            body.put("titulo", titulo);
            body.put("descricao", desc);
            body.put("dataTarefa", dataSelecionada.toString()); // yyyy-MM-dd
            body.put("hora", String.format(Locale.getDefault(), "%02d:%02d:00",
                    horaSelecionada.getHour(), horaSelecionada.getMinute()));

            String url = ApiConfig.URL_TAREFAS_DO_PET + petId + "/tarefas";
            Log.d(TAG, "POST " + url + " body=" + body);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        Toast.makeText(this, "Tarefa criada!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish(); // Agenda recarrega no onResume
                    },
                    error -> {
                        if (btnConfirmar != null) btnConfirmar.setEnabled(true);
                        String msg = "Erro ao criar tarefa.";
                        if (error.networkResponse != null) {
                            int s = error.networkResponse.statusCode;
                            if (s == 403) {
                                msg = "Apenas o tutor principal pode criar tarefas.";
                            } else {
                                msg += " (" + s + ")";
                            }
                            try {
                                String err = new String(
                                        error.networkResponse.data,
                                        java.nio.charset.StandardCharsets.UTF_8);
                                Log.e(TAG, err);
                            } catch (Exception ignored) {
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

            VolleySingleton.getInstance(this).addToRequestQueue(request);
        } catch (Exception e) {
            if (btnConfirmar != null) btnConfirmar.setEnabled(true);
            Log.e(TAG, "montar body", e);
            Toast.makeText(this, "Erro ao montar dados", Toast.LENGTH_SHORT).show();
        }
    }
}