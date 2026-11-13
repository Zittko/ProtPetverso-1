package com.example.protpetverso_1.schedule;

import com.example.protpetverso_1.R;

public final class TarefaIconHelper {

    private TarefaIconHelper() {}

    public static int resolverIcone(String titulo, String desc) {
        String t = ((titulo != null ? titulo : "") + " " + (desc != null ? desc : ""))
                .toLowerCase();

        if (t.contains("banho") || t.contains("tosa") || t.contains("unha") || t.contains("lavar") || t.contains("petshop")) {
            return R.drawable.ic_tarefa_banho;
        }
        if (t.contains("ração") || t.contains("racao") || t.contains("comida")
                || t.contains("alimentar") || t.contains("agua") || t.contains("água")) {
            return R.drawable.ic_tarefa_comida;
        }
        if (t.contains("passeio") || t.contains("caminhada") || t.contains("passear") || t.contains("parque")
                || t.contains("andar")) {
            return R.drawable.ic_tarefa_passeio;
        }
        if (t.contains("vacina") || t.contains("vet") || t.contains("consulta")
                || t.contains("remédio") || t.contains("remedio") || t.contains("veterinário") || t.contains("veterinario")) {
            return R.drawable.ic_tarefa_vet;
        }
        return R.drawable.ic_tarefa_default;
    }
}