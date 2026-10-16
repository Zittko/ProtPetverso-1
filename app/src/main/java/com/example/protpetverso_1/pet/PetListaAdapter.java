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
        return lista.size() + 1;
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
        if (getItemViewType(position) == TIPO_ADD) {
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAdicionarClick();
            });
            return;
        }

        PetResumo pet = lista.get(position);
        PetVH h = (PetVH) holder;

        if (h.txtNome != null) h.txtNome.setText(pet.getNome());
        if (h.txtPapel != null) h.txtPapel.setText(pet.getPapelLabel());

        if (h.imgFoto != null) {
            h.imgFoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (pet.hasFoto()) {
                Bitmap bmp = decodificarBase64(pet.getFotoPetBase64());
                if (bmp != null) {
                    h.imgFoto.setImageBitmap(bmp);
                } else {
                    h.imgFoto.setImageResource(R.drawable.ic_tarefa_default);
                }
            } else {
                h.imgFoto.setImageResource(R.drawable.ic_tarefa_default);
            }
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPetClick(pet);
        });
    }

    private Bitmap decodificarBase64(String base64) {
        try {
            if (base64 == null || base64.isEmpty()) return null;
            if (base64.contains(",")) {
                base64 = base64.substring(base64.indexOf(",") + 1);
            }
            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
        } catch (Exception e) {
            return null;
        }
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