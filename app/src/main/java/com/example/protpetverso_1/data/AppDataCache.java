package com.example.protpetverso_1.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.protpetverso_1.pet.PetResumo;
import com.example.protpetverso_1.schedule.Tarefa;
import com.example.protpetverso_1.vaccine.Vacina;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AppDataCache {

    private static final String TAG = "AppDataCache";
    private static final String PREF = "petverso_disk_cache";
    private static final String KEY_PETS = "meus_pets_json";
    private static final String KEY_USER_JSON = "usuario_perfil_json";
    private static final String KEY_TAREFAS_PREFIX = "tarefas_pet_";
    private static final String KEY_VACINAS_PREFIX = "vacinas_pet_";
    private static final String KEY_PERFIL_PET_PREFIX = "perfil_pet_";
    private static final String KEY_TAREFAS_DIA_PREFIX = "tarefas_dia_";

    private static final AppDataCache INSTANCE = new AppDataCache();

    public static AppDataCache get() {
        return INSTANCE;
    }

    private AppDataCache() {}

    private Context appContext;
    private SharedPreferences disk;

    private JSONObject usuarioPerfilJson;
    private String usuarioNome;
    private String usuarioApelido;
    private String usuarioEmail;
    private String usuarioTelefone;
    private String usuarioFotoBase64;
    private String usuarioFotoPath;

    private final List<PetResumo> meusPets = new ArrayList<>();
    private final Map<Long, JSONObject> perfilPetPorId = new HashMap<>();
    private final Map<Long, List<Tarefa>> tarefasPorPetId = new HashMap<>();
    private final Map<Long, List<Vacina>> vacinasPorPetId = new HashMap<>();
    private final Map<String, List<Tarefa>> tarefasPorDia = new HashMap<>();

    public synchronized void init(Context context) {
        if (context == null) return;
        this.appContext = context.getApplicationContext();
        this.disk = appContext.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    private boolean ready() {
        return appContext != null && disk != null;
    }

    // ==================== DISCO ====================

    public synchronized void carregarDoDisco() {
        if (!ready()) {
            Log.w(TAG, "init() não chamado");
            return;
        }
        carregarUsuarioDoDisco();
        carregarPetsDoDisco();
        carregarMapasDoDisco();
        Log.d(TAG, "Disco OK pets=" + meusPets.size()
                + " perfis=" + perfilPetPorId.size()
                + " diasAgenda=" + tarefasPorDia.size());
    }

    private void carregarUsuarioDoDisco() {
        String raw = disk.getString(KEY_USER_JSON, null);
        if (raw == null || raw.isEmpty()) return;
        try {
            JSONObject json = new JSONObject(raw);
            usuarioPerfilJson = json;
            usuarioNome = json.optString("nome", "");
            usuarioApelido = json.optString("apelido", "");
            usuarioEmail = json.optString("email", "");
            usuarioTelefone = json.optString("telefone", "");
            usuarioFotoBase64 = json.optString("fotoBase64", "");
            usuarioFotoPath = json.optString("fotoPath", "");
        } catch (Exception e) {
            Log.e(TAG, "user disk", e);
        }
    }

    private void carregarPetsDoDisco() {
        String raw = disk.getString(KEY_PETS, null);
        if (raw == null || raw.isEmpty()) return;
        try {
            JSONArray arr = new JSONArray(raw);
            List<PetResumo> lista = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                long id = o.optLong("idPet", -1);
                if (id <= 0) continue;
                lista.add(new PetResumo(
                        id,
                        o.optString("nome", "Pet"),
                        o.optString("papel", ""),
                        null,
                        o.optString("fotoPath", "")
                ));
            }
            meusPets.clear();
            meusPets.addAll(lista);
        } catch (Exception e) {
            Log.e(TAG, "pets disk", e);
        }
    }

    private void carregarMapasDoDisco() {
        Map<String, ?> all = disk.getAll();
        for (String key : all.keySet()) {
            Object val = all.get(key);
            if (val == null) continue;
            String raw = String.valueOf(val);
            try {
                if (key.startsWith(KEY_TAREFAS_PREFIX)) {
                    long id = parseId(key, KEY_TAREFAS_PREFIX);
                    if (id > 0) tarefasPorPetId.put(id, lerTarefasJson(raw));
                } else if (key.startsWith(KEY_VACINAS_PREFIX)) {
                    long id = parseId(key, KEY_VACINAS_PREFIX);
                    if (id > 0) vacinasPorPetId.put(id, lerVacinasJson(raw));
                } else if (key.startsWith(KEY_PERFIL_PET_PREFIX)) {
                    long id = parseId(key, KEY_PERFIL_PET_PREFIX);
                    if (id > 0) perfilPetPorId.put(id, new JSONObject(raw));
                } else if (key.startsWith(KEY_TAREFAS_DIA_PREFIX)) {
                    String dia = key.substring(KEY_TAREFAS_DIA_PREFIX.length());
                    tarefasPorDia.put(dia, lerTarefasJson(raw));
                }
            } catch (Exception e) {
                Log.e(TAG, "mapa disk " + key, e);
            }
        }
    }

    private long parseId(String key, String prefix) {
        try {
            return Long.parseLong(key.substring(prefix.length()));
        } catch (Exception e) {
            return -1;
        }
    }

    // ==================== USUÁRIO ====================

    public void setUsuarioPerfil(JSONObject json) {
        this.usuarioPerfilJson = json;
        if (json == null) return;
        usuarioNome = json.optString("nome", "");
        usuarioApelido = json.optString("apelido", "");
        usuarioEmail = json.optString("email", "");
        usuarioTelefone = json.optString("telefone", "");
        usuarioFotoBase64 = json.optString("fotoBase64", "");

        if (ready() && usuarioFotoBase64 != null && !usuarioFotoBase64.isEmpty()) {
            long uid = json.optLong("idUsuario", json.optLong("id", 0));
            String path = PhotoDiskCache.salvarFotoUsuario(appContext, uid, usuarioFotoBase64);
            if (path != null) usuarioFotoPath = path;
        }
        persistirUsuario();
    }

    private void persistirUsuario() {
        if (!ready()) return;
        try {
            JSONObject o = usuarioPerfilJson != null
                    ? new JSONObject(usuarioPerfilJson.toString())
                    : new JSONObject();
            o.put("nome", usuarioNome != null ? usuarioNome : "");
            o.put("apelido", usuarioApelido != null ? usuarioApelido : "");
            o.put("email", usuarioEmail != null ? usuarioEmail : "");
            o.put("telefone", usuarioTelefone != null ? usuarioTelefone : "");
            o.put("fotoPath", usuarioFotoPath != null ? usuarioFotoPath : "");
            o.remove("fotoBase64");
            disk.edit().putString(KEY_USER_JSON, o.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "persist user", e);
        }
    }

    public boolean temUsuario() {
        return usuarioPerfilJson != null
                || (usuarioNome != null && !usuarioNome.isEmpty())
                || (usuarioEmail != null && !usuarioEmail.isEmpty());
    }

    public JSONObject getUsuarioPerfilJson() {
        return usuarioPerfilJson;
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
        return usuarioFotoBase64 != null ? usuarioFotoBase64 : "";
    }

    public String getUsuarioFotoPath() {
        return usuarioFotoPath;
    }

    // ==================== PETS ====================

    public void setMeusPets(List<PetResumo> lista) {
        meusPets.clear();
        if (lista != null) {
            for (PetResumo p : lista) {
                if (p == null || p.getIdPet() <= 0) continue;
                String path = p.getFotoPath();
                if (ready() && p.getFotoPetBase64() != null
                        && !p.getFotoPetBase64().trim().isEmpty()) {
                    String saved = PhotoDiskCache.salvarFotoPet(
                            appContext, p.getIdPet(), p.getFotoPetBase64());
                    if (saved != null) path = saved;
                } else if ((path == null || path.isEmpty()) && ready()) {
                    java.io.File f = PhotoDiskCache.arquivoPet(appContext, p.getIdPet());
                    if (f.isFile() && f.length() > 0) path = f.getAbsolutePath();
                }
                meusPets.add(new PetResumo(
                        p.getIdPet(), p.getNome(), p.getPapel(),
                        p.getFotoPetBase64(), path));
            }
        }
        persistirPets();
    }

    private void persistirPets() {
        if (!ready()) return;
        try {
            JSONArray arr = new JSONArray();
            for (PetResumo p : meusPets) {
                JSONObject o = new JSONObject();
                o.put("idPet", p.getIdPet());
                o.put("nome", p.getNome());
                o.put("papel", p.getPapel());
                o.put("fotoPath", p.getFotoPath() != null ? p.getFotoPath() : "");
                arr.put(o);
            }
            disk.edit().putString(KEY_PETS, arr.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "persist pets", e);
        }
    }

    public List<PetResumo> getMeusPets() {
        return new ArrayList<>(meusPets);
    }

    public boolean temMeusPets() {
        return !meusPets.isEmpty();
    }

    public void setListaPets(List<PetResumo> lista) {
        setMeusPets(lista);
    }

    public List<PetResumo> getListaPets() {
        return getMeusPets();
    }

    public boolean temListaPets() {
        return temMeusPets();
    }

    public void setPerfilPet(long petId, JSONObject json) {
        if (petId <= 0 || json == null) return;
        perfilPetPorId.put(petId, json);
        if (!ready()) return;
        try {
            JSONObject copy = new JSONObject(json.toString());
            String foto = copy.optString("fotoPetBase64", "");
            if (foto.isEmpty()) foto = copy.optString("fotoBase64", "");
            if (!foto.isEmpty()) {
                String path = PhotoDiskCache.salvarFotoPet(appContext, petId, foto);
                if (path != null) copy.put("fotoPath", path);
            }
            copy.remove("fotoPetBase64");
            copy.remove("fotoBase64");
            disk.edit().putString(KEY_PERFIL_PET_PREFIX + petId, copy.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "persist perfil pet", e);
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
        tarefasPorPetId.put(petId,
                tarefas != null ? new ArrayList<>(tarefas) : new ArrayList<>());
        if (!ready()) return;
        disk.edit()
                .putString(KEY_TAREFAS_PREFIX + petId, tarefasParaJson(tarefasPorPetId.get(petId)))
                .apply();
    }

    public List<Tarefa> getTarefas(long petId) {
        List<Tarefa> list = tarefasPorPetId.get(petId);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public boolean temTarefas(long petId) {
        return tarefasPorPetId.containsKey(petId);
    }

    public void setTarefasDoDia(LocalDate dia, List<Tarefa> lista) {
        if (dia == null) return;
        tarefasPorDia.put(dia.toString(),
                lista != null ? new ArrayList<>(lista) : new ArrayList<>());
        persistirTarefasDoDiaEmDisco(dia);
    }

    public void persistirTarefasDoDiaEmDisco(LocalDate dia) {
        if (!ready() || dia == null) return;
        List<Tarefa> lista = tarefasPorDia.get(dia.toString());
        disk.edit()
                .putString(KEY_TAREFAS_DIA_PREFIX + dia, tarefasParaJson(lista))
                .apply();
    }

    public List<Tarefa> getTarefasDoDia(LocalDate dia) {
        if (dia == null) return new ArrayList<>();
        List<Tarefa> l = tarefasPorDia.get(dia.toString());
        return l != null ? new ArrayList<>(l) : new ArrayList<>();
    }

    public boolean temTarefasDoDia(LocalDate dia) {
        return dia != null && tarefasPorDia.containsKey(dia.toString());
    }

    public void invalidarTarefasDoDia() {
        tarefasPorDia.clear();
    }

    private String tarefasParaJson(List<Tarefa> lista) {
        JSONArray arr = new JSONArray();
        if (lista == null) return arr.toString();
        try {
            for (Tarefa t : lista) {
                if (t == null) continue;
                JSONObject o = new JSONObject();
                long id = t.getIdTarefa();
                try {
                    if (id <= 0) id = t.getId();
                } catch (Throwable ignored) {}
                o.put("idTarefa", id);
                o.put("titulo", t.getTitulo());
                o.put("descricao", t.getDesc());
                o.put("concluida", t.isConcluida());
                o.put("idPet", t.getIdPet());
                o.put("nomePet", t.getNomePet() != null ? t.getNomePet() : "");
                if (t.getData() != null) o.put("dataTarefa", t.getData().toString());
                if (t.getHorario() != null) o.put("hora", t.getHorario().toString());
                arr.put(o);
            }
        } catch (Exception e) {
            Log.e(TAG, "tarefas json", e);
        }
        return arr.toString();
    }

    private List<Tarefa> lerTarefasJson(String raw) {
        List<Tarefa> out = new ArrayList<>();
        if (raw == null || raw.isEmpty() || "null".equals(raw)) return out;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                out.add(Tarefa.fromJson(arr.getJSONObject(i)));
            }
        } catch (Exception e) {
            Log.e(TAG, "ler tarefas", e);
        }
        return out;
    }

    // ==================== VACINAS ====================

    public void setVacinas(long petId, List<Vacina> lista) {
        if (petId <= 0) return;
        vacinasPorPetId.put(petId, lista != null ? new ArrayList<>(lista) : new ArrayList<>());
        if (!ready()) return;
        disk.edit()
                .putString(KEY_VACINAS_PREFIX + petId, vacinasParaJson(vacinasPorPetId.get(petId)))
                .apply();
    }

    public List<Vacina> getVacinas(long petId) {
        List<Vacina> l = vacinasPorPetId.get(petId);
        return l != null ? new ArrayList<>(l) : new ArrayList<>();
    }

    public boolean temVacinas(long petId) {
        return vacinasPorPetId.containsKey(petId);
    }

    public void invalidarVacinas(long petId) {
        vacinasPorPetId.remove(petId);
        if (ready()) disk.edit().remove(KEY_VACINAS_PREFIX + petId).apply();
    }

    private String vacinasParaJson(List<Vacina> lista) {
        JSONArray arr = new JSONArray();
        if (lista == null) return arr.toString();
        try {
            for (Vacina v : lista) {
                if (v == null) continue;
                JSONObject o = new JSONObject();
                o.put("idVacina", v.getIdVacina());
                o.put("nome", v.getNome());
                o.put("observacao", v.getObservacao());
                o.put("dosagemUnica", v.isDosagemUnica());
                o.put("statusVacina", v.getStatusVacina() != null ? v.getStatusVacina() : "");
                if (v.getIntervaloQuantidade() != null) {
                    o.put("intervaloQuantidade", v.getIntervaloQuantidade());
                }
                if (v.getIntervaloUnidade() != null) {
                    o.put("intervaloUnidade", v.getIntervaloUnidade());
                }
                if (v.getDataAplicacao() != null) {
                    o.put("dataAplicacao", v.getDataAplicacao().toString());
                }
                if (v.getDataProximaDose() != null) {
                    o.put("dataProximaDose", v.getDataProximaDose().toString());
                }
                arr.put(o);
            }
        } catch (Exception e) {
            Log.e(TAG, "vacinas json", e);
        }
        return arr.toString();
    }

    private List<Vacina> lerVacinasJson(String raw) {
        List<Vacina> out = new ArrayList<>();
        if (raw == null || raw.isEmpty() || "null".equals(raw)) return out;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                out.add(Vacina.fromJson(arr.getJSONObject(i)));
            }
        } catch (Exception e) {
            Log.e(TAG, "ler vacinas", e);
        }
        return out;
    }

    // ==================== LIMPAR ====================

    public void limparTudo(boolean apagarFotos) {
        usuarioPerfilJson = null;
        usuarioNome = null;
        usuarioApelido = null;
        usuarioEmail = null;
        usuarioTelefone = null;
        usuarioFotoBase64 = null;
        usuarioFotoPath = null;
        meusPets.clear();
        perfilPetPorId.clear();
        tarefasPorPetId.clear();
        vacinasPorPetId.clear();
        tarefasPorDia.clear();
        if (ready()) {
            disk.edit().clear().apply();
            if (apagarFotos) PhotoDiskCache.limparTudo(appContext);
        }
    }

    public void limparTudo() {
        limparTudo(true);
    }
}