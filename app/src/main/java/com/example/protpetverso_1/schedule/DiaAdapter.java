package com.example.protpetverso_1.schedule;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.protpetverso_1.R;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DiaAdapter extends RecyclerView.Adapter<DiaAdapter.VH> {

    public interface OnDiaClickListener {
        void onDiaClick(LocalDate data);
    }

    private final List<LocalDate> dias = new ArrayList<>();
    private LocalDate selecionado = LocalDate.now();
    private OnDiaClickListener listener;
    private boolean modoGrade = false;
    private YearMonth mesReferencia = YearMonth.now();
    /** Largura de cada chip na semana (para caberem 5 na tela). */
    private int larguraSemanaPx = 0;

    public void setOnDiaClickListener(OnDiaClickListener listener) {
        this.listener = listener;
    }

    public void setModoGrade(boolean grade) {
        this.modoGrade = grade;
        notifyDataSetChanged();
    }

    public void setMesReferencia(YearMonth mes) {
        this.mesReferencia = mes != null ? mes : YearMonth.now();
        notifyDataSetChanged();
    }

    public void setLarguraItemSemana(int px) {
        this.larguraSemanaPx = px;
        notifyDataSetChanged();
    }

    public void setDias(List<LocalDate> lista) {
        dias.clear();
        if (lista != null) dias.addAll(lista);
        notifyDataSetChanged();
    }

    public void setDiaSelecionado(LocalDate data) {
        this.selecionado = data;
        notifyDataSetChanged();
    }

    public LocalDate getDiaSelecionado() {
        return selecionado;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_data_agenda, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        LocalDate d = dias.get(position);
        Locale pt = new Locale("pt", "BR");

        String nome = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, pt).toUpperCase(pt);
        if (nome.endsWith(".")) {
            nome = nome.substring(0, nome.length() - 1);
        }

        h.txtNome.setText(nome);
        h.txtNumero.setText(String.valueOf(d.getDayOfMonth()));

        boolean sel = selecionado != null && selecionado.equals(d);
        h.container.setBackgroundResource(
                sel ? R.drawable.bg_dia_selecionado : R.drawable.bg_dia_normal);

        boolean outroMes = modoGrade
                && mesReferencia != null
                && !YearMonth.from(d).equals(mesReferencia);

        float alpha = 1f;
        if (outroMes) {
            alpha = sel ? 0.75f : 0.35f;
        }
        h.itemView.setAlpha(alpha);

        float density = h.itemView.getResources().getDisplayMetrics().density;
        ViewGroup.MarginLayoutParams lp =
                (ViewGroup.MarginLayoutParams) h.itemView.getLayoutParams();

        if (lp != null) {
            if (modoGrade) {
                int m = (int) (3 * density);
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                lp.setMargins(m, m, m, m);
                h.itemView.setLayoutParams(lp);

                h.itemView.post(() -> {
                    int w = h.itemView.getWidth();
                    if (w > 0) {
                        ViewGroup.LayoutParams p = h.itemView.getLayoutParams();
                        if (p.height != w) {
                            p.height = w;
                            h.itemView.setLayoutParams(p);
                        }
                    }
                });
            } else {
                int m = (int) (3 * density);
                int size = larguraSemanaPx > 0
                        ? larguraSemanaPx
                        : (int) (56 * density);
                lp.width = size;
                lp.height = size;
                lp.setMargins(m, 0, m, 0);
                h.itemView.setLayoutParams(lp);
            }
        }

        h.itemView.setOnClickListener(v -> {
            selecionado = d;
            notifyDataSetChanged();
            if (listener != null) listener.onDiaClick(d);
        });
    }

    @Override
    public int getItemCount() {
        return dias.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        LinearLayout container;
        TextView txtNome, txtNumero;

        VH(@NonNull View v) {
            super(v);
            container = v.findViewById(R.id.containerDia);
            txtNome = v.findViewById(R.id.txtNomeDia);
            txtNumero = v.findViewById(R.id.txtNumeroDia);
        }
    }
}