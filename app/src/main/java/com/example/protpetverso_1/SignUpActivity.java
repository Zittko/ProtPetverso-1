package com.example.protpetverso_1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpActivity extends AppCompatActivity {
    TextInputEditText edtNome, edtCriarEmail, edtCriarSenha, edtSenha;
    TextInputLayout ipEdtNome, ipEdtCriarEmail, ipEdtCriarSenha, ipEdtSenha;
    ScrollView signupScroll;
    ImageButton btnVoltar;

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

        ViewCompat.setOnApplyWindowInsetsListener(signupScroll, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = Math.max(systemBars.bottom, ime.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottom);
            return insets;
        });

        btnVoltar.setOnClickListener(v -> {
            finish();
        });

        edtSenha.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) scrollToView(ipEdtSenha);
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