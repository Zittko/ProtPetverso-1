package com.example.protpetverso_1;
// Define o pacote do projeto.

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
// Imports necessários para criar views e acessar os componentes do layout do item.

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
// Base do Adapter e do ViewHolder da RecyclerView.

import java.util.List;
// Tipo da lista de alertas de saúde.

public class AlertaSaudeAdapter extends RecyclerView.Adapter<AlertaSaudeAdapter.AlertaSaudeViewHolder> {
// Adapter responsável por ligar a lista de AlertaSaude com a RecyclerView.
// É quem cria e preenche cada card de alerta de saúde.

    private List<AlertaSaude> listaAlertasSaude;
    // Lista de dados que será exibida na RecyclerView.
    // Vem do pet atual (petAtual.getAlertasSaude()).

    public AlertaSaudeAdapter(Context applicationContext, List<AlertaSaude> listaAlertasSaude) {
        // Construtor. Recebe a lista de alertas que deve ser mostrada.
        this.listaAlertasSaude = listaAlertasSaude;
    }

    @NonNull
    @Override
    public AlertaSaudeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Chamado quando a RecyclerView precisa criar um novo card (item).
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarefa_card, parent, false);
        // Infla o layout do card (item_tarefa_card.xml).
        // Observação: está reutilizando o mesmo layout das tarefas.
        return new AlertaSaudeViewHolder(view);
        // Retorna o ViewHolder que guarda as referências dos componentes do card.
    }

    @Override
    public void onBindViewHolder(@NonNull AlertaSaudeViewHolder holder, int position) {
        // Chamado para preencher os dados de um card específico.
        AlertaSaude alertaSaude = listaAlertasSaude.get(position);
        // Pega o alerta da posição atual.

        holder.titleTarefa.setText(alertaSaude.getTitulo());
        // Coloca o título no TextView do card.

        holder.descTarefa.setText(alertaSaude.getDesc());
        // Coloca a descrição no TextView do card.

        holder.imgTarefa.setImageResource(alertaSaude.getImagemResId());
        // Coloca a imagem/ícone no ImageView do card.
    }

    @Override
    public int getItemCount() {
        return listaAlertasSaude.size();
        // Informa quantos itens a RecyclerView deve mostrar.
        // Se retornar 0, nada aparece.
    }

    static class AlertaSaudeViewHolder extends RecyclerView.ViewHolder {
        // Guarda as referências dos componentes visuais de cada card.
        // Evita ficar chamando findViewById várias vezes.

        TextView titleTarefa, descTarefa;
        ImageView imgTarefa;

        public AlertaSaudeViewHolder(@NonNull View itemView) {
            super(itemView);
            // Liga os componentes do XML com as variáveis Java.
            imgTarefa = itemView.findViewById(R.id.imgTarefa);
            titleTarefa = itemView.findViewById(R.id.titleTarefa);
            descTarefa = itemView.findViewById(R.id.descTarefa);
        }
    }
}