package com.example.protpetverso_1;
// Define o pacote do projeto.

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
// Imports para Intent, Views e TextWatcher.

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
// Imports para Edge-to-Edge e tratamento de teclado/barras do sistema.

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
// Componentes Material dos campos de texto.

public class LoginActivity extends AppCompatActivity {
// Tela de Login. É a Activity que aparece após a Splash.

    TextInputEditText edtUser, edtSenha;
    // Campos onde o usuário digita e-mail e senha.

    Button btnEntrar;
    // Botão de login.

    TextInputLayout ipEdtUser, ipEdtSenha;
    // Containers dos campos (usados para mostrar mensagens de erro).

    TextView txtForgotPassword, txtCadastrar;
    // Textos clicáveis: "Esqueceu a senha?" e "Cadastrar".

    ScrollView loginScroll;
    // ScrollView principal do layout. Usado para subir a tela quando o teclado abre.

    private void scrollToView(View target) {
        // Faz a tela rolar suavemente até o campo focado (principalmente a senha).
        loginScroll.postDelayed(() -> {
            int[] location = new int[2];
            target.getLocationInWindow(location);
            int offset = 400;
            loginScroll.smoothScrollTo(0, location[1] - offset);
        }, 350);
        // Conectado com: loginScroll + ipEdtSenha
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Ativa o modo edge-to-edge (conteúdo atrás das barras do sistema).

        setContentView(R.layout.login_layout);
        // Carrega o layout XML da tela de login.
        // Conectado com: login_layout.xml

        // Liga os componentes do XML com as variáveis Java
        loginScroll = findViewById(R.id.loginScroll);
        ipEdtUser = findViewById(R.id.ipEdtUser);
        ipEdtSenha = findViewById(R.id.ipEdtSenha);
        edtUser = findViewById(R.id.edtUser);
        edtSenha = findViewById(R.id.edtSenha);
        txtCadastrar = findViewById(R.id.txtCadastrar);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        btnEntrar = findViewById(R.id.btnEntrar);

        // Trata as barras do sistema e o teclado (IME)
        ViewCompat.setOnApplyWindowInsetsListener(loginScroll, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = Math.max(systemBars.bottom, ime.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottom);
            return insets;
        });
        // Faz o conteúdo subir quando o teclado aparece.
        // Conectado com: loginScroll

        // Ação do botão Entrar
        btnEntrar.setOnClickListener(v -> {
            // Arrays para validar os dois campos de forma organizada
            TextInputEditText[] editTexts = {edtUser, edtSenha};
            TextInputLayout[] layouts = {ipEdtUser, ipEdtSenha};
            String[] mensagens = {"Digite um email", "Senha não digitada"};

            // Verifica se algum campo está vazio
            for (int i = 0; i < editTexts.length; i++) {
                String texto = String.valueOf(editTexts[i].getText()).trim();
                if (texto.isEmpty()) {
                    layouts[i].setError(mensagens[i]); // mostra o erro no campo
                    return; // para a execução
                } else {
                    layouts[i].setError(null); // limpa o erro
                }
            }

            // Se passou na validação, vai para a MenuActivity
            startActivity(new Intent(getApplicationContext(), MenuActivity.class));
            finish(); // fecha a LoginActivity (não volta para ela com o botão voltar)
            // Conectado com: MenuActivity
        });

        // Quando o campo senha ganha foco, sobe a tela
        edtSenha.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) scrollToView(ipEdtSenha);
        });

        // Clique no texto "Cadastrar"
        txtCadastrar.setOnClickListener(v -> {
            edtUser.setText("");
            edtSenha.setText("");
            // Limpa os campos antes de ir para o cadastro
            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
            // Conectado com: SignUpActivity
        });

        // Limpa o erro do e-mail enquanto o usuário digita
        edtUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ipEdtUser.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Limpa o erro da senha enquanto o usuário digita
        edtSenha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ipEdtSenha.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}