package com.example.protpetverso_1.pet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.protpetverso_1.R;
import com.example.protpetverso_1.util.Formatadores;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/** Lista de pedidos de alteração (tarefa/vacina) para o tutor principal aprovar ou recusar. */
public class SolicitacaoAlteracaoAdapter
        extends RecyclerView.Adapter<SolicitacaoAlteracaoAdapter.VH> {

    public interface Listener {
        void onAprovar(SolicitacaoAlteracao item);
        void onRecusar(SolicitacaoAlteracao item);
    }

    private final List<SolicitacaoAlteracao> lista = new ArrayList<>();
    private final Listener listener;

    public SolicitacaoAlteracaoAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItens(List<SolicitacaoAlteracao> novos) {
        lista.clear();
        if (novos != null) lista.addAll(novos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_solicitacao_alteracao, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SolicitacaoAlteracao item = lista.get(position);
        h.txtTipo.setText(item.tituloTipo());
        h.txtPet.setText("Pet: " + item.nomePet);
        h.txtSolicitante.setText("Quem pediu: " + item.nomeSolicitante);
        h.txtData.setText(Formatadores.dataHoraIsoParaBr(item.dataDeCriacao));
        h.txtProposto.setText(item.proposto);

        boolean temAtual = item.atual != null && !item.atual.isEmpty();
        h.boxAtual.setVisibility(temAtual ? View.VISIBLE : View.GONE);
        if (temAtual) h.txtAtual.setText(item.atual);

        h.btnAprovar.setOnClickListener(v -> listener.onAprovar(item));
        h.btnRecusar.setOnClickListener(v -> listener.onRecusar(item));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtTipo, txtPet, txtSolicitante, txtData, txtProposto, txtAtual;
        View boxAtual;
        MaterialButton btnAprovar, btnRecusar;

        VH(@NonNull View v) {
            super(v);
            txtTipo = v.findViewById(R.id.txtTipoAlteracao);
            txtPet = v.findViewById(R.id.txtPetAlteracao);
            txtSolicitante = v.findViewById(R.id.txtSolicitanteAlteracao);
            txtData = v.findViewById(R.id.txtDataAlteracao);
            txtProposto = v.findViewById(R.id.txtPropostoAlteracao);
            txtAtual = v.findViewById(R.id.txtAtualAlteracao);
            boxAtual = v.findViewById(R.id.boxAtualAlteracao);
            btnAprovar = v.findViewById(R.id.btnAprovarAlteracao);
            btnRecusar = v.findViewById(R.id.btnRecusarAlteracao);
        }
    }
}
