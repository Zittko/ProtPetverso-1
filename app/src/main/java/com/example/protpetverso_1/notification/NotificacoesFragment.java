package com.example.protpetverso_1.notification;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.protpetverso_1.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

/** Tela do sino: histórico local de notificações. Funciona sem internet. */
public class NotificacoesFragment extends Fragment implements NotificacaoAdapter.Listener {

    private NotificacaoAdapter adapter;
    private RecyclerView recycler;
    private TextView txtVazio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notificacoes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recycler = view.findViewById(R.id.recyclerNotificacoes);
        txtVazio = view.findViewById(R.id.txtSemNotificacoes);
        adapter = new NotificacaoAdapter(this);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        view.findViewById(R.id.btnMarcarLidas).setOnClickListener(v -> {
            NotificacaoStore.marcarTodasLidas(requireContext());
            recarregar();
        });
        view.findViewById(R.id.btnLimparNotificacoes).setOnClickListener(v -> confirmarLimpar());
    }

    @Override
    public void onResume() {
        super.onResume();
        titulo("Notificações");
        recarregar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        titulo("Tela Inicial");
    }

    private void recarregar() {
        if (!isAdded()) return;
        List<NotificacaoLocal> lista = NotificacaoStore.listar(requireContext());
        adapter.setLista(lista);
        txtVazio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        recycler.setVisibility(lista.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(NotificacaoLocal n) {
        if (!n.isLida()) {
            NotificacaoStore.marcarLida(requireContext(), n.getId());
            recarregar();
        }
    }

    private void confirmarLimpar() {
        if (NotificacaoStore.listar(requireContext()).isEmpty()) return;
        new AlertDialog.Builder(requireContext(), R.style.DialogPetVerso)
                .setTitle("Limpar notificações")
                .setMessage("Deseja apagar todas as notificações da lista? Essa ação não pode ser desfeita.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Limpar", (d, w) -> {
                    NotificacaoStore.limpar(requireContext());
                    recarregar();
                })
                .show();
    }

    private void titulo(String t) {
        if (getActivity() == null) return;
        MaterialToolbar tb = getActivity().findViewById(R.id.toolbarMenu);
        if (tb != null) tb.setTitle(t);
    }
}
