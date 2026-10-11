package com.example.protpetverso_1.schedule;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.protpetverso_1.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MesAdapter extends RecyclerView.Adapter<MesAdapter.VH> {

    public interface OnDayClickListener {
        void onDayClick(LocalDate data);
    }

    private final List<LocalDate> celulas = new ArrayList<>();
    private LocalDate diaSelecionado;
    private final LocalDate hoje = LocalDate.now();
    private final OnDayClickListener listener;

    public MesAdapter(OnDayClickListener listener) {
        this.listener = listener;
    }

    public void setMes(LocalDate qualquerDiaDoMes, LocalDate selecionado) {
        this.diaSelecionado = selecionado;
        celulas.clear();

        LocalDate primeiro = qualquerDiaDoMes.withDayOfMonth(1);
        int offset = primeiro.getDayOfWeek().getValue() - 1; // Seg=0
        for (int i = 0; i < offset; i++) celulas.add(null);

        int total = qualquerDiaDoMes.lengthOfMonth();
        for (int d = 1; d <= total; d++) {
            celulas.add(primeiro.withDayOfMonth(d));
        }
        while (celulas.size() % 7 != 0) celulas.add(null);

        notifyDataSetChanged();
    }

    public void setDiaSelecionado(LocalDate data) {
        this.diaSelecionado = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dia_mes, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        LocalDate data = celulas.get(position);

        if (data == null) {
            h.txt.setText("");
            h.txt.setEnabled(false);
            h.txt.setBackgroundResource(android.R.color.transparent);
            h.txt.setOnClickListener(null);
            return;
        }

        h.txt.setText(String.valueOf(data.getDayOfMonth()));

        boolean passado = data.isBefore(hoje);
        boolean sel = data.equals(diaSelecionado);

        if (passado) {
            h.txt.setTextColor(Color.parseColor("#BDBDBD"));
            h.txt.setBackgroundResource(android.R.color.transparent);
            h.txt.setEnabled(false);
            h.txt.setOnClickListener(null);
        } else if (sel) {
            h.txt.setBackgroundResource(R.drawable.bg_dia_selecionado);
            h.txt.setTextColor(Color.WHITE);
            h.txt.setEnabled(true);
            h.txt.setOnClickListener(v -> {
                if (listener != null) listener.onDayClick(data);
            });
        } else {
            h.txt.setBackgroundResource(android.R.color.transparent);
            h.txt.setTextColor(Color.BLACK);
            h.txt.setEnabled(true);
            h.txt.setOnClickListener(v -> {
                if (listener != null) listener.onDayClick(data);
            });
        }
    }

    @Override
    public int getItemCount() {
        return celulas.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txt;
        VH(@NonNull View itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.txtDiaMes);
        }
    }
}