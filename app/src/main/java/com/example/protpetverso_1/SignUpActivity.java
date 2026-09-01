package com.example.protpetverso_1;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpActivity extends AppCompatActivity {
    TextInputEditText edtNome, edtCriarEmail, edtCriarSenha, edtSenha;
    TextInputLayout ipEdtNome, ipEdtCriarEmail, ipEdtCriarSenha, ipEdtSenha;
    ScrollView signupScroll;
    ImageButton btnVoltar;
    Button btnCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup_layout);

        edtNome = findViewById(R.id.edtNome);
        edtCriarEmail = findViewById(R.id.edtCriarEmail);
        edtCriarSenha = findViewById(R.id.edtCriarSenha);
        edtSenha = findViewById(R.id.edtSenha);
        ipEdtNome = findViewById(R.id.ipEdtNome);
        ipEdtCriarEmail = findViewById(R.id.ipEdtCriarEmail);
        ipEdtCriarSenha = findViewById(R.id.ipEdtCriarSenha);
        ipEdtSenha = findViewById(R.id.ipEdtSenha);
        signupScroll = findViewById(R.id.signupScroll);
        btnVoltar = findViewById(R.id.btnVoltar);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        ViewCompat.setOnApplyWindowInsetsListener(signupScroll, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = Math.max(systemBars.bottom, ime.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottom);
            return insets;
        });

        btnCadastrar.setOnClickListener(v -> {
            ipEdtNome.setError(null);
            ipEdtCriarEmail.setError(null);
            ipEdtCriarSenha.setError(null);
            ipEdtSenha.setError(null);

            String nome = String.valueOf(edtNome.getText()).trim();
            String email = String.valueOf(edtCriarEmail.getText()).trim();
            String senha = String.valueOf(edtCriarSenha.getText()).trim();
            String confirma = String.valueOf(edtSenha.getText()).trim();

            boolean valido = true;

            if (nome.isEmpty()) {
                ipEdtNome.setError("Digite um nome");
                valido = false;
            }

            if (email.isEmpty()) {
                ipEdtCriarEmail.setError("Digite um email");
                valido = false;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                ipEdtCriarEmail.setError("E-mail inválido");
                valido = false;
            }

            if (senha.isEmpty()) {
                ipEdtCriarSenha.setError("Digite uma senha");
                valido = false;
            } else if (senha.length() < 6) {
                ipEdtCriarSenha.setError("A senha deve ter pelo menos 6 caracteres");
                valido = false;
            }

            if (confirma.isEmpty()) {
                ipEdtSenha.setError("Confirme a senha");
                valido = false;
            } else if (!senha.equals(confirma)) {
                ipEdtSenha.setError("A senha digitada deve ser igual à senha criada acima");
                valido = false;
            }

            MaterialCheckBox cbTermos = findViewById(R.id.cbTermos);
            MaterialCheckBox cbPrivacidade = findViewById(R.id.cbPrivacidade);

            if (!cbTermos.isChecked() || !cbPrivacidade.isChecked()) {
                Toast.makeText(this, "Aceite os Termos e a Política de Privacidade", Toast.LENGTH_SHORT).show();
                valido = false;
            }

            if (valido) {
                // startActivity(new Intent(this, ProximaActivity.class));
                // finish();
            }
        });

        btnVoltar.setOnClickListener(v -> {
            finish();
        });

        edtSenha.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) scrollToView(ipEdtSenha);
        });
        edtSenha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String senhaCriada = String.valueOf(edtCriarSenha.getText()).trim();
                String confirma = String.valueOf(edtSenha.getText()).trim();

                if (!confirma.isEmpty() && !senhaCriada.equals(confirma)) {
                    ipEdtSenha.setError("A senha digitada deve ser igual à senha criada acima");
                } else {
                    ipEdtSenha.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    private void scrollToView(View target) {
        signupScroll.postDelayed(() -> {
            int[] location = new int[2];
            target.getLocationInWindow(location);
            int offset = 400;
            signupScroll.smoothScrollTo(0, location[1] - offset);
        }, 350);
    }
}