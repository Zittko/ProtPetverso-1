package com.example.protpetverso_1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TarefaAdapter extends RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder> {
    private List<Tarefa> listaTarefas;

    public TarefaAdapter(Context applicationContext, List<Tarefa> listaTarefas) {
        this.listaTarefas = listaTarefas;
    }

    @NonNull
    @Override
    public TarefaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarefa_card, parent, false);
        return new TarefaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TarefaViewHolder holder, int position) {
        Tarefa tarefa = listaTarefas.get(position);
        holder.titleTarefa.setText(tarefa.getTitulo());
        holder.descTarefa.setText(tarefa.getDesc());
        holder.imgTarefa.setImageResource(tarefa.getImagemResId());
    }

    @Override
    public int getItemCount() {
        return listaTarefas.size();
    }

    static class TarefaViewHolder extends RecyclerView.ViewHolder {
        TextView titleTarefa, descTarefa;
        ImageView imgTarefa;
        public TarefaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTarefa = itemView.findViewById(R.id.imgTarefa);
            titleTarefa = itemView.findViewById(R.id.titleTarefa);
            descTarefa = itemView.findViewById(R.id.descTarefa);
        }
    }
}
