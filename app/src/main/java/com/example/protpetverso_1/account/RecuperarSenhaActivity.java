package com.example.protpetverso_1.account;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.protpetverso_1.R;

public class RecuperarSenhaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recuperar_senha_layout);

        View voltar = findViewById(R.id.btnVoltar);
        if (voltar != null) voltar.setOnClickListener(v -> finish());
    }
}
