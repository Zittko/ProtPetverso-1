package com.example.protpetverso_1;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.List;
public class DiaAdapter extends RecyclerView.Adapter<DiaAdapter.DiaViewHolder>{

    private List<LocalDate> listaDias;
    private LocalDate diaSelecionado;

    private OnDayClickListener listener;

    private Context context;

    public interface OnDayClickListener {
        void onDayClick(LocalDate data);
    }

    public DiaAdapter(Context context, List<LocalDate> listaDias, LocalDate diaSelecionado, OnDayClickListener listener) {
        this.context = context;
        this.listaDias = listaDias;
        this.diaSelecionado = diaSelecionado;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data_agenda, parent, false);
        return new DiaViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onBindViewHolder(@NonNull DiaViewHolder holder, int position) {
        LocalDate data = listaDias.get(position);

        //Nome do dia
        String[] nomes = {"Seg", "Ter", "Qua", "Quinta", "Sex", "Sáb", "Dom"};
        int indexDia = data.getDayOfWeek().getValue() - 1;
        holder.txtNomeDia.setText(nomes[indexDia]);

        holder.txtNumeroDia.setText(String.valueOf(data.getDayOfMonth()));

        boolean selecionado = data.equals(diaSelecionado);

        if (selecionado) {
            //Dia selecionado
            holder.txtNumeroDia.setBackgroundResource(R.drawable.bg_btn_entrar);
            holder.txtNumeroDia.setTextColor(Color.WHITE);
            holder.txtNomeDia.setTextColor(Color.parseColor("#3F51B5"));
        } else {
            holder.txtNumeroDia.setBackgroundResource(android.R.color.transparent);
            holder.txtNumeroDia.setTextColor(Color.parseColor("#FFFFFF"));
            holder.txtNomeDia.setTextColor(Color.parseColor("#FFFFFF"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDayClick(data);
            }
        });

    }

    @Override
    public int getItemCount() {
        return listaDias.size();
    }

    public void setDiaSelecionado(LocalDate novaData) {
        this.diaSelecionado = novaData;
        notifyDataSetChanged();
    }

    static class DiaViewHolder extends RecyclerView.ViewHolder {
        TextView txtNomeDia, txtNumeroDia;

        public DiaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNomeDia = itemView.findViewById(R.id.txtNomeDia);
            txtNumeroDia = itemView.findViewById(R.id.txtNumeroDia);
        }
    }
}
