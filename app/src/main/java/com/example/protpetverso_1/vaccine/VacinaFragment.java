package com.example.protpetverso_1.vaccine;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.VolleySingleton;
import com.example.protpetverso_1.data.AppDataCache;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VacinaFragment extends Fragment implements VacinaAdapter.Listener {

    private static final String TAG = "VACINA";

    private SessionManager sessionManager;
    private long petId = -1;
    private boolean isDono = true;

    private RecyclerView recyclerVacinas;
    private TextView txtSemVacinas;
    private MaterialButton btnAbaHistorico, btnAbaProximas;
    private FloatingActionButton fabNovaVacina;
    private VacinaAdapter adapter;

    private final List<Vacina> todasVacinas = new ArrayList<>();
    private boolean abaHistorico = true;

    public VacinaFragment() {}

    private boolean podeAtualizarUi() {
        return isAdded() && getContext() != null && getView() != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vacina, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        petId = sessionManager.obterPetId();
        isDono = sessionManager.isDonoDoPetSelecionado();

        atualizarTituloToolbar("Vacinas e Imunização");

        recyclerVacinas = view.findViewById(R.id.recyclerVacinas);
        txtSemVacinas = view.findViewById(R.id.txtSemVacinas);
        btnAbaHistorico = view.findViewById(R.id.btnAbaHistorico);
        btnAbaProximas = view.findViewById(R.id.btnAbaProximas);
        fabNovaVacina = view.findViewById(R.id.fabNovaVacina);

        adapter = new VacinaAdapter(this);
        if (recyclerVacinas != null) {
            recyclerVacinas.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerVacinas.setAdapter(adapter);
        }

        if (btnAbaHistorico != null) {
            btnAbaHistorico.setOnClickListener(v -> {
                abaHistorico = true;
                atualizarEstiloAbas();
                filtrarEExibir();
            });
        }
        if (btnAbaProximas != null) {
            btnAbaProximas.setOnClickListener(v -> {
                abaHistorico = false;
                atualizarEstiloAbas();
                filtrarEExibir();
            });
        }

        if (fabNovaVacina != null) {
            if (!isDono) {
                fabNovaVacina.setVisibility(View.GONE);
            } else {
                fabNovaVacina.setOnClickListener(v -> abrirCadastro(null));
            }
        }

        atualizarEstiloAbas();

        if (petId > 0 && AppDataCache.get().temVacinas(petId)) {
            todasVacinas.clear();
            todasVacinas.addAll(AppDataCache.get().getVacinas(petId));
            filtrarEExibir();
        }
        carregarVacinasDaApi();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sessionManager == null) return;
        long atual = sessionManager.obterPetId();
        if (atual > 0 && atual != petId) {
            petId = atual;
            isDono = sessionManager.isDonoDoPetSelecionado();
            if (fabNovaVacina != null) {
                fabNovaVacina.setVisibility(isDono ? View.VISIBLE : View.GONE);
            }
        }
        if (petId > 0 && AppDataCache.get().temVacinas(petId)) {
            todasVacinas.clear();
            todasVacinas.addAll(AppDataCache.get().getVacinas(petId));
            filtrarEExibir();
        }
        carregarVacinasDaApi();
    }

    private void atualizarEstiloAbas() {
        if (btnAbaHistorico != null) btnAbaHistorico.setAlpha(abaHistorico ? 1f : 0.55f);
        if (btnAbaProximas != null) btnAbaProximas.setAlpha(abaHistorico ? 0.55f : 1f);
    }

    private void carregarVacinasDaApi() {
        if (petId <= 0) {
            if (txtSemVacinas != null) {
                txtSemVacinas.setText("Selecione um pet para ver as vacinas.");
                txtSemVacinas.setVisibility(View.VISIBLE);
            }
            if (recyclerVacinas != null) recyclerVacinas.setVisibility(View.GONE);
            return;
        }

        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) return;

        String url = ApiConfig.URL_VACINAS_DO_PET + petId + "/vacinas";
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<Vacina> lista = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            lista.add(Vacina.fromJson(response.getJSONObject(i)));
                        } catch (Exception e) {
                            Log.e(TAG, "parse vacina", e);
                        }
                    }
                    AppDataCache.get().setVacinas(petId, lista);
                    if (!podeAtualizarUi()) return;
                    todasVacinas.clear();
                    todasVacinas.addAll(lista);
                    filtrarEExibir();
                },
                error -> {
                    Log.e(TAG, "listar vacinas", error);
                    if (!podeAtualizarUi()) return;
                    if (todasVacinas.isEmpty()) {
                        Toast.makeText(requireContext(), "Erro ao carregar vacinas.", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Authorization", "Bearer " + token);
                return h;
            }
        };
        VolleySingleton.getInstance(requireContext().getApplicationContext()).addToRequestQueue(request);
    }

    private void filtrarEExibir() {
        List<Vacina> exibir;
        if (abaHistorico) {
            exibir = new ArrayList<>(todasVacinas);
        } else {
            exibir = todasVacinas.stream()
                    .filter(v -> v.getDataProximaDose() != null)
                    .sorted(Comparator.comparing(Vacina::getDataProximaDose,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        }
        if (adapter != null) adapter.setItens(exibir);
        boolean vazio = exibir.isEmpty();
        if (recyclerVacinas != null) recyclerVacinas.setVisibility(vazio ? View.GONE : View.VISIBLE);
        if (txtSemVacinas != null) {
            txtSemVacinas.setVisibility(vazio ? View.VISIBLE : View.GONE);
            txtSemVacinas.setText(abaHistorico
                    ? "Nenhuma vacina cadastrada."
                    : "Nenhuma dose próxima cadastrada.");
        }
    }

    @Override
    public void onVacinaClick(Vacina vacina) {
        if (!isDono) {
            Toast.makeText(requireContext(),
                    "Apenas o tutor principal pode editar vacinas.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (vacina == null || vacina.getIdVacina() <= 0) {
            Toast.makeText(requireContext(),
                    "Não foi possível identificar a vacina para edição.", Toast.LENGTH_SHORT).show();
            return;
        }
        abrirCadastro(vacina);
    }

    private void abrirCadastro(@Nullable Vacina editar) {
        Intent i = new Intent(requireContext(), CadastrarVacinaActivity.class);
        i.putExtra(CadastrarVacinaActivity.EXTRA_PET_ID, petId);

        if (editar != null && editar.getIdVacina() > 0) {
            i.putExtra(CadastrarVacinaActivity.EXTRA_MODO_EDICAO, true);
            // Chave oficial
            i.putExtra(CadastrarVacinaActivity.EXTRA_ID_VACINA, editar.getIdVacina());
            // Compatibilidade com código antigo da Cadu
            i.putExtra("VACINA_ID", editar.getIdVacina());

            i.putExtra(CadastrarVacinaActivity.EXTRA_NOME, editar.getNome());
            if (editar.getDataAplicacao() != null) {
                i.putExtra(CadastrarVacinaActivity.EXTRA_DATA, editar.getDataAplicacao().toString());
            }
            if (editar.getDataProximaDose() != null) {
                i.putExtra(CadastrarVacinaActivity.EXTRA_DATA_PROXIMA, editar.getDataProximaDose().toString());
            }
            i.putExtra(CadastrarVacinaActivity.EXTRA_OBS, editar.getObservacao());
            i.putExtra(CadastrarVacinaActivity.EXTRA_DOSE_UNICA, editar.isDosagemUnica());
            if (editar.getIntervaloQuantidade() != null) {
                i.putExtra(CadastrarVacinaActivity.EXTRA_INTERVALO_QTD, editar.getIntervaloQuantidade());
            }
            if (editar.getIntervaloUnidade() != null) {
                i.putExtra(CadastrarVacinaActivity.EXTRA_INTERVALO_UN, editar.getIntervaloUnidade());
            }
            i.putExtra(CadastrarVacinaActivity.EXTRA_STATUS, editar.getStatusVacina());
            i.putExtra(CadastrarVacinaActivity.EXTRA_PRECISA_CONFIRMAR, editar.isPendenteOuAtrasada());
        } else {
            i.putExtra(CadastrarVacinaActivity.EXTRA_MODO_EDICAO, false);
        }
        startActivity(i);
    }

    private void atualizarTituloToolbar(String titulo) {
        if (getActivity() == null) return;
        MaterialToolbar toolbar = getActivity().findViewById(R.id.toolbarMenu);
        if (toolbar != null) toolbar.setTitle(titulo);
    }
}