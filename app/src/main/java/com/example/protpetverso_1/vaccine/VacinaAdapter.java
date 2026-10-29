package com.example.protpetverso_1.vaccine;

import android.graphics.Color;
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

public class VacinaAdapter extends RecyclerView.Adapter<VacinaAdapter.VH> {

    public interface Listener {
        void onVacinaClick(Vacina vacina);
    }

    private final List<Vacina> lista = new ArrayList<>();
    private Listener listener;

    public VacinaAdapter() {}

    public VacinaAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setItens(List<Vacina> itens) {
        lista.clear();
        if (itens != null) lista.addAll(itens);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vacina_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Vacina vac = lista.get(position);
        h.txtNome.setText(vac.getNome());
        h.txtAplicada.setText("Aplicada em: " + vac.getDataAplicacaoFormatada());

        String prox = vac.getDataProximaDoseFormatada();
        if (prox != null) {
            h.txtProxima.setVisibility(View.VISIBLE);
            h.txtProxima.setText("Próxima dose: " + prox);
        } else if (h.txtProxima != null) {
            h.txtProxima.setVisibility(View.GONE);
        }

        h.txtStatus.setText("Status: " + vac.getStatusLabel());
        String efetivo = vac.getStatusEfetivo();
        if (Vacina.STATUS_APLICADA.equals(efetivo)) {
            h.txtStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else if (Vacina.STATUS_ATRASADA.equals(efetivo)) {
            h.txtStatus.setTextColor(Color.parseColor("#C62828"));
        } else {
            h.txtStatus.setTextColor(Color.parseColor("#E65100"));
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onVacinaClick(vac);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView txtNome, txtAplicada, txtProxima, txtStatus;

        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgVacina);
            txtNome = v.findViewById(R.id.txtNomeVacina);
            txtAplicada = v.findViewById(R.id.txtAplicadaEm);
            txtProxima = v.findViewById(R.id.txtProximaDose);
            txtStatus = v.findViewById(R.id.txtStatusVacina);
        }
    }
}