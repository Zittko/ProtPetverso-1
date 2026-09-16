package com.example.protpetverso_1;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Armazena e recupera a sessão do usuário de forma segura
 * (Token JWT + dados básicos) usando EncryptedSharedPreferences.
 */
public class SessionManager {


    private static final String PREF_NAME = "user_session";
    private static final String KEY_TOKEN = "AUTH_TOKEN";
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_USER_NAME = "USER_NAME";
    private static final String KEY_USER_EMAIL = "USER_EMAIL";

    private SharedPreferences prefs;

    public SessionManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            prefs = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            // Fallback caso a criptografia falhe
            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    /**
     * Salva os dados da sessão após login ou cadastro bem-sucedido.
     */
    public void salvarSessao(String token, long idUsuario, String nome, String email) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_USER_ID, idUsuario)
                .putString(KEY_USER_NAME, nome)
                .putString(KEY_USER_EMAIL, email)
                .apply();
    }

    public String obterToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public long obterUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String obterNome() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    public String obterEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Verifica se existe um token salvo (usuário logado).
     */
    public boolean isLogado() {
        String token = obterToken();
        return token != null && !token.isEmpty();
    }

    /**
     * Limpa toda a sessão (logout).
     */
    public void limparSessao() {
        prefs.edit().clear().apply();
    }
}