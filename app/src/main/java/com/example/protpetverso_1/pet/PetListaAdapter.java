package com.example.protpetverso_1.pet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
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

public class PetListaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TIPO_PET = 0;
    private static final int TIPO_ADD = 1;

    public interface Listener {
        void onPetClick(PetResumo pet);
        void onAdicionarClick();
    }

    private final List<PetResumo> lista = new ArrayList<>();
    private final Listener listener;

    public PetListaAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItens(List<PetResumo> pets) {
        lista.clear();
        if (pets != null) lista.addAll(pets);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position < lista.size() ? TIPO_PET : TIPO_ADD;
    }

    @Override
    public int getItemCount() {
        return lista.size() + 1; // + card Adicionar
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TIPO_ADD) {
            return new AddVH(inf.inflate(R.layout.item_pet_adicionar, parent, false));
        }
        return new PetVH(inf.inflate(R.layout.item_pet_lista, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AddVH) {
            holder.itemView.setOnClickListener(v -> listener.onAdicionarClick());
            return;
        }

        PetResumo pet = lista.get(position);
        PetVH h = (PetVH) holder;

        h.txtNome.setText(pet.getNome());
        h.txtPapel.setText(pet.getPapelLabel());

        // ===== FOTO DA API (Base64) =====
        if (pet.hasFoto()) {
            try {
                byte[] decoded = Base64.decode(pet.getFotoPetBase64(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                if (bitmap != null) {
                    h.imgFoto.setImageBitmap(bitmap);
                } else {
                    h.imgFoto.setImageResource(R.drawable.thor); // fallback
                }
            } catch (Exception e) {
                h.imgFoto.setImageResource(R.drawable.thor); // fallback em caso de erro
            }
        } else {
            h.imgFoto.setImageResource(R.drawable.thor); // sem foto → imagem padrão
        }

        h.itemView.setOnClickListener(v -> listener.onPetClick(pet));
    }

    static class PetVH extends RecyclerView.ViewHolder {
        ImageView imgFoto;
        TextView txtNome, txtPapel;

        PetVH(@NonNull View v) {
            super(v);
            imgFoto = v.findViewById(R.id.imgPetLista);
            txtNome = v.findViewById(R.id.txtNomePetLista);
            txtPapel = v.findViewById(R.id.txtPapelPetLista);
        }
    }

    static class AddVH extends RecyclerView.ViewHolder {
        AddVH(@NonNull View v) {
            super(v);
        }
    }
}