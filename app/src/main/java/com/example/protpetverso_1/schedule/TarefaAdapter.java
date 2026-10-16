package com.example.protpetverso_1.schedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.protpetverso_1.R;

import java.util.ArrayList;
import java.util.List;

public class TarefaAdapter extends RecyclerView.Adapter<TarefaAdapter.VH> {

    public interface OnTarefaClickListener {
        void onTarefaClick(Tarefa tarefa, int position);
    }

    private final Context context;
    private final List<Tarefa> lista = new ArrayList<>();
    private OnTarefaClickListener listener;

    public TarefaAdapter(Context context, List<Tarefa> inicial) {
        this.context = context;
        if (inicial != null) lista.addAll(inicial);
    }

    public void setOnTarefaClickListener(OnTarefaClickListener listener) {
        this.listener = listener;
    }

    public void setItens(List<Tarefa> tarefas) {
        lista.clear();
        if (tarefas != null) lista.addAll(tarefas);
        notifyDataSetChanged();
    }

    public void atualizarItem(int position, Tarefa tarefa) {
        if (position >= 0 && position < lista.size() && tarefa != null) {
            lista.set(position, tarefa);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarefa_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Tarefa t = lista.get(position);

        if (h.titleTarefa != null) h.titleTarefa.setText(t.getTituloComHorario());
        if (h.descTarefa != null) h.descTarefa.setText(t.getDesc());

        if (h.txtRodape != null) {
            String rodape = t.getRodapeTexto();
            if (rodape == null || rodape.isEmpty()) {
                h.txtRodape.setVisibility(View.GONE);
            } else {
                h.txtRodape.setVisibility(View.VISIBLE);
                h.txtRodape.setText(rodape);
            }
        }

        if (h.imgTarefa != null) {
            int icon = t.getImagemResId() != 0
                    ? t.getImagemResId()
                    : TarefaIconHelper.resolverIcone(t.getTitulo(), t.getDesc());
            h.imgTarefa.setImageResource(icon);
        }

        if (t.isConcluida()) {
            if (h.viewStatusBg != null) {
                h.viewStatusBg.setBackgroundResource(R.drawable.bg_status_concluida);
            }
            if (h.imgStatusCheck != null) {
                h.imgStatusCheck.setImageResource(R.drawable.ic_check_branco);
                h.imgStatusCheck.setVisibility(View.VISIBLE);
            }
            if (h.btnStatus != null) {
                h.btnStatus.setClickable(false);
                h.btnStatus.setOnClickListener(null);
            }
        } else {
            if (h.viewStatusBg != null) {
                h.viewStatusBg.setBackgroundResource(R.drawable.bg_status_pendente);
            }
            if (h.imgStatusCheck != null) {
                h.imgStatusCheck.setVisibility(View.GONE);
            }
            if (h.btnStatus != null) {
                h.btnStatus.setClickable(true);
                h.btnStatus.setOnClickListener(v -> {
                    int pos = h.getBindingAdapterPosition();
                    if (listener != null && pos != RecyclerView.NO_POSITION) {
                        listener.onTarefaClick(lista.get(pos), pos);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        FrameLayout btnStatus;
        View viewStatusBg;
        ImageView imgStatusCheck, imgTarefa;
        TextView titleTarefa, descTarefa, txtRodape;

        VH(@NonNull View itemView) {
            super(itemView);
            btnStatus = itemView.findViewById(R.id.btnStatus);
            viewStatusBg = itemView.findViewById(R.id.viewStatusBg);
            imgStatusCheck = itemView.findViewById(R.id.imgStatusCheck);
            imgTarefa = itemView.findViewById(R.id.imgTarefa);
            titleTarefa = itemView.findViewById(R.id.titleTarefa);
            descTarefa = itemView.findViewById(R.id.descTarefa);
            txtRodape = itemView.findViewById(R.id.txtRodape);
        }
    }
}