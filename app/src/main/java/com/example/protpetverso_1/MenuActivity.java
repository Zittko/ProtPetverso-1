package com.example.protpetverso_1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MenuItem;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.protpetverso_1.account.LoginActivity;
import com.example.protpetverso_1.account.LogoutHelper;
import com.example.protpetverso_1.data.AppDataCache;
import com.example.protpetverso_1.data.DataPreloader;
import com.example.protpetverso_1.notification.NotificacaoHelper;
import com.example.protpetverso_1.notification.NotificacaoPrefs;
import com.example.protpetverso_1.notification.NotificacaoScheduler;
import com.example.protpetverso_1.notification.NotificacaoStore;
import com.example.protpetverso_1.notification.NotificacoesFragment;
import com.example.protpetverso_1.notification.PreferenciasNotificacaoFragment;
import com.example.protpetverso_1.pet.EscolhaPetActivity;
import com.example.protpetverso_1.pet.PerfilPetFragment;
import com.example.protpetverso_1.pet.PetsFragment;
import com.example.protpetverso_1.pet.SolicitacoesVinculoFragment;
import com.example.protpetverso_1.schedule.AgendaFragment;
import com.example.protpetverso_1.user.PerfilUsuarioFragment;
import com.example.protpetverso_1.vaccine.VacinaFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

/**
 * Tela principal após o login.
 * 1) Garante cache disco → memória (offline / abertura rápida)
 * 2) Abre Home (pode pintar com cache)
 * 3) DataPreloader atualiza pela API em segundo plano
 */
