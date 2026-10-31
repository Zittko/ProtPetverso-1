package com.example.protpetverso_1.user;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.protpetverso_1.ApiConfig;
import com.example.protpetverso_1.R;
import com.example.protpetverso_1.SessionManager;
import com.example.protpetverso_1.UCropHelper;
import com.example.protpetverso_1.VolleySingleton;
import com.example.protpetverso_1.data.AppDataCache;
import com.example.protpetverso_1.data.PhotoDiskCache;
import com.google.android.material.appbar.MaterialToolbar;
import com.yalantis.ucrop.UCrop;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Perfil do Usuário — cache disco/memória → UI → GET API.
 * Lápis: abre EditarUsuarioActivity.
 */
public class PerfilUsuarioFragment extends Fragment {

    private SessionManager sessionManager;

    private ImageView imgFotoPerfil;
    private TextView txtUsername;
    private TextView txtNome;
    private TextView txtEmail;
    private TextView txtTelefone;
    private ImageButton btnEditarPerfil;

    private String fotoBase64Atual;
    private String apelidoAtual = "";

    private final ActivityResultLauncher<String> selecionarFoto =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null || getContext() == null) return;
                iniciarRecorte(uri);
            });

    private final ActivityResultLauncher<Intent> recortarFoto =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (!isAdded()) return;
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    final Uri resultUri = UCrop.getOutput(result.getData());
                    if (resultUri != null) {
                        processarFotoRecortada(resultUri);
                    }
                } else if (result.getResultCode() == UCrop.RESULT_ERROR && result.getData() != null) {
                    Throwable cropError = UCrop.getError(result.getData());
                    Toast.makeText(requireContext(),
                            "Erro ao recortar: " + (cropError != null ? cropError.getMessage() : ""),
                            Toast.LENGTH_SHORT).show();
                }
            });

    public PerfilUsuarioFragment() {}

    private boolean podeAtualizarUi() {
        return isAdded() && getContext() != null && getView() != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil_usuario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        atualizarTituloToolbar("Perfil do Usuário");

        imgFotoPerfil = view.findViewById(R.id.imgFotoPerfil);
        txtUsername = view.findViewById(R.id.txtUsername);
        txtNome = view.findViewById(R.id.txtNome);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtTelefone = view.findViewById(R.id.txtTelefone);
        btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);

        if (imgFotoPerfil != null) {
            imgFotoPerfil.setClickable(true);
            imgFotoPerfil.setFocusable(true);
            imgFotoPerfil.setOnClickListener(v -> selecionarFoto.launch("image/*"));
        }
        if (btnEditarPerfil != null) {
            btnEditarPerfil.setOnClickListener(v -> {
                Intent i = new Intent(requireContext(), EditarUsuarioActivity.class);
                i.putExtra("NOME", textoDe(txtNome));
                i.putExtra("EMAIL", textoDe(txtEmail));
                i.putExtra("TELEFONE", textoDe(txtTelefone));
                i.putExtra("APELIDO", textoDe(txtUsername));
                startActivity(i);
            });
        }

        if (AppDataCache.get().temUsuario()) {
            aplicarCacheUsuario();
        } else {
            preencherComSessaoLocal();
        }
        buscarUsuarioNaApi();
    }

    @Override
    public void onResume() {
        super.onResume();
        atualizarTituloToolbar("Perfil do Usuário");
        if (sessionManager != null) {
            buscarUsuarioNaApi();
        }
    }

    private String textoDe(TextView tv) {
        if (tv == null || tv.getText() == null) return "";
        String s = tv.getText().toString().trim();
        return "—".equals(s) ? "" : s;
    }

    private void aplicarCacheUsuario() {
        apelidoAtual = valorOuPadrao(AppDataCache.get().getUsuarioApelido(), "usuário");
        preencherCampos(
                apelidoAtual,
                AppDataCache.get().getUsuarioNome(),
                AppDataCache.get().getUsuarioEmail(),
                valorOuPadrao(AppDataCache.get().getUsuarioTelefone(), "—")
        );
        fotoBase64Atual = AppDataCache.get().getUsuarioFotoBase64();
        aplicarFotoUsuario();
    }

    private void aplicarFotoUsuario() {
        if (imgFotoPerfil == null) return;
        Bitmap bmp = PhotoDiskCache.carregarBitmap(AppDataCache.get().getUsuarioFotoPath());
        if (bmp != null) {
            imgFotoPerfil.setImageBitmap(bmp);
            imgFotoPerfil.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return;
        }
        String b64 = AppDataCache.get().getUsuarioFotoBase64();
        if (b64 == null || b64.isEmpty()) b64 = fotoBase64Atual;
        mostrarFotoBase64(b64, imgFotoPerfil);
    }

    private void iniciarRecorte(Uri origem) {
        if (getContext() == null) return;
        try {
            Intent intent = UCropHelper.criarIntentRecorte(
                    requireContext(),
                    origem,
                    "Ajustar foto",
                    1f,
                    1f,
                    true
            );
            recortarFoto.launch(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Não foi possível abrir o recorte.", Toast.LENGTH_SHORT).show();
        }
    }

    private void processarFotoRecortada(Uri uri) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(
                    requireContext().getContentResolver().openInputStream(uri));
            if (bitmap == null) {
                Toast.makeText(requireContext(), "Erro ao processar foto", Toast.LENGTH_SHORT).show();
                return;
            }
            fotoBase64Atual = bitmapParaBase64(bitmap);
            if (imgFotoPerfil != null) {
                imgFotoPerfil.setImageBitmap(bitmap);
                imgFotoPerfil.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            enviarFotoUsuario();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Erro ao processar foto", Toast.LENGTH_SHORT).show();
        }
    }

    private void buscarUsuarioNaApi() {
        if (sessionManager == null || getContext() == null) return;
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) return;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.URL_USUARIO_PERFIL,
                null,
                response -> {
                    AppDataCache.get().setUsuarioPerfil(response);
                    if (!podeAtualizarUi()) return;

                    String nome = response.optString("nome", "");
                    String email = response.optString("email", "");
                    String telefone = response.optString("telefone", "—");
                    apelidoAtual = response.optString("apelido", "");
                    if (apelidoAtual.isEmpty()) apelidoAtual = "usuário";
                    fotoBase64Atual = response.optString("fotoBase64", "");

                    preencherCampos(apelidoAtual, nome, email, telefone);
                    aplicarFotoUsuario();
                },
                error -> {
                    if (!podeAtualizarUi()) return;
                    if (!AppDataCache.get().temUsuario()) {
                        Toast.makeText(requireContext(), "Erro ao carregar perfil.", Toast.LENGTH_SHORT).show();
                        preencherComSessaoLocal();
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

    private void enviarFotoUsuario() {
        if (sessionManager == null) return;
        String token = sessionManager.obterToken();
        if (token == null || token.isEmpty()) return;
        if (fotoBase64Atual == null || fotoBase64Atual.isEmpty()) {
            Toast.makeText(requireContext(), "Nenhuma foto selecionada.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONObject body = new JSONObject();
            body.put("fotoBase64", fotoBase64Atual);
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    ApiConfig.URL_ATUALIZAR_PERFIL,
                    body,
                    response -> {
                        AppDataCache.get().setUsuarioPerfil(response);
                        if (!podeAtualizarUi()) return;
                        Toast.makeText(requireContext(), "Foto atualizada!", Toast.LENGTH_SHORT).show();
                        fotoBase64Atual = response.optString("fotoBase64", fotoBase64Atual);
                        aplicarFotoUsuario();
                    },
                    error -> {
                        if (podeAtualizarUi()) {
                            Toast.makeText(requireContext(), "Erro ao enviar foto.", Toast.LENGTH_SHORT).show();
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> h = new HashMap<>();
                    h.put("Authorization", "Bearer " + token);
                    h.put("Content-Type", "application/json");
                    return h;
                }
            };
            VolleySingleton.getInstance(requireContext().getApplicationContext()).addToRequestQueue(request);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Erro ao montar envio da foto.", Toast.LENGTH_SHORT).show();
        }
    }

    private void preencherComSessaoLocal() {
        if (sessionManager == null) return;
        apelidoAtual = valorOuPadrao(sessionManager.obterApelido(), "usuário");
        preencherCampos(
                apelidoAtual,
                valorOuPadrao(sessionManager.obterNome(), ""),
                valorOuPadrao(sessionManager.obterEmail(), ""),
                valorOuPadrao(sessionManager.obterTelefone(), "—")
        );
        aplicarFotoUsuario();
    }

    private void preencherCampos(String username, String nome, String email, String telefone) {
        if (txtUsername != null) txtUsername.setText(username);
        if (txtNome != null) txtNome.setText(nome);
        if (txtEmail != null) txtEmail.setText(email);
        if (txtTelefone != null) txtTelefone.setText(telefone);
    }

    private String valorOuPadrao(String valor, String padrao) {
        return (valor == null || valor.isEmpty()) ? padrao : valor;
    }

    private String bitmapParaBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
    }

    private void mostrarFotoBase64(String base64, ImageView imageView) {
        if (base64 == null || base64.isEmpty() || imageView == null) return;
        try {
            if (base64.contains(",")) {
                base64 = base64.substring(base64.indexOf(",") + 1);
            }
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bmp != null) {
                imageView.setImageBitmap(bmp);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void atualizarTituloToolbar(String titulo) {
        if (getActivity() == null) return;
        MaterialToolbar toolbar = getActivity().findViewById(R.id.toolbarMenu);
        if (toolbar != null) toolbar.setTitle(titulo);
    }
}