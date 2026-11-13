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

import com.example.protpetverso_1.data.PhotoDiskCache;
import com.example.protpetverso_1.pet.PetResumo;

import java.util.ArrayList;
import java.util.List;

public class HomePetPagerAdapter extends RecyclerView.Adapter<HomePetPagerAdapter.VH> {

    private static final String TAG = "HOME_PET_PAGER";
    private final List<PetResumo> lista = new ArrayList<>();

    public HomePetPagerAdapter() {}

    public HomePetPagerAdapter(List<PetResumo> pets) {
        if (pets != null) lista.addAll(pets);
    }

    public void setItens(List<PetResumo> pets) {
        lista.clear();
        if (pets != null) lista.addAll(pets);
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
        Bitmap bmp = null;
        if (pet != null && PhotoDiskCache.existe(pet.getFotoPath())) {
            bmp = PhotoDiskCache.carregarBitmap(pet.getFotoPath());
        }
        if (bmp == null && pet != null) {
            bmp = decodificarBase64(pet.getFotoPetBase64());
        }
        if (bmp != null) h.imgPet.setImageBitmap(bmp);
        else h.imgPet.setImageResource(R.drawable.petversologo);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    private Bitmap decodificarBase64(String base64) {
        try {
            if (base64 == null || base64.trim().isEmpty()) return null;
            String b = base64.trim();
            if (b.contains(",")) b = b.substring(b.indexOf(",") + 1);
            b = b.replace("\n", "").replace("\r", "").replace(" ", "");
            byte[] decoded = Base64.decode(b, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
        } catch (Exception e) {
            Log.e(TAG, "base64", e);
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