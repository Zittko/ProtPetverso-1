package com.example.protpetverso_1.pet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.protpetverso_1.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class SolicitacaoVinculoAdapter
        extends RecyclerView.Adapter<SolicitacaoVinculoAdapter.VH> {

    public interface Listener {
        void onAceitar(SolicitacaoVinculo item);
        void onRecusar(SolicitacaoVinculo item);
    }

    private final List<SolicitacaoVinculo> lista = new ArrayList<>();
    private final Listener listener;

    public SolicitacaoVinculoAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItens(List<SolicitacaoVinculo> novos) {
        lista.clear();
        if (novos != null) lista.addAll(novos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_solicitacao_vinculo, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SolicitacaoVinculo item = lista.get(position);
        h.txtNomePet.setText("Pet: " + item.nomePet);
        h.txtSolicitante.setText("Quem pediu: " + item.nomeSolicitante);
        h.txtData.setText(item.dataDeCriacao != null ? item.dataDeCriacao : "");

        h.btnAceitar.setOnClickListener(v -> listener.onAceitar(item));
        h.btnRecusar.setOnClickListener(v -> listener.onRecusar(item));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtNomePet, txtSolicitante, txtData;
        MaterialButton btnAceitar, btnRecusar;

        VH(@NonNull View itemView) {
            super(itemView);
            txtNomePet = itemView.findViewById(R.id.txtNomePet);
            txtSolicitante = itemView.findViewById(R.id.txtSolicitante);
            txtData = itemView.findViewById(R.id.txtData);
            btnAceitar = itemView.findViewById(R.id.btnAceitar);
            btnRecusar = itemView.findViewById(R.id.btnRecusar);
        }
    }
}