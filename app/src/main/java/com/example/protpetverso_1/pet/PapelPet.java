package com.example.protpetverso_1.pet;

import android.content.Context;

import com.example.protpetverso_1.conta.SessionManager;
import com.example.protpetverso_1.data.AppDataCache;
public final class PapelPet {

    private PapelPet() {}
    public static boolean ehDono(Context ctx, long idPet) {
        if (idPet > 0) {
            for (PetResumo p : AppDataCache.get().getMeusPets()) {
                if (p != null && p.getIdPet() == idPet) {
                    String papel = p.getPapel();
                    if ("DONO".equalsIgnoreCase(papel)) return true;
                    if ("SUPORTE".equalsIgnoreCase(papel)) return false;
                }
            }
        }
        SessionManager sm = new SessionManager(ctx);
        if (idPet <= 0 || sm.obterPetId() == idPet) return sm.obterPetEhDono();
        return true;
    }
}
