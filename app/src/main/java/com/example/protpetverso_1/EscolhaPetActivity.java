package com.example.protpetverso_1;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class EscolhaPetActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.escolha_pet_layout);
        // Liga o card "Cadastrar novo pet"
        MaterialCardView cardCadastrarPet = findViewById(R.id.cardCadastrarPet);

        cardCadastrarPet.setOnClickListener(v -> {
            // Abre a tela de formulário do pet
            startActivity(new Intent(EscolhaPetActivity.this, PetSignUpActivity.class));
        });
    }

}