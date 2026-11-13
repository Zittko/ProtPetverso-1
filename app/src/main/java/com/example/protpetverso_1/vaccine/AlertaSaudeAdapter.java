package com.example.protpetverso_1.vaccine;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.protpetverso_1.R;

import java.util.ArrayList;
import java.util.List;

public class AlertaSaudeAdapter extends RecyclerView.Adapter<AlertaSaudeAdapter.AlertaSaudeViewHolder> {

    private final Context context;
    private final List<AlertaSaude> listaAlertasSaude = new ArrayList<>();

    public AlertaSaudeAdapter(Context context, List<AlertaSaude> lista) {
        this.context = context;
        if (lista != null) listaAlertasSaude.addAll(lista);
    }

    public void setItens(List<AlertaSaude> lista) {
        listaAlertasSaude.clear();
        if (lista != null) listaAlertasSaude.addAll(lista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlertaSaudeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alerta_saude_card, parent, false);
        return new AlertaSaudeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertaSaudeViewHolder holder, int position) {
        AlertaSaude a = listaAlertasSaude.get(position);
        holder.titleSaude.setText(a.getTitulo());
        holder.descSaude.setText(a.getDesc());
        int res = a.getImagemResId() != 0 ? a.getImagemResId() : R.drawable.ic_vacina;
        holder.imgSaude.setImageResource(res);
    }

    @Override
    public int getItemCount() {
        return listaAlertasSaude.size();
    }

    static class AlertaSaudeViewHolder extends RecyclerView.ViewHolder {
        TextView titleSaude, descSaude;
        ImageView imgSaude;

        AlertaSaudeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSaude = itemView.findViewById(R.id.imgSaude);
            titleSaude = itemView.findViewById(R.id.titleSaude);
            descSaude = itemView.findViewById(R.id.descSaude);
        }
    }
}