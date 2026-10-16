package com.example.protpetverso_1;

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

import com.example.protpetverso_1.pet.PetResumo;

import java.util.List;

public class HomePetPagerAdapter extends RecyclerView.Adapter<HomePetPagerAdapter.VH> {

    private final List<PetResumo> lista;

    public HomePetPagerAdapter(List<PetResumo> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pet_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PetResumo pet = lista.get(position);
        h.txtNome.setText(pet.getNome());
        if (pet.hasFoto()) {
            Bitmap bmp = decode(pet.getFotoPetBase64());
            if (bmp != null) h.img.setImageBitmap(bmp);
            else h.img.setImageResource(R.drawable.ic_tarefa_default);
        } else {
            h.img.setImageResource(R.drawable.ic_tarefa_default);
        }
        h.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    private static Bitmap decode(String b64) {
        try {
            if (b64 == null || b64.isEmpty()) return null;
            if (b64.contains(",")) b64 = b64.substring(b64.indexOf(",") + 1);
            byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView txtNome;
        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgPet);
            txtNome = v.findViewById(R.id.txtPetNome);
        }
    }
}