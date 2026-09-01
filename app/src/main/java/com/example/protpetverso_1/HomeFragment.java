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
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;
// Classe de Fragmento Home (View para o menu)
public class HomeFragment extends Fragment {
    private ViewPager2 viewPagerPets; // Variável ViewPager2 (Componente da roleta)
    private ImageButton btnAnterior, btnProximo; // Variável para os botões das setas
    private RecyclerView recyclerViewTarefas, recyclerViewTarefasAlertasSaude; // Recycler Views para a Agenda e os Alertas de Saúde

    private void atualizarAgenda(List<Tarefa> tarefasDoPet) {
        TarefaAdapter adapter = new TarefaAdapter(requireContext(), tarefasDoPet);
        recyclerViewTarefas.setAdapter(adapter);
    }
    private void atualizarSaude(List<AlertaSaude> saudeDoPet) {
        AlertaSaudeAdapter adapter = new AlertaSaudeAdapter(requireContext(), saudeDoPet);
        recyclerViewTarefasAlertasSaude.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Definições de busca no XML o que é o que
        viewPagerPets = view.findViewById(R.id.viewPagerPets);
        btnAnterior = view.findViewById(R.id.btnAnterior);
        btnProximo = view.findViewById(R.id.btnProximo);
        recyclerViewTarefas = view.findViewById(R.id.recyclerViewTarefas);
        recyclerViewTarefasAlertasSaude = view.findViewById(R.id.recyclerViewAlertasSaude);

        // Ciração de uma lista de array para a lista de pets
        List<Pet> listaPets = new ArrayList<>();
        listaPets.add(new Pet("Thor", R.drawable.thor));
        listaPets.add(new Pet("Luna", R.drawable.luna));
        listaPets.add(new Pet("Mel", R.drawable.mel));
        listaPets.add(new Pet("Bob", R.drawable.bob));

        // Ciração de uma lista de array para a lista de tarefas
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

        List<AlertaSaude> listaAlertasSaudesPet1 = new ArrayList<>();
        listaAlertasSaudesPet1.add(new AlertaSaude(
                "PlanetaPet Pets - Banho & Tosa 10/05",
                "O seu pet está com serviços agendados no dia 10/05, no PetShop RiDog.",
                R.drawable.petversologo
        ));
        listaAlertasSaudesPet1.add(new AlertaSaude(
                "Verifique se há água para beber",
                "Não esqueça de hidratar o seu pet, é muito importante para a sáude dele!",
                R.drawable.racao
        ));

        // Conexão da ViewPager2 com a array criada de pets já passada pelo adapter
        PetAdapter adapterPet = new PetAdapter(requireContext(), listaPets);
        viewPagerPets.setAdapter(adapterPet);

        viewPagerPets.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                Pet petAtual = listaPets.get(position);

                atualizarAgenda(petAtual.getTarefas());
                atualizarSaude(petAtual.getAlertasSaude());
            }
        });

        // Conexão da RecyclerView com a array criada de tarefas já passada pelo adapter
        TarefaAdapter adapterTarefa = new TarefaAdapter(requireContext(), listaTarefas);
        recyclerViewTarefas.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewTarefas.setAdapter(adapterTarefa);

        // Botões das setas para passar ou voltar de imagem da ViewPager2
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