package com.example.protpetverso_1.schedule;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CriarTarefaActivity extends AppCompatActivity {

    private static final String TAG = "CRIAR_TAREFA";

    private TextInputEditText edtTitulo, edtData, edtHora, edtDesc;
    private MaterialButton btnConfirmar;
    private SessionManager sessionManager;

    private long petId = -1;
    private long idTarefa = -1;
    private boolean modoEdicao = false;

    private LocalDate dataSelecionada = LocalDate.now();
    private LocalTime horaSelecionada = LocalTime.of(8, 0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_criar_tarefa);

        getWindow().setStatusBarColor(Color.parseColor("#F08C4B"));
        new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);

        sessionManager = new SessionManager(this);

        modoEdicao = getIntent().getBooleanExtra("MODO_EDICAO", false);
        idTarefa = getIntent().getLongExtra("ID_TAREFA", -1);
        petId = getIntent().getLongExtra("PET_ID", sessionManager.obterPetId());

        String tituloExtra = getIntent().getStringExtra("TITULO");
        String descExtra = getIntent().getStringExtra("DESCRICAO");
        String dataExtra = getIntent().getStringExtra("DATA");
        String horaExtra = getIntent().getStringExtra("HORA");

        if (dataExtra != null && !dataExtra.isEmpty()) {
            try { dataSelecionada = LocalDate.parse(dataExtra); } catch (Exception ignored) {}
        }
        if (horaExtra != null && !horaExtra.isEmpty()) {
            try {
                horaSelecionada = LocalTime.parse(horaExtra.length() >= 5 ? horaExtra.substring(0, 5) : horaExtra);
            } catch (Exception ignored) {}
        }

        if (!modoEdicao && dataSelecionada.isBefore(LocalDate.now())) {
            dataSelecionada = LocalDate.now();
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbarCriarTarefa);
        if (toolbar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(bars.left, bars.top, bars.right, 0);
                return insets;
            });
            toolbar.setTitle(modoEdicao ? "Editar tarefa" : "Nova tarefa");
            toolbar.setTitleTextColor(Color.parseColor("#000000"));
            toolbar.setNavigationIconTint(Color.parseColor("#000000"));
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        edtTitulo = findViewById(R.id.edtTituloTarefa);
        edtData = findViewById(R.id.edtDataTarefa);
        edtHora = findViewById(R.id.edtHoraTarefa);
        edtDesc = findViewById(R.id.edtDescTarefa);
        btnConfirmar = findViewById(R.id.btnConfirmarTarefa);

        if (edtTitulo != null && tituloExtra != null) edtTitulo.setText(tituloExtra);
        if (edtDesc != null && descExtra != null) edtDesc.setText(descExtra);

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
            btnConfirmar.setText(modoEdicao ? "Salvar alterações" : "Confirmar");
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
                    if (!modoEdicao && escolhida.isBefore(LocalDate.now())) {
                        Toast.makeText(this, "Não é possível criar tarefa em data passada.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dataSelecionada = escolhida;
                    atualizarTextoData();
                },
                dataSelecionada.getYear(),
                dataSelecionada.getMonthValue() - 1,
                dataSelecionada.getDayOfMonth()
        );
        if (!modoEdicao) {
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        }
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
        if (!modoEdicao && dataSelecionada.isBefore(LocalDate.now())) {
            Toast.makeText(this, "Não é possível criar tarefa em data passada.", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Sessão expirada", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnConfirmar != null) btnConfirmar.setEnabled(false);

        try {
            JSONObject body = new JSONObject();
            body.put("titulo", titulo);
            body.put("descricao", desc);
            body.put("dataTarefa", dataSelecionada.toString());
            body.put("hora", String.format(Locale.getDefault(), "%02d:%02d:00",
                    horaSelecionada.getHour(), horaSelecionada.getMinute()));

            int method;
            String url;
            if (modoEdicao && idTarefa > 0) {
                method = Request.Method.PUT;
                url = ApiConfig.URL_ATUALIZAR_TAREFA + idTarefa;
            } else {
                method = Request.Method.POST;
                url = ApiConfig.URL_TAREFAS_DO_PET + petId + "/tarefas";
            }

            Log.d(TAG, (modoEdicao ? "PUT " : "POST ") + url + " body=" + body);

            JsonObjectRequest request = new JsonObjectRequest(
                    method, url, body,
                    response -> {
                        Toast.makeText(this,
                                modoEdicao ? "Tarefa atualizada!" : "Tarefa criada!",
                                Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    },
                    error -> {
                        if (btnConfirmar != null) btnConfirmar.setEnabled(true);
                        String msg = modoEdicao ? "Erro ao atualizar tarefa." : "Erro ao criar tarefa.";
                        if (error.networkResponse != null) {
                            int s = error.networkResponse.statusCode;
                            if (s == 403) {
                                msg = "Apenas o tutor principal (DONO) pode alterar tarefas.";
                            } else if (s == 409 || s == 400) {
                                msg = "Esta tarefa já foi concluída e não pode ser alterada.";
                            } else {
                                msg += " (" + s + ")";
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