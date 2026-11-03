package com.example.protpetverso_1;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.security.KeyStore;

/**
 * Sessão do usuário + último pet selecionado.
 *
 * As preferências são criptografadas (EncryptedSharedPreferences). Se o Keystore não
 * conseguir abri-las (AEADBadTagException / VERIFICATION_FAILED — chave do Keystore não
 * corresponde mais ao arquivo salvo), os dados antigos são irrecuperáveis: apagamos o
 * arquivo e a chave corrompidos e recriamos. Só se isso também falhar usamos prefs normais.
 *
 * O SharedPreferences é criado uma única vez por processo (as Activities instanciam
 * SessionManager várias vezes; antes, cada instância repetia a tentativa e o erro).
 */
public class SessionManager {

    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "user_session";
    private static final String PREF_NAME_FALLBACK = "user_session_fallback";

    // Nomes internos usados pela biblioteca androidx.security.crypto
    private static final String KEYSET_PREF_FILE = "__androidx_security_crypto_encrypted_prefs__";
    private static final String MASTER_KEY_ALIAS = MasterKey.DEFAULT_MASTER_KEY_ALIAS;

    private static final Object LOCK = new Object();
    private static volatile SharedPreferences cachedPrefs;

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
        this.prefs = obterPrefs(context.getApplicationContext());
    }

    private static SharedPreferences obterPrefs(Context appContext) {
        SharedPreferences local = cachedPrefs;
        if (local != null) return local;

        synchronized (LOCK) {
            if (cachedPrefs != null) return cachedPrefs;

            SharedPreferences p;
            try {
                p = criarPrefsCriptografadas(appContext);
            } catch (Exception e) {
                Log.w(TAG, "EncryptedSharedPreferences ilegível (chave do Keystore não confere com o "
                        + "arquivo salvo). Limpando e recriando; será necessário fazer login novamente.", e);
                try {
                    limparDadosCriptografadosCorrompidos(appContext);
                    p = criarPrefsCriptografadas(appContext);
                } catch (Exception e2) {
                    Log.e(TAG, "Não foi possível recriar as prefs criptografadas — usando fallback.", e2);
                    p = appContext.getSharedPreferences(PREF_NAME_FALLBACK, Context.MODE_PRIVATE);
                }
            }
            cachedPrefs = p;
            return p;
        }
    }

    private static SharedPreferences criarPrefsCriptografadas(Context appContext) throws Exception {
        MasterKey masterKey = new MasterKey.Builder(appContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                appContext,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    /** Remove o arquivo de prefs, o keyset do Tink e a master key do Keystore. */
    private static void limparDadosCriptografadosCorrompidos(Context appContext) {
        try {
            appContext.deleteSharedPreferences(PREF_NAME);
        } catch (Exception e) {
            Log.w(TAG, "Falha ao apagar " + PREF_NAME, e);
        }
        try {
            appContext.deleteSharedPreferences(KEYSET_PREF_FILE);
        } catch (Exception e) {
            Log.w(TAG, "Falha ao apagar keyset", e);
        }
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            if (ks.containsAlias(MASTER_KEY_ALIAS)) {
                ks.deleteEntry(MASTER_KEY_ALIAS);
            }
        } catch (Exception e) {
            Log.w(TAG, "Falha ao apagar a master key do Keystore", e);
        }
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

    /** Esquece o pet selecionado (usado quando a API diz que o usuário não tem mais nenhum pet). */
    public void limparPet() {
        prefs.edit()
                .remove(KEY_PET_ID)
                .remove(KEY_PET_NOME)
                .remove(KEY_PET_RACA)
                .remove(KEY_PET_ESPECIE)
                .remove(KEY_PET_PESO)
                .remove(KEY_PET_SEXO)
                .remove(KEY_PET_PORTE)
                .remove(KEY_PET_DATA)
                .remove(KEY_PET_PAPEL)
                .apply();
    }

    // ==================== SESSÃO ====================

    public void limparSessao() {
        prefs.edit().clear().apply();
    }
}