public class MenuActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private ActivityResultLauncher<String> permissaoNotificacoes;
    private boolean abrirNotificacoesPendente = false;
    private DrawerArrowDrawable setaMenu; // ícone do drawer (hambúrguer) da toolbar
    // Atualiza a bolinha do sino sempre que a lista de notificações muda
    private final SharedPreferences.OnSharedPreferenceChangeListener ouvinteNotificacoes =
            (sp, chave) -> runOnUiThread(this::atualizarBolinhaSino);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.menu_layout);

        // Cache persistente → memória (antes dos fragments)
        AppDataCache.get().init(this);
        AppDataCache.get().carregarDoDisco();

        // Sem sessão válida → login
        SessionManager session = new SessionManager(this);
        if (!session.isLogado()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Sem nenhum pet → não entra na tela inicial
        if (EscolhaPetActivity.precisaEscolherPet(session)) {
            irParaEscolhaObrigatoria();
            return;
        }

        toolbar = findViewById(R.id.toolbarMenu);

        // Notificações locais: canais, permissão (Android 13+) e agendamento dos lembretes
        permissaoNotificacoes = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), concedida -> { });
        prepararNotificacoes();

        // Sino do menu superior: abre a lista de notificações
        if (toolbar != null) {
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menuNotificacoes) {
                    abrirFragment(new NotificacoesFragment(), "Notificações");
                    return true;
                }
                return false;
            });
        }
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        final int preto = Color.parseColor("#000000");

        if (toolbar != null) {
            toolbar.setTitleTextColor(preto);
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

            setaMenu = toggle.getDrawerArrowDrawable();
            setaMenu.setColor(preto);

            // Telas abertas a partir de outra (perfil do pet, notificações) mostram a seta de voltar
            getSupportFragmentManager().addOnBackStackChangedListener(this::atualizarNavegacaoToolbar);
            atualizarNavegacaoToolbar();
        }

        if (navigationView != null) {
            ColorStateList pretoCsl = ColorStateList.valueOf(preto);
            navigationView.setItemIconTintList(pretoCsl);
            navigationView.setItemTextColor(pretoCsl);

            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                String t = item.getTitle() != null ? item.getTitle().toString().trim() : "";

                if (id == R.id.drawerPerfilUsuario
                        || t.equalsIgnoreCase("Perfil do Usuário")) {
                    abrirFragment(new PerfilUsuarioFragment(), "Perfil do Usuário");

                } else if (id == R.id.drawerSolicitacoes
                        || t.toLowerCase().contains("solicit")) {
                    abrirFragment(new SolicitacoesVinculoFragment(), "Solicitações de Vínculo");

                } else if (id == R.id.drawerPrefNotificacoes) {
                    abrirFragment(new PreferenciasNotificacaoFragment(), "Preferências de notificação");

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

        // Fragment inicial: Home (pode usar cache do disco imediatamente)
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
                    titulo = "Vacinas e Imunização";
                } else if (itemId == R.id.menuTrending) {
                    selectedFragment = new PetsFragment();
                    titulo = "Pets";
                }

                if (selectedFragment != null) {
                    // Trocar de aba descarta as telas empilhadas (perfil do pet, notificações...)
                    getSupportFragmentManager().popBackStack(null,
                            androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
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

        // Veio de toque em uma notificação do aparelho: abre a lista do sino
        tratarIntent(getIntent());

        // Atualiza cache pela API (rede); não bloqueia a UI
        // Se a API confirmar que não há pets (cache/sessão desatualizados), volta para a escolha
        DataPreloader.preloadTudo(this, () -> {
            if (!isFinishing()) irParaEscolhaObrigatoria();
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        tratarIntent(intent);
    }

    /** Marca para abrir a lista no onResume (trocar fragment com a tela parada daria erro de estado). */
    private void tratarIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra(NotificacaoHelper.EXTRA_ABRIR_NOTIFICACOES, false)) {
            intent.removeExtra(NotificacaoHelper.EXTRA_ABRIR_NOTIFICACOES);
            abrirNotificacoesPendente = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificacaoStore.registrarOuvinte(this, ouvinteNotificacoes);
        atualizarBolinhaSino();
        if (abrirNotificacoesPendente) {
            abrirNotificacoesPendente = false;
            abrirFragment(new NotificacoesFragment(), "Notificações");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        NotificacaoStore.removerOuvinte(this, ouvinteNotificacoes);
    }

    /** Sino com bolinha vermelha quando há notificações não lidas. */
    private void atualizarBolinhaSino() {
        if (toolbar == null) return;
        MenuItem sino = toolbar.getMenu().findItem(R.id.menuNotificacoes);
        if (sino == null) return;
        boolean naoLidas = NotificacaoStore.naoLidas(this) > 0;
        sino.setIcon(naoLidas ? R.drawable.ic_sino_nao_lida : R.drawable.ic_sino);
    }

    /** Cria os canais, pede a permissão uma vez (Android 13+) e agenda os lembretes. */
    private void prepararNotificacoes() {
        NotificacaoHelper.criarCanais(this);
        NotificacaoPrefs prefs = new NotificacaoPrefs(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
                && !prefs.permissaoJaPedida()) {
            prefs.permissaoPedida();
            permissaoNotificacoes.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
        NotificacaoScheduler.agendarTodos(this, false);
    }

    private void irParaEscolhaObrigatoria() {
        Intent intent = EscolhaPetActivity.criarIntent(this, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void fazerLogout() {
        // Invalida o token na API, limpa tudo local e volta ao Login
        LogoutHelper.sair(this);
    }

    /** Telas acessadas a partir de outra tela: mostram a seta de voltar no lugar do ícone do drawer. */
    private boolean ehTelaFilha(Fragment f) {
        return f instanceof PerfilPetFragment || f instanceof NotificacoesFragment;
    }

    /** Título conforme a tela atual (corrige o título ao voltar). */
    private String tituloDe(Fragment f) {
        if (f instanceof HomeFragment) return "Tela Inicial";
        if (f instanceof AgendaFragment) return "Agenda";
        if (f instanceof VacinaFragment) return "Vacinas e Imunização";
        if (f instanceof PetsFragment) return "Pets";
        if (f instanceof PerfilPetFragment) return "Perfil do Pet";
        if (f instanceof NotificacoesFragment) return "Notificações";
        if (f instanceof PerfilUsuarioFragment) return "Perfil do Usuário";
        if (f instanceof SolicitacoesVinculoFragment) return "Solicitações de Vínculo";
        if (f instanceof PreferenciasNotificacaoFragment) return "Preferências de notificação";
        return null;
    }

    private void atualizarNavegacaoToolbar() {
        if (toolbar == null || setaMenu == null) return;
        Fragment atual = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        String titulo = tituloDe(atual);
        if (titulo != null) toolbar.setTitle(titulo);

        if (atual != null && ehTelaFilha(atual)
                && getSupportFragmentManager().getBackStackEntryCount() > 0) {
            toolbar.setNavigationIcon(R.drawable.baseline_arrow_back);
            toolbar.setNavigationIconTint(Color.parseColor("#000000"));
            toolbar.setNavigationContentDescription("Voltar");
            toolbar.setNavigationOnClickListener(v -> getSupportFragmentManager().popBackStack());
        } else {
            toolbar.setNavigationIcon(setaMenu);
            toolbar.setNavigationContentDescription("Menu");
            toolbar.setNavigationOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }
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