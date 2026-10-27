package com.example.protpetverso_1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.protpetverso_1.account.LoginActivity;
import com.example.protpetverso_1.data.AppDataCache;

/**
 * Splash: garante cache em disco na memória e encaminha
 * logado → MenuActivity | não logado → LoginActivity
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION_MS = 1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.splash_layout);

        // Seguro mesmo se PetVersoApp já tiver chamado: idempotente
        AppDataCache.get().init(this);
        AppDataCache.get().carregarDoDisco();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing()) return;

            SessionManager session = new SessionManager(this);
            Intent intent;

            if (session.isLogado()) {
                // Home já pode usar pets/tarefas/fotos do disco enquanto a API sobe
                intent = new Intent(SplashActivity.this, MenuActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }, SPLASH_DURATION_MS);
    }
}