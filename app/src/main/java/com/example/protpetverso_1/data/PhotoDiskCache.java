package com.example.protpetverso_1.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Salva fotos de perfil (pet/usuário) em arquivos no cache do app.
 * Pasta: context.getCacheDir()/petverso_photos/
 */
public final class PhotoDiskCache {

    private static final String TAG = "PhotoDiskCache";
    private static final String DIR = "petverso_photos";

    private PhotoDiskCache() {}

    private static File dir(Context context) {
        File d = new File(context.getApplicationContext().getCacheDir(), DIR);
        if (!d.exists()) {
            // noinspection ResultOfMethodCallIgnored
            d.mkdirs();
        }
        return d;
    }

    public static File arquivoPet(Context context, long petId) {
        return new File(dir(context), "pet_" + petId + ".jpg");
    }

    public static File arquivoUsuario(Context context, long userId) {
        return new File(dir(context), "user_" + userId + ".jpg");
    }

    /**
     * Grava base64 em arquivo JPEG. Retorna o caminho absoluto ou null.
     */
    public static String salvarBase64(Context context, File destino, String base64) {
        if (context == null || destino == null || base64 == null) return null;
        String b64 = base64.trim();
        if (b64.isEmpty()) return null;
        try {
            if (b64.contains(",")) {
                b64 = b64.substring(b64.indexOf(',') + 1);
            }
            b64 = b64.replace("\n", "").replace("\r", "").replace(" ", "");
            byte[] raw = Base64.decode(b64, Base64.DEFAULT);
            if (raw == null || raw.length == 0) return null;

            Bitmap bmp = BitmapFactory.decodeByteArray(raw, 0, raw.length);
            if (bmp == null) {
                // grava bytes crus se não for bitmap decodificável
                try (FileOutputStream fos = new FileOutputStream(destino)) {
                    fos.write(raw);
                    fos.flush();
                }
                return destino.getAbsolutePath();
            }

            try (FileOutputStream fos = new FileOutputStream(destino)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                fos.flush();
            }
            if (!bmp.isRecycled()) bmp.recycle();
            return destino.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "salvarBase64 " + destino.getName(), e);
            return null;
        }
    }

    public static String salvarFotoPet(Context context, long petId, String base64) {
        if (petId <= 0) return null;
        return salvarBase64(context, arquivoPet(context, petId), base64);
    }

    public static String salvarFotoUsuario(Context context, long userId, String base64) {
        if (userId <= 0) return null;
        return salvarBase64(context, arquivoUsuario(context, userId), base64);
    }

    public static Bitmap carregarBitmap(String path) {
        if (path == null || path.trim().isEmpty()) return null;
        File f = new File(path);
        if (!f.exists() || f.length() == 0) return null;
        try {
            return BitmapFactory.decodeFile(f.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "carregarBitmap", e);
            return null;
        }
    }

    public static boolean existe(String path) {
        return path != null && new File(path).isFile() && new File(path).length() > 0;
    }

    /** Remove fotos de pets (ex.: logout). */
    public static void limparTudo(Context context) {
        if (context == null) return;
        File d = dir(context);
        File[] files = d.listFiles();
        if (files == null) return;
        for (File f : files) {
            // noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }
}