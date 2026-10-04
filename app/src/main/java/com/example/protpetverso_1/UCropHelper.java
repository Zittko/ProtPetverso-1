package com.example.protpetverso_1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;

import androidx.core.content.ContextCompat;

import com.yalantis.ucrop.UCrop;

import java.io.File;

/**
 * Configuração padrão do uCrop para todas as telas do app
 * (cadastro de usuário, cadastro de pet, perfil do usuário e perfil do pet).
 *
 * - Recorte quadrado 1:1
 * - Toolbar legível (fundo preto, ícones brancos)
 * - Usa CenterTitleUCropActivity para centralizar o título
 *
 * O tema Theme.PetVerso.UCrop no Manifest ajuda a não colar
 * os botões na área da câmera (notch).
 */
public final class UCropHelper {

    /** Impede criar instância desta classe utilitária. */
    private UCropHelper() {
    }

    /**
     * Monta o Intent do uCrop pronto para ser lançado com ActivityResultLauncher.
     *
     * @param context Activity ou requireContext() do Fragment
     * @param origem  URI da imagem escolhida na galeria
     * @param titulo  Texto da toolbar (ex.: "Ajustar foto", "Ajustar foto do pet")
     * @return Intent configurado para a tela de recorte
     */
    public static Intent criarIntentRecorte(Context context, Uri origem, String titulo) {
        // Arquivo temporário onde o uCrop salva a imagem recortada
        Uri destino = Uri.fromFile(new File(
                context.getCacheDir(),
                "crop_" + System.currentTimeMillis() + ".jpg"
        ));

        UCrop.Options options = new UCrop.Options();

        // Formato e qualidade da imagem final
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(80);

        // Título da barra superior
        options.setToolbarTitle(titulo != null ? titulo : "Ajustar foto");

        // Mantém proporção fixa (não libera recorte livre)
        options.setFreeStyleCropEnabled(false);

        // Cores da toolbar e dos controles
        options.setToolbarColor(Color.BLACK);
        options.setStatusBarColor(Color.BLACK);
        options.setToolbarWidgetColor(Color.WHITE);

        try {
            // Cor laranja do app nos controles inferiores (grid / rotação)
            options.setActiveControlsWidgetColor(
                    ContextCompat.getColor(context, R.color.primary_orange)
            );
        } catch (Exception e) {
            // Fallback se a cor não existir no projeto
            options.setActiveControlsWidgetColor(Color.parseColor("#FF9800"));
        }

        options.setHideBottomControls(false);
        options.setShowCropGrid(true);
        options.setShowCropFrame(true);

        // Monta o Intent padrão do uCrop
        Intent intent = UCrop.of(origem, destino)
                .withAspectRatio(1, 1)
                .withMaxResultSize(800, 800)
                .withOptions(options)
                .getIntent(context);

        // Troca a Activity padrão pela nossa (título centralizado)
        // É preciso ter CenterTitleUCropActivity no Manifest
        intent.setClass(context, CenterTitleUCropActivity.class);

        return intent;
    }
}