package com.example.protpetverso_1;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.menu_layout);
        MaterialToolbar toolbar = findViewById(R.id.toolbarMenu);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavbar);

        ViewPager2 viewPagerPets = findViewById(R.id.viewPagerPets);
        ImageButton btnAnterior = findViewById(R.id.btnAnterior);
        ImageButton btnProximo = findViewById(R.id.btnProximo);

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        List<Pet> listaPets = new ArrayList<>();
        listaPets.add(new Pet("Thor", R.drawable.petversologo));
        listaPets.add(new Pet("Luna", R.drawable.petversologo));
        listaPets.add(new Pet("Mel", R.drawable.petversologo));
        listaPets.add(new Pet("Bob", R.drawable.petversologo));

        PetAdapter adapter = new PetAdapter(listaPets);
        viewPagerPets.setAdapter(adapter);

        btnProximo.setOnClickListener(v -> {
            int atual = viewPagerPets.getCurrentItem();
            int proximo = (atual + 1) % listaPets.size();
            viewPagerPets.setCurrentItem(proximo, true);
        });

        btnAnterior.setOnClickListener(v -> {
            int atual = viewPagerPets.getCurrentItem();
            int anterior = (atual - 1 + listaPets.size()) % listaPets.size();
            viewPagerPets.setCurrentItem(anterior, true);
        });
    }
}