package com.example.protpetverso_1;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.yalantis.ucrop.UCropActivity;

/**
 * UCrop com título da toolbar centralizado.
 */
public class CenterTitleUCropActivity extends UCropActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        centralizarTituloToolbar();
    }

    private void centralizarTituloToolbar() {
        Toolbar toolbar = findViewById(com.yalantis.ucrop.R.id.toolbar);
        if (toolbar == null) return;

        // Procura o TextView do título dentro da toolbar
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof TextView) {
                TextView title = (TextView) child;
                Toolbar.LayoutParams lp = new Toolbar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                lp.gravity = Gravity.CENTER;
                title.setLayoutParams(lp);
                title.setGravity(Gravity.CENTER);
                break;
            }
        }
    }
}