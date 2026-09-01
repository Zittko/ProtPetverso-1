package com.example.protpetverso_1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class AdapterCompele extends RecyclerView.Adapter<AdapterCompele.ViewHolder> {
    private Context context;
    private List<ComponentesEletronicos> LstCompele;

    public AdapterCompele (Context context, List<ComponentesEletronicos> lstCompele) {
        this.context = context;
        LstCompele = lstCompele;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.modelo_compele, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.ModeloImgCompele.setImageResource(LstCompele.get(position).getImagem());
        holder.ModeloNomeCompele.setText(LstCompele.get(position).getNome());
        holder.ModeloDescCompele.setText(LstCompele.get(position).getDesc());
        holder.ModeloPrecoCompele.setText(LstCompele.get(position).getPreco());
    }

    @Override
    public int getItemCount() {
        return LstCompele.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView ModeloCardCompele;
        ImageView ModeloImgCompele;
        TextView ModeloNomeCompele;
        TextView ModeloDescCompele;
        TextView ModeloPrecoCompele;
        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            ModeloCardCompele = itemView.findViewById(R.id.ModeloCardCompele);
            ModeloImgCompele = itemView.findViewById(R.id.ModeloImgCompele);
            ModeloNomeCompele = itemView.findViewById(R.id.ModeloNomeCompele);
            ModeloDescCompele = itemView.findViewById(R.id.ModeloDescCompele);
            ModeloPrecoCompele = itemView.findViewById(R.id.ModeloPrecoCompele);
        }
    }
}
