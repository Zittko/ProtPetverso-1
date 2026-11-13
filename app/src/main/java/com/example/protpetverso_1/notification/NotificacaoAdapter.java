package com.example.protpetverso_1.notification;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.protpetverso_1.R;

import java.util.ArrayList;
import java.util.List;

/** Lista do sino: ícone conforme o texto (igual às tarefas), título, descrição e horário. */
public class NotificacaoAdapter extends RecyclerView.Adapter<NotificacaoAdapter.VH> {

    public interface Listener {
        void onClick(NotificacaoLocal n);
    }

    private final List<NotificacaoLocal> lista = new ArrayList<>();
    private final Listener listener;

    public NotificacaoAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setLista(List<NotificacaoLocal> nova) {
        lista.clear();
        if (nova != null) lista.addAll(nova);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notificacao, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NotificacaoLocal n = lista.get(position);
        h.titulo.setText(n.getTitulo());
        h.desc.setText(n.getDescricao());
        h.icone.setImageResource(NotificacaoIconHelper.resolverIcone(n.getTitulo(), n.getDescricao()));
        h.hora.setText(DateUtils.getRelativeTimeSpanString(
                n.getCriadaEm(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE));
        h.ponto.setVisibility(n.isLida() ? View.GONE : View.VISIBLE);
        h.titulo.setTypeface(null, n.isLida() ? Typeface.NORMAL : Typeface.BOLD);
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(n);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView icone;
        final TextView titulo, desc, hora;
        final View ponto;

        VH(@NonNull View v) {
            super(v);
            icone = v.findViewById(R.id.imgNotificacao);
            titulo = v.findViewById(R.id.txtTituloNotificacao);
            desc = v.findViewById(R.id.txtDescNotificacao);
            hora = v.findViewById(R.id.txtHoraNotificacao);
            ponto = v.findViewById(R.id.pontoNaoLida);
        }
    }
}
