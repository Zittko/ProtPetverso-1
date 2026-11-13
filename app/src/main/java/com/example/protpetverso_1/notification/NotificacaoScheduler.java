package com.example.protpetverso_1.notification;

import android.content.Context;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * Agenda os lembretes recorrentes com WorkManager (sobrevive a reiniciar o celular).
 * Não há horário fixo: alimentação a cada 8h, passeio a cada 7 dias, vacinas verificadas
 * a cada 24h. O Android pode atrasar um pouco por economia de bateria (é só um lembrete).
 */
public final class NotificacaoScheduler {

    // v2: nomes novos para não herdar os agendamentos antigos (horário fixo)
    private static final String W_ALIMENTACAO = "notif_alimentacao_v2";
    private static final String W_PASSEIO = "notif_passeio_v2";
    private static final String W_VACINAS = "notif_vacinas_v2";
    private static final String[] ANTIGOS = {"notif_alimentacao", "notif_passeio", "notif_vacinas"};

    /** Primeiro disparo logo após ativar (mínimo do WorkManager), depois segue a recorrência. */
    private static final long PRIMEIRO_AVISO_MIN = 15;

    private NotificacaoScheduler() {}

    /**
     * @param substituir true quando o usuário mexeu nas preferências;
     *                   false ao abrir o app (mantém o que já está agendado).
     */
    public static void agendarTodos(Context ctx, boolean substituir) {
        Context c = ctx.getApplicationContext();
        NotificacaoPrefs p = new NotificacaoPrefs(c);
        WorkManager wm = WorkManager.getInstance(c);
        for (String antigo : ANTIGOS) wm.cancelUniqueWork(antigo);
        ExistingPeriodicWorkPolicy politica = substituir
                ? ExistingPeriodicWorkPolicy.UPDATE : ExistingPeriodicWorkPolicy.KEEP;

        if (p.alimentacaoAtiva()) {
            wm.enqueueUniquePeriodicWork(W_ALIMENTACAO, politica,
                    pedido(NotificacaoLocal.TIPO_ALIMENTACAO, NotificacaoPrefs.ALIMENTACAO_A_CADA_HORAS, TimeUnit.HOURS));
        } else {
            wm.cancelUniqueWork(W_ALIMENTACAO);
        }

        if (p.passeioAtivo()) {
            wm.enqueueUniquePeriodicWork(W_PASSEIO, politica,
                    pedido(NotificacaoLocal.TIPO_PASSEIO, NotificacaoPrefs.PASSEIO_A_CADA_DIAS, TimeUnit.DAYS));
        } else {
            wm.cancelUniqueWork(W_PASSEIO);
        }

        if (p.vacinasAtivo()) {
            wm.enqueueUniquePeriodicWork(W_VACINAS, politica,
                    pedido(NotificacaoLocal.TIPO_VACINAS, 24, TimeUnit.HOURS));
        } else {
            wm.cancelUniqueWork(W_VACINAS);
        }
    }

    /** Logout: cancela todos os lembretes. */
    public static void cancelarTodos(Context ctx) {
        WorkManager wm = WorkManager.getInstance(ctx.getApplicationContext());
        wm.cancelUniqueWork(W_ALIMENTACAO);
        wm.cancelUniqueWork(W_PASSEIO);
        wm.cancelUniqueWork(W_VACINAS);
        for (String antigo : ANTIGOS) wm.cancelUniqueWork(antigo);
    }

    private static PeriodicWorkRequest pedido(String tipo, long periodo, TimeUnit unidade) {
        return new PeriodicWorkRequest.Builder(NotificacaoWorker.class, periodo, unidade)
                .setInitialDelay(PRIMEIRO_AVISO_MIN, TimeUnit.MINUTES)
                .setInputData(new Data.Builder().putString(NotificacaoWorker.CHAVE_TIPO, tipo).build())
                .build();
    }
}
