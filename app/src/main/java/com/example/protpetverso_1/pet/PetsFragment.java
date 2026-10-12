package com.example.protpetverso_1.pet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.protpetverso_1.data.DataPreloader;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aba Pets — lista meusPets + card Adicionar.
 * Cache (AppDataCache) → UI na hora → GET em background.
 */
public class PetsFragment extends Fragment implements PetListaAdapter.Listener {

    private static final String TAG = "PETS_LISTA";

    private SessionManager sessionManager;
    private PetListaAdapter adapter;

    public PetsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        RecyclerView recycler = view.findViewById(R.id.recyclerPets);
        // Ajuste o id se no seu XML for outro (ex.: recyclerViewPets)
        if (recycler == null) {
            Log.e(TAG, "RecyclerView não encontrado no layout");
            return;
        }

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PetListaAdapter(this);
        recycler.setAdapter(adapter);

        // 1) Cache na hora (vindo do DataPreloader na MenuActivity)
        if (AppDataCache.get().temMeusPets()) {
            adapter.setItens(AppDataCache.get().getMeusPets());
        }

        // 2) Atualiza da API e grava no cache
        carregarMeusPets();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ao voltar (ex.: após cadastrar pet), mostra o que já tem e refresca
        if (AppDataCache.get().temMeusPets() && adapter != null) {
            adapter.setItens(AppDataCache.get().getMeusPets());
        }
        carregarMeusPets();
    }

    /** GET /api/pets/meusPets → AppDataCache + adapter */
    private void carregarMeusPets() {
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Sessão expirada.", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                ApiConfig.URL_MEUS_PETS,
                null,
                response -> {
                    List<PetResumo> lista = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject o = response.getJSONObject(i);
                            long id = o.optLong("idPet", o.optLong("id", -1));
                            String nome = o.optString("nome", "Pet");
                            String papel = o.optString("papel", "");
                            String foto = o.optString("fotoPetBase64", "");
                            if (foto.isEmpty()) {
                                foto = o.optString("fotoBase64", "");
                            }
                            if (id > 0) {
                                lista.add(new PetResumo(id, nome, papel, foto));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "item", e);
                        }
                    }

                    AppDataCache.get().setMeusPets(lista);

                    if (isAdded() && adapter != null) {
                        adapter.setItens(lista);
                    }
                },
                error -> {
                    // Só avisa se não houver nada em cache
                    if (!AppDataCache.get().temMeusPets() && isAdded()) {
                        Toast.makeText(requireContext(), "Erro ao carregar pets.", Toast.LENGTH_SHORT).show();
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

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    @Override
    public void onPetClick(PetResumo pet) {
        if (pet == null || pet.getIdPet() <= 0) {
            Toast.makeText(requireContext(), "Pet inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Define pet selecionado na sessão
        sessionManager.salvarPet(pet.getIdPet(), pet.getNome(), "", "", "", "", "", "");

        // Pré-carrega perfil + tarefas desse pet no cache
        DataPreloader.preloadPetSelecionado(requireContext(), pet.getIdPet());

        PerfilPetFragment fragment = new PerfilPetFragment();
        Bundle args = new Bundle();
        args.putLong("PET_ID", pet.getIdPet());
        fragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onAdicionarClick() {
        startActivity(new Intent(requireContext(), EscolhaPetActivity.class));
    }
}