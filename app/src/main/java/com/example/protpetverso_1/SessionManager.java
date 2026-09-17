package com.example.protpetverso_1;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Armazena e recupera a sessão do usuário e dados locais do último pet.
 * Usa EncryptedSharedPreferences (AES-256).
 */
public class SessionManager {

    private static final String PREF_NAME = "user_session";

    // Usuário / sessão
    private static final String KEY_TOKEN = "AUTH_TOKEN";
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_USER_NAME = "USER_NAME";
    private static final String KEY_USER_EMAIL = "USER_EMAIL";
    private static final String KEY_APELIDO = "USER_APELIDO";
    private static final String KEY_TELEFONE = "USER_TELEFONE";

    // Último pet cadastrado
    private static final String KEY_PET_ID = "PET_ID";
    private static final String KEY_PET_NOME = "PET_NOME";
    private static final String KEY_PET_RACA = "PET_RACA";
    private static final String KEY_PET_ESPECIE = "PET_ESPECIE";
    private static final String KEY_PET_PESO = "PET_PESO";
    private static final String KEY_PET_SEXO = "PET_SEXO";
    private static final String KEY_PET_PORTE = "PET_PORTE";
    private static final String KEY_PET_DATA = "PET_DATA";

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
            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    // ==================== SESSÃO / USUÁRIO ====================

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

    public void salvarApelido(String apelido) {
        prefs.edit().putString(KEY_APELIDO, apelido).apply();
    }

    public String obterApelido() {
        return prefs.getString(KEY_APELIDO, "");
    }

    public void salvarTelefone(String telefone) {
        prefs.edit().putString(KEY_TELEFONE, telefone).apply();
    }

    public String obterTelefone() {
        return prefs.getString(KEY_TELEFONE, "");
    }

    public boolean isLogado() {
        String token = obterToken();
        return token != null && !token.isEmpty();
    }

    public void limparSessao() {
        prefs.edit().clear().apply();
    }

    // ==================== PET ====================

    public void salvarPet(long id, String nome, String raca, String especie,
                          String peso, String sexo, String porte, String dataNascimento) {
        prefs.edit()
                .putLong(KEY_PET_ID, id)
                .putString(KEY_PET_NOME, nome)
                .putString(KEY_PET_RACA, raca)
                .putString(KEY_PET_ESPECIE, especie)
                .putString(KEY_PET_PESO, peso)
                .putString(KEY_PET_SEXO, sexo)
                .putString(KEY_PET_PORTE, porte)
                .putString(KEY_PET_DATA, dataNascimento)
                .apply();
    }

    public long obterPetId() {
        return prefs.getLong(KEY_PET_ID, -1);
    }

    public String obterPetNome() {
        return prefs.getString(KEY_PET_NOME, "");
    }

    public String obterPetRaca() {
        return prefs.getString(KEY_PET_RACA, "");
    }

    public String obterPetEspecie() {
        return prefs.getString(KEY_PET_ESPECIE, "");
    }

    public String obterPetPeso() {
        return prefs.getString(KEY_PET_PESO, "");
    }

    public String obterPetSexo() {
        return prefs.getString(KEY_PET_SEXO, "");
    }

    public String obterPetPorte() {
        return prefs.getString(KEY_PET_PORTE, "");
    }

    public String obterPetData() {
        return prefs.getString(KEY_PET_DATA, "");
    }

    public boolean temPetSalvo() {
        return obterPetId() > 0 || !obterPetNome().isEmpty();
    }
}