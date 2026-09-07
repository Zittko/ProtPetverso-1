package com.example.protpetverso_1;
// Define o pacote do projeto. Todos os arquivos deste app pertencem a este pacote.

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Classe de Fragmento Agenda (View para o menu)
public class AgendaFragment extends Fragment {
// Declara o Fragment da aba "Agenda". É uma das telas controladas pela BottomNavigationView.

    private  RecyclerView recyclerDias, recyclerTarefas;
    private  TextView txtTituloLista, txtSemTarefas, txtMesAno;

    private ImageView btnExpandirCalendario;

    private FloatingActionButton btnNovaTarefa;

    private DiaAdapter diaAdapter;
    private TarefaAdapter tarefaAdapter;

    private List<LocalDate> listaDias =new ArrayList<>();
    private List<Tarefa> listaTarefasExibidas = new ArrayList<>();

    private  LocalDate diaSelecionado;

    public AgendaFragment() {
        // Construtor vazio obrigatório para Fragments.
        // O Android precisa dele para recriar o Fragment automaticamente.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceStats){
        return inflater.inflate(R.layout.fragment_agenda, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        //Liga os componentes
        recyclerDias = view.findViewById(R.id.recyclerDias);
        recyclerTarefas = view.findViewById(R.id.recyclerTarefas);
        txtTituloLista = view.findViewById(R.id.txtTituloLista);
        txtSemTarefas = view.findViewById(R.id.txtSemTarefas);
        //txtMesAno = view.findViewById(R.id.txtMesAno);
        btnExpandirCalendario = view.findViewById(R.id.btnExpandirCalendario);
        btnNovaTarefa = view.findViewById(R.id.btnNovaTarefa);

        recyclerTarefas.setLayoutManager(new LinearLayoutManager(requireContext()));
        tarefaAdapter = new TarefaAdapter(requireContext(), listaTarefasExibidas);
        recyclerTarefas.setAdapter(tarefaAdapter);

        configurarBarraDeDias();

        filtrarTarefasPorDia();

        btnNovaTarefa.setOnClickListener(v ->{
            Toast.makeText(requireContext(), "Criar nova tarefa", Toast.LENGTH_SHORT).show();
        });

        btnExpandirCalendario.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Abrir calendário completo", Toast.LENGTH_SHORT).show();
        });
    }

    private void configurarBarraDeDias(){
        listaDias.clear();
        LocalDate hoje = LocalDate.now();

        //Gera os próximos 5 dias (começando de hoje)
        for (int i = 0; i < 5; i++){
            listaDias.add(hoje.plusDays(i));
        }

        diaSelecionado = hoje;

        atualizarTextoMesAno();

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerDias.setLayoutManager(layoutManager);

        diaAdapter = new DiaAdapter(requireContext(), listaDias, diaSelecionado, dataClicada -> {
            // Quando o usuário clica em um dia
            diaSelecionado = dataClicada;
            diaAdapter.setDiaSelecionado(diaSelecionado);
            filtrarTarefasPorDia();
        } );

        recyclerDias.setAdapter(diaAdapter);

    }

    //Filtra as tarefas do pet selecionado de acordo com o dia escolhido
    private void filtrarTarefasPorDia(){
        Pet pet = PetSelecionado.getPet();

        listaTarefasExibidas.clear();

        if(pet == null){
            txtTituloLista.setText("Nenhum pet selecionado");
            tarefaAdapter.notifyDataSetChanged();
            atualizarVisibilidadeLista();
            return;
        }

        List<Tarefa> tarefasDoPet = pet.getTarefas();

        if(tarefasDoPet != null ){
            for(Tarefa tarefa : tarefasDoPet){
                // Só mostra a tarefa se ela tiver data e for do dia selecionado
                if (tarefa.getData() != null && tarefa.getData().equals(diaSelecionado)) {
                    listaTarefasExibidas.add(tarefa);
                }
            }
        }

        String nomeDia = diaSelecionado.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        txtTituloLista.setText("Tarefas de " + pet.getNome() + " - " + nomeDia);

        tarefaAdapter.notifyDataSetChanged();
        atualizarVisibilidadeLista();
    }

    //Mostra a lista ou a mensagem de nenhuma tarefa

    private void atualizarVisibilidadeLista(){
        if(listaTarefasExibidas.isEmpty()){
            recyclerTarefas.setVisibility(View.GONE);
            txtSemTarefas.setVisibility(View.VISIBLE);
        }else{
            recyclerTarefas.setVisibility(View.VISIBLE);
            txtSemTarefas.setVisibility(View.GONE);
        }
    }

    private  void atualizarTextoMesAno(){
        if(txtMesAno != null && diaSelecionado != null){
            String mes = diaSelecionado.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
            int ano = diaSelecionado.getYear();
            mes = mes.substring(0, 1).toUpperCase() + mes.substring(1);
            txtMesAno.setText(mes + " " + ano);
        }
    }

    //Sempre que a Agenda voltar a ficar visível, atualiza as tarefas (Caso tenha trocado de pet na home)
    @Override
    public void onResume(){
        super.onResume();
        filtrarTarefasPorDia();
    }
}