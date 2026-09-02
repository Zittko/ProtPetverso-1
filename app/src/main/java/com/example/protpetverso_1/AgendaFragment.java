package com.example.protpetverso_1;
// Define o pacote do projeto. Todos os arquivos deste app pertencem a este pacote.

import android.os.Bundle;
// Usado para salvar/restaurar o estado do Fragment (quando a tela gira, por exemplo).

import androidx.fragment.app.Fragment;
// Classe base. AgendaFragment herda de Fragment para ser uma tela dentro da MenuActivity.

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Usados para criar e retornar o layout visual do Fragment.

// Classe de Fragmento Agenda (View para o menu)
public class AgendaFragment extends Fragment {
// Declara o Fragment da aba "Agenda". É uma das telas controladas pela BottomNavigationView.

    private atualizarAgenda atualizarAgenda;

    public AgendaFragment() {
        // Construtor vazio obrigatório para Fragments.
        // O Android precisa dele para recriar o Fragment automaticamente.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Chamado quando o Fragment é criado.
        // Aqui normalmente se inicializam dados (listas, ViewModels, etc.).
        // No momento está vazio.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Méthodo principal que cria a interface visual do Fragment.

        return inflater.inflate(R.layout.fragment_agenda, container, false);
        // Infla (carrega) o arquivo XML fragment_agenda.xml e o transforma em View.
        // Este layout é o que aparece quando o usuário clica na aba "Agenda" da BottomNavigationView.
        // Está conectado com:
        // - MenuActivity (que faz o replace deste Fragment no fragmentContainer)
        // - bottom_nav_menu.xml (item menuAgenda)
        // - fragment_agenda.xml (o layout visual desta tela)
    }
}