package com.example.protpetverso_1;
// Define o pacote do projeto.

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;
// Imports para Intent, Views, TextWatcher e Toast.

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
// Imports para Edge-to-Edge e tratamento de teclado/barras do sistema.

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
// Componentes Material: campos de texto e checkboxes.

public class SignUpActivity extends AppCompatActivity {
// Tela de Cadastro. Aberta a partir da LoginActivity.

    TextInputEditText edtNome, edtCriarEmail, edtCriarSenha, edtSenha;
    // Campos de digitação: nome, e-mail, senha e confirmação de senha.

    TextInputLayout ipEdtNome, ipEdtCriarEmail, ipEdtCriarSenha, ipEdtSenha;
    // Containers dos campos (usados para mostrar erros).

    ScrollView signupScroll;
    // ScrollView principal do layout. Usado para subir a tela com o teclado.

    ImageButton btnVoltar;
    // Botão de voltar para a tela anterior.

    Button btnCadastrar;
    // Botão que tenta realizar o cadastro.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Ativa o modo edge-to-edge.

        setContentView(R.layout.signup_layout);
        // Carrega o layout da tela de cadastro.
        // Conectado com: signup_layout.xml

        // Liga os componentes do XML com as variáveis Java
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

        // Trata barras do sistema + teclado
        ViewCompat.setOnApplyWindowInsetsListener(signupScroll, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = Math.max(systemBars.bottom, ime.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottom);
            return insets;
        });
        // Faz o conteúdo subir quando o teclado aparece.
        // Conectado com: signupScroll

        // Ação do botão Cadastrar
        btnCadastrar.setOnClickListener(v -> {
            // Limpa erros anteriores
            ipEdtNome.setError(null);
            ipEdtCriarEmail.setError(null);
            ipEdtCriarSenha.setError(null);
            ipEdtSenha.setError(null);

            // Pega os textos digitados
            String nome = String.valueOf(edtNome.getText()).trim();
            String email = String.valueOf(edtCriarEmail.getText()).trim();
            String senha = String.valueOf(edtCriarSenha.getText()).trim();
            String confirma = String.valueOf(edtSenha.getText()).trim();

            boolean valido = true;
            // Flag que indica se o formulário passou em todas as validações.

            // Validação do nome
            if (nome.isEmpty()) {
                ipEdtNome.setError("Digite um nome");
                valido = false;
            }

            // Validação do e-mail
            if (email.isEmpty()) {
                ipEdtCriarEmail.setError("Digite um email");
                valido = false;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                ipEdtCriarEmail.setError("E-mail inválido");
                valido = false;
            }

            // Validação da senha
            if (senha.isEmpty()) {
                ipEdtCriarSenha.setError("Digite uma senha");
                valido = false;
            } else if (senha.length() < 6) {
                ipEdtCriarSenha.setError("A senha deve ter pelo menos 6 caracteres");
                valido = false;
            }

            // Validação da confirmação de senha
            if (confirma.isEmpty()) {
                ipEdtSenha.setError("Confirme a senha");
                valido = false;
            } else if (!senha.equals(confirma)) {
                ipEdtSenha.setError("A senha digitada deve ser igual à senha criada acima");
                valido = false;
            }

            // Validação dos checkboxes de termos
            MaterialCheckBox cbTermos = findViewById(R.id.cbTermos);
            MaterialCheckBox cbPrivacidade = findViewById(R.id.cbPrivacidade);

            if (!cbTermos.isChecked() || !cbPrivacidade.isChecked()) {
                Toast.makeText(this, "Aceite os Termos e a Política de Privacidade", Toast.LENGTH_SHORT).show();
                valido = false;
            }
            // Conectado com: cbTermos e cbPrivacidade no signup_layout.xml

            // Se tudo estiver válido, segue o fluxo (ainda comentado)
            if (valido) {
                // startActivity(new Intent(this, ProximaActivity.class));
                // finish();
            }
        });

        // Botão de voltar
        btnVoltar.setOnClickListener(v -> {
            finish();
            // Fecha a SignUpActivity e volta para a tela anterior (LoginActivity).
        });

        // Quando o campo de confirmar senha ganha foco, sobe a tela
        edtSenha.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) scrollToView(ipEdtSenha);
        });

        // Validação em tempo real da confirmação de senha
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
        // Conectado com: edtCriarSenha + ipEdtSenha
    }

    private void scrollToView(View target) {
        // Faz a tela rolar suavemente até o campo focado.
        signupScroll.postDelayed(() -> {
            int[] location = new int[2];
            target.getLocationInWindow(location);
            int offset = 400;
            signupScroll.smoothScrollTo(0, location[1] - offset);
        }, 350);
        // Conectado com: signupScroll
    }
}