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

public class AlertaSaudeAdapter extends RecyclerView.Adapter<AlertaSaudeAdapter.AlertaSaudeViewHolder> {
    private List<AlertaSaude> listaAlertasSaude;

    public AlertaSaudeAdapter(Context applicationContext, List<AlertaSaude> listaAlertasSaude) {
        this.listaAlertasSaude = listaAlertasSaude;
    }

    @NonNull
    @Override
    public AlertaSaudeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarefa_card, parent, false);
        return new AlertaSaudeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertaSaudeViewHolder holder, int position) {
        AlertaSaude alertaSaude = listaAlertasSaude.get(position);
        holder.titleTarefa.setText(alertaSaude.getTitulo());
        holder.descTarefa.setText(alertaSaude.getDesc());
        holder.imgTarefa.setImageResource(alertaSaude.getImagemResId());
    }

    @Override
    public int getItemCount() {
        return listaAlertasSaude.size();
    }

    static class AlertaSaudeViewHolder extends RecyclerView.ViewHolder{
        TextView titleTarefa, descTarefa;
        ImageView imgTarefa;
        public AlertaSaudeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTarefa = itemView.findViewById(R.id.imgTarefa);
            titleTarefa = itemView.findViewById(R.id.titleTarefa);
            descTarefa = itemView.findViewById(R.id.descTarefa);
        }
    }
}
