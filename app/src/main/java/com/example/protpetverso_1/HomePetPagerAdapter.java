package com.example.protpetverso_1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.protpetverso_1.pet.PetResumo;

import java.util.ArrayList;
import java.util.List;

/**
 * Carrossel de pets na Home — apenas a foto (sem nome / faixa escura).
 */
public class HomePetPagerAdapter extends RecyclerView.Adapter<HomePetPagerAdapter.VH> {

    private static final String TAG = "HOME_PET_PAGER";

    private final List<PetResumo> lista = new ArrayList<>();

    public HomePetPagerAdapter() {
    }

    public HomePetPagerAdapter(List<PetResumo> pets) {
        if (pets != null) {
            lista.addAll(pets);
        }
    }

    public void setItens(List<PetResumo> pets) {
        lista.clear();
        if (pets != null) {
            lista.addAll(pets);
        }
        notifyDataSetChanged();
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
        if (h.imgPet == null) return;

        h.imgPet.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (position < 0 || position >= lista.size()) {
            h.imgPet.setImageResource(R.drawable.petversologo);
            return;
        }

        PetResumo pet = lista.get(position);
        String base64 = null;
        if (pet != null) {
            base64 = pet.getFotoPetBase64();
        }

        Log.d(TAG, "pos=" + position
                + " nome=" + (pet != null ? pet.getNome() : "null")
                + " fotoLen=" + (base64 != null ? base64.length() : 0));

        Bitmap bmp = decodificarBase64(base64);
        if (bmp != null) {
            h.imgPet.setImageBitmap(bmp);
        } else {
            // Placeholder se não houver foto ou Base64 inválido
            h.imgPet.setImageResource(R.drawable.petversologo);
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    private Bitmap decodificarBase64(String base64) {
        try {
            if (base64 == null) return null;
            base64 = base64.trim();
            if (base64.isEmpty()) return null;

            if (base64.contains(",")) {
                base64 = base64.substring(base64.indexOf(",") + 1);
            }
            base64 = base64.replace("\n", "").replace("\r", "").replace(" ", "");

            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
            if (decoded == null || decoded.length == 0) return null;

            return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao decodificar foto", e);
            return null;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView imgPet;

        VH(@NonNull View v) {
            super(v);
            imgPet = v.findViewById(R.id.imgPet);
        }
    }
}