package com.example.protpetverso_1.notification;

import com.example.protpetverso_1.R;
import com.example.protpetverso_1.schedule.TarefaIconHelper;

/**
 * Ícone da notificação conforme o texto do título ou da descrição, igual às tarefas
 * (ração/água, passeio, vacina/veterinário, banho...). Sem palavra-chave, usa o ícone padrão.
 */
public final class NotificacaoIconHelper {

    private NotificacaoIconHelper() {}

    public static int resolverIcone(String titulo, String descricao) {
        return TarefaIconHelper.resolverIcone(titulo, descricao);
    }
}
