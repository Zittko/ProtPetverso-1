package com.example.protpetverso_1.schedule;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.protpetverso_1.R;

import java.time.LocalDate;
import java.util.List;

public class DiaAdapter extends RecyclerView.Adapter<DiaAdapter.DiaViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(LocalDate data);
    }

    private List<LocalDate> listaDias;
    private LocalDate diaSelecionado;
    private final OnDayClickListener listener;
    private final Context context;

    public DiaAdapter(Context context, List<LocalDate> listaDias,
                      LocalDate diaSelecionado, OnDayClickListener listener) {
        this.context = context;
        this.listaDias = listaDias;
        this.diaSelecionado = diaSelecionado;
        this.listener = listener;
    }

    public void setListaDias(List<LocalDate> novaLista) {
        this.listaDias = novaLista;
        notifyDataSetChanged();
    }

    public void setDiaSelecionado(LocalDate novaData) {
        this.diaSelecionado = novaData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_data_agenda, parent, false);
        return new DiaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaViewHolder holder, int position) {
        LocalDate data = listaDias.get(position);
        String[] nomes = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"};
        int index = data.getDayOfWeek().getValue() - 1; // 1=Seg ... 7=Dom
        holder.txtNomeDia.setText(nomes[index]);
        holder.txtNumeroDia.setText(String.valueOf(data.getDayOfMonth()));

        boolean sel = data.equals(diaSelecionado);
        if (sel) {
            holder.itemView.setBackgroundResource(R.drawable.bg_dia_selecionado);
            holder.txtNomeDia.setTextColor(Color.WHITE);
            holder.txtNumeroDia.setTextColor(Color.WHITE);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_dia_normal);
            holder.txtNomeDia.setTextColor(Color.parseColor("#5D4037"));
            holder.txtNumeroDia.setTextColor(Color.parseColor("#5D4037"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDayClick(data);
        });
    }

    @Override
    public int getItemCount() {
        return listaDias != null ? listaDias.size() : 0;
    }

    static class DiaViewHolder extends RecyclerView.ViewHolder {
        TextView txtNomeDia, txtNumeroDia;

        DiaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNomeDia = itemView.findViewById(R.id.txtNomeDia);
            txtNumeroDia = itemView.findViewById(R.id.txtNumeroDia);
        }
    }
}