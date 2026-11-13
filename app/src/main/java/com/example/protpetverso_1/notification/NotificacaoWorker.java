package com.example.protpetverso_1.notification;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.RequestFuture;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.VolleySingleton;
import com.example.protpetverso_1.data.AppDataCache;
import com.example.protpetverso_1.pet.PetResumo;
import com.example.protpetverso_1.vaccine.Vacina;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Dispara, no horário agendado, a notificação do tipo recebido em CHAVE_TIPO. */
public class NotificacaoWorker extends Worker {

    public static final String CHAVE_TIPO = "tipo";

    public NotificacaoWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context c = getApplicationContext();
        SessionManager sm = new SessionManager(c);
        if (!sm.isLogado()) return Result.success(); // sem sessão não há o que lembrar

        NotificacaoPrefs prefs = new NotificacaoPrefs(c);
        String tipo = getInputData().getString(CHAVE_TIPO);
        if (tipo == null) return Result.success();

        switch (tipo) {
            case NotificacaoLocal.TIPO_ALIMENTACAO:
                if (prefs.alimentacaoAtiva()) NotificacaoHelper.notificarAlimentacao(c);
                break;
            case NotificacaoLocal.TIPO_PASSEIO:
                if (prefs.passeioAtivo()) NotificacaoHelper.notificarPasseio(c);
                break;
            case NotificacaoLocal.TIPO_VACINAS:
                if (prefs.vacinasAtivo()) verificarVacinas(c, sm);
                break;
            default:
                break;
        }
        return Result.success();
    }

    /** Uma notificação por pet com MAIS de VACINAS_LIMITE vacinas pendentes ou atrasadas. */
    private void verificarVacinas(Context c, SessionManager sm) {
        AppDataCache cache = AppDataCache.get();
        cache.init(c);
        cache.carregarDoDisco();
        String token = sm.obterToken();

        List<PetResumo> pets = cache.getMeusPets();
        if (pets.isEmpty()) pets = buscarPets(c, token);

        for (PetResumo pet : pets) {
            int total = contarPendentes(c, token, pet.getIdPet());
            if (total > NotificacaoPrefs.VACINAS_LIMITE) NotificacaoHelper.notificarVacinas(c, pet.getNome(), pet.getIdPet(), total);
        }
    }

    /** Consulta a API (dados atuais); se falhar, usa as vacinas guardadas no aparelho. */
    private int contarPendentes(Context c, String token, long idPet) {
        JSONArray arr = pedirArray(c, token, ApiConfig.URL_VACINAS_DO_PET + idPet + "/vacinas");
        List<Vacina> vacinas = new ArrayList<>();
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o != null) vacinas.add(Vacina.fromJson(o));
            }
        } else {
            vacinas = AppDataCache.get().getVacinas(idPet);
        }
        int total = 0;
        for (Vacina v : vacinas) if (v != null && v.isPendenteOuAtrasada()) total++;
        return total;
    }

    private List<PetResumo> buscarPets(Context c, String token) {
        List<PetResumo> lista = new ArrayList<>();
        JSONArray arr = pedirArray(c, token, ApiConfig.URL_MEUS_PETS);
        if (arr == null) return lista;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;
            lista.add(new PetResumo(o.optLong("idPet", -1), o.optString("nome", "pet"),
                    o.optString("papel", ""), null));
        }
        return lista;
    }

    /** GET síncrono (este código roda em thread de trabalho, nunca na principal). */
    private JSONArray pedirArray(Context c, String token, String url) {
        try {
            RequestFuture<JSONArray> futuro = RequestFuture.newFuture();
            JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null, futuro, futuro) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> h = new HashMap<>();
                    h.put("Authorization", "Bearer " + token);
                    return h;
                }
            };
            VolleySingleton.getInstance(c).addToRequestQueue(req);
            return futuro.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
    }
}
