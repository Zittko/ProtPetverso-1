package com.example.protpetverso_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.protpetverso_1.account.LoginActivity;
import com.example.protpetverso_1.pet.EscolhaPetActivity;
import com.example.protpetverso_1.pet.PerfilPetFragment;
import com.example.protpetverso_1.pet.PetsFragment;
import com.example.protpetverso_1.pet.SolicitacoesVinculoActivity;
import com.example.protpetverso_1.schedule.AgendaFragment;
import com.example.protpetverso_1.user.PerfilUsuarioFragment;
import com.example.protpetverso_1.vaccine.VacinaFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

/**
 * Tela principal após o login.
 * Toolbar + Drawer + BottomNavigation + troca de Fragments.
 */
public class MenuActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.menu_layout);

        toolbar = findViewById(R.id.toolbarMenu);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        if (toolbar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
                return insets;
            });
        }
        if (bottomNav != null) {
            ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        if (drawerLayout != null && toolbar != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar,
                    R.string.app_name, R.string.app_name
            );
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            toolbar.setNavigationOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        // Itens do menu lateral
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                String t = item.getTitle() != null ? item.getTitle().toString().trim() : "";

                // Preferência: ID do item (mais estável que o texto)
                if (id == R.id.drawerPerfilUsuario
                        || t.equalsIgnoreCase("Perfil do Usuário")) {
                    abrirFragment(new PerfilUsuarioFragment(), "Perfil do Usuário");

                } else if (id == R.id.drawerPerfilPet
                        || t.equalsIgnoreCase("Perfil do Pet")) {
                    abrirPerfilPetSelecionado();

                } else if (id == R.id.drawerSolicitacoes
                        || t.toLowerCase().contains("solicit")) {
                    // Conta A: lista e aceita/recusa vínculos
                    startActivity(new Intent(MenuActivity.this, SolicitacoesVinculoActivity.class));

                } else if (id == R.id.drawerEscolhaPet
                        || t.toLowerCase().contains("vincular")
                        || t.toLowerCase().contains("cadastrar")) {
                    startActivity(new Intent(MenuActivity.this, EscolhaPetActivity.class));

                } else if (id == R.id.drawerLogout
                        || t.equalsIgnoreCase("Sair")) {
                    fazerLogout();
                }

                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            });
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new HomeFragment())
                    .commit();
            if (toolbar != null) {
                toolbar.setTitle("Tela Inicial");
            }
        }

        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                String titulo = "Tela Inicial";
                int itemId = item.getItemId();

                if (itemId == R.id.menuHome) {
                    selectedFragment = new HomeFragment();
                    titulo = "Tela Inicial";
                } else if (itemId == R.id.menuAgenda) {
                    selectedFragment = new AgendaFragment();
                    titulo = "Agenda";
                } else if (itemId == R.id.menuForyou) {
                    selectedFragment = new VacinaFragment();
                    titulo = "Vacinas";
                } else if (itemId == R.id.menuTrending) {
                    selectedFragment = new PetsFragment();
                    titulo = "Pets";
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, selectedFragment)
                            .commit();
                    if (toolbar != null) {
                        toolbar.setTitle(titulo);
                    }
                    return true;
                }
                return false;
            });
        }
    }

    private void abrirPerfilPetSelecionado() {
        SessionManager sm = new SessionManager(this);
        long petId = sm.obterPetId();

        PerfilPetFragment fragment = new PerfilPetFragment();
        Bundle args = new Bundle();
        args.putLong("PET_ID", petId);
        fragment.setArguments(args);

        abrirFragment(fragment, "Perfil do Pet");
    }

    private void fazerLogout() {
        SessionManager sm = new SessionManager(this);
        sm.limparSessao();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void abrirFragment(Fragment fragment, String tituloToolbar) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();

        if (toolbar != null) {
            toolbar.setTitle(tituloToolbar);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}