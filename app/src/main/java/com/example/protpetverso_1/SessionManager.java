package com.example.protpetverso_1;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Sessão do usuário + último pet selecionado.
 * Se o Keystore falhar (VERIFICATION_FAILED), usa prefs normais.
 */
public class SessionManager {

    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "user_session";
    private static final String PREF_NAME_FALLBACK = "user_session_fallback";

    private static final String KEY_TOKEN = "AUTH_TOKEN";
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_USER_NAME = "USER_NAME";
    private static final String KEY_USER_EMAIL = "USER_EMAIL";
    private static final String KEY_APELIDO = "USER_APELIDO";
    private static final String KEY_TELEFONE = "USER_TELEFONE";

    private static final String KEY_PET_ID = "PET_ID";
    private static final String KEY_PET_NOME = "PET_NOME";
    private static final String KEY_PET_RACA = "PET_RACA";
    private static final String KEY_PET_ESPECIE = "PET_ESPECIE";
    private static final String KEY_PET_PESO = "PET_PESO";
    private static final String KEY_PET_SEXO = "PET_SEXO";
    private static final String KEY_PET_PORTE = "PET_PORTE";
    private static final String KEY_PET_DATA = "PET_DATA";
    private static final String KEY_PET_PAPEL = "PET_PAPEL";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        SharedPreferences p;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            p = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e(TAG, "EncryptedSharedPreferences falhou — usando fallback.", e);
            p = context.getApplicationContext()
                    .getSharedPreferences(PREF_NAME_FALLBACK, Context.MODE_PRIVATE);
        }
        this.prefs = p;
    }

    // ==================== USUÁRIO ====================

    /** Login: salvarSessao(token, userId, nome, email) */
    public void salvarSessao(String token, long userId, String nome, String email) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USER_NAME, nome != null ? nome : "")
                .putString(KEY_USER_EMAIL, email != null ? email : "")
                .apply();
    }

    /** Alias de salvarSessao. */
    public void salvarLogin(String token, long userId, String nome, String email) {
        salvarSessao(token, userId, nome, email);
    }

    public void salvarPerfil(String nome, String email, String apelido, String telefone) {
        prefs.edit()
                .putString(KEY_USER_NAME, nome != null ? nome : "")
                .putString(KEY_USER_EMAIL, email != null ? email : "")
                .putString(KEY_APELIDO, apelido != null ? apelido : "")
                .putString(KEY_TELEFONE, telefone != null ? telefone : "")
                .apply();
    }

    public void salvarTelefone(String telefone) {
        prefs.edit()
                .putString(KEY_TELEFONE, telefone != null ? telefone : "")
                .apply();
    }

    public void salvarApelido(String apelido) {
        prefs.edit()
                .putString(KEY_APELIDO, apelido != null ? apelido : "")
                .apply();
    }

    public void salvarNome(String nome) {
        prefs.edit()
                .putString(KEY_USER_NAME, nome != null ? nome : "")
                .apply();
    }

    public void salvarEmail(String email) {
        prefs.edit()
                .putString(KEY_USER_EMAIL, email != null ? email : "")
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

    public String obterApelido() {
        return prefs.getString(KEY_APELIDO, null);
    }

    public String obterTelefone() {
        return prefs.getString(KEY_TELEFONE, null);
    }

    public boolean isLogado() {
        String token = obterToken();
        return token != null && !token.isEmpty();
    }

    // ==================== PET ====================

    /** Home / Vacinas / Agenda: salvarPet(idPet, nome, papel) */
    public void salvarPet(long idPet, String nome, String papel) {
        prefs.edit()
                .putLong(KEY_PET_ID, idPet)
                .putString(KEY_PET_NOME, nome != null ? nome : "")
                .putString(KEY_PET_PAPEL, papel != null ? papel : "")
                .apply();
    }

    /**
     * Versão completa (perfil do pet).
     * Não altera o papel; use salvarPet(id, nome, papel) ou salvarPetPapel.
     */
    public void salvarPet(long id, String nome, String raca, String especie,
                          String peso, String sexo, String porte, String dataNascimento) {
        prefs.edit()
                .putLong(KEY_PET_ID, id)
                .putString(KEY_PET_NOME, nome != null ? nome : "")
                .putString(KEY_PET_RACA, raca != null ? raca : "")
                .putString(KEY_PET_ESPECIE, especie != null ? especie : "")
                .putString(KEY_PET_PESO, peso != null ? peso : "")
                .putString(KEY_PET_SEXO, sexo != null ? sexo : "")
                .putString(KEY_PET_PORTE, porte != null ? porte : "")
                .putString(KEY_PET_DATA, dataNascimento != null ? dataNascimento : "")
                .apply();
    }

    public void salvarPetPapel(String papel) {
        prefs.edit()
                .putString(KEY_PET_PAPEL, papel != null ? papel : "")
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

    public String obterPetPapel() {
        return prefs.getString(KEY_PET_PAPEL, "DONO");
    }

    public boolean obterPetEhDono() {
        String p = obterPetPapel();
        return p == null || p.isEmpty() || p.equalsIgnoreCase("DONO");
    }

    public boolean isDonoDoPetSelecionado() {
        return obterPetEhDono();
    }

    public boolean temPetSalvo() {
        return obterPetId() > 0 || !obterPetNome().isEmpty();
    }

    // ==================== SESSÃO ====================

    public void limparSessao() {
        prefs.edit().clear().apply();
    }
}