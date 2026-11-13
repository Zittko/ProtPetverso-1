package com.example.protpetverso_1.notification;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.protpetverso_1.MenuActivity;
import com.example.protpetverso_1.R;

/** Textos padrão, canais e exibição das notificações do aparelho (e registro na lista do sino). */
public final class NotificacaoHelper {

    public static final String CANAL_LEMBRETES = "lembretes";
    public static final String CANAL_VACINAS = "vacinas";
    public static final String EXTRA_ABRIR_NOTIFICACOES = "ABRIR_NOTIFICACOES";

    // Título como alerta + descrição curta explicando o motivo
    public static final String TITULO_ALIMENTACAO = "Verifique se há água e ração";
    public static final String DESC_ALIMENTACAO =
            "Não esqueça de alimentar o seu pet periodicamente, é importante para a energia e saúde do seu animal.";

    public static final String TITULO_PASSEIO = "Leve o seu pet para passear";
    public static final String DESC_PASSEIO =
            "Passeios regulares ajudam a gastar energia, mantêm o corpo ativo e fortalecem o vínculo com você.";

    public static final String DESC_VACINAS =
            "Atenção à saúde do seu pet: confira a carteira de vacinas e mantenha as doses em dia.";

    private NotificacaoHelper() {}

    public static String tituloVacinas(String nomePet, int quantidade) {
        String nome = (nomePet == null || nomePet.trim().isEmpty()) ? "pet" : nomePet.trim();
        return "O " + nome + " possui " + quantidade
                + (quantidade == 1 ? " vacina pendente ou atrasada" : " vacinas pendentes ou atrasadas");
    }

    public static void criarCanais(Context c) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        if (nm == null) return;
        nm.createNotificationChannel(new NotificationChannel(
                CANAL_LEMBRETES, "Lembretes de rotina", NotificationManager.IMPORTANCE_DEFAULT));
        nm.createNotificationChannel(new NotificationChannel(
                CANAL_VACINAS, "Vacinas pendentes ou atrasadas", NotificationManager.IMPORTANCE_HIGH));
    }

    public static boolean podeNotificar(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return NotificationManagerCompat.from(c).areNotificationsEnabled();
    }

    public static void notificarAlimentacao(Context c) {
        notificar(c, NotificacaoLocal.TIPO_ALIMENTACAO, TITULO_ALIMENTACAO, DESC_ALIMENTACAO, -1, CANAL_LEMBRETES);
    }

    public static void notificarPasseio(Context c) {
        notificar(c, NotificacaoLocal.TIPO_PASSEIO, TITULO_PASSEIO, DESC_PASSEIO, -1, CANAL_LEMBRETES);
    }

    public static void notificarVacinas(Context c, String nomePet, long idPet, int quantidade) {
        notificar(c, NotificacaoLocal.TIPO_VACINAS, tituloVacinas(nomePet, quantidade), DESC_VACINAS,
                idPet, CANAL_VACINAS);
    }

    /** Registra na lista do sino (sempre) e mostra no aparelho (se a permissão estiver concedida). */
    public static void notificar(Context ctx, String tipo, String titulo, String descricao,
                                 long idPet, String canal) {
        Context c = ctx.getApplicationContext();
        NotificacaoLocal n = NotificacaoStore.adicionar(c, tipo, titulo, descricao, idPet);
        if (!podeNotificar(c)) return;

        criarCanais(c);
        Intent abrir = new Intent(c, MenuActivity.class)
                .putExtra(EXTRA_ABRIR_NOTIFICACOES, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(c, (int) (n.getId() % Integer.MAX_VALUE), abrir,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(c, canal)
                .setSmallIcon(R.drawable.baseline_notifications)
                .setContentTitle(titulo)
                .setContentText(descricao)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(descricao))
                .setColor(0xFFDF945D)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(CANAL_VACINAS.equals(canal)
                        ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_DEFAULT);

        // id por tipo e pet: a mesma notificação do mesmo pet substitui a anterior no aparelho
        int idNotif = (tipo + "_" + idPet).hashCode();
        try {
            NotificationManagerCompat.from(c).notify(idNotif, b.build());
        } catch (SecurityException ignored) { }
    }
}
