package com.example.protpetverso_1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private List<Pet> listaPets;

    public PetAdapter(List<Pet> listaPets) {
        this.listaPets = listaPets;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pet_card, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = listaPets.get(position);
        holder.txtPetNome.setText(pet.getNome());
        holder.imgPet.setImageResource(pet.getImagemResId());
    }

    @Override
    public int getItemCount() {
        return listaPets.size();
    }

    static class PetViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPet;
        TextView txtPetNome;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPet = itemView.findViewById(R.id.imgPet);
            txtPetNome = itemView.findViewById(R.id.txtPetNome);
        }
    }
}