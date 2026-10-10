package com.example.protpetverso_1.schedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.protpetverso_1.R;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TarefaAdapter extends RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder> {

    private final List<Tarefa> listaTarefas = new ArrayList<>();
    private final Context context;

    public TarefaAdapter(Context context, List<Tarefa> listaTarefas) {
        this.context = context;
        if (listaTarefas != null) {
            this.listaTarefas.addAll(listaTarefas);
        }
    }

    /** Atualiza a lista exibida (após GET da API). */
    public void setItens(List<Tarefa> novas) {
        listaTarefas.clear();
        if (novas != null) listaTarefas.addAll(novas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TarefaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarefa_card, parent, false);
        return new TarefaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TarefaViewHolder holder, int position) {
        Tarefa tarefa = listaTarefas.get(position);

        // Título com horário
        if (tarefa.getHorario() != null) {
            LocalTime h = tarefa.getHorario();
            String horaFormatada = String.format("%02d:%02d", h.getHour(), h.getMinute());
            holder.titleTarefa.setText(horaFormatada + " - " + tarefa.getTitulo());
        } else {
            holder.titleTarefa.setText(tarefa.getTitulo());
        }

        holder.descTarefa.setText(tarefa.getDesc());
        holder.imgTarefa.setImageResource(
                tarefa.getImagemResId() != 0
                        ? tarefa.getImagemResId()
                        : android.R.drawable.ic_menu_my_calendar
        );

        // Status concluída
        if (tarefa.isConcluida()) {
            holder.imgStatus.setImageResource(android.R.drawable.checkbox_on_background);
        } else {
            holder.imgStatus.setImageResource(android.R.drawable.checkbox_off_background);
        }

        // Rodapé: quem executou ou pet
        if (tarefa.isConcluida() && tarefa.getExecutadoPorNome() != null
                && !tarefa.getExecutadoPorNome().isEmpty()) {
            holder.txtRodape.setText("Feito por: " + tarefa.getExecutadoPorNome());
        } else if (tarefa.getNomePet() != null && !tarefa.getNomePet().isEmpty()) {
            holder.txtRodape.setText("Pet: " + tarefa.getNomePet());
        } else {
            holder.txtRodape.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return listaTarefas.size();
    }

    static class TarefaViewHolder extends RecyclerView.ViewHolder {
        TextView titleTarefa, descTarefa, txtRodape;
        ImageView imgTarefa, imgStatus;

        public TarefaViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTarefa = itemView.findViewById(R.id.titleTarefa);
            descTarefa = itemView.findViewById(R.id.descTarefa);
            txtRodape = itemView.findViewById(R.id.txtRodape);
            imgTarefa = itemView.findViewById(R.id.imgTarefa);
            imgStatus = itemView.findViewById(R.id.imgStatus);
        }
    }
}