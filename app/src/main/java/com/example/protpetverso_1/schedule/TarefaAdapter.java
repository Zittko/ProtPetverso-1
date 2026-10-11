package com.example.protpetverso_1.schedule;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.protpetverso_1.R;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TarefaAdapter extends RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder> {

    public interface OnTarefaClickListener {
        void onToggleConcluida(Tarefa tarefa, int position);
    }

    private final List<Tarefa> listaTarefas = new ArrayList<>();
    private final Context context;
    private OnTarefaClickListener listener;

    public TarefaAdapter(Context context, List<Tarefa> lista) {
        this.context = context;
        if (lista != null) listaTarefas.addAll(lista);
    }

    public void setOnTarefaClickListener(OnTarefaClickListener listener) {
        this.listener = listener;
    }

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
    public void onBindViewHolder(@NonNull TarefaViewHolder h, int position) {
        Tarefa t = listaTarefas.get(position);

        if (t.getHorario() != null) {
            LocalTime hr = t.getHorario();
            h.titleTarefa.setText(String.format("%02d:%02d - %s", hr.getHour(), hr.getMinute(), t.getTitulo()));
        } else {
            h.titleTarefa.setText(t.getTitulo());
        }

        h.descTarefa.setText(t.getDesc());
        h.imgTarefa.setImageResource(t.getImagemResId());

        if (t.isConcluida()) {
            h.imgStatus.setImageResource(android.R.drawable.checkbox_on_background);
            h.titleTarefa.setPaintFlags(h.titleTarefa.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            h.imgStatus.setImageResource(android.R.drawable.checkbox_off_background);
            h.titleTarefa.setPaintFlags(h.titleTarefa.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        // Rodapé: Feito por / Pet
        if (t.isConcluida() && t.getExecutadoPorNome() != null && !t.getExecutadoPorNome().isEmpty()) {
            h.txtRodape.setText("Feito por: " + t.getExecutadoPorNome());
        } else if (t.getNomePet() != null && !t.getNomePet().isEmpty()) {
            h.txtRodape.setText("Pet: " + t.getNomePet());
        } else {
            h.txtRodape.setText("");
        }

        h.imgStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onToggleConcluida(t, h.getBindingAdapterPosition());
            } else {
                Toast.makeText(context,
                        "Marcar como concluída em breve (aguardando endpoint da API).",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaTarefas.size();
    }

    static class TarefaViewHolder extends RecyclerView.ViewHolder {
        TextView titleTarefa, descTarefa, txtRodape;
        ImageView imgTarefa, imgStatus;

        TarefaViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTarefa = itemView.findViewById(R.id.titleTarefa);
            descTarefa = itemView.findViewById(R.id.descTarefa);
            txtRodape = itemView.findViewById(R.id.txtRodape);
            imgTarefa = itemView.findViewById(R.id.imgTarefa);
            imgStatus = itemView.findViewById(R.id.imgStatus);
        }
    }
}