package com.example.protpetverso_1.account;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.VolleySingleton;
import com.example.protpetverso_1.data.AppDataCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Logout único do app.
 *
 * 1. Guarda o token e cancela as requisições em andamento (para que respostas atrasadas
 *    não repovoem o cache depois de limpo).
 * 2. Limpa cache, fotos e sessão local.
 * 3. Avisa a API: POST /api/logout (Bearer token) → 204. A API apaga a sessão no banco,
 *    já que o token não expira sozinho.
 * 4. Volta para o Login.
 *
 * A saída local nunca depende da rede: se a chamada falhar (sem internet, servidor
 * dormindo, token já inválido), o usuário sai do mesmo jeito.
 */
public final class LogoutHelper {

    private static final String TAG = "LOGOUT";
    private static final int TIMEOUT_MS = 30_000; // o servidor gratuito pode demorar a acordar

    private LogoutHelper() {}

    public static void sair(Activity activity) {
        Context app = activity.getApplicationContext();
        SessionManager session = new SessionManager(app);
        String token = session.obterToken();

        VolleySingleton.getInstance(app).getRequestQueue().cancelAll(request -> true);

        AppDataCache.get().init(app);
        AppDataCache.get().limparTudo(true);
        session.limparSessao();

        invalidarTokenNaApi(app, token);

        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private static void invalidarTokenNaApi(Context app, String token) {
        if (token == null || token.isEmpty()) return;

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.URL_LOGOUT,
                response -> Log.d(TAG, "Sessão encerrada na API"),
                error -> {
                    int status = error.networkResponse != null ? error.networkResponse.statusCode : -1;
                    // 403 = token já inválido (nada a fazer). Outros: a sessão local já foi apagada.
                    Log.w(TAG, "Logout na API não confirmado (HTTP " + status + ")", error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

            @Override
            public byte[] getBody() {
                return new byte[0]; // POST sem corpo; envia Content-Length: 0
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MS, 1, 1f));
        VolleySingleton.getInstance(app).addToRequestQueue(request);
    }
}