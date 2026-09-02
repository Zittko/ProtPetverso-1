package com.example.protpetverso_1;
// Define o pacote do projeto.

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
// Imports básicos de View e botões.

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
// Imports do Fragment, RecyclerView e ViewPager2.

import java.util.ArrayList;
import java.util.List;
// Usados para criar as listas de Pets, Tarefas e Alertas.

// Classe de Fragmento Home (View para o menu)
public class HomeFragment extends Fragment {
// Fragment principal da aba "Início". Controla o carrossel de pets e as listas de tarefas/alertas.

    private ViewPager2 viewPagerPets;
    // Componente que mostra um pet por vez (carrossel).

    private ImageButton btnAnterior, btnProximo;
    // Botões de seta para navegar entre os pets.

    private RecyclerView recyclerViewTarefas, recyclerViewTarefasAlertasSaude;
    // Lista de tarefas (Agenda do Dia) e lista de alertas de saúde.

    private void atualizarAgenda(List<Tarefa> tarefasDoPet) {
        // Atualiza a RecyclerView de tarefas com a lista do pet atual.
        TarefaAdapter adapter = new TarefaAdapter(requireContext(), tarefasDoPet);
        recyclerViewTarefas.setAdapter(adapter);
        // Conectado com: TarefaAdapter + classe Tarefa + recyclerViewTarefas
    }

    private void atualizarSaude(List<AlertaSaude> saudeDoPet) {
        // Atualiza a RecyclerView de alertas de saúde com a lista do pet atual.
        AlertaSaudeAdapter adapter = new AlertaSaudeAdapter(requireContext(), saudeDoPet);
        recyclerViewTarefasAlertasSaude.setAdapter(adapter);
        // Conectado com: AlertaSaudeAdapter + classe AlertaSaude + recyclerViewTarefasAlertasSaude
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Cria a interface visual do Fragment.
        return inflater.inflate(R.layout.fragment_home, container, false);
        // Conectado com: fragment_home.xml
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Chamado depois que o layout foi criado. Aqui se faz a lógica.

        // Liga os componentes do XML com as variáveis Java
        viewPagerPets = view.findViewById(R.id.viewPagerPets);
        btnAnterior = view.findViewById(R.id.btnAnterior);
        btnProximo = view.findViewById(R.id.btnProximo);
        recyclerViewTarefas = view.findViewById(R.id.recyclerViewTarefas);
        recyclerViewTarefasAlertasSaude = view.findViewById(R.id.recyclerViewAlertasSaude);

        // Cria as tarefas do pet Thor
        List<Tarefa> tarefasThor = new ArrayList<>();
        tarefasThor.add(new Tarefa(
                "PlanetaPet Pets - Banho & Tosa 10/05",
                "O seu pet está com serviços agendados no dia 10/05, no PetShop RiDog.",
                R.drawable.petversologo
        ));
        tarefasThor.add(new Tarefa(
                "Verifique se há água para beber",
                "Não esqueça de hidratar o seu pet, é muito importante para a sáude dele!",
                R.drawable.racao
        ));
        // Conectado com: classe Tarefa

        // Cria os alertas de saúde do pet Thor
        List<AlertaSaude> saudesThor = new ArrayList<>();
        saudesThor.add(new AlertaSaude(
                "PlanetaPet Pets - Banho & Tosa 10/05",
                "O seu pet está com serviços agendados no dia 10/05, no PetShop RiDog.",
                R.drawable.petversologo
        ));
        saudesThor.add(new AlertaSaude(
                "Verifique se há água para beber",
                "Não esqueça de hidratar o seu pet, é muito importante para a sáude dele!",
                R.drawable.racao
        ));
        // Conectado com: classe AlertaSaude

        // Cria a lista de pets. Cada pet já leva suas próprias tarefas e alertas.
        List<Pet> listaPets = new ArrayList<>();
        listaPets.add(new Pet("Thor", R.drawable.thor, tarefasThor, saudesThor));
        listaPets.add(new Pet("Luna", R.drawable.luna, new ArrayList<>(), new ArrayList<>()));
        listaPets.add(new Pet("Mel", R.drawable.mel, new ArrayList<>(), new ArrayList<>()));
        listaPets.add(new Pet("Bob", R.drawable.bob, new ArrayList<>(), new ArrayList<>()));
        // Conectado com: classe Pet

        // Configura o carrossel de pets
        PetAdapter adapterPet = new PetAdapter(requireContext(), listaPets);
        viewPagerPets.setAdapter(adapterPet);
        // Conectado com: PetAdapter + ViewPager2

        // Define como as listas serão organizadas (vertical)
        recyclerViewTarefas.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewTarefasAlertasSaude.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Mostra os dados do primeiro pet (Thor) ao abrir a tela
        Pet primeiroPet = listaPets.get(0);
        atualizarAgenda(primeiroPet.getTarefas());
        atualizarSaude(primeiroPet.getAlertasSaude());

        // Botão de próximo pet
        btnProximo.setOnClickListener(v -> {
            int atual = viewPagerPets.getCurrentItem();
            int proximo = (atual + 1) % listaPets.size(); // volta para o primeiro no final
            viewPagerPets.setCurrentItem(proximo, true);
        });

        // Botão de pet anterior
        btnAnterior.setOnClickListener(v -> {
            int atual = viewPagerPets.getCurrentItem();
            int anterior = (atual - 1 + listaPets.size()) % listaPets.size(); // vai para o último no início
            viewPagerPets.setCurrentItem(anterior, true);
        });

        // Detecta quando o usuário muda de pet (seta ou deslize)
        viewPagerPets.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // position = índice do pet que está aparecendo agora
                Pet petAtual = listaPets.get(position);
                // Atualiza as duas RecyclerViews com os dados desse pet
                atualizarAgenda(petAtual.getTarefas());
                atualizarSaude(petAtual.getAlertasSaude());
            }
        });
        // Conectado com: ViewPager2 + métodos atualizarAgenda e atualizarSaude
    }
}