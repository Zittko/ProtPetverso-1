package com.example.protpetverso_1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPagerPets;
    private ImageButton btnAnterior, btnProximo;
    private RecyclerView recyclerViewTarefas;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPagerPets = view.findViewById(R.id.viewPagerPets);
        btnAnterior = view.findViewById(R.id.btnAnterior);
        btnProximo = view.findViewById(R.id.btnProximo);
        recyclerViewTarefas = view.findViewById(R.id.recyclerViewTarefas);

        List<Pet> listaPets = new ArrayList<>();
        listaPets.add(new Pet("Thor", R.drawable.thor));
        listaPets.add(new Pet("Luna", R.drawable.luna));
        listaPets.add(new Pet("Mel", R.drawable.mel));
        listaPets.add(new Pet("Bob", R.drawable.bob));

        List<Tarefa> listaTarefas = new ArrayList<>();
        listaTarefas.add(new Tarefa(
                "PlanetaPet Pets - Banho & Tosa 10/05",
                "O seu pet está com serviços agendados no dia 10/05, no PetShop RiDog.",
                R.drawable.petversologo
        ));
        listaTarefas.add(new Tarefa(
                "Verifique se há água para beber",
                "Não esqueça de hidratar o seu pet, é muito importante para a sáude dele!",
                R.drawable.racao
        ));

        PetAdapter adapterPet = new PetAdapter(requireContext(), listaPets);
        viewPagerPets.setAdapter(adapterPet);

        TarefaAdapter adapterTarefa = new TarefaAdapter(requireContext(), listaTarefas);
        recyclerViewTarefas.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewTarefas.setAdapter(adapterTarefa);

        btnProximo.setOnClickListener(v -> {
            int atual = viewPagerPets.getCurrentItem();
            int proximo = (atual + 1) % listaPets.size();
            viewPagerPets.setCurrentItem(proximo, true);
        });

        btnAnterior.setOnClickListener(v -> {
            int atual = viewPagerPets.getCurrentItem();
            int anterior = (atual - 1 + listaPets.size()) % listaPets.size();
            viewPagerPets.setCurrentItem(anterior, true);
        });
    }
}