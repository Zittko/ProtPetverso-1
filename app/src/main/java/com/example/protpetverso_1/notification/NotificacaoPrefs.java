package com.example.protpetverso_1.notification;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Preferências de notificação: apenas ligar/desligar cada lembrete.
 * Os lembretes são recorrentes (não agendados em horário fixo) e servem só de alerta.
 */
public class NotificacaoPrefs {

    /** Alimentação: a cada 8 horas. */
    public static final int ALIMENTACAO_A_CADA_HORAS = 8;
    /** Passeio: a cada 7 dias. */
    public static final int PASSEIO_A_CADA_DIAS = 7;
    /** Vacinas: verificação diária; só avisa quando o pet tem MAIS de 3 pendentes/atrasadas. */
    public static final int VACINAS_LIMITE = 3;

    private static final String PREF = "petverso_notif_prefs";
    private final SharedPreferences sp;

    public NotificacaoPrefs(Context c) {
        sp = c.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public boolean alimentacaoAtiva() { return sp.getBoolean("alim_on", true); }
    public void alimentacao(boolean on) { sp.edit().putBoolean("alim_on", on).apply(); }

    public boolean passeioAtivo() { return sp.getBoolean("pass_on", true); }
    public void passeio(boolean on) { sp.edit().putBoolean("pass_on", on).apply(); }

    public boolean vacinasAtivo() { return sp.getBoolean("vac_on", true); }
    public void vacinas(boolean on) { sp.edit().putBoolean("vac_on", on).apply(); }

    public boolean permissaoJaPedida() { return sp.getBoolean("perm_pedida", false); }
    public void permissaoPedida() { sp.edit().putBoolean("perm_pedida", true).apply(); }
}
