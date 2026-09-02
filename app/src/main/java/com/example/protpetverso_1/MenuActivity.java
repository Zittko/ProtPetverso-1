package com.example.protpetverso_1;
// Define o pacote do projeto.

import android.os.Bundle;
// Usado para salvar/restaurar o estado da Activity.

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
// Imports para Edge-to-Edge, Activity e Fragments.

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
// Componentes Material: Toolbar e Bottom Navigation.

public class MenuActivity extends AppCompatActivity {
// Activity principal do app após o login.
// Ela contém a Toolbar, a BottomNavigation e o espaço onde os Fragments são trocados.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Ativa o modo edge-to-edge (conteúdo atrás das barras do sistema).

        setContentView(R.layout.menu_layout);
        // Carrega o layout principal.
        // Conectado com: menu_layout.xml

        MaterialToolbar toolbar = findViewById(R.id.toolbarMenu);
        // Toolbar do topo (título + ícone de menu).
        // Conectado com: menu_layout.xml (toolbarMenu)

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavbar);
        // Barra de navegação inferior com as abas.
        // Conectado com: menu_layout.xml (bottomNavbar) + bottom_nav_menu.xml

        // Faz a Toolbar respeitar a área da status bar / câmera
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Faz a BottomNavigation respeitar a área do botão Home
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Abre o HomeFragment como tela inicial
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new HomeFragment())
                .commit();
        // Conectado com:
        // - fragmentContainer (no menu_layout.xml)
        // - HomeFragment

        // Listener de clique dos itens da Bottom Navigation
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            // Decide qual Fragment abrir de acordo com o ícone clicado
            if (itemId == R.id.menuHome) {
                selectedFragment = new HomeFragment();
                // Aba Início
            } else if (itemId == R.id.menuAgenda) {
                selectedFragment = new AgendaFragment();
                // Aba Agenda
            } else if (itemId == R.id.menuForyou) {
                selectedFragment = new VacinaFragment();
                // Aba Vacina
            } else if (itemId == R.id.menuTrending) {
                selectedFragment = new PetsFragment();
                // Aba Lista/Pets
            }

            // Troca o Fragment atual pelo selecionado
            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
                return true; // item selecionado com sucesso
            }
            return false;
        });
        // Conectado com:
        // - bottom_nav_menu.xml (IDs dos itens)
        // - HomeFragment, AgendaFragment, VacinaFragment, PetsFragment
        // - fragmentContainer
    }
}