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
 * Recorte das fotos.
 * Perfil do usuário: círculo 1:1, cantos escurecidos.
 * Cadastro do pet: 12:7, igual ao card 240×140.
 * Demais telas: quadrado 1:1.
 */
public final class UCropHelper {

    private UCropHelper() {
    }

    public static Intent criarIntentRecorte(Context context, Uri origem, String titulo) {
        return criarIntentRecorte(context, origem, titulo, 1f, 1f, false);
    }

    /**
     * @param circular true escurece os cantos de fora do círculo. A proporção vira 1:1.
     */
    public static Intent criarIntentRecorte(Context context, Uri origem, String titulo,
                                            float ratioX, float ratioY, boolean circular) {
        Uri destino = Uri.fromFile(new File(
                context.getCacheDir(),
                "crop_" + System.currentTimeMillis() + ".jpg"
        ));

        if (circular || ratioX <= 0f || ratioY <= 0f) {
            ratioX = 1f;
            ratioY = 1f;
        }

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(80);
        options.setToolbarTitle(titulo != null ? titulo : "Ajustar foto");
        options.setFreeStyleCropEnabled(false);
        options.setCircleDimmedLayer(circular);

        options.setToolbarColor(Color.BLACK);
        options.setStatusBarColor(Color.BLACK);
        options.setToolbarWidgetColor(Color.WHITE);
        options.setDimmedLayerColor(Color.parseColor("#B3000000"));
        options.setCropFrameColor(Color.WHITE);
        options.setCropGridColor(Color.parseColor("#80FFFFFF"));

        try {
            options.setActiveControlsWidgetColor(
                    ContextCompat.getColor(context, R.color.primary_orange)
            );
        } catch (Exception e) {
            options.setActiveControlsWidgetColor(Color.parseColor("#FF9800"));
        }

        options.setHideBottomControls(false);
        options.setShowCropGrid(true);
        options.setShowCropFrame(!circular);

        int maxLado = 960;
        int maxW;
        int maxH;
        if (ratioX >= ratioY) {
            maxW = maxLado;
            maxH = Math.max(1, Math.round(maxLado * ratioY / ratioX));
        } else {
            maxH = maxLado;
            maxW = Math.max(1, Math.round(maxLado * ratioX / ratioY));
        }

        Intent intent = UCrop.of(origem, destino)
                .withAspectRatio(ratioX, ratioY)
                .withMaxResultSize(maxW, maxH)
                .withOptions(options)
                .getIntent(context);

        intent.setClass(context, CenterTitleUCropActivity.class);
        return intent;
    }
}