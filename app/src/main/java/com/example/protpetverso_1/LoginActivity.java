package com.example.protpetverso_1;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText edtUser, edtSenha;
    Button btnEntrar;
    TextInputLayout ipEdtUser, ipEdtSenha;
    TextView txtForgotPassword, txtCadastrar;
    ScrollView loginScroll;
    private void scrollToView(View target) {
        loginScroll.postDelayed(() -> {
            int[] location = new int[2];
            target.getLocationInWindow(location);
            int offset = 400;
            loginScroll.smoothScrollTo(0, location[1] - offset);
        }, 350);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_layout);

        loginScroll = findViewById(R.id.loginScroll);
        ipEdtUser = findViewById(R.id.ipEdtUser);
        ipEdtSenha = findViewById(R.id.ipEdtSenha);
        edtUser = findViewById(R.id.edtUser);
        edtSenha = findViewById(R.id.edtSenha);
        txtCadastrar = findViewById(R.id.txtCadastrar);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        btnEntrar = findViewById(R.id.btnEntrar);

        ViewCompat.setOnApplyWindowInsetsListener(loginScroll, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = Math.max(systemBars.bottom, ime.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottom);
            return insets;
        });

        btnEntrar.setOnClickListener(v -> {
            String user = String.valueOf(edtUser.getText()).trim();
            String senha = String.valueOf(edtSenha.getText()).trim();
            ipEdtUser.setError(null);
            ipEdtSenha.setError(null);
            if (user.isEmpty()) {
                ipEdtUser.setError("Digite um email");
            }
            if (senha.isEmpty()) {
                ipEdtSenha.setError("Senha não digitada");
                return;
            }
            if ((user.equalsIgnoreCase("projetopetverso@gmail.com"))
                    && senha.equals("123")) {
                startActivity(new Intent(getApplicationContext(), MenuActivity.class));
                finish();
            } else {
                ipEdtSenha.setError("Usuário ou senha incorretos");
            }
        });

        edtSenha.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) scrollToView(ipEdtSenha);
        });

        txtCadastrar.setOnClickListener(v -> {
            edtUser.setText("");
            edtSenha.setText("");
            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
        });

        edtUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ipEdtUser.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        edtSenha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ipEdtSenha.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


}