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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aba Pets — lista GET /api/pets/meusPets + card Adicionar.
 * Clique no pet → seleciona e abre PerfilPetFragment.
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
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PetListaAdapter(this);
        recycler.setAdapter(adapter);

        carregarMeusPets();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sessionManager != null) {
            carregarMeusPets();
        }
    }

    /**
     * GET /api/pets/meusPets
     * JSON: idPet, nome, papel, fotoPetBase64
     */
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
                    Log.d(TAG, "meusPets qtd=" + response.length());
                    List<PetResumo> lista = new ArrayList<>();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject o = response.getJSONObject(i);

                            long id = o.optLong("idPet", -1);
                            if (id <= 0) {
                                id = o.optLong("id", -1);
                            }

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
                            Log.e(TAG, "Erro item " + i, e);
                        }
                    }

                    adapter.setItens(lista);
                },
                error -> {
                    String msg = "Erro ao carregar pets.";
                    if (error.networkResponse != null) {
                        msg += " (" + error.networkResponse.statusCode + ")";
                        Log.e(TAG, "Erro GET meusPets: " + error.networkResponse.statusCode);
                    }
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
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

    /** Seleciona o pet e abre o perfil com o id correto. */
    @Override
    public void onPetClick(PetResumo pet) {
        if (pet == null || pet.getIdPet() <= 0) {
            Toast.makeText(requireContext(), "Pet inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.salvarPet(
                pet.getIdPet(),
                pet.getNome(),
                "", "", "", "", "", ""
        );

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

    /** Cadastrar novo ou vincular por código. */
    @Override
    public void onAdicionarClick() {
        startActivity(new Intent(requireContext(), EscolhaPetActivity.class));
    }
}