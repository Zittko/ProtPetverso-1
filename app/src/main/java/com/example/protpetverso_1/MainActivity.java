package com.example.protpetverso_1;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<ComponentesEletronicos> LstCompele;
    RecyclerView idRecCompEle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginScroll), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        idRecCompEle = findViewById(R.id.idRecCompEle);

        LstCompele = new ArrayList<>();

        LstCompele.add(new ComponentesEletronicos("Indutor", "Um indutor é um dispositivo elétrico passivo que armazena energia na forma de campo magnético, normalmente combinando o efeito de vários loops da corrente elétrica.", "Preço: R$2,00", R.drawable.indutor));
        LstCompele.add(new ComponentesEletronicos("Indutor", "Um indutor é um dispositivo elétrico passivo que armazena energia na forma de campo magnético, normalmente combinando o efeito de vários loops da corrente elétrica.", "Preço: R$2,00", R.drawable.indutor));

        AdapterCompele adapterCompele = new AdapterCompele(getApplicationContext(), LstCompele);
        idRecCompEle.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));

        idRecCompEle.setAdapter(adapterCompele);
    }
}