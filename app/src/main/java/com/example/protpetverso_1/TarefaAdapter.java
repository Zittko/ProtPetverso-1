package com.example.protpetverso_1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalTime;
import java.util.List;

public class TarefaAdapter extends RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder> {

    private List<Tarefa> listaTarefas;
    private Context context;

    public TarefaAdapter(Context context, List<Tarefa> listaTarefas) {
        this.context = context;
        this.listaTarefas = listaTarefas;
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

        // ===== Título com horário =====
        String tituloCompleto;
        if (tarefa.getHorario() != null) {
            // Formata o horário (ex: 08:00)
            LocalTime horario = tarefa.getHorario();
            String horaFormatada = String.format("%02d:%02d", horario.getHour(), horario.getMinute());
            tituloCompleto = horaFormatada + " - " + tarefa.getTitulo();
        } else {
            tituloCompleto = tarefa.getTitulo();
        }
        holder.titleTarefa.setText(tituloCompleto);

        // ===== Descrição =====
        holder.descTarefa.setText(tarefa.getDesc());

        // ===== Imagem =====
        holder.imgTarefa.setImageResource(tarefa.getImagemResId());

        // ===== Status (por enquanto fixo - depois melhoramos) =====
        // Se quiser, depois adicionamos um campo "concluida" na classe Tarefa
        holder.imgStatus.setImageResource(android.R.drawable.checkbox_off_background);

        // ===== Rodapé (por enquanto genérico) =====
        holder.txtRodape.setText("");
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