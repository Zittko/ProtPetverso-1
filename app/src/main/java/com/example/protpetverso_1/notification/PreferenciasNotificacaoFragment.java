package com.example.protpetverso_1.notification;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.protpetverso_1.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

/** Preferências de notificação (menu lateral): ligar/desligar cada lembrete recorrente. */
public class PreferenciasNotificacaoFragment extends Fragment {

    private NotificacaoPrefs prefs;
    private MaterialSwitch swAlim, swPasseio, swVacinas;
    private TextView txtAviso;
    private boolean carregando = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preferencias_notificacao, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        prefs = new NotificacaoPrefs(requireContext());

        swAlim = v.findViewById(R.id.swAlimentacao);
        swPasseio = v.findViewById(R.id.swPasseio);
        swVacinas = v.findViewById(R.id.swVacinas);
        txtAviso = v.findViewById(R.id.txtAvisoPermissao);

        for (MaterialSwitch s : new MaterialSwitch[]{swAlim, swPasseio, swVacinas}) estilizar(s);

        swAlim.setOnCheckedChangeListener((b, on) -> { if (!carregando) { prefs.alimentacao(on); reagendar(); atualizar(); } });
        swPasseio.setOnCheckedChangeListener((b, on) -> { if (!carregando) { prefs.passeio(on); reagendar(); atualizar(); } });
        swVacinas.setOnCheckedChangeListener((b, on) -> { if (!carregando) { prefs.vacinas(on); reagendar(); atualizar(); } });


        v.findViewById(R.id.btnTestarNotificacao).setOnClickListener(x -> testar());
        txtAviso.setOnClickListener(x -> abrirConfiguracoesDoSistema());
    }

    @Override
    public void onResume() {
        super.onResume();
        titulo("Preferências de notificação");
        atualizar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        titulo("Tela Inicial");
    }

    private void atualizar() {
        if (!isAdded()) return;
        carregando = true;
        swAlim.setChecked(prefs.alimentacaoAtiva());
        swPasseio.setChecked(prefs.passeioAtivo());
        swVacinas.setChecked(prefs.vacinasAtivo());
        carregando = false;


        txtAviso.setVisibility(NotificacaoHelper.podeNotificar(requireContext()) ? View.GONE : View.VISIBLE);
    }

    private void reagendar() {
        NotificacaoScheduler.agendarTodos(requireContext(), true);
    }

    private void testar() {
        NotificacaoHelper.criarCanais(requireContext());
        NotificacaoHelper.notificarAlimentacao(requireContext());
        NotificacaoHelper.notificarPasseio(requireContext());
        // Usa o nome de um pet do usuário (se houver) para o teste ficar igual à notificação real
        String nomePet = "Pet de teste";
        long idPet = -1;
        java.util.List<com.example.protpetverso_1.pet.PetResumo> pets =
                com.example.protpetverso_1.data.AppDataCache.get().getMeusPets();
        if (pets != null && !pets.isEmpty() && pets.get(0).getNome() != null
                && !pets.get(0).getNome().trim().isEmpty()) {
            nomePet = pets.get(0).getNome();
            idPet = pets.get(0).getIdPet();
        }
        NotificacaoHelper.notificarVacinas(requireContext(), nomePet, idPet, NotificacaoPrefs.VACINAS_LIMITE + 1);
        if (NotificacaoHelper.podeNotificar(requireContext())) {
            Toast.makeText(requireContext(), "Notificações de teste enviadas (alimentação, passeio e vacinas).", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(),
                    "Registrada na lista, mas o aparelho está bloqueando as notificações.", Toast.LENGTH_LONG).show();
        }
    }

    private void abrirConfiguracoesDoSistema() {
        Intent i = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
        try {
            startActivity(i);
        } catch (Exception e) {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + requireContext().getPackageName())));
        }
    }

    private static void estilizar(MaterialSwitch s) {
        int[][] st = {{android.R.attr.state_checked}, {}};
        s.setThumbTintList(new ColorStateList(st, new int[]{0xFFFFFFFF, 0xFFFFFFFF}));
        s.setTrackTintList(new ColorStateList(st, new int[]{0xFFDF945D, 0xFFBDBDBD}));
    }

    private void titulo(String t) {
        if (getActivity() == null) return;
        MaterialToolbar tb = getActivity().findViewById(R.id.toolbarMenu);
        if (tb != null) tb.setTitle(t);
    }
}