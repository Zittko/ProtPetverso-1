package com.example.protpetverso_1.data;

import com.example.protpetverso_1.pet.PetResumo;
import com.example.protpetverso_1.schedule.Tarefa;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache em memória de tudo que veio da API nesta sessão.
 * Limpar sempre no logout ({@link #limparTudo()}).
 *
 * Pets: preferir setMeusPets / getMeusPets / temMeusPets.
 * Aliases setListaPets / getListaPets / temListaPets apontam para a mesma lista
 * (compatível com HomeFragment e outros usos).
 */
public final class AppDataCache {

    private static final AppDataCache INSTANCE = new AppDataCache();

    public static AppDataCache get() {
        return INSTANCE;
    }

    private AppDataCache() {}

    // ---- Usuário ----
    private JSONObject usuarioPerfilJson;
    private String usuarioNome;
    private String usuarioApelido;
    private String usuarioEmail;
    private String usuarioTelefone;
    private String usuarioFotoBase64;

    // ---- Lista de pets (meusPets da API) ----
    private final List<PetResumo> meusPets = new ArrayList<>();

    // ---- Perfil do pet (por id) ----
    private final Map<Long, JSONObject> perfilPetPorId = new HashMap<>();

    // ---- Tarefas do pet (por id) ----
    private final Map<Long, List<Tarefa>> tarefasPorPetId = new HashMap<>();

    // ==================== USUÁRIO ====================

    public void setUsuarioPerfil(JSONObject json) {
        this.usuarioPerfilJson = json;
        if (json == null) return;
        usuarioNome = json.optString("nome", "");
        usuarioApelido = json.optString("apelido", "");
        usuarioEmail = json.optString("email", "");
        usuarioTelefone = json.optString("telefone", "");
        usuarioFotoBase64 = json.optString("fotoBase64", "");
    }

    public boolean temUsuario() {
        return usuarioPerfilJson != null;
    }

    public String getUsuarioNome() {
        return usuarioNome != null ? usuarioNome : "";
    }

    public String getUsuarioApelido() {
        return usuarioApelido != null ? usuarioApelido : "";
    }

    public String getUsuarioEmail() {
        return usuarioEmail != null ? usuarioEmail : "";
    }

    public String getUsuarioTelefone() {
        return usuarioTelefone != null ? usuarioTelefone : "";
    }

    public String getUsuarioFotoBase64() {
        return usuarioFotoBase64;
    }

    public JSONObject getUsuarioPerfilJson() {
        return usuarioPerfilJson;
    }

    // ==================== PETS ====================

    public void setMeusPets(List<PetResumo> lista) {
        meusPets.clear();
        if (lista != null) {
            meusPets.addAll(lista);
        }
    }

    public List<PetResumo> getMeusPets() {
        return new ArrayList<>(meusPets);
    }

    public boolean temMeusPets() {
        return !meusPets.isEmpty();
    }

    /** Alias de {@link #setMeusPets(List)} — HomeFragment / nomes antigos. */
    public void setListaPets(List<PetResumo> lista) {
        setMeusPets(lista);
    }

    /** Alias de {@link #getMeusPets()}. */
    public List<PetResumo> getListaPets() {
        return getMeusPets();
    }

    /** Alias de {@link #temMeusPets()}. */
    public boolean temListaPets() {
        return temMeusPets();
    }

    public void setPerfilPet(long petId, JSONObject json) {
        if (petId > 0 && json != null) {
            perfilPetPorId.put(petId, json);
        }
    }

    public JSONObject getPerfilPet(long petId) {
        return perfilPetPorId.get(petId);
    }

    public boolean temPerfilPet(long petId) {
        return perfilPetPorId.containsKey(petId);
    }

    // ==================== TAREFAS ====================

    public void setTarefas(long petId, List<Tarefa> tarefas) {
        if (petId <= 0) return;
        tarefasPorPetId.put(petId, tarefas != null ? new ArrayList<>(tarefas) : new ArrayList<>());
    }

    public List<Tarefa> getTarefas(long petId) {
        List<Tarefa> list = tarefasPorPetId.get(petId);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    /**
     * true se já houve GET de tarefas para esse pet nesta sessão
     * (mesmo que a lista esteja vazia).
     */
    public boolean temTarefas(long petId) {
        return tarefasPorPetId.containsKey(petId);
    }

    // ==================== LIMPAR ====================

    /** Chamar no logout. */
    public void limparTudo() {
        usuarioPerfilJson = null;
        usuarioNome = null;
        usuarioApelido = null;
        usuarioEmail = null;
        usuarioTelefone = null;
        usuarioFotoBase64 = null;
        meusPets.clear();
        perfilPetPorId.clear();
        tarefasPorPetId.clear();
    }
}